package com.hirehub.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates dynamic statistics and metrics for the Employer Dashboard.
 */
public class EmployerDashboardStats {

    private final int totalJobs;
    private final int activeJobs;
    private final int totalApplicants;
    private final List<Application> recentApplications;

    public EmployerDashboardStats(int totalJobs, int activeJobs, int totalApplicants, List<Application> recentApplications) {
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.totalApplicants = totalApplicants;
        this.recentApplications = recentApplications != null ? Collections.unmodifiableList(recentApplications) : Collections.emptyList();
    }

    public int getTotalJobs() {
        return totalJobs;
    }

    public int getActiveJobs() {
        return activeJobs;
    }

    public int getTotalApplicants() {
        return totalApplicants;
    }

    public List<Application> getRecentApplications() {
        return recentApplications;
    }
}
