package com.vorono4ka;

import jtimeselector.JTimeSelector;

import javax.swing.*;
import java.awt.*;

public class JTimeSelectorGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(JTimeSelectorGUI::createGUI);
    }

    public static void createGUI() {
        JFrame frame = new JFrame("Time Selector Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        final JTimeSelector jTimeSelector = new JTimeSelector((x) -> Float.toString(x / 1000f));
        frame.add(new JScrollPane(jTimeSelector));
        jTimeSelector.addTimeValuesLayer("Test Layer", new long[]{1000, 2000, 3000, 4000, 5000, 6000});
        jTimeSelector.addTimeValuesLayer("Test Layer 2", new long[]{2_000, 3_000, 4_000, 5_000, 6_000, 8_000, 10_000, 15_000, 16_000});
        jTimeSelector.addTimeValuesLayer("Empty layer", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 2", new long[]{});
        //jTimeSelector.addTimeValuesLayer("Empty layer 3", new long[]{});
        frame.setSize(800, 400);
        frame.setVisible(true);
        // test of the correct behaviour if layers are added/removed:
//        new Thread(() -> {
//            try {
//                Thread.sleep(4000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.removeAllGraphLayers();
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("TestLayer 2nd phase", new long[]{2_000L, 3_000L, 4_000L, 5_000L, 6_000L, 8_000L, 10_000L, 15_000L, 16_000L});
//            jTimeSelector.requireRepaint();
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(JTimeSelector.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            jTimeSelector.addTimeValuesLayer("NewLayer", new long[]{1_200L, 2_000L, 3_000L, 4_000L, 5_000L, 6_000L});
//            jTimeSelector.requireRepaint();
//
//        }).start();
        jTimeSelector.addTimeSelectionChangedListener((x) -> {
            String type = switch (x.getTimeSelectionType()) {
                case SingleValue -> "single value";
                case Interval -> "interval";
                case None -> "none";
            };
            System.out.println("Time selection changed to " + type);
        });
    }
}
