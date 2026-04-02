import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;

public class SMeter extends JPanel {

    // The current value of the meter (0.0 to 15.0)
    // 0-9 represents S0 to S9.
    // 10-15 represents +10dB to +60dB (1 unit = 10dB for simplicity here)
    private double value = 0.0;

    public SMeter() {
        setPreferredSize(new Dimension(300, 200));
        setBackground(new Color(245, 245, 240)); // Classic warm meter background
    }

    /**
     * Sets the value of the meter and triggers a repaint.
     * @param value A double between 0.0 and 15.0
     */
    public void setValue(double value) {
        // Clamp the value between 0 and 15
        this.value = Math.max(0.0, Math.min(15.0, value));
        repaint(); // Request a redraw
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g;
        // Enable anti-aliasing for smooth lines and text
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Meter geometric parameters
        int centerX = width / 2;
        int centerY = height - 30; // Pivot point of the needle near the bottom
        int radius = Math.min(width, height) - 40;
        
        // Angles for the arc (in degrees)
        double startAngle = 145; 
        double sweepAngle = -110; // Sweep from left to right
        
        drawScale(g2d, centerX, centerY, radius, startAngle, sweepAngle);
        drawText(g2d, centerX, centerY, radius);
        drawNeedle(g2d, centerX, centerY, radius, startAngle, sweepAngle);
    }

    private void drawScale(Graphics2D g2d, int cx, int cy, int radius, double startAngle, double sweepAngle) {
        g2d.setStroke(new BasicStroke(2.0f));
        
        // Draw the main arc
        Arc2D.Double arc = new Arc2D.Double(cx - radius, cy - radius, radius * 2, radius * 2, startAngle, sweepAngle, Arc2D.OPEN);
        g2d.setColor(Color.BLACK);
        g2d.draw(arc);

        // Draw ticks and labels
        int totalTicks = 15;
        double angleStep = sweepAngle / totalTicks;

        for (int i = 0; i <= totalTicks; i++) {
            double currentAngle = startAngle + (i * angleStep);
            double rad = Math.toRadians(currentAngle);
            
            // Determine tick length
            int tickLen = (i % 2 == 0 || i == 9) ? 12 : 6;
            
            // Calculate coordinates for the tick line
            int x1 = (int) (cx + radius * Math.cos(rad));
            int y1 = (int) (cy - radius * Math.sin(rad));
            int x2 = (int) (cx + (radius - tickLen) * Math.cos(rad));
            int y2 = (int) (cy - (radius - tickLen) * Math.sin(rad));

            // S-units (0-9) are black, > S9 (+10 to +60) are red
            if (i > 9) {
                g2d.setColor(Color.RED);
            } else {
                g2d.setColor(Color.BLACK);
            }

            g2d.draw(new Line2D.Double(x1, y1, x2, y2));

            // Draw labels
            if (i <= 9 && i % 2 != 0) {
                drawLabel(g2d, String.valueOf(i), cx, cy, radius - 25, rad);
            } else if (i == 9) {
                drawLabel(g2d, "9", cx, cy, radius - 25, rad);
            } else if (i == 11) {
                drawLabel(g2d, "+20", cx, cy, radius - 25, rad);
            } else if (i == 13) {
                drawLabel(g2d, "+40", cx, cy, radius - 25, rad);
            } else if (i == 15) {
                drawLabel(g2d, "+60", cx, cy, radius - 25, rad);
            }
        }
    }

    private void drawLabel(Graphics2D g2d, String text, int cx, int cy, int textRadius, double rad) {
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        
        int tx = (int) (cx + textRadius * Math.cos(rad)) - (textWidth / 2);
        int ty = (int) (cy - textRadius * Math.sin(rad)) + (textHeight / 2);
        
        g2d.drawString(text, tx, ty);
    }

    private void drawText(Graphics2D g2d, int cx, int cy, int radius) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
        
        String title = "SIGNAL";
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, cx - fm.stringWidth(title) / 2, cy - radius + 50);
        
        g2d.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String subtitle = "S-UNITS / dB over 9";
        fm = g2d.getFontMetrics();
        g2d.drawString(subtitle, cx - fm.stringWidth(subtitle) / 2, cy - radius + 65);
    }

    private void drawNeedle(Graphics2D g2d, int cx, int cy, int radius, double startAngle, double sweepAngle) {
        // Calculate the current angle based on the value
        double fraction = this.value / 15.0;
        double currentAngle = startAngle + (fraction * sweepAngle);
        double rad = Math.toRadians(currentAngle);

        int needleLength = radius + 5; // Extends slightly past the arc
        
        int tipX = (int) (cx + needleLength * Math.cos(rad));
        int tipY = (int) (cy - needleLength * Math.sin(rad));

        // Draw the needle
        g2d.setColor(Color.DARK_GRAY);
        g2d.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(new Line2D.Double(cx, cy, tipX, tipY));

        // Draw the pivot pin
        int pinRadius = 8;
        g2d.setColor(Color.BLACK);
        g2d.fillOval(cx - pinRadius, cy - pinRadius, pinRadius * 2, pinRadius * 2);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillOval(cx - pinRadius/2, cy - pinRadius/2, pinRadius, pinRadius);
    }

    // --- MAIN METHOD FOR TESTING ---
    public static void mainttt(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("HF Transceiver S-Meter");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            SMeter sMeter = new SMeter();
            frame.add(sMeter, BorderLayout.CENTER);

            // Add a slider to control the meter
            // Slider goes from 0 to 150 (which we will divide by 10 to get 0.0 to 15.0)
            JSlider slider = new JSlider(0, 150, 0);
            slider.setMajorTickSpacing(10);
            slider.setPaintTicks(true);
            slider.addChangeListener(e -> {
                double val = slider.getValue() / 10.0;
                sMeter.setValue(val);
            });
            
            JPanel controlPanel = new JPanel(new BorderLayout());
            controlPanel.add(new JLabel(" Signal Strength: ", JLabel.CENTER), BorderLayout.NORTH);
            controlPanel.add(slider, BorderLayout.CENTER);
            controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            frame.add(controlPanel, BorderLayout.SOUTH);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}