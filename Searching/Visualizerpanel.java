import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;

class VisualizerPanel extends JPanel {

    private static final Color BG         = new Color(10,  11,  14);
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

    private static final Font MONO   = new Font("JetBrains Mono", Font.PLAIN,  13);
    private static final Font MONO_B = new Font("JetBrains Mono", Font.BOLD,   13);
    private static final Font MONO_L = new Font("JetBrains Mono", Font.PLAIN,  18);
    private static final Font MONO_XL= new Font("JetBrains Mono", Font.BOLD,   22);
    private static final Font SANS_B = new Font("Segoe UI",       Font.BOLD,   14);
    private static final Font SMALL  = new Font("Segoe UI",       Font.PLAIN,  11);
    private static final Font CAP_F  = new Font("Segoe UI",       Font.ITALIC, 13);

    private final AnimState state;
    private JScrollPane scrollParent;

    VisualizerPanel(AnimState state) {
        this.state = state;
        setBackground(BG);
    }

    void setScrollParent(JScrollPane sp) { this.scrollParent = sp; }

    @Override
    public Dimension getPreferredSize() {
        if (!state.isArrayAlgo() || state.currentArray == null || state.currentArray.length == 0)
            return new Dimension(800, 430);
        int w = (scrollParent != null) ? scrollParent.getViewport().getWidth() : 800;
        if (w <= 0) w = 800;
        int perRow  = Math.max(1, (w - 48 + 6) / (56 + 6));
        int numRows = (state.currentArray.length + perRow - 1) / perRow;
        return new Dimension(w, Math.max(430, numRows * 160 + 80));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);

        if      (state.isGraphAlgo())  drawGraph(g2);
        else if (state.isStringAlgo()) drawString(g2);
        else                           drawArray(g2);

