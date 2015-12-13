package xbeereceiver;

import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.XBeeException;

/**
 * XBee Receiver for Sensor Project
 * Utilizes XBee Java API
 */
public class XBeeReceiver {
	
	/* Constants */
	
	// TODO Replace with the serial port where your receiver module is connected.
	private static final String PORT = "COM4";
	// TODO Replace with the baud rate of you receiver module.
	private static final int BAUD_RATE = 57600;

	/**
	 * Application main method.
	 * 
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		System.out.println(" +-----------------------------------------+");
		System.out.println(" |  XBeeReceiver for Sensor Project  |");
		System.out.println(" +-----------------------------------------+\n");
		
		XBeeDevice myDevice = new XBeeDevice(PORT, BAUD_RATE);
		
		try {
			System.out.println(myDevice.toString());
			myDevice.open();
			
			XBeePacketListener listener = new XBeePacketListener();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					listener.closeSocket();
					myDevice.close();
				}
			});
			
			myDevice.addPacketListener(listener);
			//myDevice.addPacketListener(new MyTestPacketListener());
			//myDevice.addDataListener(new MyDataReceiveListener(myDevice));
			
			System.out.println("\n>> Waiting for data...");
			
		} catch (XBeeException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}