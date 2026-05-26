public class JumpSearch extends SearchAlgorithm {

    public JumpSearch(Object[] arr, Object target, boolean ignoreCase) {
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
        int step = (int) Math.floor(Math.sqrt(n));
        int prev = 0;
        int curr = step;

        while (curr < n && compare(arr[curr], target) <= 0) {
            comparisons++;
            prev = curr;
            curr += step;
        }

        for (int i = prev; i < Math.min(curr, n); i++) {
            comparisons++;
            if (compare(arr[i], target) == 0) {
                stopTimer();
                return i;
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Jump Search"; }

    @Override
    public String getTimeComplexity() { return "O(√n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}