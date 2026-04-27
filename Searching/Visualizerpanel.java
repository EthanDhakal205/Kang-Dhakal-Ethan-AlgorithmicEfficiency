import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

class VisualizerPanel extends JPanel {

    // ── Dark palette ──────────────────────────────────────────────────────────
    private static final Color[] DARK = {
        new Color(10,  11,  14),   // 0  BG
        new Color(24,  27,  34),   // 1  CARD
        new Color(38,  42,  54),   // 2  BORDER
        new Color(82,  130, 255),  // 3  ACCENT
        new Color(52,  211, 153),  // 4  ACCENT2
        new Color(251, 191, 36),   // 5  WARN
        new Color(239, 68,  68),   // 6  DANGER
        new Color(220, 225, 235),  // 7  TEXT
        new Color(100, 110, 130),  // 8  TEXT_DIM
        new Color(55,  62,  78),   // 9  TEXT_HINT
        new Color(30,  34,  44),   // 10 CELL_DEF
        new Color(22,  35,  55),   // 11 CELL_SCAN
        new Color(65,  48,  12),   // 12 CELL_CHECK
        new Color(12,  54,  36),   // 13 CELL_FOUND
        new Color(14,  18,  28),   // 14 CAPTION_BG
        new Color(18,  42,  78),   // 15 CELL_PIVOT
        new Color(16,  60,  42),   // 16 CELL_SWAP
        new Color(22,  36,  68),   // 17 CELL_MERGE
        new Color(28,  34,  50),   // 18 CELL_ACTIVE
        new Color(22,  68,  50),   // 19 GRAPH_PATH_FILL
        new Color(55,  40,  8),    // 20 GRAPH_CURRENT_FILL
        new Color(18,  28,  52),   // 21 GRAPH_VISITED_FILL
        new Color(80,  30,  10),   // 22 STR_WIN_MISMATCH
        new Color(20,  40,  80),   // 23 STR_WIN_NORMAL
        new Color(12,  60,  38),   // 24 STR_MATCH_UNDERLINE
        new Color(12,  65,  42),   // 25 STR_MATCH_FILL
        new Color(80,  25,  10),   // 26 STR_MISMATCH_FILL
        new Color(25,  38,  68),   // 27 STR_IN_WINDOW_FILL
        new Color(160, 190, 255),  // 28 STR_IN_WINDOW_TEXT
    };

    // ── Light palette ─────────────────────────────────────────────────────────
    private static final Color[] LIGHT = {
        new Color(245, 246, 250),  // 0  BG
        new Color(215, 218, 230),  // 1  CARD
        new Color(180, 185, 205),  // 2  BORDER
        new Color(50,  100, 220),  // 3  ACCENT
        new Color(15,  160, 100),  // 4  ACCENT2
        new Color(200, 140, 10),   // 5  WARN
        new Color(200, 50,  50),   // 6  DANGER
        new Color(15,  18,  35),   // 7  TEXT
        new Color(80,  88,  110),  // 8  TEXT_DIM
        new Color(160, 168, 190),  // 9  TEXT_HINT
        new Color(210, 213, 225),  // 10 CELL_DEF
        new Color(200, 215, 240),  // 11 CELL_SCAN
        new Color(245, 225, 180),  // 12 CELL_CHECK
        new Color(195, 240, 215),  // 13 CELL_FOUND
        new Color(240, 242, 250),  // 14 CAPTION_BG
        new Color(210, 225, 255),  // 15 CELL_PIVOT
        new Color(205, 245, 225),  // 16 CELL_SWAP
        new Color(215, 225, 250),  // 17 CELL_MERGE
        new Color(220, 224, 240),  // 18 CELL_ACTIVE
        new Color(195, 240, 215),  // 19 GRAPH_PATH_FILL
        new Color(250, 235, 195),  // 20 GRAPH_CURRENT_FILL
        new Color(210, 220, 248),  // 21 GRAPH_VISITED_FILL
        new Color(255, 215, 200),  // 22 STR_WIN_MISMATCH
        new Color(210, 220, 248),  // 23 STR_WIN_NORMAL
        new Color(15,  160, 100),  // 24 STR_MATCH_UNDERLINE
        new Color(200, 245, 220),  // 25 STR_MATCH_FILL
        new Color(255, 210, 205),  // 26 STR_MISMATCH_FILL
        new Color(215, 225, 250),  // 27 STR_IN_WINDOW_FILL
        new Color(50,  100, 220),  // 28 STR_IN_WINDOW_TEXT
    };

