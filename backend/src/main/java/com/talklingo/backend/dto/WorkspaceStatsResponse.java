package com.talklingo.backend.dto;

public class WorkspaceStatsResponse {

    private long totalSessions;
    private long totalMessages;
    private String topLanguagePair;

    public long getTotalSessions() {
        return totalSessions;
    }

    public void setTotalSessions(long totalSessions) {
        this.totalSessions = totalSessions;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public String getTopLanguagePair() {
        return topLanguagePair;
    }

    public void setTopLanguagePair(String topLanguagePair) {
        this.topLanguagePair = topLanguagePair;
    }
}
