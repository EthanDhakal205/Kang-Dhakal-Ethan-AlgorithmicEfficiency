package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Bubble Sort — O(n²) average/worst, O(n) best (optimized).
 * Stable: YES | In-place: YES
 * Simple but inefficient. Good for teaching fundamentals.
 */
public class BubbleSort {

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;
        int n = arr.length;

        AlgoStep initial = new AlgoStep(arr, StepType.INITIAL, "Starting Bubble Sort — we'll bubble the largest elements to the end each pass.");
        initial.setComparisons(0); initial.setSwaps(0);
        steps.add(initial);

        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                comparisons++;
                // Compare step
                AlgoStep compare = new AlgoStep(arr, StepType.COMPARE,
                        "Comparing arr[" + j + "]=" + arr[j] + " with arr[" + (j+1) + "]=" + arr[j+1], j, j+1);
                compare.setComparisons(comparisons); compare.setSwaps(swaps);
                steps.add(compare);

                if (arr[j] > arr[j + 1]) {
                    // Swap
                    int tmp = arr[j]; arr[j] = arr[j+1]; arr[j+1] = tmp;
                    swaps++;
                    swapped = true;

                    AlgoStep swap = new AlgoStep(arr, StepType.SWAP,
                            "Swapping " + arr[j+1] + " and " + arr[j] + " — larger element bubbles right.", j, j+1);
                    swap.setComparisons(comparisons); swap.setSwaps(swaps);
                    steps.add(swap);
                }
            }
            // Mark last sorted element
            AlgoStep sorted = new AlgoStep(arr, StepType.SORTED,
                    "Pass " + (i+1) + " complete — element " + arr[n-i-1] + " is in its final position.", n-i-1);
            sorted.setComparisons(comparisons); sorted.setSwaps(swaps);
            steps.add(sorted);

            if (!swapped) break; // Optimized: already sorted
        }

        AlgoStep fin = new AlgoStep(arr, StepType.FINAL, "Array is fully sorted! Bubble Sort complete.");
        fin.setComparisons(comparisons); fin.setSwaps(swaps);
        steps.add(fin);

        AlgoResult result = new AlgoResult("Bubble Sort", "sort", steps);
        result.setTotalComparisons(comparisons);
        result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n)");
        result.setTimeComplexityAvg("O(n²)");
        result.setTimeComplexityWorst("O(n²)");
        result.setSpaceComplexity("O(1)");
        result.setStable(true);
        result.setDescription("Bubble Sort repeatedly steps through the list, compares adjacent elements and swaps them if they're in the wrong order. The pass through the list is repeated until no swaps are needed.");
        return result;
    }
}
