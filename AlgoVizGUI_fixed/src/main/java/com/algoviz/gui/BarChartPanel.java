package com.algoviz.gui;

import com.algoviz.models.AlgoStep;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * BarChartPanel — custom-painted Swing panel that renders the algorithm
 * visualization. Draws animated bars, glow effects, grid lines, value labels,
 * index labels, and a search beam for search algorithms.
 */
public class BarChartPanel extends JPanel {

    private AlgoStep currentStep;
    private int[] initialArray;

    // Animated bar heights for smooth transitions
    private double[] animatedHeights;
    private Timer animTimer;

    public BarChartPanel() {
        setBackground(Theme.BG2);
        setPreferredSize(new Dimension(800, 320));
        setMinimumSize(new Dimension(400, 200));
    }

    public void setStep(AlgoStep step) {
        this.currentStep = step;
        if (step != null && animatedHeights == null) {
            animatedHeights = new double[step.getArray().length];
        }
        repaint();
    }

    public void reset() {
        this.currentStep = null;
        this.animatedHeights = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);

        int W = getWidth();
        int H = getHeight();

        // Background fill
        g2.setColor(Theme.BG2);
        g2.fillRect(0, 0, W, H);

        // Grid lines (horizontal)
        g2.setColor(new Color(0x1a1a30));
        int gridLines = 5;
        for (int i = 1; i <= gridLines; i++) {
            int gy = H - (int)(H * i / (gridLines + 1.0)) - 20;
            g2.drawLine(10, gy, W - 10, gy);
        }

        // Draw empty state
        if (currentStep == null) {
            drawEmptyState(g2, W, H);
            g2.dispose();
            return;
        }

        int[] arr = currentStep.getArray();
        if (arr == null || arr.length == 0) {
            drawEmptyState(g2, W, H);
            g2.dispose();
            return;
        }

