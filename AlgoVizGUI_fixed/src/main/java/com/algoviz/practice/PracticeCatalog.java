package com.algoviz.practice;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PracticeCatalog {

    public enum Kind {
        SORT,
        SEARCH
    }

    public record TestCase(String label, int[] input, Integer target, int[] expectedArray, Integer expectedIndex) {
        public static TestCase sort(String label, int[] input, int[] expectedArray) {
            return new TestCase(label, Arrays.copyOf(input, input.length), null,
                    Arrays.copyOf(expectedArray, expectedArray.length), null);
        }

        public static TestCase search(String label, int[] input, int target, int expectedIndex) {
            return new TestCase(label, Arrays.copyOf(input, input.length), target, null, expectedIndex);
        }

        public boolean isSortCase() {
            return expectedArray != null;
        }

        public String describe() {
            if (isSortCase()) {
                return label + System.lineSeparator()
                        + "input    = " + Arrays.toString(input) + System.lineSeparator()
                        + "expected = " + Arrays.toString(expectedArray);
            }
            return label + System.lineSeparator()
                    + "input    = " + Arrays.toString(input) + System.lineSeparator()
                    + "target   = " + target + System.lineSeparator()
                    + "expected = " + expectedIndex;
        }
    }

    public record Problem(String algorithmId, String title, Kind kind, String prompt,
                          String starterCode, String answerCode, List<TestCase> testCases) {
        public String signatureHint() {
            return kind == Kind.SORT
                    ? "public static int[] solve(int[] input)"
                    : "public static int solve(int[] input, int target)";
        }
    }

    private static final String SORTER_PROMPT = """
            Return the numbers in ascending order.
            You may sort in place or copy the array first, but the method must return the final sorted array.
            Keep helper methods inside the same UserSolution class if you need them.
            """;

    private static final String SEARCH_PROMPT = """
            Return the matching index, or -1 when the target is missing.
            Linear Search receives any array order.
            Binary Search receives an ascending sorted array in every test case.
            """;

    private static final List<TestCase> SORT_TESTS = List.of(
            TestCase.sort("Already sorted input", new int[]{1, 2, 3, 4}, new int[]{1, 2, 3, 4}),
            TestCase.sort("Mixed values", new int[]{5, 1, 4, 2, 8}, new int[]{1, 2, 4, 5, 8}),
            TestCase.sort("Duplicates and negatives", new int[]{3, -1, 3, 2, 0}, new int[]{-1, 0, 2, 3, 3}),
            TestCase.sort("Single element", new int[]{9}, new int[]{9}),
            TestCase.sort("Empty input", new int[]{}, new int[]{})
    );

    private static final List<TestCase> LINEAR_TESTS = List.of(
            TestCase.search("Target in the middle", new int[]{4, 2, 7, 1}, 7, 2),
            TestCase.search("Target missing", new int[]{10, 20, 30}, 5, -1),
            TestCase.search("Single element hit", new int[]{9}, 9, 0),
            TestCase.search("Late match", new int[]{8, 6, 4, 2, 0}, 0, 4)
    );

    private static final List<TestCase> BINARY_TESTS = List.of(
            TestCase.search("Middle hit", new int[]{1, 3, 5, 7, 9}, 7, 3),
            TestCase.search("First element", new int[]{1, 4, 7, 11, 19, 25}, 1, 0),
            TestCase.search("Missing target", new int[]{2, 4, 6, 8, 10, 12}, 1, -1),
            TestCase.search("Single element hit", new int[]{42}, 42, 0)
    );

    private static final Map<String, Problem> PROBLEMS = new LinkedHashMap<>();

    static {
        PROBLEMS.put("bubble", new Problem(
                "bubble",
                "Bubble Sort",
                Kind.SORT,
                SORTER_PROMPT,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);

                        // TODO: implement Bubble Sort

                        return arr;
                    }
                }
                """,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);
                        for (int i = 0; i < arr.length - 1; i++) {
                            boolean swapped = false;
                            for (int j = 0; j < arr.length - i - 1; j++) {
                                if (arr[j] > arr[j + 1]) {
                                    int temp = arr[j];
                                    arr[j] = arr[j + 1];
                                    arr[j + 1] = temp;
                                    swapped = true;
                                }
                            }
                            if (!swapped) {
                                break;
                            }
                        }
                        return arr;
                    }
                }
                """,
                SORT_TESTS
        ));

        PROBLEMS.put("merge", new Problem(
                "merge",
                "Merge Sort",
                Kind.SORT,
                SORTER_PROMPT,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);

                        // TODO: implement Merge Sort

                        return arr;
                    }
                }
                """,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);
                        mergeSort(arr, 0, arr.length - 1);
                        return arr;
                    }

                    private static void mergeSort(int[] arr, int left, int right) {
                        if (left >= right) {
                            return;
                        }
                        int mid = left + (right - left) / 2;
                        mergeSort(arr, left, mid);
                        mergeSort(arr, mid + 1, right);
                        merge(arr, left, mid, right);
                    }

                    private static void merge(int[] arr, int left, int mid, int right) {
                        int[] merged = new int[right - left + 1];
                        int i = left;
                        int j = mid + 1;
                        int k = 0;

                        while (i <= mid && j <= right) {
                            if (arr[i] <= arr[j]) {
                                merged[k++] = arr[i++];
                            } else {
                                merged[k++] = arr[j++];
                            }
                        }

                        while (i <= mid) {
                            merged[k++] = arr[i++];
                        }
                        while (j <= right) {
                            merged[k++] = arr[j++];
                        }

                        System.arraycopy(merged, 0, arr, left, merged.length);
                    }
                }
                """,
                SORT_TESTS
        ));

        PROBLEMS.put("quick", new Problem(
                "quick",
                "Quick Sort",
                Kind.SORT,
                SORTER_PROMPT,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);

                        // TODO: implement Quick Sort

                        return arr;
                    }
                }
                """,
                """
                import java.util.Arrays;

                public class UserSolution {
                    public static int[] solve(int[] input) {
                        int[] arr = Arrays.copyOf(input, input.length);
                        quickSort(arr, 0, arr.length - 1);
                        return arr;
                    }

                    private static void quickSort(int[] arr, int low, int high) {
                        if (low >= high) {
                            return;
                        }
                        int pivotIndex = partition(arr, low, high);
                        quickSort(arr, low, pivotIndex - 1);
                        quickSort(arr, pivotIndex + 1, high);
                    }

                    private static int partition(int[] arr, int low, int high) {
                        int pivot = arr[high];
                        int smaller = low - 1;
                        for (int current = low; current < high; current++) {
                            if (arr[current] <= pivot) {
                                smaller++;
                                swap(arr, smaller, current);
                            }
                        }
                        swap(arr, smaller + 1, high);
                        return smaller + 1;
                    }

                    private static void swap(int[] arr, int i, int j) {
                        int temp = arr[i];
                        arr[i] = arr[j];
                        arr[j] = temp;
                    }
                }
                """,
                SORT_TESTS
        ));

        PROBLEMS.put("linear", new Problem(
                "linear",
                "Linear Search",
                Kind.SEARCH,
                SEARCH_PROMPT,
                """
                public class UserSolution {
                    public static int solve(int[] input, int target) {
                        // TODO: implement Linear Search
                        return -1;
                    }
                }
                """,
                """
                public class UserSolution {
                    public static int solve(int[] input, int target) {
                        for (int i = 0; i < input.length; i++) {
                            if (input[i] == target) {
                                return i;
                            }
                        }
                        return -1;
                    }
                }
                """,
                LINEAR_TESTS
        ));

        PROBLEMS.put("binary", new Problem(
                "binary",
                "Binary Search",
                Kind.SEARCH,
                SEARCH_PROMPT,
                """
                public class UserSolution {
                    public static int solve(int[] input, int target) {
                        // TODO: implement Binary Search
                        return -1;
                    }
                }
                """,
                """
                public class UserSolution {
                    public static int solve(int[] input, int target) {
                        int left = 0;
                        int right = input.length - 1;

                        while (left <= right) {
                            int mid = left + (right - left) / 2;
                            if (input[mid] == target) {
                                return mid;
                            }
                            if (input[mid] < target) {
                                left = mid + 1;
                            } else {
                                right = mid - 1;
                            }
                        }

                        return -1;
                    }
                }
                """,
                BINARY_TESTS
        ));
    }

    private PracticeCatalog() {
    }

    public static Optional<Problem> find(String algorithmId) {
        return Optional.ofNullable(PROBLEMS.get(algorithmId));
    }

    public static String supportedAlgorithmsLabel() {
        return "Bubble Sort, Merge Sort, Quick Sort, Linear Search, and Binary Search";
    }
}
