package com.algoviz.algorithms.searching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import com.algoviz.models.AlgoStep.StepType;


public class JumpSearch {

    public static AlgoResult search(int[] inputArray, int target) {
        int[] arr = Arrays.copyOf(inputArray, inputArray.length);
        Arrays.sort(arr);
        List<AlgoStep> steps = new ArrayList<>();
        int comparisons = 0;
        int n = arr.length;
        int step = (int) Math.sqrt(n);

        steps.add(s(arr, StepType.INITIAL, "Jump Search for " + target + ". Jump size = √" + n + " = " + step + ". Jumping ahead in blocks!", comparisons, -1));

        int prev = 0;
        while (prev < n && arr[Math.min(step, n) - 1] < target) {
            comparisons++;
            steps.add(s(arr, StepType.HIGHLIGHT, "Block [" + prev + "..." + (Math.min(step, n)-1) + "]: max=" + arr[Math.min(step,n)-1] + " < " + target + ". Jump!", comparisons, -1, prev, Math.min(step, n)-1));
            prev = step;
            step += (int) Math.sqrt(n);
            if (prev >= n) break;
        }

        steps.add(s(arr, StepType.HIGHLIGHT, "Target block found! Linear searching backwards from index " + (Math.min(step, n) - 1) + "...", comparisons, -1, prev, Math.min(step, n) - 1));

        while (prev < Math.min(step, n)) {
            comparisons++;
            steps.add(s(arr, StepType.COMPARE, "Checking arr[" + prev + "]=" + arr[prev] + " == " + target + "?", comparisons, -1, prev));
            if (arr[prev] == target) {
                AlgoStep found = new AlgoStep(arr, StepType.FOUND, "Found " + target + " at index " + prev + "!", prev);
                found.setComparisons(comparisons); found.setFoundIndex(prev); steps.add(found);
                return finish("Jump Search", steps, comparisons, prev,
                        "O(1)", "O(√n)", "O(√n)", "O(1)",
                        "Jump Search divides array into √n sized blocks, jumps until the block containing target is found, then does linear search within the block. Great middle ground between linear and binary search.");
            }
            prev++;
        }

        steps.add(s(arr, StepType.NOT_FOUND, "X " + target + " not found.", comparisons, -1));
        return finish("Jump Search", steps, comparisons, -1, "O(1)", "O(√n)", "O(√n)", "O(1)",
                "Jump Search divides array into blocks and searches block by block.");
    }

    private static AlgoStep s(int[] arr, StepType t, String desc, int comp, int fi, int... idx) {
        AlgoStep st = new AlgoStep(arr, t, desc, idx); st.setComparisons(comp); st.setFoundIndex(fi); return st;
    }

    static AlgoResult finish(String name, List<AlgoStep> steps, int comp, int found,
                             String best, String avg, String worst, String space, String desc) {
        AlgoResult r = LinearSearch.buildResult(name, steps, comp, 0, found);
        r.setTimeComplexityBest(best); r.setTimeComplexityAvg(avg); r.setTimeComplexityWorst(worst);
        r.setSpaceComplexity(space); r.setStable(true); r.setDescription(desc);
        return r;
    }
}
