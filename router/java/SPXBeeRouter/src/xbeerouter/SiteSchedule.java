package xbeerouter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

// Stores the schedule of open and closed store hours
public class SiteSchedule {
	LocalTime[] openTimes = new LocalTime[7];
	LocalTime[] closedTimes = new LocalTime[7];
	String[] weekdays = {"sunday", "monday", "tuesday", "wednesday", "thursday",
	                   "friday", "saturday"};
	String timezone;
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
			timezone = prop.getProperty("timezone", "America/Los_Angeles");
		}
	}
	
	public boolean closed() {
		Calendar now = GregorianCalendar.getInstance(TimeZone.getTimeZone(timezone));
		int dayOfWeek = now.get(Calendar.DAY_OF_WEEK) - 1;
		LocalTime nowLT = LocalTime.now();
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
		Temporal nowT = LocalDateTime.now();
		LocalTime openLT = openTimes[dayOfWeek - 1];
		Temporal openT = openLT.atDate(LocalDate.ofYearDay(now.get(Calendar.YEAR), now.get(Calendar.DAY_OF_YEAR)));
		long msToOpen = Duration.between(nowT, openT).toMillis();
		LOGGER.info("ms to open time: " + msToOpen);
		return msToOpen;
	}
	
}

