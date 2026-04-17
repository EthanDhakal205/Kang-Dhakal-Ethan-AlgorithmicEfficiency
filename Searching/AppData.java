import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AppData {

    static final Map<String, String> CAPTIONS = new HashMap<>();
    static final Map<String, String> CODE_MAP = new HashMap<>();
    private static final Map<String, String> TIME_MAP = new HashMap<>();
    private static final Map<String, String> SPACE_MAP = new HashMap<>();
    static final List<String[]> APP_DATA = new ArrayList<>();

    static {
        CAPTIONS.put("LINEAR_CHECK", "Checking arr[%d] = %s against target %s.");
        CAPTIONS.put("LINEAR_MISS", "arr[%d] = %s is not the target. Move forward.");
        CAPTIONS.put("LINEAR_FOUND", "Found it. arr[%d] = %s matches the target at index %d.");
        CAPTIONS.put("LINEAR_NOTFOUND", "Reached the end of the array. The target is not present.");
        CAPTIONS.put("BINARY_MID", "Midpoint is arr[%d] = %s. Compare it with target %s.");
        CAPTIONS.put("BINARY_LEFT", "Target is smaller, so discard the right half.");
        CAPTIONS.put("BINARY_RIGHT", "Target is larger, so discard the left half.");
        CAPTIONS.put("BINARY_FOUND", "Binary Search found the target at index %d.");
        CAPTIONS.put("TERNARY_MIDS", "Probe mid1[%d]=%s and mid2[%d]=%s to split the range into thirds.");
        CAPTIONS.put("TERNARY_LEFT", "Target is left of mid1, so keep the left third.");
        CAPTIONS.put("TERNARY_RIGHT", "Target is right of mid2, so keep the right third.");
        CAPTIONS.put("TERNARY_MID", "Target is between mid1 and mid2, so keep the middle third.");
        CAPTIONS.put("JUMP_JUMP", "Jump ahead by block size %d to index %d.");
        CAPTIONS.put("JUMP_LINEAR", "Target must be in the current block, so switch to linear scan from %d.");
        CAPTIONS.put("INTERP_POS", "Estimate the probe index from the value distribution: %d.");
        CAPTIONS.put("INTERP_LEFT", "Probe value is smaller than the target. Shift right.");
        CAPTIONS.put("INTERP_RIGHT", "Probe value is larger than the target. Shift left.");
        CAPTIONS.put("EXP_BOUND", "Doubling the bound to %d to locate a useful search range.");
        CAPTIONS.put("EXP_BINARY", "Range located: [%d, %d]. Switch to binary search inside it.");
        CAPTIONS.put("FIB_PROBE", "Fibonacci probe at index %d using fibM2 = %d.");
        CAPTIONS.put("FIB_LEFT", "Probe value is too large. Reduce the Fibonacci window to the left.");
        CAPTIONS.put("FIB_RIGHT", "Probe value is too small. Shift the Fibonacci window right.");
        CAPTIONS.put("GRAPH_VISIT", "Visiting node '%s'. Is it the target '%s'?");
        CAPTIONS.put("GRAPH_ENQUEUE", "Queueing the neighbors of '%s': %s");
        CAPTIONS.put("GRAPH_PUSH", "Pushing the neighbors of '%s' onto the stack: %s");
        CAPTIONS.put("GRAPH_PATH", "Target found. Reconstructed path: %s");
        CAPTIONS.put("KMP_MATCH", "text[%d]='%s' matches pattern[%d]='%s'. Advance both pointers.");
        CAPTIONS.put("KMP_MISMATCH", "Mismatch at text[%d]. Fall back in the pattern to LPS position %d.");
        CAPTIONS.put("KMP_FOUND", "Full pattern match found at text position %d.");
        CAPTIONS.put("RK_WINDOW", "Rolling hash window at position %d.");
        CAPTIONS.put("RK_HASHMATCH", "Hash match at position %d. Verify characters directly.");
        CAPTIONS.put("RK_FOUND", "Confirmed substring match at position %d.");

        CODE_MAP.put("Linear Search", lines(
            "public int linearSearch(Object[] arr, Object target) {",
            "    for (int i = 0; i < arr.length; i++) {",
            "        if (compare(arr[i], target) == 0) return i;",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Binary Search", lines(
            "public int binarySearch(Object[] arr, Object target) {",
            "    int low = 0, high = arr.length - 1;",
            "    while (low <= high) {",
            "        int mid = (low + high) / 2;",
            "        int cmp = compare(arr[mid], target);",
            "        if (cmp == 0) return mid;",
            "        if (cmp < 0) low = mid + 1;",
            "        else high = mid - 1;",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Ternary Search", lines(
            "public int ternarySearch(Object[] arr, Object target) {",
            "    int low = 0, high = arr.length - 1;",
            "    while (low <= high) {",
            "        int third = (high - low) / 3;",
            "        int mid1 = low + third, mid2 = high - third;",
            "        if (compare(arr[mid1], target) == 0) return mid1;",
            "        if (compare(arr[mid2], target) == 0) return mid2;",
            "        if (compare(target, arr[mid1]) < 0) high = mid1 - 1;",
            "        else if (compare(target, arr[mid2]) > 0) low = mid2 + 1;",
            "        else { low = mid1 + 1; high = mid2 - 1; }",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Jump Search", lines(
            "public int jumpSearch(Object[] arr, Object target) {",
            "    int step = (int) Math.sqrt(arr.length);",
            "    int prev = 0, curr = step;",
            "    while (curr < arr.length && compare(arr[curr], target) <= 0) {",
            "        prev = curr;",
            "        curr += step;",
            "    }",
            "    for (int i = prev; i < Math.min(curr, arr.length); i++) {",
            "        if (compare(arr[i], target) == 0) return i;",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Interpolation Search", lines(
            "public int interpolationSearch(Object[] arr, Object target) {",
            "    int low = 0, high = arr.length - 1;",
            "    while (low <= high) {",
            "        int pos = estimate(low, high, target, arr);",
            "        if (pos < low || pos > high) break;",
            "        int cmp = compare(arr[pos], target);",
            "        if (cmp == 0) return pos;",
            "        if (cmp < 0) low = pos + 1;",
            "        else high = pos - 1;",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Exponential Search", lines(
            "public int exponentialSearch(Object[] arr, Object target) {",
            "    if (compare(arr[0], target) == 0) return 0;",
            "    int bound = 1;",
            "    while (bound < arr.length && compare(arr[bound], target) <= 0) {",
            "        bound *= 2;",
            "    }",
            "    return binarySearch(arr, target, bound / 2, Math.min(bound, arr.length - 1));",
            "}"
        ));
        CODE_MAP.put("Fibonacci Search", lines(
            "public int fibonacciSearch(Object[] arr, Object target) {",
            "    int fibMm2 = 0, fibMm1 = 1, fib = 1;",
            "    while (fib < arr.length) {",
            "        fibMm2 = fibMm1;",
            "        fibMm1 = fib;",
            "        fib = fibMm1 + fibMm2;",
            "    }",
            "    int offset = -1;",
            "    while (fib > 1) {",
            "        int i = Math.min(offset + fibMm2, arr.length - 1);",
            "        int cmp = compare(arr[i], target);",
            "        if (cmp < 0) { fib = fibMm1; fibMm1 = fibMm2; fibMm2 = fib - fibMm1; offset = i; }",
            "        else if (cmp > 0) { fib = fibMm2; fibMm1 -= fibMm2; fibMm2 = fib - fibMm1; }",
            "        else return i;",
            "    }",
            "    return -1;",
            "}"
        ));
        CODE_MAP.put("Breadth-First Search", lines(
            "public List<String> bfs(Map<String, List<String>> graph, String start, String target) {",
            "    Queue<String> queue = new LinkedList<>();",
            "    Map<String, String> parent = new HashMap<>();",
            "    Set<String> visited = new HashSet<>();",
            "    queue.add(start);",
            "    parent.put(start, null);",
            "    while (!queue.isEmpty()) {",
            "        String node = queue.poll();",
            "        if (!visited.add(node)) continue;",
            "        if (node.equals(target)) return buildPath(parent, target);",
            "        for (String next : graph.getOrDefault(node, List.of())) {",
            "            parent.putIfAbsent(next, node);",
            "            queue.add(next);",
            "        }",
            "    }",
            "    return List.of();",
            "}"
        ));
        CODE_MAP.put("Depth-First Search", lines(
            "public List<String> dfs(Map<String, List<String>> graph, String start, String target) {",
            "    Deque<String> stack = new ArrayDeque<>();",
            "    Map<String, String> parent = new HashMap<>();",
            "    Set<String> visited = new HashSet<>();",
            "    stack.push(start);",
            "    parent.put(start, null);",
            "    while (!stack.isEmpty()) {",
            "        String node = stack.pop();",
            "        if (!visited.add(node)) continue;",
            "        if (node.equals(target)) return buildPath(parent, target);",
            "        for (String next : reversed(graph.getOrDefault(node, List.of()))) {",
            "            parent.putIfAbsent(next, node);",
            "            stack.push(next);",
            "        }",
            "    }",
            "    return List.of();",
            "}"
        ));
        CODE_MAP.put("KMP Search", lines(
            "public List<Integer> kmpSearch(String text, String pattern) {",
            "    int[] lps = buildLps(pattern);",
            "    List<Integer> matches = new ArrayList<>();",
            "    for (int i = 0, j = 0; i < text.length();) {",
            "        if (text.charAt(i) == pattern.charAt(j)) {",
            "            i++; j++;",
            "            if (j == pattern.length()) {",
            "                matches.add(i - j);",
            "                j = lps[j - 1];",
            "            }",
            "        } else if (j > 0) {",
            "            j = lps[j - 1];",
            "        } else {",
            "            i++;",
            "        }",
            "    }",
            "    return matches;",
            "}"
        ));
        CODE_MAP.put("Rabin-Karp Search", lines(
            "public List<Integer> rabinKarp(String text, String pattern) {",
            "    long patternHash = hash(pattern);",
            "    long windowHash = hash(text.substring(0, pattern.length()));",
            "    List<Integer> matches = new ArrayList<>();",
            "    for (int i = 0; i <= text.length() - pattern.length(); i++) {",
            "        if (windowHash == patternHash && text.startsWith(pattern, i)) {",
            "            matches.add(i);",
            "        }",
            "        windowHash = rollHash(text, i, pattern.length(), windowHash);",
            "    }",
            "    return matches;",
            "}"
        ));
        CODE_MAP.put("Bubble Sort", lines(
            "public void bubbleSort(int[] arr) {",
            "    for (int i = 0; i < arr.length - 1; i++) {",
            "        boolean swapped = false;",
            "        for (int j = 0; j < arr.length - i - 1; j++) {",
            "            if (arr[j] > arr[j + 1]) {",
            "                swap(arr, j, j + 1);",
            "                swapped = true;",
            "            }",
            "        }",
            "        if (!swapped) break;",
            "    }",
            "}"
        ));
        CODE_MAP.put("Insertion Sort", lines(
            "public void insertionSort(int[] arr) {",
            "    for (int i = 1; i < arr.length; i++) {",
            "        int key = arr[i];",
            "        int j = i - 1;",
            "        while (j >= 0 && arr[j] > key) {",
            "            arr[j + 1] = arr[j];",
            "            j--;",
            "        }",
            "        arr[j + 1] = key;",
            "    }",
            "}"
        ));
        CODE_MAP.put("Selection Sort", lines(
            "public void selectionSort(int[] arr) {",
            "    for (int i = 0; i < arr.length - 1; i++) {",
            "        int minIndex = i;",
            "        for (int j = i + 1; j < arr.length; j++) {",
            "            if (arr[j] < arr[minIndex]) minIndex = j;",
            "        }",
            "        swap(arr, i, minIndex);",
            "    }",
            "}"
        ));
        CODE_MAP.put("Gnome Sort", lines(
            "public void gnomeSort(int[] arr) {",
            "    int index = 0;",
            "    while (index < arr.length) {",
            "        if (index == 0 || arr[index] >= arr[index - 1]) {",
            "            index++;",
            "        } else {",
            "            swap(arr, index, index - 1);",
            "            index--;",
            "        }",
            "    }",
            "}"
        ));
        CODE_MAP.put("Merge Sort", lines(
            "public void mergeSort(int[] arr, int left, int right) {",
            "    if (left >= right) return;",
            "    int mid = (left + right) / 2;",
            "    mergeSort(arr, left, mid);",
            "    mergeSort(arr, mid + 1, right);",
            "    merge(arr, left, mid, right);",
            "}"
        ));
        CODE_MAP.put("Quick Sort", lines(
            "public void quickSort(int[] arr, int low, int high) {",
            "    if (low >= high) return;",
            "    int pivotIndex = partition(arr, low, high);",
            "    quickSort(arr, low, pivotIndex - 1);",
            "    quickSort(arr, pivotIndex + 1, high);",
            "}"
        ));
        CODE_MAP.put("Heap Sort", lines(
            "public void heapSort(int[] arr) {",
            "    for (int i = arr.length / 2 - 1; i >= 0; i--) heapify(arr, arr.length, i);",
            "    for (int end = arr.length - 1; end > 0; end--) {",
            "        swap(arr, 0, end);",
            "        heapify(arr, end, 0);",
            "    }",
            "}"
        ));
        CODE_MAP.put("Tim Sort", lines(
            "public void timSort(int[] arr) {",
            "    final int RUN = 32;",
            "    for (int start = 0; start < arr.length; start += RUN) {",
            "        insertionSort(arr, start, Math.min(start + RUN - 1, arr.length - 1));",
            "    }",
            "    for (int size = RUN; size < arr.length; size *= 2) {",
            "        for (int left = 0; left < arr.length; left += 2 * size) {",
            "            merge(arr, left, left + size - 1, Math.min(left + 2 * size - 1, arr.length - 1));",
            "        }",
            "    }",
            "}"
        ));
        CODE_MAP.put("Radix Sort", lines(
            "public void radixSort(int[] arr) {",
            "    int max = Arrays.stream(arr).max().orElse(0);",
            "    for (int exp = 1; max / exp > 0; exp *= 10) {",
            "        countingSortByDigit(arr, exp);",
            "    }",
            "}"
        ));
        CODE_MAP.put("Bogo Sort", lines(
            "public void bogoSort(int[] arr) {",
            "    while (!isSorted(arr)) {",
            "        shuffle(arr);",
            "    }",
            "}"
        ));

        addProfile("Linear Search", "O(n)", "O(1)");
        addProfile("Binary Search", "O(log n)", "O(1)");
        addProfile("Ternary Search", "O(log3 n)", "O(1)");
        addProfile("Jump Search", "O(sqrt n)", "O(1)");
        addProfile("Interpolation Search", "O(log log n) average", "O(1)");
        addProfile("Exponential Search", "O(log n)", "O(1)");
        addProfile("Fibonacci Search", "O(log n)", "O(1)");
        addProfile("Breadth-First Search", "O(V + E)", "O(V)");
        addProfile("Depth-First Search", "O(V + E)", "O(V)");
        addProfile("KMP Search", "O(n + m)", "O(m)");
        addProfile("Rabin-Karp Search", "O(n + m) average", "O(1)");
        addProfile("Bubble Sort", "best O(n), average O(n^2), worst O(n^2)", "O(1)");
        addProfile("Insertion Sort", "best O(n), average O(n^2), worst O(n^2)", "O(1)");
        addProfile("Selection Sort", "O(n^2)", "O(1)");
        addProfile("Gnome Sort", "best O(n), average O(n^2), worst O(n^2)", "O(1)");
        addProfile("Merge Sort", "O(n log n)", "O(n)");
        addProfile("Quick Sort", "average O(n log n), worst O(n^2)", "O(log n)");
        addProfile("Heap Sort", "O(n log n)", "O(1)");
        addProfile("Tim Sort", "best O(n), average O(n log n), worst O(n log n)", "O(n)");
        addProfile("Radix Sort", "O(nk)", "O(n + k)");
        addProfile("Bogo Sort", "average O((n+1)!)", "O(1)");

        APP_DATA.add(new String[]{
            "Linear Search", "O(n)", "O(1)", "Small or unsorted data",
            "Scanning inventory, searching short logs, checking basic lists",
            "It works without any sorting or indexing."
        });
        APP_DATA.add(new String[]{
            "Binary Search", "O(log n)", "O(1)", "Large sorted arrays",
            "Dictionary lookups, sorted database indexes, static leaderboards",
            "Extremely fast once the data is sorted."
        });
        APP_DATA.add(new String[]{
            "Ternary Search", "O(log3 n)", "O(1)", "Specialized discrete searches",
            "Unimodal optimization problems, academic comparisons with Binary Search",
            "Usually less practical than Binary Search on arrays."
        });
        APP_DATA.add(new String[]{
            "Jump Search", "O(sqrt n)", "O(1)", "Block-oriented sorted data",
            "Sequential storage, page-based scans, systems with expensive random seeks",
            "Useful when jumping in chunks is cheaper than full random access."
        });
        APP_DATA.add(new String[]{
            "Interpolation Search", "O(log log n) average", "O(1)", "Uniformly distributed numeric data",
            "Phone directories, evenly spaced IDs, dense numeric lookup tables",
            "Fast on uniform data but can degrade badly on skewed input."
        });
        APP_DATA.add(new String[]{
            "Exponential Search", "O(log n)", "O(1)", "Unknown-size sorted data",
            "Unbounded streams, iterators over sorted files, range discovery before binary search",
            "It finds the window first, then applies Binary Search."
        });
        APP_DATA.add(new String[]{
            "Fibonacci Search", "O(log n)", "O(1)", "Systems that avoid division",
            "Embedded devices, older architectures, teaching alternative divide strategies",
            "It navigates the array with Fibonacci offsets instead of midpoint division."
        });
        APP_DATA.add(new String[]{
            "Breadth-First Search", "O(V + E)", "O(V)", "Shortest path in unweighted graphs",
            "Social graph hops, routing on unweighted maps, crawler frontier expansion",
            "BFS guarantees the shortest unweighted path."
        });
        APP_DATA.add(new String[]{
            "Depth-First Search", "O(V + E)", "O(V)", "Deep traversal and backtracking",
            "Cycle detection, dependency walks, maze solving, topological workflows",
            "DFS uses less memory on wide graphs but does not guarantee shortest paths."
        });
        APP_DATA.add(new String[]{
            "KMP Search", "O(n + m)", "O(m)", "Single-pattern text search",
            "Editors, log scanners, DNA sequence matching, content moderation",
            "The LPS table avoids re-checking characters."
        });
        APP_DATA.add(new String[]{
            "Rabin-Karp Search", "O(n + m) average", "O(1)", "Hash-based substring search",
            "Plagiarism checks, multi-pattern filters, rolling-hash pipelines",
            "Hash collisions require a direct verification step."
        });
        APP_DATA.add(new String[]{
            "Bubble Sort", "O(n^2)", "O(1)", "Teaching and tiny arrays",
            "Intro CS demos, simple visualizers, swap-heavy animation examples",
            "Easy to explain, but rarely a production choice."
        });
        APP_DATA.add(new String[]{
            "Insertion Sort", "O(n^2)", "O(1)", "Small or nearly sorted arrays",
            "Hybrid sort base cases, incremental maintenance of ordered data",
            "Excellent on tiny inputs and already-ordered data."
        });
        APP_DATA.add(new String[]{
            "Selection Sort", "O(n^2)", "O(1)", "Low-write environments",
            "Memory-constrained demos, cases where swaps are more expensive than comparisons",
            "It minimizes swaps but still pays quadratic comparison cost."
        });
        APP_DATA.add(new String[]{
            "Gnome Sort", "O(n^2)", "O(1)", "Educational comparisons",
            "Visual explanations of local swap behavior and backtracking",
            "Mostly used because it is simple and visually distinctive."
        });
        APP_DATA.add(new String[]{
            "Merge Sort", "O(n log n)", "O(n)", "Stable large-scale sorting",
            "Linked-list sorting, external sorting, stable batch processing",
            "Predictable performance and stability are its main strengths."
        });
        APP_DATA.add(new String[]{
            "Quick Sort", "O(n log n) average", "O(log n)", "Fast in-memory sorting",
            "General-purpose array sorting, partition-based divide and conquer",
            "Very fast in practice, but pivot choice matters."
        });
        APP_DATA.add(new String[]{
            "Heap Sort", "O(n log n)", "O(1)", "Guaranteed in-place sorting",
            "Systems that need bounded memory with reliable worst-case performance",
            "It trades cache-friendliness for strict memory usage."
        });
        APP_DATA.add(new String[]{
            "Tim Sort", "O(n log n)", "O(n)", "Real-world application sorting",
            "Java and Python runtime sorting, partially ordered business data",
            "It exploits existing runs and is used in production runtimes."
        });
        APP_DATA.add(new String[]{
            "Radix Sort", "O(nk)", "O(n + k)", "Integer-heavy non-comparison sorting",
            "Sorting IDs, ZIP codes, bounded integers, fixed-width numeric keys",
            "It is fast when the key width is limited and values are non-negative."
        });
        APP_DATA.add(new String[]{
            "Bogo Sort", "O((n+1)!)", "O(1)", "Cautionary examples only",
            "Comedy demos, impossibility lessons, worst-case algorithm discussions",
            "This exists to show what not to do."
        });
    }

    static String getTimeComplexity(String algorithm) {
        return TIME_MAP.getOrDefault(algorithm, "-");
    }

    static String getSpaceComplexity(String algorithm) {
        return SPACE_MAP.getOrDefault(algorithm, "-");
    }

    private static void addProfile(String algorithm, String time, String space) {
        TIME_MAP.put(algorithm, time);
        SPACE_MAP.put(algorithm, space);
    }

    private static String lines(String... lines) {
        return String.join("\n", lines);
    }
}
