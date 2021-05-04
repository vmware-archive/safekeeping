package com.vmware.safekeeping.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SimpleTimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class DateUtility {
	/**
	 * @return
	 */
	public static String convertGregorianCurrentTimeToString() {
		return convertGregorianToString(new GregorianCalendar());
	}

	public static String convertGregorianToString(final GregorianCalendar gCalendar) {
		final DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm a z");
		// Converted to date object
		final Date date = gCalendar.getTime();
		// Formatted to String value
		return df.format(date);
	}

	public static String convertXmlGregorianToString(final XMLGregorianCalendar xc) {
		return DateUtility.convertGregorianToString(xc.toGregorianCalendar());
	}

	public static XMLGregorianCalendar getXMLGregorianCalendar(final GregorianCalendar gregorianCalendar)
			throws DatatypeConfigurationException {
		DatatypeFactory datatypeFactory;
		datatypeFactory = DatatypeFactory.newInstance();
		return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);

	}

	public static XMLGregorianCalendar getXMLGregorianCalendarNow() throws DatatypeConfigurationException {
		return getXMLGregorianCalendar(new GregorianCalendar());
	}

	public static Calendar toCalendar(final Date date) {
		final Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal;
	}

	public static String toGMTString(final Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.setTimeZone(new SimpleTimeZone(0, "GMT"));
		sdf.applyPattern("dd MMM yyyy HH:mm:ss z");
		return sdf.format(date);

	}

	private DateUtility() {
		throw new IllegalStateException("Utility class");
	}

}
