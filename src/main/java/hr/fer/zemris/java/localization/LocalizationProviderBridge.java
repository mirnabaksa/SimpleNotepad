package hr.fer.zemris.java.localization;

/**
 * {@link ILocalizationProvider} decorator. Enables optimal multi-window app
 * localization.
 * 
 * @author Mirna Baksa
 *
 */
public class LocalizationProviderBridge extends AbstractLocalizationProvider {
	/** Localization provider. */
	private ILocalizationProvider localizationProvider;
	/** Marks if the bridge is connected. */
	private boolean connected;
	/** Localization listener. */
	private ILocalizationListener listener;

	/**
	 * Constructs a new {@link LocalizationProviderBridge}.
	 * 
	 * @param localizationProvider
	 *            localization provider
	 */
	public LocalizationProviderBridge(ILocalizationProvider localizationProvider) {
		this.localizationProvider = localizationProvider;
	}

	/**
	 * Connects the bridge to the localization provider.
	 */
	public void connect() {
		if (connected)
			return;
		this.connected = true;
		localizationProvider.addLocalizationListener(listener = new ILocalizationListener() {
			@Override
			public void localizationChanged() {
				fire();
			}
		});
	}

	/**
	 * Disconnects the bridge from the localization provider.
	 */
	public void disconnect() {
		this.connected = false;
		localizationProvider.removeLocalizationListener(listener);
	}

	@Override
	public String getString(String key) {
		return localizationProvider.getString(key);
	}

}
