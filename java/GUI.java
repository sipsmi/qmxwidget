import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.JButton;
import javax.swing.SpinnerListModel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import java.awt.Panel;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.border.TitledBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.EtchedBorder;

public class GUI {

	private JFrame frmGfozIc;
	private XmlRpcQmx qmx;// = new XmlRPCIC7000();
	private MainClass mc2;// = new MainClass();
	private static boolean debug = false;
	private int sig = 0;
	private JTextField lcdText;
	// Added CAT commands SA (AGC meter), SM (S meter), PC (Power meter), SW (SWR
	// meter)
	private int agc, pc, sw = 0;
	private JTextField pwrt;
	private JTextField swrt;
	private int oldVal1 = 0;
	private JTextField txtVfoa;
	private JTextField txtVfob;
	private boolean isTx = false;
	private int thisWpm = 20;
	private int thisMode = 3;

	/**
	 * Launch the application.
	 */
	public static void xmain(String[] args, XmlRpcQmx ic7000, MainClass mc) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					GUI window = new GUI();
					window.frmGfozIc.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
		qmx = new XmlRpcQmx();
		mc2 = new MainClass();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmGfozIc = new JFrame();
		frmGfozIc.setTitle("G0FOZ - QMX Controller");
		frmGfozIc.setBounds(100, 100, 450, 300);
		frmGfozIc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmGfozIc.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		frmGfozIc.getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		JSpinner spinner = new JSpinner();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 55, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);
		JSlider power = new JSlider();
		power.setPaintLabels(true);
		power.setMajorTickSpacing(20);
		power.setMinorTickSpacing(5);
		power.setPaintTicks(true);
		// power.addChangeListener(new ChangeListener() {
		// @Override
		// public void stateChanged(ChangeEvent evt) {
		// JSlider Temp = (JSlider) evt.getSource();
		// if (!Temp.getValueIsAdjusting()) {
		// int value = Temp.getValue();
		// ic7000.flrigSetInteger("rig.set_power", value);
		// System.out.println("Power" + value);
		// }
		// }
		// });
		// power.setPaintLabels(true);
		// power.setSnapToTicks(true);
		// power.setPaintTicks(true);
		// power.setMinorTickSpacing(2);
		// power.setMajorTickSpacing(20);
		GridBagConstraints gbc_power = new GridBagConstraints();
		gbc_power.fill = GridBagConstraints.BOTH;
		gbc_power.insets = new Insets(0, 0, 5, 5);
		gbc_power.gridx = 0;
		gbc_power.gridy = 0;
		panel.add(power, gbc_power);

		JSlider wpm = new JSlider();
		wpm.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSlider Temp = (JSlider) e.getSource();
				if (!Temp.getValueIsAdjusting()) {
					int value = Temp.getValue();
					if (qmx != null) {
						if (debug)
							System.out.println("CatResp: " + qmx.sendCatStringmain("KS" + value));
						spinner.setValue(value);
					}
					System.out.println("WPM" + value);
				}
			}

		});

		JLabel lblNewLabel_1 = new JLabel("Signal (dB)");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JSpinner spinnerMode = new JSpinner();
		spinnerMode.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				qmx.sendCatStringmain("MD"  + qmx.setModeInt(  (String) spinnerMode.getValue()   ));
	    }
				
		});
		spinnerMode.setModel(new SpinnerListModel(new String[] { "LSB", "USB", "CW", "FSK", "CWR", "FSR" }));
		GridBagConstraints gbc_spinnerMode = new GridBagConstraints();
		gbc_spinnerMode.insets = new Insets(0, 0, 5, 0);
		gbc_spinnerMode.gridx = 2;
		gbc_spinnerMode.gridy = 0;
		panel.add(spinnerMode, gbc_spinnerMode);

		JToggleButton tglbtnNewToggleButton = new JToggleButton("STOP");
		GridBagConstraints gbc_tglbtnNewToggleButton = new GridBagConstraints();
		gbc_tglbtnNewToggleButton.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnNewToggleButton.gridx = 2;
		gbc_tglbtnNewToggleButton.gridy = 1;
		panel.add(tglbtnNewToggleButton, gbc_tglbtnNewToggleButton);

		lcdText = new JTextField();
		GridBagConstraints gbc_lcdText = new GridBagConstraints();
		gbc_lcdText.insets = new Insets(0, 0, 5, 5);
		gbc_lcdText.fill = GridBagConstraints.HORIZONTAL;
		gbc_lcdText.gridx = 0;
		gbc_lcdText.gridy = 2;
		panel.add(lcdText, gbc_lcdText);
		lcdText.setColumns(10);

		JLabel lblLcd = new JLabel("LCD");
		GridBagConstraints gbc_lblLcd = new GridBagConstraints();
		gbc_lblLcd.anchor = GridBagConstraints.LINE_START;
		gbc_lblLcd.insets = new Insets(0, 0, 5, 5);
		gbc_lblLcd.gridx = 1;
		gbc_lblLcd.gridy = 2;
		panel.add(lblLcd, gbc_lblLcd);

		JButton btnSendCq = new JButton("send CQ");
		btnSendCq.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				qmx.sendCatStringmain("");
				System.out.println("Send M1");
			}
		});

		txtVfoa = new JTextField();
		txtVfoa.setText("0");
		GridBagConstraints gbc_txtVfoa = new GridBagConstraints();
		gbc_txtVfoa.anchor = GridBagConstraints.EAST;
		gbc_txtVfoa.insets = new Insets(0, 0, 5, 5);
		gbc_txtVfoa.gridx = 0;
		gbc_txtVfoa.gridy = 3;
		panel.add(txtVfoa, gbc_txtVfoa);
		txtVfoa.setColumns(8);

		JLabel lblVfoa = new JLabel("VFOA");
		GridBagConstraints gbc_lblVfoa = new GridBagConstraints();
		gbc_lblVfoa.anchor = GridBagConstraints.WEST;
		gbc_lblVfoa.insets = new Insets(0, 0, 5, 5);
		gbc_lblVfoa.gridx = 1;
		gbc_lblVfoa.gridy = 3;
		panel.add(lblVfoa, gbc_lblVfoa);
		GridBagConstraints gbc_btnSendCq = new GridBagConstraints();
		gbc_btnSendCq.insets = new Insets(0, 0, 5, 0);
		gbc_btnSendCq.gridx = 2;
		gbc_btnSendCq.gridy = 3;
		panel.add(btnSendCq, gbc_btnSendCq);

		JToggleButton tglbtnPtt = new JToggleButton("PTT");
		tglbtnPtt.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					System.out.println("PTT on" + qmx.sendCatStringmain("TQ1"));
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					System.out.println("PTT off" + qmx.sendCatStringmain("TQ0"));
				}
			}
		});

		txtVfob = new JTextField();
		txtVfob.setText("0");
		GridBagConstraints gbc_txtVfob = new GridBagConstraints();
		gbc_txtVfob.anchor = GridBagConstraints.EAST;
		gbc_txtVfob.insets = new Insets(0, 0, 5, 5);
		gbc_txtVfob.gridx = 0;
		gbc_txtVfob.gridy = 4;
		panel.add(txtVfob, gbc_txtVfob);
		txtVfob.setColumns(8);

		JLabel lblVfob = new JLabel("VFOB");
		GridBagConstraints gbc_lblVfob = new GridBagConstraints();
		gbc_lblVfob.anchor = GridBagConstraints.WEST;
		gbc_lblVfob.insets = new Insets(0, 0, 5, 5);
		gbc_lblVfob.gridx = 1;
		gbc_lblVfob.gridy = 4;
		panel.add(lblVfob, gbc_lblVfob);
		GridBagConstraints gbc_tglbtnPtt = new GridBagConstraints();
		gbc_tglbtnPtt.insets = new Insets(0, 0, 5, 0);
		gbc_tglbtnPtt.gridx = 2;
		gbc_tglbtnPtt.gridy = 4;
		panel.add(tglbtnPtt, gbc_tglbtnPtt);
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
		gbc_wpm.gridy = 5;
		panel.add(wpm, gbc_wpm);

		// intialise the dials
		int pwr = 4; // = ic7000.getCATInteger("rig.get_power");
		power.setValue(pwr);

		JLabel lblNewLabel_2 = new JLabel("WPM");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.LINE_START;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 5;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				JSpinner Temp = (JSpinner) e.getSource();

				int value = (int) Temp.getValue();
				if (qmx != null) {
					System.out.println("CatResp: " + qmx.sendCatStringmain("KS" + value));
					wpm.setValue(value);
					System.out.println("WPM" + value);
				}
			}
		});
		spinner.setModel(new SpinnerNumberModel(Integer.valueOf(20), Integer.valueOf(12), Integer.valueOf(30),
				Integer.valueOf(1)));
		spinner.setToolTipText("Change the thing");
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 2;
		gbc_spinner.gridy = 5;
		panel.add(spinner, gbc_spinner);

		pwrt = new JTextField();
		GridBagConstraints gbc_pwrt = new GridBagConstraints();
		gbc_pwrt.anchor = GridBagConstraints.EAST;
		gbc_pwrt.insets = new Insets(0, 0, 5, 5);
		gbc_pwrt.gridx = 0;
		gbc_pwrt.gridy = 6;
		panel.add(pwrt, gbc_pwrt);
		pwrt.setColumns(4);

		JLabel lblPwr = new JLabel("PWR");
		GridBagConstraints gbc_lblPwr = new GridBagConstraints();
		gbc_lblPwr.anchor = GridBagConstraints.LINE_START;
		gbc_lblPwr.insets = new Insets(0, 0, 5, 5);
		gbc_lblPwr.gridx = 1;
		gbc_lblPwr.gridy = 6;
		panel.add(lblPwr, gbc_lblPwr);

		swrt = new JTextField();
		GridBagConstraints gbc_swrt = new GridBagConstraints();
		gbc_swrt.anchor = GridBagConstraints.EAST;
		gbc_swrt.insets = new Insets(0, 0, 5, 5);
		gbc_swrt.gridx = 0;
		gbc_swrt.gridy = 7;
		panel.add(swrt, gbc_swrt);
		swrt.setColumns(4);

		JLabel lblSwr = new JLabel("SWR");
		GridBagConstraints gbc_lblSwr = new GridBagConstraints();
		gbc_lblSwr.anchor = GridBagConstraints.LINE_START;
		gbc_lblSwr.insets = new Insets(0, 0, 5, 5);
		gbc_lblSwr.gridx = 1;
		gbc_lblSwr.gridy = 7;
		panel.add(lblSwr, gbc_lblSwr);

		JCheckBox chckbxTx = new JCheckBox("TX");
		chckbxTx.setEnabled(false);
		//chckbxTx.addItemListener(new ItemListener() {
		//	public void itemStateChanged(ItemEvent ev) {
		//		if (ev.getStateChange() == ItemEvent.SELECTED) {
		//			System.out.println("PTT on" + qmx.sendCatStringmain("TQ1"));
		//		} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
		//			System.out.println("PTT off" + qmx.sendCatStringmain("TQ0"));
		//		}
		//	}
		//});

		GridBagConstraints gbc_chckbxTx = new GridBagConstraints();
		gbc_chckbxTx.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxTx.gridx = 2;
		gbc_chckbxTx.gridy = 7;
		panel.add(chckbxTx, gbc_chckbxTx);
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		GridBagConstraints gbc_statusPanel = new GridBagConstraints();
		gbc_statusPanel.fill = GridBagConstraints.BOTH;
		gbc_statusPanel.insets = new Insets(0, 0, 0, 5);
		gbc_statusPanel.gridx = 0;
		gbc_statusPanel.gridy = 8;
		panel.add(statusPanel, gbc_statusPanel);
		GridBagLayout gbl_statusPanel = new GridBagLayout();
		gbl_statusPanel.columnWidths = new int[]{0, 0};
		gbl_statusPanel.rowHeights = new int[]{0, 0};
		gbl_statusPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_statusPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		statusPanel.setLayout(gbl_statusPanel);
		
		JLabel statusLabel = new JLabel("Status");
		GridBagConstraints gbc_statusLabel = new GridBagConstraints();
		gbc_statusLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_statusLabel.gridx = 0;
		gbc_statusLabel.gridy = 0;
		statusPanel.add(statusLabel, gbc_statusLabel);
		if (debug)
			System.out.println("Timer Fired, pwer:" + pwr);

		// set up the timer
		//
		Timer timer = new Timer(100, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				statusLabel.setText(qmx.sendCatStringmain("IF") );
				// TX state
				isTx = (qmx.getIntFromString(qmx.sendCatStringmain("TQ"))) > 0 ? true : false;
				chckbxTx.setSelected(isTx);
				// currentn mode
				thisMode = qmx.getIntFromString(qmx.sendCatStringmain("MD"));
				spinnerMode.setValue( qmx.getModeString(thisMode));
				// current WPM
				thisWpm = qmx.getIntFromString(qmx.sendCatStringmain("KS"));
				wpm.setValue(thisWpm);
				// signal strenght
				sig = qmx.getIntFromString(qmx.sendCatStringmain("SM"));
				power.setValue(sig);
				lcdText.setText(qmx.sendCatStringmain("LC").replaceAll("(LC|;)", ""));
				agc = qmx.getIntFromString(qmx.sendCatStringmain("SA"));
				pc = qmx.getIntFromString(qmx.sendCatStringmain("PC"));
				sw = qmx.getIntFromString(qmx.sendCatStringmain("SW"));
				// display the VFO contents
				txtVfoa.setText(Integer.toString(qmx.getIntFromString(qmx.sendCatStringmain("FA"))));
				txtVfob.setText(Integer.toString(qmx.getIntFromString(qmx.sendCatStringmain("FB"))));
				qmx.sendCatStringmain("FB");

				if (oldVal1 != (pc + sw)) {
					System.out.println("CatResp RX Sig: " + sig + "dB" + " agc/pc/sw " + agc + "/" + pc + "/" + sw);
					pwrt.setText(Float.toString((float) ((float) pc / 10.0)));
					swrt.setText(Float.toString((float) ((float) sw / 100.0)));
					oldVal1 = (pc + sw);
				}

			}
		});
		timer.setRepeats(true);
		timer.start();
	}

}
