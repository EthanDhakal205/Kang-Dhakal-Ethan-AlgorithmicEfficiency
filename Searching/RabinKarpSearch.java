import java.util.*;

public class RabinKarpSearch extends SearchAlgorithm {

    private String text;
    private String pattern;
    private List<Integer> matchPositions;

    private static final long BASE  = 31;
    private static final long MOD   = 1_000_000_007L;

    public RabinKarpSearch(String text, String pattern, boolean ignoreCase) {
        super(null, null, ignoreCase);
        this.text           = text;
        this.pattern        = pattern;
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

    private long charValue(char c) {
        return Character.toLowerCase(c) - 'a' + 1;
    }

    private long computeHash(String s, int length) {
        long hash = 0;
        for (int i = 0; i < length; i++) {
            hash = (hash * BASE + charValue(s.charAt(i))) % MOD;
        }
        return hash;
    }

    private long computePower(int length) {
        long power = 1;
        for (int i = 0; i < length - 1; i++) {
            power = (power * BASE) % MOD;
        }
        return power;
    }

    private boolean verifyMatch(String t, String p, int start) {
        comparisons++;
        for (int i = 0; i < p.length(); i++) {
            if (t.charAt(start + i) != p.charAt(i)) return false;
        }
        return true;
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

        long patternHash = computeHash(p, m);
        long windowHash  = computeHash(t, m);
        long power       = computePower(m);

        comparisons++;
        if (windowHash == patternHash && verifyMatch(t, p, 0)) {
            matchPositions.add(0);
        }

        for (int i = 1; i <= n - m; i++) {
            windowHash = (windowHash - charValue(t.charAt(i - 1)) * power % MOD + MOD) % MOD;
            windowHash = (windowHash * BASE + charValue(t.charAt(i + m - 1))) % MOD;

            comparisons++;

            if (windowHash == patternHash && verifyMatch(t, p, i)) {
                matchPositions.add(i);
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
    public String getName() { return "Rabin-Karp"; }

    @Override
    public String getTimeComplexity() { return "O(n + m)"; }

    @Override
    public String getSpaceComplexity() { return "O(1)"; }
}