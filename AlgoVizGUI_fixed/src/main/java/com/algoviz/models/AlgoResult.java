package com.algoviz.models;

import java.util.List;

/**
 * Full result returned by an algorithm — all steps, stats, and metadata.
 */
public class AlgoResult {

    private String algorithmName;
    private String category;         // "sort" or "search"
    private List<AlgoStep> steps;
    private int totalComparisons;
    private int totalSwaps;
    private long executionTimeNs;
    private String timeComplexityBest;
    private String timeComplexityAvg;
    private String timeComplexityWorst;
    private String spaceComplexity;
    private boolean stable;
    private String description;
    private int foundIndex;          // for search results

    public AlgoResult() {}

    public AlgoResult(String algorithmName, String category, List<AlgoStep> steps) {
        this.algorithmName = algorithmName;
        this.category = category;
        this.steps = steps;
    }

    // Compute aggregate stats from steps
    public void computeStats() {
        if (steps == null || steps.isEmpty()) return;
        AlgoStep last = steps.get(steps.size() - 1);
        this.totalComparisons = last.getComparisons();
        this.totalSwaps = last.getSwaps();
        this.foundIndex = last.getFoundIndex();
    }

    // --- Getters & Setters ---

    public String getAlgorithmName() { return algorithmName; }
    public void setAlgorithmName(String algorithmName) { this.algorithmName = algorithmName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public List<AlgoStep> getSteps() { return steps; }
    public void setSteps(List<AlgoStep> steps) { this.steps = steps; }

    public int getTotalComparisons() { return totalComparisons; }
    public void setTotalComparisons(int totalComparisons) { this.totalComparisons = totalComparisons; }

    public int getTotalSwaps() { return totalSwaps; }
    public void setTotalSwaps(int totalSwaps) { this.totalSwaps = totalSwaps; }

    public long getExecutionTimeNs() { return executionTimeNs; }
    public void setExecutionTimeNs(long executionTimeNs) { this.executionTimeNs = executionTimeNs; }

    public String getTimeComplexityBest() { return timeComplexityBest; }
    public void setTimeComplexityBest(String v) { this.timeComplexityBest = v; }

    public String getTimeComplexityAvg() { return timeComplexityAvg; }
    public void setTimeComplexityAvg(String v) { this.timeComplexityAvg = v; }

    public String getTimeComplexityWorst() { return timeComplexityWorst; }
    public void setTimeComplexityWorst(String v) { this.timeComplexityWorst = v; }

    public String getSpaceComplexity() { return spaceComplexity; }
    public void setSpaceComplexity(String v) { this.spaceComplexity = v; }

    public boolean isStable() { return stable; }
    public void setStable(boolean stable) { this.stable = stable; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getFoundIndex() { return foundIndex; }
    public void setFoundIndex(int foundIndex) { this.foundIndex = foundIndex; }
}
