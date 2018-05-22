package hr.fer.zemris.java.hw10jnotepadapp;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;

import hr.fer.zemris.java.localization.ILocalizationListener;
import hr.fer.zemris.java.localization.ILocalizationProvider;

/**
 * Models a status bar for the {@link JNotepadPP} app.
 * <p>
 * The status bar shows basic document information: <br>
 * length of the document currently in the editor, information about the current
 * caret position (line and column), the length of the selected part of text(if
 * any) and current date and time. The document info is left aligned, while the
 * clock is right aligned.
 * <p>
 * Information will be refreshed when needed - document info can be updated with
 * the call of {@link JStatusBar#updateStatusBar(JFileTab)}. The clock is
 * refreshed periodically. Dynamic language change is supported.
 * 
 * @author Mirna Baksa
 *
 */
public class JStatusBar extends JPanel {
	/** Default serialization version. */
	private static final long serialVersionUID = 1L;
	/** Shows length of the document currently in the editor. */
	private JTextArea length;
	/** Shows caret information. */
	private JTextArea caretInfo;
	/** Clock. */
	private JClock clock;
	/** Localization provider. */
	private ILocalizationProvider provider;
	/** Notepad app. */
	@SuppressWarnings("unused")
	private JNotepadPP app;

	/**
	 * Constructs a new {@link JStatusBar}.
	 * @param provider localization provider
	 * @param app notepad app
	 */
	public JStatusBar(ILocalizationProvider provider, JNotepadPP app) {
		this.app = app;
		this.provider = provider;
		provider.addLocalizationListener(new ILocalizationListener() {
			@Override
			public void localizationChanged(){
				JTabbedPane tabs = app.getTabs();
				if(tabs.getTabCount() == 0) return;
				
				JScrollPane filePane = (JScrollPane) tabs.getSelectedComponent();
				JStatusBar.this.updateStatusBar((JFileTab) filePane.getViewport().getView());
			}
		});
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		initGUI();
	}

	/**
	 * Initializes the GUI of the status bar.
	 */
	private void initGUI() {
		JPanel info = new JPanel();
		info.setLayout(new GridLayout(1,2));
		
		length = new JTextArea(provider.getString("length") +  ": 0");
		length.setOpaque(false);
		info.add(length);

		caretInfo = new JTextArea();
		caretInfo.setText("Ln:" + 0 + " Col:" + 0 + " Sel:" + 0);
		caretInfo.setOpaque(false);
		info.add(caretInfo);
		
		this.add(info, BorderLayout.LINE_START);
		
		this.clock = new JClock();
		clock.setOpaque(false);
		this.add(clock, BorderLayout.LINE_END);
	}

	/**
	 * Updated the status bar with new information about currently edited
	 * document.
	 * <p>
	 * The update will refresh the document length info and the caret info - its
	 * position (line and column) and the length od the selected part of text
	 * (if any).
	 * 
	 * @param editor
	 *            currently edited document
	 */
	public void updateStatusBar(JFileTab editor) {
		int lineNum = 1;
		int colNum = 1;

		int caretPos = editor.getCaretPosition();
		try {
			lineNum = editor.getLineOfOffset(caretPos);
			colNum = caretPos - editor.getLineStartOffset(lineNum);
		} catch (BadLocationException ignorable) {
		}

		int selectedLength = Math.abs(editor.getCaret().getDot() - editor.getCaret().getMark());
		int documentLength = editor.getDocument().getLength();
		this.length.setText(provider.getString("length") + ": " + String.valueOf(documentLength));
		caretInfo.setText("Ln:" + (lineNum + 1) + " Col:" + colNum + " Sel:" + selectedLength);
	}

}
