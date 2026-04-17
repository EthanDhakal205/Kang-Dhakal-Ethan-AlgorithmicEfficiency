import java.util.*;

class AppData {

    static final Map<String, String> CAPTIONS = new HashMap<>();
    static {
        CAPTIONS.put("LINEAR_CHECK",    "Checking arr[%d] = %s — equal to target %s?");
        CAPTIONS.put("LINEAR_MISS",     "arr[%d] = %s is not the target. Moving forward.");
        CAPTIONS.put("LINEAR_FOUND",    "Found! arr[%d] = %s matches target. Returning index %d.");
        CAPTIONS.put("LINEAR_NOTFOUND", "Reached end of array. Target not present. Returning -1.");
        CAPTIONS.put("BINARY_MID",      "Mid is index %d (value %s). Comparing to target %s.");
        CAPTIONS.put("BINARY_LEFT",     "Target < mid. Discarding right half, searching left.");
        CAPTIONS.put("BINARY_RIGHT",    "Target > mid. Discarding left half, searching right.");
        CAPTIONS.put("BINARY_FOUND",    "Match found at index %d!");
        CAPTIONS.put("TERNARY_MIDS",    "Splitting into thirds — probing mid1[%d]=%s and mid2[%d]=%s.");
        CAPTIONS.put("TERNARY_LEFT",    "Target < mid1. Narrowing to the left third.");
        CAPTIONS.put("TERNARY_RIGHT",   "Target > mid2. Narrowing to the right third.");
        CAPTIONS.put("TERNARY_MID",     "Target between mid1 and mid2. Narrowing to middle third.");
        CAPTIONS.put("JUMP_JUMP",       "Jumping by step %d to index %d — checking if we overshot target.");
        CAPTIONS.put("JUMP_LINEAR",     "Overshot! Doing linear scan backwards from index %d.");
        CAPTIONS.put("INTERP_POS",      "Estimating probe position by value distribution — landing at index %d.");
        CAPTIONS.put("INTERP_LEFT",     "Probe value smaller than target. Shifting search right.");
        CAPTIONS.put("INTERP_RIGHT",    "Probe value larger than target. Shifting search left.");
        CAPTIONS.put("EXP_BOUND",       "Doubling bound to %d — expanding range to find the target.");
        CAPTIONS.put("EXP_BINARY",      "Range located: [%d, %d]. Switching to binary search within it.");
        CAPTIONS.put("FIB_PROBE",       "Fibonacci probe at index %d (fibM2 = %d). Comparing value.");
        CAPTIONS.put("FIB_LEFT",        "Value > target. Reducing Fibonacci numbers — search left.");
        CAPTIONS.put("FIB_RIGHT",       "Value < target. Advancing offset right — search right.");
        CAPTIONS.put("GRAPH_VISIT",     "Visiting node '%s' — is this the target '%s'?");
        CAPTIONS.put("GRAPH_ENQUEUE",   "Enqueueing neighbours of '%s': %s");
        CAPTIONS.put("GRAPH_PUSH",      "Pushing neighbours of '%s' onto stack: %s");
        CAPTIONS.put("GRAPH_PATH",      "Target found! Path traced back to start: %s");
        CAPTIONS.put("KMP_MATCH",       "text[%d]='%s' matches pattern[%d]='%s' — both pointers advance.");
        CAPTIONS.put("KMP_MISMATCH",    "Mismatch at text[%d]. LPS table skips pattern back to position %d.");
        CAPTIONS.put("KMP_FOUND",       "Full pattern match found at text position %d!");
        CAPTIONS.put("RK_WINDOW",       "Rolling hash window at position %d — computed in O(1).");
        CAPTIONS.put("RK_HASHMATCH",    "Hash match at position %d — verifying character by character.");
        CAPTIONS.put("RK_FOUND",        "Confirmed match at position %d!");
    }

