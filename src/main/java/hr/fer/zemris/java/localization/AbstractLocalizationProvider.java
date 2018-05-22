package hr.fer.zemris.java.localization;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementor of the {@link ILocalizationProvider}. Enables the registration of
 * localization listeners - implementors of the {@link ILocalizationListener}
 * interface. Notifies the listeners about localization changes.
 * 
 * @author Mirna Baksa
 *
 */
public abstract class AbstractLocalizationProvider implements ILocalizationProvider {
	/** Localization change listeners. */
	private List<ILocalizationListener> listeners;

	/**
	 * Constructs a new {@link AbstractLocalizationProvider}.
	 */
	public AbstractLocalizationProvider() {
		listeners = new ArrayList<>();
	}

	@Override
	public void addLocalizationListener(ILocalizationListener l) {
		if (l == null) {
			throw new IllegalArgumentException("The listener can not be null.");
		}
		if (!listeners.contains(l))
			listeners.add(l);
	}

	@Override
	public void removeLocalizationListener(ILocalizationListener l) {
		if (l == null) {
			throw new IllegalArgumentException("The listener can not be null.");
		}
		listeners.remove(l);
	}

	/**
	 * Notifies listeners about localization changes.
	 */
	public void fire() {
		List<ILocalizationListener> tempListeners = new ArrayList<>(this.listeners);
		for (ILocalizationListener l : tempListeners) {
			l.localizationChanged();
		}
	}

}