    // ── Live color references — swapped by applyTheme() ───────────────────────
    private static Color BG, CARD, BORDER, ACCENT, ACCENT2, WARN, DANGER,
                         TEXT, TEXT_DIM, TEXT_HINT,
                         CELL_DEF, CELL_SCAN, CELL_CHECK, CELL_FOUND,
                         CAPTION_BG, CELL_PIVOT, CELL_SWAP, CELL_MERGE, CELL_ACTIVE,
                         GRAPH_PATH_FILL, GRAPH_CURRENT_FILL, GRAPH_VISITED_FILL,
                         STR_WIN_MISMATCH, STR_WIN_NORMAL, STR_MATCH_UNDERLINE,
                         STR_MATCH_FILL, STR_MISMATCH_FILL, STR_IN_WINDOW_FILL,
                         STR_IN_WINDOW_TEXT;

    private static final Font MONO     = new Font("JetBrains Mono", Font.PLAIN, 13);
    private static final Font MONO_B   = new Font("JetBrains Mono", Font.BOLD,  13);
    private static final Font MONO_L   = new Font("JetBrains Mono", Font.PLAIN, 18);
    private static final Font SANS_B   = new Font("Segoe UI",       Font.BOLD,  14);
    private static final Font SMALL    = new Font("Segoe UI",       Font.PLAIN, 11);
    private static final Font CAPTION_FONT = new Font("Segoe UI",   Font.ITALIC, 13);

    private final AnimState state;
    private JScrollPane scrollParent;

    VisualizerPanel(AnimState state) {
        this.state = state;
        applyTheme(true); // dark by default
        setBackground(BG);
    }

    // ── Theme API ─────────────────────────────────────────────────────────────

    void applyTheme(boolean darkMode) {
        Color[] t = darkMode ? DARK : LIGHT;
        BG                  = t[0];
        CARD                = t[1];
        BORDER              = t[2];
        ACCENT              = t[3];
        ACCENT2             = t[4];
        WARN                = t[5];
        DANGER              = t[6];
        TEXT                = t[7];
        TEXT_DIM            = t[8];
        TEXT_HINT           = t[9];
        CELL_DEF            = t[10];
        CELL_SCAN           = t[11];
        CELL_CHECK          = t[12];
        CELL_FOUND          = t[13];
        CAPTION_BG          = t[14];
        CELL_PIVOT          = t[15];
        CELL_SWAP           = t[16];
        CELL_MERGE          = t[17];
        CELL_ACTIVE         = t[18];
        GRAPH_PATH_FILL     = t[19];
        GRAPH_CURRENT_FILL  = t[20];
        GRAPH_VISITED_FILL  = t[21];
        STR_WIN_MISMATCH    = t[22];
        STR_WIN_NORMAL      = t[23];
        STR_MATCH_UNDERLINE = t[24];
        STR_MATCH_FILL      = t[25];
        STR_MISMATCH_FILL   = t[26];
        STR_IN_WINDOW_FILL  = t[27];
        STR_IN_WINDOW_TEXT  = t[28];
        setBackground(BG);
        repaint();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    void setScrollParent(JScrollPane scrollParent) {
        this.scrollParent = scrollParent;
    }

    @Override
    public Dimension getPreferredSize() {
        if (!state.usesArrayVisualizer() || state.currentArray == null || state.currentArray.length == 0) {
            return new Dimension(800, 430);
        }
        int width = scrollParent != null ? scrollParent.getViewport().getWidth() : 800;
        if (width <= 0) width = 800;
        int perRow = Math.max(1, (width - 48 + 6) / (56 + 6));
        int numRows = (state.currentArray.length + perRow - 1) / perRow;
        return new Dimension(width, Math.max(430, numRows * 160 + 80));
    }

    // ── Paint dispatch ────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        if      (state.isGraphAlgo())  drawGraph(g2);
        else if (state.isStringAlgo()) drawString(g2);
        else if (state.isSortAlgo())   drawSortArray(g2);
        else                           drawSearchArray(g2);

        if (state.showCaptions && state.captionPhase != 0) drawCaption(g2);
    }

    // ── Caption ───────────────────────────────────────────────────────────────

