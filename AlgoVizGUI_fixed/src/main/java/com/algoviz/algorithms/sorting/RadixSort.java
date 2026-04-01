package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

/**
 * Radix Sort — O(nk) where k = number of digits.
 * Stable: YES | In-place: NO
 * Non-comparison sort! Can beat O(n log n) for integers with small digit count.
 */
public class RadixSort {

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;

        steps.add(step(arr, StepType.INITIAL, "Starting Radix Sort — we sort by each digit position (LSD to MSD). No comparisons needed!", comparisons, swaps));

        int max = Arrays.stream(arr).max().getAsInt();
        int digits = String.valueOf(max).length();

        for (int exp = 1; max / exp > 0; exp *= 10) {
            int digitPlace = (int)(Math.log10(exp)) + 1;
            steps.add(step(arr, StepType.HIGHLIGHT,
                    "Pass " + digitPlace + "/" + digits + ": Sorting by the " + ordinal(digitPlace) + " digit (place value " + exp + ")",
                    comparisons, swaps));

            arr = countingSort(arr, exp, steps, comparisons, swaps);
            swaps += arr.length;

            steps.add(step(arr, StepType.MERGE, "After digit-" + digitPlace + " pass: array partially sorted by this digit.", comparisons, swaps));
        }

        steps.add(step(arr, StepType.FINAL, "Radix Sort complete! Sorted without a single comparison.", comparisons, swaps));

        AlgoResult result = new AlgoResult("Radix Sort", "sort", steps);
        result.setTotalComparisons(0); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(nk)"); result.setTimeComplexityAvg("O(nk)"); result.setTimeComplexityWorst("O(nk)");
        result.setSpaceComplexity("O(n + k)"); result.setStable(true);
        result.setDescription("Radix Sort is a non-comparison integer sort. It processes digits from least significant to most significant, using counting sort as a subroutine. Can achieve linear time for fixed-width integers.");
        return result;
    }

    private static int[] countingSort(int[] arr, int exp, List<AlgoStep> steps, int comparisons, int swaps) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count = new int[10];

        for (int x : arr) count[(x / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];

        for (int i = n - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[count[digit] - 1] = arr[i];
            count[digit]--;
        }
        return output;
    }

    private static String ordinal(int n) {
        String[] s = {"1st","2nd","3rd","4th","5th","6th","7th","8th","9th","10th"};
        return n <= 10 ? s[n-1] : n + "th";
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int sw) {
        AlgoStep s = new AlgoStep(arr, t, desc);
        s.setComparisons(comp); s.setSwaps(sw); return s;
    }
}
