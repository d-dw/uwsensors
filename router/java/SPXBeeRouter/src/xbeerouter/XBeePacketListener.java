package xbeerouter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.zeromq.ZMQ;

import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.packet.XBeePacket;

public class XBeePacketListener implements IPacketReceiveListener {
	
	private ZMQ.Context zcontext;
	private ZMQ.Socket zpub;
	private Map<String, SensorTime> timingMap = Collections.synchronizedMap(new HashMap<String, SensorTime>());
	
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public XBeePacketListener() {
		zcontext = ZMQ.context(1);
		zpub = zcontext.socket(ZMQ.PUB);
		zpub.bind("tcp://127.0.0.1:5556");
		LOGGER.info("ZMQ Publishing on port 5556");
	}

	public void packetReceived(XBeePacket packet) {
		// Skip to 11th byte for start of payload
		byte[] data = Arrays.copyOfRange(packet.getPacketData(), 11, packet.getPacketLength());
		LinkedHashMap<String, String> params = packet.getParameters();
		if (params.get("Frame type").contains("80")) {
			int rssi = Integer.parseInt(params.get("RSSI"), 16);
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
			Date t;
			if (timingMap.containsKey(addr)) {
				t = timingMap.get(addr).getAndSyncTime(sTime, seqNum);
			} else {
				timingMap.put(addr, new SensorTime(sTime, seqNum));
				t = timingMap.get(addr).getTime(sTime);
			}
			while (ib.position() < ib.capacity()) {
				t.setTime(t.getTime() + SensorTime.READING_INTERVAL_MS);
				int d = ib.get();
				StringBuilder sb = new StringBuilder();
				sb.append("UWSP").append(' ');
				sb.append("LAB").append(' ');
				sb.append(type).append(' ');
				sb.append(seqNum).append(' ');
				sb.append(t.getTime()).append(' ');
				sb.append(d);
				zpub.send(sb.toString());
				
				LOGGER.info(String.format("From %s -%ddb >> Seq: %d Time:%d | %dcm%n", addr, rssi, seqNum, t, d));
			}
		}
	}
	
	public void closeSocket() {
		zpub.close();
		zcontext.term();
	}

}
