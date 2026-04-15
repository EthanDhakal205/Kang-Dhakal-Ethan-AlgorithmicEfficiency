import java.util.*;
import javax.swing.SwingUtilities;

class AlgoRunner {

    interface Callbacks {
        void setCellActive(int i);
        void setCellFound(int i);
        void setCellScanned(int i);
        void markScanned(int from, int to);
        void showCaption(String text);
        void setStatus(String msg);
        void repaint();
        void sleep() throws InterruptedException;
        int  compareValues(Object a, Object b);
    }

    private final AnimState state;
    private final Callbacks cb;

    AlgoRunner(AnimState state, Callbacks cb) {
        this.state = state;
        this.cb    = cb;
    }

    private boolean matches(Object a, Object b) { return cb.compareValues(a, b) == 0; }

    private String fmt(String key, Object... args) {
        String t = AppData.CAPTIONS.get(key);
        if (t == null) return key;
        try { return String.format(t, args); }
        catch (Exception e) { return t; }
    }

    void runArrayAlgo() throws InterruptedException {
        state.resetArrayState();
        long t0 = System.nanoTime();
        String algo = state.selectedAlgo;
        if      (algo.equals("Linear Search"))        runLinear();
        else if (algo.equals("Binary Search"))        runBinary();
        else if (algo.equals("Ternary Search"))       runTernary();
        else if (algo.equals("Jump Search"))          runJump();
        else if (algo.equals("Interpolation Search")) runInterpolation();
        else if (algo.equals("Exponential Search"))   runExponential();
        else if (algo.equals("Fibonacci Search"))     runFibonacci();
        state.elapsedNs = System.nanoTime() - t0;
    }

    private void runLinear() throws InterruptedException {
        Object[] arr = state.currentArray;
        for (int i = 0; i < arr.length; i++) {
            cb.showCaption(fmt("LINEAR_CHECK", i, arr[i], state.currentTarget));
            cb.setCellActive(i); cb.sleep(); state.comparisons++;
            if (matches(arr[i], state.currentTarget)) {
                cb.setCellFound(i);
                cb.showCaption(fmt("LINEAR_FOUND", i, arr[i], i));
                state.resultIndex = i; cb.setStatus("found at index " + i); return;
            }
            cb.showCaption(fmt("LINEAR_MISS", i, arr[i]));
            cb.setCellScanned(i);
        }
        state.resultIndex = -1;
        cb.showCaption(fmt("LINEAR_NOTFOUND"));
        cb.setStatus("not found");
    }

