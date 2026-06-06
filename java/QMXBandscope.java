/*
 * G0FOZ    code (at) bockhampton.info
 * Copyleft
 * No responsibility will be taken for impact of this code on your system!
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import org.jtransforms.fft.DoubleFFT_1D;

/**
 * QMXBandscope is a Java Swing panel that captures stereo audio (I/Q signals),
 * performs a Fast Fourier Transform (FFT), and renders a real-time spectrum analyzer 
 * and waterfall display. 
 */
public class QMXBandscope extends JPanel {

    private static final long serialVersionUID = 1645222813962590763L;
    // --- DSP Constants ---
    private static final int SAMPLE_RATE = 48000;
    private static final int FFT_SIZE = 2048; 
    
    // --- Data Buffers ---
    private double[] magnitudesDb = new double[FFT_SIZE];
    private BufferedImage waterfallImage;
    private int[] waterfallPixels; 
    
    // --- Dynamic Level Controls (Volatile for Thread Safety) ---
    private volatile double minDbLevel = -100.0;
    private volatile double maxDbLevel = -10.0;
    private volatile int spectrumOffset = 50;

    // --- UI Components ---
    private ScopeDisplay display;

    public QMXBandscope() {
        setLayout(new BorderLayout());
        setBackground(Color.DARK_GRAY);

        // 1. Initialize data buffers
        waterfallImage = new BufferedImage(FFT_SIZE, 300, BufferedImage.TYPE_INT_RGB);
        waterfallPixels = ((DataBufferInt) waterfallImage.getRaster().getDataBuffer()).getData();

        // 2. Setup the control panel with sliders FIRST (at the top)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(1, 3, 10, 0));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Slider: Waterfall Noise Floor (Min dB)
        JSlider minSlider = createSlider("Waterfall Min (Noise Floor)", -100, -30, (int)minDbLevel);
        minSlider.addChangeListener(e -> minDbLevel = minSlider.getValue());
        
        // Slider: Waterfall Peak (Max dB)
        JSlider maxSlider = createSlider("Waterfall Max (Peak)", -60, 40, (int)maxDbLevel);
        maxSlider.addChangeListener(e -> maxDbLevel = maxSlider.getValue());

        // Slider: Spectrum Line Graph Y-Offset
        JSlider offsetSlider = createSlider("Spectrum Line Offset", 40, 100, spectrumOffset);
        offsetSlider.addChangeListener(e -> spectrumOffset = offsetSlider.getValue());

        controlPanel.add(minSlider);
        controlPanel.add(maxSlider);
        controlPanel.add(offsetSlider);

        // Add controls to the NORTH so they are never pushed off the bottom of the window
        add(controlPanel, BorderLayout.NORTH);

        // 3. Setup the custom drawing area
        display = new ScopeDisplay();
        // Scaled down from 1024x600 to fit better inside your main GUI bounds
        display.setPreferredSize(new Dimension(580, 400)); 
        add(display, BorderLayout.CENTER);
    }

    /**
     * Helper method to generate nicely formatted JSliders.
     */
    private JSlider createSlider(String title, int min, int max, int value) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
        slider.setBorder(BorderFactory.createTitledBorder(title));
        slider.setMajorTickSpacing(20);
        slider.setMinorTickSpacing(10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        return slider;
    }

    /**
     * Inner class responsible exclusively for rendering the scope visuals.
     */
    private class ScopeDisplay extends JPanel {
        
        private static final long serialVersionUID = -5904630221697349376L;

		public ScopeDisplay() {
            setBackground(Color.BLACK);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            int width = getWidth();
            int height = getHeight();
            int halfHeight = height / 2;

            // 1. DRAW WATERFALL (Bottom Half)
            g.drawImage(waterfallImage, 0, halfHeight, width, halfHeight, null);

            // 2. DRAW SPECTRUM (Top Half)
            g.setColor(Color.GREEN);
            
            for (int i = 0; i < FFT_SIZE - 1; i++) {
                int x1 = (int) ((i / (double) FFT_SIZE) * width);
                int x2 = (int) (((i + 1) / (double) FFT_SIZE) * width);
                
                // Incorporate dynamic offset from slider
                int y1 = halfHeight - (int) ((magnitudesDb[i] + spectrumOffset) * 3);
                int y2 = halfHeight - (int) ((magnitudesDb[i + 1] + spectrumOffset) * 3);
                
                y1 = Math.max(0, Math.min(halfHeight, y1));
                y2 = Math.max(0, Math.min(halfHeight, y2));

                g.drawLine(x1, y1, x2, y2);
            }
        }
    }

    /**
     * Maps a decibel (dB) value to a color using dynamic ranges set by the sliders.
     */
    private int getColorMap(double db) {
        // Grab current snapshot of volatile variables to prevent mid-calc shifting
        double currentMin = minDbLevel;
        double currentMax = maxDbLevel;
        
        // Safety check to prevent divide-by-zero if sliders overlap
        if (currentMin >= currentMax) currentMax = currentMin + 1; 

        double normalized = (db - currentMin) / (currentMax - currentMin);
        normalized = Math.max(0.0, Math.min(1.0, normalized)); 
        
        float hue = (float) (0.66 - (normalized * 0.66));
        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }

    public void startCapture() {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 2, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            byte[] buffer = new byte[FFT_SIZE * 4]; 
            double[] complexData = new double[FFT_SIZE * 2]; 
            DoubleFFT_1D fft = new DoubleFFT_1D(FFT_SIZE);

            double[] window = new double[FFT_SIZE];
            for (int i = 0; i < FFT_SIZE; i++) {
                window[i] = 0.5 * (1 - Math.cos((2 * Math.PI * i) / (FFT_SIZE - 1)));
            }

            System.out.println("Listening to QMX...");

            while (true) {
                int totalBytesRead = 0;
                while (totalBytesRead < buffer.length) {
                    int bytesRead = line.read(buffer, totalBytesRead, buffer.length - totalBytesRead);
                    if (bytesRead == -1) break; 
                    totalBytesRead += bytesRead;
                }
                
                if (totalBytesRead == buffer.length) {
                    for (int i = 0, j = 0; i < buffer.length; i += 4, j++) {
                        short left  = (short) ((buffer[i] & 0xFF) | (buffer[i+1] << 8));
                        short right = (short) ((buffer[i+2] & 0xFF) | (buffer[i+3] << 8));
                        
                        complexData[j * 2] = (left / 32768.0) * window[j];
                        complexData[j * 2 + 1] = (right / 32768.0) * window[j];
                    }

                    fft.complexForward(complexData);

                    System.arraycopy(waterfallPixels, 0, waterfallPixels, FFT_SIZE, waterfallPixels.length - FFT_SIZE);

                    for (int i = 0; i < FFT_SIZE; i++) {
                        double re = complexData[i * 2];
                        double im = complexData[i * 2 + 1];
                        
                        double mag = Math.sqrt(re * re + im * im);
                        double magDb = 20 * Math.log10(mag + 1e-10); 
                        
                        magnitudesDb[i] = magDb;
                        waterfallPixels[i] = getColorMap(magDb); 
                    }

                    // Target the display sub-panel for repainting, not the whole frame
                    SwingUtilities.invokeLater(display::repaint);
                }
            }
        } catch (LineUnavailableException e) {
            System.err.println("Audio input line unavailable. Is another program using the soundcard?");
            e.printStackTrace();
        }
    }
}