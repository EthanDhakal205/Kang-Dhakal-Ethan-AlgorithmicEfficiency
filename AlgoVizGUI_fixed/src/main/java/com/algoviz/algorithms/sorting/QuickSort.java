package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class QuickSort {

    private static int comparisons, swaps;

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        comparisons = 0; swaps = 0;

        AlgoStep initial = new AlgoStep(arr, StepType.INITIAL, "Starting Quick Sort — we pick a pivot and partition around it recursively.");
        initial.setComparisons(0); initial.setSwaps(0);
        steps.add(initial);

        quickSort(arr, 0, arr.length - 1, steps);

        AlgoStep fin = new AlgoStep(arr, StepType.FINAL, "Quick Sort complete! All partitions resolved.");
        fin.setComparisons(comparisons); fin.setSwaps(swaps);
        steps.add(fin);

        AlgoResult result = new AlgoResult("Quick Sort", "sort", steps);
        result.setTotalComparisons(comparisons);
        result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n log n)");
        result.setTimeComplexityAvg("O(n log n)");
        result.setTimeComplexityWorst("O(n²)");
        result.setSpaceComplexity("O(log n)");
        result.setStable(false);
        result.setDescription("Quick Sort selects a 'pivot' element and partitions the array around it so smaller elements come before it and larger elements after. It then recursively sorts the sub-arrays.");
        return result;
    }

    private static void quickSort(int[] arr, int low, int high, List<AlgoStep> steps) {
        if (low < high) {
            int pivotIdx = partition(arr, low, high, steps);

            AlgoStep pivotDone = new AlgoStep(arr, StepType.SORTED,
                    "Pivot " + arr[pivotIdx] + " at index " + pivotIdx + " is now in its correct position.", pivotIdx);
            pivotDone.setComparisons(comparisons); pivotDone.setSwaps(swaps);
            steps.add(pivotDone);

            quickSort(arr, low, pivotIdx - 1, steps);
            quickSort(arr, pivotIdx + 1, high, steps);
        }
    }

    private static int partition(int[] arr, int low, int high, List<AlgoStep> steps) {
        int pivot = arr[high];

        AlgoStep pivotStep = new AlgoStep(arr, StepType.PIVOT,
                "Pivot selected: " + pivot + " at index " + high + ". Partitioning [" + low + "..." + high + "].", high);
        pivotStep.setComparisons(comparisons); pivotStep.setSwaps(swaps);
        steps.add(pivotStep);

        int i = low - 1;

        for (int j = low; j < high; j++) {
            comparisons++;
            AlgoStep compare = new AlgoStep(arr, StepType.COMPARE,
                    "Comparing arr[" + j + "]=" + arr[j] + " with pivot=" + pivot, j, high);
            compare.setComparisons(comparisons); compare.setSwaps(swaps);
            steps.add(compare);

            if (arr[j] <= pivot) {
                i++;
                if (i != j) {
                    int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
                    swaps++;
                    AlgoStep swap = new AlgoStep(arr, StepType.SWAP,
                            "Moving " + arr[j] + " to left partition — it's ≤ pivot.", i, j);
                    swap.setComparisons(comparisons); swap.setSwaps(swaps);
                    steps.add(swap);
                }
            }
        }

        // Place pivot in correct position
        int tmp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = tmp;
        swaps++;
        AlgoStep placePivot = new AlgoStep(arr, StepType.SWAP,
                "Placing pivot " + arr[i+1] + " at index " + (i+1) + " — its final position.", i+1, high);
        placePivot.setComparisons(comparisons); placePivot.setSwaps(swaps);
        steps.add(placePivot);

        return i + 1;
    }
}
