import java.util.ArrayList;
import java.util.List;

public class KMPSearch extends SearchAlgorithm {

    private String text;
    private String pattern;
    private List<Integer> matchPositions;

    public KMPSearch(String text, String pattern, boolean ignoreCase) {
        super(null, null, ignoreCase);
        this.text          = text;
        this.pattern       = pattern;
        this.matchPositions = new ArrayList<>();
    }

    @Override
    protected boolean validate() {
        if (text == null || text.isEmpty()) return false;
        if (pattern == null || pattern.isEmpty()) return false;
        if (pattern.length() > text.length()) return false;
        return true;
    }

    private String normalize(String s) {
        return ignoreCase ? s.toLowerCase() : s;
    }

    private int[] buildLPS(String pattern) {
        int m      = pattern.length();
        int[] lps  = new int[m];
        int len    = 0;
        int i      = 1;

        lps[0] = 0;

        while (i < m) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                lps[i] = ++len;
                i++;
            } else if (len > 0) {
                len = lps[len - 1];
            } else {
                lps[i] = 0;
                i++;
            }
        }
        return lps;
    }

    public List<Integer> getMatchPositions() {
        return matchPositions;
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — text or pattern is null/empty, or pattern is longer than text.");
            return -1;
        }

        reset();
        matchPositions.clear();
        startTimer();

        String t = normalize(text);
        String p = normalize(pattern);

        int n    = t.length();
        int m    = p.length();
        int[] lps = buildLPS(p);

        int i = 0;
        int j = 0;

        while (i < n) {
            comparisons++;

            if (t.charAt(i) == p.charAt(j)) {
                i++;
                j++;
            }

            if (j == m) {
                matchPositions.add(i - j);
                j = lps[j - 1];
            } else if (i < n && t.charAt(i) != p.charAt(j)) {
                if (j > 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        stopTimer();

        if (!matchPositions.isEmpty()) {
            System.out.println("matches found at positions: " + matchPositions);
            return matchPositions.get(0);
        }

        return -1;
    }

    @Override
    public String getName() { return "Knuth-Morris-Pratt (KMP)"; }

    @Override
    public String getTimeComplexity() { return "O(n + m)"; }

    @Override
    public String getSpaceComplexity() { return "O(m)"; }
}
