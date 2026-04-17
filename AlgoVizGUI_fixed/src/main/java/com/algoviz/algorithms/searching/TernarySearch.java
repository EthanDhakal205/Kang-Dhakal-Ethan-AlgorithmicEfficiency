package com.algoviz.algorithms.searching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;


public class TernarySearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;

        steps.add(s(arr, StepType.INITIAL,
                "Ternary Search for " + target + ". Divides array into THREE sections using two midpoints. More splits but more comparisons per step — turns out binary is still better!",
                comparisons, -1));

        int left = 0, right = arr.length - 1;

        while (left <= right) {
            int mid1 = left + (right - left) / 3;
            int mid2 = right - (right - left) / 3;

            comparisons += 2;
            steps.add(s(arr, StepType.COMPARE,
                    "Three sections: [" + left + "..." + mid1 + "] [" + (mid1+1) + "..." + (mid2-1) + "] [" + mid2 + "..." + right + "]. Checking mid1=" + arr[mid1] + ", mid2=" + arr[mid2],
                    comparisons, -1, left, mid1, mid2, right));

            if (arr[mid1] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, " Found " + target + " at mid1=" + mid1 + "!", mid1);
                found.setComparisons(comparisons); found.setFoundIndex(mid1); steps.add(found);
                return done(steps, comparisons, mid1);
            }
            if (arr[mid2] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "Found " + target + " at mid2=" + mid2 + "!", mid2);
                found.setComparisons(comparisons); found.setFoundIndex(mid2); steps.add(found);
                return done(steps, comparisons, mid2);
            }

            if (target < arr[mid1]) {
                steps.add(s(arr, StepType.HIGHLIGHT, target + " < arr[mid1]=" + arr[mid1] + " → search LEFT third [" + left + "..." + (mid1-1) + "]", comparisons, -1, left, mid1-1));
                right = mid1 - 1;
            } else if (target > arr[mid2]) {
                steps.add(s(arr, StepType.HIGHLIGHT, target + " > arr[mid2]=" + arr[mid2] + " → search RIGHT third [" + (mid2+1) + "..." + right + "]", comparisons, -1, mid2+1, right));
                left = mid2 + 1;
            } else {
                steps.add(s(arr, StepType.HIGHLIGHT, target + " is in MIDDLE third [" + (mid1+1) + "..." + (mid2-1) + "]", comparisons, -1, mid1+1, mid2-1));
                left = mid1 + 1;
                right = mid2 - 1;
            }
        }

        steps.add(s(arr, StepType.NOT_FOUND, "X " + target + " not found.", comparisons, -1));
        return JumpSearch.finish("Ternary Search", steps, comparisons, -1,
                "O(1)", "O(log₃ n)", "O(log₃ n)", "O(1)",
                "Ternary Search divides the array into three equal parts and determines which third contains the target. Despite the smaller range each step, it requires 2 comparisons per iteration vs Binary's 1-2, making it slightly less efficient in practice.");
    }

    private static AlgoResult done(List<AlgoStep> steps, int comp, int found) {
        return JumpSearch.finish("Ternary Search", steps, comp, found,
                "O(1)", "O(log₃ n)", "O(log₃ n)", "O(1)",
                "Ternary Search divides the array into 3 parts. Despite seeming more efficient, it actually performs more comparisons than Binary Search due to 2 comparisons per round.");
    }

    private static AlgoStep s(int[] arr, StepType t, String desc, int comp, int fi, int... idx) {
        AlgoStep st = new AlgoStep(arr, t, desc, idx); st.setComparisons(comp); st.setFoundIndex(fi); return st;
    }
}
