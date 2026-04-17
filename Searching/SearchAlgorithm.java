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
}