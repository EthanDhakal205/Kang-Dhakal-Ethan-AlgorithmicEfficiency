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
    private static final Color CELL_CHECK = new Color(60,  45,  15);
    private static final Color CELL_FOUND = new Color(15,  50,  35);

    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font MONO_B = new Font("JetBrains Mono", Font.BOLD,  13);
    private static final Font SANS   = new Font("Segoe UI",       Font.PLAIN, 13);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font TITLE  = new Font("Segoe UI",       Font.BOLD,  22);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN, 11);

    // ── state ─────────────────────────────────────────────────
    private String   selectedAlgo = "Linear Search";
    private int[]    cellStates;
    private int      comparisons  = 0;
    private long     elapsedNs    = 0;
    private int      resultIndex  = -2;
    private boolean  running      = false;
    private int      stepDelay    = 350;
    private List<Integer> matchPositions = new ArrayList<>();
    private List<String>  visitOrder     = new ArrayList<>();

    // ── data ──────────────────────────────────────────────────
    private Object[] currentArray  = {2, 5, 8, 11, 14, 19, 27, 33, 45};
    private Object   currentTarget = 19;
    private boolean  ignoreCase    = false;
    private String   currentText    = "the cat sat on the caterpillar";
    private String   currentPattern = "cat";
    private Map<String, List<String>> currentGraph;
    private String   graphStart  = "A";
    private String   graphTarget = "F";

    // ── swing refs ────────────────────────────────────────────
    private VisualizerPanel vizPanel;
    private JLabel  statusLabel, cmpLabel, timeLabel, resultLabel, algoLabel;
    private JSlider speedSlider;
    private JButton runBtn, resetBtn;
    private JPanel  inputPanel;
    private CardLayout inputCards;

    private static final String[] ARRAY_ALGOS  = {
        "Linear Search","Binary Search","Ternary Search",
        "Jump Search","Interpolation Search","Exponential Search","Fibonacci Search"
    };
    private static final String[] GRAPH_ALGOS  = {"Breadth-First Search","Depth-First Search"};
    private static final String[] STRING_ALGOS = {"KMP Search","Rabin-Karp Search"};

    // ═══════════════════════════════════════════════════════════
    //  INIT
    // ═══════════════════════════════════════════════════════════
    public AlgorithmGUI() {
        buildDefaultGraph();
        setTitle("Search Algorithm Visualizer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
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
        cat.setFont(SMALL);
        cat.setForeground(TEXT_HINT);
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
            new EmptyBorder(14, 24, 14, 24)));

        algoLabel = new JLabel(selectedAlgo);
        algoLabel.setFont(SANS_B);
        algoLabel.setForeground(TEXT);
        bar.add(algoLabel, BorderLayout.WEST);

        JPanel sp = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        sp.setOpaque(false);
        JLabel sl = new JLabel("speed");
        sl.setFont(SMALL); sl.setForeground(TEXT_DIM);
        speedSlider = new JSlider(1, 5, 3);
        speedSlider.setBackground(PANEL);
        speedSlider.setPreferredSize(new Dimension(100, 24));
        speedSlider.addChangeListener(e -> {
            int[] d = {700, 450, 280, 130, 50};
            stepDelay = d[speedSlider.getValue() - 1];
        });
        sp.add(sl); sp.add(speedSlider);
        bar.add(sp, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(20, 24, 0, 24));

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

    // ── INPUT PANELS — live update via DocumentListener ───────
    private JPanel buildArrayInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JTextField arrField = styledField(22);
        arrField.setText("2, 5, 8, 11, 14, 19, 27, 33, 45");
        JTextField tgtField = styledField(6);
        tgtField.setText("19");
        JCheckBox icBox = new JCheckBox("ignore case");
        icBox.setFont(SMALL); icBox.setForeground(TEXT_DIM);
        icBox.setOpaque(false); icBox.setFocusPainted(false);

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

        JTextField sField = styledField(4); sField.setText("A");
        JTextField tField = styledField(4); tField.setText("F");
        JLabel info = new JLabel("graph: A->B,C  B->D,E  C->F  D->null  E->F  F->null");
        info.setFont(SMALL); info.setForeground(TEXT_HINT);

        Runnable apply = () -> {
            graphStart  = sField.getText().trim().toUpperCase();
            graphTarget = tField.getText().trim().toUpperCase();
            resetVisuals();
        };
        addLiveListener(sField, apply);
        addLiveListener(tField, apply);

        p.add(dimLabel("start:")); p.add(sField);
        p.add(dimLabel("target:")); p.add(tField);
        p.add(info);
        return p;
    }

    private JPanel buildStringInput() {
        JPanel p = roundPanel();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        JTextField txtField = styledField(28); txtField.setText("the cat sat on the caterpillar");
        JTextField patField = styledField(10); patField.setText("cat");
        JCheckBox icBox = new JCheckBox("ignore case");
        icBox.setFont(SMALL); icBox.setForeground(TEXT_DIM);
        icBox.setOpaque(false); icBox.setFocusPainted(false);

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
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                running = false;
                SwingUtilities.invokeLater(() -> runBtn.setEnabled(true));
            }
        }).start();
    }

    private void runArrayAlgo() throws InterruptedException {
        cellStates = new int[currentArray.length];
        long t0 = System.nanoTime();
        switch (selectedAlgo) {
            case "Linear Search"        -> runLinear();
            case "Binary Search"        -> runBinary();
            case "Ternary Search"       -> runTernary();
            case "Jump Search"          -> runJump();
            case "Interpolation Search" -> runInterpolation();
            case "Exponential Search"   -> runExponential();
            case "Fibonacci Search"     -> runFibonacci();
        }
        elapsedNs = System.nanoTime() - t0;
        updateStats();
    }

    private void runLinear() throws InterruptedException {
        for (int i = 0; i < currentArray.length; i++) {
            setCellState(i, 2); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) {
                setCellState(i, 3); resultIndex = i; setStatus("found at index " + i); return;
            }
            setCellState(i, 1);
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runBinary() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            setCellState(mid, 2); sleep(); comparisons++;
            int c = compareValues(currentArray[mid], currentTarget);
            if (c == 0) { setCellState(mid, 3); resultIndex = mid; setStatus("found at index " + mid); return; }
            setCellState(mid, 1);
            if (c < 0) { markScanned(low, mid - 1); low = mid + 1; }
            else        { markScanned(mid + 1, high); high = mid - 1; }
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runTernary() throws InterruptedException {
        int low = 0, high = currentArray.length - 1;
        while (low <= high) {
            int t = (high - low) / 3;
            int m1 = low + t, m2 = high - t;
            setCellState(m1, 2); setCellState(m2, 2); sleep(); comparisons += 2;
            int c1 = compareValues(currentArray[m1], currentTarget);
            int c2 = compareValues(currentArray[m2], currentTarget);
            if (c1 == 0) { setCellState(m1, 3); resultIndex = m1; setStatus("found at index " + m1); return; }
            if (c2 == 0) { setCellState(m2, 3); resultIndex = m2; setStatus("found at index " + m2); return; }
            setCellState(m1, 1); setCellState(m2, 1);
            if      (compareValues(currentTarget, currentArray[m1]) < 0) { markScanned(m1 + 1, high); high = m1 - 1; }
            else if (compareValues(currentTarget, currentArray[m2]) > 0) { markScanned(low, m2 - 1);  low  = m2 + 1; }
            else { markScanned(low, m1 - 1); markScanned(m2 + 1, high); low = m1 + 1; high = m2 - 1; }
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runJump() throws InterruptedException {
        int n = currentArray.length;
        int step = (int) Math.floor(Math.sqrt(n));
        int prev = 0, curr = step;
        while (curr < n && compareValues(currentArray[curr], currentTarget) <= 0) {
            setCellState(curr, 2); sleep(); comparisons++;
            setCellState(curr, 1);
            prev = curr; curr += step;
        }
        for (int i = prev; i < Math.min(curr, n); i++) {
            setCellState(i, 2); sleep(); comparisons++;
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
            setCellState(pos, 2); sleep(); comparisons++;
            int c = compareValues(currentArray[pos], currentTarget);
            if (c == 0) { setCellState(pos, 3); resultIndex = pos; setStatus("found at index " + pos); return; }
            setCellState(pos, 1);
            if (c < 0) low = pos + 1; else high = pos - 1;
        }
        resultIndex = -1; setStatus("not found");
    }

    private void runExponential() throws InterruptedException {
        int n = currentArray.length;
        setCellState(0, 2); sleep(); comparisons++;
        if (matches(currentArray[0], currentTarget)) { setCellState(0, 3); resultIndex = 0; setStatus("found at index 0"); return; }
        setCellState(0, 1);
        int bound = 1;
        while (bound < n && compareValues(currentArray[bound], currentTarget) <= 0) {
            setCellState(bound, 2); sleep(); comparisons++;
            setCellState(bound, 1);
            bound *= 2;
        }
        int low = bound / 2, high = Math.min(bound, n - 1);
        while (low <= high) {
            int mid = (low + high) / 2;
            setCellState(mid, 2); sleep(); comparisons++;
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
            setCellState(i, 2); sleep(); comparisons++;
            int c = compareValues(currentArray[i], currentTarget);
            if (c < 0)      { fib = fm1; fm1 = fm2; fm2 = fib - fm1; offset = i; setCellState(i, 1); }
            else if (c > 0) { fib = fm2; fm1 -= fm2; fm2 = fib - fm1; setCellState(i, 1); }
            else            { setCellState(i, 3); resultIndex = i; setStatus("found at index " + i); return; }
        }
        if (fm1 == 1 && offset + 1 < n) {
            int i = offset + 1;
            setCellState(i, 2); sleep(); comparisons++;
            if (matches(currentArray[i], currentTarget)) { setCellState(i, 3); resultIndex = i; setStatus("found at index " + i); return; }
            setCellState(i, 1);
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
            vizPanel.setGraphState(visited, node, Collections.emptySet(), null); sleep();
            if (node.equals(graphTarget)) {
                List<String> path = new ArrayList<>();
                String cur = node;
                while (cur != null) { path.add(0, cur); cur = parent.get(cur); }
                vizPanel.setGraphState(visited, null, new HashSet<>(path), path);
                resultIndex = visitOrder.size() - 1;
                setStatus("found '" + graphTarget + "' — path: " + path);
                found = true; break;
            }
            List<String> neighbors = new ArrayList<>(currentGraph.getOrDefault(node, new ArrayList<>()));
            if (!isBFS) Collections.reverse(neighbors);
            for (String nb : neighbors) {
                if (!visited.contains(nb)) {
                    if (!parent.containsKey(nb)) parent.put(nb, node);
                    if (isBFS) queue.add(nb); else stack.push(nb);
                }
            }
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

        if (p.isEmpty() || n == 0) {
            resultIndex = -1; setStatus("text or pattern is empty"); updateStats(); return;
        }
        if (m > n) {
            resultIndex = -1; setStatus("pattern longer than text"); updateStats(); return;
        }

        long t0 = System.nanoTime();
        if (selectedAlgo.equals("KMP Search")) runKMP(t, p, n, m);
        else                                    runRabinKarp(t, p, n, m);

        elapsedNs = System.nanoTime() - t0;
        vizPanel.setStringState(t, p, -1, -1, matchPositions);
        resultIndex = matchPositions.isEmpty() ? -1 : matchPositions.get(0);
        setStatus(matchPositions.isEmpty()
            ? "no matches found"
            : matchPositions.size() + " match(es) at positions " + matchPositions);
        updateStats();
    }

    private void runKMP(String t, String p, int n, int m) throws InterruptedException {
        int[] lps = buildLPS(p);
        int i = 0, j = 0;
        while (i < n) {
            comparisons++;
            vizPanel.setStringState(t, p, i, j, new ArrayList<>(matchPositions));
            sleep();
            if (t.charAt(i) == p.charAt(j)) { i++; j++; }
            if (j == m) {
                matchPositions.add(i - j);
                j = lps[j - 1];
            } else if (i < n && t.charAt(i) != p.charAt(j)) {
                j = (j > 0) ? lps[j - 1] : 0;
                if (j == 0) i++;
            }
        }
    }

    private void runRabinKarp(String t, String p, int n, int m) throws InterruptedException {
        // BASE 257 is safe for all printable ASCII and handles spaces, punctuation, etc.
        final long BASE = 257L;
        final long MOD  = 1_000_000_007L;

        long ph = 0, wh = 0, pw = 1;
        for (int i = 0; i < m; i++) {
            ph = (ph * BASE + p.charAt(i)) % MOD;
            wh = (wh * BASE + t.charAt(i)) % MOD;
            if (i > 0) pw = (pw * BASE) % MOD;
        }

        comparisons++;
        if (wh == ph && t.substring(0, m).equals(p)) matchPositions.add(0);
        vizPanel.setStringState(t, p, 0, 0, new ArrayList<>(matchPositions));
        sleep();

        for (int i = 1; i <= n - m; i++) {
            wh = (wh - t.charAt(i - 1) * pw % MOD + MOD) % MOD;
            wh = (wh * BASE + t.charAt(i + m - 1)) % MOD;
            comparisons++;
            vizPanel.setStringState(t, p, i, 0, new ArrayList<>(matchPositions));
            sleep();
            if (wh == ph && t.substring(i, i + m).equals(p)) matchPositions.add(i);
        }
    }

    private int[] buildLPS(String p) {
        int m = p.length();
        int[] lps = new int[m];
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

        VisualizerPanel() {
            setBackground(BG);
            setPreferredSize(new Dimension(800, 400));
        }

        void setStringState(String t, String p, int ti, int pj, List<Integer> matches) {
            this.textStr   = t; this.patStr = p;
            this.textI     = ti; this.patJ  = pj;
            this.strMatches = new ArrayList<>(matches);
            this.mode = "string";
            SwingUtilities.invokeLater(this::repaint);
        }

        void setGraphState(Set<String> visited, String current, Set<String> path, List<String> pathList) {
            this.graphVisited  = new HashSet<>(visited);
            this.graphCurrent  = current;
            this.graphPath     = new HashSet<>(path);
            this.graphPathList = pathList;
            this.mode = "graph";
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
        }

        // ── BAR CHART ARRAY ───────────────────────────────────
        private void drawArray(Graphics2D g2) {
            if (currentArray == null || currentArray.length == 0) return;
            int n  = currentArray.length;
            int w  = getWidth(), h = getHeight();

            int cellW  = Math.min(72, (w - 48) / n);
            int gap    = 6;
            int totalW = n * cellW + (n - 1) * gap;
            int startX = (w - totalW) / 2;
            int baseY  = h - 40;
            int topPad = 50; // space above tallest bar for value label + arrow
            int barArea = baseY - topPad;

            // compute min/max for scaling
            double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
            boolean allNumeric = true;
            for (Object o : currentArray) {
                if (o instanceof Number) {
                    double v = ((Number) o).doubleValue();
                    if (v < minVal) minVal = v;
                    if (v > maxVal) maxVal = v;
                } else { allNumeric = false; }
            }
            double range = (maxVal == minVal) ? 1.0 : maxVal - minVal;
            int minBarH = 32;

            for (int i = 0; i < n; i++) {
                int x     = startX + i * (cellW + gap);
                int state = (cellStates != null && i < cellStates.length) ? cellStates[i] : 0;

                int barH;
                if (allNumeric) {
                    double v   = ((Number) currentArray[i]).doubleValue();
                    double pct = (v - minVal) / range;
                    barH = minBarH + (int)(pct * (barArea - minBarH));
                } else {
                    barH = barArea / 2;
                }
                int barY = baseY - barH;

                Color bg2 = switch (state) {
                    case 1 -> CELL_SCAN;
                    case 2 -> CELL_CHECK;
                    case 3 -> CELL_FOUND;
                    default -> CELL_DEF;
                };
                Color bc = switch (state) {
                    case 2 -> WARN;
                    case 3 -> ACCENT2;
                    default -> BORDER;
                };

                g2.setColor(bg2);
                g2.fillRoundRect(x, barY, cellW, barH, 6, 6);
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(state > 1 ? 1.5f : 0.5f));
                g2.drawRoundRect(x, barY, cellW, barH, 6, 6);

                // value label — inside bar if tall enough, above it if short
                String val = String.valueOf(currentArray[i]);
                g2.setFont(MONO_B);
                g2.setColor(state == 3 ? ACCENT2 : state == 2 ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (cellW - fm.stringWidth(val)) / 2;
                int ty = (barH > fm.getAscent() + 8) ? barY + fm.getAscent() + 4 : barY - 4;
                g2.drawString(val, tx, ty);

                // index below baseline
                g2.setFont(SMALL);
                g2.setColor(TEXT_HINT);
                String idx = String.valueOf(i);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(idx, x + (cellW - fm2.stringWidth(idx)) / 2, baseY + 18);
            }

            // pointer arrow above active bar top
            if (cellStates != null) {
                for (int i = 0; i < cellStates.length; i++) {
                    if (cellStates[i] == 2 || cellStates[i] == 3) {
                        int barH;
                        if (allNumeric && currentArray[i] instanceof Number) {
                            double v   = ((Number) currentArray[i]).doubleValue();
                            double pct = (v - minVal) / range;
                            barH = minBarH + (int)(pct * (barArea - minBarH));
                        } else { barH = barArea / 2; }
                        int barY = baseY - barH;
                        int cx   = startX + i * (cellW + gap) + cellW / 2;
                        g2.setColor(cellStates[i] == 3 ? ACCENT2 : WARN);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawLine(cx, barY - 6, cx, barY - 20);
                        int[] px = {cx - 5, cx + 5, cx};
                        int[] py = {barY - 18, barY - 18, barY - 6};
                        g2.fillPolygon(px, py, 3);
                    }
                }
            }
        }

        // ── GRAPH ─────────────────────────────────────────────
        private void drawGraph(Graphics2D g2) {
            int w = getWidth(), h = getHeight();
            Map<String, Point> pos = graphLayout(w, h);

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

            int r = 24;
            for (Map.Entry<String, Point> e : pos.entrySet()) {
                String node = e.getKey();
                Point  pt   = e.getValue();
                boolean isVisited = graphVisited.contains(node);
                boolean isCurrent = node.equals(graphCurrent);
                boolean isOnPath  = graphPath.contains(node);
                boolean isStart   = node.equals(graphStart);
                boolean isTarget  = node.equals(graphTarget);

                Color fill = isOnPath  ? new Color(30, 80, 60)
                           : isCurrent ? new Color(60, 45, 10)
                           : isVisited ? new Color(20, 30, 55)
                           : CARD;
                Color stroke = isOnPath  ? ACCENT2
                             : isCurrent ? WARN
                             : isStart   ? ACCENT
                             : isTarget  ? DANGER
                             : BORDER;

                g2.setColor(fill);
                g2.fillOval(pt.x - r, pt.y - r, r * 2, r * 2);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(isOnPath || isCurrent ? 2.5f : 1.2f));
                g2.drawOval(pt.x - r, pt.y - r, r * 2, r * 2);

                g2.setFont(MONO_B);
                g2.setColor(isOnPath ? ACCENT2 : isCurrent ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(node, pt.x - fm.stringWidth(node) / 2,
                              pt.y + fm.getAscent() / 2 - 2);
            }

            drawDot(g2, 20, h - 60, ACCENT,  "start");
            drawDot(g2, 20, h - 40, DANGER,  "target");
            drawDot(g2, 20, h - 20, ACCENT2, "path");
        }

        private void drawDot(Graphics2D g2, int x, int y, Color c, String label) {
            g2.setColor(c); g2.fillOval(x, y - 5, 10, 10);
            g2.setFont(SMALL); g2.setColor(TEXT_DIM);
            g2.drawString(label, x + 16, y + 4);
        }

        private Map<String, Point> graphLayout(int w, int h) {
            List<String> nodes = new ArrayList<>(currentGraph.keySet());
            Map<String, Point> pos = new LinkedHashMap<>();
            int cx = w / 2, cy = h / 2;
            int rad = Math.min(w, h) / 2 - 60;
            for (int i = 0; i < nodes.size(); i++) {
                double angle = 2 * Math.PI * i / nodes.size() - Math.PI / 2;
                pos.put(nodes.get(i), new Point(
                    cx + (int)(rad * Math.cos(angle)),
                    cy + (int)(rad * Math.sin(angle))
                ));
            }
            return pos;
        }

        // ── STRING ────────────────────────────────────────────
        private void drawString(Graphics2D g2) {
            if (textStr.isEmpty()) return;
            int w = getWidth(), h = getHeight();

            // shrink cells to fit long strings
            int maxLen = Math.max(textStr.length(), patStr.length());
            int cellW  = Math.max(12, Math.min(26, (w - 48) / Math.max(maxLen, 1)));
            int cellH  = 34;
            int gap    = 2;
            int startX = 24;

            // text row
            int ty = h / 2 - cellH - 20;
            g2.setFont(SMALL); g2.setColor(TEXT_HINT);
            g2.drawString("text", startX, ty - 4);

            for (int i = 0; i < textStr.length(); i++) {
                int currentIndex = i;
                int x = startX + i * (cellW + gap);
                if (x + cellW > w - 8) break;

                boolean isMatch   = strMatches.stream().anyMatch(m -> currentIndex >= m && currentIndex < m + patStr.length());
                boolean isCurrent = (i == textI);

                Color bg2 = isMatch ? CELL_FOUND : isCurrent ? CELL_CHECK : CELL_DEF;
                Color bc  = isMatch ? ACCENT2    : isCurrent ? WARN       : BORDER;

                g2.setColor(bg2);
                g2.fillRoundRect(x, ty, cellW, cellH, 4, 4);
                g2.setColor(bc);
                g2.setStroke(new BasicStroke(isMatch || isCurrent ? 1.5f : 0.5f));
                g2.drawRoundRect(x, ty, cellW, cellH, 4, 4);

                int fs = Math.max(9, cellW - 6);
                g2.setFont(new Font("JetBrains Mono", Font.PLAIN, fs));
                g2.setColor(isMatch ? ACCENT2 : isCurrent ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                String ch = String.valueOf(textStr.charAt(i));
                g2.drawString(ch, x + (cellW - fm.stringWidth(ch)) / 2,
                              ty + (cellH + fm.getAscent()) / 2 - 2);
            }

            // pattern row — aligned to current window position
            if (!patStr.isEmpty() && textI >= 0 && textI < textStr.length()) {
                int py2 = ty + cellH + 20;
                g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                g2.drawString("pattern", startX, py2 - 4);

                int windowStart = Math.max(0, textI - patJ);

                for (int j = 0; j < patStr.length(); j++) {
                    int x = startX + (windowStart + j) * (cellW + gap);
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
                    g2.drawString(ch, x + (cellW - fm.stringWidth(ch)) / 2,
                                  py2 + (cellH + fm.getAscent()) / 2 - 2);
                }
            }

            if (!strMatches.isEmpty()) {
                g2.setFont(SMALL); g2.setColor(ACCENT2);
                g2.drawString(strMatches.size() + " match(es) — positions: " + strMatches, startX, h - 16);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════
    private void setCellState(int i, int state) {
        if (cellStates != null && i >= 0 && i < cellStates.length) {
            cellStates[i] = state;
            SwingUtilities.invokeLater(() -> vizPanel.repaint());
        }
    }

    private void markScanned(int from, int to) {
        for (int i = Math.max(0, from); i <= Math.min(to, cellStates.length - 1); i++) {
            if (cellStates[i] != 3) cellStates[i] = 1;
        }
        SwingUtilities.invokeLater(() -> vizPanel.repaint());
    }

    private void sleep() throws InterruptedException { Thread.sleep(stepDelay); }
    private boolean matches(Object a, Object b) { return compareValues(a, b) == 0; }

    private int compareValues(Object a, Object b) {
        if (a instanceof Number && b instanceof Number)
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        if (a instanceof String && b instanceof String)
            return ignoreCase ? ((String) a).compareToIgnoreCase((String) b)
                              : ((String) a).compareTo((String) b);
        return String.valueOf(a).compareTo(String.valueOf(b));
    }

    private void setStatus(String msg) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(msg));
    }

    private void updateStats() {
        SwingUtilities.invokeLater(() -> {
            cmpLabel.setText("comparisons: " + comparisons);
            timeLabel.setText("time: " + (elapsedNs > 0 ? elapsedNs + " ns" : "—"));
            resultLabel.setText(resultIndex == -1 ? "result: not found"
                              : resultIndex >= 0  ? "result: index " + resultIndex
                              : "result: —");
            vizPanel.repaint();
        });
    }

    private void resetVisuals() {
        if (running) return;
        cellStates = currentArray != null ? new int[currentArray.length] : new int[0];
        comparisons = 0; elapsedNs = 0; resultIndex = -2;
        matchPositions.clear(); visitOrder.clear();
        if (vizPanel == null) return;
        vizPanel.graphVisited.clear(); vizPanel.graphCurrent = null;
        vizPanel.graphPath.clear();   vizPanel.graphPathList = null;
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

    private void buildDefaultGraph() {
        currentGraph = new LinkedHashMap<>();
        currentGraph.put("A", Arrays.asList("B", "C"));
        currentGraph.put("B", Arrays.asList("A", "D", "E"));
        currentGraph.put("C", Arrays.asList("A", "F"));
        currentGraph.put("D", Arrays.asList("B"));
        currentGraph.put("E", Arrays.asList("B", "F"));
        currentGraph.put("F", Arrays.asList("C", "E"));
    }

    // ── widget helpers ────────────────────────────────────────
    private JPanel roundPanel() {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
        };
        p.setOpaque(false);
        return p;
    }

    private JLabel dimLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL); l.setForeground(TEXT_DIM); return l;
    }

    private JLabel monoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(MONO); l.setForeground(TEXT_DIM); return l;
    }

    private JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setBackground(new Color(14, 16, 21));
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setFont(MONO);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER, 1, true),
            new EmptyBorder(4, 8, 4, 8)));
        return f;
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
        b.setFont(SANS_B);
        b.setForeground(primary ? Color.WHITE : TEXT_DIM);
        b.setOpaque(false); b.setContentAreaFilled(false);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(primary ? 90 : 70, 34));
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
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
            });
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean active = getText().trim().equals(selectedAlgo);
            if (active) {
                g2.setColor(new Color(30, 40, 70));
                g2.fillRoundRect(8, 2, getWidth() - 16, getHeight() - 4, 6, 6);
                g2.setColor(ACCENT);
                g2.fillRoundRect(8, 2, 3, getHeight() - 4, 2, 2);
            } else if (hovered) {
                g2.setColor(new Color(25, 28, 38));
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