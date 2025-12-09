package com.zerooneblog.api.domain;

public enum ReportReason {
    SPAM("Spam or misleading content"),
    HARASSMENT("Harassment or hate speech"),
    INAPPROPRIATE_CONTENT("Nudity or inappropriate content"),
    INTELLECTUAL_PROPERTY("Intellectual property violation"),
    SELF_HARM("Self-harm or suicidal content"),
    OTHER("Other");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
