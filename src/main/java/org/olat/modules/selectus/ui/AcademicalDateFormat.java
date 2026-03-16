/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

/**
 * 
 * Initial date: 9 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum AcademicalDateFormat {
	
	monthYear("mm.yyyy"),
	year("yyyy");
	
	private final String format;

	private AcademicalDateFormat(String format) {
		this.format = format;
	}
	
	public static AcademicalDateFormat[] yearsOnly() {
		return new AcademicalDateFormat[] { AcademicalDateFormat.year };
	}
	
	public String format() {
		return format;
	}
	
	public static AcademicalDateFormat format(String text) {
		for(AcademicalDateFormat value:values()) {
			if(value.format.equals(text)) {
				return value;
			}
		}
		return year;
	}
	
	public static boolean hasFormat(AcademicalDateFormat format, AcademicalDateFormat[] formatArray) {
		boolean found = false;
		if(formatArray == null) {
			found = false;
		} else if(formatArray.length == 1) {
			found = formatArray[0] == format;
		} else {
			AcademicalDateFormat[] allFormats = values();
			for(int i=allFormats.length; i-->0; ) {
				if(allFormats[i] == format) {
					found = true;
				}
			}
		}
		return found;
	}
}
