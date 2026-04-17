package com.algoviz.algorithms.searching;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;

import java.util.*;

public class BinarySearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr); // Ensure sorted
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;

        steps.add(step(arr, StepType.INITIAL, "Binary Search for target=" + target + ". Array must be sorted — dividing search space in half each step.", comparisons, -1));

        int left = 0, right = arr.length - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            comparisons++;

            steps.add(step(arr, StepType.COMPARE,
                    "Search space [" + left + "..." + right + "], mid=" + mid + ". Comparing arr[" + mid + "]=" + arr[mid] + " with " + target,
                    comparisons, -1, left, mid, right));

            if (arr[mid] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "Found " + target + " at index " + mid + "! Took only " + comparisons + " comparison(s).", mid);
                found.setComparisons(comparisons); found.setFoundIndex(mid); steps.add(found);

                AlgoResult result = LinearSearch.buildResult("Binary Search", steps, comparisons, 0, mid);
                result.setTimeComplexityBest("O(1)"); result.setTimeComplexityAvg("O(log n)"); result.setTimeComplexityWorst("O(log n)");
                result.setSpaceComplexity("O(1)"); result.setStable(true);
                result.setDescription("Binary Search halves the search space each comparison. Only works on sorted arrays. Extremely efficient — 1 million elements requires at most 20 comparisons!");
                return result;
            } 
            else if (arr[mid] < target) {
                steps.add(step(arr, StepType.HIGHLIGHT, arr[mid] + " < " + target + " → target must be in RIGHT half. Discarding left.", comparisons, -1, mid+1, right));
                left = mid + 1;
            } 
            else {
                steps.add(step(arr, StepType.HIGHLIGHT, arr[mid] + " > " + target + " → target must be in LEFT half. Discarding right.", comparisons, -1, left, mid-1));
                right = mid - 1;
            }
        }

        steps.add(step(arr, StepType.NOT_FOUND, "X " + target + " not found. Search space exhausted.", comparisons, -1));
        AlgoResult result = LinearSearch.buildResult("Binary Search", steps, comparisons, 0, -1);
        result.setTimeComplexityBest("O(1)"); result.setTimeComplexityAvg("O(log n)"); result.setTimeComplexityWorst("O(log n)");
        result.setSpaceComplexity("O(1)"); result.setStable(true);
        result.setDescription("Binary Search halves the search space each comparison. Only works on sorted arrays.");
        return result;
    }

    private static AlgoStep step(int[] arr, StepType t, String desc, int comp, int found, int... idx) {
        AlgoStep s = new AlgoStep(arr, t, desc, idx);
        s.setComparisons(comp); s.setFoundIndex(found); return s;
    }
}
