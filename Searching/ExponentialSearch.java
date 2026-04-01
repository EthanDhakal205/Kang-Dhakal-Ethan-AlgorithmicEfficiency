public class ExponentialSearch extends SearchAlgorithm {

    public ExponentialSearch(Object[] arr, Object target, boolean ignoreCase) {
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

    private int compare(Object a, Object b) {
        if (ignoreCase && a instanceof String && b instanceof String) {
            return ((String) a).compareToIgnoreCase((String) b);
        } else if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        } else if (a instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        return -1;
    }

    private int binarySearch(int low, int high) {
        while (low <= high) {
            int mid = (low + high) / 2;
            comparisons++;

            int cmp = compare(arr[mid], target);

            if (cmp == 0)      return mid;
            else if (cmp < 0)  low  = mid + 1;
            else               high = mid - 1;
        }
        return -1;
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — array is null/empty, target is null, or array is not sorted.");
            return -1;
        }

        reset();
        startTimer();

        int n = arr.length;

        comparisons++;
        if (compare(arr[0], target) == 0) {
            stopTimer();
            return 0;
        }

        int bound = 1;
        while (bound < n && compare(arr[bound], target) <= 0) {
            comparisons++;
            bound *= 2;
        }

        int low  = bound / 2;
        int high = Math.min(bound, n - 1);

        int result = binarySearch(low, high);

        stopTimer();
        return result;
    }

    @Override
    public String getName() { return "Exponential Search"; }

    @Override
    public String getTimeComplexity() { return "O(log n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}