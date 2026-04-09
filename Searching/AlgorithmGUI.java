import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
    private static final Color CELL_CHECK = new Color(60,  45,  15);
    private static final Color CELL_FOUND = new Color(15,  50,  35);
    private static final Color CAPTION_BG = new Color(18,  22,  32);

    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font MONO_B = new Font("JetBrains Mono", Font.BOLD,  13);
    private static final Font SANS   = new Font("Segoe UI",       Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font TITLE  = new Font("Segoe UI",       Font.BOLD,  22);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN, 11);
    private static final Font CAP_F  = new Font("Segoe UI",       Font.PLAIN, 13);

    // ── animation state ───────────────────────────────────────
    // cellAnimProgress[i] = 0..1 for bar-grow / glow animation
    private float[]  cellAnim;
    private Timer    animTimer;
    private String   captionText = "";
    private float    captionAlpha = 0f;
    private Timer    captionFadeTimer;

    // ── algo state ────────────────────────────────────────────
    private String   selectedAlgo = "Linear Search";
    private int[]    cellStates;
    private int      comparisons  = 0;
    private long     elapsedNs    = 0;
    private int      resultIndex  = -2;
    private boolean  running      = false;
    private int      stepDelay    = 350;
    private List<Integer> matchPositions = new ArrayList<>();
    private List<String>  visitOrder     = new ArrayList<>();
    private boolean  showCaptions = true;

    // ── data ──────────────────────────────────────────────────
    private Object[] currentArray  = {2, 5, 8, 11, 14, 19, 27, 33, 45};
    private Object   currentTarget = 19;
    private boolean  ignoreCase    = false;
    private String   currentText    = "the cat sat on the caterpillar";
    private String   currentPattern = "cat";
    private Map<String, List<String>> currentGraph;
    private String   graphStart  = "A";
    private String   graphTarget = "C";
    private int      graphDepth  = 3;   // customizable tree depth
    private int      graphBranch = 2;   // customizable branching factor

    // ── swing refs ────────────────────────────────────────────
    private VisualizerPanel vizPanel;
    private JLabel  statusLabel, cmpLabel, timeLabel, resultLabel, algoLabel;
    private JSlider speedSlider;
    private JButton runBtn, resetBtn, codeBtn;
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
    //  CAPTIONS — per-algorithm step descriptions
    // ═══════════════════════════════════════════════════════════
    private static final Map<String, String[]> CAPTIONS = new HashMap<>();
    static {
        CAPTIONS.put("LINEAR_CHECK",    new String[]{"Checking index %d — is arr[%d] = %s equal to target %s?"});
        CAPTIONS.put("LINEAR_MISS",     new String[]{"arr[%d] = %s ≠ target. Move to next element."});
        CAPTIONS.put("LINEAR_FOUND",    new String[]{"Match found! arr[%d] = %s equals target. Return index %d."});
        CAPTIONS.put("LINEAR_NOTFOUND", new String[]{"Reached end of array — target not found. Return -1."});
        CAPTIONS.put("BINARY_MID",      new String[]{"Midpoint is index %d (value %s). Compare with target %s."});
        CAPTIONS.put("BINARY_LEFT",     new String[]{"Target < mid. Discard right half — search left side."});
        CAPTIONS.put("BINARY_RIGHT",    new String[]{"Target > mid. Discard left half — search right side."});
        CAPTIONS.put("BINARY_FOUND",    new String[]{"Match found at index %d!"});
        CAPTIONS.put("TERNARY_MIDS",    new String[]{"Dividing range into thirds. Checking mid1[%d]=%s and mid2[%d]=%s."});
        CAPTIONS.put("TERNARY_LEFT",    new String[]{"Target < mid1. Search the left third."});
        CAPTIONS.put("TERNARY_RIGHT",   new String[]{"Target > mid2. Search the right third."});
        CAPTIONS.put("TERNARY_MID",     new String[]{"Target is between mid1 and mid2. Search the middle third."});
        CAPTIONS.put("JUMP_JUMP",       new String[]{"Jumping ahead by step √n=%d — checking index %d."});
        CAPTIONS.put("JUMP_LINEAR",     new String[]{"Overshot target block. Running linear search from index %d."});
        CAPTIONS.put("INTERP_POS",      new String[]{"Estimating position using value distribution — probe at index %d."});
        CAPTIONS.put("INTERP_LEFT",     new String[]{"Probe value < target. Search right portion."});
        CAPTIONS.put("INTERP_RIGHT",    new String[]{"Probe value > target. Search left portion."});
        CAPTIONS.put("EXP_BOUND",       new String[]{"Doubling bound to %d — finding range that contains target."});
        CAPTIONS.put("EXP_BINARY",      new String[]{"Range found [%d, %d]. Now running binary search within it."});
        CAPTIONS.put("FIB_PROBE",       new String[]{"Fibonacci probe at index %d (fibM2=%d). Comparing with target."});
        CAPTIONS.put("FIB_LEFT",        new String[]{"Value > target. Reduce fibonacci numbers — search left portion."});
        CAPTIONS.put("FIB_RIGHT",       new String[]{"Value < target. Shift offset right — search right portion."});
        CAPTIONS.put("GRAPH_VISIT",     new String[]{"Visiting node '%s' — checking if it is the target '%s'."});
        CAPTIONS.put("GRAPH_ENQUEUE",   new String[]{"Adding neighbors of '%s' to the queue: %s"});
        CAPTIONS.put("GRAPH_PUSH",      new String[]{"Pushing neighbors of '%s' onto the stack: %s"});
        CAPTIONS.put("GRAPH_PATH",      new String[]{"Target found! Tracing path back to start: %s"});
        CAPTIONS.put("KMP_MATCH",       new String[]{"text[%d]='%s' matches pattern[%d]='%s' — both pointers advance."});
        CAPTIONS.put("KMP_MISMATCH",    new String[]{"Mismatch at text[%d]. Using LPS table to skip — jump pattern to %d."});
        CAPTIONS.put("KMP_FOUND",       new String[]{"Pattern match found at position %d!"});
        CAPTIONS.put("RK_WINDOW",       new String[]{"Sliding window at position %d. Rolling hash computed."});
        CAPTIONS.put("RK_MATCH",        new String[]{"Hash match at position %d — verifying exact match."});
        CAPTIONS.put("RK_FOUND",        new String[]{"Verified match at position %d!"});
    }

    // ═══════════════════════════════════════════════════════════
    //  CODE SNIPPETS
    // ═══════════════════════════════════════════════════════════
    private static final Map<String, String> CODE_MAP = new HashMap<>();
    static {
        CODE_MAP.put("Linear Search",
            "public static int linearSearch(Object[] arr, Object target, boolean ignoreCase) {\n" +
            "    for (int i = 0; i < arr.length; i++) {\n" +
            "        comparisons++;\n" +
            "        if (ignoreCase && target instanceof String && arr[i] instanceof String) {\n" +
            "            if (((String) arr[i]).equalsIgnoreCase((String) target)) return i;\n" +
            "        } else if (arr[i] instanceof Number && target instanceof Number) {\n" +
            "            if (((Number) arr[i]).doubleValue() == ((Number) target).doubleValue()) return i;\n" +
            "        } else {\n" +
            "            if (arr[i].equals(target)) return i;\n" +
            "        }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Binary Search",
            "public static int binarySearch(Object[] arr, Object target, boolean ignoreCase) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        int mid = (low + high) / 2;\n" +
            "        comparisons++;\n" +
            "        int c = compare(arr[mid], target);\n" +
            "        if (c == 0) return mid;\n" +
            "        else if (c < 0) low = mid + 1;   // search right\n" +
            "        else            high = mid - 1;  // search left\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Ternary Search",
            "public static int ternarySearch(Object[] arr, Object target) {\n" +
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
            "public static int jumpSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length;\n" +
            "    int step = (int) Math.floor(Math.sqrt(n));\n" +
            "    int prev = 0, curr = step;\n" +
            "    while (curr < n && compare(arr[curr], target) <= 0) {\n" +
            "        prev = curr;\n" +
            "        curr += step;\n" +
            "        comparisons++;\n" +
            "    }\n" +
            "    // linear search in block [prev, min(curr, n)]\n" +
            "    for (int i = prev; i < Math.min(curr, n); i++) {\n" +
            "        comparisons++;\n" +
            "        if (compare(arr[i], target) == 0) return i;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Interpolation Search",
            "public static int interpolationSearch(Object[] arr, Object target) {\n" +
            "    int low = 0, high = arr.length - 1;\n" +
            "    while (low <= high) {\n" +
            "        double lo = toDouble(arr[low]), hi = toDouble(arr[high]);\n" +
            "        double tv = toDouble(target);\n" +
            "        int pos = (hi == lo) ? low\n" +
            "            : low + (int)(((tv - lo) / (hi - lo)) * (high - low));\n" +
            "        if (pos < low || pos > high) break;\n" +
            "        comparisons++;\n" +
            "        int c = compare(arr[pos], target);\n" +
            "        if (c == 0) return pos;\n" +
            "        else if (c < 0) low  = pos + 1;\n" +
            "        else            high = pos - 1;\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("Exponential Search",
            "public static int exponentialSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length;\n" +
            "    if (compare(arr[0], target) == 0) return 0;\n" +
            "    int bound = 1;\n" +
            "    while (bound < n && compare(arr[bound], target) <= 0) {\n" +
            "        bound *= 2;  // double the bound\n" +
            "        comparisons++;\n" +
            "    }\n" +
            "    // binary search in [bound/2, min(bound, n-1)]\n" +
            "    int low = bound / 2, high = Math.min(bound, n - 1);\n" +
            "    return binarySearch(arr, target, low, high);\n" +
            "}");
        CODE_MAP.put("Fibonacci Search",
            "public static int fibonacciSearch(Object[] arr, Object target) {\n" +
            "    int n = arr.length;\n" +
            "    int fm2 = 0, fm1 = 1, fib = 1;\n" +
            "    while (fib < n) { fm2 = fm1; fm1 = fib; fib = fm1 + fm2; }\n" +
            "    int offset = -1;\n" +
            "    while (fib > 1) {\n" +
            "        int i = Math.min(offset + fm2, n - 1);\n" +
            "        comparisons++;\n" +
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
            "    queue.add(start);\n" +
            "    parent.put(start, null);\n" +
            "    while (!queue.isEmpty()) {\n" +
            "        String node = queue.poll();  // FIFO — level by level\n" +
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
            "        String node = stack.pop();   // LIFO — depth first\n" +
            "        if (visited.contains(node)) continue;\n" +
            "        visited.add(node);\n" +
            "        comparisons++;\n" +
            "        if (node.equals(target)) return buildPath(parent, node);\n" +
            "        List<String> neighbors = graph.getOrDefault(node, List.of());\n" +
            "        // reverse to preserve left-to-right order\n" +
            "        for (int i = neighbors.size()-1; i >= 0; i--) {\n" +
            "            String nb = neighbors.get(i);\n" +
            "            if (!visited.contains(nb)) {\n" +
            "                if (!parent.containsKey(nb)) parent.put(nb, node);\n" +
            "                stack.push(nb);\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    return -1;\n" +
            "}");
        CODE_MAP.put("KMP Search",
            "public List<Integer> kmpSearch(String text, String pattern) {\n" +
            "    int[] lps = buildLPS(pattern);  // O(m) preprocessing\n" +
            "    List<Integer> results = new ArrayList<>();\n" +
            "    int i = 0, j = 0;\n" +
            "    while (i < text.length()) {\n" +
            "        comparisons++;\n" +
            "        if (text.charAt(i) == pattern.charAt(j)) { i++; j++; }\n" +
            "        if (j == pattern.length()) {\n" +
            "            results.add(i - j);  // match found\n" +
            "            j = lps[j - 1];      // skip re-checking\n" +
            "        } else if (i < text.length() && text.charAt(i) != pattern.charAt(j)) {\n" +
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
            "    if (wh == ph && text.substring(0, m).equals(pattern)) results.add(0);\n" +
            "    for (int i = 1; i <= n - m; i++) {\n" +
            "        // rolling hash: remove left char, add right char\n" +
            "        wh = (wh - text.charAt(i-1) * pw % MOD + MOD) % MOD;\n" +
            "        wh = (wh * BASE + text.charAt(i+m-1)) % MOD;\n" +
            "        comparisons++;\n" +
            "        if (wh == ph && text.substring(i, i+m).equals(pattern)) results.add(i);\n" +
            "    }\n" +
            "    return results;\n" +
            "}");
    }

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
        setVisible(true);
        refreshInputPanel();
    }

    // ═══════════════════════════════════════════════════════════
    //  SIDEBAR
    // ═══════════════════════════════════════════════════════════
    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(PANEL);
        side.setPreferredSize(new Dimension(230, 0));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JLabel title = new JLabel("  searchviz");
        title.setFont(TITLE);
        title.setForeground(ACCENT);
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
        algoLabel.setFont(SANS_B);
        algoLabel.setForeground(TEXT);
        bar.add(algoLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        // captions toggle
        captionToggle = new JCheckBox("captions");
        captionToggle.setSelected(true);
        captionToggle.setFont(SMALL);
        captionToggle.setForeground(TEXT_DIM);
        captionToggle.setOpaque(false);
        captionToggle.setFocusPainted(false);
        captionToggle.addActionListener(e -> showCaptions = captionToggle.isSelected());

        // view code button
        codeBtn = actionButton("view code", false);
        codeBtn.addActionListener(e -> showCodeDialog());

        // speed
        JLabel sl = new JLabel("speed");
        sl.setFont(SMALL); sl.setForeground(TEXT_DIM);
        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(PANEL);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] d = {700, 450, 280, 130, 50};
            stepDelay = d[speedSlider.getValue() - 1];
        });

        right.add(captionToggle);
        right.add(codeBtn);
        right.add(sl);
        right.add(speedSlider);
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
        center.add(vizPanel, BorderLayout.CENTER);
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

        // depth and branch spinners
        SpinnerNumberModel depthModel  = new SpinnerNumberModel(3, 1, 6, 1);
        SpinnerNumberModel branchModel = new SpinnerNumberModel(2, 2, 4, 1);
        JSpinner depthSpin  = styledSpinner(depthModel);
        JSpinner branchSpin = styledSpinner(branchModel);

        Runnable applyGraph = () -> {
            graphStart  = sField.getText().trim().isEmpty() ? "A" : sField.getText().trim().toUpperCase();
            graphTarget = tField.getText().trim().isEmpty() ? "C" : tField.getText().trim().toUpperCase();
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
    //  CODE VIEWER DIALOG
    // ═══════════════════════════════════════════════════════════
    private void showCodeDialog() {
        JDialog dlg = new JDialog(this, selectedAlgo + " — source code", false);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        String code = CODE_MAP.getOrDefault(selectedAlgo, "// code not available");

        JTextArea area = new JTextArea(code);
        area.setFont(MONO);
        area.setBackground(new Color(14, 16, 22));
        area.setForeground(new Color(190, 200, 220));
        area.setCaretColor(ACCENT);
        area.setEditable(false);
        area.setLineWrap(false);
        area.setBorder(new EmptyBorder(16, 20, 16, 20));
        area.setSelectionColor(new Color(50, 80, 140));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.getViewport().setBackground(new Color(14, 16, 22));
        scroll.setPreferredSize(new Dimension(680, 420));

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            new EmptyBorder(12, 20, 12, 20)));
        JLabel hTitle = new JLabel(selectedAlgo);
        hTitle.setFont(SANS_B); hTitle.setForeground(TEXT);
        JLabel complexity = new JLabel("time: " + getTimeComplexity(selectedAlgo) + "   space: " + getSpaceComplexity(selectedAlgo));
        complexity.setFont(SMALL); complexity.setForeground(TEXT_DIM);
        header.add(hTitle, BorderLayout.WEST);
        header.add(complexity, BorderLayout.EAST);

        dlg.add(header, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

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
    //  GRAPH BUILDER — generates tree from depth + branch
    // ═══════════════════════════════════════════════════════════
    private void buildTreeGraph() {
        currentGraph = new LinkedHashMap<>();
        int[] counter = {0};
        buildNode(null, graphDepth, graphBranch, counter);
        // ensure graphStart and graphTarget exist in the graph
        List<String> nodes = new ArrayList<>(currentGraph.keySet());
        if (!nodes.isEmpty() && !currentGraph.containsKey(graphStart)) graphStart = nodes.get(0);
        if (nodes.size() > 1 && !currentGraph.containsKey(graphTarget)) graphTarget = nodes.get(nodes.size() - 1);
    }

    private String buildNode(String parent, int depth, int branch, int[] counter) {
        String name = nodeLabel(counter[0]++);
        currentGraph.put(name, new ArrayList<>());
        if (parent != null) {
            currentGraph.get(parent).add(name);
        }
        if (depth > 1) {
            for (int i = 0; i < branch; i++) {
                buildNode(name, depth - 1, branch, counter);
            }
        }
        return name;
    }

    // label nodes A, B, C … Z, AA, AB …
    private String nodeLabel(int n) {
        StringBuilder sb = new StringBuilder();
        do {
            sb.insert(0, (char)('A' + n % 26));
            n = n / 26 - 1;
        } while (n >= 0);
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════
    //  CAPTION SYSTEM
    // ═══════════════════════════════════════════════════════════
    private void showCaption(String text) {
        if (!showCaptions) return;
        captionText  = text;
        captionAlpha = 1f;
        if (captionFadeTimer != null) { captionFadeTimer.cancel(); captionFadeTimer = null; }
        Timer ft = new Timer(true);
        captionFadeTimer = ft;
        ft.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                captionAlpha = Math.max(0f, captionAlpha - 0.04f);
                SwingUtilities.invokeLater(() -> vizPanel.repaint());
                if (captionAlpha <= 0f) { ft.cancel(); }
            }
        }, (long)(stepDelay * 0.7), 40);
        SwingUtilities.invokeLater(() -> vizPanel.repaint());
    }

    // ── animate a cell highlight (pulse effect) ───────────────
    private void animateCell(int idx) {
        if (cellAnim == null || idx < 0 || idx >= cellAnim.length) return;
        if (animTimer != null) { animTimer.cancel(); animTimer = null; }
        Timer at = new Timer(true);
        animTimer = at;
        final float[] t = {0f};
        at.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() {
                t[0] += 0.12f;
                if (t[0] >= 1f) { t[0] = 1f; cellAnim[idx] = 1f; at.cancel(); }
                else { cellAnim[idx] = t[0]; }
                SwingUtilities.invokeLater(() -> { if (vizPanel != null) vizPanel.repaint(); });
            }
        }, 0, 16);
    }

    // ═══════════════════════════════════════════════════════════
    //  RUN LOGIC
    // ═══════════════════════════════════════════════════════════
    private void runAlgorithm() {
        running = true;
        runBtn.setEnabled(false);
        comparisons = 0; elapsedNs = 0; resultIndex = -2;
        matchPositions.clear(); visitOrder.clear();

        new Thread(() -> {
            try {
                if      (isArrayAlgo())  runArrayAlgo();
                else if (isGraphAlgo())  runGraphAlgo();
                else                     runStringAlgo();
            } catch (InterruptedException ignored) {
            } catch (Exception ex) { ex.printStackTrace(); }
            finally {
                running = false;
                SwingUtilities.invokeLater(() -> runBtn.setEnabled(true));
            }
        }).start();
    }

    private void runArrayAlgo() throws InterruptedException {
        int n = currentArray.length;
        cellStates = new int[n];
        cellAnim   = new float[n];
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

    // ── helper: check + animate + caption ────────────────────
    private String fmt(String key, Object... args) {
        String[] templates = CAPTIONS.get(key);
        if (templates == null) return key;
        try { return String.format(templates[0], args); }
        catch (Exception e) { return templates[0]; }
    }

    private void runLinear() throws InterruptedException {
        for (int i = 0; i < currentArray.length; i++) {
            showCaption(fmt("LINEAR_CHECK", i, i, currentArray[i], currentTarget));
            setCellState(i, 2); animateCell(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) {
                setCellState(i, 3); animateCell(i);
                showCaption(fmt("LINEAR_FOUND", i, currentArray[i], i));
                resultIndex = i; setStatus("found at index " + i); return;
            }
            showCaption(fmt("LINEAR_MISS", i, currentArray[i]));
            setCellState(i, 1);
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
            setCellState(mid, 2); animateCell(mid); sleep(); comparisons++;
            int c = compareValues(currentArray[mid], currentTarget);
            if (c == 0) {
                setCellState(mid, 3); animateCell(mid);
                showCaption(fmt("BINARY_FOUND", mid));
                resultIndex = mid; setStatus("found at index " + mid); return;
            }
            setCellState(mid, 1);
            if (c < 0) { showCaption(fmt("BINARY_RIGHT")); markScanned(low, mid - 1); low = mid + 1; }
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
            setCellState(m1, 2); setCellState(m2, 2); animateCell(m1); sleep(); comparisons += 2;
            int c1 = compareValues(currentArray[m1], currentTarget);
            int c2 = compareValues(currentArray[m2], currentTarget);
            if (c1 == 0) { setCellState(m1, 3); resultIndex = m1; setStatus("found at index " + m1); return; }
            if (c2 == 0) { setCellState(m2, 3); resultIndex = m2; setStatus("found at index " + m2); return; }
            setCellState(m1, 1); setCellState(m2, 1);
            if      (compareValues(currentTarget, currentArray[m1]) < 0) { showCaption(fmt("TERNARY_LEFT"));  markScanned(m1+1,high); high = m1-1; }
            else if (compareValues(currentTarget, currentArray[m2]) > 0) { showCaption(fmt("TERNARY_RIGHT")); markScanned(low,m2-1);  low  = m2+1; }
            else { showCaption(fmt("TERNARY_MID")); markScanned(low,m1-1); markScanned(m2+1,high); low=m1+1; high=m2-1; }
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
            setCellState(curr, 2); animateCell(curr); sleep(); comparisons++;
            setCellState(curr, 1);
            prev = curr; curr += step;
        }
        showCaption(fmt("JUMP_LINEAR", prev));
        sleep();
        for (int i = prev; i < Math.min(curr, n); i++) {
            setCellState(i, 2); animateCell(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) {
                setCellState(i, 3); resultIndex = i; setStatus("found at index " + i); return;
            }
            setCellState(i, 1);
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runInterpolation() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int pos;
            if (currentArray[low] instanceof Number && currentTarget instanceof Number) {
                double lo = ((Number) currentArray[low]).doubleValue();
                double hi = ((Number) currentArray[high]).doubleValue();
                double tv = ((Number) currentTarget).doubleValue();
                pos = (hi == lo) ? low : low + (int)(((tv - lo) / (hi - lo)) * (high - low));
            } else { pos = (low + high) / 2; }
            if (pos < low || pos > high) break;
            showCaption(fmt("INTERP_POS", pos));
            setCellState(pos, 2); animateCell(pos); sleep(); comparisons++;
            int c = compareValues(currentArray[pos], currentTarget);
            if (c == 0) { setCellState(pos, 3); resultIndex = pos; setStatus("found at index " + pos); return; }
            setCellState(pos, 1);
            if (c < 0) { showCaption(fmt("INTERP_LEFT")); low = pos + 1; }
            else        { showCaption(fmt("INTERP_RIGHT")); high = pos - 1; }
            sleep();
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runExponential() throws InterruptedException {
        int n = currentArray.length;
        setCellState(0, 2); animateCell(0); sleep(); comparisons++;
        if (matches(currentArray[0], currentTarget)) { setCellState(0, 3); resultIndex = 0; setStatus("found at index 0"); return; }
        setCellState(0, 1);
        int bound = 1;
        while (bound < n && compareValues(currentArray[bound], currentTarget) <= 0) {
            showCaption(fmt("EXP_BOUND", bound));
            setCellState(bound, 2); animateCell(bound); sleep(); comparisons++;
            setCellState(bound, 1);
            bound *= 2;
        }
        int low = bound / 2, high = Math.min(bound, n - 1);
        showCaption(fmt("EXP_BINARY", low, high));
        sleep();
        while (low <= high) {
            int mid = (low + high) / 2;
            setCellState(mid, 2); animateCell(mid); sleep(); comparisons++;
            int c = compareValues(currentArray[mid], currentTarget);
            if (c == 0) { setCellState(mid, 3); resultIndex = mid; setStatus("found at index " + mid); return; }
            setCellState(mid, 1);
            if (c < 0) low = mid + 1; else high = mid - 1;
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
            setCellState(i, 2); animateCell(i); sleep(); comparisons++;
            int c = compareValues(currentArray[i], currentTarget);
            if (c < 0)      { showCaption(fmt("FIB_RIGHT")); fib=fm1; fm1=fm2; fm2=fib-fm1; offset=i; setCellState(i,1); }
            else if (c > 0) { showCaption(fmt("FIB_LEFT"));  fib=fm2; fm1-=fm2; fm2=fib-fm1; setCellState(i,1); }
            else            { setCellState(i,3); resultIndex=i; setStatus("found at index "+i); return; }
            sleep();
        }
        if (fm1==1 && offset+1 < n) {
            int i = offset+1;
            setCellState(i,2); animateCell(i); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) { setCellState(i,3); resultIndex=i; setStatus("found at index "+i); return; }
            setCellState(i,1);
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runGraphAlgo() throws InterruptedException {
        visitOrder.clear();
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
            showCaption(fmt("GRAPH_VISIT", node, graphTarget));
            vizPanel.setGraphState(visited, node, Collections.emptySet(), null); sleep();
            if (node.equals(graphTarget)) {
                List<String> path = new ArrayList<>();
                String cur = node;
                while (cur != null) { path.add(0, cur); cur = parent.get(cur); }
                showCaption(fmt("GRAPH_PATH", path));
                vizPanel.setGraphState(visited, null, new HashSet<>(path), path);
                resultIndex = visitOrder.size()-1;
                setStatus("found '" + graphTarget + "' — path: " + path);
                found = true; break;
            }
            List<String> neighbors = new ArrayList<>(currentGraph.getOrDefault(node, new ArrayList<>()));
            if (!isBFS) Collections.reverse(neighbors);
            List<String> newNeighbors = new ArrayList<>();
            for (String nb : neighbors) {
                if (!visited.contains(nb)) {
                    if (!parent.containsKey(nb)) parent.put(nb, node);
                    if (isBFS) queue.add(nb); else stack.push(nb);
                    newNeighbors.add(nb);
                }
            }
            if (!newNeighbors.isEmpty()) {
                String captKey = isBFS ? "GRAPH_ENQUEUE" : "GRAPH_PUSH";
                showCaption(fmt(captKey, node, newNeighbors));
            }
        }
        elapsedNs = 0;
        if (!found) { resultIndex=-1; setStatus("'" + graphTarget + "' not reachable"); }
        updateStats();
    }

    // ── STRING ────────────────────────────────────────────────
    private void runStringAlgo() throws InterruptedException {
        matchPositions.clear();
        String t = ignoreCase ? currentText.toLowerCase()    : currentText;
        String p = ignoreCase ? currentPattern.toLowerCase() : currentPattern;
        int n = t.length(), m = p.length();
        if (p.isEmpty() || n==0) { resultIndex=-1; setStatus("text or pattern is empty"); updateStats(); return; }
        if (m > n)               { resultIndex=-1; setStatus("pattern longer than text");  updateStats(); return; }
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
                sleep();
                i++; j++;
                if (j == m) {
                    int matchStart = i - j;
                    matchPositions.add(matchStart);
                    showCaption(fmt("KMP_FOUND", matchStart));
                    j = lps[j - 1];
                }
            } else {
                int nextJ = (j > 0) ? lps[j - 1] : 0;
                showCaption(fmt("KMP_MISMATCH", i, nextJ));
                sleep();
                if (j > 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
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
        if (wh == ph && t.substring(0,m).equals(p)) { matchPositions.add(0); showCaption(fmt("RK_FOUND",0)); }
        else showCaption(fmt("RK_WINDOW", 0));
        vizPanel.setStringState(t, p, 0, 0, new ArrayList<>(matchPositions));
        sleep();
        for (int i = 1; i <= n-m; i++) {
            wh = (wh - t.charAt(i-1) * pw % MOD + MOD) % MOD;
            wh = (wh * BASE + t.charAt(i+m-1)) % MOD;
            comparisons++;
            vizPanel.setStringState(t, p, i, 0, new ArrayList<>(matchPositions));
            if (wh == ph) {
                showCaption(fmt("RK_MATCH", i));
                sleep();
                if (t.substring(i, i+m).equals(p)) { matchPositions.add(i); showCaption(fmt("RK_FOUND",i)); }
            } else {
                showCaption(fmt("RK_WINDOW", i));
                sleep();
            }
        }
    }

    private int[] buildLPS(String p) {
        int m = p.length(); int[] lps = new int[m];
        int len = 0, i = 1;
        while (i < m) {
            if (p.charAt(i) == p.charAt(len)) { lps[i++] = ++len; }
            else if (len > 0) { len = lps[len-1]; }
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

        VisualizerPanel() { setBackground(BG); setPreferredSize(new Dimension(800, 420)); }

        void setStringState(String t, String p, int ti, int pj, List<Integer> matches) {
            textStr = t; patStr = p; textI = ti; patJ = pj;
            strMatches = new ArrayList<>(matches); mode = "string";
            SwingUtilities.invokeLater(this::repaint);
        }
        void setGraphState(Set<String> vis, String cur, Set<String> path, List<String> pList) {
            graphVisited  = new HashSet<>(vis);
            graphCurrent  = cur;
            graphPath     = new HashSet<>(path);
            graphPathList = pList;
            mode = "graph";
            SwingUtilities.invokeLater(this::repaint);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            if      (mode.equals("graph"))  drawGraph(g2);
            else if (mode.equals("string")) drawString(g2);
            else                            drawArray(g2);
            if (showCaptions && captionAlpha > 0.02f) drawCaption(g2);
        }

        // ── CAPTION OVERLAY ───────────────────────────────────
        private void drawCaption(Graphics2D g2) {
            int w = getWidth(), h = getHeight();
            g2.setFont(CAP_F);
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(captionText);
            int cx = (w - tw) / 2;
            int cy = h - 28;
            int pad = 10;
            float clampedAlpha = Math.max(0f, Math.min(1f, captionAlpha));
            AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampedAlpha);
            g2.setComposite(ac);
            g2.setColor(CAPTION_BG);
            g2.fillRoundRect(cx - pad, cy - fm.getAscent() - 4, tw + pad*2, fm.getHeight() + 8, 8, 8);
            g2.setColor(TEXT);
            g2.drawString(captionText, cx, cy);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        // ── BAR ARRAY ─────────────────────────────────────────
        private void drawArray(Graphics2D g2) {
            if (currentArray == null || currentArray.length == 0) return;
            int n = currentArray.length, w = getWidth(), h = getHeight();
            int cellW = Math.min(72, (w - 48) / n);
            int gap = 6, totalW = n * cellW + (n-1)*gap;
            int startX = (w - totalW) / 2;
            int baseY = h - 44, topPad = 54, barArea = baseY - topPad;

            double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
            boolean allNum = true;
            for (Object o : currentArray) {
                if (o instanceof Number) {
                    double v = ((Number)o).doubleValue();
                    if (v < minVal) minVal = v; if (v > maxVal) maxVal = v;
                } else { allNum = false; }
            }
            double range = (maxVal == minVal) ? 1.0 : maxVal - minVal;
            int minBarH = 32;

            for (int i = 0; i < n; i++) {
                int x = startX + i * (cellW + gap);
                int state = (cellStates != null && i < cellStates.length) ? cellStates[i] : 0;

                int barH = allNum
                    ? minBarH + (int)(((((Number)currentArray[i]).doubleValue()-minVal)/range) * (barArea-minBarH))
                    : barArea/2;
                int barY = baseY - barH;

                // animated glow when cell is active
                float anim = (cellAnim != null && i < cellAnim.length) ? cellAnim[i] : 0f;

                Color bg2 = (state==1) ? CELL_SCAN : (state==2) ? CELL_CHECK : (state==3) ? CELL_FOUND : CELL_DEF;
                Color bc  = (state==2) ? WARN : (state==3) ? ACCENT2 : BORDER;

                // pulse: blend toward brighter on anim
                if ((state == 2 || state == 3) && anim < 1f) {
                    float t = (float)(Math.sin(anim * Math.PI));
                    Color glow = state == 3 ? ACCENT2 : WARN;
                    bg2 = blendColor(bg2, glow, t * 0.25f);
                }

                g2.setColor(bg2);
                g2.fillRoundRect(x, barY, cellW, barH, 6, 6);
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(state > 1 ? 1.8f : 0.5f));
                g2.drawRoundRect(x, barY, cellW, barH, 6, 6);

                // animated top-bar glow line
                if (state == 2 || state == 3) {
                    g2.setColor(state==3 ? ACCENT2 : WARN);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x+3, barY+1, x+cellW-3, barY+1);
                }

                String val = String.valueOf(currentArray[i]);
                g2.setFont(MONO_B); g2.setColor(state==3 ? ACCENT2 : state==2 ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (cellW - fm.stringWidth(val))/2;
                int ty = (barH > fm.getAscent()+8) ? barY + fm.getAscent()+4 : barY-4;
                g2.drawString(val, tx, ty);

                g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                String idx = String.valueOf(i);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(idx, x + (cellW - fm2.stringWidth(idx))/2, baseY+18);
            }

            // pointer arrows
            if (cellStates != null) {
                for (int i = 0; i < cellStates.length; i++) {
                    if (cellStates[i] == 2 || cellStates[i] == 3) {
                        int barH = allNum && currentArray[i] instanceof Number
                            ? minBarH + (int)(((((Number)currentArray[i]).doubleValue()-minVal)/range)*(barArea-minBarH))
                            : barArea/2;
                        int barY = baseY - barH;
                        int cx2 = startX + i*(cellW+gap) + cellW/2;
                        Color ac = cellStates[i]==3 ? ACCENT2 : WARN;
                        g2.setColor(ac);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(cx2, barY-6, cx2, barY-22);
                        int[] px = {cx2-5,cx2+5,cx2};
                        int[] py = {barY-20,barY-20,barY-6};
                        g2.fillPolygon(px, py, 3);
                    }
                }
            }
        }

        // ── GRAPH ─────────────────────────────────────────────
        private void drawGraph(Graphics2D g2) {
            if (currentGraph == null || currentGraph.isEmpty()) return;
            int w = getWidth(), h = getHeight();
            Map<String, Point> pos = treeLayout(w, h);

            // edges
            for (Map.Entry<String, List<String>> e : currentGraph.entrySet()) {
                Point from = pos.get(e.getKey());
                if (from == null) continue;
                for (String nb : e.getValue()) {
                    Point to = pos.get(nb);
                    if (to == null) continue;
                    boolean onPath = graphPath.contains(e.getKey()) && graphPath.contains(nb);
                    g2.setColor(onPath ? ACCENT : BORDER);
                    g2.setStroke(new BasicStroke(onPath ? 2.5f : 1f));
                    g2.drawLine(from.x, from.y, to.x, to.y);
                }
            }

            // nodes — radius scales with graph size
            int totalNodes = currentGraph.size();
            int r = Math.max(10, Math.min(22, 400 / Math.max(totalNodes, 1)));

            for (Map.Entry<String, Point> e : pos.entrySet()) {
                String node = e.getKey(); Point pt = e.getValue();
                boolean isVis  = graphVisited.contains(node);
                boolean isCur  = node.equals(graphCurrent);
                boolean isPath = graphPath.contains(node);
                boolean isSt   = node.equals(graphStart);
                boolean isTgt  = node.equals(graphTarget);

                Color fill = isPath ? new Color(30,80,60) : isCur ? new Color(60,45,10)
                           : isVis  ? new Color(20,30,55)  : CARD;
                Color stroke = isPath ? ACCENT2 : isCur ? WARN : isSt ? ACCENT : isTgt ? DANGER : BORDER;

                g2.setColor(fill);
                g2.fillOval(pt.x-r, pt.y-r, r*2, r*2);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(isPath||isCur ? 2.2f : 1f));
                g2.drawOval(pt.x-r, pt.y-r, r*2, r*2);

                // label — only show if radius is big enough
                if (r >= 12) {
                    int fs = Math.max(8, Math.min(12, r - 4));
                    g2.setFont(new Font("JetBrains Mono", Font.BOLD, fs));
                    g2.setColor(isPath ? ACCENT2 : isCur ? WARN : TEXT);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(node, pt.x - fm.stringWidth(node)/2, pt.y + fm.getAscent()/2 - 1);
                }
            }

            drawDot(g2, 20, h-60, ACCENT, "start");
            drawDot(g2, 20, h-40, DANGER, "target");
            drawDot(g2, 20, h-20, ACCENT2,"path");
        }

        // Tree layout — positions nodes in level-order rows
        private Map<String, Point> treeLayout(int w, int h) {
            Map<String, Point> pos = new LinkedHashMap<>();
            if (currentGraph.isEmpty()) return pos;

            // BFS level order
            String root = currentGraph.keySet().iterator().next();
            Map<String, Integer> level = new LinkedHashMap<>();
            Queue<String> q = new LinkedList<>();
            q.add(root); level.put(root, 0);
            int maxLevel = 0;
            while (!q.isEmpty()) {
                String node = q.poll();
                int lv = level.get(node);
                if (lv > maxLevel) maxLevel = lv;
                List<String> ch = currentGraph.getOrDefault(node, new ArrayList<>());
                for (String c : ch) {
                    if (!level.containsKey(c)) { level.put(c, lv+1); q.add(c); }
                }
            }

            // group nodes by level
            Map<Integer, List<String>> byLevel = new LinkedHashMap<>();
            for (Map.Entry<String,Integer> e : level.entrySet()) {
                byLevel.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
            }

            int topPad = 30, botPad = 50;
            int usableH = h - topPad - botPad;
            int levels = maxLevel + 1;

            for (Map.Entry<Integer, List<String>> e : byLevel.entrySet()) {
                int lv = e.getKey();
                List<String> nodes = e.getValue();
                int y = topPad + (levels <= 1 ? usableH/2 : (int)((double)lv/(levels-1)*usableH));
                for (int i = 0; i < nodes.size(); i++) {
                    int x = (int)((i+1.0)/(nodes.size()+1) * w);
                    pos.put(nodes.get(i), new Point(x, y));
                }
            }
            return pos;
        }

        private void drawDot(Graphics2D g2, int x, int y, Color c, String label) {
            g2.setColor(c); g2.fillOval(x, y-5, 10, 10);
            g2.setFont(SMALL); g2.setColor(TEXT_DIM);
            g2.drawString(label, x+16, y+4);
        }

        // ── STRING ────────────────────────────────────────────
        private void drawString(Graphics2D g2) {
            if (textStr.isEmpty()) return;
            int w = getWidth(), h = getHeight();
            int maxLen = Math.max(textStr.length(), patStr.length());
            int cellW  = Math.max(12, Math.min(26, (w-48)/Math.max(maxLen,1)));
            int cellH  = 34, gap = 2, startX = 24;
            int ty     = h/2 - cellH - 20;

            g2.setFont(SMALL); g2.setColor(TEXT_HINT);
            g2.drawString("text", startX, ty-4);

            for (int i = 0; i < textStr.length(); i++) {
                int x = startX + i*(cellW+gap);
                int currentIndex = i;
                if (x+cellW > w-8) break;
                boolean isMatch   = strMatches.stream().anyMatch(m -> currentIndex >= m && currentIndex < m+patStr.length());
                boolean isCurrent = (i == textI);
                Color bg2 = isMatch ? CELL_FOUND : isCurrent ? CELL_CHECK : CELL_DEF;
                Color bc  = isMatch ? ACCENT2    : isCurrent ? WARN       : BORDER;
                g2.setColor(bg2);  g2.fillRoundRect(x, ty, cellW, cellH, 4, 4);
                g2.setColor(bc); g2.setStroke(new BasicStroke(isMatch||isCurrent ? 1.5f : 0.5f));
                g2.drawRoundRect(x, ty, cellW, cellH, 4, 4);
                int fs = Math.max(9, cellW-6);
                g2.setFont(new Font("JetBrains Mono", Font.PLAIN, fs));
                g2.setColor(isMatch ? ACCENT2 : isCurrent ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                String ch = String.valueOf(textStr.charAt(i));
                g2.drawString(ch, x+(cellW-fm.stringWidth(ch))/2, ty+(cellH+fm.getAscent())/2-2);
            }

            if (!patStr.isEmpty() && textI >= 0 && textI < textStr.length()) {
                int py2 = ty+cellH+20;
                g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                g2.drawString("pattern", startX, py2-4);
                int ws = Math.max(0, textI-patJ);
                for (int j = 0; j < patStr.length(); j++) {
                    int x = startX + (ws+j)*(cellW+gap);
                    if (x+cellW > w-8) break;
                    boolean active = (j == patJ);
                    g2.setColor(active ? CELL_CHECK : CARD);
                    g2.fillRoundRect(x, py2, cellW, cellH, 4, 4);
                    g2.setColor(active ? WARN : BORDER);
                    g2.setStroke(new BasicStroke(active ? 1.5f : 0.5f));
                    g2.drawRoundRect(x, py2, cellW, cellH, 4, 4);
                    int fs = Math.max(9, cellW-6);
                    g2.setFont(new Font("JetBrains Mono", Font.PLAIN, fs));
                    g2.setColor(active ? WARN : TEXT_DIM);
                    FontMetrics fm = g2.getFontMetrics();
                    String ch = String.valueOf(patStr.charAt(j));
                    g2.drawString(ch, x+(cellW-fm.stringWidth(ch))/2, py2+(cellH+fm.getAscent())/2-2);
                }
            }
            if (!strMatches.isEmpty()) {
                g2.setFont(SMALL); g2.setColor(ACCENT2);
                g2.drawString(strMatches.size()+" match(es) — positions: "+strMatches, startX, h-48);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════
    private Color blendColor(Color a, Color b, float t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
        );
    }

    private void setCellState(int i, int state) {
        if (cellStates != null && i >= 0 && i < cellStates.length) {
            cellStates[i] = state;
            SwingUtilities.invokeLater(() -> vizPanel.repaint());
        }
    }

    private void markScanned(int from, int to) {
        for (int i = Math.max(0,from); i <= Math.min(to, cellStates.length-1); i++) {
            if (cellStates[i] != 3) cellStates[i] = 1;
        }
        SwingUtilities.invokeLater(() -> vizPanel.repaint());
    }

    private void sleep() throws InterruptedException { Thread.sleep(stepDelay); }
    private boolean matches(Object a, Object b)      { return compareValues(a,b)==0; }

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
            vizPanel.repaint();
        });
    }

    private void resetVisuals() {
        if (running) return;
        if (vizPanel == null) return;
        cellStates = currentArray != null ? new int[currentArray.length] : new int[0];
        cellAnim   = currentArray != null ? new float[currentArray.length] : new float[0];
        comparisons = 0; elapsedNs = 0; resultIndex = -2;
        matchPositions.clear(); visitOrder.clear();
        captionText = ""; captionAlpha = 0f;
        vizPanel.graphVisited.clear(); vizPanel.graphCurrent = null;
        vizPanel.graphPath.clear(); vizPanel.graphPathList = null;
        vizPanel.textI = -1; vizPanel.patJ = -1; vizPanel.strMatches.clear();
        vizPanel.textStr = currentText; vizPanel.patStr = currentPattern;
        vizPanel.mode = isGraphAlgo() ? "graph" : isStringAlgo() ? "string" : "array";
        if (statusLabel != null) statusLabel.setText("ready — press run");
        if (cmpLabel    != null) cmpLabel.setText("comparisons: —");
        if (timeLabel   != null) timeLabel.setText("time: —");
        if (resultLabel != null) resultLabel.setText("result: —");
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

    // ── widget helpers ────────────────────────────────────────
    private JPanel roundPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false); return p;
    }

    private JLabel dimLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(SMALL); l.setForeground(TEXT_DIM); return l;
    }
    private JLabel monoLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(MONO); l.setForeground(TEXT_DIM); return l;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(14,16,21)); f.setForeground(TEXT);
        f.setCaretColor(ACCENT); f.setFont(MONO);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true), new EmptyBorder(4,8,4,8)));
        return f;
    }

    private JSpinner styledSpinner(SpinnerNumberModel model) {
        JSpinner s = new JSpinner(model);
        s.setBackground(new Color(14,16,21));
        s.setPreferredSize(new Dimension(54, 28));
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(new Color(14,16,21)); tf.setForeground(TEXT);
            tf.setFont(MONO); tf.setCaretColor(ACCENT);
            tf.setBorder(new EmptyBorder(2,4,2,4));
        }
        return s;
    }

    private JCheckBox styledCheckbox(String label) {
        JCheckBox cb = new JCheckBox(label);
        cb.setFont(SMALL); cb.setForeground(TEXT_DIM);
        cb.setOpaque(false); cb.setFocusPainted(false);
        return cb;
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
        b.setPreferredSize(new Dimension(label.length() > 4 ? 100 : 90, 34));
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
            setMaximumSize(new Dimension(230, 32));
            setPreferredSize(new Dimension(230, 32));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered=true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered=false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getText().trim().equals(selectedAlgo);
            if (active) {
                g2.setColor(new Color(30,40,70));
                g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 6, 6);
                g2.setColor(ACCENT);
                g2.fillRoundRect(8, 2, 3, getHeight()-4, 2, 2);
            } else if (hovered) {
                g2.setColor(new Color(25,28,38));
                g2.fillRoundRect(8, 2, getWidth()-16, getHeight()-4, 6, 6);
            }
            setForeground(active ? ACCENT : hovered ? TEXT : TEXT_DIM);
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AlgorithmGUI::new);
    }
}