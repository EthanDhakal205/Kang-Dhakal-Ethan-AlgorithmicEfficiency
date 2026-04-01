package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

/**
 * Bogo Sort — O((n+1)!) average. The WORST sort imaginable.
 * Stable: NO | In-place: YES
 * Randomly shuffles until sorted. Included purely for educational humor.
 * We cap at 200 attempts for sanity.
 */
public class BogoSort {

    public static AlgoResult sort(int[] inputArray) {
        // Cap input at 6 elements for bogo sort sanity
        int[] arr = Arrays.copyOf(inputArray, Math.min(inputArray.length, 6));
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;
        int maxAttempts = 200;
        Random rng = new Random(42); // Seeded for reproducibility

        steps.add(step(arr, StepType.INITIAL,
                "⚠️ BOGO SORT — The world's worst algorithm! We just randomly shuffle until it's sorted. Pure chaos.", comparisons, swaps));

        int attempt = 0;
        while (!isSorted(arr) && attempt < maxAttempts) {
            attempt++;
            comparisons += arr.length - 1;

            steps.add(step(arr, StepType.COMPARE,
                    "Attempt " + attempt + ": Is it sorted? Nope! Shuffling randomly...", comparisons, swaps));

            shuffle(arr, rng);
            swaps += arr.length;

            steps.add(step(arr, StepType.SWAP,
                    "Shuffled! New arrangement: checking if we got lucky...", comparisons, swaps));
        }

        if (isSorted(arr)) {
            steps.add(step(arr, StepType.FINAL,
                    "🎉 IT'S SORTED after " + attempt + " random shuffles! Expected attempts: " + factorial(arr.length) + ". We got lucky!", comparisons, swaps));
        } else {
            // Force sort for display
            Arrays.sort(arr);
            steps.add(step(arr, StepType.FINAL,
                    "😤 Gave up after " + maxAttempts + " attempts. Force-sorted for your sanity. Bogo Sort is a joke. (avg attempts for n=" + arr.length + ": " + factorial(arr.length) + ")", comparisons, swaps));
        }

        AlgoResult result = new AlgoResult("Bogo Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n)"); result.setTimeComplexityAvg("O((n+1)!)"); result.setTimeComplexityWorst("O(∞)");
        result.setSpaceComplexity("O(1)"); result.setStable(false);
        result.setDescription("Bogo Sort (a.k.a. Stupid Sort or Shotgun Sort) randomly shuffles the array and checks if it's sorted. Repeats until sorted. Average case is O((n+1)!) — for 10 elements that's over 39 million shuffles on average. Never use this.");
        return result;
    }

    private static boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++)
            if (arr[i] > arr[i + 1]) return false;
        return true;
    }

    private static void shuffle(int[] arr, Random rng) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
        }
    }

    private static long factorial(int n) {
        long f = 1;
        for (int i = 2; i <= n; i++) f *= i;
        return f;
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int sw) {
        AlgoStep s = new AlgoStep(arr, t, desc);
        s.setComparisons(comp); s.setSwaps(sw); return s;
    }
}
