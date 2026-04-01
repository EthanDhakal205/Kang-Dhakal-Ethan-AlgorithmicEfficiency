package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Insertion Sort — O(n²) average/worst, O(n) best.
 * Stable: YES | In-place: YES
 * Excellent for nearly-sorted or small arrays. Used inside TimSort.
 */
public class InsertionSort {

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;

        AlgoStep initial = new AlgoStep(arr, StepType.INITIAL, "Starting Insertion Sort — we build a sorted portion one element at a time.");
        initial.setComparisons(0); initial.setSwaps(0);
        steps.add(initial);

        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;

            AlgoStep pick = new AlgoStep(arr, StepType.HIGHLIGHT,
                    "Picking up element " + key + " at index " + i + " — inserting into sorted region.", i);
            pick.setComparisons(comparisons); pick.setSwaps(swaps);
            steps.add(pick);

            while (j >= 0 && arr[j] > key) {
                comparisons++;
                arr[j + 1] = arr[j];
                swaps++;

                AlgoStep shift = new AlgoStep(arr, StepType.SWAP,
                        "Shifting " + arr[j] + " right to make room for " + key, j, j+1);
                shift.setComparisons(comparisons); shift.setSwaps(swaps);
                steps.add(shift);
                j--;
            }
            if (j >= 0) { comparisons++; } // final comparison that breaks loop

            arr[j + 1] = key;

            AlgoStep insert = new AlgoStep(arr, StepType.SORTED,
                    "Inserted " + key + " at position " + (j+1) + ". Sorted region grows to index " + i + ".", j+1);
            insert.setComparisons(comparisons); insert.setSwaps(swaps);
            steps.add(insert);
        }

        AlgoStep fin = new AlgoStep(arr, StepType.FINAL, "Insertion Sort complete!");
        fin.setComparisons(comparisons); fin.setSwaps(swaps);
        steps.add(fin);

        AlgoResult result = new AlgoResult("Insertion Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n)"); result.setTimeComplexityAvg("O(n²)"); result.setTimeComplexityWorst("O(n²)");
        result.setSpaceComplexity("O(1)"); result.setStable(true);
        result.setDescription("Insertion Sort builds the sorted array one element at a time by inserting each element into its correct position within the already-sorted portion. Very efficient for small or nearly-sorted data.");
        return result;
    }
}
