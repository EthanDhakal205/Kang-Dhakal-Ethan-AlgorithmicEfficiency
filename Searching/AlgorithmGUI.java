import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AlgorithmGUI extends JFrame {

    // ── palette ───────────────────────────────────────────────
    private static final Color BG         = new Color(10,  11,  14);
    private static final Color PANEL      = new Color(17,  19,  24);
    private static final Color CARD       = new Color(24,  27,  34);
    private static final Color BORDER     = new Color(38,  42,  54);
    private static final Color ACCENT     = new Color(82, 130, 255);
    private static final Color ACCENT2    = new Color(52, 211, 153);
    private static final Color WARN       = new Color(251, 191,  36);
    private static final Color DANGER     = new Color(239,  68,  68);
    private static final Color TEXT       = new Color(220, 225, 235);
    private static final Color TEXT_DIM   = new Color(100, 110, 130);
    private static final Color TEXT_HINT  = new Color(55,  62,  78);
    private static final Color CELL_DEF   = new Color(30,  34,  44);
    private static final Color CELL_SCAN  = new Color(22,  35,  55);
    private static final Color CELL_CHECK = new Color(65,  48,  12);
    private static final Color CELL_FOUND = new Color(12,  54,  36);

    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font MONO_B = new Font("JetBrains Mono", Font.BOLD,  13);
    private static final Font SANS   = new Font("Segoe UI",       Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font TITLE  = new Font("Segoe UI",       Font.BOLD,  22);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN, 11);
    private static final Font CAP_F  = new Font("Segoe UI",       Font.ITALIC, 13);

    // ═══════════════════════════════════════════════════════════
    //  ANIMATION ENGINE  (javax.swing.Timer at 60 fps)
    // ═══════════════════════════════════════════════════════════
    // Per-cell: 0=idle, 1=checking, 2=found, 3=scanned
    // cellAnim[i] = current visual blend progress (0..1)
    // cellAnimTarget[i] = where each cell is heading
    private float[]  cellAnim;
    private float[]  cellAnimPrev;   // previous state colour blend
    private int[]    cellAnimState;  // target state for colour

    // Caption slide-up / fade
    // phase: 0=idle, 1=slide-in, 2=hold, 3=fade-out
    private int     captionPhase    = 0;
    private float   captionProgress = 0f;  // 0..1 within current phase
    private String  captionText     = "";
    private String  captionQueued   = null;
    private int     captionHoldTicks = 0;
    private int     captionHoldMax   = 0;

    // Graph node pulse for BFS/DFS
    private Map<String, Float> nodeAnim = new HashMap<>();

    // master 60fps Swing timer
    private javax.swing.Timer animLoop;

    // ── algo state ────────────────────────────────────────────
    private String   selectedAlgo  = "Linear Search";
    private int[]    cellStates;
    private int      comparisons   = 0;
    private long     elapsedNs     = 0;
    private int      resultIndex   = -2;
    private volatile boolean running = false;
    private int      stepDelay     = 350;
    private List<Integer> matchPositions = new ArrayList<>();
    private List<String>  visitOrder     = new ArrayList<>();
    private boolean  showCaptions  = true;

    // ── data ──────────────────────────────────────────────────
    private Object[] currentArray  = {2, 5, 8, 11, 14, 19, 27, 33, 45};
    private Object   currentTarget = 19;
    private boolean  ignoreCase    = false;
    private String   currentText    = "the cat sat on the caterpillar";
    private String   currentPattern = "cat";
    private Map<String, List<String>> currentGraph;
    private String   graphStart  = "A";
    private String   graphTarget = "C";
    private int      graphDepth  = 3;
    private int      graphBranch = 2;

    // ── swing refs ────────────────────────────────────────────
    private VisualizerPanel vizPanel;
    private JScrollPane     vizScroll;
    private JLabel  statusLabel, cmpLabel, timeLabel, resultLabel, algoLabel;
    private JSlider speedSlider;
    private JButton runBtn, resetBtn, codeBtn, appsBtn;
    private JPanel  inputPanel;
    private CardLayout inputCards;
    private JCheckBox captionToggle;

    private static final String[] ARRAY_ALGOS  = {
        "Linear Search","Binary Search","Ternary Search",
        "Jump Search","Interpolation Search","Exponential Search","Fibonacci Search"
    };
    private static final String[] GRAPH_ALGOS  = {"Breadth-First Search","Depth-First Search"};
    private static final String[] STRING_ALGOS = {"KMP Search","Rabin-Karp Search"};

    // ═══════════════════════════════════════════════════════════
    //  CAPTIONS
    // ═══════════════════════════════════════════════════════════
    private static final Map<String, String> CAPTIONS = new HashMap<>();
    static {
        CAPTIONS.put("LINEAR_CHECK",    "Checking arr[%d] = %s — is it equal to target %s?");
        CAPTIONS.put("LINEAR_MISS",     "arr[%d] = %s is not the target. Moving forward.");
        CAPTIONS.put("LINEAR_FOUND",    "Found it! arr[%d] = %s matches target. Returning index %d.");
        CAPTIONS.put("LINEAR_NOTFOUND", "Reached end of array. Target not present. Returning -1.");
        CAPTIONS.put("BINARY_MID",      "Mid is index %d (value %s). Comparing to target %s.");
        CAPTIONS.put("BINARY_LEFT",     "Target < mid value. Discarding right half, searching left.");
        CAPTIONS.put("BINARY_RIGHT",    "Target > mid value. Discarding left half, searching right.");
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
        CAPTIONS.put("RK_WINDOW",       "Rolling hash window at position %d — hash computed in O(1).");
        CAPTIONS.put("RK_MATCH",        "Hash collision at position %d — verifying character by character.");
        CAPTIONS.put("RK_FOUND",        "Confirmed match at position %d!");
    }

    // ═══════════════════════════════════════════════════════════
    //  CODE SNIPPETS
    // ═══════════════════════════════════════════════════════════
    private static final Map<String, String> CODE_MAP = new HashMap<>();
    static {
        CODE_MAP.put("Linear Search",
            "public int linearSearch(Object[] arr, Object target) {\n" +
            "    for (int i = 0; i < arr.length; i++) {\n" +
            "        comparisons++;\n" +
            "        if (compare(arr[i], target) == 0) return i;\n" +
            "    }\n" +
            "    return -1;  // not found\n" +
            "}");
        CODE_MAP.put("Binary Search",
            "public int binarySearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        int mid = (low + high) / 2;\n" +
            "        comparisons++;\n" +
            "        int c = compare(arr[mid], target);\n" +
            "        if (c == 0)      return mid;\n" +
            "        else if (c < 0)  low  = mid + 1;  // search right half\n" +
            "        else             high = mid - 1;  // search left half\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Ternary Search",
            "public int ternarySearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        int third = (high - low) / 3;\n" +
            "        int mid1 = low + third;\n" +
            "        int mid2 = high - third;\n" +
            "        comparisons += 2;\n" +
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
            "    // jump forward in fixed steps\n" +
            "    while (curr < n && compare(arr[curr], target) <= 0) {\n" +
            "        prev = curr;\n" +
            "        curr += step;\n" +
            "        comparisons++;\n" +
            "    }\n" +
            "    // linear scan in identified block\n" +
            "    for (int i = prev; i < Math.min(curr, n); i++) {\n" +
            "        comparisons++;\n" +
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
            "        // estimate position based on value spread\n" +
            "        int pos = (hi == lo) ? low\n" +
            "            : low + (int)(((tv - lo) / (hi - lo)) * (high - low));\n" +
            "        if (pos < low || pos > high) break;\n" +
            "        comparisons++;\n" +
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
            "    // double bound until we overshoot\n" +
            "    while (bound < arr.length && compare(arr[bound], target) <= 0) {\n" +
            "        bound *= 2;\n" +
            "        comparisons++;\n" +
            "    }\n" +
            "    // binary search in narrowed range\n" +
            "    int low = bound / 2;\n" +
            "    int high = Math.min(bound, arr.length - 1);\n" +
            "    return binarySearch(arr, target, low, high);\n" +
            "}");
        CODE_MAP.put("Fibonacci Search",
            "public int fibonacciSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length;\n" +
            "    int fm2 = 0, fm1 = 1, fib = 1;\n" +
            "    while (fib < n) { fm2 = fm1; fm1 = fib; fib = fm1 + fm2; }\n" +
            "    int offset = -1;\n" +
            "    while (fib > 1) {\n" +
            "        int i = Math.min(offset + fm2, n - 1);\n" +
            "        comparisons++;\n" +
            "        int c = compare(arr[i], target);\n" +
            "        if (c < 0) {\n" +
            "            // shift right — use next fibonacci\n" +
            "            fib = fm1; fm1 = fm2; fm2 = fib - fm1; offset = i;\n" +
            "        } else if (c > 0) {\n" +
            "            // shift left — use smaller fibonacci\n" +
            "            fib = fm2; fm1 -= fm2; fm2 = fib - fm1;\n" +
            "        } else return i;\n" +
            "    }\n" +
            "    if (fm1 == 1 && compare(arr[offset + 1], target) == 0) return offset + 1;\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Breadth-First Search",
            "public int bfs(Map<String,List<String>> graph, String start, String target) {\n" +
            "    Queue<String> queue = new LinkedList<>();\n" +
            "    Map<String,String> parent = new HashMap<>();\n" +
            "    Set<String> visited = new HashSet<>();\n" +
            "    queue.add(start);\n" +
            "    parent.put(start, null);\n" +
            "    while (!queue.isEmpty()) {\n" +
            "        String node = queue.poll();   // FIFO — level by level\n" +
            "        if (visited.contains(node)) continue;\n" +
            "        visited.add(node);\n" +
            "        comparisons++;\n" +
            "        if (node.equals(target)) return buildPath(parent, node);\n" +
            "        for (String nb : graph.getOrDefault(node, List.of())) {\n" +
            "            if (!visited.contains(nb)) {\n" +
            "                parent.put(nb, node);\n" +
            "                queue.add(nb);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Depth-First Search",
            "public int dfs(Map<String,List<String>> graph, String start, String target) {\n" +
            "    Deque<String> stack = new ArrayDeque<>();\n" +
            "    Map<String,String> parent = new HashMap<>();\n" +
            "    Set<String> visited = new HashSet<>();\n" +
            "    stack.push(start);\n" +
            "    parent.put(start, null);\n" +
            "    while (!stack.isEmpty()) {\n" +
            "        String node = stack.pop();    // LIFO — depth first\n" +
            "        if (visited.contains(node)) continue;\n" +
            "        visited.add(node);\n" +
            "        comparisons++;\n" +
            "        if (node.equals(target)) return buildPath(parent, node);\n" +
            "        List<String> nb = graph.getOrDefault(node, List.of());\n" +
            "        for (int i = nb.size() - 1; i >= 0; i--) {\n" +
            "            if (!visited.contains(nb.get(i))) {\n" +
            "                if (!parent.containsKey(nb.get(i))) parent.put(nb.get(i), node);\n" +
            "                stack.push(nb.get(i));\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("KMP Search",
            "public List<Integer> kmpSearch(String text, String pattern) {\n" +
            "    int[] lps = buildLPS(pattern);     // failure function, O(m)\n" +
            "    List<Integer> results = new ArrayList<>();\n" +
            "    int i = 0, j = 0;\n" +
            "    while (i < text.length()) {\n" +
            "        comparisons++;\n" +
            "        if (text.charAt(i) == pattern.charAt(j)) {\n" +
            "            i++; j++;\n" +
            "            if (j == pattern.length()) {\n" +
            "                results.add(i - j);    // match found\n" +
            "                j = lps[j - 1];        // avoid re-checking matched chars\n" +
            "            }\n" +
            "        } else {\n" +
            "            j = (j > 0) ? lps[j - 1] : 0;\n" +
            "            if (j == 0) i++;           // no prefix to reuse\n" +
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
            "        wh = (wh * BASE + text.charAt(i))    % MOD;\n" +
            "        if (i > 0) pw = (pw * BASE) % MOD;\n" +
            "    }\n" +
            "    List<Integer> results = new ArrayList<>();\n" +
            "    for (int i = 0; i <= n - m; i++) {\n" +
            "        if (i > 0) {\n" +
            "            // rolling hash: remove left char, add right char — O(1)\n" +
            "            wh = (wh - text.charAt(i-1) * pw % MOD + MOD) % MOD;\n" +
            "            wh = (wh * BASE + text.charAt(i + m - 1)) % MOD;\n" +
            "        }\n" +
            "        comparisons++;\n" +
            "        if (wh == ph && text.substring(i, i+m).equals(pattern))\n" +
            "            results.add(i);\n" +
            "    }\n" +
            "    return results;\n" +
            "}");
    }

    // ═══════════════════════════════════════════════════════════
    //  APPLICATIONS DATA
    // ═══════════════════════════════════════════════════════════
    private static final String[][] APP_DATA = {
        // { name, time, space, best_for, real_world, note }
        {"Linear Search",
            "O(n)", "O(1)",
            "Small or unsorted datasets",
            "Finding a contact in a short list, scanning a log file for an error, checking unsorted inventory",
            "Only option when data is unsorted and no index exists. Simple and reliable."},
        {"Binary Search",
            "O(log n)", "O(1)",
            "Large sorted arrays with random access",
            "Dictionary lookups, autocomplete trie traversal, finding a record in a sorted database index, std::lower_bound in C++",
            "Halves search space each step — extremely fast on large sorted arrays."},
        {"Ternary Search",
            "O(log\u2083 n)", "O(1)",
            "Finding peaks/minima in unimodal functions",
            "Game AI optimisation, finding the optimal price point in economics models, signal processing",
            "More comparisons per step than binary — rarely preferred for discrete arrays."},
        {"Jump Search",
            "O(\u221an)", "O(1)",
            "Sorted data where backward traversal is costly",
            "Searching magnetic tape storage, reading sorted blocks from disk sequentially",
            "Designed for systems where backward seek is expensive (e.g. tape drives)."},
        {"Interpolation Search",
            "O(log log n)", "O(1)",
            "Uniformly distributed sorted data",
            "Searching phone books, postal code lookups, sorted numeric datasets with even spread",
            "Outperforms binary search on uniform distributions. Degrades to O(n) on skewed data."},
        {"Exponential Search",
            "O(log n)", "O(1)",
            "Unbounded / infinite sorted arrays",
            "Searching unbounded sorted streams, database cursors without a known end, sparse sorted files",
            "Finds the range first, then binary searches within it. Handles unknown size elegantly."},
        {"Fibonacci Search",
            "O(log n)", "O(1)",
            "Systems where division is expensive",
            "Older CPU architectures, embedded systems, searching on hardware without FPU",
            "Uses only addition and subtraction — useful where division is a costly operation."},
        {"Breadth-First Search",
            "O(V + E)", "O(V)",
            "Shortest path in unweighted graphs",
            "Social network friend suggestions, GPS shortest route, web crawlers, peer-to-peer networking",
            "Guarantees shortest path in unweighted graphs. Memory-heavy for wide graphs."},
        {"Depth-First Search",
            "O(V + E)", "O(V)",
            "Cycle detection, topological sort, maze solving",
            "Compiler dependency resolution, solving puzzles (mazes, Sudoku), detecting circular imports",
            "Memory-efficient for deep graphs. Does not guarantee shortest path."},
        {"KMP Search",
            "O(n + m)", "O(m)",
            "Finding a single pattern in large text",
            "Text editors (find & replace), antivirus signature scanning, DNA subsequence search",
            "LPS table preprocessing means no character is ever re-compared. Best for repeated single-pattern search."},
        {"Rabin-Karp Search",
            "O(n + m) avg", "O(1)",
            "Multi-pattern search and plagiarism detection",
            "Plagiarism detection systems, searching for multiple virus signatures simultaneously, substring hashing in databases",
            "Rolling hash makes multi-pattern search efficient. Hash collisions require verification."},
    };

    // ═══════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════
    public AlgorithmGUI() {
        buildTreeGraph();
        setTitle("Search Algorithm Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1150, 760));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildMain(),    BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        startAnimLoop();
        setVisible(true);
        refreshInputPanel();
    }

    // ═══════════════════════════════════════════════════════════
    //  ANIMATION LOOP  — single Swing timer drives everything
    // ═══════════════════════════════════════════════════════════
    private void startAnimLoop() {
        animLoop = new javax.swing.Timer(16, e -> tickAnimations());
        animLoop.start();
    }

    private void tickAnimations() {
        boolean dirty = false;

        // ── per-cell lerp ──────────────────────────────────────
        if (cellAnim != null && cellAnimState != null) {
            for (int i = 0; i < cellAnim.length; i++) {
                float target = (cellAnimState[i] == 2 || cellAnimState[i] == 3) ? 1f : 0f;
                float diff = target - cellAnim[i];
                if (Math.abs(diff) > 0.002f) {
                    cellAnim[i] += diff * 0.18f;   // smooth exponential ease
                    dirty = true;
                } else if (cellAnim[i] != target) {
                    cellAnim[i] = target;
                    dirty = true;
                }
            }
        }

        // ── graph node pulse ───────────────────────────────────
        if (!nodeAnim.isEmpty()) {
            List<String> keys = new ArrayList<>(nodeAnim.keySet());
            for (String k : keys) {
                float v = nodeAnim.get(k);
                if (v < 1f) { nodeAnim.put(k, Math.min(1f, v + 0.14f)); dirty = true; }
            }
        }

        // ── caption state machine ──────────────────────────────
        if (captionPhase == 1) {
            // slide in: 0 → 1
            captionProgress += 0.07f;
            if (captionProgress >= 1f) { captionProgress = 1f; captionPhase = 2; }
            dirty = true;
        } else if (captionPhase == 2) {
            // hold
            captionHoldTicks++;
            if (captionHoldTicks >= captionHoldMax) {
                captionPhase = 3;
                captionProgress = 1f;
            }
            dirty = true;
        } else if (captionPhase == 3) {
            // fade out
            captionProgress -= 0.05f;
            if (captionProgress <= 0f) {
                captionProgress = 0f;
                captionPhase = 0;
                // show queued caption if one is waiting
                if (captionQueued != null) {
                    pushCaption(captionQueued);
                    captionQueued = null;
                }
            }
            dirty = true;
        }

        if (dirty && vizPanel != null) vizPanel.repaint();
    }

    // Push a new caption — interrupts current or queues
    private void pushCaption(String text) {
        if (!showCaptions) return;
        captionText      = text;
        captionPhase     = 1;
        captionProgress  = 0f;
        captionHoldTicks = 0;
        // hold time scales with step delay so fast mode shows less
        captionHoldMax   = Math.max(6, stepDelay / 20);
    }

    private void showCaption(String text) {
        if (!showCaptions) return;
        if (captionPhase == 0 || captionPhase == 3) {
            // idle or fading out — push immediately
            captionQueued = null;
            SwingUtilities.invokeLater(() -> pushCaption(text));
        } else {
            // currently showing — queue for after fade-out
            captionQueued = text;
            // accelerate the current fade-out
            SwingUtilities.invokeLater(() -> { if (captionPhase == 2) { captionPhase = 3; captionProgress = 1f; } });
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(PANEL);
        side.setPreferredSize(new Dimension(232, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JLabel title = new JLabel("  searchviz");
        title.setFont(TITLE); title.setForeground(ACCENT);
        title.setBorder(new EmptyBorder(24, 16, 20, 16));
        side.add(title, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setBackground(PANEL);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setBorder(new EmptyBorder(0, 0, 16, 0));
        addCategory(list, "ARRAY BASED",   ARRAY_ALGOS);
        addCategory(list, "GRAPH / TREE",  GRAPH_ALGOS);
        addCategory(list, "STRING / TEXT", STRING_ALGOS);

        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(PANEL);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        side.add(scroll, BorderLayout.CENTER);
        return side;
    }

    private void addCategory(JPanel parent, String label, String[] algos) {
        JLabel cat = new JLabel(label);
        cat.setFont(SMALL); cat.setForeground(TEXT_HINT);
        cat.setBorder(new EmptyBorder(14, 18, 6, 0));
        cat.setAlignmentX(LEFT_ALIGNMENT);
        parent.add(cat);
        for (String algo : algos) {
            AlgoButton btn = new AlgoButton(algo);
            btn.setAlignmentX(LEFT_ALIGNMENT);
            btn.addActionListener(e -> {
                selectedAlgo = algo;
                algoLabel.setText(algo);
                refreshInputPanel();
            });
            parent.add(btn);
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════
    private JPanel buildMain() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.add(buildTopBar(),    BorderLayout.NORTH);
        main.add(buildCenter(),    BorderLayout.CENTER);
        main.add(buildBottomBar(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 24, 12, 24)));

        algoLabel = new JLabel(selectedAlgo);
        algoLabel.setFont(SANS_B); algoLabel.setForeground(TEXT);
        bar.add(algoLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        captionToggle = new JCheckBox("captions");
        captionToggle.setSelected(true);
        captionToggle.setFont(SMALL); captionToggle.setForeground(TEXT_DIM);
        captionToggle.setOpaque(false); captionToggle.setFocusPainted(false);
        captionToggle.addActionListener(e -> showCaptions = captionToggle.isSelected());

        codeBtn = actionButton("code", false);
        codeBtn.addActionListener(e -> showCodeDialog());

        appsBtn = actionButton("applications", false);
        appsBtn.addActionListener(e -> showApplicationsDialog());

        JLabel sl = new JLabel("speed");
        sl.setFont(SMALL); sl.setForeground(TEXT_DIM);
        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(PANEL);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] d = {700, 450, 280, 130, 50};
            stepDelay = d[speedSlider.getValue() - 1];
            captionHoldMax = Math.max(6, stepDelay / 20);
        });

        right.add(captionToggle); right.add(codeBtn); right.add(appsBtn);
        right.add(sl); right.add(speedSlider);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 14));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(18, 24, 0, 24));

        inputCards = new CardLayout();
        inputPanel = new JPanel(inputCards);
        inputPanel.setBackground(BG);
        inputPanel.add(buildArrayInput(),  "array");
        inputPanel.add(buildGraphInput(),  "graph");
        inputPanel.add(buildStringInput(), "string");
        center.add(inputPanel, BorderLayout.NORTH);

        vizPanel = new VisualizerPanel();
        vizScroll = new JScrollPane(vizPanel);
        vizScroll.setBorder(null);
        vizScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        vizScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        vizScroll.getViewport().setBackground(BG);
        vizScroll.getVerticalScrollBar().setUnitIncrement(20);
        center.add(vizScroll, BorderLayout.CENTER);
        return center;
    }

    // ── INPUT PANELS ──────────────────────────────────────────
    private JPanel buildArrayInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField arrField = styledField(22); arrField.setText("2, 5, 8, 11, 14, 19, 27, 33, 45");
        JTextField tgtField = styledField(6);  tgtField.setText("19");
        JCheckBox icBox = styledCheckbox("ignore case");

        Runnable apply = () -> parseArrayInput(arrField.getText(), tgtField.getText(), icBox.isSelected());
        addLiveListener(arrField, apply);
        addLiveListener(tgtField, apply);
        icBox.addActionListener(e -> apply.run());

        p.add(dimLabel("array:")); p.add(arrField);
        p.add(dimLabel("target:")); p.add(tgtField);
        p.add(icBox);
        return p;
    }

    private JPanel buildGraphInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JTextField sField = styledField(3); sField.setText("A");
        JTextField tField = styledField(3); tField.setText("C");
        SpinnerNumberModel depthModel  = new SpinnerNumberModel(3, 1, 6, 1);
        SpinnerNumberModel branchModel = new SpinnerNumberModel(2, 2, 4, 1);
        JSpinner depthSpin  = styledSpinner(depthModel);
        JSpinner branchSpin = styledSpinner(branchModel);

        Runnable applyGraph = () -> {
            graphStart  = sField.getText().trim().isEmpty()  ? "A" : sField.getText().trim().toUpperCase();
            graphTarget = tField.getText().trim().isEmpty()  ? "C" : tField.getText().trim().toUpperCase();
            graphDepth  = (Integer) depthSpin.getValue();
            graphBranch = (Integer) branchSpin.getValue();
            buildTreeGraph();
            resetVisuals();
        };
        addLiveListener(sField, applyGraph);
        addLiveListener(tField, applyGraph);
        depthSpin.addChangeListener(e -> applyGraph.run());
        branchSpin.addChangeListener(e -> applyGraph.run());

        p.add(dimLabel("start:")); p.add(sField);
        p.add(dimLabel("target:")); p.add(tField);
        p.add(dimLabel("depth:")); p.add(depthSpin);
        p.add(dimLabel("branches:")); p.add(branchSpin);
        return p;
    }

    private JPanel buildStringInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        JTextField txtField = styledField(28); txtField.setText("the cat sat on the caterpillar");
        JTextField patField = styledField(10); patField.setText("cat");
        JCheckBox icBox = styledCheckbox("ignore case");

        Runnable apply = () -> {
            currentText    = txtField.getText();
            currentPattern = patField.getText();
            ignoreCase     = icBox.isSelected();
            resetVisuals();
        };
        addLiveListener(txtField, apply);
        addLiveListener(patField, apply);
        icBox.addActionListener(e -> apply.run());

        p.add(dimLabel("text:")); p.add(txtField);
        p.add(dimLabel("pattern:")); p.add(patField);
        p.add(icBox);
        return p;
    }

    private void addLiveListener(JTextField field, Runnable onChange) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(onChange); }
            public void removeUpdate(DocumentEvent e)  { SwingUtilities.invokeLater(onChange); }
            public void changedUpdate(DocumentEvent e) { SwingUtilities.invokeLater(onChange); }
        });
    }

    private JPanel buildBottomBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(PANEL);
        bar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
            new EmptyBorder(12, 24, 12, 24)));

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        stats.setOpaque(false);
        cmpLabel    = monoLabel("comparisons: —");
        timeLabel   = monoLabel("time: —");
        resultLabel = monoLabel("result: —");
        statusLabel = monoLabel("configure and press run");
        stats.add(cmpLabel); stats.add(timeLabel);
        stats.add(resultLabel); stats.add(statusLabel);
        bar.add(stats, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.setOpaque(false);
        resetBtn = actionButton("reset", false);
        runBtn   = actionButton("run",   true);
        resetBtn.addActionListener(e -> resetVisuals());
        runBtn.addActionListener(e -> { if (!running) runAlgorithm(); });
        btns.add(resetBtn); btns.add(runBtn);
        bar.add(btns, BorderLayout.EAST);
        return bar;
    }

    // ═══════════════════════════════════════════════════════════
    //  CODE DIALOG
    // ═══════════════════════════════════════════════════════════
    private void showCodeDialog() {
        JDialog dlg = new JDialog(this, selectedAlgo + " — source code", false);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JTextArea area = new JTextArea(CODE_MAP.getOrDefault(selectedAlgo, "// not available"));
        area.setFont(MONO);
        area.setBackground(new Color(13, 15, 20));
        area.setForeground(new Color(190, 205, 225));
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setSelectionColor(new Color(50, 80, 140));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(new Color(13, 15, 20));
        scroll.setPreferredSize(new Dimension(660, 400));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)));
        JLabel hTitle = new JLabel(selectedAlgo);
        hTitle.setFont(SANS_B); hTitle.setForeground(TEXT);
        JLabel comp = new JLabel("time: " + getTimeComplexity(selectedAlgo) + "   space: " + getSpaceComplexity(selectedAlgo));
        comp.setFont(SMALL); comp.setForeground(TEXT_DIM);
        header.add(hTitle, BorderLayout.WEST);
        header.add(comp, BorderLayout.EAST);

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════
    //  APPLICATIONS DIALOG
    // ═══════════════════════════════════════════════════════════
    private void showApplicationsDialog() {
        JDialog dlg = new JDialog(this, "Search algorithms — real-world applications", false);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());
        dlg.setPreferredSize(new Dimension(860, 680));

        // header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(PANEL);
        hdr.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(14, 24, 14, 24)));
        JLabel hTitle = new JLabel("When to use each algorithm");
        hTitle.setFont(SANS_B); hTitle.setForeground(TEXT);
        JLabel hSub = new JLabel("time complexity  |  real-world applications  |  key insight");
        hSub.setFont(SMALL); hSub.setForeground(TEXT_DIM);
        hdr.add(hTitle, BorderLayout.WEST);
        hdr.add(hSub, BorderLayout.EAST);
        dlg.add(hdr, BorderLayout.NORTH);

        // card grid
        JPanel grid = new JPanel();
        grid.setBackground(BG);
        grid.setLayout(new GridLayout(0, 2, 12, 12));
        grid.setBorder(new EmptyBorder(16, 20, 16, 20));

        for (String[] row : APP_DATA) {
            grid.add(buildAppCard(row));
        }
        // pad to even count
        if (APP_DATA.length % 2 != 0) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            grid.add(empty);
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        dlg.add(scroll, BorderLayout.CENTER);

        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel buildAppCard(String[] row) {
        // row: { name, time, space, best_for, real_world, note }
        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setLayout(new BorderLayout(0, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1),
            new EmptyBorder(14, 16, 14, 16)));

        // top row: name + badges
        JPanel top = new JPanel(new BorderLayout(8, 0));
        top.setOpaque(false);
        JLabel name = new JLabel(row[0]);
        name.setFont(SANS_B); name.setForeground(TEXT);
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(badge("T: " + row[1], ACCENT, new Color(14, 22, 44)));
        badges.add(badge("S: " + row[2], ACCENT2, new Color(10, 30, 22)));
        top.add(name, BorderLayout.WEST);
        top.add(badges, BorderLayout.EAST);
        card.add(top, BorderLayout.NORTH);

        // content
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel bestFor = wrappedLabel("Best for: " + row[3], SANS_B, SMALL, TEXT, 380);
        JLabel realWorld = wrappedLabel(row[4], null, SMALL, TEXT_DIM, 380);
        JLabel note = wrappedLabel(row[5], null, CAP_F, new Color(90, 100, 120), 380);

        body.add(bestFor);
        body.add(Box.createVerticalStrut(5));
        body.add(realWorld);
        body.add(Box.createVerticalStrut(5));
        body.add(note);
        card.add(body, BorderLayout.CENTER);

        return card;
    }

    private JLabel badge(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                super.paintComponent(g);
            }
        };
        l.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        l.setForeground(fg);
        l.setOpaque(false);
        l.setBorder(new EmptyBorder(2, 7, 2, 7));
        return l;
    }

    private JLabel wrappedLabel(String text, Font boldFont, Font baseFont, Color color, int width) {
        JLabel l = new JLabel("<html><body style='width:" + width + "px'>" + text + "</body></html>");
        l.setFont(boldFont != null ? boldFont : baseFont);
        l.setForeground(color);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    // ═══════════════════════════════════════════════════════════
    //  COMPLEXITY HELPERS
    // ═══════════════════════════════════════════════════════════
    private String getTimeComplexity(String algo) {
        if (algo.equals("Linear Search"))        return "O(n)";
        if (algo.equals("Binary Search"))        return "O(log n)";
        if (algo.equals("Ternary Search"))       return "O(log3 n)";
        if (algo.equals("Jump Search"))          return "O(sqrt n)";
        if (algo.equals("Interpolation Search")) return "O(log log n)";
        if (algo.equals("Exponential Search"))   return "O(log n)";
        if (algo.equals("Fibonacci Search"))     return "O(log n)";
        if (algo.equals("Breadth-First Search")) return "O(V + E)";
        if (algo.equals("Depth-First Search"))   return "O(V + E)";
        if (algo.equals("KMP Search"))           return "O(n + m)";
        if (algo.equals("Rabin-Karp Search"))    return "O(n + m)";
        return "-";
    }
    private String getSpaceComplexity(String algo) {
        if (algo.equals("Breadth-First Search") || algo.equals("Depth-First Search")) return "O(V)";
        if (algo.equals("KMP Search")) return "O(m)";
        return "O(1)";
    }

    // ═══════════════════════════════════════════════════════════
    //  GRAPH BUILDER
    // ═══════════════════════════════════════════════════════════
    private void buildTreeGraph() {
        currentGraph = new LinkedHashMap<>();
        int[] counter = {0};
        buildNode(null, graphDepth, graphBranch, counter);
        List<String> nodes = new ArrayList<>(currentGraph.keySet());
        if (!nodes.isEmpty() && !currentGraph.containsKey(graphStart))  graphStart  = nodes.get(0);
        if (nodes.size() > 1 && !currentGraph.containsKey(graphTarget)) graphTarget = nodes.get(nodes.size() - 1);
    }

    private String buildNode(String parent, int depth, int branch, int[] counter) {
        String name = nodeLabel(counter[0]++);
        currentGraph.put(name, new ArrayList<>());
        if (parent != null) currentGraph.get(parent).add(name);
        if (depth > 1) { for (int i = 0; i < branch; i++) buildNode(name, depth - 1, branch, counter); }
        return name;
    }

    private String nodeLabel(int n) {
        StringBuilder sb = new StringBuilder();
        do { sb.insert(0, (char)('A' + n % 26)); n = n / 26 - 1; } while (n >= 0);
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    //  RUN LOGIC
    // ═══════════════════════════════════════════════════════════
    private void runAlgorithm() {
        running = true;
        runBtn.setEnabled(false);
        comparisons = 0; elapsedNs = 0; resultIndex = -2;
        matchPositions.clear(); visitOrder.clear();
        nodeAnim.clear();

        new Thread(() -> {
            try {
                if      (isArrayAlgo()) runArrayAlgo();
                else if (isGraphAlgo()) runGraphAlgo();
                else                    runStringAlgo();
            } catch (InterruptedException ignored) {
            } catch (Exception ex) { ex.printStackTrace(); }
            finally {
                running = false;
                SwingUtilities.invokeLater(() -> runBtn.setEnabled(true));
            }
        }).start();
    }

    // ── cell state helpers ────────────────────────────────────
    private void setCellActive(int i) {
        if (cellStates == null || i < 0 || i >= cellStates.length) return;
        cellStates[i] = 2;
        if (cellAnimState != null && i < cellAnimState.length) cellAnimState[i] = 2;
    }
    private void setCellFound(int i) {
        if (cellStates == null || i < 0 || i >= cellStates.length) return;
        cellStates[i] = 3;
        if (cellAnimState != null && i < cellAnimState.length) cellAnimState[i] = 3;
    }
    private void setCellScanned(int i) {
        if (cellStates == null || i < 0 || i >= cellStates.length) return;
        cellStates[i] = 1;
        if (cellAnimState != null && i < cellAnimState.length) cellAnimState[i] = 1;
    }

    private void markScanned(int from, int to) {
        for (int i = Math.max(0, from); i <= Math.min(to, cellStates.length - 1); i++) {
            if (cellStates[i] != 3) setCellScanned(i);
        }
    }

    private String fmt(String key, Object... args) {
        String template = CAPTIONS.get(key);
        if (template == null) return key;
        try { return String.format(template, args); }
        catch (Exception e) { return template; }
    }

    private void runArrayAlgo() throws InterruptedException {
        int n = currentArray.length;
        cellStates    = new int[n];
        cellAnim      = new float[n];
        cellAnimPrev  = new float[n];
        cellAnimState = new int[n];
        long t0 = System.nanoTime();
        if      (selectedAlgo.equals("Linear Search"))        runLinear();
        else if (selectedAlgo.equals("Binary Search"))        runBinary();
        else if (selectedAlgo.equals("Ternary Search"))       runTernary();
        else if (selectedAlgo.equals("Jump Search"))          runJump();
        else if (selectedAlgo.equals("Interpolation Search")) runInterpolation();
        else if (selectedAlgo.equals("Exponential Search"))   runExponential();
        else if (selectedAlgo.equals("Fibonacci Search"))     runFibonacci();
        elapsedNs = System.nanoTime() - t0;
        updateStats();
    }

    private void sleep() throws InterruptedException { Thread.sleep(stepDelay); }
    private boolean matches(Object a, Object b) { return compareValues(a, b) == 0; }

    private void runLinear() throws InterruptedException {
        for (int i = 0; i < currentArray.length; i++) {
            showCaption(fmt("LINEAR_CHECK", i, currentArray[i], currentTarget));
            setCellActive(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) {
                setCellFound(i);
                showCaption(fmt("LINEAR_FOUND", i, currentArray[i], i));
                resultIndex = i; setStatus("found at index " + i); return;
            }
            showCaption(fmt("LINEAR_MISS", i, currentArray[i]));
            setCellScanned(i);
        }
        resultIndex = -1;
        showCaption(fmt("LINEAR_NOTFOUND"));
        setStatus("not found");
    }

    private void runBinary() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            showCaption(fmt("BINARY_MID", mid, currentArray[mid], currentTarget));
            setCellActive(mid); sleep(); comparisons++;
            int c = compareValues(currentArray[mid], currentTarget);
            if (c == 0) {
                setCellFound(mid); showCaption(fmt("BINARY_FOUND", mid));
                resultIndex = mid; setStatus("found at index " + mid); return;
            }
            setCellScanned(mid);
            if (c < 0) { showCaption(fmt("BINARY_RIGHT")); markScanned(low, mid - 1); low  = mid + 1; }
            else        { showCaption(fmt("BINARY_LEFT"));  markScanned(mid + 1, high); high = mid - 1; }
            sleep();
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runTernary() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int t = (high - low) / 3;
            int m1 = low + t, m2 = high - t;
            showCaption(fmt("TERNARY_MIDS", m1, currentArray[m1], m2, currentArray[m2]));
            setCellActive(m1); setCellActive(m2); sleep(); comparisons += 2;
            int c1 = compareValues(currentArray[m1], currentTarget);
            int c2 = compareValues(currentArray[m2], currentTarget);
            if (c1 == 0) { setCellFound(m1); resultIndex = m1; setStatus("found at index " + m1); return; }
            if (c2 == 0) { setCellFound(m2); resultIndex = m2; setStatus("found at index " + m2); return; }
            setCellScanned(m1); setCellScanned(m2);
            if      (compareValues(currentTarget, currentArray[m1]) < 0) { showCaption(fmt("TERNARY_LEFT"));  markScanned(m1+1, high); high = m1-1; }
            else if (compareValues(currentTarget, currentArray[m2]) > 0) { showCaption(fmt("TERNARY_RIGHT")); markScanned(low,  m2-1); low  = m2+1; }
            else { showCaption(fmt("TERNARY_MID")); markScanned(low, m1-1); markScanned(m2+1, high); low = m1+1; high = m2-1; }
            sleep();
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runJump() throws InterruptedException {
        int n = currentArray.length;
        int step = (int) Math.floor(Math.sqrt(n));
        int prev = 0, curr = step;
        while (curr < n && compareValues(currentArray[curr], currentTarget) <= 0) {
            showCaption(fmt("JUMP_JUMP", step, curr));
            setCellActive(curr); sleep(); comparisons++;
            setCellScanned(curr);
            prev = curr; curr += step;
        }
        showCaption(fmt("JUMP_LINEAR", prev));
        for (int i = prev; i < Math.min(curr, n); i++) {
            setCellActive(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) {
                setCellFound(i); resultIndex = i; setStatus("found at index " + i); return;
            }
            setCellScanned(i);
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runInterpolation() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int pos;
            if (currentArray[low] instanceof Number && currentTarget instanceof Number) {
                double lo = ((Number)currentArray[low]).doubleValue();
                double hi = ((Number)currentArray[high]).doubleValue();
                double tv = ((Number)currentTarget).doubleValue();
                pos = (hi == lo) ? low : low + (int)(((tv - lo) / (hi - lo)) * (high - low));
            } else { pos = (low + high) / 2; }
            if (pos < low || pos > high) break;
            showCaption(fmt("INTERP_POS", pos));
            setCellActive(pos); sleep(); comparisons++;
            int c = compareValues(currentArray[pos], currentTarget);
            if (c == 0) { setCellFound(pos); resultIndex = pos; setStatus("found at index " + pos); return; }
            setCellScanned(pos);
            if (c < 0) { showCaption(fmt("INTERP_LEFT")); low = pos + 1; }
            else        { showCaption(fmt("INTERP_RIGHT")); high = pos - 1; }
            sleep();
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runExponential() throws InterruptedException {
        int n = currentArray.length;
        setCellActive(0); sleep(); comparisons++;
        if (matches(currentArray[0], currentTarget)) { setCellFound(0); resultIndex = 0; setStatus("found at index 0"); return; }
        setCellScanned(0);
        int bound = 1;
        while (bound < n && compareValues(currentArray[bound], currentTarget) <= 0) {
            showCaption(fmt("EXP_BOUND", bound));
            setCellActive(bound); sleep(); comparisons++;
            setCellScanned(bound);
            bound *= 2;
        }
        int lo = bound / 2, hi = Math.min(bound, n - 1);
        showCaption(fmt("EXP_BINARY", lo, hi)); sleep();
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            setCellActive(mid); sleep(); comparisons++;
            int c = compareValues(currentArray[mid], currentTarget);
            if (c == 0) { setCellFound(mid); resultIndex = mid; setStatus("found at index " + mid); return; }
            setCellScanned(mid);
            if (c < 0) lo = mid + 1; else hi = mid - 1;
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runFibonacci() throws InterruptedException {
        int n = currentArray.length;
        int fm2 = 0, fm1 = 1, fib = 1;
        while (fib < n) { fm2 = fm1; fm1 = fib; fib = fm1 + fm2; }
        int offset = -1;
        while (fib > 1) {
            int i = Math.min(offset + fm2, n - 1);
            showCaption(fmt("FIB_PROBE", i, fm2));
            setCellActive(i); sleep(); comparisons++;
            int c = compareValues(currentArray[i], currentTarget);
            if (c < 0)      { showCaption(fmt("FIB_RIGHT")); fib=fm1; fm1=fm2; fm2=fib-fm1; offset=i; setCellScanned(i); }
            else if (c > 0) { showCaption(fmt("FIB_LEFT"));  fib=fm2; fm1-=fm2; fm2=fib-fm1; setCellScanned(i); }
            else            { setCellFound(i); resultIndex=i; setStatus("found at index "+i); return; }
            sleep();
        }
        if (fm1 == 1 && offset + 1 < n) {
            int i = offset + 1;
            setCellActive(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) { setCellFound(i); resultIndex=i; setStatus("found at index "+i); return; }
            setCellScanned(i);
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runGraphAlgo() throws InterruptedException {
        visitOrder.clear(); nodeAnim.clear();
        boolean isBFS = selectedAlgo.equals("Breadth-First Search");
        Queue<String> queue = new LinkedList<>();
        Deque<String> stack = new ArrayDeque<>();
        Set<String> visited = new LinkedHashSet<>();
        Map<String, String> parent = new LinkedHashMap<>();
        if (isBFS) queue.add(graphStart); else stack.push(graphStart);
        parent.put(graphStart, null);
        boolean found = false;

        while (isBFS ? !queue.isEmpty() : !stack.isEmpty()) {
            String node = isBFS ? queue.poll() : stack.pop();
            if (visited.contains(node)) continue;
            visited.add(node); visitOrder.add(node); comparisons++;
            nodeAnim.put(node, 0f);  // trigger pulse
            showCaption(fmt("GRAPH_VISIT", node, graphTarget));
            vizPanel.setGraphState(visited, node, Collections.emptySet(), null); sleep();

            if (node.equals(graphTarget)) {
                List<String> path = new ArrayList<>();
                String cur = node;
                while (cur != null) { path.add(0, cur); cur = parent.get(cur); }
                showCaption(fmt("GRAPH_PATH", path));
                vizPanel.setGraphState(visited, null, new HashSet<>(path), path);
                resultIndex = visitOrder.size() - 1;
                setStatus("found '" + graphTarget + "' — path: " + path);
                found = true; break;
            }

            List<String> neighbors = new ArrayList<>(currentGraph.getOrDefault(node, new ArrayList<>()));
            if (!isBFS) Collections.reverse(neighbors);
            List<String> fresh = new ArrayList<>();
            for (String nb : neighbors) {
                if (!visited.contains(nb)) {
                    if (!parent.containsKey(nb)) parent.put(nb, node);
                    if (isBFS) queue.add(nb); else stack.push(nb);
                    fresh.add(nb);
                }
            }
            if (!fresh.isEmpty()) showCaption(fmt(isBFS ? "GRAPH_ENQUEUE" : "GRAPH_PUSH", node, fresh));
        }
        elapsedNs = 0;
        if (!found) { resultIndex = -1; setStatus("'" + graphTarget + "' not reachable"); }
        updateStats();
    }

    // ── STRING ALGOS ──────────────────────────────────────────
    private void runStringAlgo() throws InterruptedException {
        matchPositions.clear();
        String t = ignoreCase ? currentText.toLowerCase()    : currentText;
        String p = ignoreCase ? currentPattern.toLowerCase() : currentPattern;
        int n = t.length(), m = p.length();
        if (p.isEmpty() || n == 0) { resultIndex=-1; setStatus("text or pattern is empty"); updateStats(); return; }
        if (m > n)                 { resultIndex=-1; setStatus("pattern longer than text");  updateStats(); return; }
        long t0 = System.nanoTime();
        if (selectedAlgo.equals("KMP Search")) runKMP(t, p, n, m);
        else                                   runRabinKarp(t, p, n, m);
        elapsedNs = System.nanoTime() - t0;
        vizPanel.setStringState(t, p, -1, -1, matchPositions);
        resultIndex = matchPositions.isEmpty() ? -1 : matchPositions.get(0);
        setStatus(matchPositions.isEmpty() ? "no matches found"
            : matchPositions.size() + " match(es) at positions " + matchPositions);
        updateStats();
    }

    private void runKMP(String t, String p, int n, int m) throws InterruptedException {
        int[] lps = buildLPS(p);
        int i = 0, j = 0;
        while (i < n) {
            comparisons++;
            vizPanel.setStringState(t, p, i, j, new ArrayList<>(matchPositions));
            if (t.charAt(i) == p.charAt(j)) {
                showCaption(fmt("KMP_MATCH", i, t.charAt(i), j, p.charAt(j)));
                sleep(); i++; j++;
                if (j == m) {
                    int ms = i - j;
                    matchPositions.add(ms);
                    showCaption(fmt("KMP_FOUND", ms));
                    j = lps[j - 1];
                }
            } else {
                int nextJ = (j > 0) ? lps[j - 1] : 0;
                showCaption(fmt("KMP_MISMATCH", i, nextJ));
                sleep();
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
        comparisons++;
        if (wh == ph && t.substring(0, m).equals(p)) { matchPositions.add(0); showCaption(fmt("RK_FOUND", 0)); }
        else showCaption(fmt("RK_WINDOW", 0));
        vizPanel.setStringState(t, p, 0, 0, new ArrayList<>(matchPositions));
        sleep();
        for (int i = 1; i <= n - m; i++) {
            wh = (wh - t.charAt(i - 1) * pw % MOD + MOD) % MOD;
            wh = (wh * BASE + t.charAt(i + m - 1)) % MOD;
            comparisons++;
            vizPanel.setStringState(t, p, i, 0, new ArrayList<>(matchPositions));
            if (wh == ph) {
                showCaption(fmt("RK_MATCH", i)); sleep();
                if (t.substring(i, i + m).equals(p)) { matchPositions.add(i); showCaption(fmt("RK_FOUND", i)); }
            } else {
                showCaption(fmt("RK_WINDOW", i)); sleep();
            }
        }
    }

    private int[] buildLPS(String p) {
        int m = p.length(); int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            if (p.charAt(i) == p.charAt(len)) { lps[i++] = ++len; }
            else if (len > 0) { len = lps[len - 1]; }
            else { lps[i++] = 0; }
        }
        return lps;
    }

    // ═══════════════════════════════════════════════════════════
    //  VISUALIZER PANEL
    // ═══════════════════════════════════════════════════════════
    class VisualizerPanel extends JPanel {
        String mode = "array";
        String textStr = "", patStr = "";
        int    textI = -1, patJ = -1;
        List<Integer> strMatches   = new ArrayList<>();
        Set<String>   graphVisited = new HashSet<>();
        String        graphCurrent = null;
        Set<String>   graphPath    = new HashSet<>();
        List<String>  graphPathList = null;

        VisualizerPanel() { setBackground(BG); }

        @Override
        public Dimension getPreferredSize() {
            if (!mode.equals("array") || currentArray == null || currentArray.length == 0) {
                return new Dimension(800, 430);
            }
            int w = (vizScroll != null) ? vizScroll.getViewport().getWidth() : 800;
            if (w <= 0) w = 800;
            int cellW = 56, gap = 6, sidePad = 24;
            int perRow = Math.max(1, (w - sidePad * 2 + gap) / (cellW + gap));
            int numRows = (currentArray.length + perRow - 1) / perRow;
            int rowH = 160;
            int totalH = Math.max(430, numRows * rowH + 80);
            return new Dimension(w, totalH);
        }

        void setStringState(String t, String p, int ti, int pj, List<Integer> matches) {
            textStr = t; patStr = p; textI = ti; patJ = pj;
            strMatches = new ArrayList<>(matches); mode = "string";
            SwingUtilities.invokeLater(this::repaint);
        }
        void setGraphState(Set<String> vis, String cur, Set<String> path, List<String> pList) {
            graphVisited = new HashSet<>(vis); graphCurrent = cur;
            graphPath = new HashSet<>(path); graphPathList = pList;
            mode = "graph"; SwingUtilities.invokeLater(this::repaint);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
            if      (mode.equals("graph"))  drawGraph(g2);
            else if (mode.equals("string")) drawString(g2);
            else                            drawArray(g2);
            if (showCaptions && captionPhase != 0) drawCaption(g2);
        }

        // ─────────────────────────────────────────────────────
        //  CAPTION — slide up + fade in / fade out
        // ─────────────────────────────────────────────────────
        private void drawCaption(Graphics2D g2) {
            if (captionText.isEmpty()) return;
            int w = getWidth();
            // Anchor caption to visible viewport, not full panel height
            Rectangle vis = getVisibleRect();
            int visH   = vis.height > 0 ? vis.height : getHeight();
            int visTop = vis.y;
            g2.setFont(CAP_F);
            FontMetrics fm = g2.getFontMetrics();
            int tw  = fm.stringWidth(captionText);
            int pad = 14;

            // alpha: ease-in on phase 1, full on phase 2, ease-out on phase 3
            float alpha;
            float slideY;  // pixels offset from resting position
            if (captionPhase == 1) {
                float ease = easeOutCubic(captionProgress);
                alpha  = ease;
                slideY = 18f * (1f - ease);  // slides up from +18px
            } else if (captionPhase == 2) {
                alpha  = 1f;
                slideY = 0f;
            } else {
                float ease = easeInCubic(captionProgress);
                alpha  = 1f - ease;
                slideY = 0f;
            }

            float clampedAlpha = Math.max(0f, Math.min(1f, alpha));
            if (clampedAlpha < 0.02f) return;

            int restingY = visTop + visH - 24;
            int cy = (int)(restingY + slideY);
            int cx = (w - tw) / 2;

            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampedAlpha);
            g2.setComposite(ac);

            // pill background
            int bgX = cx - pad;
            int bgY = cy - fm.getAscent() - 6;
            int bgW = tw + pad * 2;
            int bgH = fm.getHeight() + 12;
            g2.setColor(new Color(14, 18, 28));
            g2.fillRoundRect(bgX, bgY, bgW, bgH, 12, 12);
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(0.8f));
            g2.drawRoundRect(bgX, bgY, bgW, bgH, 12, 12);

            // text
            g2.setColor(TEXT);
            g2.drawString(captionText, cx, cy);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        private float easeOutCubic(float t) {
            float f = 1f - t;
            return 1f - f * f * f;
        }
        private float easeInCubic(float t) {
            return t * t * t;
        }

        // ─────────────────────────────────────────────────────
        //  ARRAY — multi-row animated bar chart
        // ─────────────────────────────────────────────────────
        private void drawArray(Graphics2D g2) {
            if (currentArray == null || currentArray.length == 0) return;
            int n   = currentArray.length;
            int w   = getWidth();

            // fixed cell geometry
            final int CELL_W  = 56;
            final int GAP     = 6;
            final int SIDE    = 24;   // left/right padding
            final int ROW_BAR = 100;  // bar area height per row
            final int ROW_PAD = 60;   // space below bars (label + gap)
            final int ROW_H   = ROW_BAR + ROW_PAD;
            final int TOP_PAD = 20;   // top padding

            // how many cells fit per row
            int usableW = Math.max(CELL_W + GAP, w - SIDE * 2);
            int perRow  = (usableW + GAP) / (CELL_W + GAP);
            int numRows = (n + perRow - 1) / perRow;

            // numeric range for bar scaling (global across all rows)
            double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
            boolean allNum = true;
            for (Object o : currentArray) {
                if (o instanceof Number) {
                    double v = ((Number)o).doubleValue();
                    if (v < minVal) minVal = v;
                    if (v > maxVal) maxVal = v;
                } else { allNum = false; }
            }
            double range  = (maxVal == minVal) ? 1.0 : maxVal - minVal;
            int    minBarH = 22;

            // draw row by row
            for (int row = 0; row < numRows; row++) {
                int startIdx = row * perRow;
                int endIdx   = Math.min(startIdx + perRow, n);
                int rowCount = endIdx - startIdx;

                // center this row
                int totalW = rowCount * CELL_W + (rowCount - 1) * GAP;
                int startX = (w - totalW) / 2;
                int baseY  = TOP_PAD + (row + 1) * ROW_H - ROW_PAD + 10;  // baseline for this row

                // row divider line (except first row)
                if (row > 0) {
                    g2.setColor(BORDER);
                    g2.setStroke(new BasicStroke(0.5f));
                    g2.drawLine(SIDE, TOP_PAD + row * ROW_H - 8,
                                w - SIDE, TOP_PAD + row * ROW_H - 8);
                }

                for (int idx = startIdx; idx < endIdx; idx++) {
                    int col   = idx - startIdx;
                    int x     = startX + col * (CELL_W + GAP);
                    int state = (cellStates    != null && idx < cellStates.length)    ? cellStates[idx]    : 0;
                    float anim = (cellAnim     != null && idx < cellAnim.length)      ? cellAnim[idx]      : 0f;
                    int   as   = (cellAnimState != null && idx < cellAnimState.length) ? cellAnimState[idx] : 0;

                    int barH = allNum
                        ? minBarH + (int)(((((Number)currentArray[idx]).doubleValue() - minVal) / range) * (ROW_BAR - minBarH))
                        : ROW_BAR / 2;

                    float scaleBoost = (as == 2) ? 0.08f * anim : (as == 3) ? 0.04f : 0f;
                    int animBarH = (int)(barH * (1f + scaleBoost));
                    int barY = baseY - animBarH;

                    // colour
                    Color baseColor;
                    Color borderColor;
                    if (as == 2) {
                        baseColor   = lerpColor(CELL_DEF, CELL_CHECK, anim);
                        borderColor = lerpColor(BORDER,   WARN,        anim);
                    } else if (as == 3) {
                        baseColor   = lerpColor(CELL_CHECK, CELL_FOUND, anim);
                        borderColor = lerpColor(WARN,       ACCENT2,    anim);
                    } else if (state == 1) {
                        baseColor   = CELL_SCAN;
                        borderColor = BORDER;
                    } else {
                        baseColor   = CELL_DEF;
                        borderColor = BORDER;
                    }

                    // glow rim
                    if ((as == 2 || as == 3) && anim > 0.05f) {
                        Color glow = as == 3 ? ACCENT2 : WARN;
                        Color glowC = new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), (int)(55 * anim));
                        g2.setColor(glowC);
                        g2.fillRoundRect(x - 3, barY - 3, CELL_W + 6, animBarH + 6, 10, 10);
                    }

                    g2.setColor(baseColor);
                    g2.fillRoundRect(x, barY, CELL_W, animBarH, 6, 6);
                    g2.setColor(borderColor);
                    g2.setStroke(new BasicStroke((as == 2 || as == 3) ? 1.5f + anim * 0.5f : 0.5f));
                    g2.drawRoundRect(x, barY, CELL_W, animBarH, 6, 6);

                    // cap glow line
                    if ((as == 2 || as == 3) && anim > 0.1f) {
                        Color cap = as == 3 ? ACCENT2 : WARN;
                        g2.setColor(new Color(cap.getRed(), cap.getGreen(), cap.getBlue(),
                                              (int)(255 * Math.min(1f, anim * 1.5f))));
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(x + 4, barY + 1, x + CELL_W - 4, barY + 1);
                    }

                    // value label
                    String val = String.valueOf(currentArray[idx]);
                    g2.setFont(MONO_B);
                    Color textCol = (as == 2 || as == 3) ? lerpColor(TEXT, as == 3 ? ACCENT2 : WARN, anim) : TEXT;
                    g2.setColor(textCol);
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_W - fm.stringWidth(val)) / 2;
                    int ty = (animBarH > fm.getAscent() + 8) ? barY + fm.getAscent() + 4 : barY - 4;
                    g2.drawString(val, tx, ty);

                    // index label below baseline
                    g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                    String idxStr = String.valueOf(idx);
                    FontMetrics fm2 = g2.getFontMetrics();
                    g2.drawString(idxStr, x + (CELL_W - fm2.stringWidth(idxStr)) / 2, baseY + 16);

                    // pointer arrow above active bar
                    if ((as == 2 || as == 3) && anim >= 0.1f) {
                        Color ac = (as == 3) ? ACCENT2 : WARN;
                        g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(),
                                              (int)(255 * Math.min(1f, anim * 1.5f))));
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(x + CELL_W / 2, barY - 5, x + CELL_W / 2, barY - 18);
                        int[] px = {x + CELL_W / 2 - 5, x + CELL_W / 2 + 5, x + CELL_W / 2};
                        int[] py = {barY - 16, barY - 16, barY - 5};
                        g2.fillPolygon(px, py, 3);
                    }
                }
            }
        }

        // ─────────────────────────────────────────────────────
        //  GRAPH
        // ─────────────────────────────────────────────────────
        private void drawGraph(Graphics2D g2) {
            if (currentGraph == null || currentGraph.isEmpty()) return;
            int w = getWidth(), h = getHeight();
            Map<String, Point> pos = treeLayout(w, h);
            int total = currentGraph.size();
            int r = Math.max(10, Math.min(22, 380 / Math.max(total, 1)));

            // edges first
            for (Map.Entry<String, List<String>> e : currentGraph.entrySet()) {
                Point from = pos.get(e.getKey());
                if (from == null) continue;
                for (String nb : e.getValue()) {
                    Point to = pos.get(nb);
                    if (to == null) continue;
                    boolean onPath = graphPath.contains(e.getKey()) && graphPath.contains(nb);
                    g2.setColor(onPath ? ACCENT : BORDER);
                    g2.setStroke(new BasicStroke(onPath ? 2.4f : 1f));
                    g2.drawLine(from.x, from.y, to.x, to.y);
                }
            }

            // nodes
            for (Map.Entry<String, Point> e : pos.entrySet()) {
                String node = e.getKey(); Point pt = e.getValue();
                boolean isVis  = graphVisited.contains(node);
                boolean isCur  = node.equals(graphCurrent);
                boolean isPath = graphPath.contains(node);
                boolean isSt   = node.equals(graphStart);
                boolean isTgt  = node.equals(graphTarget);
                float pulse = nodeAnim.containsKey(node) ? nodeAnim.get(node) : 0f;

                Color fill   = isPath ? new Color(22,68,50) : isCur ? new Color(55,40,8)
                             : isVis  ? new Color(18,28,52) : CARD;
                Color stroke = isPath ? ACCENT2 : isCur ? WARN : isSt ? ACCENT : isTgt ? DANGER : BORDER;

                // pulse glow ring
                if (pulse > 0f && pulse < 1f) {
                    float ring = (float)Math.sin(pulse * Math.PI);
                    int rAlpha = (int)(80 * ring);
                    g2.setColor(new Color(stroke.getRed(), stroke.getGreen(), stroke.getBlue(), rAlpha));
                    int rr = r + (int)(ring * 8);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(pt.x - rr, pt.y - rr, rr * 2, rr * 2);
                }

                g2.setColor(fill);
                g2.fillOval(pt.x - r, pt.y - r, r * 2, r * 2);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(isPath || isCur ? 2.2f : 1f));
                g2.drawOval(pt.x - r, pt.y - r, r * 2, r * 2);

                if (r >= 12) {
                    int fs = Math.max(8, Math.min(12, r - 4));
                    g2.setFont(new Font("JetBrains Mono", Font.BOLD, fs));
                    g2.setColor(isPath ? ACCENT2 : isCur ? WARN : TEXT);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(node, pt.x - fm.stringWidth(node) / 2, pt.y + fm.getAscent() / 2 - 1);
                }
            }

            // legend
            drawDot(g2, 20, h - 60, ACCENT,  "start");
            drawDot(g2, 20, h - 40, DANGER,  "target");
            drawDot(g2, 20, h - 20, ACCENT2, "path");
        }

        private Map<String, Point> treeLayout(int w, int h) {
            Map<String, Point> pos = new LinkedHashMap<>();
            if (currentGraph.isEmpty()) return pos;
            String root = currentGraph.keySet().iterator().next();
            Map<String, Integer> level = new LinkedHashMap<>();
            Queue<String> q = new LinkedList<>();
            q.add(root); level.put(root, 0);
            int maxLevel = 0;
            while (!q.isEmpty()) {
                String node = q.poll();
                int lv = level.get(node);
                if (lv > maxLevel) maxLevel = lv;
                for (String c : currentGraph.getOrDefault(node, new ArrayList<>())) {
                    if (!level.containsKey(c)) { level.put(c, lv + 1); q.add(c); }
                }
            }
            Map<Integer, List<String>> byLevel = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : level.entrySet()) {
                byLevel.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }
            int topPad = 30, botPad = 60;
            int usableH = h - topPad - botPad;
            int levels = maxLevel + 1;
            for (Map.Entry<Integer, List<String>> e : byLevel.entrySet()) {
                int lv = e.getKey();
                List<String> nodes = e.getValue();
                int y = topPad + (levels <= 1 ? usableH / 2 : (int)((double)lv / (levels - 1) * usableH));
                for (int i = 0; i < nodes.size(); i++) {
                    int x = (int)((i + 1.0) / (nodes.size() + 1) * w);
                    pos.put(nodes.get(i), new Point(x, y));
                }
            }
            return pos;
        }

        private void drawDot(Graphics2D g2, int x, int y, Color c, String label) {
            g2.setColor(c); g2.fillOval(x, y - 5, 10, 10);
            g2.setFont(SMALL); g2.setColor(TEXT_DIM);
            g2.drawString(label, x + 16, y + 4);
        }

        // ─────────────────────────────────────────────────────
        //  STRING
        // ─────────────────────────────────────────────────────
        private void drawString(Graphics2D g2) {
            if (textStr.isEmpty()) return;
            int w = getWidth(), h = getHeight();
            int maxLen = Math.max(textStr.length(), patStr.length());
            int cellW  = Math.max(12, Math.min(26, (w - 48) / Math.max(maxLen, 1)));
            int cellH  = 34, gap = 2, startX = 24;
            int ty     = h / 2 - cellH - 20;

            g2.setFont(SMALL); g2.setColor(TEXT_HINT);
            g2.drawString("text", startX, ty - 4);

            for (int i = 0; i < textStr.length(); i++) {
                int x = startX + i * (cellW + gap);
                int currentIndex = i;
                if (x + cellW > w - 8) break;
                boolean isMatch   = strMatches.stream().anyMatch(m -> currentIndex >= m && currentIndex < m + patStr.length());
                boolean isCurrent = (i == textI);
                Color bg2 = isMatch ? CELL_FOUND : isCurrent ? CELL_CHECK : CELL_DEF;
                Color bc  = isMatch ? ACCENT2    : isCurrent ? WARN       : BORDER;
                g2.setColor(bg2); g2.fillRoundRect(x, ty, cellW, cellH, 4, 4);
                g2.setColor(bc); g2.setStroke(new BasicStroke(isMatch || isCurrent ? 1.5f : 0.5f));
                g2.drawRoundRect(x, ty, cellW, cellH, 4, 4);
                int fs = Math.max(9, cellW - 6);
                g2.setFont(new Font("JetBrains Mono", Font.PLAIN, fs));
                g2.setColor(isMatch ? ACCENT2 : isCurrent ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                String ch = String.valueOf(textStr.charAt(i));
                g2.drawString(ch, x + (cellW - fm.stringWidth(ch)) / 2, ty + (cellH + fm.getAscent()) / 2 - 2);
            }

            if (!patStr.isEmpty() && textI >= 0 && textI < textStr.length()) {
                int py2 = ty + cellH + 20;
                g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                g2.drawString("pattern", startX, py2 - 4);
                int ws = Math.max(0, textI - patJ);
                for (int j = 0; j < patStr.length(); j++) {
                    int x = startX + (ws + j) * (cellW + gap);
                    if (x + cellW > w - 8) break;
                    boolean active = (j == patJ);
                    g2.setColor(active ? CELL_CHECK : CARD);
                    g2.fillRoundRect(x, py2, cellW, cellH, 4, 4);
                    g2.setColor(active ? WARN : BORDER);
                    g2.setStroke(new BasicStroke(active ? 1.5f : 0.5f));
                    g2.drawRoundRect(x, py2, cellW, cellH, 4, 4);
                    int fs = Math.max(9, cellW - 6);
                    g2.setFont(new Font("JetBrains Mono", Font.PLAIN, fs));
                    g2.setColor(active ? WARN : TEXT_DIM);
                    FontMetrics fm = g2.getFontMetrics();
                    String ch = String.valueOf(patStr.charAt(j));
                    g2.drawString(ch, x + (cellW - fm.stringWidth(ch)) / 2, py2 + (cellH + fm.getAscent()) / 2 - 2);
                }
            }
            if (!strMatches.isEmpty()) {
                g2.setFont(SMALL); g2.setColor(ACCENT2);
                g2.drawString(strMatches.size() + " match(es) — positions: " + strMatches, startX, h - 52);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  COLOUR + LAYOUT HELPERS
    // ═══════════════════════════════════════════════════════════
    private Color lerpColor(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
        );
    }

    private int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number)
            return Double.compare(((Number)a).doubleValue(), ((Number)b).doubleValue());
        if (a instanceof String && b instanceof String)
            return ignoreCase ? ((String)a).compareToIgnoreCase((String)b) : ((String)a).compareTo((String)b);
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private void setStatus(String msg) { SwingUtilities.invokeLater(() -> statusLabel.setText(msg)); }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            cmpLabel.setText("comparisons: " + comparisons);
            timeLabel.setText("time: " + (elapsedNs > 0 ? elapsedNs + " ns" : "—"));
            resultLabel.setText(resultIndex == -1 ? "result: not found"
                              : resultIndex >= 0  ? "result: index " + resultIndex : "result: —");
        });
    }

    private void resetVisuals() {
        if (running) return;
        if (vizPanel == null) return;
        int n = currentArray != null ? currentArray.length : 0;
        cellStates    = new int[n];
        cellAnim      = new float[n];
        cellAnimPrev  = new float[n];
        cellAnimState = new int[n];
        comparisons = 0; elapsedNs = 0; resultIndex = -2;
        matchPositions.clear(); visitOrder.clear(); nodeAnim.clear();
        captionPhase = 0; captionProgress = 0f; captionText = ""; captionQueued = null;
        vizPanel.graphVisited.clear(); vizPanel.graphCurrent = null;
        vizPanel.graphPath.clear(); vizPanel.graphPathList = null;
        vizPanel.textI = -1; vizPanel.patJ = -1; vizPanel.strMatches.clear();
        vizPanel.textStr = currentText; vizPanel.patStr = currentPattern;
        vizPanel.mode = isGraphAlgo() ? "graph" : isStringAlgo() ? "string" : "array";
        if (statusLabel != null) statusLabel.setText("ready — press run");
        if (cmpLabel    != null) cmpLabel.setText("comparisons: —");
        if (timeLabel   != null) timeLabel.setText("time: —");
        if (resultLabel != null) resultLabel.setText("result: —");
        vizPanel.revalidate();
        vizPanel.repaint();
    }

    private void refreshInputPanel() {
        if (isArrayAlgo())      inputCards.show(inputPanel, "array");
        else if (isGraphAlgo()) inputCards.show(inputPanel, "graph");
        else                    inputCards.show(inputPanel, "string");
        resetVisuals();
    }

    private void parseArrayInput(String arrText, String tgtText, boolean ic) {
        if (running) return;
        try {
            String[] parts = arrText.split(",");
            if (parts.length == 0) return;
            Object[] arr = new Object[parts.length];
            for (int i = 0; i < parts.length; i++) {
                String s = parts[i].trim();
                if (s.isEmpty()) return;
                try { arr[i] = Integer.parseInt(s); }
                catch (NumberFormatException e1) {
                    try { arr[i] = Double.parseDouble(s); }
                    catch (NumberFormatException e2) { arr[i] = s; }
                }
            }
            currentArray = arr;
            String tgt = tgtText.trim();
            if (!tgt.isEmpty()) {
                try { currentTarget = Integer.parseInt(tgt); }
                catch (NumberFormatException e1) {
                    try { currentTarget = Double.parseDouble(tgt); }
                    catch (NumberFormatException e2) { currentTarget = tgt; }
                }
            }
            ignoreCase = ic;
            resetVisuals();
        } catch (Exception ignored) {}
    }

    private boolean isArrayAlgo()  { for (String s : ARRAY_ALGOS)  if (s.equals(selectedAlgo)) return true; return false; }
    private boolean isGraphAlgo()  { for (String s : GRAPH_ALGOS)  if (s.equals(selectedAlgo)) return true; return false; }
    private boolean isStringAlgo() { for (String s : STRING_ALGOS) if (s.equals(selectedAlgo)) return true; return false; }

    // ── widget builders ───────────────────────────────────────
    private JPanel roundPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false); return p;
    }
    private JLabel dimLabel(String text)  { JLabel l = new JLabel(text); l.setFont(SMALL); l.setForeground(TEXT_DIM);  return l; }
    private JLabel monoLabel(String text) { JLabel l = new JLabel(text); l.setFont(MONO);  l.setForeground(TEXT_DIM);  return l; }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(14, 16, 21)); f.setForeground(TEXT);
        f.setCaretColor(ACCENT); f.setFont(MONO);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(4, 8, 4, 8)));
        return f;
    }
    private JSpinner styledSpinner(SpinnerNumberModel model) {
        JSpinner s = new JSpinner(model);
        s.setPreferredSize(new Dimension(54, 28));
        JComponent ed = s.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor)ed).getTextField();
            tf.setBackground(new Color(14, 16, 21)); tf.setForeground(TEXT);
            tf.setFont(MONO); tf.setCaretColor(ACCENT);
            tf.setBorder(new EmptyBorder(2, 4, 2, 4));
        }
        return s;
    }
    private JCheckBox styledCheckbox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(SMALL); cb.setForeground(TEXT_DIM);
        cb.setOpaque(false); cb.setFocusPainted(false); return cb;
    }
    private JButton actionButton(String label, boolean primary) {
        JButton b = new JButton(label) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(primary ? ACCENT : CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                super.paintComponent(g);
            }
        };
        b.setFont(SANS_B); b.setForeground(primary ? Color.WHITE : TEXT_DIM);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        int w = label.length() >= 10 ? 120 : label.length() >= 6 ? 100 : 80;
        b.setPreferredSize(new Dimension(w, 34));
        return b;
    }

    class AlgoButton extends JButton {
        private boolean hovered = false;
        AlgoButton(String name) {
            super("  " + name);
            setFont(SANS); setForeground(TEXT_DIM);
            setOpaque(false); setContentAreaFilled(false);
            setBorderPainted(false); setFocusPainted(false);
            setHorizontalAlignment(SwingConstants.LEFT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(232, 32));
            setPreferredSize(new Dimension(232, 32));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getText().trim().equals(selectedAlgo);
            if (active) {
                g2.setColor(new Color(28, 38, 70));
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 6, 6);
                g2.setColor(ACCENT);
                g2.fillRoundRect(8, 2, 3, getHeight() - 4, 2, 2);
            } else if (hovered) {
                g2.setColor(new Color(24, 27, 38));
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 6, 6);
            }
            setForeground(active ? ACCENT : hovered ? TEXT : TEXT_DIM);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AlgorithmGUI::new);
    }
}