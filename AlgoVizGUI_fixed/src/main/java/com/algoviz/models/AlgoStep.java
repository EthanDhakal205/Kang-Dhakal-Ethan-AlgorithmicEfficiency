package com.algoviz.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a single step in an algorithm's execution.
 * Each step captures the array state + metadata for frontend animation.
 */
public class AlgoStep {

    public enum StepType {
        COMPARE,       // Two elements being compared
        SWAP,          // Two elements being swapped
        PIVOT,         // Pivot element highlighted (QuickSort)
        MERGE,         // Merging phase (MergeSort)
        FOUND,         // Target element found (Search)
        NOT_FOUND,     // Target not in array (Search)
        HIGHLIGHT,     // Generic highlight (e.g. current index)
        SORTED,        // Mark elements as sorted/finalized
        INITIAL,       // Initial state
        FINAL          // Final sorted state
    }

    private int[] array;
    private StepType type;
    private List<Integer> highlightedIndices;
    private int comparisons;
    private int swaps;
    private String description;
    private int foundIndex;    // For search algorithms: index of found element (-1 if not found)

    public AlgoStep(int[] array, StepType type, String description) {
        this.array = Arrays.copyOf(array, array.length);
        this.type = type;
        this.highlightedIndices = new ArrayList<>();
        this.description = description;
        this.foundIndex = -1;
    }

    public AlgoStep(int[] array, StepType type, String description, int... highlighted) {
        this(array, type, description);
        for (int idx : highlighted) {
            this.highlightedIndices.add(idx);
        }
    }

    // --- Getters & Setters ---

    public int[] getArray() { return array; }
    public void setArray(int[] array) { this.array = Arrays.copyOf(array, array.length); }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public List<Integer> getHighlightedIndices() { return highlightedIndices; }
    public void setHighlightedIndices(List<Integer> highlightedIndices) { this.highlightedIndices = highlightedIndices; }

    public int getComparisons() { return comparisons; }
    public void setComparisons(int comparisons) { this.comparisons = comparisons; }

    public int getSwaps() { return swaps; }
    public void setSwaps(int swaps) { this.swaps = swaps; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getFoundIndex() { return foundIndex; }
    public void setFoundIndex(int foundIndex) { this.foundIndex = foundIndex; }
}
