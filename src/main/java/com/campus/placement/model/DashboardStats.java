package com.campus.placement.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregated metrics for the Admin Dashboard KPI tiles and charts.
 */
// [Unit 2: HashMap / Map]
public class DashboardStats {

    private int totalStudents;
    private int placedStudents;
    private int activeDrives;
    private int totalOffers;
    private double averagePackageLpa;
    private double highestPackageLpa;
    private Map<String, Integer> placedByBranch = new LinkedHashMap<>();
    private Map<String, Integer> packageDistribution = new LinkedHashMap<>();

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getPlacedStudents() {
        return placedStudents;
    }

    public void setPlacedStudents(int placedStudents) {
        this.placedStudents = placedStudents;
    }

    public double getPlacedPercentage() {
        return totalStudents == 0 ? 0.0 : (placedStudents * 100.0) / totalStudents;
    }

    public int getActiveDrives() {
        return activeDrives;
    }

    public void setActiveDrives(int activeDrives) {
        this.activeDrives = activeDrives;
    }

    public int getTotalOffers() {
        return totalOffers;
    }

    public void setTotalOffers(int totalOffers) {
        this.totalOffers = totalOffers;
    }

    public double getAveragePackageLpa() {
        return averagePackageLpa;
    }

    public void setAveragePackageLpa(double averagePackageLpa) {
        this.averagePackageLpa = averagePackageLpa;
    }

    public double getHighestPackageLpa() {
        return highestPackageLpa;
    }

    public void setHighestPackageLpa(double highestPackageLpa) {
        this.highestPackageLpa = highestPackageLpa;
    }

    public Map<String, Integer> getPlacedByBranch() {
        return placedByBranch;
    }

    public void setPlacedByBranch(Map<String, Integer> placedByBranch) {
        this.placedByBranch = placedByBranch;
    }

    public Map<String, Integer> getPackageDistribution() {
        return packageDistribution;
    }

    public void setPackageDistribution(Map<String, Integer> packageDistribution) {
        this.packageDistribution = packageDistribution;
    }
}
