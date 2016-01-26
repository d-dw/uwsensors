package xbeerouter;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/*
 * SensorTime attempts to calculate the start time of individual sensors
 * and syncs that time with the system time, assumed to be accurate.
 * 
 * TODO: Does not detect millisecond overflows of the sensors, as currently 
 * battery will run out long before the overflow of ~60 days.
 */
public class SensorTime {
	public static final int MAX_PKT_BUF = 9;
	public static final int READINGS_PER_PKT_LONGS = 22;
	public static final int READING_INTERVAL_MS = 500;
	
	private Date startTime;
	private long seqNum;
	
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public SensorTime(long sTime, long seqNum) {
		initialSync(sTime, seqNum);
	}
	
	public synchronized Date getTime(long sTime) {
		return new Date(startTime.getTime() + sTime);
	}
	
	public synchronized Date getAndSyncTime(long sTime, long seqNum) {
		if (seqNum + MAX_PKT_BUF < this.seqNum) {
			LOGGER.warning("Possible sensor reset detected!");
			initialSync(sTime, seqNum);
			return new Date(startTime.getTime() + sTime);
		}
		this.seqNum = seqNum;
		return new Date(startTime.getTime() + sTime);
	}
	
	private void initialSync(long sTime, long seqNum) {
		Calendar now = Calendar.getInstance();
		if (seqNum < 9) {
			now.add(Calendar.MILLISECOND, -(MAX_PKT_BUF * READINGS_PER_PKT_LONGS * READING_INTERVAL_MS));
			startTime = now.getTime();
		} else {
			LOGGER.warning("Started receiving late readings from previously unknown sensor!");
			now.setTimeInMillis(now.getTimeInMillis() - (READINGS_PER_PKT_LONGS * READING_INTERVAL_MS * seqNum));
			startTime = now.getTime();
		}
		this.seqNum = seqNum;
	}

}
