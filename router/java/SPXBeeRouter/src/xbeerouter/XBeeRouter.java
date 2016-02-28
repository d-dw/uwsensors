package xbeerouter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
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
		
		SiteSchedule sSchedule = null;
		try {
			FileInputStream in = new FileInputStream(args[0]);
			Properties props = new Properties();
			props.load(in);
			in.close();
			sSchedule = new SiteSchedule(props);
		} catch (FileNotFoundException e) {
			LOGGER.severe("No properties file provided. Quitting.");
			System.exit(1);
		} catch (IOException e) {
			LOGGER.severe("Error while processing properties file!");
			LOGGER.severe(e.getMessage());
			System.exit(1);
		}
		
		final XBeeDevice xbDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			xbDevice.open();
			
			final XBeePacketListener listener = new XBeePacketListener();
			final SensorControlListener controlListener = new SensorControlListener(sSchedule, xbDevice);
			
			xbDevice.addPacketListener(listener);
			xbDevice.addPacketListener(controlListener);
			
			LOGGER.info(">> Waiting for data...");
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					LOGGER.info("XBeeRouter Shutting Down!");
					xbDevice.close();
					listener.closeZMQSocket();
				}
			});
		} catch (XBeeException e) {
			LOGGER.severe(e.getLocalizedMessage());
			LOGGER.severe("Unable to initialize XBee device! Quitting.");
			System.exit(1);
		}
	}
}