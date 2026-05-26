public class BinarySearch extends SearchAlgorithm {

    public BinarySearch(Object[] arr, Object target, boolean ignoreCase) {
        super(arr, target, ignoreCase);
    }

    @Override
    protected boolean validate() {
        return arr != null && arr.length > 0 && target != null && isSortedAscending();
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed - array is null/empty, target is null, or array is not sorted.");
            return -1;
        }

        reset();
        startTimer();

        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            int result = compareValues(arr[mid], target);
            if (result == 0) {
                stopTimer();
                return mid;
            } else if (result < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Binary Search"; }

    @Override
    public String getTimeComplexity() { return "O(log n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}
