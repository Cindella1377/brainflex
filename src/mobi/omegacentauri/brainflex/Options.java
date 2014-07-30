package mobi.omegacentauri.brainflex;

import java.awt.Checkbox;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.TextEvent;
import java.awt.event.TextListener;
import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

public class Options extends JFrame {
	private static final long serialVersionUID = 4138326933073169336L;
	Preferences prefs;
	protected File saveFile;
	static final String BINEXT = ".thg";
	
	public Options() {
		super();
		
		saveFile = null;
		
		prefs = Preferences.userNodeForPackage(BrainFlex.class);
		
		setLocationByPlatform(true);
		
		setTitle("BrainFlex Options");
		setSize(640,200);

		Container pane = getContentPane();
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		
		Box comPortBox = new Box(BoxLayout.X_AXIS);
//		comPortPanel.setLayout(new BoxLayout(comPortPanel, BoxLayout.X_AXIS));

		final JComboBox<String> inMode = new JComboBox<String>();
		inMode.addItem("Serial port");
		inMode.addItem("Saved file");

//		JLabel label = new JLabel("Serial port: ");
		comPortBox.add(inMode);

		final TextField comPortField = new TextField(prefs.get(BrainFlex.PREF_SERIAL_PORT, ""));
		comPortField.selectAll();
		Dimension m = comPortField.getMaximumSize();
		m.height = inMode.getMaximumSize().height;
		comPortField.setMaximumSize(m);
		comPortBox.add(comPortField);
		
		inMode.setSelectedIndex(prefs.getBoolean(BrainFlex.PREF_FILE_MODE, false) ? 1 : 0);
		comPortField.addTextListener(new TextListener() {
			
			@Override
			public void textValueChanged(TextEvent arg0) {
				prefs.put(BrainFlex.PREF_SERIAL_PORT, comPortField.getText());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
			}
		});
		
		final JLabel notes = new JLabel(" ", SwingConstants.LEFT);

		final Checkbox rawCheck = new Checkbox("Raw data window", prefs.getBoolean(BrainFlex.PREF_RAW, true));
		
		final Checkbox powerCheck = new Checkbox("Processed data window", prefs.getBoolean(BrainFlex.PREF_POWER, true));
		
		rawCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_RAW, rawCheck.getState());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
				updateNotes(notes,rawCheck,powerCheck);
			}
		});
		
		powerCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_POWER, powerCheck.getState());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
				updateNotes(notes,rawCheck,powerCheck);
			}
		});

		updateNotes(notes,rawCheck,powerCheck);

		final Checkbox logCheck = new Checkbox("Log window", prefs.getBoolean(BrainFlex.PREF_LOG_WINDOW, true));
		
		logCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_LOG_WINDOW, logCheck.getState());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
			}
		});
		
		final Checkbox customFWCheck = new Checkbox("Custom BrainLink firmware (github.com/arpruss/custom-brainlink-firmware)", 
				prefs.getBoolean(BrainFlex.PREF_CUSTOM_FW, false));
		
		customFWCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_CUSTOM_FW, customFWCheck.getState());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
			}
		});
		
		rawCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_POWER, powerCheck.getState());
				try {
					prefs.flush();
				} catch (BackingStoreException e) {
				}
			}
		});

		final Checkbox saveBinaryCheck = new Checkbox("Save binary data", prefs.getBoolean(BrainFlex.PREF_SAVE_BINARY, false));
		
		saveBinaryCheck.setState(prefs.getBoolean(BrainFlex.PREF_SAVE_BINARY, false));
		
		saveBinaryCheck.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_SAVE_BINARY, saveBinaryCheck.getState());
				flushPrefs();
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		
		final JButton go = new JButton("Go");

		go.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if ( powerCheck.getState() || rawCheck.getState()) {
					if (inMode.getSelectedIndex() == 0) {
						if (comPortField.getText() == null ||
								comPortField.getText().length() == 0)
							return;
						
						if (prefs.getBoolean(BrainFlex.PREF_SAVE_BINARY, false)) {
							final JFileChooser fc = new JFileChooser();
							fc.setFileFilter(new FileFilter() {

								@Override
								public boolean accept(File arg0) {
									if (arg0.isDirectory())
										return false;
									return arg0.getName().endsWith(BINEXT);
								}

								@Override
								public String getDescription() {
									return "*"+BINEXT;
								}});
							
							if (fc.showSaveDialog(go) == JFileChooser.APPROVE_OPTION) {
								String n = fc.getSelectedFile().getPath();
								if (! n.endsWith(BINEXT))
									n += BINEXT;
								Options.this.saveFile = new File(n);
							}
							else {
								return;
							}
						}
						
						Options.this.dispose();
						new BrainFlex(saveFile);
					}
					else {
						final JFileChooser fc = new JFileChooser();
						fc.setSelectedFile(new File(prefs.get(BrainFlex.PREF_FILE_NAME, null)));
						fc.setFileFilter(new FileFilter() {

							@Override
							public boolean accept(File arg0) {
								if (arg0.isDirectory())
									return false;
								return arg0.getName().endsWith(BINEXT);
							}

							@Override
							public String getDescription() {
								return "*"+BINEXT;
							}});
						
						if (fc.showOpenDialog(go) == JFileChooser.APPROVE_OPTION) {
							String n = fc.getSelectedFile().getPath();
							if (! n.endsWith(BINEXT))
								n += BINEXT;
							prefs.put(BrainFlex.PREF_FILE_NAME, n);
							flushPrefs();
							Options.this.dispose();
							new BrainFlex(null);
						}						
					}
				}
			}
		});
		
		JRootPane root = getRootPane();
		root.setDefaultButton(go);
		
		buttonPanel.add(go);
				
		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Options.this.dispose();
			}
		});
		
		buttonPanel.add(cancel);
		buttonPanel.add(notes);
		
		pane.add(comPortBox);
		pane.add(rawCheck);
		pane.add(powerCheck);
		pane.add(logCheck);
		pane.add(customFWCheck);
		pane.add(saveBinaryCheck);
		pane.add(buttonPanel);
		
		if (prefs.getBoolean(BrainFlex.PREF_FILE_MODE, false)) {
			comPortField.setEnabled(false);
			inMode.setSelectedIndex(1);
			saveBinaryCheck.setEnabled(false);
		}
		else {
			comPortField.setEnabled(true);
			inMode.setSelectedIndex(0);
			saveBinaryCheck.setEnabled(true);
		}
			
		inMode.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				prefs.putBoolean(BrainFlex.PREF_FILE_MODE, inMode.getSelectedIndex() == 1);
				flushPrefs();
				comPortField.setEnabled(inMode.getSelectedIndex() != 1);
				saveBinaryCheck.setEnabled(inMode.getSelectedIndex() == 1);
			}
		});
		
		setVisible(true);
	}

	protected void updateNotes(JLabel notes, Checkbox rawCheck,
			Checkbox powerCheck) {
		if (!rawCheck.getState() && !powerCheck.getState()) {
			notes.setText("At least one data window needs to be active.");
		}
		else {
			notes.setText("");
		}
	}
	
	public void flushPrefs() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
		}
	}


}

