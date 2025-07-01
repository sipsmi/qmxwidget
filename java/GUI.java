/*
 *  G0FOZ    code (at) bockhampton.info
 *  Copyleft
 *  No responsibility will be taken for impact of this code on your system!
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerListModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicIconFactory;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import javax.swing.BoxLayout;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.Button;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.GridLayout;
import javax.swing.SpringLayout;

/**
 * 
 */
public class GUI {
	// mS for each timer fired off
	static final int timerInterval = 25;
	// minor and major ticks to minimise impact on CAT traffic
	private int tickCount = 0;
	private static int tickLimit = 10;
	

	private JFrame frmGfozIc;
	private static XmlRpcQmx qmx;// = new XmlRPCIC7000();
	private static MainClass mc2;// = new MainClass();
	private static QSerialPort serialPort;
	private static RigctlClient rigctl;
	private int sig = 0;
	private JTextField lcdText;
	// Added CAT commands SA (AGC meter), SM (S meter), PC (Power meter), SW (SWR
	// meter)
	private int agc, pc, sw = 0;
	private int oldVal1 = 0;
	private JTextField txtVfoa;
	private JTextField txtVfob;
	private boolean isTx = false;
	private boolean lock = false;
	private int thisWpm = 20;
	private int thisMode = 3;
	private CircularStringBuffer cwBuffer;

	private static MeterPlot plot;
	private MeterPlot pplot;
	private MeterPlot splot;
	private int pwrZCount = 0;
	private int freqA = 0;
	private int freqB = 0;
	private long offset = 12000;
	private String responseString = null;
	String sCommand = "";
	private int tsw = 0; // variable for read swr
	private int tpc = 0; // variable for read power
	private int freqStep = 100;
	
	// Sort some GUI components for global class access
	private DefaultValueDataset dataset = new DefaultValueDataset(10D);
	private JLabel statusLabel;
	private JCheckBox chckbxTx;
	private JSpinner spinnerMode;
	private JSlider wpm;

	/**
	 * Launch the application.
	 */

