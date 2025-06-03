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
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DialShape;
import org.jfree.chart.plot.MeterInterval;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.data.Range;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;

import javax.swing.BoxLayout;
import java.awt.Font;
import javax.swing.SwingConstants;

public class GUI {

	private JFrame frmGfozIc;
	private XmlRpcQmx qmx;// = new XmlRPCIC7000();
	private MainClass mc2;// = new MainClass();
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
	private boolean lockWPM = false;
	private int thisWpm = 20;
	private int thisMode = 3;
	// minor and major ticks to minimise impact on CAT traffic
	private int tickCount = 0;
	private static int tickLimit = 5;
	private static MeterPlot plot;
	private MeterPlot pplot;
	private MeterPlot splot;
	private int pwrZCount = 0;
	private int freqA = 0;
	private int freqB = 0;
	private long offset = 12000;

	/**
	 * Launch the application.
	 */
	public static void xmain(String[] args, XmlRpcQmx ic7000, MainClass mc) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GUI window = new GUI(ic7000, mc);
					window.frmGfozIc.setVisible(true);
					GUI.rigctl = new RigctlClient("localhost", 4532);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI(XmlRpcQmx ic7000, MainClass mc) {
		initialize();
		qmx = ic7000;
		mc2 = mc;
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

		JPanel meter_panel = new JPanel();
		meter_panel.setBounds(0, 0, 750, 100);
		frmGfozIc.getContentPane().add(meter_panel, c);
		JPanel panel = new JPanel();
		panel.setBounds(0, 100, 750, 250);
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		frmGfozIc.getContentPane().add(panel, c);

		GridBagLayout panelLayout = new GridBagLayout();

		// Power Meter
		DefaultValueDataset dataset2 = new DefaultValueDataset(0D);
		JFreeChart pdial = createPwrChart(dataset2);
		pplot = (MeterPlot) pdial.getPlot();

		// SWR Meter
		DefaultValueDataset dataset3 = new DefaultValueDataset(1D);
		JFreeChart swrdial = createSwrChart(dataset3);
		splot = (MeterPlot) swrdial.getPlot();
		panelLayout.columnWidths = new int[] { 500, 60, 50, 0, 00 };
		panelLayout.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		panelLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panel.setLayout(panelLayout);
		JSpinner spinnerMode = new JSpinner();
		spinnerMode.setToolTipText("Select Mode");
		spinnerMode.setFont(new Font("Verdana", Font.PLAIN, 18));
		spinnerMode.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				qmx.sendCatStringmain("MD" + mc2.setModeInt((String) spinnerMode.getValue()));
			}

		});

		lcdText = new JTextField();
		lcdText.setFont(new Font("Verdana", Font.BOLD, 16));
		lcdText.setEditable(false);
		GridBagConstraints gbc_lcdText = new GridBagConstraints();
		gbc_lcdText.insets = new Insets(0, 0, 5, 5);
		gbc_lcdText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lcdText.gridx = 0;
		gbc_lcdText.gridy = 0;
		panel.add(lcdText, gbc_lcdText);
		lcdText.setColumns(32);

		JLabel lblLcd = new JLabel("LCD");
		GridBagConstraints gbc_lblLcd = new GridBagConstraints();
		gbc_lblLcd.anchor = GridBagConstraints.LINE_START;
		gbc_lblLcd.insets = new Insets(0, 0, 5, 5);
		gbc_lblLcd.gridx = 1;
		gbc_lblLcd.gridy = 0;
		panel.add(lblLcd, gbc_lblLcd);
		spinnerMode.setModel(new SpinnerListModel(new String[] { "LSB", "USB", "CW", "FSK", "CWR", "FSR" }));
		GridBagConstraints gbc_spinnerMode = new GridBagConstraints();
		gbc_spinnerMode.insets = new Insets(0, 0, 5, 5);
		gbc_spinnerMode.gridx = 2;
		gbc_spinnerMode.gridy = 0;
		panel.add(spinnerMode, gbc_spinnerMode);

		txtVfoa = new JTextField();
		txtVfoa.setHorizontalAlignment(SwingConstants.RIGHT);
		txtVfoa.setEditable(false);
		txtVfoa.setText("0");
		GridBagConstraints gbc_txtVfoa = new GridBagConstraints();
		gbc_txtVfoa.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVfoa.anchor = GridBagConstraints.EAST;
		gbc_txtVfoa.insets = new Insets(0, 0, 5, 5);
		gbc_txtVfoa.gridx = 0;
		gbc_txtVfoa.gridy = 1;
		panel.add(txtVfoa, gbc_txtVfoa);
		txtVfoa.setColumns(20);

		JLabel lblVfoa = new JLabel("VFOA");
		GridBagConstraints gbc_lblVfoa = new GridBagConstraints();
		gbc_lblVfoa.anchor = GridBagConstraints.WEST;
		gbc_lblVfoa.insets = new Insets(0, 0, 5, 5);
		gbc_lblVfoa.gridx = 1;
		gbc_lblVfoa.gridy = 1;
		panel.add(lblVfoa, gbc_lblVfoa);

		// null
		// mc2.toString();

		JButton btnSendCq = new JButton("send CQ");
		btnSendCq.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				qmx.sendCatStringmain("KY CQ CQ CQ DE " + mc2.getMyCall() + " " + mc2.getMyCall() + " PSE K");
				System.out.println("Send M1");
			}
		});

		txtVfob = new JTextField();
		txtVfob.setHorizontalAlignment(SwingConstants.RIGHT);
		txtVfob.setEditable(false);
		txtVfob.setText("0");
		GridBagConstraints gbc_txtVfob = new GridBagConstraints();
		gbc_txtVfob.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtVfob.insets = new Insets(0, 0, 5, 5);
		gbc_txtVfob.gridx = 0;
		gbc_txtVfob.gridy = 2;
		panel.add(txtVfob, gbc_txtVfob);
		txtVfob.setColumns(8);

		JLabel lblVfob = new JLabel("VFOB");
		GridBagConstraints gbc_lblVfob = new GridBagConstraints();
		gbc_lblVfob.anchor = GridBagConstraints.WEST;
		gbc_lblVfob.insets = new Insets(0, 0, 5, 5);
		gbc_lblVfob.gridx = 1;
		gbc_lblVfob.gridy = 2;
		panel.add(lblVfob, gbc_lblVfob);

		JSlider wpm = new JSlider();
		wpm.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				lockWPM = true;
				// JSlider Temp = (JSlider) e.getSource();
				if (!wpm.getValueIsAdjusting()) {
					int value = wpm.getValue();
					if (qmx != null) {
						qmx.dispDebug("CatResp: " + qmx.sendCatStringmain("KS" + value));
						qmx.dispDebug("WPM" + value);
					}
					lockWPM = false;
				}
			}

		});
		wpm.setMajorTickSpacing(5);
		wpm.setMinorTickSpacing(1);
		wpm.setToolTipText("Speed WPM");
		wpm.setValue(22);
		wpm.setPaintLabels(true);
		wpm.setPaintTicks(true);
		wpm.setMinimum(10);
		wpm.setMaximum(30);
		GridBagConstraints gbc_wpm = new GridBagConstraints();
		gbc_wpm.fill = GridBagConstraints.HORIZONTAL;
		gbc_wpm.insets = new Insets(0, 0, 5, 5);
		gbc_wpm.gridx = 0;
		gbc_wpm.gridy = 3;
		panel.add(wpm, gbc_wpm);

		JLabel lblNewLabel_2 = new JLabel("WPM");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.LINE_START;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 3;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		GridBagConstraints gbc_btnSendCq = new GridBagConstraints();
		gbc_btnSendCq.insets = new Insets(0, 0, 5, 5);
		gbc_btnSendCq.gridx = 2;
		gbc_btnSendCq.gridy = 3;
		panel.add(btnSendCq, gbc_btnSendCq);

		JToggleButton tglbtnPtt = new JToggleButton("PTT");
		tglbtnPtt.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("PTT on" + qmx.sendCatStringmain("TQ1"));
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					System.out.println("PTT off" + qmx.sendCatStringmain("TQ0"));
				}
			}
		});
		GridBagConstraints gbc_tglbtnPtt = new GridBagConstraints();
		gbc_tglbtnPtt.insets = new Insets(0, 0, 5, 5);
		gbc_tglbtnPtt.gridx = 2;
		gbc_tglbtnPtt.gridy = 4;
		panel.add(tglbtnPtt, gbc_tglbtnPtt);

		// intialise the dials
		int pwr = 4;

		JCheckBox chckbxTx = new JCheckBox("TX");
		chckbxTx.setEnabled(false);

		GridBagConstraints gbc_chckbxTx = new GridBagConstraints();
		gbc_chckbxTx.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxTx.gridx = 3;
		gbc_chckbxTx.gridy = 4;
		panel.add(chckbxTx, gbc_chckbxTx);

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Info", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(51, 51, 51)));
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.insets = new Insets(0, 0, 0, 5);
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 8;
		panel.add(statusPanel, gbc_statusPanel);
		GridBagLayout gbl_statusPanel = new GridBagLayout();
		gbl_statusPanel.columnWidths = new int[] { 0, 0 };
		gbl_statusPanel.rowHeights = new int[] { 0, 0 };
		gbl_statusPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_statusPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		statusPanel.setLayout(gbl_statusPanel);

		JLabel statusLabel = new JLabel("Status");
		GridBagConstraints gbc_statusLabel = new GridBagConstraints();
		gbc_statusLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_statusLabel.gridx = 0;
		gbc_statusLabel.gridy = 0;
		statusPanel.add(statusLabel, gbc_statusLabel);

		DefaultValueDataset dataset = new DefaultValueDataset(10D);
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
		//

		Timer timer = new Timer(10, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				// section for fas queries
				tickCount++;
				sig = mc2.getIntFromString(qmx.sendCatStringmain("SM"));

				dataset.setValue(sig);
				plot.setDataset(dataset);
				agc = mc2.getIntFromString(qmx.sendCatStringmain("SA"));
				int tpc = mc2.getIntFromString(qmx.sendCatStringmain("PC"));
				int tsw = mc2.getIntFromString(qmx.sendCatStringmain("SW"));
				// only go to zero if been there for a hilen (QSK PTT(
				if (tpc < 10) {
					pwrZCount++;
					if (pwrZCount > 12) {
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

				if (tickCount > tickLimit) // only so often
				{
					tickCount = 0; // reset
					statusLabel.setText(qmx.sendCatStringmain("IF"));
					// TX state
					isTx = (mc2.getIntFromString(qmx.sendCatStringmain("TQ"))) > 0 ? true : false;
					chckbxTx.setSelected(isTx);
					// currentn mode
					thisMode = mc2.getIntFromString(qmx.sendCatStringmain("MD"));
					spinnerMode.setValue(mc2.getModeString(thisMode));
					// current WPM
					if (!lockWPM) {
						thisWpm = mc2.getIntFromString(qmx.sendCatStringmain("KS"));
						wpm.setValue(thisWpm);
					}
					// signal strenght
					lcdText.setText(qmx.sendCatStringmain("LC").replaceAll("(LC|;)", ""));

					// display the VFO contents
					freqA = mc2.getIntFromString(qmx.sendCatStringmain("FA"));
					freqB = mc2.getIntFromString(qmx.sendCatStringmain("FB"));
					txtVfoa.setText(Integer.toString(freqA));
					txtVfob.setText(Integer.toString(freqB));
					// qmx.sendCatStringmain("FB");
					try {

						rigctl.sendFrequency((long) freqA - offset);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
		timer.setRepeats(true);
		timer.start();
	}

	// define signal meter
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
}
