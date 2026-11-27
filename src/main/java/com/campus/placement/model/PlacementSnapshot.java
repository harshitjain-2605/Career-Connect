package com.campus.placement.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable snapshot of core placement entities used for binary backup/restore.
 */
// [Unit 3: Object Serialization/Deserialization]
public class PlacementSnapshot implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDateTime createdAt;
    private List<Company> companies = new ArrayList<>();
    private List<Student> students = new ArrayList<>();
    private List<Drive> drives = new ArrayList<>();
    private List<Offer> offers = new ArrayList<>();

    public PlacementSnapshot() {
        this.createdAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<Company> getCompanies() {
        return companies;
    }

    public void setCompanies(List<Company> companies) {
        this.companies = companies;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public List<Drive> getDrives() {
        return drives;
    }

    public void setDrives(List<Drive> drives) {
        this.drives = drives;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
}