	public static void xmain(String[] args, XmlRpcQmx ic7000, MainClass mc, QSerialPort sPort) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GUI window = new GUI(ic7000, mc, sPort);
					window.frmGfozIc.setVisible(true);
					GUI.rigctl = new RigctlClient();
					GUI.serialPort = sPort;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI(XmlRpcQmx ic7000, MainClass mc, QSerialPort sPort) {
		initialize();
		qmx = ic7000;
		mc2 = mc;
		serialPort = sPort;
		cwBuffer = new CircularStringBuffer(72);
		cwBuffer.add("                                ");
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGfozIc = new JFrame();
		frmGfozIc.setTitle("G0FOZ - QMX Controller");
		frmGfozIc.setBounds(0, 0, 750, 350);
		frmGfozIc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGfozIc.getContentPane().setLayout(null);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		GridBagConstraints c2 = new GridBagConstraints();
		c2.anchor = GridBagConstraints.FIRST_LINE_START;

		JPanel meter_panel = new JPanel();
		meter_panel.setBounds(0, 0, 750, 100);
		frmGfozIc.getContentPane().add(meter_panel, c);
		JPanel panel = new JPanel();
		panel.setBounds(0, 105, 750, 350);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		frmGfozIc.getContentPane().add(panel, c2);

		// Power Meter
		DefaultValueDataset dataset2 = new DefaultValueDataset(0D);
		JFreeChart pdial = createPwrChart(dataset2);
		pplot = (MeterPlot) pdial.getPlot();

		// SWR Meter
		DefaultValueDataset dataset3 = new DefaultValueDataset(1D);
		JFreeChart swrdial = createSwrChart(dataset3);
		splot = (MeterPlot) swrdial.getPlot();
		spinnerMode = new JSpinner();
		spinnerMode.setToolTipText("Select Mode");
		spinnerMode.setFont(new Font("Verdana", Font.PLAIN, 18));
		spinnerMode.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				serialPort.sendCatStringmain("MD" + mc2.setModeInt((String) spinnerMode.getValue()));
			}

		});
		FlowLayout fl_panel = new FlowLayout(FlowLayout.LEFT, 5, 5);
		panel.setLayout(fl_panel);

		lcdText = new JTextField();
		lcdText.setFont(new Font("Verdana", Font.BOLD, 16));
		lcdText.setEditable(false);
		panel.add(lcdText);
		lcdText.setColumns(32);

		JLabel lblLcd = new JLabel("LCD");
		panel.add(lblLcd);
		spinnerMode.setModel(new SpinnerListModel(new String[] { "LSB", "USB", "CW", "FSK", "CWR", "FSR" }));
		panel.add(spinnerMode);

		JLabel label = new JLabel("");
		panel.add(label);

		txtVfoa = new JTextField();
		txtVfoa.setHorizontalAlignment(SwingConstants.RIGHT);
		txtVfoa.setEditable(false);
		txtVfoa.setText("0");
		panel.add(txtVfoa);
		txtVfoa.setColumns(14);

		JLabel lblVfoa = new JLabel("VFOA");
		panel.add(lblVfoa);

		// Button to send CQ
		// -----------------------------------------------------------------------------------------
		JButton btnSendCq = new JButton("send CQ");
		btnSendCq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				serialPort.sendCatStringmain("KY CQ CQ CQ DE " + mc2.getMyCall() + " " + mc2.getMyCall() + " PSE K");
				System.out.println("Send M1");
			}
		});

		JButton buttonVfoA = new JButton("VFO A");
		panel.add(buttonVfoA);

		JLabel label_1 = new JLabel("");
		panel.add(label_1);

		txtVfob = new JTextField();
		txtVfob.setHorizontalAlignment(SwingConstants.RIGHT);
		txtVfob.setEditable(false);
		txtVfob.setText("0");
		panel.add(txtVfob);
		txtVfob.setColumns(14);

		JLabel lblVfob = new JLabel("VFOB");
		lblVfob.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblVfob);

		wpm = new JSlider();
		wpm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				lock = true;
				// JSlider Temp = (JSlider) e.getSource();
				if (!wpm.getValueIsAdjusting()) {
					int value = wpm.getValue();
					if (qmx != null) {
						serialPort.sendCatStringmain("KS" + value);
						qmx.dispDebug("WPM" + value);
					}
					lock = false;
				}
			}

		});

		JButton buttonVfoB = new JButton("VFO B");
		panel.add(buttonVfoB);

		JLabel label_2 = new JLabel("");
		panel.add(label_2);
		wpm.setMajorTickSpacing(5);
		wpm.setMinorTickSpacing(1);
		wpm.setToolTipText("Speed WPM");
		wpm.setValue(22);
		wpm.setPaintLabels(true);
		wpm.setPaintTicks(true);
		wpm.setMinimum(10);
		wpm.setMaximum(30);
		panel.add(wpm);

		JLabel lblNewLabel_2 = new JLabel("WPM");
		panel.add(lblNewLabel_2);
		panel.add(btnSendCq);

		JToggleButton tglbtnPtt = new JToggleButton("PTT");
		tglbtnPtt.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				lock = true;
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("PTT on" + serialPort.sendCatStringmain("TQ1"));
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					System.out.println("PTT off" + serialPort.sendCatStringmain("TQ0"));
				}
				lock = false;
			}
		});

		JLabel label_3 = new JLabel("");
		panel.add(label_3);

		// sub-panel for the frequency buttons
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 10, 10, 50, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		// frequency up
		// -------------------------------------------------------------------------
		JButton btnUp = new JButton("");
		ImageIcon upIcon = (ImageIcon) UIManager.getIcon("Table.ascendingSortIcon");
		btnUp.setIcon(upIcon);

		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnUp.insets = new Insets(0, 0, 5, 5);
		gbc_btnUp.gridx = 0;
		gbc_btnUp.gridy = 0;
		panel_1.add(btnUp, gbc_btnUp);
		btnUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lock = true;
				setFrequency(freqA, freqStep);
				lock = false;
			}
		});

		// frequency down
		// -------------------------------------------------------------------------
		JButton btnDwn = new JButton("");
		ImageIcon downIcon = (ImageIcon) UIManager.getIcon("Table.descendingSortIcon");
		btnDwn.setIcon(downIcon);
		GridBagConstraints gbc_btnDwn = new GridBagConstraints();
		gbc_btnDwn.insets = new Insets(0, 0, 5, 0);
		gbc_btnDwn.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnDwn.gridx = 1;
		gbc_btnDwn.gridy = 0;
		panel_1.add(btnDwn, gbc_btnDwn);
		btnDwn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lock = true;
				setFrequency(freqA, -freqStep);
				lock = false;
			}
		});
		;

		// set frequency step
		JSpinner spinnerFreqStep = new JSpinner();
		spinnerFreqStep.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				freqStep = mc2.getStepString((String) spinnerFreqStep.getValue());
				System.out.println("Freq step now " + freqStep);
			}

		});
		spinnerFreqStep.setModel(
				new SpinnerListModel(new String[] { " 100 Hz", " 500 Hz", " 1 KHz", "10 KHz", "100 KHz", " 1 MHz" }));
		GridBagConstraints gbc_spinnerFreqStep = new GridBagConstraints();
		gbc_spinnerFreqStep.fill = GridBagConstraints.BOTH;
		gbc_spinnerFreqStep.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerFreqStep.gridx = 2;
		gbc_spinnerFreqStep.gridy = 0;
		panel_1.add(spinnerFreqStep, gbc_spinnerFreqStep);

		JLabel label_4 = new JLabel("");
		panel.add(label_4);
		panel.add(tglbtnPtt);

		chckbxTx = new JCheckBox("TX");
		chckbxTx.setEnabled(false);
		panel.add(chckbxTx);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Info", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel.add(statusPanel);
		statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

		statusLabel = new JLabel("Status");
		statusPanel.add(statusLabel);

		JLabel label_5 = new JLabel("");
		panel.add(label_5);

		JLabel label_6 = new JLabel("");
		panel.add(label_6);

		JLabel label_7 = new JLabel("");
		panel.add(label_7);

		// DefaultValueDataset dataset = new DefaultValueDataset(10D);
		JFreeChart sdial = createChart(dataset);
		plot = (MeterPlot) sdial.getPlot();
		GridBagConstraints gbc_dial = new GridBagConstraints();
		gbc_dial.fill = GridBagConstraints.BOTH;
		gbc_dial.insets = new Insets(0, 0, 0, 0);
		gbc_dial.gridx = 0;
		gbc_dial.gridy = 0;

		ChartPanel chartpanel = new ChartPanel(sdial);
		chartpanel.setBounds(0, 0, 250, 100);
		chartpanel.setFillZoomRectangle(false);
		chartpanel.setEnforceFileExtensions(false);
		// chartpanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null,
		// null));
		chartpanel.setMaximumDrawWidth(250);
		chartpanel.setMaximumDrawHeight(100);
		chartpanel.setLayout(new BoxLayout(chartpanel, BoxLayout.X_AXIS));

		ChartPanel swrchartpanel = new ChartPanel(swrdial);
		swrchartpanel.setBounds(250, 0, 250, 100);
		swrchartpanel.setDomainZoomable(false);
		swrchartpanel.setBorder(null);
		swrchartpanel.setMaximumDrawWidth(250);
		swrchartpanel.setMaximumDrawHeight(100);
		swrchartpanel.setLayout(null);

		ChartPanel pchartpanel = new ChartPanel(pdial);
		pchartpanel.setBounds(500, 0, 250, 100);
		pchartpanel.setDomainZoomable(false);
		pchartpanel.setBorder(null);
		pchartpanel.setMaximumDrawWidth(250);
		pchartpanel.setMaximumDrawHeight(100);
		pchartpanel.setLayout(null);
		meter_panel.setLayout(null);
		meter_panel.add(chartpanel);
		meter_panel.add(swrchartpanel);
		meter_panel.add(pchartpanel);

		// set up the timer
		//
		// split into regular and not regular

		Timer timer = new Timer(timerInterval, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// sort any messages waiting
				processMessageQueue();

				// section for fast queries
				tickCount++;
				serialPort.sendCatStringmain("SM"); // request signal
				serialPort.sendCatStringmain("PC");
				serialPort.sendCatStringmain("SW");

				// agc = mc2.getIntFromString(serialPort.sendCatStringmain("SA"));
				// int tpc = mc2.getIntFromString(serialPort.sendCatStringmain("PC"));
				// int tsw = mc2.getIntFromString(serialPort.sendCatStringmain("SW"));
				// only go to zero if been there for a hilen (QSK PTT(
				if (tpc < 10) {
					pwrZCount++;
					if (pwrZCount > 30) {
						pwrZCount = 0;
						pc = tpc;
						sw = tsw;
					}
				} else {
					pc = tpc;
					sw = tsw;
				}
				// update GUI only if things have changed
				if (oldVal1 != (pc + sw)) {
					qmx.dispDebug("CatResp RX Sig: " + sig + "dB" + " agc/pc/sw " + agc + "/" + pc + "/" + sw);
					dataset2.setValue(pc / 10.0);
					dataset3.setValue((float) (sw / 100.0));
					pplot.setDataset(dataset2);
					splot.setDataset(dataset3);
					oldVal1 = (pc + sw);
				}

				if (tickCount > tickLimit) // only so often do these 
				{
					tickCount = 0; // reset
					//serialPort.sendCatStringmain("IF");
					serialPort.sendCatStringmain("TQ");
					serialPort.sendCatStringmain("MD");
					serialPort.sendCatStringmain("KS");
					serialPort.sendCatStringmain("LC");
					// display the VFO contents
					serialPort.sendCatStringmain("FA");
					serialPort.sendCatStringmain("FB");
					serialPort.sendCatStringmain("TB");
					try {
						rigctl.sendFrequency((long) freqA - offset);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}

	/**
	 * @param fNow
	 * @param offset
	 */
	private void setFrequency(int fNow, int offset) {
		String catString = "FA" + Integer.toString(fNow + offset);
		serialPort.sendCatStringmain(catString);
		catString = "FB" + Integer.toString(fNow + offset);
		serialPort.sendCatStringmain(catString);

	}

	// define signal meter
	/**
	 * @param dataset
	 * @return
	 */
	private static JFreeChart createChart(ValueDataset dataset) {
		MeterPlot plot = new MeterPlot(dataset);
		plot.addInterval(new MeterInterval("All", new Range(0.0, 100.0)));
		plot.addInterval(new MeterInterval("High", new Range(80.0, 100.0)));
		plot.setDialOutlinePaint(Color.white);
		// plot.addInterval(new MeterInterval("Low", new Range(0.00, 70.0), Color.RED,
		// new BasicStroke(2.0f), null));
		plot.setUnits("dB");
		plot.setTickLabelsVisible(true);
		plot.setDialShape(DialShape.CHORD);
		plot.setValuePaint(Color.GRAY);
		// plot.getIntervals().a
		plot.setTickLabelsVisible(true);
		plot.setRange(new Range(0, 100));
		plot.setMeterAngle(180);
		plot.setTickLabelPaint(Color.ORANGE);
		JFreeChart chart = new JFreeChart("RX Signal", JFreeChart.DEFAULT_TITLE_FONT, plot, false);
		return chart;
	}

	// define power meter
	/**
	 * @param dataset
	 * @return
	 */
	private static JFreeChart createPwrChart(ValueDataset dataset) {
		MeterPlot pplot = new MeterPlot(dataset);
		pplot.addInterval(new MeterInterval("All", new Range(5.0, 6.0)));
		// pplot.addInterval(new MeterInterval("High", new Range(4.0, 6.0)));
		pplot.setDialOutlinePaint(Color.white);
		pplot.addInterval(new MeterInterval("Low", new Range(1.00, 2.0), Color.RED, new BasicStroke(2.0f), null));
		pplot.addInterval(new MeterInterval("Mid", new Range(3.0, 4.0), Color.GREEN, new BasicStroke(2.0f), null));
		pplot.setUnits("Watts");
		pplot.setTickLabelsVisible(true);
		pplot.setDialShape(DialShape.CHORD);
		pplot.setValuePaint(Color.GRAY);
		// plot.getIntervals().a
		pplot.setTickLabelsVisible(true);
		pplot.setRange(new Range(0, 6));
		pplot.setMeterAngle(180);
		pplot.setTickLabelPaint(Color.ORANGE);
		JFreeChart chart = new JFreeChart("TX Power", JFreeChart.DEFAULT_TITLE_FONT, pplot, false);
		return chart;
	}

	// define swr meter
	/**
	 * @param dataset
	 * @return
	 */
	private static JFreeChart createSwrChart(ValueDataset dataset) {
		MeterPlot splot = new MeterPlot(dataset);
		// splot.addInterval(new MeterInterval("All", new Range(5.0, 6.0)));
		// pplot.addInterval(new MeterInterval("High", new Range(4.0, 6.0)));
		splot.setDialOutlinePaint(Color.white);
		splot.addInterval(new MeterInterval("Low", new Range(2.00, 3.0), Color.RED, new BasicStroke(2.0f), null));
		splot.addInterval(new MeterInterval("Mid", new Range(1.0, 2.0), Color.GREEN, new BasicStroke(2.0f), null));
		splot.setUnits("VSWR");
		splot.setTickLabelsVisible(true);
		splot.setDialShape(DialShape.CHORD);
		splot.setValuePaint(Color.GRAY);
		// plot.getIntervals().a
		splot.setTickLabelsVisible(true);
		splot.setRange(new Range(1, 3));
		splot.setMeterAngle(180);
		splot.setTickLabelPaint(Color.ORANGE);
		JFreeChart chart = new JFreeChart("SWR", JFreeChart.DEFAULT_TITLE_FONT, splot, false);
		return chart;
	}

	/**
	 * 
	 */
	private void processMessageQueue() {
		// process reponse queue here
		// could do this in seperate function
		while (serialPort.sizeResponse() > 0) {
			responseString = serialPort.fetchAndRemoveResponse();
			if (responseString == null || responseString.charAt(0) == '?' || responseString.length() < 2) {
				// System.out.println("INvalid reponse received: "+responseString );
				continue;
			} else if (responseString.charAt(2) == ';') {
				// System.out.println("Confirmation reponse received: "+responseString );
				continue;
			}
			String sc = responseString.substring(0, 2);
			// System.out.println("Process reponse(" + sc + "): " + responseString);
			switch (sc) {
			case "SM": // Signal Meter
				sig = mc2.getIntFromString(responseString);
				dataset.setValue(sig);
				plot.setDataset(dataset);
				break;
			case "FA": // VFO A
				freqA = mc2.getIntFromString(responseString);
				txtVfoa.setText(mc2.getHumanFreqString(freqA));
				break;
			case "FB": // CFO B
				freqB = mc2.getIntFromString(responseString);
				txtVfob.setText(mc2.getHumanFreqString(freqB));
				break;
			case "PC": // Power Meter
				tpc = mc2.getIntFromString(responseString);
				break;
			case "SW": // SWR Meter
				tsw = mc2.getIntFromString(responseString);
				break;
			case "LC": // LCD Screen contents
				lcdText.setText(responseString.replaceAll("(LC|;)", ""));
				break;
			case "IF": // TS480 Information String
				statusLabel.setText(responseString);
				break;
			// agc = mc2.getIntFromString(serialPort.sendCatStringmain("SA"));
			// int tpc = mc2.getIntFromString(serialPort.sendCatStringmain("PC"));
			// int tsw = mc2.getIntFromString(serialPort.sendCatStringmain("SW"));
			case "TQ": // Transmit states
				// TX state
				isTx = (mc2.getIntFromString(responseString) > 0) ? true : false;
				chckbxTx.setSelected(isTx);
				break;
			case "MD": // currentn mode
				thisMode = mc2.getIntFromString(responseString);
				spinnerMode.setValue(mc2.getModeString(thisMode));
				break;
			case "TB": // cw receiver buffer
				cwBuffer.add(  mc2.getCWTextFromTB(responseString) );
				statusLabel.setText( cwBuffer.toString() );
				
				break;
			case "KS": // current WPM
				if (!lock) {
					thisWpm = mc2.getIntFromString(responseString);
					wpm.setValue(thisWpm);
				}
				break;

			default:
				System.out.println("Process reponse not supported (" + sc + "): " + responseString);
			}

		}
	}

}
