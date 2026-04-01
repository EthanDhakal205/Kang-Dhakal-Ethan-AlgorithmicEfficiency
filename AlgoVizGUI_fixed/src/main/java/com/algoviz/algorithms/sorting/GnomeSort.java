package com.algoviz.algorithms.sorting;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

/**
 * Gnome Sort (a.k.a. Stupid Sort v2) — O(n²) average.
 * Stable: YES | In-place: YES
 * Like Insertion Sort but the gnome walks back and forth comparing adjacent elements.
 * Named after a Dutch garden gnome sorting flower pots.
 */
public class GnomeSort {

    public static AlgoResult sort(int[] inputArray) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0, swaps = 0;
        int n = arr.length;

        steps.add(step(arr, StepType.INITIAL,
                "🪴 Gnome Sort! A garden gnome sorting flower pots — walks forward until finds out-of-order pair, swaps, walks back.", comparisons, swaps));

        int pos = 0;
        while (pos < n) {
            if (pos == 0) {
                steps.add(step(arr, StepType.HIGHLIGHT, "Gnome at position 0 — can only move forward.", comparisons, swaps, pos));
                pos++;
            } else {
                comparisons++;
                steps.add(step(arr, StepType.COMPARE,
                        "Gnome at pos " + pos + ": comparing arr[" + (pos-1) + "]=" + arr[pos-1] + " with arr[" + pos + "]=" + arr[pos],
                        comparisons, swaps, pos-1, pos));

                if (arr[pos] >= arr[pos - 1]) {
                    steps.add(step(arr, StepType.HIGHLIGHT, "In order! 🪴 Gnome steps forward.", comparisons, swaps, pos));
                    pos++;
                } else {
                    int tmp = arr[pos]; arr[pos] = arr[pos-1]; arr[pos-1] = tmp;
                    swaps++;
                    steps.add(step(arr, StepType.SWAP,
                            "Out of order! 🪴 Gnome swaps " + arr[pos] + " and " + arr[pos-1] + ", then steps back.",
                            comparisons, swaps, pos-1, pos));
                    pos--;
                }
            }
        }

        steps.add(step(arr, StepType.FINAL, "🪴 Gnome Sort complete! The gnome reached the end without any out-of-order pairs.", comparisons, swaps));

        AlgoResult result = new AlgoResult("Gnome Sort", "sort", steps);
        result.setTotalComparisons(comparisons); result.setTotalSwaps(swaps);
        result.setTimeComplexityBest("O(n)"); result.setTimeComplexityAvg("O(n²)"); result.setTimeComplexityWorst("O(n²)");
        result.setSpaceComplexity("O(1)"); result.setStable(true);
        result.setDescription("Gnome Sort works like a garden gnome sorting flower pots. It moves forward comparing adjacent elements, and when it finds an out-of-order pair, swaps them and steps backward. Simple but quadratic — fun to watch!");
        return result;
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int sw, int... idx) {
        AlgoStep s = new AlgoStep(arr, t, desc, idx);
        s.setComparisons(comp); s.setSwaps(sw); return s;
    }
}
