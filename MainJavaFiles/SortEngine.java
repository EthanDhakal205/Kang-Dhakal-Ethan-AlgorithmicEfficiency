import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

class SortEngine {

    enum StepType {
        INITIAL,
        COMPARE,
        SWAP,
        PIVOT,
        MERGE,
        HIGHLIGHT,
        SORTED,
        FINAL
    }

    static final class SortFrame {
        private final int[] array;
        private final StepType type;
        private final List<Integer> highlightedIndices;
        private final Set<Integer> lockedIndices;
        private final int pivotIndex;
        private final String description;
        private final int comparisons;
        private final int swaps;

        SortFrame(
            int[] array,
            StepType type,
            String description,
            int comparisons,
            int swaps,
            int pivotIndex,
            int[] highlightedIndices,
            Set<Integer> lockedIndices
        ) {
            this.array = Arrays.copyOf(array, array.length);
            this.type = type;
            this.description = description;
            this.comparisons = comparisons;
            this.swaps = swaps;
            this.pivotIndex = pivotIndex;
            this.highlightedIndices = new ArrayList<>();
            for (int index : highlightedIndices) {
                this.highlightedIndices.add(index);
            }
            this.lockedIndices = new LinkedHashSet<>(lockedIndices);
        }

        int[] getArray() {
            return Arrays.copyOf(array, array.length);
        }

        StepType getType() {
            return type;
        }

        List<Integer> getHighlightedIndices() {
            return new ArrayList<>(highlightedIndices);
        }

        Set<Integer> getLockedIndices() {
            return new LinkedHashSet<>(lockedIndices);
        }

        int getPivotIndex() {
            return pivotIndex;
        }

        String getDescription() {
            return description;
        }

        int getComparisons() {
            return comparisons;
        }

        int getSwaps() {
            return swaps;
        }
    }

    static final class SortRun {
        private final List<SortFrame> frames;
        private final int comparisons;
        private final int swaps;
        private final long elapsedNs;

        SortRun(List<SortFrame> frames, int comparisons, int swaps, long elapsedNs) {
            this.frames = frames;
            this.comparisons = comparisons;
            this.swaps = swaps;
            this.elapsedNs = elapsedNs;
        }

        List<SortFrame> getFrames() {
            return frames;
        }

        int getComparisons() {
            return comparisons;
        }

        int getSwaps() {
            return swaps;
        }

        long getElapsedNs() {
            return elapsedNs;
        }
    }

    static SortRun run(String algorithm, int[] input) {
        int[] arr = Arrays.copyOf(input, input.length);
        Builder builder = new Builder();
        long startedAt = System.nanoTime();

        switch (algorithm) {
            case "Bubble Sort" -> runBubbleSort(arr, builder);
            case "Insertion Sort" -> runInsertionSort(arr, builder);
            case "Selection Sort" -> runSelectionSort(arr, builder);
            case "Gnome Sort" -> runGnomeSort(arr, builder);
            case "Bogo Sort" -> runBogoSort(arr, builder);
            case "Merge Sort" -> runMergeSort(arr, builder);
            case "Quick Sort" -> runQuickSort(arr, builder);
            case "Heap Sort" -> runHeapSort(arr, builder);
            case "Tim Sort" -> runTimSort(arr, builder);
            case "Radix Sort" -> runRadixSort(arr, builder);
            default -> throw new IllegalArgumentException("Unsupported sort algorithm: " + algorithm);
        }

        long elapsedNs = System.nanoTime() - startedAt;
        return new SortRun(builder.frames, builder.comparisons, builder.swaps, elapsedNs);
    }

