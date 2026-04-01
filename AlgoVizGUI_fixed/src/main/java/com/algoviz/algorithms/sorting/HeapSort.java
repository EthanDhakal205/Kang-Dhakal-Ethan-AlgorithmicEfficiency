package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Heap Sort — O(n log n) all cases.
 * Stable: NO | In-place: YES
 * Uses a binary max-heap. Consistent performance but poor cache behavior.
 */
public class HeapSort {

    private static int comparisons, swaps;

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        comparisons = 0; swaps = 0;
        int n = arr.length;

        AlgoStep initial = new AlgoStep(arr, StepType.INITIAL, "Starting Heap Sort — first build a Max-Heap, then extract elements in order.");
        initial.setComparisons(0); initial.setSwaps(0);
        steps.add(initial);

        // Build max heap
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i, steps);
        }

        AlgoStep heapBuilt = new AlgoStep(arr, StepType.HIGHLIGHT, "Max-Heap constructed! Root is the largest element: " + arr[0]);
        heapBuilt.setComparisons(comparisons); heapBuilt.setSwaps(swaps);
        steps.add(heapBuilt);

        // Extract elements one by one
        for (int i = n - 1; i > 0; i--) {
            int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
            swaps++;
            AlgoStep extract = new AlgoStep(arr, StepType.SWAP,
                    "Extracted max " + arr[i] + " → moved to position " + i + ". Re-heapifying...", 0, i);
            extract.setComparisons(comparisons); extract.setSwaps(swaps);
            steps.add(extract);

            heapify(arr, i, 0, steps);

            AlgoStep sorted = new AlgoStep(arr, StepType.SORTED, "Element " + arr[i] + " is in final position.", i);
            sorted.setComparisons(comparisons); sorted.setSwaps(swaps);
            steps.add(sorted);
        }

        AlgoStep fin = new AlgoStep(arr, StepType.FINAL, "Heap Sort complete!");
        fin.setComparisons(comparisons); fin.setSwaps(swaps);
        steps.add(fin);

        AlgoResult result = new AlgoResult("Heap Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n log n)"); result.setTimeComplexityAvg("O(n log n)"); result.setTimeComplexityWorst("O(n log n)");
        result.setSpaceComplexity("O(1)"); result.setStable(false);
        result.setDescription("Heap Sort builds a max-heap from the data and then repeatedly extracts the maximum element, placing it at the end. It achieves guaranteed O(n log n) performance with O(1) extra space.");
        return result;
    }

    private static void heapify(int[] arr, int n, int i, List<AlgoStep> steps) {
        int largest = i, left = 2 * i + 1, right = 2 * i + 2;
        comparisons++;

        if (left < n && arr[left] > arr[largest]) largest = left;
        if (right < n && arr[right] > arr[largest]) largest = right;
        comparisons++;

        if (largest != i) {
            AlgoStep compare = new AlgoStep(arr, StepType.COMPARE,
                    "Comparing parent arr[" + i + "]=" + arr[i] + " with child arr[" + largest + "]=" + arr[largest], i, largest);
            compare.setComparisons(comparisons); compare.setSwaps(swaps);
            steps.add(compare);

            int tmp = arr[i]; arr[i] = arr[largest]; arr[largest] = tmp;
            swaps++;
            AlgoStep swap = new AlgoStep(arr, StepType.SWAP,
                    "Sifting down: swapping parent " + arr[largest] + " with larger child " + arr[i], i, largest);
            swap.setComparisons(comparisons); swap.setSwaps(swaps);
            steps.add(swap);

            heapify(arr, n, largest, steps);
        }
    }
}
