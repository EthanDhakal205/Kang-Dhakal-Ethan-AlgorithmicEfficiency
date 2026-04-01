package com.algoviz.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * PlaybackPanel — the transport control bar: ⏮ ◀ ▶/⏸ ▶ ⏭
 * Fully custom-painted circular buttons.
 */
public class PlaybackPanel extends JPanel {

    public interface PlaybackListener {
        void onFirst();
        void onPrev();
        void onPlayPause();
        void onNext();
        void onLast();
    }

    private final CircleButton btnFirst;
    private final CircleButton btnPrev;
    private final CircleButton btnPlay;
    private final CircleButton btnNext;
    private final CircleButton btnLast;
    private boolean playing = false;

    public PlaybackPanel(PlaybackListener listener) {
        setBackground(Theme.BG);
        setLayout(new FlowLayout(FlowLayout.CENTER, 10, 8));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setPreferredSize(new Dimension(0, 70));

        btnFirst = new CircleButton("⏮", 38, Theme.TEXT2, false);
        btnPrev  = new CircleButton("◀", 38, Theme.TEXT2, false);
        btnPlay  = new CircleButton("▶", 52, Theme.BG, true);
        btnNext  = new CircleButton("▶", 38, Theme.TEXT2, false);
        btnLast  = new CircleButton("⏭", 38, Theme.TEXT2, false);

        btnFirst.addActionListener(e -> listener.onFirst());
        btnPrev.addActionListener(e  -> listener.onPrev());
        btnPlay.addActionListener(e  -> listener.onPlayPause());
        btnNext.addActionListener(e  -> listener.onNext());
        btnLast.addActionListener(e  -> listener.onLast());

        add(btnFirst);
        add(btnPrev);
        add(btnPlay);
        add(btnNext);
        add(btnLast);
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
        btnPlay.setLabel(playing ? "⏸" : "▶");
        btnPlay.repaint();
    }

    // ── CIRCLE BUTTON ─────────────────────────────────────────────────────

    private static class CircleButton extends JButton {
        private final int size;
        private final boolean primary;
        private boolean hovered = false;
        private String label;

        CircleButton(String label, int size, Color fg, boolean primary) {
            this.label   = label;
            this.size    = size;
            this.primary = primary;
            setForeground(fg);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(size, size));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered=true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered=false; repaint(); }
            });
        }

        public void setLabel(String l) { this.label = l; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            Theme.enableAA(g2);
            int W = getWidth(), H = getHeight();
            int d = Math.min(W, H);
            int ox = (W - d) / 2, oy = (H - d) / 2;

            if (primary) {
                // Solid accent fill
                Color bg = hovered ? new Color(0x00ffab) : Theme.ACCENT;
                g2.setColor(bg);
                g2.fillOval(ox, oy, d, d);

                if (hovered) {
                    g2.setColor(Theme.withAlpha(Theme.ACCENT, 50));
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(ox - 3, oy - 3, d + 6, d + 6);
                    g2.setStroke(new BasicStroke(1f));
                }
                g2.setColor(Theme.BG);
            } else {
                // Outline style
                g2.setColor(hovered ? Theme.withAlpha(Theme.ACCENT, 30) : Theme.SURFACE);
                g2.fillOval(ox, oy, d, d);
                g2.setColor(hovered ? Theme.ACCENT : Theme.BORDER2);
                g2.drawOval(ox, oy, d - 1, d - 1);

                if (hovered) {
                    g2.setColor(Theme.ACCENT);
                    g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(d * 0.35)));
                } else {
                    g2.setColor(Theme.TEXT2);
                    g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, (int)(d * 0.35)));
                }
            }

            if (primary) {
                g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, (int)(d * 0.38)));
            }

            // Center the label
            FontMetrics fm = g2.getFontMetrics();
            int tx = ox + (d - fm.stringWidth(label)) / 2;
            int ty = oy + (d - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, tx, ty);

            g2.dispose();
        }
    }
}
