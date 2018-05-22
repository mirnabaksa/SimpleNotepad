package hr.fer.zemris.java.localization;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

/**
 * Enables the localization on a frame.
 * 
 * @author Mirna Baksa
 *
 */
public class FormLocalizationProvider extends LocalizationProviderBridge {

	/**
	 * Constructs a new {@link FormLocalizationProvider}.
	 * 
	 * @param provider
	 *            localization provider
	 * @param frame
	 *            frame to provide
	 */
	public FormLocalizationProvider(ILocalizationProvider provider, JFrame frame) {
		super(provider);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent e) {
				connect();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				disconnect();
			}
		});
	}
}