    private void drawCaption(Graphics2D g2) {
        if (state.captionText.isEmpty()) return;

        Rectangle visible = getVisibleRect();
        int width = getWidth();
        int visibleHeight = visible.height > 0 ? visible.height : getHeight();

        g2.setFont(CAPTION_FONT);
        FontMetrics metrics = g2.getFontMetrics();
        int textWidth = metrics.stringWidth(state.captionText);
        int padding = 14;

        float alpha, slideY;
        if (state.captionPhase == 1) {
            float ease = easeOut(state.captionProgress);
            alpha  = ease;
            slideY = 18f * (1f - ease);
        } else if (state.captionPhase == 2) {
            alpha  = 1f;
            slideY = 0f;
        } else {
            alpha  = 1f - easeIn(state.captionProgress);
            slideY = 0f;
        }

        float clampedAlpha = Math.max(0f, Math.min(1f, alpha));
        if (clampedAlpha < 0.02f) return;

        int textY = (int) (visible.y + visibleHeight - 24 + slideY);
        int textX = (width - textWidth) / 2;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clampedAlpha));
        g2.setColor(CAPTION_BG);
        g2.fillRoundRect(textX - padding, textY - metrics.getAscent() - 6,
            textWidth + padding * 2, metrics.getHeight() + 12, 12, 12);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRoundRect(textX - padding, textY - metrics.getAscent() - 6,
            textWidth + padding * 2, metrics.getHeight() + 12, 12, 12);
        g2.setColor(TEXT);
        g2.drawString(state.captionText, textX, textY);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    // ── Search array ──────────────────────────────────────────────────────────

    private void drawSearchArray(Graphics2D g2) {
        if (state.currentArray == null || state.currentArray.length == 0) return;

        int width = getWidth();
        final int cellWidth   = 56;
        final int gap         = 6;
        final int sidePadding = 24;
        final int rowBarHeight = 100;
        final int rowPadding  = 60;
        final int rowHeight   = rowBarHeight + rowPadding;
        final int top         = 20;

        int perRow  = Math.max(1, (width - sidePadding * 2 + gap) / (cellWidth + gap));
        int numRows = (state.currentArray.length + perRow - 1) / perRow;

        double minValue = Double.MAX_VALUE, maxValue = -Double.MAX_VALUE;
        boolean allNumeric = true;
        for (Object value : state.currentArray) {
            if (value instanceof Number) {
                double numeric = ((Number) value).doubleValue();
                minValue = Math.min(minValue, numeric);
                maxValue = Math.max(maxValue, numeric);
            } else {
                allNumeric = false;
            }
        }
        double range = maxValue == minValue ? 1.0 : maxValue - minValue;
        int minBarHeight = 22;

        for (int row = 0; row < numRows; row++) {
            int startIndex = row * perRow;
            int endIndex   = Math.min(startIndex + perRow, state.currentArray.length);
            int count      = endIndex - startIndex;
            int startX     = (width - (count * cellWidth + (count - 1) * gap)) / 2;
            int baseY      = top + (row + 1) * rowHeight - rowPadding + 10;

            if (row > 0) {
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(sidePadding, top + row * rowHeight - 8, width - sidePadding, top + row * rowHeight - 8);
            }

            for (int index = startIndex; index < endIndex; index++) {
                int column       = index - startIndex;
                int x            = startX + column * (cellWidth + gap);
                int cellState    = state.cellStates    != null && index < state.cellStates.length    ? state.cellStates[index]    : 0;
                float animation  = state.cellAnim      != null && index < state.cellAnim.length      ? state.cellAnim[index]      : 0f;
                int animState    = state.cellAnimState != null && index < state.cellAnimState.length  ? state.cellAnimState[index] : 0;

                int barHeight = allNumeric
                    ? minBarHeight + (int) (((((Number) state.currentArray[index]).doubleValue() - minValue) / range) * (rowBarHeight - minBarHeight))
                    : rowBarHeight / 2;
                float boost = animState == 2 ? 0.08f * animation : animState == 3 ? 0.04f : 0f;
                int animatedBarHeight = (int) (barHeight * (1f + boost));
                int barY = baseY - animatedBarHeight;

                Color fill   = animState == 2 ? lerp(CELL_DEF, CELL_CHECK, animation)
                             : animState == 3 ? lerp(CELL_CHECK, CELL_FOUND, animation)
                             : cellState == 1 ? CELL_SCAN : CELL_DEF;
                Color stroke = animState == 2 ? lerp(BORDER, WARN, animation)
                             : animState == 3 ? lerp(WARN, ACCENT2, animation)
                             : BORDER;

                if ((animState == 2 || animState == 3) && animation > 0.05f) {
                    Color glow = animState == 3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), (int) (55 * animation)));
                    g2.fillRoundRect(x - 3, barY - 3, cellWidth + 6, animatedBarHeight + 6, 10, 10);
                }

                g2.setColor(fill);
                g2.fillRoundRect(x, barY, cellWidth, animatedBarHeight, 6, 6);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke((animState == 2 || animState == 3) ? 1.5f + animation * 0.5f : 0.5f));
                g2.drawRoundRect(x, barY, cellWidth, animatedBarHeight, 6, 6);

                if ((animState == 2 || animState == 3) && animation > 0.1f) {
                    Color cap = animState == 3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(cap.getRed(), cap.getGreen(), cap.getBlue(),
                        (int) (255 * Math.min(1f, animation * 1.5f))));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x + 4, barY + 1, x + cellWidth - 4, barY + 1);
                }

                drawArrayValue(g2, String.valueOf(state.currentArray[index]), x, barY, cellWidth, animatedBarHeight,
                    (animState == 2 || animState == 3)
                        ? lerp(TEXT, animState == 3 ? ACCENT2 : WARN, animation)
                        : TEXT);
                drawArrayIndex(g2, index, x, cellWidth, baseY);

                if ((animState == 2 || animState == 3) && animation >= 0.1f) {
                    Color accent = animState == 3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(),
                        (int) (255 * Math.min(1f, animation * 1.5f))));
                    g2.setStroke(new BasicStroke(2f));
                    int midX = x + cellWidth / 2;
                    g2.drawLine(midX, barY - 5, midX, barY - 18);
                    g2.fillPolygon(new int[]{midX - 5, midX + 5, midX}, new int[]{barY - 16, barY - 16, barY - 5}, 3);
                }
            }
        }
    }

    // ── Sort array ────────────────────────────────────────────────────────────

    private void drawSortArray(Graphics2D g2) {
        if (state.currentArray == null || state.currentArray.length == 0) return;

        int width = getWidth();
        final int cellWidth    = 56;
        final int gap          = 6;
        final int sidePadding  = 24;
        final int rowBarHeight = 100;
        final int rowPadding   = 60;
        final int rowHeight    = rowBarHeight + rowPadding;
        final int top          = 20;

        int perRow  = Math.max(1, (width - sidePadding * 2 + gap) / (cellWidth + gap));
        int numRows = (state.currentArray.length + perRow - 1) / perRow;

        double minValue = Double.MAX_VALUE, maxValue = -Double.MAX_VALUE;
        for (Object value : state.currentArray) {
            if (value instanceof Number) {
                double numeric = ((Number) value).doubleValue();
                minValue = Math.min(minValue, numeric);
                maxValue = Math.max(maxValue, numeric);
            }
        }
        double range = maxValue == minValue ? 1.0 : maxValue - minValue;
        int minBarHeight = 22;

        for (int row = 0; row < numRows; row++) {
            int startIndex = row * perRow;
            int endIndex   = Math.min(startIndex + perRow, state.currentArray.length);
            int count      = endIndex - startIndex;
            int startX     = (width - (count * cellWidth + (count - 1) * gap)) / 2;
            int baseY      = top + (row + 1) * rowHeight - rowPadding + 10;

            if (row > 0) {
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(sidePadding, top + row * rowHeight - 8, width - sidePadding, top + row * rowHeight - 8);
            }

            for (int index = startIndex; index < endIndex; index++) {
                int column   = index - startIndex;
                int x        = startX + column * (cellWidth + gap);
                double numeric = state.currentArray[index] instanceof Number
                    ? ((Number) state.currentArray[index]).doubleValue() : 0.0;
                int barHeight = minBarHeight + (int) (((numeric - minValue) / range) * (rowBarHeight - minBarHeight));
                int barY = baseY - barHeight;

                boolean locked  = state.sortLockedIndices.contains(index);
                boolean focused = state.sortHighlightedIndices.contains(index);
                boolean pivot   = state.sortPivotIndex == index;

                Color fill      = CELL_DEF;
                Color stroke    = BORDER;
                Color textColor = TEXT;
                Color capColor  = null;

                if (locked) {
                    fill      = CELL_FOUND;
                    stroke    = ACCENT2;
                    capColor  = ACCENT2;
                    textColor = ACCENT2;
                } else if (pivot) {
                    fill      = CELL_PIVOT;
                    stroke    = ACCENT;
                    capColor  = ACCENT;
                    textColor = ACCENT;
                } else if (focused) {
                    switch (state.sortStepType) {
                        case COMPARE -> {
                            fill      = CELL_CHECK;
                            stroke    = WARN;
                            capColor  = WARN;
                            textColor = WARN;
                        }
                        case SWAP -> {
                            fill      = CELL_SWAP;
                            stroke    = ACCENT2;
                            capColor  = ACCENT2;
                            textColor = ACCENT2;
                        }
                        case MERGE, HIGHLIGHT -> {
                            fill      = CELL_MERGE;
                            stroke    = ACCENT;
                            capColor  = ACCENT;
                            textColor = STR_IN_WINDOW_TEXT; // reuse light-blue tint
                        }
                        case SORTED, FINAL -> {
                            fill      = CELL_FOUND;
                            stroke    = ACCENT2;
                            capColor  = ACCENT2;
                            textColor = ACCENT2;
                        }
                        default -> {
                            fill      = CELL_ACTIVE;
                            stroke    = ACCENT;
                            capColor  = ACCENT;
                            textColor = ACCENT;
                        }
                    }
                }

                if (focused || pivot || locked) {
                    Color glow = capColor != null ? capColor : stroke;
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), 55));
                    g2.fillRoundRect(x - 3, barY - 3, cellWidth + 6, barHeight + 6, 10, 10);
                }

                g2.setColor(fill);
                g2.fillRoundRect(x, barY, cellWidth, barHeight, 6, 6);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(focused || pivot || locked ? 1.8f : 0.5f));
                g2.drawRoundRect(x, barY, cellWidth, barHeight, 6, 6);

                if (capColor != null) {
                    g2.setColor(capColor);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x + 4, barY + 1, x + cellWidth - 4, barY + 1);
                }

                drawArrayValue(g2, String.valueOf(state.currentArray[index]), x, barY, cellWidth, barHeight, textColor);
                drawArrayIndex(g2, index, x, cellWidth, baseY);

                if (pivot) {
                    g2.setFont(SMALL);
                    g2.setColor(ACCENT);
                    String label = "pivot";
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(label, x + (cellWidth - metrics.stringWidth(label)) / 2, barY - 8);
                } else if (locked) {
                    g2.setFont(SMALL);
                    g2.setColor(ACCENT2);
                    String label = "fixed";
                    FontMetrics metrics = g2.getFontMetrics();
                    g2.drawString(label, x + (cellWidth - metrics.stringWidth(label)) / 2, barY - 8);
                } else if (focused && state.sortStepType == SortEngine.StepType.COMPARE) {
                    int midX = x + cellWidth / 2;
                    g2.setColor(WARN);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(midX, barY - 5, midX, barY - 18);
                    g2.fillPolygon(new int[]{midX - 5, midX + 5, midX}, new int[]{barY - 16, barY - 16, barY - 5}, 3);
                }
            }
        }
    }

    // ── Shared array helpers ──────────────────────────────────────────────────

    private void drawArrayValue(Graphics2D g2, String value, int x, int barY,
                                int cellWidth, int barHeight, Color textColor) {
        g2.setFont(MONO_B);
        g2.setColor(textColor);
        FontMetrics metrics = g2.getFontMetrics();
        int textY = barHeight > metrics.getAscent() + 8 ? barY + metrics.getAscent() + 4 : barY - 4;
        g2.drawString(value, x + (cellWidth - metrics.stringWidth(value)) / 2, textY);
    }

    private void drawArrayIndex(Graphics2D g2, int index, int x, int cellWidth, int baseY) {
        g2.setFont(SMALL);
        g2.setColor(TEXT_HINT);
        String label = String.valueOf(index);
        FontMetrics metrics = g2.getFontMetrics();
        g2.drawString(label, x + (cellWidth - metrics.stringWidth(label)) / 2, baseY + 16);
    }

    // ── Graph ─────────────────────────────────────────────────────────────────

    private void drawGraph(Graphics2D g2) {
        if (state.currentGraph == null || state.currentGraph.isEmpty()) return;

        int width  = getWidth();
        int height = getHeight();
        Map<String, Point> positions = treeLayout(width, height);
        int radius = Math.max(10, Math.min(22, 380 / Math.max(state.currentGraph.size(), 1)));

        // Draw edges
        for (Map.Entry<String, List<String>> entry : state.currentGraph.entrySet()) {
            Point from = positions.get(entry.getKey());
            if (from == null) continue;
            for (String neighbor : entry.getValue()) {
                Point to = positions.get(neighbor);
                if (to == null) continue;
                boolean onPath = state.graphPath.contains(entry.getKey()) && state.graphPath.contains(neighbor);
                g2.setColor(onPath ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(onPath ? 2.4f : 1f));
                g2.drawLine(from.x, from.y, to.x, to.y);
            }
        }

        // Draw nodes
        for (Map.Entry<String, Point> entry : positions.entrySet()) {
            String node  = entry.getKey();
            Point  point = entry.getValue();
            boolean onPath  = state.graphPath.contains(node);
            boolean current = node.equals(state.graphCurrent);
            boolean visited = state.graphVisited.contains(node);
            float pulse = state.nodeAnim.containsKey(node) ? state.nodeAnim.get(node) : 0f;

            Color fill   = onPath   ? GRAPH_PATH_FILL
                         : current  ? GRAPH_CURRENT_FILL
                         : visited  ? GRAPH_VISITED_FILL
                         : CARD;
            Color stroke = onPath                          ? ACCENT2
                         : current                         ? WARN
                         : node.equals(state.graphStart)   ? ACCENT
                         : node.equals(state.graphTarget)  ? DANGER
                         : BORDER;

            if (pulse > 0f && pulse < 1f) {
                float ring = (float) Math.sin(pulse * Math.PI);
                g2.setColor(new Color(stroke.getRed(), stroke.getGreen(), stroke.getBlue(), (int) (80 * ring)));
                int ringRadius = radius + (int) (ring * 8);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(point.x - ringRadius, point.y - ringRadius, ringRadius * 2, ringRadius * 2);
            }

            g2.setColor(fill);
            g2.fillOval(point.x - radius, point.y - radius, radius * 2, radius * 2);
            g2.setColor(stroke);
            g2.setStroke(new BasicStroke(onPath || current ? 2.2f : 1f));
            g2.drawOval(point.x - radius, point.y - radius, radius * 2, radius * 2);

            if (radius >= 12) {
                int fontSize = Math.max(8, Math.min(12, radius - 4));
                g2.setFont(new Font("JetBrains Mono", Font.BOLD, fontSize));
                g2.setColor(onPath ? ACCENT2 : current ? WARN : TEXT);
                FontMetrics metrics = g2.getFontMetrics();
                g2.drawString(node,
                    point.x - metrics.stringWidth(node) / 2,
                    point.y + metrics.getAscent() / 2 - 1);
            }
        }

        drawLegendDot(g2, 20, height - 60, ACCENT,  "start");
        drawLegendDot(g2, 20, height - 40, DANGER,  "target");
        drawLegendDot(g2, 20, height - 20, ACCENT2, "path");
    }

    // ── String search ─────────────────────────────────────────────────────────

    private void drawString(Graphics2D g2) {
        String text    = state.strText;
        String pattern = state.strPattern;
        if (text.isEmpty()) return;

        int width       = getWidth();
        int height      = getHeight();
        int windowStart = state.strWindowStart;
        int patternLength = pattern.length();

        final int charWidth  = 34;
        final int charHeight = 48;
        final int gap        = 3;
        final int side       = 20;

        int totalTextWidth = text.length() * (charWidth + gap);
        int viewportWidth  = width - side * 2;
        int xOffset;
        if (totalTextWidth <= viewportWidth) {
            xOffset = (viewportWidth - totalTextWidth) / 2;
        } else if (windowStart >= 0) {
            int windowMid = windowStart * (charWidth + gap) + (patternLength * (charWidth + gap)) / 2;
            xOffset = viewportWidth / 2 - windowMid;
            xOffset = Math.min(0, Math.max(xOffset, viewportWidth - totalTextWidth));
        } else {
            xOffset = 0;
        }

        int textRowY    = height / 2 - 80;
        int patternRowY = textRowY + charHeight + 38;
        int infoY       = patternRowY + charHeight + 32;

        // "text" label
        g2.setFont(SMALL);
        g2.setColor(TEXT_HINT);
        g2.drawString("text", side, textRowY - 8);

        // Sliding window highlight behind text row
        if (windowStart >= 0 && patternLength > 0) {
            int windowX     = side + xOffset + windowStart * (charWidth + gap) - 4;
            int windowWidth = patternLength * (charWidth + gap) - gap + 8;
            g2.setColor(state.strMismatch ? STR_WIN_MISMATCH : STR_WIN_NORMAL);
            g2.fillRoundRect(windowX, textRowY - 6, windowWidth, charHeight + 12, 10, 10);
            g2.setColor(state.strMismatch ? WARN : ACCENT);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(windowX, textRowY - 6, windowWidth, charHeight + 12, 10, 10);
        }

        // Match underlines
        for (int position : state.matchPositions) {
            int x = side + xOffset + position * (charWidth + gap) - 4;
            int matchWidth = patternLength * (charWidth + gap) - gap + 8;
            if (x + matchWidth < side || x > width - side) continue;
            g2.setColor(STR_MATCH_UNDERLINE);
            g2.fillRoundRect(x, textRowY + charHeight + 2, matchWidth, 5, 3, 3);
        }

        // Text row characters
        for (int i = 0; i < text.length(); i++) {
            int x = side + xOffset + i * (charWidth + gap);
            if (x + charWidth < side || x > width - side) continue;

            boolean inWindow = windowStart >= 0 && i >= windowStart && i < windowStart + patternLength;
            boolean matched  = false;
            boolean mismatch = false;

            for (int position : state.matchPositions) {
                if (i >= position && i < position + patternLength) { matched = true; break; }
            }
            if (inWindow) {
                int posInWindow = i - windowStart;
                if (state.strMismatch && posInWindow == state.strMatchedChars) mismatch = true;
                else if (posInWindow < state.strMatchedChars)                  matched  = true;
            }

            Color fill      = matched   ? STR_MATCH_FILL
                            : mismatch  ? STR_MISMATCH_FILL
                            : inWindow  ? STR_IN_WINDOW_FILL
                            : CELL_DEF;
            Color textColor = matched   ? ACCENT2
                            : mismatch  ? DANGER
                            : inWindow  ? STR_IN_WINDOW_TEXT
                            : TEXT_DIM;

            g2.setColor(fill);
            g2.fillRoundRect(x, textRowY, charWidth, charHeight, 6, 6);
            g2.setColor(matched ? ACCENT2 : mismatch ? DANGER : inWindow ? ACCENT : BORDER);
            g2.setStroke(new BasicStroke(matched || mismatch || inWindow ? 1.2f : 0.5f));
            g2.drawRoundRect(x, textRowY, charWidth, charHeight, 6, 6);

            g2.setFont(MONO_L);
            g2.setColor(textColor);
            FontMetrics metrics = g2.getFontMetrics();
            String ch = String.valueOf(text.charAt(i));
            g2.drawString(ch,
                x + (charWidth - metrics.stringWidth(ch)) / 2,
                textRowY + (charHeight + metrics.getAscent()) / 2 - 2);

            g2.setFont(SMALL);
            g2.setColor(TEXT_HINT);
            String label = String.valueOf(i);
            FontMetrics smallMetrics = g2.getFontMetrics();
            g2.drawString(label,
                x + (charWidth - smallMetrics.stringWidth(label)) / 2,
                textRowY + charHeight + 14);
        }

        // "pattern" label
        g2.setFont(SMALL);
        g2.setColor(TEXT_HINT);
        g2.drawString("pattern", side, patternRowY - 8);

        // Pattern row characters
        if (windowStart >= 0 && !pattern.isEmpty()) {
            for (int j = 0; j < pattern.length(); j++) {
                int x = side + xOffset + (windowStart + j) * (charWidth + gap);
                if (x + charWidth < side || x > width - side) continue;

                boolean matched  = j < state.strMatchedChars && !state.strMismatch;
                boolean mismatch = state.strMismatch && j == state.strMatchedChars;
                Color fill      = matched  ? STR_MATCH_FILL
                                : mismatch ? STR_MISMATCH_FILL
                                : CELL_DEF;
                Color textColor = matched  ? ACCENT2 : mismatch ? DANGER : TEXT_DIM;
                Color stroke    = matched  ? ACCENT2 : mismatch ? DANGER : BORDER;

                g2.setColor(fill);
                g2.fillRoundRect(x, patternRowY, charWidth, charHeight, 6, 6);
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(matched || mismatch ? 1.5f : 0.5f));
                g2.drawRoundRect(x, patternRowY, charWidth, charHeight, 6, 6);

                g2.setFont(MONO_L);
                g2.setColor(textColor);
                FontMetrics metrics = g2.getFontMetrics();
                String ch = String.valueOf(pattern.charAt(j));
                g2.drawString(ch,
                    x + (charWidth - metrics.stringWidth(ch)) / 2,
                    patternRowY + (charHeight + metrics.getAscent()) / 2 - 2);

                g2.setFont(SMALL);
                if (matched) {
                    g2.setColor(ACCENT2);
                    g2.drawString("v", x + charWidth / 2 - 3, patternRowY + charHeight + 13);
                } else if (mismatch) {
                    g2.setColor(DANGER);
                    g2.drawString("x", x + charWidth / 2 - 3, patternRowY + charHeight + 13);
                }
            }

            // Window bracket lines
            g2.setColor(BORDER);
            g2.setStroke(new BasicStroke(0.5f));
            int windowX1 = side + xOffset + windowStart * (charWidth + gap) - 4;
            int windowX2 = windowX1 + patternLength * (charWidth + gap) - gap + 8;
            g2.drawLine(windowX1 + 4, textRowY + charHeight + 6, windowX1 + 4, patternRowY - 6);
            g2.drawLine(windowX2 - 4, textRowY + charHeight + 6, windowX2 - 4, patternRowY - 6);
        }

        drawStringInfo(g2, pattern, infoY, width);
    }

    private void drawStringInfo(Graphics2D g2, String pattern, int y, int width) {
        int panelX      = 20;
        int panelWidth  = width - 40;
        int panelHeight = 56;
        g2.setColor(CARD);
        g2.fillRoundRect(panelX, y, panelWidth, panelHeight, 8, 8);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(panelX, y, panelWidth, panelHeight, 8, 8);

        int cursorX = panelX + 16;
        int cursorY = y + 20;

        g2.setFont(MONO_B);
        g2.setColor(ACCENT);
        String mode = state.strIsKMP ? "KMP" : "Rabin-Karp";
        g2.drawString(mode, cursorX, cursorY);
        cursorX += g2.getFontMetrics().stringWidth(mode) + 20;

        g2.setFont(MONO);
        g2.setColor(TEXT_DIM);
        if (state.strWindowStart >= 0) {
            String position = "window: [" + state.strWindowStart + ", "
                + (state.strWindowStart + pattern.length() - 1) + "]";
            g2.drawString(position, cursorX, cursorY);
            cursorX += g2.getFontMetrics().stringWidth(position) + 20;

            String matched = "matched: " + state.strMatchedChars + "/" + pattern.length();
            g2.setColor(state.strMismatch ? DANGER
                : (state.strMatchedChars == pattern.length() ? ACCENT2 : TEXT_DIM));
            g2.drawString(matched, cursorX, cursorY);
            cursorX += g2.getFontMetrics().stringWidth(matched) + 20;
        }

        if (!state.strIsKMP && state.strHashVal >= 0) {
            g2.setColor(TEXT_DIM);
            g2.drawString("hash: " + (state.strHashVal % 99999L) + " / " + (state.strPatHash % 99999L), cursorX, cursorY);
        }

        if (state.strIsKMP && state.strLPS != null && state.strLPS.length <= 20) {
            cursorY = y + 40;
            cursorX = panelX + 16;
            g2.setColor(TEXT_HINT);
            g2.setFont(SMALL);
            g2.drawString("LPS:", cursorX, cursorY);
            cursorX += 34;
            for (int k = 0; k < state.strLPS.length; k++) {
                boolean current = k == state.strKmpJ && state.strWindowStart >= 0;
                g2.setColor(current ? WARN : TEXT_DIM);
                g2.setFont(current ? MONO_B : MONO);
                g2.drawString(String.valueOf(state.strLPS[k]), cursorX, cursorY);
                cursorX += g2.getFontMetrics().stringWidth(state.strLPS[k] >= 10 ? "99" : "9") + 6;
            }
        }

        if (!state.matchPositions.isEmpty()) {
            cursorY = y + 40;
            cursorX = width - 20;
            g2.setFont(SMALL);
            g2.setColor(ACCENT2);
            String message = state.matchPositions.size() + " match(es) at " + state.matchPositions;
            cursorX -= g2.getFontMetrics().stringWidth(message);
            g2.drawString(message, cursorX, cursorY);
        }
    }

    // ── Graph layout ──────────────────────────────────────────────────────────

    private Map<String, Point> treeLayout(int width, int height) {
        Map<String, Point> positions = new LinkedHashMap<>();
        if (state.currentGraph.isEmpty()) return positions;

        String root = state.currentGraph.keySet().iterator().next();
        Map<String, Integer> levels = new LinkedHashMap<>();
        Queue<String> queue = new LinkedList<>();
        queue.add(root);
        levels.put(root, 0);
        int maxLevel = 0;

        while (!queue.isEmpty()) {
            String node = queue.poll();
            int level   = levels.get(node);
            maxLevel = Math.max(maxLevel, level);
            for (String child : state.currentGraph.getOrDefault(node, new ArrayList<>())) {
                if (!levels.containsKey(child)) {
                    levels.put(child, level + 1);
                    queue.add(child);
                }
            }
        }

        Map<Integer, List<String>> nodesByLevel = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : levels.entrySet()) {
            nodesByLevel.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
        }

        int topPadding   = 30;
        int usableHeight = height - topPadding - 60;
        int numLevels    = maxLevel + 1;
        for (Map.Entry<Integer, List<String>> entry : nodesByLevel.entrySet()) {
            int level         = entry.getKey();
            List<String> nodes = entry.getValue();
            int y = topPadding + (numLevels <= 1 ? usableHeight / 2
                : (int) ((double) level / (numLevels - 1) * usableHeight));
            for (int i = 0; i < nodes.size(); i++) {
                int x = (int) ((i + 1.0) / (nodes.size() + 1) * width);
                positions.put(nodes.get(i), new Point(x, y));
            }
        }
        return positions;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private void drawLegendDot(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(color);
        g2.fillOval(x, y - 5, 10, 10);
        g2.setFont(SMALL);
        g2.setColor(TEXT_DIM);
        g2.drawString(label, x + 16, y + 4);
    }

    static Color lerp(Color start, Color end, float t) {
        float clamped = Math.max(0f, Math.min(1f, t));
        return new Color(
            (int) (start.getRed()   + (end.getRed()   - start.getRed())   * clamped),
            (int) (start.getGreen() + (end.getGreen() - start.getGreen()) * clamped),
            (int) (start.getBlue()  + (end.getBlue()  - start.getBlue())  * clamped)
        );
    }

    private float easeOut(float t) { float f = 1f - t; return 1f - f * f * f; }
    private float easeIn(float t)  { return t * t * t; }
}