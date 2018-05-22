package hr.fer.zemris.java.localization;

/**
 * Provider of translations for a given key. Supports the dynamical change of
 * language through the registration of {@link ILocalizationListener}s.
 * 
 * @author Mirna Baksa
 *
 */
public interface ILocalizationProvider {
	/**
	 * Adds a {@link ILocalizationListener} to the list of listeners.
	 * @param l listener to add
	 */
	public void addLocalizationListener(ILocalizationListener l);
	/**
	 * Remove a {@link ILocalizationListener} from the list of listeners.
	 * @param l listener to remove
	 */
	public void removeLocalizationListener(ILocalizationListener l);

	/**
	 * Gets the localized translation for the given key.
	 * @param key key to translate
	 * @return translation
	 */
	public String getString(String key);

}
