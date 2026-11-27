package com.campus.placement.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a campus placement drive hosted by a {@link Company}.
 * Demonstrates {@link HashSet} for allowed branches and {@link LocalDate} for dates.
 */
// [Unit 2: HashSet & LocalDate]
public class Drive extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private int companyId;
    private String companyName; // joined display field
    private String jobRole;
    private double packageLpa;
    private String location;
    private JobType jobType;
    private LocalDate driveDate;
    private LocalDate lastDate;
    private double minCgpa;
    private int maxBacklogs;
    private int batchYear;
    private DriveStatus status;
    private String description;
    private Set<String> allowedBranches = new HashSet<>();

    public Drive() {
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public double getPackageLpa() {
        return packageLpa;
    }

    public void setPackageLpa(double packageLpa) {
        this.packageLpa = packageLpa;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public LocalDate getDriveDate() {
        return driveDate;
    }

    public void setDriveDate(LocalDate driveDate) {
        this.driveDate = driveDate;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    public void setLastDate(LocalDate lastDate) {
        this.lastDate = lastDate;
    }

    public double getMinCgpa() {
        return minCgpa;
    }

    public void setMinCgpa(double minCgpa) {
        this.minCgpa = minCgpa;
    }

    public int getMaxBacklogs() {
        return maxBacklogs;
    }

    public void setMaxBacklogs(int maxBacklogs) {
        this.maxBacklogs = maxBacklogs;
    }

    public int getBatchYear() {
        return batchYear;
    }

    public void setBatchYear(int batchYear) {
        this.batchYear = batchYear;
    }

    public DriveStatus getStatus() {
        return status;
    }

    public void setStatus(DriveStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getAllowedBranches() {
        return allowedBranches;
    }

    public void setAllowedBranches(Set<String> allowedBranches) {
        this.allowedBranches = allowedBranches != null ? new HashSet<>(allowedBranches) : new HashSet<>();
    }

    @Override
    public String toString() {
        return (companyName != null ? companyName + " - " : "") + jobRole + " (" + packageLpa + " LPA)";
    }
}
