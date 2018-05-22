package hr.fer.zemris.java.hw10jnotepadapp;

import java.nio.file.Path;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Models a file tab in the {@link JNotepadPP} app. Each file tab stores the
 * path of the file shown in the tab.
 * 
 * @author Mirna Baksa
 *
 */
public class JFileTab extends JTextArea {
	/** Default serialization version. */
	private static final long serialVersionUID = 1L;
	/** Path of the file shown in the tab. */
	private Path filePath;
	/** App in which the tab is shown. */
	private JNotepadPP app;
	/** Marks if the document has any unsaved changes. */
	private boolean unsavedChanges;

	/**
	 * Constructs a new {@link JFileTab}.
	 * 
	 * @param filePath
	 *            path of the file shown in the tab
	 * @param app
	 *            reference to the app in which the tab is shown
	 */
	public JFileTab(Path filePath, JNotepadPP app) {
		this.filePath = filePath;
		this.app = app;

		getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				change();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				change();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				change();
			}

			private void change() {
				unsavedChanges = true;
				JTabbedPane tabs = app.getTabs();
				if (tabs.getTabCount() == 0)
					return;
				

				tabs.setIconAt(tabs.getSelectedIndex(), JNotepadPP.modifiedIcon);
			}
		});

		addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				boolean enabled;
				if(JFileTab.this.getSelectedText() != null) enabled = true;
				else enabled = false;
				JFileTab.this.app.setToolsEnabled(enabled);
				
				JFileTab.this.app.getStatusBar().updateStatusBar(JFileTab.this);
			}

		});
	}

	/**
	 * Sets the file path of the tab.
	 * 
	 * @param filePath
	 *            path to be set
	 */
	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}

	/**
	 * Gets the file path of the tab.
	 * 
	 * @return file path
	 */
	public Path getFilePath() {
		return filePath;
	}

	/**
	 * Sets the flag marking if the document was changed or its changes saved.
	 * 
	 * @param edited
	 *            <code>true</code> if the document was edited and has unsaved
	 *            changes, <code>false</code> if the document was saved (has no
	 *            unsaved changes).
	 */
	public void setEdited(boolean edited) {
		this.unsavedChanges = edited;
	}

	/**
	 * Returns information about unsaved changes in the document.
	 * 
	 * @return <code>true</code> if there are unsaved changes,
	 *         <code>false</code> otherwise.
	 */
	public boolean isEdited() {
		return this.unsavedChanges;
	}

	/**
	 * Returns information about the selection of text in the document.
	 * 
	 * @return <code> true</code> if the part of the document is selected,
	 *         <code>false</code> otherwise.
	 */
	public boolean isSelected() {
		if (this.getSelectedText() == null)
			return false;
		return true;
	}

}
