import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt; // Added for fast pixel shifting
import org.jtransforms.fft.DoubleFFT_1D;

public class QMXBandscope extends JPanel {

    private static final int SAMPLE_RATE = 48000;
    private static final int FFT_SIZE = 2048; 
    
    private double[] magnitudesDb = new double[FFT_SIZE];
    private BufferedImage waterfallImage;
    private int[] waterfallPixels; // Direct access to the image's pixels
    
    public QMXBandscope() {
        setPreferredSize(new Dimension(1024, 600)); 
        setBackground(Color.BLACK);
        
        // Initialize the image
        waterfallImage = new BufferedImage(FFT_SIZE, 300, BufferedImage.TYPE_INT_RGB);
        
        // Grab the raw pixel array backing the image for ultra-fast, bug-free shifting
        waterfallPixels = ((DataBufferInt) waterfallImage.getRaster().getDataBuffer()).getData();
    }

    private int getColorMap(double db) {
        double minDb = -100.0;
        double maxDb = -10.0; 
        
        double normalized = (db - minDb) / (maxDb - minDb);
        normalized = Math.max(0.0, Math.min(1.0, normalized)); 
        
        float hue = (float) (0.66 - (normalized * 0.66));
        return Color.HSBtoRGB(hue, 1.0f, 1.0f);
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
            
            int y1 = halfHeight - (int) ((magnitudesDb[i] + 100) * 3);
            int y2 = halfHeight - (int) ((magnitudesDb[i + 1] + 100) * 3);
            
            y1 = Math.max(0, Math.min(halfHeight, y1));
            y2 = Math.max(0, Math.min(halfHeight, y2));

            g.drawLine(x1, y1, x2, y2);
        }
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
                // Robust Audio Read: Ensure we get exactly the bytes we asked for
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

                    // --- FAST WATERFALL SHIFT ---
                    // Shifts all pixels down by one row (FFT_SIZE) instantly. 100% OS-independent.
                    System.arraycopy(waterfallPixels, 0, waterfallPixels, FFT_SIZE, waterfallPixels.length - FFT_SIZE);

                    for (int i = 0; i < FFT_SIZE; i++) {
                        double re = complexData[i * 2];
                        double im = complexData[i * 2 + 1];
                        
                        double mag = Math.sqrt(re * re + im * im);
                        double magDb = 20 * Math.log10(mag + 1e-10); 
                        
                        magnitudesDb[i] = magDb;
                        
                        // Set the new color directly into the top row of our pixel array
                        waterfallPixels[i] = getColorMap(magDb); 
                    }

                    SwingUtilities.invokeLater(this::repaint);
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void __main(String[] args) {
        JFrame frame = new JFrame("QMX IQ Bandscope & Waterfall");
        QMXBandscope scope = new QMXBandscope();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scope);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Thread(scope::startCapture).start();
    }
}