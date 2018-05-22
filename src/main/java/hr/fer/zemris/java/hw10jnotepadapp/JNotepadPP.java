package hr.fer.zemris.java.hw10jnotepadapp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import hr.fer.zemris.java.localization.FormLocalizationProvider;
import hr.fer.zemris.java.localization.ILocalizationListener;
import hr.fer.zemris.java.localization.ILocalizationProvider;
import hr.fer.zemris.java.localization.LocalizableAction;
import hr.fer.zemris.java.localization.LocalizationProvider;

/**
 * A generic simple text editor app.
 * <p>
 * The app provides basic file editing operations. Multiple files can be opened
 * and edited simultaneously. Each editing operation can be accessed through
 * menus, toolbars or keyboard shortcuts.
 * <p>
 * Localization is supported - currently supported languages are English,
 * German, Spanish, Italian and Croatian. Disclaimer: translations are acquired
 * through Google Translator and therefore the author is not held responsible
 * for their accuracy.
 * 
 * 
 * @author Mirna Baksa
 *
 */
@SuppressWarnings("serial")
public class JNotepadPP extends JFrame {
	/** Application name. */
	private final String APP_NAME = "JNotepad++";
	/** Icon marking the document has been modified (contents not saved). */
	protected static ImageIcon modifiedIcon;
	/** Icon marking the document is not modified (contents saved). */
	private static ImageIcon unmodifiedIcon;
	/** Tabs panel. */
	private JTabbedPane tabs;
	/** Editor's status bar. */
	private JStatusBar statusBar;
	/** Tools menu. */
	private JMenu tools;
	/** Editor's clipboard. */
	private String clipboard;
	/** Localization provider. */
	private FormLocalizationProvider provider = new FormLocalizationProvider(LocalizationProvider.getInstance(), this);
	/** Current language of the app. */
	private String currentLanguage;

