package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeSort {

    private static int comparisons, swaps;

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        comparisons = 0; swaps = 0;

        AlgoStep initial = new AlgoStep(arr, StepType.INITIAL, "Starting Merge Sort — divide array in half recursively, then merge back sorted.");
        initial.setComparisons(0); initial.setSwaps(0);
        steps.add(initial);

        mergeSort(arr, 0, arr.length - 1, steps);

        AlgoStep fin = new AlgoStep(arr, StepType.FINAL, "Merge Sort complete! All sublists merged into sorted order.");
        fin.setComparisons(comparisons); fin.setSwaps(swaps);
        steps.add(fin);

        AlgoResult result = new AlgoResult("Merge Sort", "sort", steps);
        result.setTotalComparisons(comparisons);
        result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n log n)");
        result.setTimeComplexityAvg("O(n log n)");
        result.setTimeComplexityWorst("O(n log n)");
        result.setSpaceComplexity("O(n)");
        result.setStable(true);
        result.setDescription("Merge Sort is a divide-and-conquer algorithm. It divides the array into halves, recursively sorts each half, then merges the two sorted halves. It guarantees O(n log n) in all cases.");
        return result;
    }

    private static void mergeSort(int[] arr, int left, int right, List<AlgoStep> steps) {
        if (left < right) {
            int mid = (left + right) / 2;

            AlgoStep divide = new AlgoStep(arr, StepType.HIGHLIGHT,
                    "Dividing: [" + left + "..." + mid + "] and [" + (mid+1) + "..." + right + "]", left, mid, right);
            divide.setComparisons(comparisons); divide.setSwaps(swaps);
            steps.add(divide);

            mergeSort(arr, left, mid, steps);
            mergeSort(arr, mid + 1, right, steps);
            merge(arr, left, mid, right, steps);
        }
    }

    private static void merge(int[] arr, int left, int mid, int right, List<AlgoStep> steps) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] L = Arrays.copyOfRange(arr, left, mid + 1);
        int[] R = Arrays.copyOfRange(arr, mid + 1, right + 1);

        AlgoStep mergeStart = new AlgoStep(arr, StepType.MERGE,
                "Merging subarrays [" + left + "..." + mid + "] and [" + (mid+1) + "..." + right + "]", left, mid, right);
        mergeStart.setComparisons(comparisons); mergeStart.setSwaps(swaps);
        steps.add(mergeStart);

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            comparisons++;
            if (L[i] <= R[j]) {
                arr[k] = L[i];
                i++;
            } else {
                arr[k] = R[j];
                j++;
            }
            swaps++;
            AlgoStep place = new AlgoStep(arr, StepType.SWAP,
                    "Placing " + arr[k] + " at position " + k, k);
            place.setComparisons(comparisons); place.setSwaps(swaps);
            steps.add(place);
            k++;
        }

        while (i < n1) {
            arr[k] = L[i];
            i++; k++; swaps++;
            AlgoStep place = new AlgoStep(arr, StepType.SWAP, "Copying remaining left element " + arr[k-1], k-1);
            place.setComparisons(comparisons); place.setSwaps(swaps);
            steps.add(place);
        }

        while (j < n2) {
            arr[k] = R[j];
            j++; k++; swaps++;
            AlgoStep place = new AlgoStep(arr, StepType.SWAP, "Copying remaining right element " + arr[k-1], k-1);
            place.setComparisons(comparisons); place.setSwaps(swaps);
            steps.add(place);
        }
    }
}
