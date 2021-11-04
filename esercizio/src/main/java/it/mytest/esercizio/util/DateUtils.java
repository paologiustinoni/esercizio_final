package it.mytest.esercizio.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateUtils {

	private static Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);
	
	final static String DATE_FORMAT = "yyyy-MM-dd";
	
	public static boolean isDateValid(String date) {
		LOGGER.debug("date={}", date);
		return GenericValidator.isDate(date, DATE_FORMAT, true);
	}
	
    public static boolean isValidInterval(String fromAccountingDate, String toAccountingDate) {
    	LOGGER.debug("fromAccountingDate={}, toAccountingDate={}", fromAccountingDate, toAccountingDate);
		try {
			Date date1 = new SimpleDateFormat(DATE_FORMAT).parse(fromAccountingDate);
			Date date2 = new SimpleDateFormat(DATE_FORMAT).parse(toAccountingDate);
			
			int compareTo = date1.compareTo(date2);
			return (compareTo < 0)?true: false;
			
		} catch (ParseException e) {
			LOGGER.error("{}, {}", e, e.getMessage());		
		}

		return false;
	}
	
}
