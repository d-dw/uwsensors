package xbeerouter;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.logging.Logger;

// Stores the schedule of open and closed store hours
public class SiteSchedule {
	LocalTime[] openTimes = new LocalTime[7];
	LocalTime[] closedTimes = new LocalTime[7];
	String[] weekdays = {"sunday", "monday", "tuesday", "wednesday", "thursday",
	                   "friday", "saturday"};
	private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public SiteSchedule(Properties prop) {
		String[] times = new String[2];
		for (int i = 0; i < 7; i++) {
			times = prop.getProperty(weekdays[i] + "hours").split("\\s+", 2);
			openTimes[i] = LocalTime.parse(times[0]);
			closedTimes[i] = LocalTime.parse(times[1]);
			if (openTimes[i].isAfter(closedTimes[i])) {
				LOGGER.severe("Closed time before open time in hours for " + weekdays[i]);
				System.exit(1);
			}
		}
	}
	
	public boolean closed() {
		Calendar now = GregorianCalendar.getInstance();
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;
		LocalTime nowLT = LocalTime.from(now.getTime().toInstant());
		return openTimes[dayOfWeek].isAfter(nowLT) ||
				closedTimes[dayOfWeek].isBefore(nowLT);
	}
	
	// Returns milliseconds to open
	public long timeToOpen() {
		if (!closed())
			return 0;
		Calendar now = GregorianCalendar.getInstance();
		if (now.get(Calendar.AM_PM) == Calendar.PM) {
			now.add(Calendar.DAY_OF_YEAR, 1);
		}
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
		LocalTime nowLT = LocalTime.now();
		LocalTime openLT = openTimes[dayOfWeek - 1];
		long msToOpen = Duration.between(nowLT, openLT).toMillis();
		LOGGER.info("ms to open time: " + msToOpen);
		return msToOpen;
	}
}

