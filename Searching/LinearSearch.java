public class LinearSearch extends SearchAlgorithm {

    public LinearSearch(Object[] arr, Object target, boolean ignoreCase) {
        super(arr, target, ignoreCase);
    }

    @Override
    protected boolean validate() {
        return arr != null && arr.length > 0 && target != null;
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — array is null/empty or target is null.");
            return -1;
        }

        reset();
        startTimer();

        for (int i = 0; i < arr.length; i++) {
            comparisons++;

            if (ignoreCase && target instanceof String && arr[i] instanceof String) {
                if (((String) arr[i]).equalsIgnoreCase((String) target)) {
                    stopTimer();
                    return i;
                }
            } else if (arr[i] instanceof Number && target instanceof Number) {
                if (((Number) arr[i]).doubleValue() == ((Number) target).doubleValue()) {
                    stopTimer();
                    return i;
                }
            } else {
                if (arr[i].equals(target)) {
                    stopTimer();
                    return i;
                }
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Linear Search"; }

    @Override
    public String getTimeComplexity() { return "O(n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}
