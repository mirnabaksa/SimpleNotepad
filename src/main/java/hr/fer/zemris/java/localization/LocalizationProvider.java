package hr.fer.zemris.java.localization;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides optimal app localization. Enables dynamical language change where
 * all listeners will be notified when the language changes.
 * 
 * @author Mirna Baksa
 *
 */
public class LocalizationProvider extends AbstractLocalizationProvider {
	/** Only instance of the {@link LocalizationProvider} */
	private static LocalizationProvider instance = new LocalizationProvider();
	/** Current language tag. */
	private String language;
	/** Bundle or the given language. */
	private ResourceBundle bundle;

	/**
	 * Constructs a new {@link LocalizationProvider}.
	 */
	private LocalizationProvider() {
		setLanguage("en");
	}

	/**
	 * Returns the instance of {@link LocalizationProvider}.
	 * 
	 * @return
	 */
	public static LocalizationProvider getInstance() {
		return instance;
	}

	/**
	 * Sets the language of the provider.
	 * 
	 * @param language
	 *            language to be set
	 */
	public void setLanguage(String language) {
		this.language = language;
		Locale locale = Locale.forLanguageTag(this.language);
		bundle = ResourceBundle.getBundle("hr.fer.zemris.java.localization.translations", locale);
		fire();
	}

	@Override
	public String getString(String key) {
		return bundle.getString(key);
	}

}
