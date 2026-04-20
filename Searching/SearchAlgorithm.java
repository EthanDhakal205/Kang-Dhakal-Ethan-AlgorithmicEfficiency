public abstract class SearchAlgorithm {

    protected Object[] arr;
    protected Object target;
    protected boolean ignoreCase;
    protected int comparisons;
    protected long startTime;
    protected long endTime;

    public SearchAlgorithm(Object[] arr, Object target, boolean ignoreCase) {
        this.arr = arr;
        this.target = target;
        this.ignoreCase = ignoreCase;
        this.comparisons = 0;
    }

    public abstract int search();

    public abstract String getName();

    public abstract String getTimeComplexity();

    public abstract String getSpaceComplexity();

    protected abstract boolean validate();

    protected void startTimer() {
        startTime = System.nanoTime();
    }

    protected void stopTimer() {
        endTime = System.nanoTime();
    }

    public long getElapsedTime() {
        return endTime - startTime;
    }

    public int getComparisons() {
        return comparisons;
    }

    public void reset() {
        comparisons = 0;
        startTime = 0;
        endTime = 0;
    }

    @SuppressWarnings("unchecked")
    protected int compareValues(Object left, Object right) {
        if (ignoreCase && left instanceof String && right instanceof String) {
            return ((String) left).compareToIgnoreCase((String) right);
        }
        if (left instanceof Number && right instanceof Number) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
        }
        if (left instanceof Comparable<?> comparable) {
            try {
                return ((Comparable<Object>) comparable).compareTo(right);
            } catch (ClassCastException ignored) {
            }
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    }

    protected boolean isSortedAscending() {
        if (arr == null || arr.length == 0) {
            return false;
        }
        for (int i = 0; i < arr.length - 1; i++) {
            if (compareValues(arr[i], arr[i + 1]) > 0) {
                return false;
            }
        }
        return true;
    }
}
