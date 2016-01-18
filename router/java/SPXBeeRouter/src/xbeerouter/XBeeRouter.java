package xbeerouter;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;

/**
 * XBee Receiver for Sensor Project
 * Utilizes XBee Java API
 */
public class XBeeRouter {
	
	/* Constants */
	
	private static final String PORT = "COM4";
	private static final int BAUD_RATE = 57600;

	/**
	 * Application main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		System.out.println(" +-----------------------------------------+");
		System.out.println(" |  XBeeRouter for UW Sensor Project  |");
		System.out.println(" +-----------------------------------------+\n");
		
		final XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			System.out.println(myDevice.toString());
			myDevice.open();
			
			final XBeePacketListener listener = new XBeePacketListener();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					listener.closeSocket();
					myDevice.close();
				}
			});
			
			myDevice.addPacketListener(listener);
			
			System.out.println("\n>> Waiting for data...");
			
		} catch (XBeeException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}