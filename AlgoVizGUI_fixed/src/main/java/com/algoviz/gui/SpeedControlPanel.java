package com.algoviz.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SpeedControlPanel extends JPanel {

    public record SpeedOption(String label, double multiplier) {
        @Override
        public String toString() {
            return label;
        }
    }

    private final JComboBox<SpeedOption> speedBox;

    public SpeedControlPanel() {
        setBackground(Theme.BG);
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        setPreferredSize(new Dimension(0, 48));
        setBorder(new EmptyBorder(0, 10, 0, 10));

        JLabel label = new JLabel("SPEED");
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT2);

        speedBox = new JComboBox<>(new SpeedOption[]{
                new SpeedOption("0.25x", 0.25),
                new SpeedOption("0.5x", 0.5),
                new SpeedOption("1x", 1.0),
                new SpeedOption("2x", 2.0),
                new SpeedOption("4x", 4.0)
        });
        speedBox.setSelectedIndex(2);
        speedBox.setFont(Theme.FONT_MONO_BOLD);
        speedBox.setBackground(Theme.SURFACE);
        speedBox.setForeground(Theme.TEXT);
        speedBox.setFocusable(false);
        speedBox.setBorder(BorderFactory.createLineBorder(Theme.BORDER2));
        speedBox.setPreferredSize(new Dimension(92, 28));

        JLabel hint = new JLabel("Slower options available for step-by-step learning");
        hint.setFont(Theme.FONT_LABEL);
        hint.setForeground(Theme.TEXT3);

        add(label);
        add(speedBox);
        add(hint);
    }

    public double getSpeedMultiplier() {
        SpeedOption option = (SpeedOption) speedBox.getSelectedItem();
        return option != null ? option.multiplier() : 1.0;
    }
}