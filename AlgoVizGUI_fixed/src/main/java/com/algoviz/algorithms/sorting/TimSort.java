package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

/**
 * Tim Sort — O(n log n) worst, O(n) best.
 * Stable: YES | In-place: NO
 * Hybrid of Merge Sort + Insertion Sort. Used in Java's Arrays.sort() for primitives
 * and Python's built-in sort. The real-world GOAT sorting algorithm.
 */
public class TimSort {

    private static final int RUN = 32;
    private static int comparisons, swaps;

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        comparisons = 0; swaps = 0;
        int n = arr.length;

        steps.add(step(arr, StepType.INITIAL, "Starting Tim Sort — the algorithm used by Java & Python! Combines Insertion Sort on small runs + Merge Sort.", comparisons, swaps));

        // Step 1: Sort individual runs with Insertion Sort
        for (int i = 0; i < n; i += RUN) {
            int end = Math.min(i + RUN - 1, n - 1);
            steps.add(step(arr, StepType.HIGHLIGHT,
                    "Insertion-sorting RUN [" + i + "..." + end + "] (run size up to " + RUN + ")", comparisons, swaps, i, end));
            insertionSortRun(arr, i, end, steps);
        }

        steps.add(step(arr, StepType.MERGE, "All runs sorted! Now merging runs with Merge Sort...", comparisons, swaps));

        // Step 2: Merge sorted runs
        for (int size = RUN; size < n; size *= 2) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);
                if (mid < right) {
                    steps.add(step(arr, StepType.MERGE,
                            "Merging runs [" + left + "..." + mid + "] and [" + (mid+1) + "..." + right + "]",
                            comparisons, swaps, left, mid, right));
                    merge(arr, left, mid, right, steps);
                }
            }
        }

        steps.add(step(arr, StepType.FINAL, "Tim Sort complete! This is what Java and Python use under the hood.", comparisons, swaps));

        AlgoResult result = new AlgoResult("Tim Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n)"); result.setTimeComplexityAvg("O(n log n)"); result.setTimeComplexityWorst("O(n log n)");
        result.setSpaceComplexity("O(n)"); result.setStable(true);
        result.setDescription("Tim Sort is a hybrid sorting algorithm derived from Merge Sort and Insertion Sort. It divides data into small 'runs' sorted by Insertion Sort, then merges them. This is the actual algorithm used in Java (Arrays.sort) and Python (list.sort).");
        return result;
    }

    private static void insertionSortRun(int[] arr, int left, int right, List<AlgoStep> steps) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i], j = i - 1;
            while (j >= left && arr[j] > key) {
                comparisons++;
                arr[j + 1] = arr[j]; j--; swaps++;
            }
            arr[j + 1] = key;
        }
        AlgoStep s = new AlgoStep(arr, StepType.SORTED, "Run insertion-sorted.", left, right);
        s.setComparisons(comparisons); s.setSwaps(swaps); steps.add(s);
    }

    private static void merge(int[] arr, int left, int mid, int right, List<AlgoStep> steps) {
        int[] L = Arrays.copyOfRange(arr, left, mid + 1);
        int[] R = Arrays.copyOfRange(arr, mid + 1, right + 1);
        int i = 0, j = 0, k = left;
        while (i < L.length && j < R.length) {
            comparisons++;
            if (L[i] <= R[j]) arr[k++] = L[i++];
            else arr[k++] = R[j++];
            swaps++;
        }
        while (i < L.length) { arr[k++] = L[i++]; swaps++; }
        while (j < R.length) { arr[k++] = R[j++]; swaps++; }

        AlgoStep s = new AlgoStep(arr, StepType.MERGE, "Runs merged into sorted segment [" + left + "..." + right + "]", left, right);
        s.setComparisons(comparisons); s.setSwaps(swaps); steps.add(s);
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int sw, int... idx) {
        AlgoStep s = new AlgoStep(arr, t, desc, idx);
        s.setComparisons(comp); s.setSwaps(sw); return s;
    }
}