    private static void runBubbleSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Bubble Sort. Larger elements bubble to the end on each pass.");
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                builder.incrementComparisons();
                builder.add(arr, StepType.COMPARE,
                    "Comparing arr[" + j + "]=" + arr[j] + " with arr[" + (j + 1) + "]=" + arr[j + 1] + ".",
                    j, j + 1);
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                    builder.incrementSwaps();
                    swapped = true;
                    builder.add(arr, StepType.SWAP,
                        "Swapped " + arr[j] + " and " + arr[j + 1] + " so the larger value moves right.",
                        j, j + 1);
                }
            }
            builder.lock(arr, "Index " + (n - i - 1) + " is now fixed in final order.", n - i - 1);
            if (!swapped) {
                break;
            }
        }
        builder.finish(arr, "Bubble Sort complete. The array is fully sorted.");
    }

    private static void runInsertionSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Insertion Sort. Each value is inserted into the sorted prefix.");
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            builder.add(arr, StepType.HIGHLIGHT,
                "Picking up " + key + " at index " + i + " for insertion into the sorted region.",
                i);
            while (j >= 0) {
                builder.incrementComparisons();
                builder.add(arr, StepType.COMPARE,
                    "Checking whether " + arr[j] + " should stay ahead of " + key + ".",
                    j, j + 1);
                if (arr[j] <= key) {
                    break;
                }
                arr[j + 1] = arr[j];
                builder.incrementSwaps();
                builder.add(arr, StepType.SWAP,
                    "Shifted " + arr[j + 1] + " right to make room for " + key + ".",
                    j, j + 1);
                j--;
            }
            arr[j + 1] = key;
            builder.add(arr, StepType.HIGHLIGHT,
                "Inserted " + key + " at index " + (j + 1) + ".",
                j + 1);
        }
        builder.finish(arr, "Insertion Sort complete. Every element is in order.");
    }

    private static void runSelectionSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Selection Sort. Each pass selects the minimum remaining value.");
        int n = arr.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            builder.add(arr, StepType.HIGHLIGHT,
                "Searching for the minimum value in range [" + i + ", " + (n - 1) + "].",
                i);
            for (int j = i + 1; j < n; j++) {
                builder.incrementComparisons();
                builder.add(arr, StepType.COMPARE,
                    "Comparing candidate minimum " + arr[minIndex] + " with " + arr[j] + ".",
                    minIndex, j);
                if (arr[j] < arr[minIndex]) {
                    minIndex = j;
                    builder.add(arr, StepType.HIGHLIGHT,
                        "New minimum found at index " + minIndex + " with value " + arr[minIndex] + ".",
                        minIndex);
                }
            }
            if (minIndex != i) {
                int temp = arr[i];
                arr[i] = arr[minIndex];
                arr[minIndex] = temp;
                builder.incrementSwaps();
                builder.add(arr, StepType.SWAP,
                    "Placed the minimum value " + arr[i] + " at index " + i + ".",
                    i, minIndex);
            }
            builder.lock(arr, "Index " + i + " is finalized.", i);
        }
        builder.finish(arr, "Selection Sort complete. The array is fully sorted.");
    }

    private static void runGnomeSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Gnome Sort. Walk forward, swap out-of-order neighbors, then step back.");
        int position = 0;
        while (position < arr.length) {
            if (position == 0) {
                builder.add(arr, StepType.HIGHLIGHT,
                    "At index 0, so the gnome can only move forward.",
                    position);
                position++;
                continue;
            }
            builder.incrementComparisons();
            builder.add(arr, StepType.COMPARE,
                "Comparing arr[" + (position - 1) + "]=" + arr[position - 1]
                    + " with arr[" + position + "]=" + arr[position] + ".",
                position - 1, position);
            if (arr[position] >= arr[position - 1]) {
                builder.add(arr, StepType.HIGHLIGHT,
                    "Those values are already in order, so move forward.",
                    position);
                position++;
            } else {
                int temp = arr[position];
                arr[position] = arr[position - 1];
                arr[position - 1] = temp;
                builder.incrementSwaps();
                builder.add(arr, StepType.SWAP,
                    "Swapped the out-of-order pair and stepped back.",
                    position - 1, position);
                position--;
            }
        }
        builder.finish(arr, "Gnome Sort complete. No out-of-order pairs remain.");
    }

    private static void runBogoSort(int[] arr, Builder builder) {
        if (arr.length > 6) {
            throw new IllegalArgumentException("Bogo Sort is limited to 6 integers or fewer in this visualizer.");
        }
        builder.add(arr, StepType.INITIAL,
            "Starting Bogo Sort. Random shuffles continue until the array happens to be sorted.");
        Random random = new Random(42L);
        int attempts = 0;
        int maxAttempts = 200;
        while (!isSorted(arr) && attempts < maxAttempts) {
            attempts++;
            builder.addComparisons(Math.max(0, arr.length - 1));
            builder.add(arr, StepType.COMPARE,
                "Attempt " + attempts + ": not sorted yet, so shuffle everything again.");
            shuffle(arr, random);
            builder.addSwaps(arr.length);
            builder.add(arr, StepType.SWAP,
                "Random shuffle complete. Checking whether luck finally worked.");
        }
        if (!isSorted(arr)) {
            Arrays.sort(arr);
            builder.finish(arr,
                "Bogo Sort hit the safety cap after " + attempts
                    + " shuffles, so the array was force-sorted for display.");
        } else {
            builder.finish(arr,
                "Bogo Sort got lucky after " + attempts + " shuffle(s).");
        }
    }

    private static void runMergeSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Merge Sort. Recursively divide, then merge sorted halves.");
        mergeSort(arr, 0, arr.length - 1, builder);
        builder.finish(arr, "Merge Sort complete. Every segment has been merged back in order.");
    }

    private static void mergeSort(int[] arr, int left, int right, Builder builder) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        builder.add(arr, StepType.HIGHLIGHT,
            "Dividing range [" + left + ", " + right + "] into [" + left + ", " + mid + "] and ["
                + (mid + 1) + ", " + right + "].",
            left, mid, right);
        mergeSort(arr, left, mid, builder);
        mergeSort(arr, mid + 1, right, builder);
        merge(arr, left, mid, right, builder);
    }

    private static void merge(int[] arr, int left, int mid, int right, Builder builder) {
        int[] leftHalf = Arrays.copyOfRange(arr, left, mid + 1);
        int[] rightHalf = Arrays.copyOfRange(arr, mid + 1, right + 1);
        int leftIndex = 0;
        int rightIndex = 0;
        int writeIndex = left;

        builder.add(arr, StepType.MERGE,
            "Merging the sorted halves [" + left + ", " + mid + "] and [" + (mid + 1) + ", " + right + "].",
            left, right);

        while (leftIndex < leftHalf.length && rightIndex < rightHalf.length) {
            builder.incrementComparisons();
            int sourceLeft = left + leftIndex;
            int sourceRight = mid + 1 + rightIndex;
            builder.add(arr, StepType.COMPARE,
                "Comparing left value " + leftHalf[leftIndex] + " with right value " + rightHalf[rightIndex] + ".",
                sourceLeft, sourceRight);
            if (leftHalf[leftIndex] <= rightHalf[rightIndex]) {
                arr[writeIndex] = leftHalf[leftIndex++];
            } else {
                arr[writeIndex] = rightHalf[rightIndex++];
            }
            builder.incrementSwaps();
            builder.add(arr, StepType.MERGE,
                "Placed " + arr[writeIndex] + " at index " + writeIndex + " during the merge.",
                writeIndex);
            writeIndex++;
        }

        while (leftIndex < leftHalf.length) {
            arr[writeIndex] = leftHalf[leftIndex++];
            builder.incrementSwaps();
            builder.add(arr, StepType.MERGE,
                "Copied remaining left-half value " + arr[writeIndex] + " into index " + writeIndex + ".",
                writeIndex);
            writeIndex++;
        }

        while (rightIndex < rightHalf.length) {
            arr[writeIndex] = rightHalf[rightIndex++];
            builder.incrementSwaps();
            builder.add(arr, StepType.MERGE,
                "Copied remaining right-half value " + arr[writeIndex] + " into index " + writeIndex + ".",
                writeIndex);
            writeIndex++;
        }
    }

    private static void runQuickSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Quick Sort. Select a pivot, partition, and sort the smaller ranges recursively.");
        quickSort(arr, 0, arr.length - 1, builder);
        builder.finish(arr, "Quick Sort complete. All partitions have been resolved.");
    }

    private static void quickSort(int[] arr, int low, int high, Builder builder) {
        if (low >= high) {
            return;
        }
        int pivotIndex = partition(arr, low, high, builder);
        quickSort(arr, low, pivotIndex - 1, builder);
        quickSort(arr, pivotIndex + 1, high, builder);
    }

    private static int partition(int[] arr, int low, int high, Builder builder) {
        int pivot = arr[high];
        builder.addPivot(arr,
            "Using " + pivot + " at index " + high + " as the pivot for range [" + low + ", " + high + "].",
            high,
            low, high);
        int smallerTail = low - 1;
        for (int j = low; j < high; j++) {
            builder.incrementComparisons();
            builder.add(arr, StepType.COMPARE,
                "Comparing " + arr[j] + " with pivot " + pivot + ".",
                j, high);
            if (arr[j] <= pivot) {
                smallerTail++;
                if (smallerTail != j) {
                    int temp = arr[smallerTail];
                    arr[smallerTail] = arr[j];
                    arr[j] = temp;
                    builder.incrementSwaps();
                    builder.add(arr, StepType.SWAP,
                        "Moved " + arr[smallerTail] + " into the pivot's left partition.",
                        smallerTail, j);
                }
            }
        }
        int temp = arr[smallerTail + 1];
        arr[smallerTail + 1] = arr[high];
        arr[high] = temp;
        builder.incrementSwaps();
        builder.add(arr, StepType.SWAP,
            "Placed pivot " + arr[smallerTail + 1] + " into index " + (smallerTail + 1) + ".",
            smallerTail + 1, high);
        builder.lock(arr, "Pivot index " + (smallerTail + 1) + " is now fixed.", smallerTail + 1);
        return smallerTail + 1;
    }

    private static void runHeapSort(int[] arr, Builder builder) {
        builder.add(arr, StepType.INITIAL,
            "Starting Heap Sort. Build a max heap, then extract the largest value one by one.");
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(arr, n, i, builder);
        }
        if (n > 0) {
            builder.add(arr, StepType.HIGHLIGHT,
                "Max heap constructed. The root now holds the largest value " + arr[0] + ".",
                0);
        }
        for (int i = n - 1; i > 0; i--) {
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            builder.incrementSwaps();
            builder.add(arr, StepType.SWAP,
                "Moved the current maximum " + arr[i] + " to final index " + i + ".",
                0, i);
            builder.lock(arr, "Index " + i + " is finalized after extraction.", i);
            heapify(arr, i, 0, builder);
        }
        builder.finish(arr, "Heap Sort complete. Every extraction is done.");
    }

    private static void heapify(int[] arr, int heapSize, int rootIndex, Builder builder) {
        int largest = rootIndex;
        int leftChild = 2 * rootIndex + 1;
        int rightChild = 2 * rootIndex + 2;

        if (leftChild < heapSize) {
            builder.incrementComparisons();
            builder.add(arr, StepType.COMPARE,
                "Comparing root " + arr[largest] + " with left child " + arr[leftChild] + ".",
                rootIndex, leftChild);
            if (arr[leftChild] > arr[largest]) {
                largest = leftChild;
            }
        }

        if (rightChild < heapSize) {
            builder.incrementComparisons();
            builder.add(arr, StepType.COMPARE,
                "Comparing current largest " + arr[largest] + " with right child " + arr[rightChild] + ".",
                largest, rightChild);
            if (arr[rightChild] > arr[largest]) {
                largest = rightChild;
            }
        }

        if (largest != rootIndex) {
            int temp = arr[rootIndex];
            arr[rootIndex] = arr[largest];
            arr[largest] = temp;
            builder.incrementSwaps();
            builder.add(arr, StepType.SWAP,
                "Swapped the parent with the larger child to restore heap order.",
                rootIndex, largest);
            heapify(arr, heapSize, largest, builder);
        }
    }

    private static void runTimSort(int[] arr, Builder builder) {
        final int runSize = 32;
        builder.add(arr, StepType.INITIAL,
            "Starting Tim Sort. Small runs use insertion sort before larger merge passes begin.");
        int n = arr.length;
        for (int start = 0; start < n; start += runSize) {
            int end = Math.min(start + runSize - 1, n - 1);
            builder.add(arr, StepType.HIGHLIGHT,
                "Insertion-sorting run [" + start + ", " + end + "].",
                start, end);
            insertionSortRun(arr, start, end, builder);
        }
        for (int size = runSize; size < n; size *= 2) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);
                if (mid < right) {
                    builder.add(arr, StepType.MERGE,
                        "Merging sorted runs [" + left + ", " + mid + "] and [" + (mid + 1) + ", " + right + "].",
                        left, right);
                    merge(arr, left, mid, right, builder);
                }
            }
        }
        builder.finish(arr, "Tim Sort complete. All runs have been merged into final order.");
    }

    private static void insertionSortRun(int[] arr, int left, int right, Builder builder) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i];
            int j = i - 1;
            while (j >= left) {
                builder.incrementComparisons();
                builder.add(arr, StepType.COMPARE,
                    "Within this run, compare " + arr[j] + " with " + key + ".",
                    j, j + 1);
                if (arr[j] <= key) {
                    break;
                }
                arr[j + 1] = arr[j];
                builder.incrementSwaps();
                builder.add(arr, StepType.SWAP,
                    "Shifted " + arr[j + 1] + " right inside the run.",
                    j, j + 1);
                j--;
            }
            arr[j + 1] = key;
            builder.add(arr, StepType.HIGHLIGHT,
                "Inserted " + key + " into its run at index " + (j + 1) + ".",
                j + 1);
        }
    }

    private static void runRadixSort(int[] arr, Builder builder) {
        for (int value : arr) {
            if (value < 0) {
                throw new IllegalArgumentException("Radix Sort in this visualizer requires non-negative integers.");
            }
        }
        builder.add(arr, StepType.INITIAL,
            "Starting Radix Sort. Digits are processed from least significant to most significant.");
        int max = 0;
        for (int value : arr) {
            if (value > max) {
                max = value;
            }
        }
        int digits = max == 0 ? 1 : String.valueOf(max).length();
        int pass = 1;
        for (int exp = 1; max / exp > 0; exp *= 10) {
            builder.add(arr, StepType.HIGHLIGHT,
                "Digit pass " + pass + " of " + digits + ": sorting by the " + ordinal(pass) + " digit.",
                0, Math.max(0, arr.length - 1));
            countingSortByDigit(arr, exp);
            builder.addSwaps(arr.length);
            builder.add(arr, StepType.MERGE,
                "Completed digit pass " + pass + ". The array is now ordered by that digit.",
                0, Math.max(0, arr.length - 1));
            pass++;
        }
        builder.finish(arr, "Radix Sort complete. All digit passes are done.");
    }

    private static void countingSortByDigit(int[] arr, int exp) {
        int[] output = new int[arr.length];
        int[] counts = new int[10];
        for (int value : arr) {
            counts[(value / exp) % 10]++;
        }
        for (int i = 1; i < counts.length; i++) {
            counts[i] += counts[i - 1];
        }
        for (int i = arr.length - 1; i >= 0; i--) {
            int digit = (arr[i] / exp) % 10;
            output[counts[digit] - 1] = arr[i];
            counts[digit]--;
        }
        System.arraycopy(output, 0, arr, 0, arr.length);
    }

    private static boolean isSorted(int[] arr) {
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] > arr[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private static void shuffle(int[] arr, Random random) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    private static String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }

    private static final class Builder {
        private final List<SortFrame> frames = new ArrayList<>();
        private final Set<Integer> locked = new LinkedHashSet<>();
        private int comparisons;
        private int swaps;

        void add(int[] arr, StepType type, String description, int... highlighted) {
            frames.add(new SortFrame(arr, type, description, comparisons, swaps, -1, highlighted, locked));
        }

        void addPivot(int[] arr, String description, int pivotIndex, int... highlighted) {
            frames.add(new SortFrame(arr, StepType.PIVOT, description, comparisons, swaps, pivotIndex, highlighted, locked));
        }

        void lock(int[] arr, String description, int... newLocks) {
            for (int index : newLocks) {
                if (index >= 0) {
                    locked.add(index);
                }
            }
            frames.add(new SortFrame(arr, StepType.SORTED, description, comparisons, swaps, -1, newLocks, locked));
        }

        void finish(int[] arr, String description) {
            for (int i = 0; i < arr.length; i++) {
                locked.add(i);
            }
            frames.add(new SortFrame(arr, StepType.FINAL, description, comparisons, swaps, -1, new int[0], locked));
        }

        void incrementComparisons() {
            comparisons++;
        }

        void incrementSwaps() {
            swaps++;
        }

        void addComparisons(int delta) {
            comparisons += delta;
        }

        void addSwaps(int delta) {
            swaps += delta;
        }
    }
}
