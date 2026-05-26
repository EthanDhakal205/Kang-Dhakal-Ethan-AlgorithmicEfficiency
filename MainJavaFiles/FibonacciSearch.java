public class FibonacciSearch extends SearchAlgorithm {

    public FibonacciSearch(Object[] arr, Object target, boolean ignoreCase) {
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

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — array is null/empty, target is null, or array is not sorted.");
            return -1;
        }

        reset();
        startTimer();

        int n    = arr.length;
        int fibM2 = 0;
        int fibM1 = 1;
        int fib   = 1;

        while (fib < n) {
            fibM2 = fibM1;
            fibM1 = fib;
            fib   = fibM1 + fibM2;
        }

        int offset = -1;

        while (fib > 1) {
            int i = Math.min(offset + fibM2, n - 1);
            comparisons++;

            int cmp = compare(arr[i], target);

            if (cmp < 0) {
                fib  = fibM1;
                fibM1 = fibM2;
                fibM2 = fib - fibM1;
                offset = i;
            } else if (cmp > 0) {
                fib  = fibM2;
                fibM1 -= fibM2;
                fibM2 = fib - fibM1;
            } else {
                stopTimer();
                return i;
            }
        }

        if (fibM1 == 1 && offset + 1 < n) {
            comparisons++;
            if (compare(arr[offset + 1], target) == 0) {
                stopTimer();
                return offset + 1;
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Fibonacci Search"; }

    @Override
    public String getTimeComplexity() { return "O(log n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}
