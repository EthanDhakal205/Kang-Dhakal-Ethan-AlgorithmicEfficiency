package com.algoviz.gui;

import com.algoviz.algorithms.searching.*;
import com.algoviz.algorithms.sorting.*;
import com.algoviz.algorithms.sorting.TimSort;
import com.algoviz.models.AlgoResult;
import com.algoviz.models.AlgoStep;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * AlgoVizApp — main JFrame and application controller.
 * Wires all panels together and manages playback state.
 * Entry point: main()
 */
public class AlgoVizApp extends JFrame {

    // PANELS
    private final SidebarPanel sidebar;
    private final TopbarPanel topbar;
    private final ControlsPanel controls;
    private final BarChartPanel barChart;
    private final StatsPanel stats;
    private final PlaybackPanel playback;
    private final InfoPanel info;

    //PLAYBACK STATE
    private List<AlgoStep> steps = new ArrayList<>();
    private int currentStep = 0;
    private boolean playing = false;
    private Timer playTimer;

    // ── ALGORITHM METADATA ────────────────────────────────────────────────
    private record AlgoMeta(String name, String cat, boolean stable, boolean inPlace,
                             String best, String avg, String worst, String space, String desc) {}

    private static final Map<String, AlgoMeta> META = new LinkedHashMap<>();
    static {
        META.put("bubble", new AlgoMeta("Bubble Sort","sort",true,true,"O(n)","O(n²)","O(n²)","O(1)", "Repeatedly compares adjacent elements and swaps if out of order. Optimized with early exit if no swaps occur. The classic first algorithm everyone learns."));
        META.put("insertion", new AlgoMeta("Insertion Sort", "sort", true,  true,  "O(n)",  "O(n²)","O(n²)","O(1)","Builds a sorted region one element at a time. Very fast on small or nearly-sorted arrays. Used as a subroutine inside Tim Sort."));
        META.put("selection",new AlgoMeta("Selection Sort", "sort", false, true,  "O(n²)",  "O(n²)","O(n²)","O(1)","Repeatedly finds the minimum of the unsorted region and places it at the front. Always O(n²). Makes exactly n-1 swaps total."));
        META.put("gnome",new AlgoMeta("Gnome Sort \uD83C\uDF3F","sort",true,  true,  "O(n)", "O(n²)", "O(n²)","O(1)","Named after a Dutch garden gnome sorting flower pots! Walks forward comparing pairs, swaps when out of order and steps back. Equivalent to insertion sort."));
        META.put("bogo",new AlgoMeta("Bogo Sort \uD83D\uDC80","sort", false, true,  "O(n)", "O((n+1)!)",   "O(\u221E)",  "O(1)","Randomly shuffles and checks if sorted. Repeats until done. Average case for 10 elements: ~3.6 million shuffles. Included for educational horror only."));
        META.put("merge",new AlgoMeta("Merge Sort", "sort",true, false, "O(n log n)", "O(n log n)",  "O(n log n)", "O(n)", "Divide-and-conquer: splits in half recursively, sorts each half, merges back. Guarantees O(n log n) in ALL cases. Used for external sorting and linked lists."));
        META.put("quick", new AlgoMeta("Quick Sort", "sort", false, true,  "O(n log n)", "O(n log n)",  "O(n²)", "O(log n)","Selects a pivot and partitions around it. Fastest in practice for average cases due to excellent cache locality. Java uses dual-pivot QuickSort for primitives."));
        META.put("heap",new AlgoMeta("Heap Sort", "sort", false, true,  "O(n log n)", "O(n log n)",  "O(n log n)", "O(1)", "Builds a max-heap then extracts elements in order. Guaranteed O(n log n) with O(1) space. Slower than Quick Sort in practice due to poor cache locality."));
        META.put("tim", new AlgoMeta("Tim Sort \u2B50", "sort", true,  false, "O(n)", "O(n log n)",  "O(n log n)", "O(n)", "The actual algorithm used by Java (Arrays.sort for objects) and Python (list.sort). Hybrid of Merge + Insertion Sort. Exploits natural runs in data for O(n) best case."));
        META.put("radix", new AlgoMeta("Radix Sort", "sort", true,  false, "O(n)",  "O(nk)", "O(nk)", "O(n+k)","Non-comparison sort — processes digits LSD to MSD. Can beat O(n log n) theoretical limit for fixed-width integers. Zero comparisons made."));
        META.put("linear", new AlgoMeta("Linear Search","search", true, true,  "O(1)", "O(n)", "O(n)",  "O(1)", "Checks every element one by one. Works on any array — sorted or not. The most reliable baseline. Optimal for unsorted data."));
        META.put("binary",new AlgoMeta("Binary Search", "search", true, true,  "O(1)", "O(log n)","O(log n)", "O(1)",  "Halves the search space each comparison. 1 million elements needs \u226420 comparisons. 1 billion needs \u226430. Requires sorted array."));
        META.put("jump", new AlgoMeta("Jump Search", "search", true,  true,  "O(1)",  "O(\u221An)","O(\u221An)", "O(1)", "Jumps \u221An elements at a time until overshooting, then linear searches back. Great middle ground between linear and binary search."));
        META.put("interpolation",new AlgoMeta("Interpolation Search", "search", true, true,  "O(1)", "O(log log n)","O(n)",  "O(1)", "Estimates WHERE the target is using value interpolation — like opening a dictionary near 'M' for a word starting with M. O(log log n) for uniform data."));
        META.put("exponential", new AlgoMeta("Exponential Search",   "search", true, true, "O(1)","O(log n)", "O(log n)", "O(1)", "Doubles the search index (1,2,4,8,16...) until overshoot, then binary searches that range. Ideal for unbounded or infinite sorted arrays."));
        META.put("fibonacci", new AlgoMeta("Fibonacci Search \uD83C\uDF00","search",true,true,"O(1)","O(log n)","O(log n)","O(1)","Uses Fibonacci numbers as division points. Only addition and subtraction — no division. Better cache performance on some architectures."));
        META.put("ternary",  new AlgoMeta("Ternary Search", "search", true,  true,  "O(1)", "O(log\u2083n)","O(log\u2083n)","O(1)", "Divides into 3 sections using 2 midpoints. Counterintuitive result: makes MORE comparisons than binary (2 per round vs ~1.5). More splits \u2260 fewer comparisons."));
    }



