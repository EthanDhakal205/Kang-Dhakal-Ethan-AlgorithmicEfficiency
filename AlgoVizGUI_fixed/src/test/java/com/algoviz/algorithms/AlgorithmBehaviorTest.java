package com.algoviz.algorithms;

import com.algoviz.algorithms.searching.BinarySearch;
import com.algoviz.algorithms.searching.LinearSearch;
import com.algoviz.algorithms.sorting.BubbleSort;
import com.algoviz.algorithms.sorting.MergeSort;
import com.algoviz.algorithms.sorting.QuickSort;
import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlgorithmBehaviorTest {

    @Test
    void bubbleSortOrdersMixedValues() {
        AlgoResult result = BubbleSort.sort(new int[]{5, 1, 4, 2, 8});
        assertArrayEquals(new int[]{1, 2, 4, 5, 8}, lastArray(result));
    }

    @Test
    void mergeSortHandlesDuplicatesAndNegatives() {
        AlgoResult result = MergeSort.sort(new int[]{3, -1, 3, 2, 0});
        assertArrayEquals(new int[]{-1, 0, 2, 3, 3}, lastArray(result));
    }

    @Test
    void quickSortHandlesEmptyInput() {
        AlgoResult result = QuickSort.sort(new int[]{});
        assertArrayEquals(new int[]{}, lastArray(result));
    }

    @Test
    void linearSearchReturnsFoundIndex() {
        AlgoResult result = LinearSearch.search(new int[]{4, 2, 7, 1}, 7);
        assertEquals(2, result.getFoundIndex());
    }

    @Test
    void binarySearchWorksOnSortedCopyOfInput() {
        AlgoResult result = BinarySearch.search(new int[]{8, 1, 5, 3}, 5);
        assertArrayEquals(new int[]{1, 3, 5, 8}, lastArray(result));
        assertEquals(2, result.getFoundIndex());
    }

    private int[] lastArray(AlgoResult result) {
        AlgoStep finalStep = result.getSteps().get(result.getSteps().size() - 1);
        return finalStep.getArray();
    }
}
