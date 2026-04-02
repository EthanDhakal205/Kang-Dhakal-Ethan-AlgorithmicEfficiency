package com.algoviz.algorithms.searching;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;


public class LinearSearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;

        steps.add(step(arr, StepType.INITIAL, "Linear Search for target=" + target + ". Checking each element one by one.", comparisons, -1));

        for (int i = 0; i < arr.length; i++) {
            comparisons++;
            steps.add(step(arr, StepType.COMPARE, "Checking arr[" + i + "]=" + arr[i] + " == " + target + "?", comparisons, -1, i));

            if (arr[i] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, " Found " + target + " at index " + i + "!", i);
                found.setComparisons(comparisons); found.setFoundIndex(i); steps.add(found);

                AlgoResult result = buildResult("Linear Search", steps, comparisons, 0, i);
                result.setTimeComplexityBest("O(1)"); result.setTimeComplexityAvg("O(n)"); result.setTimeComplexityWorst("O(n)");
                result.setSpaceComplexity("O(1)"); result.setStable(true);
                result.setDescription("Linear Search checks each element sequentially until it finds the target or exhausts the array. Works on unsorted arrays. Simple but slow for large datasets.");
                return result;
            }
        }

        steps.add(step(arr, StepType.NOT_FOUND, "X " + target + " not found after checking all " + arr.length + " elements.", comparisons, -1));
        AlgoResult result = buildResult("Linear Search", steps, comparisons, 0, -1);
        result.setTimeComplexityBest("O(1)"); result.setTimeComplexityAvg("O(n)"); result.setTimeComplexityWorst("O(n)");
        result.setSpaceComplexity("O(1)"); result.setStable(true);
        result.setDescription("Linear Search checks each element sequentially until it finds the target or exhausts the array. Works on unsorted arrays. Simple but slow for large datasets.");
        return result;
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int found, int... idx) {
        AlgoStep s = new AlgoStep(arr, t, desc, idx);
        s.setComparisons(comp); s.setFoundIndex(found); return s;
    }

    static AlgoResult buildResult(String name, List<AlgoStep> steps, int comp, int sw, int foundIdx) {
        AlgoResult r = new AlgoResult(name, "search", steps);
        r.setTotalComparisons(comp); r.setTotalSwaps(sw); r.setFoundIndex(foundIdx);
        return r;
    }
}
