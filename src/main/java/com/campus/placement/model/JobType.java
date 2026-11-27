package com.campus.placement.model;

/**
 * Employment type for a placement drive.
 */
public enum JobType {
    FULL_TIME("Full-time"),
    INTERNSHIP("Internship"),
    INTERN_PPO("Intern+PPO");

    private final String label;

    JobType(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for UI display.
     *
     * @return display label
     */
    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
