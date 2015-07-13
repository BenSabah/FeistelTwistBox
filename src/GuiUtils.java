import java.io.File;
import java.io.IOException;

import javax.swing.UIManager;
import javax.swing.JOptionPane;
import javax.swing.JFileChooser;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * This class handles all the GUI of the program that we want to display to the
 * user while running the Feistel program.
 * 
 * Happy cow says: "Muuuuuuu.."
 * 
 * @author Ben Sabah.
 */
class GuiUtils {

	/**
	 * Calling this method opens the file selector window, and returns the
	 * selected file as a File object.
	 * 
	 * @return The File object of the selected file.
	 */
	static File fileSelector() {
		JFileChooser fc = new JFileChooser();
		fc.showOpenDialog(null);
		return fc.getSelectedFile();
	}

	/**
	 * Calling this method tries to change the style of the JFrame that invoked
	 * it to a Win7 style.
	 * 
	 * @throws GuiException
	 */
	static void setWinSevenStyle() throws GuiException {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			throw new GuiException();
		}
	}

	/**
	 * This method returns the working screen width.
	 * 
	 * @return The width of the working screen.
	 */
	static int getScreenWidth() {
		return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	}

	/**
	 * This method returns the working screen height.
	 * 
	 * @return The height of the working screen.
	 */
	static int getsScreenHeight() {
		return java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	}

	/**
	 * Handle all pop-up messages.
	 */
	static class PopUpMessages {
		static final int EMPTY = -1;
		static final int FAIL = 0;
		static final int SUCCESS = 1;
		static final int ERROR = 2;
		static final int QUESTION = 3;

		private static void rawMsg(String msg, String title, int type) {
			java.awt.Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null, msg, title, type);
		}

		static void errorMsg(String msg) {
			rawMsg(msg, "Error !", ERROR);
		}

		static void encMsg(boolean isSuccessful) {
			String msg = (isSuccessful) ? "Encrypted successfully." : "Encryption failed.";
			rawMsg(msg, "Encryption:", (isSuccessful) ? SUCCESS : FAIL);
		}

		static void decMsg(boolean isSuccessful) {
			String msg = (isSuccessful) ? "Decrypted successfully." : "Decryption failed.";
			rawMsg(msg, "Decryption:", (isSuccessful) ? SUCCESS : FAIL);
		}

		static void verifyMsg(boolean isSuccessful) {
			String msg = (isSuccessful) ? "Verification Passed." : "Verification failed.";
			rawMsg(msg, "Verification:", (isSuccessful) ? SUCCESS : FAIL);
		}
	}

	/**
	 * Throwable custom Exception if GUI related exceptions present themselves.
	 */
	@SuppressWarnings("serial")
	static class GuiException extends Exception {
		public GuiException() {
			super("Error while trying to display GUI");
		}
	}

	public final static class ClipboardHandler implements ClipboardOwner {

		public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
		}

		/**
		 * Add the given string to the Clip-board, just as pressing CTRL+C.
		 * 
		 * @param str
		 *            The string we want to copy to the Clip-board.
		 */
		public void setClipboardContents(String str) {
			StringSelection stringSelection = new StringSelection(str);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, this);
		}

		/**
		 * Get the String residing on the clip-board.
		 *
		 * @return any text found on the Clip-board; if none found, return an
		 *         empty String.
		 * @throws GuiException
		 *             Thrown when can't access the Clip-board
		 * 
		 */
		public String getClipboardContents() throws GuiException {
			String result = "";
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable contents = clipboard.getContents(null);
			boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
			if (hasTransferableText) {
				try {
					result = (String) contents.getTransferData(DataFlavor.stringFlavor);
				} catch (UnsupportedFlavorException | IOException e) {
					throw new GuiException();
				}
			}
			return result;
		}
	}
}