    static final Map<String, String> CODE_MAP = new HashMap<>();
    static {
        CODE_MAP.put("Linear Search",
            "public int linearSearch(Object[] arr, Object target) {\n" +
            "    for (int i = 0; i < arr.length; i++) {\n" +
            "        if (compare(arr[i], target) == 0) return i;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Binary Search",
            "public int binarySearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        int mid = (low + high) / 2;\n" +
            "        int c = compare(arr[mid], target);\n" +
            "        if (c == 0)      return mid;\n" +
            "        else if (c < 0)  low  = mid + 1;\n" +
            "        else             high = mid - 1;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Ternary Search",
            "public int ternarySearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        int t = (high - low) / 3;\n" +
            "        int mid1 = low + t, mid2 = high - t;\n" +
            "        if (compare(arr[mid1], target) == 0) return mid1;\n" +
            "        if (compare(arr[mid2], target) == 0) return mid2;\n" +
            "        if (compare(target, arr[mid1]) < 0)       high = mid1 - 1;\n" +
            "        else if (compare(target, arr[mid2]) > 0)  low  = mid2 + 1;\n" +
            "        else { low = mid1 + 1; high = mid2 - 1; }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Jump Search",
            "public int jumpSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length;\n" +
            "    int step = (int) Math.floor(Math.sqrt(n));\n" +
            "    int prev = 0, curr = step;\n" +
            "    while (curr < n && compare(arr[curr], target) <= 0) {\n" +
            "        prev = curr; curr += step;\n" +
            "    }\n" +
            "    for (int i = prev; i < Math.min(curr, n); i++) {\n" +
            "        if (compare(arr[i], target) == 0) return i;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Interpolation Search",
            "public int interpolationSearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        double lo = toDouble(arr[low]), hi = toDouble(arr[high]);\n" +
            "        double tv = toDouble(target);\n" +
            "        int pos = (hi == lo) ? low\n" +
            "            : low + (int)(((tv - lo) / (hi - lo)) * (high - low));\n" +
            "        if (pos < low || pos > high) break;\n" +
            "        int c = compare(arr[pos], target);\n" +
            "        if (c == 0)      return pos;\n" +
            "        else if (c < 0)  low  = pos + 1;\n" +
            "        else             high = pos - 1;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Exponential Search",
            "public int exponentialSearch(Object[] arr, Object target) {\n" +
            "    if (compare(arr[0], target) == 0) return 0;\n" +
            "    int bound = 1;\n" +
            "    while (bound < arr.length && compare(arr[bound], target) <= 0)\n" +
            "        bound *= 2;\n" +
            "    return binarySearch(arr, target, bound/2, Math.min(bound, arr.length-1));\n" +
            "}");
        CODE_MAP.put("Fibonacci Search",
            "public int fibonacciSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length, fm2 = 0, fm1 = 1, fib = 1;\n" +
            "    while (fib < n) { fm2 = fm1; fm1 = fib; fib = fm1 + fm2; }\n" +
            "    int offset = -1;\n" +
            "    while (fib > 1) {\n" +
            "        int i = Math.min(offset + fm2, n - 1);\n" +
            "        int c = compare(arr[i], target);\n" +
            "        if (c < 0)      { fib=fm1; fm1=fm2; fm2=fib-fm1; offset=i; }\n" +
            "        else if (c > 0) { fib=fm2; fm1-=fm2; fm2=fib-fm1; }\n" +
            "        else            return i;\n" +
            "    }\n" +
            "    if (fm1==1 && compare(arr[offset+1], target)==0) return offset+1;\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Breadth-First Search",
            "public int bfs(Map<String,List<String>> graph, String start, String target) {\n" +
            "    Queue<String> queue = new LinkedList<>();\n" +
            "    Map<String,String> parent = new HashMap<>();\n" +
            "    Set<String> visited = new HashSet<>();\n" +
            "    queue.add(start); parent.put(start, null);\n" +
            "    while (!queue.isEmpty()) {\n" +
            "        String node = queue.poll();\n" +
            "        if (visited.contains(node)) continue;\n" +
            "        visited.add(node);\n" +
            "        if (node.equals(target)) return buildPath(parent, node);\n" +
            "        for (String nb : graph.getOrDefault(node, List.of()))\n" +
            "            if (!visited.contains(nb)) { parent.put(nb, node); queue.add(nb); }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Depth-First Search",
            "public int dfs(Map<String,List<String>> graph, String start, String target) {\n" +
            "    Deque<String> stack = new ArrayDeque<>();\n" +
            "    Map<String,String> parent = new HashMap<>();\n" +
            "    Set<String> visited = new HashSet<>();\n" +
            "    stack.push(start); parent.put(start, null);\n" +
            "    while (!stack.isEmpty()) {\n" +
            "        String node = stack.pop();\n" +
            "        if (visited.contains(node)) continue;\n" +
            "        visited.add(node);\n" +
            "        if (node.equals(target)) return buildPath(parent, node);\n" +
            "        List<String> nb = graph.getOrDefault(node, List.of());\n" +
            "        for (int i = nb.size()-1; i >= 0; i--)\n" +
            "            if (!visited.contains(nb.get(i))) {\n" +
            "                parent.putIfAbsent(nb.get(i), node); stack.push(nb.get(i));\n" +
            "            }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("KMP Search",
            "public List<Integer> kmpSearch(String text, String pattern) {\n" +
            "    int[] lps = buildLPS(pattern);\n" +
            "    List<Integer> results = new ArrayList<>();\n" +
            "    int i = 0, j = 0;\n" +
            "    while (i < text.length()) {\n" +
            "        if (text.charAt(i) == pattern.charAt(j)) {\n" +
            "            i++; j++;\n" +
            "            if (j == pattern.length()) {\n" +
            "                results.add(i - j);\n" +
            "                j = lps[j - 1];\n" +
            "            }\n" +
            "        } else {\n" +
            "            j = (j > 0) ? lps[j - 1] : 0;\n" +
            "            if (j == 0) i++;\n" +
            "        }\n" +
            "    }\n" +
            "    return results;\n" +
            "}");
        CODE_MAP.put("Rabin-Karp Search",
            "public List<Integer> rabinKarp(String text, String pattern) {\n" +
            "    final long BASE = 257L, MOD = 1_000_000_007L;\n" +
            "    int n = text.length(), m = pattern.length();\n" +
            "    long ph = 0, wh = 0, pw = 1;\n" +
            "    for (int i = 0; i < m; i++) {\n" +
            "        ph = (ph * BASE + pattern.charAt(i)) % MOD;\n" +
            "        wh = (wh * BASE + text.charAt(i)) % MOD;\n" +
            "        if (i > 0) pw = (pw * BASE) % MOD;\n" +
            "    }\n" +
            "    List<Integer> results = new ArrayList<>();\n" +
            "    for (int i = 0; i <= n - m; i++) {\n" +
            "        if (i > 0) {\n" +
            "            wh = (wh - text.charAt(i-1) * pw % MOD + MOD) % MOD;\n" +
            "            wh = (wh * BASE + text.charAt(i + m - 1)) % MOD;\n" +
            "        }\n" +
            "        if (wh == ph && text.substring(i, i+m).equals(pattern)) results.add(i);\n" +
            "    }\n" +
            "    return results;\n" +
            "}");
    }

