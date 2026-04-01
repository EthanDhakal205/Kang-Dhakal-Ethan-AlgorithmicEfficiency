package com.algoviz.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * ControlsPanel — array input field, optional target input (search),
 * and the Run / Step / Random / Reset buttons.
 * Painted with custom dark styling.
 */
public class ControlsPanel extends JPanel {

    private final JTextField arrayField;
    private final JTextField targetField;
    private final JLabel targetLabel;
    private final StyledButton btnRun;
    private final StyledButton btnStep;
    private final StyledButton btnRandom;
    private final StyledButton btnReset;

    public interface ControlListener {
        void onRun(String array, int target);
        void onStep();
        void onRandom();
        void onReset();
    }

    private ControlListener listener;

    public ControlsPanel() {
        setBackground(Theme.BG3);
        setPreferredSize(new Dimension(0, 58));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        setBorder(new EmptyBorder(0, 8, 0, 8));

        // Array input
        JLabel arrLabel = makeLabel("ARRAY");
        arrayField = makeTextField("64,34,25,12,22,11,90,45,78,3,56,88", 260);

        // Target input (hidden by default)
        targetLabel = makeLabel("TARGET");
        targetField = makeTextField("25", 60);
        targetField.setVisible(false);
        targetLabel.setVisible(false);

        // Buttons
        btnRandom = new StyledButton("⚄  RANDOM", Theme.ACCENT4, Theme.BG3);
        btnRun    = new StyledButton("▶  RUN",    Theme.ACCENT,  Theme.BG);
        btnStep   = new StyledButton("⏭  STEP",  Theme.ACCENT2, Theme.BG3);
        btnReset  = new StyledButton("↺  RESET",  Theme.BORDER2, Theme.BG3);

        btnRun.setForeground(Theme.BG);

        // Wire actions
        btnRun.addActionListener(e -> {
            if (listener != null) listener.onRun(arrayField.getText().trim(), getTarget());
        });
        btnStep.addActionListener(e -> { if (listener != null) listener.onStep(); });
        btnRandom.addActionListener(e -> { if (listener != null) listener.onRandom(); });
        btnReset.addActionListener(e -> { if (listener != null) listener.onReset(); });

        // Enter key on array field = run
        arrayField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && listener != null)
                    listener.onRun(arrayField.getText().trim(), getTarget());
            }
        });

        add(arrLabel);
        add(arrayField);
        add(targetLabel);
        add(targetField);
        add(Box.createHorizontalStrut(4));
        add(btnRandom);
        add(btnRun);
        add(btnStep);
        add(btnReset);
    }

    // ── PUBLIC API ────────────────────────────────────────────────────────

    public void setListener(ControlListener l) { this.listener = l; }

    public void setArrayText(String text) { arrayField.setText(text); }
    public String getArrayText() { return arrayField.getText().trim(); }
    public int getTarget() {
        try { return Integer.parseInt(targetField.getText().trim()); }
        catch (NumberFormatException e) { return 25; }
    }

    public void setSearchMode(boolean search) {
        targetField.setVisible(search);
        targetLabel.setVisible(search);
        revalidate();
        repaint();
    }

    // ── PAINTING ──────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        Theme.enableAA(g2);
        g2.setColor(Theme.BG3);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Theme.BORDER);
        g2.drawLine(0, getHeight() - 1, getWidth(), getHeight() - 1);
        g2.dispose();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(Theme.FONT_LABEL);
        l.setForeground(Theme.TEXT2);
        return l;
    }

    private JTextField makeTextField(String initial, int width) {
        JTextField f = new JTextField(initial) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                Theme.enableAA(g2);
                g2.setColor(Theme.SURFACE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS);
                super.paintComponent(g);
                g2.setColor(hasFocus() ? Theme.ACCENT : Theme.BORDER2);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, Theme.RADIUS, Theme.RADIUS);
                if (hasFocus()) {
                    g2.setColor(Theme.withAlpha(Theme.ACCENT, 40));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, Theme.RADIUS - 1, Theme.RADIUS - 1);
                }
                g2.dispose();
            }
        };
        f.setFont(Theme.FONT_MONO);
        f.setForeground(Theme.TEXT);
        f.setCaretColor(Theme.ACCENT);
        f.setBackground(Theme.SURFACE);
        f.setBorder(new EmptyBorder(4, 8, 4, 8));
        f.setOpaque(false);
        f.setPreferredSize(new Dimension(width, 32));
        return f;
    }

    // ── STYLED BUTTON ─────────────────────────────────────────────────────

    public static class StyledButton extends JButton {
        private final Color borderColor;
        private boolean hovered = false;

        public StyledButton(String text, Color borderColor, Color bgColor) {
            super(text);
            this.borderColor = borderColor;
            setFont(Theme.FONT_BTN);
            setForeground(borderColor);
            setBackground(bgColor);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(100, 32));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered=true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered=false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.enableAA(g2);
            int W = getWidth(), H = getHeight();

            // Background
            if (hovered) {
                g2.setColor(Theme.withAlpha(borderColor, 40));
            } else {
                g2.setColor(Theme.withAlpha(borderColor, 15));
            }
            // Special: RUN button is solid green
            if (getForeground().equals(Theme.BG)) {
                g2.setColor(hovered ? new Color(0x00ffab) : Theme.ACCENT);
            }
            g2.fillRoundRect(0, 0, W, H, Theme.RADIUS, Theme.RADIUS);

            // Border
            g2.setColor(Theme.withAlpha(borderColor, hovered ? 200 : 100));
            g2.drawRoundRect(0, 0, W - 1, H - 1, Theme.RADIUS, Theme.RADIUS);

            // Glow on hover
            if (hovered) {
                g2.setColor(Theme.withAlpha(borderColor, 30));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(-1, -1, W + 1, H + 1, Theme.RADIUS + 1, Theme.RADIUS + 1);
                g2.setStroke(new BasicStroke(1f));
            }

            // Label
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(),
                    (W - fm.stringWidth(getText())) / 2,
                    (H - fm.getHeight()) / 2 + fm.getAscent());

            g2.dispose();
        }
    }
}
