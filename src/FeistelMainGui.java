import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.FileNotFoundException;

import java.awt.Font;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.ComponentOrientation;
import java.awt.event.ComponentAdapter;
import java.util.Base64;
import java.util.Random;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.border.CompoundBorder;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

/**
 * The main class that runs and handles the Feistel algorithm class.
 * 
 * Happy cow says: "Muuuuuuu.."
 * 
 * @author Ben Sabah.
 */

public class FeistelMainGui extends JFrame {
	private static final long serialVersionUID = -5259343195128817704L;
	static Random rnd;
	static JPanel panel = new JPanel();
	static GuiUtils.ClipboardHandler clipboard;

	// ViewA GUI Components
	static JButton plainTextButton;
	static JButton cipherTextButton;
	static JButton keyButton;
	static JButton encryptButton;
	static JButton decryptButton;
	static JButton verifyButton;
	static JTextField plainTextField;
	static JTextField cipherTextField;
	static JTextField keyField;
	static Point viewASize = new Point(435, 235);

	// ViewB GUI components.
	static JButton OTFplainTextButton;
	static JButton OTFcipherTextButton;
	static JButton OTFkeyButton;
	static JTextArea OTFPlainText;
	static JTextField OTFCipherText;
	static JTextField OTFKeyText;
	static JScrollPane OTFPlainTextFrame;
	static Point viewBSize = new Point(445, 245);

	// The fields for the view switching setting.
	static boolean isViewA = false;
	static JButton viewSwitcherButton;
	static LinkedList<JComponent> viewA = new LinkedList<JComponent>();
	static LinkedList<JComponent> viewB = new LinkedList<JComponent>();

	// The files-handlers fields.
	static File plainTextFile;
	static File cipherTextFile;
	static File keyFile;
	private static RandomAccessFile plainTextRAF;
	private static RandomAccessFile cipherTextRFA;
	private static RandomAccessFile keyRFA;
	private static RandomAccessFile configRFA;

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Feistel.plainTextPath = "p.txt";
		Feistel.cipherTextPath = "c.txt";
		Feistel.keyPath = "k.txt";

		// Automatic runner, used when all the parameters are given.
		if (args.length == 4) {
			automaticRunner(args[0], args[1], args[2], args[3]);
			return;
		}

		// Semi-automatic runner, used when all the necessary files are given
		// but not the desired action
		if (args.length == 3) {
			Feistel.plainTextPath = args[0];
			Feistel.cipherTextPath = args[1];
			Feistel.keyPath = args[2];
		}

		plainTextFile = new File(Feistel.plainTextPath);
		cipherTextFile = new File(Feistel.cipherTextPath);
		keyFile = new File(Feistel.keyPath);

		// Start the accessory objects.
		rnd = new Random();
		clipboard = new GuiUtils.ClipboardHandler();

