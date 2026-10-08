package com.inventory.service;

import com.inventory.dto.SignupRequest;
import com.inventory.dto.UserResponse;
import com.inventory.dto.UserUpdateRequest;
import com.inventory.entity.Role;
import com.inventory.entity.User;
import com.inventory.entity.UserStatus;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.InvalidOperationException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityLogService activityLogService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ActivityLogService activityLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogService = activityLogService;
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return auth.getName();
        }
        return "system";
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse createUser(SignupRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("User with email '" + email + "' already exists");
        }

        Role role = Role.fromString(request.getRole());
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);

        User saved = userRepository.save(user);
        activityLogService.log("USER_CREATE", "USERS", "Created user " + saved.getEmail() + " with role " + saved.getRole().name());
        return mapToUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String currentEmail = getCurrentUserEmail();
        boolean isSelf = user.getEmail().equalsIgnoreCase(currentEmail);

        if (request.getEmail() != null && !request.getEmail().trim().equalsIgnoreCase(user.getEmail())) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException("User with email '" + newEmail + "' already exists");
            }
            user.setEmail(newEmail);
        }

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        boolean roleChanged = false;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            Role newRole = Role.fromString(request.getRole());
            if (isSelf && user.getRole() == Role.ADMIN && newRole != Role.ADMIN) {
                throw new InvalidOperationException("You cannot change your own role away from ADMIN. Administrator access cannot be removed.");
            }
            if (newRole != user.getRole()) {
                roleChanged = true;
                user.setRole(newRole);
            }
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            try {
                UserStatus newStatus = UserStatus.valueOf(request.getStatus().trim().toUpperCase());
                if (isSelf && newStatus == UserStatus.INACTIVE) {
                    throw new InvalidOperationException("You cannot deactivate your own account.");
                }
                user.setStatus(newStatus);
            } catch (IllegalArgumentException ignored) {}
        }

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        User updated = userRepository.save(user);

        if (roleChanged) {
            activityLogService.log("USER_ROLE_CHANGE", "USERS", "Changed role of user " + updated.getEmail() + " to " + updated.getRole().name());
        } else {
            activityLogService.log("USER_UPDATE", "USERS", "Updated profile for user: " + updated.getEmail());
        }

        return mapToUserResponse(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String currentEmail = getCurrentUserEmail();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new InvalidOperationException("You cannot delete your own account.");
        }

        // Safety check: Do not delete the last ADMIN
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new InvalidOperationException("Cannot delete the last remaining ADMIN user");
            }
        }

        userRepository.delete(user);
        activityLogService.log("USER_DELETE", "USERS", "Deleted user: " + user.getEmail());
    }

    @Transactional
    public UserResponse activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setStatus(UserStatus.ACTIVE);
        User saved = userRepository.save(user);
        activityLogService.log("USER_ACTIVATE", "USERS", "Activated user account: " + user.getEmail());
        return mapToUserResponse(saved);
    }

    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String currentEmail = getCurrentUserEmail();
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new InvalidOperationException("You cannot deactivate your own administrator account.");
        }

        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.countByRole(Role.ADMIN);
            if (adminCount <= 1) {
                throw new InvalidOperationException("Cannot deactivate the last remaining active ADMIN user.");
            }
        }

        user.setStatus(UserStatus.INACTIVE);
        User saved = userRepository.save(user);
        activityLogService.log("USER_DEACTIVATE", "USERS", "Deactivated user account: " + user.getEmail());
        return mapToUserResponse(saved);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (newPassword == null || newPassword.trim().length() < 3) {
            throw new InvalidOperationException("Password must be at least 3 characters");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
        activityLogService.log("USER_PASSWORD_RESET", "USERS", "Reset password for user: " + user.getEmail());
    }

    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    public UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt(),
                user.getLastLogin()
        );
    }
}
