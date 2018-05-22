package hr.fer.zemris.java.hw10jnotepadapp;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Models a clock in the format of <code> "YYYY/MM/DD hh/mm/ss" </code> (e.g. 2015/05/15
 * 11:15:25). The clock is refreshed every second.
 * 
 * @author Mirna Baksa
 *
 */
public class JClock extends JTextArea {
	/** Default serialization version. */
	private static final long serialVersionUID = 1L;
	/** Time. */
	private LocalDateTime time;

	/**
	 * Constructs a new {@link JClock}.
	 */
	public JClock() {
		DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

		Thread clockWorker = new Thread(() -> {
			while (true) {
				SwingUtilities.invokeLater(() -> {
					time = LocalDateTime.now();
					setText(time.format(format).toString());
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignorable) {
				}
			}
		});

		clockWorker.start();
	}

}
