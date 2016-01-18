package xbeerouter;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

public class SensorTime {
	public static final int MAX_PKT_BUF = 9;
	public static final int READINGS_PER_PKT_LONGS = 22;
	public static final int READING_INTERVAL_MS = 500;
	
	private Date startTime;
	private Boolean roughTime;
	private long seqNum;
	
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public SensorTime(long sTime, long seqNum) {
		initialSync(sTime, seqNum);
		this.seqNum = seqNum;
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
		incrementalSync(sTime);
		return new Date(startTime.getTime() + sTime);
	}
	
	private void initialSync(long sTime, long seqNum) {
		Calendar now = Calendar.getInstance();
		if (seqNum < 10) {
			now.add(Calendar.MINUTE, -2);
			startTime = now.getTime();
			roughTime = true;
		} else {
			LOGGER.warning("Started receiving late readings from previously unknown sensor!");
			now.setTimeInMillis(now.getTimeInMillis() - (2000 * seqNum));
			startTime = now.getTime();
			roughTime = true;
		}
	}
	
	private void incrementalSync(long sTime) {
		Calendar now = Calendar.getInstance();
		Date newStartTime = new Date(
				now.getTimeInMillis() - 
				sTime - 
				(READINGS_PER_PKT_LONGS * READING_INTERVAL_MS)
				);
		if (roughTime) {
			startTime = newStartTime;
			roughTime = false;
		} else {
			Date avgStartTime = new Date(startTime.getTime() + 
					((newStartTime.getTime() - startTime.getTime()) / 2)
					);
			startTime = avgStartTime;
		}
	}

}
