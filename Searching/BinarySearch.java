public class BinarySearch extends SearchAlgorithm {

    public BinarySearch(Object[] arr, Object target, boolean ignoreCase) {
        super(arr, target, ignoreCase);
    }

    @Override
    protected boolean validate() {
        if (arr == null || arr.length == 0 || target == null) return false;

        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] instanceof Number && arr[i + 1] instanceof Number) {
                if (((Number) arr[i]).doubleValue() > ((Number) arr[i + 1]).doubleValue()) return false;
            } else if (arr[i] instanceof String && arr[i + 1] instanceof String) {
                if (((String) arr[i]).compareToIgnoreCase((String) arr[i + 1]) > 0) return false;
            }
        }
        return true;
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — array is null/empty, target is null, or array is not sorted.");
            return -1;
        }

        reset();
        startTimer();

        int low = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            if (ignoreCase && target instanceof String && arr[mid] instanceof String) {
                int result = ((String) arr[mid]).compareToIgnoreCase((String) target);
                if (result == 0) { stopTimer(); return mid; }
                else if (result < 0) low = mid + 1;
                else high = mid - 1;
            } else if (arr[mid] instanceof Number && target instanceof Number) {
                double midVal = ((Number) arr[mid]).doubleValue();
                double targetVal = ((Number) target).doubleValue();
                if (midVal == targetVal) { stopTimer(); return mid; }
                else if (midVal < targetVal) low = mid + 1;
                else high = mid - 1;
            } else {
                if (arr[mid] instanceof Comparable) {
                    int result = ((Comparable) arr[mid]).compareTo(target);
                    if (result == 0) { stopTimer(); return mid; }
                    else if (result < 0) low = mid + 1;
                    else high = mid - 1;
                }
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