	/**
	 * Constructs a new {@link JNotepadPP}.
	 */
	public JNotepadPP() {
		setSize(800, 500);
		setLocationRelativeTo(null);
		setTitle(APP_NAME);
		currentLanguage = "en";

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (checkAppClose())
					dispose();
			}
		});

		LocalizationProvider.getInstance().addLocalizationListener(new ILocalizationListener() {
			@Override
			public void localizationChanged() {
				provider.fire();
			}
		});

		initIcons();
		initGUI();
	}

	/**
	 * Initializes the icons used to represent unmodified/modified documents.
	 * Scales the images to the appropriate size.
	 */
	private void initIcons() {
		modifiedIcon = new ImageIcon("./src/main/resources/diskette-icon-16796.png");
		unmodifiedIcon = new ImageIcon("./src/main/resources/diskette-icon-16492.png");

		Image img = modifiedIcon.getImage();
		Image scaled = img.getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		modifiedIcon = new ImageIcon(scaled);

		img = unmodifiedIcon.getImage();
		scaled = img.getScaledInstance(15, 15, Image.SCALE_SMOOTH);
		unmodifiedIcon = new ImageIcon(scaled);
	}

	/**
	 * Initializes the graphical user interface.
	 */
	private void initGUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		tabs = new JTabbedPane();
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (tabs.getTabCount() == 0) {
					JNotepadPP.this.setTitle(APP_NAME);
					return;
				}
				JScrollPane pane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
				JFileTab file = (JFileTab) pane.getViewport().getView();
				statusBar.updateStatusBar(file);
				
				setToolsEnabled(file.isSelected());

				String filePath = file.getFilePath() == null ? provider.getString("new_file")
						: file.getFilePath().toString();
				JNotepadPP.this.setTitle(filePath + " - " + APP_NAME);
			}
		

			
		});

		cp.add(tabs, BorderLayout.CENTER);

		statusBar = new JStatusBar(provider, this);
		cp.add(statusBar, BorderLayout.PAGE_END);

		setUpAllActions();
		createMenus();
		createToolbar();
	}
	
	/**
	 * Enables or disables the tools menu action.
	 * @param selected <code>true</code> if the actions need to be enabled, false otherwise.
	 */
	protected void setToolsEnabled(boolean selected) {
		upperCaseAction.setEnabled(selected);
		lowerCaseAction.setEnabled(selected);
		invertCaseAction.setEnabled(selected);
		uniqueAction.setEnabled(selected);
		sortAscendingAction.setEnabled(selected);
		sortDescendingAction.setEnabled(selected);
	}

	/**
	 * Sets up the created actions.
	 * <p>
	 * For each action its name, accelerator key, mnemonic key and short
	 * description will be set up.
	 */
	private void setUpAllActions() {
		setUpAction(newFileAction, provider.getString("new"), KeyStroke.getKeyStroke("control N"), KeyEvent.VK_N,
				"Creates a blank document.");

		setUpAction(openDocumentAction, provider.getString("open"), KeyStroke.getKeyStroke("control O"), KeyEvent.VK_O,
				"Opens a document from the disk.");

		setUpAction(saveDocumentAction, provider.getString("save"), KeyStroke.getKeyStroke("control S"), KeyEvent.VK_S,
				"Saves the document to disk.");

		setUpAction(saveDocumentAsAction, provider.getString("save_as"), KeyStroke.getKeyStroke("control alt S"),
				KeyEvent.VK_A, "Saves the document to disk.");

		setUpAction(closeDocumentAction, provider.getString("close"), KeyStroke.getKeyStroke("control L"),
				KeyEvent.VK_W, "Closes the current document.");

		setUpAction(exitAction, provider.getString("exit"), KeyStroke.getKeyStroke("control E"), KeyEvent.VK_E,
				"Exits the app.");

		setUpAction(statisticalInfoAction, provider.getString("statistics"), KeyStroke.getKeyStroke("control I"),
				KeyEvent.VK_I, "Shows statistical info about the document.");

		setUpAction(copyAction, provider.getString("copy"), KeyStroke.getKeyStroke("control C"), KeyEvent.VK_C,
				"Copies the text to clipboard..");

		setUpAction(cutAction, provider.getString("cut"), KeyStroke.getKeyStroke("control X"), KeyEvent.VK_X,
				"Cuts the text to clipboard.");

		setUpAction(pasteAction, provider.getString("paste"), KeyStroke.getKeyStroke("control V"), KeyEvent.VK_V,
				"Pastes the text.");

		setUpAction(upperCaseAction, provider.getString("to_uppercase"), KeyStroke.getKeyStroke("control alt U"),
				KeyEvent.VK_U, "Uppercases the selected text.");

		setUpAction(lowerCaseAction, provider.getString("to_lowercase"), KeyStroke.getKeyStroke("control alt L"),
				KeyEvent.VK_L, "Lowercases the selected text.");

		setUpAction(invertCaseAction, provider.getString("invert_case"), KeyStroke.getKeyStroke("control alt I"),
				KeyEvent.VK_I, "Inverts the case of the selected text.");

		setUpAction(sortAscendingAction, provider.getString("sort_ascending"), KeyStroke.getKeyStroke("control alt A"),
				KeyEvent.VK_U, "Sorts the selected text in ascending order.");

		setUpAction(sortDescendingAction, provider.getString("sort_descending"),
				KeyStroke.getKeyStroke("control alt D"), KeyEvent.VK_L, "Sorts the selected text in descending order.");

		setUpAction(uniqueAction, provider.getString("unique"), KeyStroke.getKeyStroke("control alt U"), KeyEvent.VK_I,
				"Removes duplicate lines from the selected text.");

		setUpAction(english, provider.getString("english"), KeyStroke.getKeyStroke("control shift E"), KeyEvent.VK_E,
				"Changes the language of the app to English.");

		setUpAction(german, provider.getString("german"), KeyStroke.getKeyStroke("control shift G"), KeyEvent.VK_G,
				"Changes the language of the app to German.");
		
		setUpAction(italian, provider.getString("italian"), KeyStroke.getKeyStroke("control shift I"), KeyEvent.VK_I,
				"Changes the language of the app to Italian.");

		setUpAction(spanish, provider.getString("spanish"), KeyStroke.getKeyStroke("control shift S"), KeyEvent.VK_P,
				"Changes the language of the app to Spanish.");

		setUpAction(croatian, provider.getString("croatian"), KeyStroke.getKeyStroke("control shift C"), KeyEvent.VK_O,
				"Changes the language of the app to Croatian.");
	}

	/**
	 * Sets up an individual action.
	 * 
	 * @param action
	 *            action to be set
	 * @param name
	 *            name of the action
	 * @param stroke
	 *            accelerator key
	 * @param event
	 *            mnemonic key
	 * @param shortDescription
	 *            short description of the action
	 */
	private void setUpAction(Action action, String name, KeyStroke stroke, int event, String shortDescription) {
		action.putValue(Action.NAME, name);
		action.putValue(Action.ACCELERATOR_KEY, stroke);
		action.putValue(Action.MNEMONIC_KEY, event);
		action.putValue(Action.SHORT_DESCRIPTION, shortDescription);
	}

	/**
	 * Creates the {@link JNotepadPP} app menus. Menu items are logically
	 * separated.
	 */
	private void createMenus() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu(new LocalizableAction("file", provider));
		menuBar.add(fileMenu);
		fileMenu.add(new JMenuItem(newFileAction));
		fileMenu.add(new JMenuItem(openDocumentAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(saveDocumentAction));
		fileMenu.add(new JMenuItem(saveDocumentAsAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(closeDocumentAction));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(exitAction));

		JMenu editMenu = new JMenu(new LocalizableAction("edit", provider));
		menuBar.add(editMenu);
		editMenu.add(copyAction);
		editMenu.add(cutAction);
		editMenu.add(pasteAction);
		editMenu.addSeparator();
		editMenu.add(statisticalInfoAction);

		JMenu languageMenu = new JMenu(new LocalizableAction("languages", provider));
		menuBar.add(languageMenu);
		languageMenu.add(new JMenuItem(english));
		languageMenu.add(new JMenuItem(german));
		languageMenu.add(new JMenuItem(spanish));
		languageMenu.add(new JMenuItem(italian));
		languageMenu.add(new JMenuItem(croatian));

		tools = new JMenu(new LocalizableAction("tools", provider));
		menuBar.add(tools);
		tools.add(upperCaseAction);
		tools.add(lowerCaseAction);
		tools.add(invertCaseAction);
		JMenu sort = new JMenu(new LocalizableAction("sort", provider));
		sort.add(sortAscendingAction);
		sort.add(sortDescendingAction);
		sort.add(uniqueAction);
		tools.add(sort);

		setJMenuBar(menuBar);
	}

	/**
	 * Changes the language of the app.
	 * 
	 * @author Mirna Baksa
	 *
	 */
	private class LanguageChanger extends LocalizableAction {
		/** Language tag. */
		private String languageTag;

		/**
		 * Constructs a new {@link LanguageChanger}.
		 * 
		 * @param languageTag
		 *            language tag
		 * @param key
		 *            key of the action
		 * @param lp
		 *            localization provider
		 **/
		public LanguageChanger(String languageTag, String key, ILocalizationProvider lp) {
			super(key, lp);
			this.languageTag = languageTag;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			LocalizationProvider.getInstance().setLanguage(languageTag);
			currentLanguage = this.languageTag;
		}
	}

	/** Changes the language to English. */
	private LanguageChanger english = new LanguageChanger("en", "english", provider);
	/** Changes the language to German. */
	private LanguageChanger german = new LanguageChanger("de", "german", provider);
	/** Changes the language to Italian. */
	private LanguageChanger italian = new LanguageChanger("it", "italian", provider);
	/** Changes the language to Spanish. */
	private LanguageChanger spanish = new LanguageChanger("es", "spanish", provider);
	/** Changes the language to Croatian. */
	private LanguageChanger croatian = new LanguageChanger("cro", "croatian", provider);

	/**
	 * Creates the {@link JNotepadPP} app toolbars. Toolbar items are logically
	 * separated.
	 */
	private void createToolbar() {
		JToolBar toolbar = new JToolBar(provider.getString("toolbar"));
		toolbar.setFloatable(true);

		toolbar.add(new JButton(newFileAction));
		toolbar.add(new JButton(openDocumentAction));
		toolbar.addSeparator();
		toolbar.add(new JButton(saveDocumentAction));
		toolbar.add(new JButton(saveDocumentAsAction));
		toolbar.addSeparator();
		toolbar.addSeparator();
		toolbar.add(new JButton(copyAction));
		toolbar.add(new JButton(cutAction));
		toolbar.add(new JButton(pasteAction));
		toolbar.addSeparator();
		toolbar.add(statisticalInfoAction);
		toolbar.addSeparator();
		toolbar.add(new JButton(closeDocumentAction));

		getContentPane().add(toolbar, BorderLayout.PAGE_START);

	}

	/**
	 * Opens a new blank file with the default name <code> "new file"</code>.
	 * The file will not be initially saved and its path defined - the user must
	 * save the document manually.
	 */
	private Action newFileAction = new LocalizableAction("new", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileTab newFile = new JFileTab(null, JNotepadPP.this);
			tabs.addTab(provider.getString(getKey()), new JScrollPane(newFile));
			tabs.setIconAt(tabs.getTabCount() - 1, unmodifiedIcon);
		}
	};

	/**
	 * Opens a document from the disk.
	 * <p>
	 * The chosen file has to be readable. In case of an occurrence of an error
	 * while opening a file, a suitable error message will be shown.
	 */
	private Action openDocumentAction = new LocalizableAction("open", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle(provider.getString("open_file"));

			if (fc.showOpenDialog(JNotepadPP.this) != JFileChooser.APPROVE_OPTION) {
				return;
			}

			Path filePath = fc.getSelectedFile().toPath();

			if (!Files.isReadable(filePath)) {
				JOptionPane.showMessageDialog(JNotepadPP.this, filePath + " " + provider.getString("reading_error"),
						provider.getString("error"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			String text = null;
			try {
				text = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(JNotepadPP.this, filePath + " " + provider.getString("reading_error"),
						provider.getString("error"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			JFileTab file = new JFileTab(filePath, JNotepadPP.this);
			file.setText(text);
			tabs.add(filePath.getFileName().toString(), new JScrollPane(file));

			int tabIndex = tabs.getTabCount() - 1;
			tabs.setIconAt(tabIndex, unmodifiedIcon);
			tabs.setToolTipTextAt(tabIndex, filePath.toString());
			tabs.setSelectedComponent(tabs.getComponentAt(tabIndex));
		}
	};

	/**
	 * Saves the file currently in the editor. The file is saved to its current
	 * path - see {@link notepad#saveDocumentAsAction} for saving to different
	 * path.
	 * <p>
	 * In case of an occurrence of an error while saving the file, a suitable
	 * error message will be shown and the user informed.
	 */
	private Action saveDocumentAction = new LocalizableAction("save", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			Path filePath = file.getFilePath();

			// file created in the editor and not yet saved
			if (filePath == null) {
				saveDocumentAsAction.actionPerformed(e);
				return;
			}

			try {
				Files.write(filePath, file.getText().getBytes(StandardCharsets.UTF_8));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(JNotepadPP.this, provider.getString("save_unsuccess"),
						provider.getString("error"), JOptionPane.ERROR_MESSAGE);

				return;
			}

			JOptionPane.showMessageDialog(JNotepadPP.this, provider.getString("save_success"), "Info",
					JOptionPane.INFORMATION_MESSAGE);

			file.setEdited(false);
			tabs.setIconAt(tabs.getSelectedIndex(), unmodifiedIcon);
			tabs.setTitleAt(tabs.getSelectedIndex(), filePath.getFileName().toString());
			tabs.setToolTipTextAt(tabs.getSelectedIndex(), filePath.toString());
		}
	};

	/**
	 * Saves the file currently in the editor, offering the possibility to save
	 * to a different path from the current one. If the file the user chose
	 * already exists, a suitable question message will be shown and further
	 * action taken depending on user feedback.
	 * <p>
	 * In case of an occurrence of an error while saving the file, a suitable
	 * error message will be shown and the user informed.
	 */
	private Action saveDocumentAsAction = new LocalizableAction("save_as", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();

			JFileChooser fc = new JFileChooser();
			fc.setDialogTitle("Save file as");

			if (fc.showSaveDialog(JNotepadPP.this) != JFileChooser.APPROVE_OPTION) {
				JOptionPane.showMessageDialog(JNotepadPP.this, provider.getString("save_cancel"),
						provider.getString("warning"), JOptionPane.WARNING_MESSAGE);
				return;
			}

			Path filePath = fc.getSelectedFile().toPath();

			if (filePath.toFile().exists()) {
				int selected = JOptionPane.showConfirmDialog(JNotepadPP.this, provider.getString("file_exists"),
						provider.getString("overwrite"), JOptionPane.YES_NO_OPTION);

				if (selected == JOptionPane.NO_OPTION)
					return;
			}

			try {
				Files.write(filePath, file.getText().getBytes(StandardCharsets.UTF_8));
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(JNotepadPP.this, provider.getString("save_unsucess"),
						provider.getString("error"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			JOptionPane.showMessageDialog(JNotepadPP.this, provider.getString("save_success"), "Info",
					JOptionPane.INFORMATION_MESSAGE);

			file.setFilePath(filePath);
			file.setEdited(false);
			tabs.setIconAt(tabs.getSelectedIndex(), unmodifiedIcon);
			tabs.setTitleAt(tabs.getSelectedIndex(), filePath.getFileName().toString());
			tabs.setToolTipTextAt(tabs.getSelectedIndex(), filePath.toString());

			JNotepadPP.this.setTitle(filePath + " - " + APP_NAME);
		}
	};

	/**
	 * Closes the document currently in the editor. If the file was edited and
	 * not saved, the user will be asked if saving is needed.
	 */
	private Action closeDocumentAction = new LocalizableAction("close", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			String filePath = file.getFilePath() == null ? provider.getString("new_file")
					: file.getFilePath().toString();

			if (file.isEdited()) {
				int option = JOptionPane.showConfirmDialog(JNotepadPP.this,
						filePath + provider.getString("file_not_saved"));

				if (option == JOptionPane.CANCEL_OPTION)
					return;
				if (option == JOptionPane.YES_OPTION)
					saveDocumentAction.actionPerformed(null);
			}

			tabs.remove(tabs.getSelectedIndex());
		}

	};

	/**
	 * Provides statistical info on the current document.
	 * <p>
	 * The info will show: 1. the number of characters found in the document 2.
	 * the number of non - blank characters found in the document 3. the number
	 * of lines the document contains
	 * 
	 */
	private Action statisticalInfoAction = new LocalizableAction("statistics", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();

			char[] textCharacters = file.getText().toCharArray();
			int blankChars = 0, lines = 1;

			for (char c : textCharacters) {
				if (Character.isWhitespace(c))
					blankChars++;
				if (String.valueOf(c) == System.getProperty("line.separator"))
					lines++;
			}

			// empty document
			if (textCharacters.length == 0)
				lines = 0;

			StringJoiner message = new StringJoiner(" ");
			message.add(provider.getString("stat_message") + ":").add(String.valueOf(textCharacters.length))
					.add(provider.getString("chars") + ",").add(String.valueOf(textCharacters.length - blankChars))
					.add(provider.getString("blankchars") + ",").add(String.valueOf(lines))
					.add(provider.getString("lines") + ".");

			JOptionPane.showMessageDialog(JNotepadPP.this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
		}

	};

	/**
	 * Exits the app.
	 * <p>
	 * If there are any unsaved documents currently in the editor, the user will
	 * be informed and saving of the documents enabled.
	 */
	private Action exitAction = new LocalizableAction("exit", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (checkAppClose())
				JNotepadPP.this.dispose();
		}
	};

	/**
	 * Copies the selected part of text to clipboard.
	 */
	private Action copyAction = new LocalizableAction("copy", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			Document doc = file.getDocument();

			int length = Math.abs(file.getCaret().getDot() - file.getCaret().getMark());

			if (length == 0) {
				return;
			}
			int offset = Math.min(file.getCaret().getDot(), file.getCaret().getMark());
			try {
				clipboard = doc.getText(offset, length);
			} catch (BadLocationException ignorable) {
			}
		}
	};

	/** Cuts the selected part of text to clipboard. */
	private Action cutAction = new LocalizableAction("cut", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			Document doc = file.getDocument();

			int length = Math.abs(file.getCaret().getDot() - file.getCaret().getMark());

			if (length == 0) {
				return;
			}
			int offset = Math.min(file.getCaret().getDot(), file.getCaret().getMark());
			try {
				clipboard = doc.getText(offset, length);
				doc.remove(offset, length);
			} catch (BadLocationException ignorable) {
			}
		}

	};

	/**
	 * Pastes the text from the clipboard to the text.
	 */
	private Action pasteAction = new LocalizableAction("paste", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (tabs.getTabCount() == 0)
				return;

			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			Document doc = file.getDocument();

			if (clipboard == null)
				return;
			int length = clipboard.length();
			if (length == 0)
				return;

			int offset = Math.min(file.getCaret().getDot(), file.getCaret().getMark());
			try {
				doc.insertString(offset, clipboard, null);
			} catch (BadLocationException ignorable) {
			}
		}

	};

	/**
	 * Shifts the selected part of text to upper case letters.
	 */
	private Action upperCaseAction = new LocalizableAction("to_uppercase", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedTextCaseActionPerfomed(s -> s.toUpperCase());
		}
	};

	/**
	 * Shifts the selected part of text to lower case letters.
	 */
	private Action lowerCaseAction = new LocalizableAction("to_lowercase", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedTextCaseActionPerfomed(s -> s.toLowerCase());
		}
	};

	/**
	 * Inverts the casing of the selected part of text. All upper case letters
	 * will be inverted to lower case and vise versa.
	 */
	private Action invertCaseAction = new LocalizableAction("to_lowercase", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectedTextCaseActionPerfomed(s -> invertCase(s));
		}
	};

	/**
	 * Inverts the casing in the given string. All upper case letters will be
	 * inverted to lower case and vise versa.
	 * 
	 * @param s
	 *            string to be inverted
	 * @return inverted string
	 */
	private String invertCase(String s) {
		char[] chars = s.toCharArray();
		StringBuilder result = new StringBuilder(chars.length);
		for (char c : chars) {
			if (Character.isLowerCase(c)) {
				result.append(Character.toUpperCase(c));
			} else if (Character.isUpperCase(c)) {
				result.append(Character.toLowerCase(c));
			}else{
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * Performs the action on the selected part of text using the caseAction
	 * given as an argument.
	 * 
	 * @param caseAction
	 *            action to perform over the selected part of text.
	 */
	private void selectedTextCaseActionPerfomed(UnaryOperator<String> caseAction) {
		if (caseAction == null)
			return;
		if (tabs.getTabCount() == 0)
			return;

		JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
		JFileTab file = (JFileTab) filePane.getViewport().getView();
		Document doc = file.getDocument();

		int length = Math.abs(file.getCaret().getDot() - file.getCaret().getMark());
		int offset = Math.min(file.getCaret().getDot(), file.getCaret().getMark());

		try {
			String selectedText = doc.getText(offset, length);
			doc.remove(offset, length);
			doc.insertString(offset, caseAction.apply(selectedText), null);
		} catch (BadLocationException e1) {
		}
	}

	/**
	 * Sorts the selected text in ascending order.If only a part of a line is
	 * selected, the whole line will be affected.
	 */
	private Action sortAscendingAction = new LocalizableAction("sort_ascending", provider) {

		@Override
		public void actionPerformed(ActionEvent e) {
			performSortMenuAction(true, 1);
		}

	};

	/**
	 * Sorts the text in descending order. If only a part of a line is selected,
	 * the whole line will be affected.
	 */
	private Action sortDescendingAction = new LocalizableAction("sort_descending", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			performSortMenuAction(true, -1);
		}
	};

	/**
	 * Removes duplicate lines from the selected text. If only a part of a line
	 * is selected, the whole line will be affected.
	 */
	private Action uniqueAction = new LocalizableAction("unique", provider) {
		@Override
		public void actionPerformed(ActionEvent e) {
			performSortMenuAction(false, null);
		}
	};

	/**
	 * Performs a sort menu action - sort ascending, sort descending or remove
	 * duplicates (unique) action.
	 * <p>
	 * The factor is used to determine the order of sorting - - sorting will be
	 * ascending if the factor is greater than zero, descending if the factor is
	 * less than zero. No sorting will be done if the factor is equal to zero.
	 * 
	 * @param sortAction
	 *            <code>true</code> if the action is sorting,
	 *            <code>false </code> if it is the uniqeue action.
	 * @param factor
	 *            factor determining the order of sorting
	 */
	private void performSortMenuAction(boolean sortAction, Integer factor) {
		JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
		JFileTab file = (JFileTab) filePane.getViewport().getView();
		Document doc = file.getDocument();

		try {
			int start = file.getLineOfOffset(Math.min(file.getCaret().getDot(), file.getCaret().getMark()));
			int end = file.getLineOfOffset(Math.max(file.getCaret().getDot(), file.getCaret().getMark()));

			int linesStart = file.getLineStartOffset(start);
			int linesEnd = file.getLineEndOffset(end);

			String selectedText = doc.getText(linesStart, linesEnd);
			doc.remove(linesStart, linesEnd);
			String applied = sortAction ? getSortedLines(selectedText, factor) : getUniqueLines(selectedText);
			doc.insertString(linesStart, applied, null);
		} catch (BadLocationException ex) {
		}
	}

	/**
	 * Removes duplicate lines from the given string.
	 * 
	 * @param selectedText
	 *            string to remove lines from
	 * @return string of unique lines generated from the input
	 */
	private String getUniqueLines(String selectedText) {
		Set<String> uniques = new LinkedHashSet<>();
		Scanner scanner = new Scanner(selectedText);
		while (scanner.hasNextLine()) {
			uniques.add(scanner.nextLine());
		}
		scanner.close();
		return String.join(System.lineSeparator(), uniques);
	}

	/**
	 * Sorts the lines of the given string.
	 * <p>
	 * The sorting order is defined by the factor argument - sorting will be
	 * ascending if the factor is greated than zero, descending if the factor is
	 * less than zero. No sorting will be done if the factor is equal to zero.
	 * 
	 * @param selectedText
	 *            lines to sort
	 * @param factor
	 *            factor determining the order of sorting
	 * @return sorted lines
	 */
	private String getSortedLines(String selectedText, int factor) {
		Locale currentLocale = new Locale(currentLanguage);
		Collator currentCollator = Collator.getInstance(currentLocale);

		List<String> lines = new ArrayList<>();
		Scanner scanner = new Scanner(selectedText);
		while (scanner.hasNextLine()) {
			lines.add(scanner.nextLine());
		}
		scanner.close();

		lines.sort((l1, l2) -> factor * currentCollator.compare(l1, l2));
		return String.join(System.lineSeparator(), lines);
	}

	/**
	 * Checks if there are any unsaved documents currently in the editor. Asks
	 * the user if saving is needed for each edited document and enables the
	 * saving of those documents
	 * 
	 * @return <code>true</code> if the app can be closed, <code>false</code>
	 *         otherwise.
	 */
	private boolean checkAppClose() {
		boolean canClose = true;

		for (int i = 0, n = tabs.getTabCount(); i < n; i++) {
			JScrollPane filePane = (JScrollPane) tabs.getComponentAt(tabs.getSelectedIndex());
			JFileTab file = (JFileTab) filePane.getViewport().getView();
			String filePath = file.getFilePath() == null ? provider.getString("new_file")
					: file.getFilePath().toString();

			if (file.isEdited()) {
				int option = JOptionPane.showConfirmDialog(JNotepadPP.this,
						filePath + " " + provider.getString("file_not_saved"));

				if (option == JOptionPane.CANCEL_OPTION)
					return false;
				if (option == JOptionPane.YES_OPTION)
					saveDocumentAction.actionPerformed(null);
			}
		}
		return canClose;
	}

	/**
	 * Gets the status bar of the app.
	 * 
	 * @return status bar
	 */
	public JStatusBar getStatusBar() {
		return statusBar;
	}

	/**
	 * Gets the tabs of this app.
	 * 
	 * @return tabs
	 */
	public JTabbedPane getTabs() {
		return tabs;
	}

	/**
	 * Gets the tools menu of the app.
	 * 
	 * @return tools menu
	 */
	public JMenu getTools() {
		return tools;
	}

	/**
	 * Main method - starts the program execution.
	 * 
	 * @param args
	 *            not used
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new JNotepadPP().setVisible(true));
	}
}
