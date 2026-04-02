package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * SidebarPanel — fully custom-painted sidebar navigation.
 * Renders the logo, sorted sections of algorithm buttons, and speed control.
 */
public class SidebarPanel extends JPanel {

    public record AlgoEntry(String id, String name, String category, String badge, Color badgeColor) {}

    private static final List<AlgoEntry> SORT_ALGOS = List.of(
        new AlgoEntry("bubble",    "Bubble Sort",       "sort",   "O(n²)",      Theme.ACCENT3),
        new AlgoEntry("insertion", "Insertion Sort",    "sort",   "O(n²)",      Theme.ACCENT3),
        new AlgoEntry("selection", "Selection Sort",    "sort",   "O(n²)",      Theme.ACCENT3),
        new AlgoEntry("gnome",     "Gnome Sort \uD83C\uDF3F","sort","O(n²)",   Theme.ACCENT3),
        new AlgoEntry("bogo",      "Bogo Sort \uD83D\uDC80","sort","O(\u221E)", Theme.ACCENT2),
        new AlgoEntry("merge",     "Merge Sort",        "sort",   "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("quick",     "Quick Sort",        "sort",   "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("heap",      "Heap Sort",         "sort",   "O(n log n)", Theme.ACCENT5),
        new AlgoEntry("tim",       "Tim Sort \u2B50",   "sort",   "O(n log n)", Theme.ACCENT),
        new AlgoEntry("radix",     "Radix Sort",        "sort",   "O(nk)",      Theme.ACCENT)
    );

    private static final List<AlgoEntry> SEARCH_ALGOS = List.of(
        new AlgoEntry("linear",        "Linear Search",        "search", "O(n)",        Theme.ACCENT3),
        new AlgoEntry("binary",        "Binary Search",        "search", "O(log n)",    Theme.ACCENT5),
        new AlgoEntry("jump",          "Jump Search",          "search", "O(\u221An)",  Theme.ACCENT5),
        new AlgoEntry("interpolation", "Interpolation",        "search", "O(log log n)",Theme.ACCENT),
        new AlgoEntry("exponential",   "Exponential",          "search", "O(log n)",    Theme.ACCENT5),
        new AlgoEntry("fibonacci",     "Fibonacci \uD83C\uDF00","search","O(log n)",   Theme.ACCENT5),
        new AlgoEntry("ternary",       "Ternary Search",       "search", "O(log\u2083n)",Theme.ACCENT5)
    );

    private String selectedId = "bubble";
    private BiConsumer<String, String> onSelect; // (id, category)

    // Hover tracking
    private String hoveredId = null;
    private final List<AlgoEntry> allAlgos = new ArrayList<>();

    // Speed slider
    private int speed = 3; // 1-5
    private boolean draggingSlider = false;
    private final Rectangle sliderTrack = new Rectangle();

    public SidebarPanel(BiConsumer<String, String> onSelect) {
        this.onSelect = onSelect;
        allAlgos.addAll(SORT_ALGOS);
        allAlgos.addAll(SEARCH_ALGOS);

        int contentH = 72 + 28 + (SORT_ALGOS.size() * 34) + 1 + 28 + (SEARCH_ALGOS.size() * 34) + 1 + 70;
        setPreferredSize(new Dimension(Theme.SIDEBAR_W, contentH));
        setMinimumSize(new Dimension(Theme.SIDEBAR_W, 300));
        setBackground(Theme.BG2);

        setupMouseListeners();
    }

    public int getSpeed() { return speed; }
    public String getSelectedId() { return selectedId; }

    public AlgoEntry getSelected() {
        return allAlgos.stream().filter(a -> a.id().equals(selectedId)).findFirst().orElse(null);
    }

    // ── PAINTING ──────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int W = getWidth();
        g2.setColor(Theme.BG2);
        g2.fillRect(0, 0, W, getHeight());

        // Right border
        g2.setColor(Theme.BORDER);
        g2.drawLine(W - 1, 0, W - 1, getHeight());

        int y = 0;
        y = paintLogo(g2, W, y);
        y = paintSection(g2, W, y, "\u2B1B SORTING", SORT_ALGOS);
        y = paintSection(g2, W, y, "\uD83D\uDD0D SEARCHING", SEARCH_ALGOS);
        paintFooter(g2, W, y);

        g2.dispose();
    }

    private int paintLogo(Graphics2D g2, int W, int y) {
        int h = 72;
        g2.setColor(Theme.BG2);
        g2.fillRect(0, y, W, h);

        // Bottom border
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y + h - 1, W, y + h - 1);

        // Bracket left
        g2.setFont(new Font("Dialog", Font.BOLD, 28));
        g2.setColor(Theme.ACCENT);
        g2.drawString("{", 16, y + 44);

        // Logo text
        g2.setFont(new Font("Dialog", Font.BOLD, 18));
        g2.setColor(Theme.TEXT);
        g2.drawString("ALGO", 36, y + 32);
        g2.drawString("VIZ", 36, y + 52);

        // Bracket right
        g2.setFont(new Font("Dialog", Font.BOLD, 28));
        g2.setColor(Theme.ACCENT);
        g2.drawString("}", 90, y + 44);

        // Subtitle
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString("ALGORITHM VISUALIZER", 16, y + 66);

