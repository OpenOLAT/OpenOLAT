/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jakarta.mail.Address;
import jakarta.mail.internet.InternetAddress;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 7 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DateCellRendererTest {
	
	@Test
	public void formatDe() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 9);
		cal.set(Calendar.MONTH, 4);
		cal.set(Calendar.YEAR, 2018);
		Date date = cal.getTime();
		String formattedDate = DateCellRenderer.format(date, Locale.GERMAN);
		Assert.assertEquals("9. Mai 2018", formattedDate);
	}
	
	@Test
	public void formatEe() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 9);
		cal.set(Calendar.MONTH, 5);
		cal.set(Calendar.YEAR, 2018);
		Date date = cal.getTime();
		String formattedDate = DateCellRenderer.format(date);
		Assert.assertEquals("09 June 2018", formattedDate);
	}
	
	@Test
	public void equalsAdd() throws Exception {
		
		Address add1 = new InternetAddress("test@frentix.com");
		Address add2 = new InternetAddress("test@frentix.com");
		Assert.assertTrue(add1.equals(add2));
		
	}
}
