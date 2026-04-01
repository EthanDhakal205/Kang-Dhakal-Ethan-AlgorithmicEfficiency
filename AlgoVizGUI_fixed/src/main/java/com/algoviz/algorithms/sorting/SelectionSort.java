package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Selection Sort — O(n²) always. Stable: NO. */
public class SelectionSort {
    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;
        int n = arr.length;

        steps.add(step(arr, StepType.INITIAL, "Starting Selection Sort — find the minimum each pass and place it at the front.", comparisons, swaps));

        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            steps.add(step(arr, StepType.HIGHLIGHT, "Looking for minimum in range [" + i + "..." + (n-1) + "]", comparisons, swaps, i));

            for (int j = i + 1; j < n; j++) {
                comparisons++;
                steps.add(step(arr, StepType.COMPARE, "Is arr[" + j + "]=" + arr[j] + " < current min " + arr[minIdx] + "?", comparisons, swaps, j, minIdx));
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                    steps.add(step(arr, StepType.HIGHLIGHT, "New minimum found: " + arr[minIdx] + " at index " + minIdx, comparisons, swaps, minIdx));
                }
            }

            if (minIdx != i) {
                int tmp = arr[i]; arr[i] = arr[minIdx]; arr[minIdx] = tmp;
                swaps++;
                steps.add(step(arr, StepType.SWAP, "Placing minimum " + arr[i] + " at index " + i, comparisons, swaps, i, minIdx));
            }
            steps.add(step(arr, StepType.SORTED, "Index " + i + " finalized with value " + arr[i], comparisons, swaps, i));
        }

        steps.add(step(arr, StepType.FINAL, "Selection Sort complete!", comparisons, swaps));

        AlgoResult result = new AlgoResult("Selection Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n²)"); result.setTimeComplexityAvg("O(n²)"); result.setTimeComplexityWorst("O(n²)");
        result.setSpaceComplexity("O(1)"); result.setStable(false);
        result.setDescription("Selection Sort divides the array into a sorted and unsorted region. It repeatedly finds the minimum element from the unsorted region and places it at the end of the sorted region. Simple but always O(n²).");
        return result;
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int sw, int... idx) {
        AlgoStep s = new AlgoStep(arr, t, desc, idx);
        s.setComparisons(comp); s.setSwaps(sw); return s;
    }
}