    public AlgoVizApp() {
        super("AlgoViz — Algorithm Visualizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 750));
        setPreferredSize(new Dimension(1280, 820));
        getContentPane().setBackground(Theme.BG);

        // Build panels
        sidebar = new SidebarPanel(this::onAlgoSelected);
        topbar = new TopbarPanel();
        controls = new ControlsPanel();
        barChart = new BarChartPanel();
        stats = new StatsPanel();
        playback = new PlaybackPanel(new PlaybackPanel.PlaybackListener() {
            @Override public void onFirst()  { goFirst(); }
            @Override public void onPrev() { stepBack(); }
            @Override public void onPlayPause() { togglePlay(); }
            @Override public void onNext() { stepForward(); }
            @Override public void onLast() { goLast(); }
        });
        info = new InfoPanel();

        controls.setListener(new ControlsPanel.ControlListener() {
            @Override public void onRun(String array, int target)  { run(array, target); }
            @Override public void onStep()  { if (steps.isEmpty()) run(controls.getArrayText(), controls.getTarget()); else stepForward(); }
            @Override public void onRandom() { generateRandom(); }
            @Override public void onReset()  { reset(); }
        });

        // Layout
        JScrollPane sidebarScroll = new JScrollPane(sidebar,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebarScroll.setBackground(Theme.BG2);
        sidebarScroll.getViewport().setBackground(Theme.BG2);

        JPanel mainArea = buildMainArea();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebarScroll, mainArea);
        splitPane.setDividerSize(1);
        splitPane.setDividerLocation(Theme.SIDEBAR_W);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);
        splitPane.setBackground(Theme.BG2);

        setContentPane(splitPane);
        setupKeyBindings();

        // Initialize with bubble sort
        updateTopbar("bubble");
        info.update("bubble", "sort", META.get("bubble").desc());

        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel();
        main.setBackground(Theme.BG);
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        // Topbar
        topbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        topbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Theme.TOPBAR_H));
        main.add(topbar);

        // Controls
        controls.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(controls);

        // Bar chart — takes up most space
        barChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel chartWrapper = new JPanel(new BorderLayout());
        chartWrapper.setBackground(Theme.BG);
        chartWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        chartWrapper.add(barChart, BorderLayout.CENTER);
        chartWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(chartWrapper);

        // Stats
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setBackground(Theme.BG);
        statsWrapper.setBorder(BorderFactory.createEmptyBorder(8, 10, 0, 10));
        statsWrapper.add(stats, BorderLayout.CENTER);
        statsWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(statsWrapper);

        // Playback controls
        playback.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(playback);

        // Info panel
        info.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(info);

        return main;
    }

    private void onAlgoSelected(String id, String cat) {
        reset();
        updateTopbar(id);
        controls.setSearchMode(cat.equals("search"));
        AlgoMeta m = META.get(id);
        if (m != null) info.update(id, cat, m.desc());
        // Auto-run with current array
        run(controls.getArrayText(), controls.getTarget());
    }

    private void run(String arrayStr, int target) {
        stopPlay();
        int[] arr = parseArray(arrayStr);
        if (arr == null || arr.length == 0) {
            showError("Invalid array. Use comma-separated integers, e.g. 64,34,25,12");
            return;
        }

        String id  = sidebar.getSelectedId();
        AlgoResult result = dispatch(id, arr, target);
        if (result == null) return;

        steps = result.getSteps();
        currentStep = 0;
        renderStep(0);

        // Start playing automatically
        startPlay();
    }

    private AlgoResult dispatch(String id, int[] arr, int target) {
        return switch (id) {
            case "bubble"-> BubbleSort.sort(arr);
            case "insertion" -> InsertionSort.sort(arr);
            case "selection" -> SelectionSort.sort(arr);
            case "gnome" -> GnomeSort.sort(arr);
            case "bogo" -> BogoSort.sort(arr);
            case "merge"  -> MergeSort.sort(arr);
            case "quick" -> QuickSort.sort(arr);
            case "heap" -> HeapSort.sort(arr);
            case "tim" -> TimSort.sort(arr);
            case "radix" -> RadixSort.sort(arr);
            case "linear" -> LinearSearch.search(arr, target);
            case "binary"-> BinarySearch.search(arr, target);
            case "jump" -> JumpSearch.search(arr, target);
            case "interpolation" -> InterpolationSearch.search(arr, target);
            case "exponential" -> ExponentialSearch.search(arr, target);
            case "fibonacci"-> FibonacciSearch.search(arr, target);
            case "ternary"-> TernarySearch.search(arr, target);
            default -> null;
        };
    }

    // PLAYBACK

    private void startPlay() {
        if (steps.isEmpty()) return;
        if (currentStep >= steps.size() - 1) currentStep = 0;

        playing = true;
        playback.setPlaying(true);

        int[] delayMs = {700, 350, 160, 80, 30};
        int delay = delayMs[Math.min(4, Math.max(0, sidebar.getSpeed() - 1))];

        playTimer = new Timer(delay, e -> {
            if (!playing || steps.isEmpty()) { stopPlay(); return; }
            if (currentStep >= steps.size() - 1) { stopPlay(); return; }
            currentStep++;
            renderStep(currentStep);
        });
        playTimer.start();
    }

    private void stopPlay() {
        playing = false;
        if (playTimer != null) playTimer.stop();
        playback.setPlaying(false);
    }

    private void togglePlay() {
        if (playing) stopPlay();
        else {
            if (steps.isEmpty()) run(controls.getArrayText(), controls.getTarget());
            else startPlay();
        }
    }

    private void stepForward() {
        stopPlay();
        if (steps.isEmpty()) { run(controls.getArrayText(), controls.getTarget()); return; }
        if (currentStep < steps.size() - 1) {
            currentStep++;
            renderStep(currentStep);
        }
    }

    private void stepBack() {
        stopPlay();
        if (currentStep > 0) {
            currentStep--;
            renderStep(currentStep);
        }
    }

    private void goFirst() {
        stopPlay();
        if (!steps.isEmpty()) { currentStep = 0; renderStep(0); }
    }

    private void goLast() {
        stopPlay();
        if (!steps.isEmpty()) { currentStep = steps.size() - 1; renderStep(currentStep); }
    }

    private void renderStep(int idx) {
        if (steps.isEmpty() || idx < 0 || idx >= steps.size()) return;
        AlgoStep step = steps.get(idx);

        SwingUtilities.invokeLater(() -> {
            barChart.setStep(step);
            stats.update(step.getComparisons(), step.getSwaps(), idx, steps.size() - 1, step.getDescription());
        });
    }

    private void reset() {
        stopPlay();
        steps.clear();
        currentStep = 0;
        barChart.reset();
        stats.reset();
    }

    // RANDOM ARRAY 

    private void generateRandom() {
        String id = sidebar.getSelectedId();
        int size = id.equals("bogo") ? 6 : 14;
        Random rng = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(",");
            sb.append(rng.nextInt(95) + 5);
        }
        controls.setArrayText(sb.toString());
        reset();
    }

    // TOPBAR UPDATE 

    private void updateTopbar(String id) {
        AlgoMeta m = META.get(id);
        if (m == null) return;
        topbar.update(m.name(), m.cat(), m.stable(), m.inPlace(),
                m.best(), m.avg(), m.worst(), m.space());
    }

    //KEYBOARD SHORTCUTS 

    private void setupKeyBindings() {
        JComponent root = (JComponent) getContentPane();
        InputMap  im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0),   "playPause");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "stepFwd");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0),  "stepBck");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "first");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "last");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "random");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "run");

        am.put("playPause", action(e -> togglePlay()));
        am.put("stepFwd", action(e -> stepForward()));
        am.put("stepBck", action(e -> stepBack()));
        am.put("first", action(e -> goFirst()));
        am.put("last",action(e -> goLast()));
        am.put("random", action(e -> { generateRandom(); run(controls.getArrayText(), controls.getTarget()); }));
        am.put("run",action(e -> run(controls.getArrayText(), controls.getTarget())));
    }

    private AbstractAction action(java.util.function.Consumer<ActionEvent> handler) {
        return new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { handler.accept(e); }
        };
    }

    // UTILITIES

    private int[] parseArray(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            String[] parts = input.split(",");
            int[] arr = new int[parts.length];
            for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
            return arr;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Invalid Input", JOptionPane.WARNING_MESSAGE);
    }

    // ENTRY POINT 

    public static void main(String[] args) {
        // Apply FlatLaf dark theme as base (we override most painting anyway)
        try {
            UIManager.put("Panel.background",         Theme.BG);
            UIManager.put("OptionPane.background",    Theme.BG2);
            UIManager.put("OptionPane.messageForeground", Theme.TEXT);
            UIManager.put("Button.background",        Theme.SURFACE);
            UIManager.put("Button.foreground",        Theme.TEXT);
            UIManager.put("TextField.background",     Theme.SURFACE);
            UIManager.put("TextField.foreground",     Theme.TEXT);
            UIManager.put("TextField.caretForeground",Theme.ACCENT);
            UIManager.put("SplitPane.background",     Theme.BG2);
            UIManager.put("SplitPane.dividerSize",    1);
        } catch (Exception e) {
            // Fallback - still works without FlatLaf
        }

        SwingUtilities.invokeLater(() -> {
            AlgoVizApp app = new AlgoVizApp();
            app.setVisible(true);
        });
    }
}