		// Start the GUI window.
		FeistelMainGui gui = new FeistelMainGui();
	}

	private static void automaticRunner(String textPath, String cipherPath, String keyPath,
			String configPath) {
		Feistel.plainTextPath = textPath;
		Feistel.cipherTextPath = cipherPath;
		Feistel.keyPath = keyPath;
		Feistel.configPath = configPath;

		try {
			// Start all the file-handlers for each of the required files.
			plainTextRAF = new RandomAccessFile(Feistel.plainTextPath, "rw");
			cipherTextRFA = new RandomAccessFile(Feistel.cipherTextPath, "rw");
			keyRFA = new RandomAccessFile(Feistel.keyPath, "r");
			configRFA = new RandomAccessFile(Feistel.configPath, "r");

			// Read the key and & mode from the files and close the streams.
			keyRFA.read(Feistel.key);
			keyRFA.close();
			String config = configRFA.readLine().toLowerCase();
			configRFA.close();

			// Select and run the requested mode.
			switch (config) {
			case "encrypt":
				Feistel.encryptFile(cipherTextRFA);
				GuiUtils.PopUpMessages.encMsg(true);
				break;
			case "decrypt":
				Feistel.decryptFile(plainTextRAF);
				GuiUtils.PopUpMessages.decMsg(true);
				break;
			case "verify":
				boolean verification = Feistel.verify();
				GuiUtils.PopUpMessages.verifyMsg(verification);
				break;
			default:
				GuiUtils.PopUpMessages.errorMsg("Wrong configuration.");
				break;
			}

			// Handle any errors the program might encounter
		} catch (FileNotFoundException e) {
			GuiUtils.PopUpMessages.errorMsg("Some file(s) doesn't exist");
		} catch (IOException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't read from file");
		} catch (NullPointerException e) {
			GuiUtils.PopUpMessages.errorMsg("Your file(s) are empty");
		} catch (IllegalArgumentException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
		} catch (Exception e) {
			// If we still have an unknown error this should catch it.
			System.err.println("[Error] " + e.getMessage());
			GuiUtils.PopUpMessages.errorMsg(e.getMessage());
		} finally {
			// Close all the the files streams.
			try {
				if (plainTextRAF != null)
					plainTextRAF.close();
				if (cipherTextRFA != null)
					cipherTextRFA.close();
				if (keyRFA != null)
					keyRFA.close();
				if (configRFA != null)
					configRFA.close();
			} catch (IOException e) {
				GuiUtils.PopUpMessages.errorMsg("Can't close one or more file(s)");
			}
		}
	}

	private void resizeViewBComponentsBy(int addedWidth, int addedHeight) {
		// Resize and position the buttons.
		OTFplainTextButton.setSize(80, 120 + addedHeight);
		OTFkeyButton.setLocation(10, 139 + addedHeight);
		OTFcipherTextButton.setLocation(10, 174 + addedHeight);

		// Resize and position the input fields.
		OTFPlainTextFrame.setSize(320 + addedWidth, 120 + addedHeight);
		OTFPlainText.setSize(320 + addedWidth, 120 + addedHeight);
		OTFKeyText.setLocation(100, 140 + addedHeight);
		OTFKeyText.setSize(320 + addedWidth, 25);
		OTFCipherText.setLocation(100, 175 + addedHeight);
		OTFCipherText.setSize(320 + addedWidth, 25);
	}

	private FeistelMainGui() {
		// Setup the style.
		try {
			GuiUtils.setWinSevenStyle();
		} catch (GuiUtils.GuiException e) {
			GuiUtils.PopUpMessages.errorMsg("can't display in Win7 style!");
		}

		// Adding all buttons and fields.
		setupViewA();
		setupViewB();
		setupViewSwitcher();
		addButtonsToViews();

		// Setup and center the window.
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation((GuiUtils.getScreenWidth() - getWidth()) / 2,
				(GuiUtils.getsScreenHeight() - getHeight()) / 2);

		// Starting the window.
		panel = new JPanel();
		add(panel);
		setVisible(true);

		addComponentListener(new ComponentAdapter() {
			int AddedWidth;
			int AddedHeight;

			public void componentResized(ComponentEvent evt) {
				AddedWidth = getWidth() - viewBSize.x;
				AddedHeight = getHeight() - viewBSize.y;
				resizeViewBComponentsBy(AddedWidth, AddedHeight);
			}
		});
	}

	private void viewSwitcher() {
		// We changed view setting.
		isViewA = !isViewA;

		// Set resizing and sizes
		setResizable(!isViewA);
		if (isViewA) {
			setMinimumSize(null);
			setSize(viewASize.x, viewASize.y);
		} else {
			setMinimumSize(new Dimension(viewBSize.x, viewBSize.y));
			setSize(viewBSize.x, viewBSize.y);
		}

		// Set the window title.
		setTitle(getGuiTitle(isViewA));

		// Reversing the 2 different views.
		for (JComponent curComponent : viewA) {
			curComponent.setVisible(isViewA);
		}
		for (JComponent curComponent : viewB) {
			curComponent.setVisible(!isViewA);
		}

		// Change switcher tool-tip.
		viewSwitcherButton.setToolTipText("switch to > " + getGuiTitle(!isViewA));
	}

	private void addButtonsToViews() {
		// Add all viewA components.
		viewA.add(plainTextButton);
		plainTextButton.setVisible(isViewA);
		viewA.add(cipherTextButton);
		cipherTextButton.setVisible(isViewA);
		viewA.add(keyButton);
		keyButton.setVisible(isViewA);
		viewA.add(encryptButton);
		encryptButton.setVisible(isViewA);
		viewA.add(decryptButton);
		decryptButton.setVisible(isViewA);
		viewA.add(verifyButton);
		verifyButton.setVisible(isViewA);
		viewA.add(plainTextField);
		plainTextField.setVisible(isViewA);
		viewA.add(cipherTextField);
		cipherTextField.setVisible(isViewA);
		viewA.add(keyField);
		keyField.setVisible(isViewA);

		// Add all viewB components.
		viewB.add(OTFPlainTextFrame);
		OTFPlainTextFrame.setVisible(!isViewA);
		viewB.add(OTFKeyText);
		OTFKeyText.setVisible(!isViewA);
		viewB.add(OTFCipherText);
		OTFCipherText.setVisible(!isViewA);
		viewB.add(OTFplainTextButton);
		OTFplainTextButton.setVisible(!isViewA);
		viewB.add(OTFkeyButton);
		OTFkeyButton.setVisible(!isViewA);
		viewB.add(OTFcipherTextButton);
		OTFcipherTextButton.setVisible(!isViewA);
	}

	private void setupViewSwitcher() {
		// Setting the view switcher.
		viewSwitcherButton = new JButton("<html><center><H6>@</H6></center></html>");
		viewSwitcherButton.setLocation(0, 0);
		viewSwitcherButton.setSize(13, 13);
		viewSwitcherButton.setMargin(new Insets(0, 0, 0, 0));
		viewSwitcherButton.setToolTipText("switch to > " + getGuiTitle(!isViewA));
		viewSwitcherButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				viewSwitcher();
			}
		});
		add(viewSwitcherButton);
	}

	private void setupViewA() {
		setSize(viewASize.x, viewASize.y);
		setResizable(false);

		// Setup original title.
		setTitle(getGuiTitle(isViewA));

		// Setting the plain-text loading button.
		plainTextButton = new JButton("Plain-Text file:");
		plainTextButton.setLocation(10, 10);
		plainTextButton.setSize(150, 30);
		plainTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					plainTextFile = GuiUtils.fileSelector();
					plainTextField.setText(plainTextFile.getAbsolutePath());
					Feistel.plainTextPath = plainTextFile.getAbsolutePath();
				} catch (Exception e) {
				}
			}
		});

		// Setting the cipher-text loading button.
		cipherTextButton = new JButton("Cipher-Text file:");
		cipherTextButton.setLocation(10, 50);
		cipherTextButton.setSize(150, 30);
		cipherTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					cipherTextFile = GuiUtils.fileSelector();
					cipherTextField.setText(cipherTextFile.getAbsolutePath());
					Feistel.cipherTextPath = cipherTextFile.getAbsolutePath();
				} catch (Exception e) {
				}
			}
		});

		// Setting the key loading button.
		keyButton = new JButton("Key file:");
		keyButton.setLocation(10, 90);
		keyButton.setSize(150, 30);
		keyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					keyFile = GuiUtils.fileSelector();
					keyField.setText(keyFile.getAbsolutePath());
					Feistel.keyPath = keyFile.getAbsolutePath();
				} catch (Exception e) {
				}
			}
		});

		// Setting the ENCRYPT button.
		encryptButton = new JButton("ENCRYPT");
		encryptButton.setLocation(10, 130);
		encryptButton.setSize(120, 70);
		encryptButton.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(0,
				0, 0, 0)));
		encryptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				encrypt();
			}

		});

		// Setting the DECRYPT button.
		decryptButton = new JButton("DECRYPT");
		decryptButton.setSize(120, 70);
		decryptButton.setLocation(155, 130);
		decryptButton.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(0,
				0, 0, 0)));
		decryptButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				decrypt();
			}
		});

		// Setting the VERIFY button.
		verifyButton = new JButton("VERIFY");
		verifyButton.setLocation(300, 130);
		verifyButton.setSize(120, 70);
		verifyButton.setBorder(new CompoundBorder(new LineBorder(Color.BLACK), new EmptyBorder(0,
				0, 0, 0)));
		verifyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				verify();
			}
		});

		// Setting the plain-text field.
		plainTextField = new JTextField(Feistel.plainTextPath);
		plainTextField.setLocation(170, 13);
		plainTextField.setSize(250, 25);
		plainTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Feistel.plainTextPath = plainTextField.getText();
			}
		});

		// Setting the cipher-text field.
		cipherTextField = new JTextField(Feistel.cipherTextPath);
		cipherTextField.setLocation(170, 53);
		cipherTextField.setSize(250, 25);
		cipherTextField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Feistel.cipherTextPath = cipherTextField.getText();
			}
		});

		// Setting the key field.
		keyField = new JTextField(Feistel.keyPath);
		keyField.setLocation(170, 93);
		keyField.setSize(250, 25);
		keyField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Feistel.keyPath = keyField.getText();
			}
		});

		// Adding all the buttons & fields.
		add(plainTextButton);
		add(cipherTextButton);
		add(keyButton);
		add(encryptButton);
		add(decryptButton);
		add(verifyButton);
		add(plainTextField);
		add(cipherTextField);
		add(keyField);

	}

	private void setupViewB() {
		setResizable(!isViewA);
		setMinimumSize(new Dimension(viewBSize.x, viewBSize.y));

		// Setup original title.
		setTitle(getGuiTitle(isViewA));

		String buttonName;

		// Setting the plain-text loading button.
		buttonName = "<html><center><H4>type text:</H4><br><H5>or<br>click-to-paste</H5></center></html>";
		OTFplainTextButton = new JButton(buttonName);
		OTFplainTextButton.setLocation(10, 10);
		OTFplainTextButton.setSize(80, 120);
		OTFplainTextButton.setMargin(new Insets(0, 0, 0, 0));
		OTFplainTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					OTFPlainText.setText(clipboard.getClipboardContents());
				} catch (GuiUtils.GuiException e) {
					GuiUtils.PopUpMessages.errorMsg("Can't place text to clipboard.");
				}
			}
		});

		// Setting the random key button.
		buttonName = "<html><center><H9>type key:<br>click-for-random</H9></center></html>";
		OTFkeyButton = new JButton(buttonName);
		OTFkeyButton.setLocation(10, 139);
		OTFkeyButton.setSize(80, 27);
		OTFkeyButton.setMargin(new Insets(0, 0, 0, 0));
		OTFkeyButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent event) {
				byte[] randomBytes = new byte[6];
				rnd.nextBytes(randomBytes);

				try {
					randomBytes = Base64.getEncoder().encode(randomBytes);
					OTFKeyText.setText(new String(randomBytes));
					if (OTFPlainText.getText().isEmpty()) {
						OTFCipherText.setText("");
					} else {
						String result = onTheFlyEncoding(OTFPlainText.getText(),
								OTFKeyText.getText());
						OTFCipherText.setText(result);
						result = null;
					}
				} catch (IllegalArgumentException e) {
					GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
				} catch (Exception e) {
					GuiUtils.PopUpMessages.errorMsg("ERROR (103): " + e.getMessage());
				}
			}
		});

		// Setting the cipher-text loading button.
		buttonName = "<html><center><H9>cipher text:<br>click-to-copy</H9></center></html>";
		OTFcipherTextButton = new JButton(buttonName);
		OTFcipherTextButton.setLocation(10, 174);
		OTFcipherTextButton.setSize(80, 27);
		OTFcipherTextButton.setMargin(new Insets(0, 0, 0, 0));
		OTFcipherTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				clipboard.setClipboardContents(OTFCipherText.getText());
			}
		});

		// Setting the plain-text box.
		OTFPlainText = new JTextArea();
		OTFPlainText.setLineWrap(true);
		OTFPlainTextFrame = new JScrollPane(OTFPlainText);
		OTFPlainTextFrame.setLocation(100, 10);
		OTFPlainTextFrame.setSize(320, 120);
		Font orig = OTFPlainText.getFont();
		OTFPlainText.setFont(new Font(orig.getFontName(), orig.getStyle(), 12));
		OTFPlainText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				// Check if CTRL+SHIFT were pressed.
				if (e.isControlDown() && e.isShiftDown()) {
					// Check for right or CTRL+SHIFT combo
					if (e.getKeyLocation() == 3) {
						OTFPlainText.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
						OTFPlainTextFrame
								.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
					} else if (e.getKeyLocation() == 2) {
						OTFPlainText.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
						OTFPlainTextFrame
								.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
					}

				}
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

		});
		OTFPlainText.getDocument().addDocumentListener(new DocumentListener() {
			private void update() {
				try {
					String result = onTheFlyEncoding(OTFPlainText.getText(), OTFKeyText.getText());
					OTFCipherText.setText(result);
					result = null;
				} catch (IllegalArgumentException e) {
					GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
				} catch (Exception e) {
					GuiUtils.PopUpMessages.errorMsg("ERROR (104): " + e.getMessage());
				}
			}

			public void insertUpdate(DocumentEvent e) {
				update();
			}

			public void removeUpdate(DocumentEvent e) {
				if (OTFPlainText.getText().isEmpty()) {
					OTFCipherText.setText("");
				} else {
					update();
				}
			}

			public void changedUpdate(DocumentEvent e) {
				if (OTFPlainText.getText().length() == 0) {
					OTFCipherText.setText("");
				} else {
					update();
				}
			}
		});

		// Setting the key field.
		OTFKeyText = new JTextField("Temp_Key");
		OTFKeyText.setLocation(100, 140);
		OTFKeyText.setSize(320, 25);
		OTFKeyText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					if (OTFKeyText.getText().toCharArray().length > 8) {
						OTFKeyText.setText(OTFKeyText.getText(0, 8));
					}
					if (OTFPlainText.getText().isEmpty()) {
						OTFCipherText.setText("");
					} else {
						String result = onTheFlyEncoding(OTFPlainText.getText(),
								OTFKeyText.getText());
						OTFCipherText.setText(result);
						result = null;
					}
				} catch (BadLocationException e) {
				} catch (IllegalArgumentException e) {
					GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
				} catch (Exception e) {
					GuiUtils.PopUpMessages.errorMsg("ERROR (105): " + e.getMessage());
				}
			}
		});

		// Setting the cipher-text field.
		OTFCipherText = new JTextField();
		OTFCipherText.setLocation(100, 175);
		OTFCipherText.setSize(320, 25);
		OTFCipherText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					String result = onTheFlyDecoding(OTFCipherText.getText(), OTFKeyText.getText());
					OTFPlainText.setText(result);
				} catch (IllegalArgumentException e) {
					GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
				} catch (Exception e) {
					GuiUtils.PopUpMessages.errorMsg("ERROR (106): " + e.getMessage());
				}
			}
		});

		// Adding all the buttons & fields.
		add(OTFPlainTextFrame);
		add(OTFKeyText);
		add(OTFCipherText);
		add(OTFplainTextButton);
		add(OTFkeyButton);
		add(OTFcipherTextButton);
	}

	private void encrypt() {
		try {
			updateFeistelFields();
			Feistel.encryptFile(cipherTextRFA);
			GuiUtils.PopUpMessages.encMsg(true);

		} catch (FileNotFoundException e) {
			GuiUtils.PopUpMessages.errorMsg("Some file(s) doesn't exist");
		} catch (IOException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't read from file");
		} catch (NullPointerException e) {
			GuiUtils.PopUpMessages.errorMsg("Your file(s) are empty");
		} catch (IllegalArgumentException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
		} catch (Exception e) {
			GuiUtils.PopUpMessages.errorMsg("ERROR (100): " + e.getMessage());
		} finally {
			try {
				closingStreams();
			} catch (IOException e) {
				GuiUtils.PopUpMessages.errorMsg("Can't read from file");
			}
		}
	}

	private void decrypt() {
		try {
			updateFeistelFields();
			Feistel.decryptFile(plainTextRAF);
			GuiUtils.PopUpMessages.decMsg(true);

		} catch (FileNotFoundException e) {
			GuiUtils.PopUpMessages.errorMsg("Some file(s) doesn't exist");
		} catch (IOException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't read from file");
		} catch (NullPointerException e) {
			GuiUtils.PopUpMessages.errorMsg("Your file(s) are empty");
		} catch (IllegalArgumentException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
		} catch (Exception e) {
			GuiUtils.PopUpMessages.errorMsg("ERROR (101): " + e.getMessage());
		} finally {
			try {
				closingStreams();
			} catch (IOException e) {
				GuiUtils.PopUpMessages.errorMsg("Can't read from file");
			}
		}
	}

	private void verify() {
		try {
			boolean result = Feistel.verify();
			GuiUtils.PopUpMessages.verifyMsg(result);

		} catch (FileNotFoundException e) {
			GuiUtils.PopUpMessages.errorMsg("Some file(s) doesn't exist");
		} catch (IOException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't read from file");
		} catch (NullPointerException e) {
			GuiUtils.PopUpMessages.errorMsg("Your file(s) are empty");
		} catch (IllegalArgumentException e) {
			GuiUtils.PopUpMessages.errorMsg("Can't translate cipher-text to radix64");
		} catch (Exception e) {
			// If we still have an unknown error this should catch it.
			GuiUtils.PopUpMessages.errorMsg("ERROR (102): " + e.getMessage());
		} finally {
			try {
				closingStreams();
			} catch (IOException e) {
				GuiUtils.PopUpMessages.errorMsg("Can't read from file");
			}
		}
	}

	private void updateFeistelFields() throws FileNotFoundException, IOException {
		Feistel.plainTextPath = plainTextFile.getAbsolutePath();
		Feistel.cipherTextPath = cipherTextFile.getAbsolutePath();
		Feistel.keyPath = keyFile.getAbsolutePath();
		closingStreams();
		plainTextRAF = new RandomAccessFile(plainTextFile, "rw");
		cipherTextRFA = new RandomAccessFile(cipherTextFile, "rw");
		keyRFA = new RandomAccessFile(keyFile, "r");
		keyRFA.read(Feistel.key);
		keyRFA.close();
	}

	private void closingStreams() throws IOException {
		if (plainTextRAF != null) {
			plainTextRAF.close();
		}
		if (cipherTextRFA != null) {
			cipherTextRFA.close();
		}
		if (keyRFA != null) {
			keyRFA.close();
		}
	}

	private static String onTheFlyEncoding(String input, String key) {
		byte[] plainTextBytes = input.getBytes();
		byte[] keyBytes = key.getBytes();

		// In case the key is less than 64 bits long.
		if (keyBytes.length < 8) {
			keyBytes = new byte[8];
			System.arraycopy(key.getBytes(), 0, keyBytes, 0, key.getBytes().length);
		}

		// Encrypt the text.
		byte[] cipherTextBytes = Feistel.encrypt(plainTextBytes, keyBytes);

		// Convert the resulting byte to base64 text.
		cipherTextBytes = Base64.getEncoder().encode(cipherTextBytes);

		return new String(cipherTextBytes);
	}

	private static String onTheFlyDecoding(String input, String key)
			throws IllegalArgumentException {
		byte[] cipherTextBytes = input.getBytes();
		byte[] keyBytes = key.getBytes();

		// In case the key is less than 64 bits long.
		if (keyBytes.length < 8) {
			keyBytes = new byte[8];
			System.arraycopy(key.getBytes(), 0, keyBytes, 0, key.getBytes().length);
		}

		// Convert the resulting byte to base64 text.
		cipherTextBytes = Base64.getDecoder().decode(cipherTextBytes);

		// Decipher the text.
		byte[] plainTextBytes = Feistel.decrypt(cipherTextBytes, keyBytes);

		// Return the deciphered text.
		return new String(plainTextBytes);
	}

	private static String getGuiTitle(boolean state) {
		return "Feistel " + (state ? "File" : "LIVE") + " Encrypter-Decrypter";
	}
}
