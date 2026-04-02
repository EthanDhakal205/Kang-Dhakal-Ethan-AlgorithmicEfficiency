package com.algoviz.algorithms.searching;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;


public class ExponentialSearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;
        int n = arr.length;

        steps.add(s(arr, StepType.INITIAL, "Exponential Search for " + target + ". Double the range (1,2,4,8...) until we overshoot, then binary search!", comparisons, -1));

        if (arr[0] == target) {
            comparisons++;
            AlgoStep f = new AlgoStep(arr, StepType.FOUND, "Found at index 0!", 0);
            f.setComparisons(comparisons); f.setFoundIndex(0); steps.add(f);
            return JumpSearch.finish("Exponential Search", steps, comparisons, 0, "O(1)", "O(log n)", "O(log n)", "O(1)",
                    "Exponential Search finds the range where target exists by doubling the index (1,2,4,8,16...), then applies binary search in that range. Ideal for unbounded/infinite sorted arrays.");
        }

        int bound = 1;
        while (bound < n && arr[bound] < target) {
            comparisons++;
            steps.add(s(arr, StepType.HIGHLIGHT, "arr[" + bound + "]=" + arr[bound] + " < " + target + " → doubling bound to " + (bound*2), comparisons, -1, bound));
            bound *= 2;
        }

        int left = bound / 2;
        int right = Math.min(bound, n - 1);
        steps.add(s(arr, StepType.HIGHLIGHT, "Overshot! Binary searching in range [" + left + "..." + right + "]", comparisons, -1, left, right));

        // Binary search in range
        while (left <= right) {
            int mid = (left + right) / 2;
            comparisons++;
            steps.add(s(arr, StepType.COMPARE, "Binary: arr[" + mid + "]=" + arr[mid] + " vs " + target, comparisons, -1, left, mid, right));

            if (arr[mid] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "Found " + target + " at index " + mid + "!", mid);
                found.setComparisons(comparisons); found.setFoundIndex(mid); steps.add(found);
                return JumpSearch.finish("Exponential Search", steps, comparisons, mid, "O(1)", "O(log n)", "O(log n)", "O(1)",
                        "Exponential Search doubles the index until it overshoots the target range, then performs binary search. Perfect for unbounded sorted arrays.");
            } else if (arr[mid] < target) left = mid + 1;
            else right = mid - 1;
        }

        steps.add(s(arr, StepType.NOT_FOUND, "X Not found.", comparisons, -1));
        return JumpSearch.finish("Exponential Search", steps, comparisons, -1, "O(1)", "O(log n)", "O(log n)", "O(1)",
                "Exponential Search doubles the index until overshoot, then binary searches.");
    }

    private static AlgoStep s(int[] arr, StepType t, String desc, int comp, int fi, int... idx) {
        AlgoStep st = new AlgoStep(arr, t, desc, idx); st.setComparisons(comp); st.setFoundIndex(fi); return st;
    }
}
