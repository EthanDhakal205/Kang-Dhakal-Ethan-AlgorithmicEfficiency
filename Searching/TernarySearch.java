public class TernarySearch extends SearchAlgorithm {

    public TernarySearch(Object[] arr, Object target, boolean ignoreCase) {
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
            int third = (high - low) / 3;
            int mid1 = low + third;
            int mid2 = high - third;
            comparisons += 2;

            if (ignoreCase && target instanceof String && arr[mid1] instanceof String && arr[mid2] instanceof String) {
                int cmp1 = ((String) arr[mid1]).compareToIgnoreCase((String) target);
                int cmp2 = ((String) arr[mid2]).compareToIgnoreCase((String) target);

                if (cmp1 == 0) { stopTimer(); return mid1; }
                if (cmp2 == 0) { stopTimer(); return mid2; }

                if (cmp1 > 0)       high = mid1 - 1;
                else if (cmp2 < 0)  low = mid2 + 1;
                else                { low = mid1 + 1; high = mid2 - 1; }

            } else if (arr[mid1] instanceof Number && arr[mid2] instanceof Number && target instanceof Number) {
                double mid1Val  = ((Number) arr[mid1]).doubleValue();
                double mid2Val  = ((Number) arr[mid2]).doubleValue();
                double targetVal = ((Number) target).doubleValue();

                if (mid1Val == targetVal) { stopTimer(); return mid1; }
                if (mid2Val == targetVal) { stopTimer(); return mid2; }

                if (targetVal < mid1Val)      high = mid1 - 1;
                else if (targetVal > mid2Val) low = mid2 + 1;
                else                          { low = mid1 + 1; high = mid2 - 1; }

            } else if (arr[mid1] instanceof Comparable && arr[mid2] instanceof Comparable) {
                int cmp1 = ((Comparable) arr[mid1]).compareTo(target);
                int cmp2 = ((Comparable) arr[mid2]).compareTo(target);

                if (cmp1 == 0) { stopTimer(); return mid1; }
                if (cmp2 == 0) { stopTimer(); return mid2; }

                if (cmp1 > 0)       high = mid1 - 1;
                else if (cmp2 < 0)  low = mid2 + 1;
                else                { low = mid1 + 1; high = mid2 - 1; }
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Ternary Search"; }

    @Override
    public String getTimeComplexity() { return "O(log3 n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}
