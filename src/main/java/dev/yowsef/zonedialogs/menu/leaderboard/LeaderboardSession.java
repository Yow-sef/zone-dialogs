package dev.yowsef.zonedialogs.menu.leaderboard;

public class LeaderboardSession {

    private String selectedLadder = "global";
    private String selectedMetric = "elo";
    private int ladderPage = 0;
    private String searchQuery = "";

    public String getSelectedLadder() {
        return selectedLadder;
    }

    public void setSelectedLadder(String selectedLadder) {
        if (selectedLadder == null || selectedLadder.isBlank()) {
            this.selectedLadder = "global";
        } else if (selectedLadder.equalsIgnoreCase("global")) {
            this.selectedLadder = "global";
        } else {
            // Keep the core's canonical casing; the display-name lookup is case-sensitive.
            this.selectedLadder = selectedLadder.trim();
        }
    }

    public String getSelectedMetric() {
        return selectedMetric;
    }

    public void setSelectedMetric(String selectedMetric) {
        this.selectedMetric = selectedMetric != null ? selectedMetric.toLowerCase() : "elo";
    }

    public int getLadderPage() {
        return ladderPage;
    }

    public void setLadderPage(int ladderPage) {
        this.ladderPage = Math.max(0, ladderPage);
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery != null ? searchQuery.trim() : "";
    }
}