    private void runBinary() throws InterruptedException {
        Object[] arr = state.currentArray;
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            cb.showCaption(fmt("BINARY_MID", mid, arr[mid], state.currentTarget));
            cb.setCellActive(mid); cb.sleep(); state.comparisons++;
            int c = cb.compareValues(arr[mid], state.currentTarget);
            if (c == 0) {
                cb.setCellFound(mid); cb.showCaption(fmt("BINARY_FOUND", mid));
                state.resultIndex = mid; cb.setStatus("found at index " + mid); return;
            }
            cb.setCellScanned(mid);
            if (c < 0) { cb.showCaption(fmt("BINARY_RIGHT")); cb.markScanned(low, mid-1); low  = mid+1; }
            else        { cb.showCaption(fmt("BINARY_LEFT"));  cb.markScanned(mid+1, high); high = mid-1; }
            cb.sleep();
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    private void runTernary() throws InterruptedException {
        Object[] arr = state.currentArray;
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int t = (high - low) / 3;
            int m1 = low + t, m2 = high - t;
            cb.showCaption(fmt("TERNARY_MIDS", m1, arr[m1], m2, arr[m2]));
            cb.setCellActive(m1); cb.setCellActive(m2); cb.sleep(); state.comparisons += 2;
            int c1 = cb.compareValues(arr[m1], state.currentTarget);
            int c2 = cb.compareValues(arr[m2], state.currentTarget);
            if (c1 == 0) { cb.setCellFound(m1); state.resultIndex = m1; cb.setStatus("found at index " + m1); return; }
            if (c2 == 0) { cb.setCellFound(m2); state.resultIndex = m2; cb.setStatus("found at index " + m2); return; }
            cb.setCellScanned(m1); cb.setCellScanned(m2);
            if      (cb.compareValues(state.currentTarget, arr[m1]) < 0) { cb.showCaption(fmt("TERNARY_LEFT"));  cb.markScanned(m1+1, high); high = m1-1; }
            else if (cb.compareValues(state.currentTarget, arr[m2]) > 0) { cb.showCaption(fmt("TERNARY_RIGHT")); cb.markScanned(low, m2-1); low = m2+1; }
            else { cb.showCaption(fmt("TERNARY_MID")); cb.markScanned(low, m1-1); cb.markScanned(m2+1, high); low = m1+1; high = m2-1; }
            cb.sleep();
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    private void runJump() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length, step = (int) Math.floor(Math.sqrt(n));
        int prev = 0, curr = step;
        while (curr < n && cb.compareValues(arr[curr], state.currentTarget) <= 0) {
            cb.showCaption(fmt("JUMP_JUMP", step, curr));
            cb.setCellActive(curr); cb.sleep(); state.comparisons++;
            cb.setCellScanned(curr);
            prev = curr; curr += step;
        }
        cb.showCaption(fmt("JUMP_LINEAR", prev));
        for (int i = prev; i < Math.min(curr, n); i++) {
            cb.setCellActive(i); cb.sleep(); state.comparisons++;
            if (matches(arr[i], state.currentTarget)) {
                cb.setCellFound(i); state.resultIndex = i; cb.setStatus("found at index " + i); return;
            }
            cb.setCellScanned(i);
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    private void runInterpolation() throws InterruptedException {
        Object[] arr = state.currentArray;
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int pos;
            if (arr[low] instanceof Number && state.currentTarget instanceof Number) {
                double lo = ((Number)arr[low]).doubleValue();
                double hi = ((Number)arr[high]).doubleValue();
                double tv = ((Number)state.currentTarget).doubleValue();
                pos = (hi == lo) ? low : low + (int)(((tv - lo) / (hi - lo)) * (high - low));
            } else { pos = (low + high) / 2; }
            if (pos < low || pos > high) break;
            cb.showCaption(fmt("INTERP_POS", pos));
            cb.setCellActive(pos); cb.sleep(); state.comparisons++;
            int c = cb.compareValues(arr[pos], state.currentTarget);
            if (c == 0) { cb.setCellFound(pos); state.resultIndex = pos; cb.setStatus("found at index " + pos); return; }
            cb.setCellScanned(pos);
            if (c < 0) { cb.showCaption(fmt("INTERP_LEFT")); low = pos+1; }
            else        { cb.showCaption(fmt("INTERP_RIGHT")); high = pos-1; }
            cb.sleep();
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    private void runExponential() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length;
        cb.setCellActive(0); cb.sleep(); state.comparisons++;
        if (matches(arr[0], state.currentTarget)) { cb.setCellFound(0); state.resultIndex = 0; cb.setStatus("found at index 0"); return; }
        cb.setCellScanned(0);
        int bound = 1;
        while (bound < n && cb.compareValues(arr[bound], state.currentTarget) <= 0) {
            cb.showCaption(fmt("EXP_BOUND", bound));
            cb.setCellActive(bound); cb.sleep(); state.comparisons++;
            cb.setCellScanned(bound);
            bound *= 2;
        }
        int lo = bound / 2, hi = Math.min(bound, n - 1);
        cb.showCaption(fmt("EXP_BINARY", lo, hi)); cb.sleep();
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            cb.setCellActive(mid); cb.sleep(); state.comparisons++;
            int c = cb.compareValues(arr[mid], state.currentTarget);
            if (c == 0) { cb.setCellFound(mid); state.resultIndex = mid; cb.setStatus("found at index " + mid); return; }
            cb.setCellScanned(mid);
            if (c < 0) lo = mid+1; else hi = mid-1;
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    private void runFibonacci() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length, fm2 = 0, fm1 = 1, fib = 1;
        while (fib < n) { fm2 = fm1; fm1 = fib; fib = fm1 + fm2; }
        int offset = -1;
        while (fib > 1) {
            int i = Math.min(offset + fm2, n - 1);
            cb.showCaption(fmt("FIB_PROBE", i, fm2));
            cb.setCellActive(i); cb.sleep(); state.comparisons++;
            int c = cb.compareValues(arr[i], state.currentTarget);
            if (c < 0)      { cb.showCaption(fmt("FIB_RIGHT")); fib=fm1; fm1=fm2; fm2=fib-fm1; offset=i; cb.setCellScanned(i); }
            else if (c > 0) { cb.showCaption(fmt("FIB_LEFT"));  fib=fm2; fm1-=fm2; fm2=fib-fm1; cb.setCellScanned(i); }
            else            { cb.setCellFound(i); state.resultIndex=i; cb.setStatus("found at index "+i); return; }
            cb.sleep();
        }
        if (fm1 == 1 && offset+1 < n) {
            int i = offset+1;
            cb.setCellActive(i); cb.sleep(); state.comparisons++;
            if (matches(arr[i], state.currentTarget)) { cb.setCellFound(i); state.resultIndex=i; cb.setStatus("found at index "+i); return; }
            cb.setCellScanned(i);
        }
        state.resultIndex = -1; cb.setStatus("not found");
    }

    void runGraphAlgo() throws InterruptedException {
        state.resetGraphState();
        boolean isBFS = state.selectedAlgo.equals("Breadth-First Search");
        Queue<String> queue = new LinkedList<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String>   visited = new LinkedHashSet<>();
        Map<String, String> parent = new LinkedHashMap<>();
        if (isBFS) queue.add(state.graphStart); else stack.push(state.graphStart);
        parent.put(state.graphStart, null);
        boolean found = false;

        while (isBFS ? !queue.isEmpty() : !stack.isEmpty()) {
            String node = isBFS ? queue.poll() : stack.pop();
            if (visited.contains(node)) continue;
            visited.add(node); state.visitOrder.add(node); state.comparisons++;
            state.nodeAnim.put(node, 0f);
            cb.showCaption(fmt("GRAPH_VISIT", node, state.graphTarget));
            updateGraphState(visited, node, Collections.emptySet(), null);
            cb.sleep();

            if (node.equals(state.graphTarget)) {
                List<String> path = new ArrayList<>();
                String cur = node;
                while (cur != null) { path.add(0, cur); cur = parent.get(cur); }
                cb.showCaption(fmt("GRAPH_PATH", path));
                updateGraphState(visited, null, new HashSet<>(path), path);
                state.resultIndex = state.visitOrder.size() - 1;
                cb.setStatus("found '" + state.graphTarget + "' — path: " + path);
                found = true; break;
            }

            List<String> neighbors = new ArrayList<>(state.currentGraph.getOrDefault(node, new ArrayList<>()));
            if (!isBFS) Collections.reverse(neighbors);
            List<String> fresh = new ArrayList<>();
            for (String nb : neighbors) {
                if (!visited.contains(nb)) {
                    parent.putIfAbsent(nb, node);
                    if (isBFS) queue.add(nb); else stack.push(nb);
                    fresh.add(nb);
                }
            }
            if (!fresh.isEmpty()) cb.showCaption(fmt(isBFS ? "GRAPH_ENQUEUE" : "GRAPH_PUSH", node, fresh));
        }
        state.elapsedNs = 0;
        if (!found) { state.resultIndex = -1; cb.setStatus("'" + state.graphTarget + "' not reachable"); }
    }

    private void updateGraphState(Set<String> visited, String current, Set<String> path, List<String> pathList) {
        state.graphVisited  = new HashSet<>(visited);
        state.graphCurrent  = current;
        state.graphPath     = new HashSet<>(path);
        state.graphPathList = pathList;
        cb.repaint();
    }

    void runStringAlgo() throws InterruptedException {
        state.resetStringState();
        String t = state.ignoreCase ? state.currentText.toLowerCase()    : state.currentText;
        String p = state.ignoreCase ? state.currentPattern.toLowerCase() : state.currentPattern;
        int n = t.length(), m = p.length();
        state.strText    = t;
        state.strPattern = p;
        if (p.isEmpty() || n == 0) { state.resultIndex = -1; cb.setStatus("text or pattern is empty"); return; }
        if (m > n)                 { state.resultIndex = -1; cb.setStatus("pattern longer than text");  return; }

        long t0 = System.nanoTime();
        state.strIsKMP = state.selectedAlgo.equals("KMP Search");
        if (state.strIsKMP) runKMP(t, p, n, m);
        else                runRabinKarp(t, p, n, m);
        state.elapsedNs = System.nanoTime() - t0;

        state.strWindowStart  = -1;
        state.strMatchedChars = 0;
        cb.repaint();
        state.resultIndex = state.matchPositions.isEmpty() ? -1 : state.matchPositions.get(0);
        cb.setStatus(state.matchPositions.isEmpty() ? "no matches found"
            : state.matchPositions.size() + " match(es) at positions " + state.matchPositions);
    }

    private void runKMP(String t, String p, int n, int m) throws InterruptedException {
        int[] lps = buildLPS(p);
        state.strLPS = lps;
        int i = 0, j = 0;
        while (i < n) {
            state.strWindowStart  = i - j;
            state.strMatchedChars = j;
            state.strMismatch     = false;
            state.strKmpJ         = j;
            state.comparisons++;
            cb.repaint();

            if (t.charAt(i) == p.charAt(j)) {
                cb.showCaption(fmt("KMP_MATCH", i, t.charAt(i), j, p.charAt(j)));
                cb.sleep();
                i++; j++;
                if (j == m) {
                    int ms = i - j;
                    state.matchPositions.add(ms);
                    state.strWindowStart  = ms;
                    state.strMatchedChars = m;
                    cb.repaint();
                    cb.showCaption(fmt("KMP_FOUND", ms));
                    cb.sleep();
                    j = lps[j - 1];
                }
            } else {
                state.strMismatch     = true;
                state.strMatchedChars = j;
                cb.repaint();
                int nextJ = (j > 0) ? lps[j - 1] : 0;
                cb.showCaption(fmt("KMP_MISMATCH", i, nextJ));
                cb.sleep();
                if (j > 0) j = lps[j - 1]; else i++;
            }
        }
    }

    private void runRabinKarp(String t, String p, int n, int m) throws InterruptedException {
        final long BASE = 257L, MOD = 1_000_000_007L;
        long ph = 0, wh = 0, pw = 1;
        for (int i = 0; i < m; i++) {
            ph = (ph * BASE + p.charAt(i)) % MOD;
            wh = (wh * BASE + t.charAt(i)) % MOD;
            if (i > 0) pw = (pw * BASE) % MOD;
        }
        state.strPatHash = ph;

        for (int i = 0; i <= n - m; i++) {
            if (i > 0) {
                wh = (wh - t.charAt(i-1) * pw % MOD + MOD) % MOD;
                wh = (wh * BASE + t.charAt(i + m - 1)) % MOD;
            }
            state.strWindowStart  = i;
            state.strHashVal      = wh;
            state.strMismatch     = false;
            state.strMatchedChars = 0;
            state.comparisons++;
            cb.repaint();

            if (wh == ph) {
                cb.showCaption(fmt("RK_HASHMATCH", i));
                cb.sleep();
                boolean match = t.substring(i, i + m).equals(p);
                if (match) {
                    state.matchPositions.add(i);
                    state.strMatchedChars = m;
                    cb.repaint();
                    cb.showCaption(fmt("RK_FOUND", i));
                    cb.sleep();
                } else {
                    state.strMismatch = true;
                    cb.repaint();
                    cb.sleep();
                }
            } else {
                cb.showCaption(fmt("RK_WINDOW", i));
                cb.sleep();
            }
        }
    }

    private int[] buildLPS(String p) {
        int m = p.length(), len = 0, i = 1;
        int[] lps = new int[m];
        while (i < m) {
            if (p.charAt(i) == p.charAt(len)) { lps[i++] = ++len; }
            else if (len > 0) { len = lps[len - 1]; }
            else { lps[i++] = 0; }
        }
        return lps;
    }
}