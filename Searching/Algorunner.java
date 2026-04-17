import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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
        int compareValues(Object a, Object b);
    }

    private final AnimState state;
    private final Callbacks cb;

    AlgoRunner(AnimState state, Callbacks cb) {
        this.state = state;
        this.cb = cb;
    }

    private boolean matches(Object a, Object b) {
        return cb.compareValues(a, b) == 0;
    }

    private String fmt(String key, Object... args) {
        String template = AppData.CAPTIONS.get(key);
        if (template == null) {
            return key;
        }
        try {
            return String.format(template, args);
        } catch (Exception ignored) {
            return template;
        }
    }

    void runArrayAlgo() throws InterruptedException {
        state.resetArrayState();
        long startedAt = System.nanoTime();
        String algo = state.selectedAlgo;
        if (algo.equals("Linear Search")) {
            runLinear();
        } else if (algo.equals("Binary Search")) {
            runBinary();
        } else if (algo.equals("Ternary Search")) {
            runTernary();
        } else if (algo.equals("Jump Search")) {
            runJump();
        } else if (algo.equals("Interpolation Search")) {
            runInterpolation();
        } else if (algo.equals("Exponential Search")) {
            runExponential();
        } else if (algo.equals("Fibonacci Search")) {
            runFibonacci();
        }
        state.elapsedNs = System.nanoTime() - startedAt;
    }

    void runSortAlgo() throws InterruptedException {
        state.resetSortState();
        int[] input = requireSortableIntArray();
        SortEngine.SortRun run = SortEngine.run(state.selectedAlgo, input);

        for (SortEngine.SortFrame frame : run.getFrames()) {
            applySortFrame(frame);
            cb.setStatus(frame.getDescription());
            cb.showCaption(frame.getDescription());
            cb.repaint();
            cb.sleep();
        }

        state.elapsedNs = run.getElapsedNs();
        state.comparisons = run.getComparisons();
        state.swaps = run.getSwaps();
        state.sortCompleted = true;
        cb.setStatus("sorted array ready");
        cb.repaint();
    }

    private int[] requireSortableIntArray() {
        if (state.currentArray == null || state.currentArray.length == 0) {
            throw new IllegalArgumentException("Provide at least one integer to sort.");
        }
        int[] arr = new int[state.currentArray.length];
        for (int i = 0; i < state.currentArray.length; i++) {
            Object value = state.currentArray[i];
            if (!(value instanceof Number)) {
                throw new IllegalArgumentException("Sorting requires comma-separated integers.");
            }
            double asDouble = ((Number) value).doubleValue();
            if (Math.rint(asDouble) != asDouble) {
                throw new IllegalArgumentException("Sorting requires whole integers only.");
            }
            arr[i] = (int) asDouble;
        }
        return arr;
    }

    private void applySortFrame(SortEngine.SortFrame frame) {
        int[] snapshot = frame.getArray();
        Object[] boxed = new Object[snapshot.length];
        for (int i = 0; i < snapshot.length; i++) {
            boxed[i] = snapshot[i];
        }
        state.currentArray = boxed;
        state.comparisons = frame.getComparisons();
        state.swaps = frame.getSwaps();
        state.sortHighlightedIndices = new LinkedHashSet<>(frame.getHighlightedIndices());
        state.sortLockedIndices = new LinkedHashSet<>(frame.getLockedIndices());
        state.sortPivotIndex = frame.getPivotIndex();
        state.sortStepType = frame.getType();
        state.sortCompleted = frame.getType() == SortEngine.StepType.FINAL;
    }

    private void runLinear() throws InterruptedException {
        Object[] arr = state.currentArray;
        for (int i = 0; i < arr.length; i++) {
            cb.showCaption(fmt("LINEAR_CHECK", i, arr[i], state.currentTarget));
            cb.setCellActive(i);
            cb.sleep();
            state.comparisons++;
            if (matches(arr[i], state.currentTarget)) {
                cb.setCellFound(i);
                cb.showCaption(fmt("LINEAR_FOUND", i, arr[i], i));
                state.resultIndex = i;
                cb.setStatus("found at index " + i);
                return;
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
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            cb.showCaption(fmt("BINARY_MID", mid, arr[mid], state.currentTarget));
            cb.setCellActive(mid);
            cb.sleep();
            state.comparisons++;
            int comparison = cb.compareValues(arr[mid], state.currentTarget);
            if (comparison == 0) {
                cb.setCellFound(mid);
                cb.showCaption(fmt("BINARY_FOUND", mid));
                state.resultIndex = mid;
                cb.setStatus("found at index " + mid);
                return;
            }
            cb.setCellScanned(mid);
            if (comparison < 0) {
                cb.showCaption(fmt("BINARY_RIGHT"));
                cb.markScanned(low, mid - 1);
                low = mid + 1;
            } else {
                cb.showCaption(fmt("BINARY_LEFT"));
                cb.markScanned(mid + 1, high);
                high = mid - 1;
            }
            cb.sleep();
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    private void runTernary() throws InterruptedException {
        Object[] arr = state.currentArray;
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int third = (high - low) / 3;
            int mid1 = low + third;
            int mid2 = high - third;
            cb.showCaption(fmt("TERNARY_MIDS", mid1, arr[mid1], mid2, arr[mid2]));
            cb.setCellActive(mid1);
            cb.setCellActive(mid2);
            cb.sleep();
            state.comparisons += 2;

            int cmp1 = cb.compareValues(arr[mid1], state.currentTarget);
            int cmp2 = cb.compareValues(arr[mid2], state.currentTarget);
            if (cmp1 == 0) {
                cb.setCellFound(mid1);
                state.resultIndex = mid1;
                cb.setStatus("found at index " + mid1);
                return;
            }
            if (cmp2 == 0) {
                cb.setCellFound(mid2);
                state.resultIndex = mid2;
                cb.setStatus("found at index " + mid2);
                return;
            }

            cb.setCellScanned(mid1);
            cb.setCellScanned(mid2);
            if (cb.compareValues(state.currentTarget, arr[mid1]) < 0) {
                cb.showCaption(fmt("TERNARY_LEFT"));
                cb.markScanned(mid1 + 1, high);
                high = mid1 - 1;
            } else if (cb.compareValues(state.currentTarget, arr[mid2]) > 0) {
                cb.showCaption(fmt("TERNARY_RIGHT"));
                cb.markScanned(low, mid2 - 1);
                low = mid2 + 1;
            } else {
                cb.showCaption(fmt("TERNARY_MID"));
                cb.markScanned(low, mid1 - 1);
                cb.markScanned(mid2 + 1, high);
                low = mid1 + 1;
                high = mid2 - 1;
            }
            cb.sleep();
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    private void runJump() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length;
        int step = (int) Math.floor(Math.sqrt(n));
        int previous = 0;
        int current = step;
        while (current < n && cb.compareValues(arr[current], state.currentTarget) <= 0) {
            cb.showCaption(fmt("JUMP_JUMP", step, current));
            cb.setCellActive(current);
            cb.sleep();
            state.comparisons++;
            cb.setCellScanned(current);
            previous = current;
            current += step;
        }
        cb.showCaption(fmt("JUMP_LINEAR", previous));
        for (int i = previous; i < Math.min(current, n); i++) {
            cb.setCellActive(i);
            cb.sleep();
            state.comparisons++;
            if (matches(arr[i], state.currentTarget)) {
                cb.setCellFound(i);
                state.resultIndex = i;
                cb.setStatus("found at index " + i);
                return;
            }
            cb.setCellScanned(i);
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    private void runInterpolation() throws InterruptedException {
        Object[] arr = state.currentArray;
        int low = 0;
        int high = arr.length - 1;
        while (low <= high) {
            int position;
            if (arr[low] instanceof Number && state.currentTarget instanceof Number) {
                double lowValue = ((Number) arr[low]).doubleValue();
                double highValue = ((Number) arr[high]).doubleValue();
                double targetValue = ((Number) state.currentTarget).doubleValue();
                position = highValue == lowValue
                    ? low
                    : low + (int) (((targetValue - lowValue) / (highValue - lowValue)) * (high - low));
            } else {
                position = (low + high) / 2;
            }
            if (position < low || position > high) {
                break;
            }
            cb.showCaption(fmt("INTERP_POS", position));
            cb.setCellActive(position);
            cb.sleep();
            state.comparisons++;
            int comparison = cb.compareValues(arr[position], state.currentTarget);
            if (comparison == 0) {
                cb.setCellFound(position);
                state.resultIndex = position;
                cb.setStatus("found at index " + position);
                return;
            }
            cb.setCellScanned(position);
            if (comparison < 0) {
                cb.showCaption(fmt("INTERP_LEFT"));
                low = position + 1;
            } else {
                cb.showCaption(fmt("INTERP_RIGHT"));
                high = position - 1;
            }
            cb.sleep();
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    private void runExponential() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length;
        cb.setCellActive(0);
        cb.sleep();
        state.comparisons++;
        if (matches(arr[0], state.currentTarget)) {
            cb.setCellFound(0);
            state.resultIndex = 0;
            cb.setStatus("found at index 0");
            return;
        }
        cb.setCellScanned(0);

        int bound = 1;
        while (bound < n && cb.compareValues(arr[bound], state.currentTarget) <= 0) {
            cb.showCaption(fmt("EXP_BOUND", bound));
            cb.setCellActive(bound);
            cb.sleep();
            state.comparisons++;
            cb.setCellScanned(bound);
            bound *= 2;
        }

        int low = bound / 2;
        int high = Math.min(bound, n - 1);
        cb.showCaption(fmt("EXP_BINARY", low, high));
        cb.sleep();
        while (low <= high) {
            int mid = (low + high) / 2;
            cb.setCellActive(mid);
            cb.sleep();
            state.comparisons++;
            int comparison = cb.compareValues(arr[mid], state.currentTarget);
            if (comparison == 0) {
                cb.setCellFound(mid);
                state.resultIndex = mid;
                cb.setStatus("found at index " + mid);
                return;
            }
            cb.setCellScanned(mid);
            if (comparison < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    private void runFibonacci() throws InterruptedException {
        Object[] arr = state.currentArray;
        int n = arr.length;
        int fibMm2 = 0;
        int fibMm1 = 1;
        int fib = 1;
        while (fib < n) {
            fibMm2 = fibMm1;
            fibMm1 = fib;
            fib = fibMm1 + fibMm2;
        }
        int offset = -1;
        while (fib > 1) {
            int i = Math.min(offset + fibMm2, n - 1);
            cb.showCaption(fmt("FIB_PROBE", i, fibMm2));
            cb.setCellActive(i);
            cb.sleep();
            state.comparisons++;
            int comparison = cb.compareValues(arr[i], state.currentTarget);
            if (comparison < 0) {
                cb.showCaption(fmt("FIB_RIGHT"));
                fib = fibMm1;
                fibMm1 = fibMm2;
                fibMm2 = fib - fibMm1;
                offset = i;
                cb.setCellScanned(i);
            } else if (comparison > 0) {
                cb.showCaption(fmt("FIB_LEFT"));
                fib = fibMm2;
                fibMm1 -= fibMm2;
                fibMm2 = fib - fibMm1;
                cb.setCellScanned(i);
            } else {
                cb.setCellFound(i);
                state.resultIndex = i;
                cb.setStatus("found at index " + i);
                return;
            }
            cb.sleep();
        }
        if (fibMm1 == 1 && offset + 1 < n) {
            int i = offset + 1;
            cb.setCellActive(i);
            cb.sleep();
            state.comparisons++;
            if (matches(arr[i], state.currentTarget)) {
                cb.setCellFound(i);
                state.resultIndex = i;
                cb.setStatus("found at index " + i);
                return;
            }
            cb.setCellScanned(i);
        }
        state.resultIndex = -1;
        cb.setStatus("not found");
    }

    void runGraphAlgo() throws InterruptedException {
        state.resetGraphState();
        boolean breadthFirst = state.selectedAlgo.equals("Breadth-First Search");
        Queue<String> queue = new LinkedList<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        Map<String, String> parent = new LinkedHashMap<>();

        if (breadthFirst) {
            queue.add(state.graphStart);
        } else {
            stack.push(state.graphStart);
        }
        parent.put(state.graphStart, null);

        boolean found = false;
        while (breadthFirst ? !queue.isEmpty() : !stack.isEmpty()) {
            String node = breadthFirst ? queue.poll() : stack.pop();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            state.visitOrder.add(node);
            state.comparisons++;
            state.nodeAnim.put(node, 0f);

            cb.showCaption(fmt("GRAPH_VISIT", node, state.graphTarget));
            updateGraphState(visited, node, Collections.emptySet(), null);
            cb.sleep();

            if (node.equals(state.graphTarget)) {
                List<String> path = new ArrayList<>();
                String current = node;
                while (current != null) {
                    path.add(0, current);
                    current = parent.get(current);
                }
                cb.showCaption(fmt("GRAPH_PATH", path));
                updateGraphState(visited, null, new HashSet<>(path), path);
                state.resultIndex = state.visitOrder.size() - 1;
                cb.setStatus("found target '" + state.graphTarget + "' via path " + path);
                found = true;
                break;
            }

            List<String> neighbors = new ArrayList<>(state.currentGraph.getOrDefault(node, new ArrayList<>()));
            if (!breadthFirst) {
                Collections.reverse(neighbors);
            }
            List<String> freshNeighbors = new ArrayList<>();
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    parent.putIfAbsent(neighbor, node);
                    if (breadthFirst) {
                        queue.add(neighbor);
                    } else {
                        stack.push(neighbor);
                    }
                    freshNeighbors.add(neighbor);
                }
            }
            if (!freshNeighbors.isEmpty()) {
                cb.showCaption(fmt(breadthFirst ? "GRAPH_ENQUEUE" : "GRAPH_PUSH", node, freshNeighbors));
            }
        }

        state.elapsedNs = 0;
        if (!found) {
            state.resultIndex = -1;
            cb.setStatus("target '" + state.graphTarget + "' was not reachable");
        }
    }

    private void updateGraphState(Set<String> visited, String current, Set<String> path, List<String> pathList) {
        state.graphVisited = new HashSet<>(visited);
        state.graphCurrent = current;
        state.graphPath = new HashSet<>(path);
        state.graphPathList = pathList;
        cb.repaint();
    }

    void runStringAlgo() throws InterruptedException {
        state.resetStringState();
        String text = state.ignoreCase ? state.currentText.toLowerCase() : state.currentText;
        String pattern = state.ignoreCase ? state.currentPattern.toLowerCase() : state.currentPattern;
        int n = text.length();
        int m = pattern.length();
        state.strText = text;
        state.strPattern = pattern;

        if (pattern.isEmpty() || n == 0) {
            state.resultIndex = -1;
            cb.setStatus("text or pattern is empty");
            return;
        }
        if (m > n) {
            state.resultIndex = -1;
            cb.setStatus("pattern is longer than the text");
            return;
        }

        long startedAt = System.nanoTime();
        state.strIsKMP = state.selectedAlgo.equals("KMP Search");
        if (state.strIsKMP) {
            runKmp(text, pattern, n, m);
        } else {
            runRabinKarp(text, pattern, n, m);
        }
        state.elapsedNs = System.nanoTime() - startedAt;

        state.strWindowStart = -1;
        state.strMatchedChars = 0;
        cb.repaint();
        state.resultIndex = state.matchPositions.isEmpty() ? -1 : state.matchPositions.get(0);
        if (state.matchPositions.isEmpty()) {
            cb.setStatus("no matches found");
        } else {
            cb.setStatus(state.matchPositions.size() + " match(es) at positions " + state.matchPositions);
        }
    }

    private void runKmp(String text, String pattern, int n, int m) throws InterruptedException {
        int[] lps = buildLps(pattern);
        state.strLPS = lps;
        int i = 0;
        int j = 0;
        while (i < n) {
            state.strWindowStart = i - j;
            state.strMatchedChars = j;
            state.strMismatch = false;
            state.strKmpJ = j;
            state.comparisons++;
            cb.repaint();

            if (text.charAt(i) == pattern.charAt(j)) {
                cb.showCaption(fmt("KMP_MATCH", i, text.charAt(i), j, pattern.charAt(j)));
                cb.sleep();
                i++;
                j++;
                if (j == m) {
                    int matchStart = i - j;
                    state.matchPositions.add(matchStart);
                    state.strWindowStart = matchStart;
                    state.strMatchedChars = m;
                    cb.repaint();
                    cb.showCaption(fmt("KMP_FOUND", matchStart));
                    cb.sleep();
                    j = lps[j - 1];
                }
            } else {
                state.strMismatch = true;
                state.strMatchedChars = j;
                cb.repaint();
                int nextJ = j > 0 ? lps[j - 1] : 0;
                cb.showCaption(fmt("KMP_MISMATCH", i, nextJ));
                cb.sleep();
                if (j > 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }
    }

    private void runRabinKarp(String text, String pattern, int n, int m) throws InterruptedException {
        final long base = 257L;
        final long mod = 1_000_000_007L;
        long patternHash = 0;
        long windowHash = 0;
        long power = 1;
        for (int i = 0; i < m; i++) {
            patternHash = (patternHash * base + pattern.charAt(i)) % mod;
            windowHash = (windowHash * base + text.charAt(i)) % mod;
            if (i > 0) {
                power = (power * base) % mod;
            }
        }
        state.strPatHash = patternHash;

        for (int i = 0; i <= n - m; i++) {
            if (i > 0) {
                windowHash = (windowHash - text.charAt(i - 1) * power % mod + mod) % mod;
                windowHash = (windowHash * base + text.charAt(i + m - 1)) % mod;
            }
            state.strWindowStart = i;
            state.strHashVal = windowHash;
            state.strMismatch = false;
            state.strMatchedChars = 0;
            state.comparisons++;
            cb.repaint();

            if (windowHash == patternHash) {
                cb.showCaption(fmt("RK_HASHMATCH", i));
                cb.sleep();
                if (text.substring(i, i + m).equals(pattern)) {
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

    private int[] buildLps(String pattern) {
        int[] lps = new int[pattern.length()];
        int length = 0;
        int i = 1;
        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i++] = ++length;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }
}
