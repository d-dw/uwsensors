package xbeerouter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zeromq.ZMQ;

import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.packet.XBeePacket;

public class XBeePacketListener implements IPacketReceiveListener {
	
	private ZMQ.Context zcontext;
	private ZMQ.Socket zpub;
	
	public XBeePacketListener() {
		zcontext = ZMQ.context(1);
		zpub = zcontext.socket(ZMQ.PUB);
		zpub.bind("tcp://127.0.0.1:5556");
		System.out.println("ZMQ Publishing");
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
			// Skip nonsense packets
			if (h1 != 0x6B || h2 != 0x57)
				return;
			byte vers = bb.get();
			byte type = bb.get();
			IntBuffer ib = bb.asIntBuffer();
			long seqnum = 0xFFFFFFFFl & (long) ib.get();
			long startTime = 0xFFFFFFFFl & (long) ib.get();
			long t = startTime;
			while (ib.position() < ib.capacity()) {
				t += 500;
				int d = ib.get();
				zpub.send("UWS" + type + " S" + seqnum + "T" + t + ':' + d);
				
				System.out.format("From %s -%ddb >> Seq: %d Time:%d | %dcm%n", addr, rssi, seqnum, t, d);
			}
		}
	}
	
	public void closeSocket() {
		zpub.close();
		zcontext.term();
	}

}