    static final String[][] APP_DATA = {
        {"Linear Search",        "O(n)",         "O(1)", "Small or unsorted datasets",
         "Finding a contact in a short list, scanning log files, checking unsorted inventory",
         "Only option when data is unsorted and no index exists."},
        {"Binary Search",        "O(log n)",      "O(1)", "Large sorted arrays with random access",
         "Dictionary lookups, sorted database index, std::lower_bound in C++",
         "Halves search space each step — extremely fast on large sorted arrays."},
        {"Ternary Search",       "O(log3 n)",     "O(1)", "Finding peaks in unimodal functions",
         "Game AI optimisation, finding optimal price points in economics models",
         "More comparisons per step than binary — rarely preferred for discrete arrays."},
        {"Jump Search",          "O(sqrt n)",     "O(1)", "Sorted data where backward traversal is costly",
         "Searching magnetic tape storage, reading sorted blocks from disk sequentially",
         "Designed for systems where backward seek is expensive."},
        {"Interpolation Search", "O(log log n)",  "O(1)", "Uniformly distributed sorted data",
         "Phone book lookups, postal code search, sorted numeric datasets with even spread",
         "Outperforms binary on uniform distributions. Degrades to O(n) on skewed data."},
        {"Exponential Search",   "O(log n)",      "O(1)", "Unbounded or infinite sorted arrays",
         "Unbounded sorted streams, database cursors without known end, sparse sorted files",
         "Finds the range first, then binary searches within it."},
        {"Fibonacci Search",     "O(log n)",      "O(1)", "Systems where division is expensive",
         "Older CPU architectures, embedded systems without FPU",
         "Uses only addition and subtraction — avoids costly division operations."},
        {"Breadth-First Search", "O(V + E)",      "O(V)", "Shortest path in unweighted graphs",
         "Social network suggestions, GPS shortest route, web crawlers, peer-to-peer networking",
         "Guarantees shortest path in unweighted graphs. Memory-heavy for wide graphs."},
        {"Depth-First Search",   "O(V + E)",      "O(V)", "Cycle detection, topological sort, maze solving",
         "Compiler dependency resolution, solving puzzles, detecting circular imports",
         "Memory-efficient for deep graphs. Does not guarantee shortest path."},
        {"KMP Search",           "O(n + m)",      "O(m)", "Finding a single pattern in large text",
         "Text editors find & replace, antivirus signature scanning, DNA subsequence search",
         "LPS table preprocessing means no character is ever re-compared."},
        {"Rabin-Karp Search",    "O(n + m) avg",  "O(1)", "Multi-pattern search and plagiarism detection",
         "Plagiarism detection, searching multiple virus signatures, substring hashing in databases",
         "Rolling hash makes multi-pattern search efficient. Requires collision verification."},
    };
}