package com.algoviz.algorithms.searching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;


public class InterpolationSearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;

        steps.add(s(arr, StepType.INITIAL,
                "Interpolation Search for " + target + ". Estimates WHERE in the range the target likely is — like how you'd open a dictionary!", comparisons, -1));

        int low = 0, high = arr.length - 1;

        while (low <= high && target >= arr[low] && target <= arr[high]) {
            if (low == high) {
                comparisons++;
                if (arr[low] == target) {
                    AlgoStep found = new AlgoStep(arr, StepType.FOUND, " Found " + target + " at index " + low, low);
                    found.setComparisons(comparisons); found.setFoundIndex(low); steps.add(found);
                    return JumpSearch.finish("Interpolation Search", steps, comparisons, low,
                            "O(1)", "O(log log n)", "O(n)", "O(1)",
                            "Interpolation Search improves on binary search for uniformly distributed data by estimating the probe position using the value range. Like using page numbers in a dictionary — if target is 'M', open near the middle.");
                }
                break;
            }

            // Interpolation formula: estimate position based on value
            int pos = low + (int)(((double)(target - arr[low]) / (arr[high] - arr[low])) * (high - low));
            comparisons++;

            steps.add(s(arr, StepType.COMPARE,
                    "Interpolated probe position: " + pos + " (formula: low + (target-arr[low])/(arr[high]-arr[low]) * (high-low)). arr[" + pos + "]=" + arr[pos],
                    comparisons, -1, low, pos, high));

            if (arr[pos] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, " Found " + target + " at estimated position " + pos + "!", pos);
                found.setComparisons(comparisons); found.setFoundIndex(pos); steps.add(found);
                return JumpSearch.finish("Interpolation Search", steps, comparisons, pos,
                        "O(1)", "O(log log n)", "O(n)", "O(1)",
                        "Interpolation Search improves on binary search for uniformly distributed data by estimating position using the value range.");
            } else if (arr[pos] < target) {
                steps.add(s(arr, StepType.HIGHLIGHT, arr[pos] + " < " + target + " → searching upper half", comparisons, -1, pos+1, high));
                low = pos + 1;
            } else {
                steps.add(s(arr, StepType.HIGHLIGHT, arr[pos] + " > " + target + " → searching lower half", comparisons, -1, low, pos-1));
                high = pos - 1;
            }
        }

        steps.add(s(arr, StepType.NOT_FOUND, "X " + target + " not found.", comparisons, -1));
        return JumpSearch.finish("Interpolation Search", steps, comparisons, -1,
                "O(1)", "O(log log n)", "O(n)", "O(1)",
                "Interpolation Search improves on binary search for uniformly distributed data.");
    }

    private static AlgoStep s(int[] arr, StepType t, String desc, int comp, int fi, int... idx) {
        AlgoStep st = new AlgoStep(arr, t, desc, idx); st.setComparisons(comp); st.setFoundIndex(fi); return st;
    }
}
