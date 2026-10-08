package com.inventory.dto;

import java.time.LocalDateTime;

public class SupplierResponse {

    private Long id;
    private String name;
    private String companyName;
    private String contactPerson;
    private String email;
    private String phone;
    private String gstNumber;
    private String address;
    private String status;
    private long suppliedProductsCount;
    private LocalDateTime createdAt;

    public SupplierResponse() {}

    public SupplierResponse(Long id, String name, String companyName, String contactPerson, String email, String phone, String gstNumber, String address, String status, long suppliedProductsCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phone = phone;
        this.gstNumber = gstNumber;
        this.address = address;
        this.status = status;
        this.suppliedProductsCount = suppliedProductsCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getSuppliedProductsCount() {
        return suppliedProductsCount;
    }

    public void setSuppliedProductsCount(long suppliedProductsCount) {
        this.suppliedProductsCount = suppliedProductsCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
