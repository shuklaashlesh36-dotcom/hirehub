package com.hirehub.exception;

public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(String message) {
        super(message);
    }

    public JobNotFoundException(Long jobId) {
        super("Job not found for id: " + jobId);
    }
}