        if (state.showCaptions && state.captionPhase != 0) drawCaption(g2);
    }

    private void drawCaption(Graphics2D g2) {
        if (state.captionText.isEmpty()) return;
        Rectangle vis = getVisibleRect();
        int w    = getWidth();
        int visH = vis.height > 0 ? vis.height : getHeight();

        g2.setFont(CAP_F);
        FontMetrics fm = g2.getFontMetrics();
        int tw  = fm.stringWidth(state.captionText);
        int pad = 14;

        float alpha, slideY;
        if (state.captionPhase == 1) {
            float ease = easeOut(state.captionProgress);
            alpha  = ease;
            slideY = 18f * (1f - ease);
        } else if (state.captionPhase == 2) {
            alpha = 1f; slideY = 0f;
        } else {
            alpha  = 1f - easeIn(state.captionProgress);
            slideY = 0f;
        }

        float a = Math.max(0f, Math.min(1f, alpha));
        if (a < 0.02f) return;

        int cy = (int)(vis.y + visH - 24 + slideY);
        int cx = (w - tw) / 2;

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        g2.setColor(new Color(14, 18, 28));
        g2.fillRoundRect(cx - pad, cy - fm.getAscent() - 6, tw + pad*2, fm.getHeight() + 12, 12, 12);
        g2.setColor(BORDER);
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRoundRect(cx - pad, cy - fm.getAscent() - 6, tw + pad*2, fm.getHeight() + 12, 12, 12);
        g2.setColor(TEXT);
        g2.drawString(state.captionText, cx, cy);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawArray(Graphics2D g2) {
        if (state.currentArray == null || state.currentArray.length == 0) return;
        int n = state.currentArray.length, w = getWidth();
        final int CW = 56, GAP = 6, SIDE = 24, ROW_BAR = 100, ROW_PAD = 60, ROW_H = ROW_BAR + ROW_PAD, TOP = 20;

        int perRow  = Math.max(1, (w - SIDE*2 + GAP) / (CW + GAP));
        int numRows = (n + perRow - 1) / perRow;

        double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
        boolean allNum = true;
        for (Object o : state.currentArray) {
            if (o instanceof Number) {
                double v = ((Number)o).doubleValue();
                if (v < minVal) minVal = v; if (v > maxVal) maxVal = v;
            } else { allNum = false; }
        }
        double range = (maxVal == minVal) ? 1.0 : maxVal - minVal;
        int minBarH = 22;

        for (int row = 0; row < numRows; row++) {
            int startIdx = row * perRow;
            int endIdx   = Math.min(startIdx + perRow, n);
            int count    = endIdx - startIdx;
            int startX   = (w - (count * CW + (count-1) * GAP)) / 2;
            int baseY    = TOP + (row + 1) * ROW_H - ROW_PAD + 10;

            if (row > 0) {
                g2.setColor(BORDER); g2.setStroke(new BasicStroke(0.5f));
                g2.drawLine(SIDE, TOP + row * ROW_H - 8, w - SIDE, TOP + row * ROW_H - 8);
            }

            for (int idx = startIdx; idx < endIdx; idx++) {
                int col = idx - startIdx;
                int x   = startX + col * (CW + GAP);
                int cs  = (state.cellStates    != null && idx < state.cellStates.length)    ? state.cellStates[idx]    : 0;
                float a = (state.cellAnim      != null && idx < state.cellAnim.length)      ? state.cellAnim[idx]      : 0f;
                int   as= (state.cellAnimState != null && idx < state.cellAnimState.length) ? state.cellAnimState[idx] : 0;

                int barH = allNum
                    ? minBarH + (int)(((((Number)state.currentArray[idx]).doubleValue()-minVal)/range)*(ROW_BAR-minBarH))
                    : ROW_BAR / 2;
                float boost = (as==2) ? 0.08f*a : (as==3) ? 0.04f : 0f;
                int animBarH = (int)(barH*(1f+boost));
                int barY = baseY - animBarH;

                Color bg2 = (as==2) ? lerp(CELL_DEF, CELL_CHECK, a) : (as==3) ? lerp(CELL_CHECK, CELL_FOUND, a)
                          : (cs==1) ? CELL_SCAN : CELL_DEF;
                Color bc  = (as==2) ? lerp(BORDER, WARN, a) : (as==3) ? lerp(WARN, ACCENT2, a) : BORDER;

                if ((as==2||as==3) && a > 0.05f) {
                    Color glow = as==3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(glow.getRed(), glow.getGreen(), glow.getBlue(), (int)(55*a)));
                    g2.fillRoundRect(x-3, barY-3, CW+6, animBarH+6, 10, 10);
                }
                g2.setColor(bg2); g2.fillRoundRect(x, barY, CW, animBarH, 6, 6);
                g2.setColor(bc); g2.setStroke(new BasicStroke((as==2||as==3) ? 1.5f+a*0.5f : 0.5f));
                g2.drawRoundRect(x, barY, CW, animBarH, 6, 6);

                if ((as==2||as==3) && a > 0.1f) {
                    Color cap = as==3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(cap.getRed(), cap.getGreen(), cap.getBlue(), (int)(255*Math.min(1f,a*1.5f))));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawLine(x+4, barY+1, x+CW-4, barY+1);
                }

                String val = String.valueOf(state.currentArray[idx]);
                g2.setFont(MONO_B);
                g2.setColor((as==2||as==3) ? lerp(TEXT, as==3 ? ACCENT2 : WARN, a) : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(val, x + (CW - fm.stringWidth(val))/2,
                    (animBarH > fm.getAscent()+8) ? barY+fm.getAscent()+4 : barY-4);

                g2.setFont(SMALL); g2.setColor(TEXT_HINT);
                String idxS = String.valueOf(idx);
                FontMetrics fm2 = g2.getFontMetrics();
                g2.drawString(idxS, x+(CW-fm2.stringWidth(idxS))/2, baseY+16);

                if ((as==2||as==3) && a >= 0.1f) {
                    Color ac = as==3 ? ACCENT2 : WARN;
                    g2.setColor(new Color(ac.getRed(), ac.getGreen(), ac.getBlue(), (int)(255*Math.min(1f,a*1.5f))));
                    g2.setStroke(new BasicStroke(2f));
                    int mx = x + CW/2;
                    g2.drawLine(mx, barY-5, mx, barY-18);
                    g2.fillPolygon(new int[]{mx-5,mx+5,mx}, new int[]{barY-16,barY-16,barY-5}, 3);
                }
            }
        }
    }

    private void drawGraph(Graphics2D g2) {
        if (state.currentGraph == null || state.currentGraph.isEmpty()) return;
        int w = getWidth(), h = getHeight();
        Map<String, Point> pos = treeLayout(w, h);
        int r = Math.max(10, Math.min(22, 380 / Math.max(state.currentGraph.size(), 1)));

        for (Map.Entry<String, List<String>> e : state.currentGraph.entrySet()) {
            Point from = pos.get(e.getKey()); if (from == null) continue;
            for (String nb : e.getValue()) {
                Point to = pos.get(nb); if (to == null) continue;
                boolean onPath = state.graphPath.contains(e.getKey()) && state.graphPath.contains(nb);
                g2.setColor(onPath ? ACCENT : BORDER);
                g2.setStroke(new BasicStroke(onPath ? 2.4f : 1f));
                g2.drawLine(from.x, from.y, to.x, to.y);
            }
        }

        for (Map.Entry<String, Point> e : pos.entrySet()) {
            String node = e.getKey(); Point pt = e.getValue();
            boolean isPath = state.graphPath.contains(node);
            boolean isCur  = node.equals(state.graphCurrent);
            boolean isVis  = state.graphVisited.contains(node);
            float pulse = state.nodeAnim.containsKey(node) ? state.nodeAnim.get(node) : 0f;

            Color fill   = isPath ? new Color(22,68,50) : isCur ? new Color(55,40,8) : isVis ? new Color(18,28,52) : CARD;
            Color stroke = isPath ? ACCENT2 : isCur ? WARN
                         : node.equals(state.graphStart) ? ACCENT : node.equals(state.graphTarget) ? DANGER : BORDER;

            if (pulse > 0f && pulse < 1f) {
                float ring = (float)Math.sin(pulse * Math.PI);
                g2.setColor(new Color(stroke.getRed(), stroke.getGreen(), stroke.getBlue(), (int)(80*ring)));
                int rr = r + (int)(ring * 8);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(pt.x-rr, pt.y-rr, rr*2, rr*2);
            }

            g2.setColor(fill); g2.fillOval(pt.x-r, pt.y-r, r*2, r*2);
            g2.setColor(stroke); g2.setStroke(new BasicStroke(isPath||isCur ? 2.2f : 1f));
            g2.drawOval(pt.x-r, pt.y-r, r*2, r*2);

            if (r >= 12) {
                int fs = Math.max(8, Math.min(12, r-4));
                g2.setFont(new Font("JetBrains Mono", Font.BOLD, fs));
                g2.setColor(isPath ? ACCENT2 : isCur ? WARN : TEXT);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(node, pt.x - fm.stringWidth(node)/2, pt.y + fm.getAscent()/2 - 1);
            }
        }

        drawLegendDot(g2, 20, h-60, ACCENT,  "start");
        drawLegendDot(g2, 20, h-40, DANGER,  "target");
        drawLegendDot(g2, 20, h-20, ACCENT2, "path");
    }

    private void drawString(Graphics2D g2) {
        String t = state.strText, p = state.strPattern;
        if (t.isEmpty()) return;

        int w = getWidth(), h = getHeight();
        int ws = state.strWindowStart, m = p.length();

        final int CHAR_W = 34, CHAR_H = 48, GAP = 3;
        final int SIDE   = 20;

        // scroll offset: keep the window centred in view
        int totalTextW = t.length() * (CHAR_W + GAP);
        int viewportW  = w - SIDE * 2;
        int xOff;
        if (totalTextW <= viewportW) {
            xOff = (viewportW - totalTextW) / 2;
        } else if (ws >= 0) {
            int windowMid = ws * (CHAR_W + GAP) + (m * (CHAR_W + GAP)) / 2;
            xOff = viewportW / 2 - windowMid;
            xOff = Math.min(0, Math.max(xOff, viewportW - totalTextW));
        } else {
            xOff = 0;
        }

        int textRowY = h / 2 - 80;
        int patRowY  = textRowY + CHAR_H + 38;
        int infoY    = patRowY  + CHAR_H + 32;

        g2.setFont(SMALL); g2.setColor(TEXT_HINT);
        g2.drawString("text", SIDE, textRowY - 8);

        // draw window background
        if (ws >= 0 && m > 0) {
            int winX = SIDE + xOff + ws * (CHAR_W + GAP) - 4;
            int winW = m * (CHAR_W + GAP) - GAP + 8;
            Color winCol = state.strMismatch ? new Color(80, 30, 10) : new Color(20, 40, 80);
            g2.setColor(winCol);
            g2.fillRoundRect(winX, textRowY - 6, winW, CHAR_H + 12, 10, 10);
            Color winBorder = state.strMismatch ? WARN : ACCENT;
            g2.setColor(winBorder); g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(winX, textRowY - 6, winW, CHAR_H + 12, 10, 10);
        }

        // draw confirmed match highlights under text chars
        for (int pos : state.matchPositions) {
            int x = SIDE + xOff + pos * (CHAR_W + GAP) - 4;
            int mw = m * (CHAR_W + GAP) - GAP + 8;
            if (x + mw < SIDE || x > w - SIDE) continue;
            g2.setColor(new Color(12, 60, 38));
            g2.fillRoundRect(x, textRowY + CHAR_H + 2, mw, 5, 3, 3);
        }

        // text characters
        for (int i = 0; i < t.length(); i++) {
            int x = SIDE + xOff + i * (CHAR_W + GAP);
            if (x + CHAR_W < SIDE || x > w - SIDE) continue;

            boolean inWindow = (ws >= 0 && i >= ws && i < ws + m);
            boolean isMatched = false, isMismatch = false;
            for (int pos : state.matchPositions) { if (i >= pos && i < pos + m) { isMatched = true; break; } }

            if (inWindow) {
                int posInWindow = i - ws;
                if (state.strMismatch && posInWindow == state.strMatchedChars) isMismatch = true;
                else if (posInWindow < state.strMatchedChars) isMatched = true;
            }

            Color bg2 = isMatched ? new Color(12,65,42) : isMismatch ? new Color(80,25,10) : inWindow ? new Color(25,38,68) : CELL_DEF;
            Color tc  = isMatched ? ACCENT2 : isMismatch ? DANGER : inWindow ? new Color(160,190,255) : TEXT_DIM;

            g2.setColor(bg2); g2.fillRoundRect(x, textRowY, CHAR_W, CHAR_H, 6, 6);
            g2.setColor(isMatched ? ACCENT2 : isMismatch ? DANGER : inWindow ? ACCENT : BORDER);
            g2.setStroke(new BasicStroke(isMatched || isMismatch || inWindow ? 1.2f : 0.5f));
            g2.drawRoundRect(x, textRowY, CHAR_W, CHAR_H, 6, 6);

            g2.setFont(MONO_L); g2.setColor(tc);
            FontMetrics fm = g2.getFontMetrics();
            String ch = String.valueOf(t.charAt(i));
            g2.drawString(ch, x + (CHAR_W - fm.stringWidth(ch))/2, textRowY + (CHAR_H + fm.getAscent())/2 - 2);

            g2.setFont(SMALL); g2.setColor(new Color(55,62,78));
            String idx = String.valueOf(i);
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(idx, x + (CHAR_W - fm2.stringWidth(idx))/2, textRowY + CHAR_H + 14);
        }

        // pattern row label
        g2.setFont(SMALL); g2.setColor(TEXT_HINT);
        g2.drawString("pattern", SIDE, patRowY - 8);

        // pattern characters aligned to window
        if (ws >= 0 && !p.isEmpty()) {
            for (int j = 0; j < p.length(); j++) {
                int x = SIDE + xOff + (ws + j) * (CHAR_W + GAP);
                if (x + CHAR_W < SIDE || x > w - SIDE) continue;

                boolean matched   = j < state.strMatchedChars && !state.strMismatch;
                boolean mismatched = state.strMismatch && j == state.strMatchedChars;
                boolean pending   = !matched && !mismatched;

                Color bg2 = matched ? new Color(12,65,42) : mismatched ? new Color(80,25,10) : new Color(22,28,44);
                Color tc  = matched ? ACCENT2 : mismatched ? DANGER : TEXT_DIM;
                Color bc  = matched ? ACCENT2 : mismatched ? DANGER : BORDER;

                g2.setColor(bg2); g2.fillRoundRect(x, patRowY, CHAR_W, CHAR_H, 6, 6);
                g2.setColor(bc); g2.setStroke(new BasicStroke(matched||mismatched ? 1.5f : 0.5f));
                g2.drawRoundRect(x, patRowY, CHAR_W, CHAR_H, 6, 6);

                g2.setFont(MONO_L); g2.setColor(tc);
                FontMetrics fm = g2.getFontMetrics();
                String ch = String.valueOf(p.charAt(j));
                g2.drawString(ch, x + (CHAR_W - fm.stringWidth(ch))/2, patRowY + (CHAR_H + fm.getAscent())/2 - 2);

                // match/mismatch icon below pattern char
                if (matched) {
                    g2.setColor(ACCENT2); g2.setFont(SMALL);
                    g2.drawString("v", x + CHAR_W/2 - 3, patRowY + CHAR_H + 13);
                } else if (mismatched) {
                    g2.setColor(DANGER); g2.setFont(SMALL);
                    g2.drawString("x", x + CHAR_W/2 - 3, patRowY + CHAR_H + 13);
                }
            }

            // connector lines from window top to pattern top
            g2.setColor(new Color(50, 60, 90)); g2.setStroke(new BasicStroke(0.5f));
            int wx1 = SIDE + xOff + ws * (CHAR_W + GAP) - 4;
            int wx2 = wx1 + m * (CHAR_W + GAP) - GAP + 8;
            g2.drawLine(wx1 + 4, textRowY + CHAR_H + 6, wx1 + 4, patRowY - 6);
            g2.drawLine(wx2 - 4, textRowY + CHAR_H + 6, wx2 - 4, patRowY - 6);
        }

        // info bar
        drawStringInfo(g2, t, p, infoY, w);
    }

    private void drawStringInfo(Graphics2D g2, String t, String p, int y, int w) {
        int panelX = 20, panelW = w - 40, panelH = 56;
        g2.setColor(CARD); g2.fillRoundRect(panelX, y, panelW, panelH, 8, 8);
        g2.setColor(BORDER); g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(panelX, y, panelW, panelH, 8, 8);

        int cx = panelX + 16, cy = y + 20;
        g2.setFont(MONO_B); g2.setColor(ACCENT);
        g2.drawString(state.strIsKMP ? "KMP" : "Rabin-Karp", cx, cy);
        cx += g2.getFontMetrics().stringWidth(state.strIsKMP ? "KMP" : "Rabin-Karp") + 20;

        g2.setFont(MONO); g2.setColor(TEXT_DIM);
        if (state.strWindowStart >= 0) {
            String pos = "window: [" + state.strWindowStart + ", " + (state.strWindowStart + p.length() - 1) + "]";
            g2.drawString(pos, cx, cy);
            cx += g2.getFontMetrics().stringWidth(pos) + 20;

            String match = "matched: " + state.strMatchedChars + "/" + p.length();
            g2.setColor(state.strMismatch ? DANGER : (state.strMatchedChars == p.length() ? ACCENT2 : TEXT_DIM));
            g2.drawString(match, cx, cy);
            cx += g2.getFontMetrics().stringWidth(match) + 20;
        }

        if (!state.strIsKMP && state.strHashVal >= 0) {
            g2.setColor(TEXT_DIM);
            g2.drawString("hash: " + (state.strHashVal % 99999L) + " / " + (state.strPatHash % 99999L), cx, cy);
            cx += 200;
        }

        if (state.strIsKMP && state.strLPS != null && state.strLPS.length <= 20) {
            cy = y + 40;
            cx = panelX + 16;
            g2.setColor(TEXT_HINT); g2.setFont(SMALL);
            g2.drawString("LPS:", cx, cy);
            cx += 34;
            for (int k = 0; k < state.strLPS.length; k++) {
                boolean cur = (k == state.strKmpJ && state.strWindowStart >= 0);
                g2.setColor(cur ? WARN : TEXT_DIM);
                g2.setFont(cur ? MONO_B : MONO);
                g2.drawString(String.valueOf(state.strLPS[k]), cx, cy);
                cx += g2.getFontMetrics().stringWidth(state.strLPS[k] >= 10 ? "99" : "9") + 6;
            }
        }

        if (!state.matchPositions.isEmpty()) {
            cy = y + 40; cx = w - 20;
            g2.setFont(SMALL); g2.setColor(ACCENT2);
            String ms = state.matchPositions.size() + " match(es) at " + state.matchPositions;
            cx -= g2.getFontMetrics().stringWidth(ms);
            g2.drawString(ms, cx, cy);
        }
    }

    private Map<String, Point> treeLayout(int w, int h) {
        Map<String, Point> pos = new LinkedHashMap<>();
        if (state.currentGraph.isEmpty()) return pos;
        String root = state.currentGraph.keySet().iterator().next();
        Map<String, Integer> level = new LinkedHashMap<>();
        Queue<String> q = new LinkedList<>();
        q.add(root); level.put(root, 0);
        int maxLevel = 0;
        while (!q.isEmpty()) {
            String node = q.poll(); int lv = level.get(node);
            if (lv > maxLevel) maxLevel = lv;
            for (String c : state.currentGraph.getOrDefault(node, new ArrayList<>())) {
                if (!level.containsKey(c)) { level.put(c, lv+1); q.add(c); }
            }
        }
        Map<Integer, List<String>> byLevel = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> e : level.entrySet())
            byLevel.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        int topPad = 30, usableH = h - topPad - 60, levels = maxLevel + 1;
        for (Map.Entry<Integer, List<String>> e : byLevel.entrySet()) {
            int lv = e.getKey(); List<String> nodes = e.getValue();
            int y = topPad + (levels <= 1 ? usableH/2 : (int)((double)lv/(levels-1)*usableH));
            for (int i = 0; i < nodes.size(); i++)
                pos.put(nodes.get(i), new Point((int)((i+1.0)/(nodes.size()+1)*w), y));
        }
        return pos;
    }

    private void drawLegendDot(Graphics2D g2, int x, int y, Color c, String label) {
        g2.setColor(c); g2.fillOval(x, y-5, 10, 10);
        g2.setFont(SMALL); g2.setColor(TEXT_DIM);
        g2.drawString(label, x+16, y+4);
    }

    static Color lerp(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
            (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
            (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
            (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    private float easeOut(float t) { float f = 1f-t; return 1f-f*f*f; }
    private float easeIn(float t)  { return t*t*t; }
}