        return y + h;
    }

    private int paintSection(Graphics2D g2, int W, int y, String label, List<AlgoEntry> algos) {
        // Section label
        int labelH = 28;
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString(label, 16, y + 18);
        y += labelH;

        for (AlgoEntry a : algos) {
            y = paintNavItem(g2, W, y, a);
        }

        // Divider
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y, W, y);
        y += 1;
        return y;
    }

    private int paintNavItem(Graphics2D g2, int W, int y, AlgoEntry a) {
        int itemH = 34;
        boolean active = a.id().equals(selectedId);
        boolean hovered = a.id().equals(hoveredId);

        // Active / hover background
        if (active) {
            // Gradient fill for active item
            GradientPaint gp = new GradientPaint(0, y, Theme.withAlpha(Theme.ACCENT, 30),
                    W, y, Theme.withAlpha(Theme.ACCENT, 0));
            g2.setPaint(gp);
            g2.fillRect(0, y, W, itemH);
            g2.setPaint(null);

            // Left accent bar
            g2.setColor(Theme.ACCENT);
            g2.fillRect(0, y, 3, itemH);
        } else if (hovered) {
            g2.setColor(Theme.withAlpha(Color.WHITE, 12));
            g2.fillRect(0, y, W, itemH);
            g2.setColor(Theme.BORDER2);
            g2.fillRect(0, y, 3, itemH);
        }

        // Arrow for active
        g2.setFont(Theme.FONT_NAV);
        if (active) {
            g2.setColor(Theme.ACCENT);
            g2.drawString("\u25B8 " + a.name(), 14, y + 22);
        } else {
            g2.setColor(hovered ? Theme.TEXT : Theme.TEXT2);
            g2.drawString(a.name(), 18, y + 22);
        }

        // Badge (complexity chip)
        g2.setFont(Theme.FONT_BADGE);
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(a.badge()) + 10;
        int bx = W - bw - 10;
        int by = y + (itemH - 16) / 2;

        g2.setColor(Theme.withAlpha(a.badgeColor(), 25));
        g2.fillRoundRect(bx, by, bw, 16, 4, 4);
        g2.setColor(Theme.withAlpha(a.badgeColor(), 120));
        g2.drawRoundRect(bx, by, bw - 1, 15, 4, 4);
        g2.setColor(a.badgeColor());
        g2.drawString(a.badge(), bx + 5, by + 12);

        return y + itemH;
    }

    private void paintFooter(Graphics2D g2, int W, int startY) {
        int footerH = 70;
        int y = startY;

        g2.setColor(Theme.BORDER);
        g2.drawLine(0, y, W, y);

        g2.setColor(Theme.BG2);
        g2.fillRect(0, y, W, footerH);

        // SPEED label
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT2);
        g2.drawString("SPEED", 16, y + 22);

        // Speed value
        g2.setFont(Theme.FONT_MONO_BOLD);
        g2.setColor(Theme.ACCENT);
        g2.drawString(speed + "x", W - 36, y + 22);

        // Slider track
        int tx = 16, tw = W - 52, ty = y + 36;
        int th = 4;
        sliderTrack.setBounds(tx, ty - 6, tw, th + 12); // hit area

        // Track background
        g2.setColor(Theme.BORDER2);
        g2.fillRoundRect(tx, ty, tw, th, 2, 2);

        // Track fill
        int filled = (int)((double)(speed - 1) / 4 * tw);
        Theme.fillGradientH(g2, tx, ty, filled, th, 2, Theme.ACCENT2, Theme.ACCENT);

        // Thumb
        int thumbX = tx + filled - 7;
        int thumbY = ty - 4;
        g2.setColor(Theme.ACCENT);
        g2.fillOval(thumbX, thumbY, 14, 14);
        g2.setColor(Theme.withAlpha(Theme.ACCENT, 60));
        g2.drawOval(thumbX - 2, thumbY - 2, 18, 18);

        // Keyboard hint
        g2.setFont(Theme.FONT_LABEL);
        g2.setColor(Theme.TEXT3);
        g2.drawString("SPACE play  \u2190\u2192 step  R random", 16, y + 62);
    }

    // ── MOUSE INPUT ───────────────────────────────────────────────────────

    private void setupMouseListeners() {
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String prev = hoveredId;
                hoveredId = hitTest(e.getX(), e.getY());
                if (!java.util.Objects.equals(prev, hoveredId)) repaint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingSlider) {
                    updateSlider(e.getX());
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String hit = hitTest(e.getX(), e.getY());
                if (hit != null && !hit.equals(selectedId)) {
                    selectedId = hit;
                    repaint();
                    AlgoEntry entry = allAlgos.stream()
                            .filter(a -> a.id().equals(hit)).findFirst().orElse(null);
                    if (entry != null && onSelect != null) {
                        onSelect.accept(hit, entry.category());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (sliderTrack.contains(e.getPoint())) {
                    draggingSlider = true;
                    updateSlider(e.getX());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                draggingSlider = false;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredId = null;
                repaint();
            }
        });

        // Cursor change
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                String hit = hitTest(e.getX(), e.getY());
                boolean onSlider = sliderTrack.contains(e.getPoint());
                setCursor(Cursor.getPredefinedCursor(
                        (hit != null || onSlider) ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private void updateSlider(int mouseX) {
        int tx = 16, tw = getWidth() - 52;
        double ratio = Math.max(0, Math.min(1, (double)(mouseX - tx) / tw));
        speed = 1 + (int) Math.round(ratio * 4);
        repaint();
    }

    /** Returns algo id under mouse, or null */
    private String hitTest(int mx, int my) {
        // Rebuild layout to find hit
        int y = 72; // logo height
        y += 28; // sorting label

        for (AlgoEntry a : SORT_ALGOS) {
            if (mx >= 0 && mx < getWidth() && my >= y && my < y + 34) return a.id();
            y += 34;
        }
        y += 1; // divider
        y += 28; // searching label

        for (AlgoEntry a : SEARCH_ALGOS) {
            if (mx >= 0 && mx < getWidth() && my >= y && my < y + 34) return a.id();
            y += 34;
        }
        return null;
    }
}
