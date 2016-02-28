package xbeerouter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.exceptions.TimeoutException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.packet.XBeePacket;

public class SensorControlListener implements IPacketReceiveListener {
	private SiteSchedule sSchedule;
	private XBeeDevice xbDevice;
	private Map<String, Long> lastCommandSeqnum;
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public SensorControlListener(SiteSchedule sSchedule, XBeeDevice xbDevice) {
		this.sSchedule = sSchedule;
		this.xbDevice = xbDevice;
		lastCommandSeqnum = new ConcurrentHashMap<String, Long>();
	}

	public void packetReceived(XBeePacket packet) {
		byte[] data = Arrays.copyOfRange(packet.getPacketData(), 11, packet.getPacketLength());
		LinkedHashMap<String, String> params = packet.getParameters();
		if (params.get("Frame type").contains("80")) {
			if (sSchedule.closed()) {
				String addr = params.get("64-bit source address");
				ByteBuffer bb = ByteBuffer.wrap(data);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				byte h1 = bb.get();
				byte h2 = bb.get();
				// Check for magic header bytes
				if (h1 != 0x6B || h2 != 0x57)
					return;
				byte vers = bb.get();
				byte type = bb.get();
				IntBuffer ib = bb.asIntBuffer();
				long seqNum = Integer.toUnsignedLong(ib.get());
				long sTime = Integer.toUnsignedLong(ib.get());
				// Skip sleep packet if we already sent it during this round
				if (lastCommandSeqnum.containsKey(addr) &&
						seqNum - lastCommandSeqnum.get(addr) < SensorTime.MAX_PKT_BUF)
					return;
				
				byte[] cmdPkt = createSleepCommandPacket(sSchedule.timeToOpen());
				RemoteXBeeDevice sensorXB = new RemoteXBeeDevice(xbDevice, new XBee64BitAddress(addr));
				try {
					xbDevice.sendData(sensorXB, cmdPkt);
					lastCommandSeqnum.put(addr, seqNum);
					LOGGER.info("Sleep packet sent to " + addr);
				} catch (TimeoutException e) {
					LOGGER.warning("Timeout sending sleep to " + addr);
					LOGGER.warning(e.getLocalizedMessage());
				} catch (XBeeException e) {
					LOGGER.warning("XBeeException when sending sleep to " + addr);
					LOGGER.warning(e.getLocalizedMessage());
				}
			}
		}
	}
	
	private byte[] createSleepCommandPacket(long sleepTime) {
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.put((byte)0x6B);
		buf.put((byte)0x57);
		buf.put((byte)0x01);
		buf.put((byte)0x01);
		buf.putInt((int)sleepTime);
		return buf.array();
	}

}
