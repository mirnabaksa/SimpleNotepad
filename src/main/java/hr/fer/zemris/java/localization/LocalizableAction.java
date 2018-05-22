package hr.fer.zemris.java.localization;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Models a localizable action. The language is changed dynamically.
 * 
 * @author Mirna Baksa
 *
 */
public class LocalizableAction extends AbstractAction {
	/** Default serialization version. */
	private static final long serialVersionUID = 1L;
	/** Localization provider. */
	private ILocalizationProvider lp;
	/** Action key. */
	private String key;

	/**
	 * Constructs a new {@link LocalizableAction}.
	 * 
	 * @param key
	 *            action key
	 * @param lp
	 *            localization provider
	 */
	public LocalizableAction(String key, ILocalizationProvider lp) {
		this.lp = lp;
		this.key = key;
		setTranslation();

		lp.addLocalizationListener(new ILocalizationListener() {
			@Override
			public void localizationChanged() {
				setTranslation();
			}
		});
	}

	/**
	 * Sets the translation of the action.
	 */
	private void setTranslation() {
		String translation = this.lp.getString(this.key);
		putValue(Action.NAME, translation);
	}

	/**
	 * Gets the key of the action.
	 * 
	 * @return
	 */
	public String getKey() {
		return key;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

}
