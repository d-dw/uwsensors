package xbeereceiver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;

import org.zeromq.ZMQ;

import com.digi.xbee.api.listeners.IPacketReceiveListener;
import com.digi.xbee.api.packet.XBeePacket;

public class MyDemoPacketListener implements IPacketReceiveListener {
	
	private ZMQ.Context zcontext;
	private ZMQ.Socket zpub;
	private String url = "http://www.rkocielnik.com/Business2/upload.php?";
	
	public MyDemoPacketListener() {
		zcontext = ZMQ.context(1);
		zpub = zcontext.socket(ZMQ.PUB);
		zpub.bind("tcp://127.0.0.1:5556");
		System.out.println("ZMQ Publishing");
	}

	@Override
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
				int d = ib.get();
				if (d > 400)
					d = 400;
				zpub.send("DEMO" + d);
				
				HttpURLConnection conn = null;
				try {
					String urlParams = "sensor_id=" + URLEncoder.encode("DEMO", "UTF-8") +
							"&value=" + URLEncoder.encode(Integer.toString(d), "UTF-8");
					URL requrl = new URL(url + urlParams);
					conn = (HttpURLConnection) requrl.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Content-Type", "text/html");
					conn.setRequestProperty("charset", "utf-8");
					conn.setDoOutput(true);
					conn.setDoInput(true);
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						System.out.println("HTTPGET: " + line);
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (conn != null)
						conn.disconnect();
				}
				System.out.format("From %s -%ddb >> Seq: %d Time:%d | %dcm%n", addr, rssi, seqnum, t, d);
			}
		}
	}
	
	public void closeSocket() {
		zpub.close();
		zcontext.term();
	}

}