        drawBars(g2, arr, W, H);
        g2.dispose();
    }

    private void drawBars(Graphics2D g2, int[] arr, int W, int H) {
        int n = arr.length;
        int maxVal = 0;
        for (int v : arr) if (v > maxVal) maxVal = v;
        if (maxVal == 0) maxVal = 1;

        Set<Integer> highlighted = new HashSet<>(currentStep.getHighlightedIndices());
        AlgoStep.StepType type = currentStep.getType();

        int padX = 18;
        int padBottom = 28;
        int padTop = 16;
        int chartW = W - padX * 2;
        int chartH = H - padBottom - padTop;

        int totalGap = Math.max(1, chartW / n);
        int barW = Math.max(4, totalGap - 3);

        for (int i = 0; i < n; i++) {
            int barH = Math.max(4, (int)(((double) arr[i] / maxVal) * chartH));
            int x = padX + i * totalGap;
            int y = H - padBottom - barH;
            boolean isHit = highlighted.contains(i);

            Color barColor = Theme.barColor(type, isHit);

            // Glow effect for highlighted bars
            if (isHit) {
                Theme.paintGlow(g2, x, y, barW, barH, barColor);
            }

            // Bar gradient
            if (type == AlgoStep.StepType.FINAL ||
               (type == AlgoStep.StepType.SORTED && isHit) ||
               (type == AlgoStep.StepType.FOUND && isHit)) {
                Theme.fillGradientV(g2, x, y, barW, barH, Theme.RADIUS,
                        Theme.ACCENT, new Color(0x007a50));
            } else if (type == AlgoStep.StepType.SORTED && !isHit) {
                g2.setColor(new Color(0x1a5c38));
                g2.fillRoundRect(x, y, barW, barH, Theme.RADIUS, Theme.RADIUS);
            } else if (type == AlgoStep.StepType.NOT_FOUND && isHit) {
                Theme.fillGradientV(g2, x, y, barW, barH, Theme.RADIUS,
                        new Color(0xff3c6e), new Color(0x8a1a1a));
            } else {
                // Slight gradient on all bars for depth
                Color top = isHit
                        ? barColor.brighter()
                        : new Color(
                            Math.min(255, barColor.getRed() + 20),
                            Math.min(255, barColor.getGreen() + 20),
                            Math.min(255, barColor.getBlue() + 20));
                Theme.fillGradientV(g2, x, y, barW, barH, Theme.RADIUS, top, barColor);
            }

            // Top highlight line on active bars
            if (isHit) {
                g2.setColor(Theme.withAlpha(barColor, 200));
                g2.fillRoundRect(x + 1, y + 1, barW - 2, 3, 2, 2);
            }

            // Value label (inside/above bar)
            if (barW >= 14) {
                g2.setFont(Theme.FONT_MONO_SM);
                FontMetrics fm = g2.getFontMetrics();
                String val = String.valueOf(arr[i]);
                int lx = x + (barW - fm.stringWidth(val)) / 2;
                int ly = (barH > 20) ? y + barH - 5 : y - 4;

                g2.setColor(isHit ? Color.WHITE : new Color(0xffffff, true));
                if (!isHit) g2.setColor(Theme.withAlpha(Color.WHITE, 100));
                g2.drawString(val, lx, ly);
            }

            // Index label at bottom
            if (barW >= 10 && n <= 30) {
                g2.setFont(Theme.FONT_MONO_SM);
                FontMetrics fm = g2.getFontMetrics();
                String idx = String.valueOf(i);
                int lx = x + (barW - fm.stringWidth(idx)) / 2;
                g2.setColor(Theme.TEXT3);
                g2.drawString(idx, lx, H - 8);
            }
        }

        // Search beam — vertical dashed line at found/not-found index
        int foundIdx = currentStep.getFoundIndex();
        if ((type == AlgoStep.StepType.FOUND || type == AlgoStep.StepType.NOT_FOUND) && foundIdx >= 0) {
            int fx = padX + foundIdx * totalGap + barW / 2;
            Color beamColor = (type == AlgoStep.StepType.FOUND) ? Theme.ACCENT : Theme.ACCENT3;

            // Dashed beam
            g2.setColor(beamColor);
            g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                    0, new float[]{5, 5}, 0));
            g2.drawLine(fx, 0, fx, H - padBottom);
            g2.setStroke(new BasicStroke(1f));

            // Label at top
            String label = (type == AlgoStep.StepType.FOUND) ? "FOUND" : "NOT FOUND";
            g2.setFont(Theme.FONT_BADGE);
            g2.setColor(beamColor);
            FontMetrics fm = g2.getFontMetrics();
            int lw = fm.stringWidth(label) + 10;
            int lx = Math.max(2, Math.min(fx - lw / 2, W - lw - 2));
            g2.setColor(Theme.withAlpha(beamColor, 40));
            g2.fillRoundRect(lx, 4, lw, 16, 4, 4);
            g2.setColor(beamColor);
            g2.drawString(label, lx + 5, 16);
        }
    }

    private void drawEmptyState(Graphics2D g2, int W, int H) {
        g2.setFont(Theme.FONT_MONO);
        g2.setColor(Theme.TEXT3);
        String msg = "Select an algorithm and press  ▶ RUN";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(msg, (W - fm.stringWidth(msg)) / 2, H / 2);

        // Decorative placeholder bars
        int n = 12;
        int barW = 28;
        int gap = 34;
        int startX = (W - n * gap) / 2;
        int[] demoH = {60, 100, 40, 130, 80, 50, 110, 70, 90, 45, 120, 65};
        for (int i = 0; i < n; i++) {
            int bh = (i < demoH.length) ? demoH[i] : 60;
            int x = startX + i * gap;
            int y = H - 50 - bh;
            g2.setColor(new Color(0x2a2a45));
            g2.fillRoundRect(x, y, barW, bh, 4, 4);
        }
    }
}
