package xbeerouter;

import java.util.logging.Logger;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;

/**
 * XBee Router for Sensor Project
 * Utilizes XBee Java API and ZeroMQ
 */
public class XBeeRouter {
	
	/* Constants */
	
	private static final String PORT = "/dev/tty.usbserial-A403JW2C";
	private static final int BAUD_RATE = 57600;
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	/**
	 * Application main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		System.out.println("Initializing XBeeRouter for UW Sensor Project");
		
		final XBeeDevice xbDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			xbDevice.open();
			
			final XBeePacketListener listener = new XBeePacketListener();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					xbDevice.close();
					listener.closeSocket();
				}
			});
			
			xbDevice.addPacketListener(listener);
			
			LOGGER.info(">> Waiting for data...");
			
		} catch (XBeeException e) {
			LOGGER.severe(e.getLocalizedMessage());
			LOGGER.severe("Unable to initialize XBee device! Quitting.");
			System.exit(1);
		}
	}
}