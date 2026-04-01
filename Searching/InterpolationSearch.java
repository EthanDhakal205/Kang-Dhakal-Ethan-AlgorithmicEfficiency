public class InterpolationSearch extends SearchAlgorithm {

    public InterpolationSearch(Object[] arr, Object target, boolean ignoreCase) {
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

        if (!(target instanceof Number) && !(target instanceof String)) return false;

        return true;
    }

    private int compareStrings(String a, String b) {
        return ignoreCase ? a.compareToIgnoreCase(b) : a.compareTo(b);
    }

    private int estimateStringPosition(int low, int high, String target) {
        String lowStr  = (String) arr[low];
        String highStr = (String) arr[high];

        if (lowStr.isEmpty() || highStr.isEmpty()) return low;

        char targetChar = ignoreCase ? Character.toLowerCase(target.charAt(0))
                                     : target.charAt(0);
        char lowChar    = ignoreCase ? Character.toLowerCase(lowStr.charAt(0))
                                     : lowStr.charAt(0);
        char highChar   = ignoreCase ? Character.toLowerCase(highStr.charAt(0))
                                     : highStr.charAt(0);

        if (highChar == lowChar) return low;

        return low + (int) (((double)(targetChar - lowChar) / (highChar - lowChar)) * (high - low));
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — array is null/empty, target is null, array is not sorted, or target type is not supported.");
            return -1;
        }

        reset();
        startTimer();

        int low  = 0;
        int high = arr.length - 1;

        while (low <= high) {
            int pos;
            comparisons++;

            if (target instanceof Number) {
                double lowVal    = ((Number) arr[low]).doubleValue();
                double highVal   = ((Number) arr[high]).doubleValue();
                double targetVal = ((Number) target).doubleValue();

                if (highVal == lowVal) {
                    if (lowVal == targetVal) { stopTimer(); return low; }
                    break;
                }

                pos = low + (int) (((targetVal - lowVal) / (highVal - lowVal)) * (high - low));

                if (pos < low || pos > high) break;

                double posVal = ((Number) arr[pos]).doubleValue();

                if (posVal == targetVal) { stopTimer(); return pos; }
                else if (posVal < targetVal) low  = pos + 1;
                else                         high = pos - 1;

            } else if (target instanceof String) {
                pos = estimateStringPosition(low, high, (String) target);

                if (pos < low || pos > high) break;

                int cmp = compareStrings((String) arr[pos], (String) target);

                if (cmp == 0) { stopTimer(); return pos; }
                else if (cmp < 0) low  = pos + 1;
                else              high = pos - 1;
            } else {
                break;
            }
        }

        stopTimer();
        return -1;
    }

    @Override
    public String getName() { return "Interpolation Search"; }

    @Override
    public String getTimeComplexity() { return "O(log log n)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}