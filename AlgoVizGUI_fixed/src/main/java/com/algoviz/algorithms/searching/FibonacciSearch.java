package com.algoviz.algorithms.searching;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

/**
 * Fibonacci Search — O(log n).
 * Uses Fibonacci numbers to divide the array. Better cache performance than binary search
 * because it avoids division and uses only addition/subtraction.
 */
public class FibonacciSearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;
        int n = arr.length;

        steps.add(s(arr, StepType.INITIAL,
                "Fibonacci Search for " + target + ". Uses Fibonacci numbers (1,1,2,3,5,8,13...) as division points — better cache locality than Binary Search!",
                comparisons, -1));

        // Find smallest Fibonacci >= n
        int fibM2 = 0, fibM1 = 1, fibM = 1;
        while (fibM < n) { fibM2 = fibM1; fibM1 = fibM; fibM = fibM1 + fibM2; }

        steps.add(s(arr, StepType.HIGHLIGHT,
                "Fibonacci numbers generated up to " + fibM + " (≥ array size " + n + "). Using F(m)=" + fibM + ", F(m-1)=" + fibM1 + ", F(m-2)=" + fibM2,
                comparisons, -1));

        int offset = -1;

        while (fibM > 1) {
            int i = Math.min(offset + fibM2, n - 1);
            comparisons++;

            steps.add(s(arr, StepType.COMPARE,
                    "Fibonacci probe at index " + i + ": arr[" + i + "]=" + arr[i] + " vs target=" + target + " (fib=" + fibM2 + ")",
                    comparisons, -1, i));

            if (arr[i] < target) {
                steps.add(s(arr, StepType.HIGHLIGHT, arr[i] + " < " + target + " → move offset to " + i + ", shift Fibonacci down", comparisons, -1, i));
                fibM = fibM1; fibM1 = fibM2; fibM2 = fibM - fibM1;
                offset = i;
            } else if (arr[i] > target) {
                steps.add(s(arr, StepType.HIGHLIGHT, arr[i] + " > " + target + " → search lower section, smaller Fibonacci", comparisons, -1, i));
                fibM = fibM2; fibM1 = fibM1 - fibM2; fibM2 = fibM - fibM1;
            } else {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "🎯 Found " + target + " at index " + i + "!", i);
                found.setComparisons(comparisons); found.setFoundIndex(i); steps.add(found);
                return JumpSearch.finish("Fibonacci Search", steps, comparisons, i,
                        "O(1)", "O(log n)", "O(log n)", "O(1)",
                        "Fibonacci Search uses Fibonacci numbers to divide the search range. It only uses addition and subtraction (no division), giving better cache performance on some hardware architectures than Binary Search.");
            }
        }

        if (fibM1 == 1 && offset + 1 < n) {
            comparisons++;
            steps.add(s(arr, StepType.COMPARE, "Last check: arr[" + (offset+1) + "]=" + arr[offset+1], comparisons, -1, offset+1));
            if (arr[offset + 1] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "🎯 Found " + target + " at index " + (offset+1) + "!", offset+1);
                found.setComparisons(comparisons); found.setFoundIndex(offset+1); steps.add(found);
                return JumpSearch.finish("Fibonacci Search", steps, comparisons, offset+1,
                        "O(1)", "O(log n)", "O(log n)", "O(1)",
                        "Fibonacci Search uses Fibonacci sequence to divide the array — only addition/subtraction, no division.");
            }
        }

        steps.add(s(arr, StepType.NOT_FOUND, "❌ " + target + " not found.", comparisons, -1));
        return JumpSearch.finish("Fibonacci Search", steps, comparisons, -1,
                "O(1)", "O(log n)", "O(log n)", "O(1)",
                "Fibonacci Search uses Fibonacci numbers to divide the array. No division required — only addition and subtraction.");
    }

    private static AlgoStep s(int[] arr, StepType t, String desc, int comp, int fi, int... idx) {
        AlgoStep st = new AlgoStep(arr, t, desc, idx); st.setComparisons(comp); st.setFoundIndex(fi); return st;
    }
}
