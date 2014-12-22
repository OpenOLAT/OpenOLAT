package org.olat.core.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 22.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IPUtilsTest {
	
	@Test
	public void checkRange_start_end() {
		String start = "192.168.5.5";
		String end = "192.168.5.25";
		
		boolean  check1 = IPUtils.isValidRange(start, end, "192.168.5.21");
		Assert.assertTrue(check1);
		
		boolean  check2 = IPUtils.isValidRange(start, end, "192.168.5.45");
		Assert.assertFalse(check2);
	}

	@Test
	public void checkRange_mask_31() {
		String ipWithMask = "192.168.100.1/24";
		
		boolean  allowed1 = IPUtils.isValidRange(ipWithMask, "192.168.100.1");
		Assert.assertTrue(allowed1);

		boolean  notAllowed1 = IPUtils.isValidRange(ipWithMask, "192.168.99.255");
		Assert.assertFalse(notAllowed1);
		boolean  notAllowed2 = IPUtils.isValidRange(ipWithMask, "192.168.101.1");
		Assert.assertFalse(notAllowed2);
		boolean  notAllowed3 = IPUtils.isValidRange(ipWithMask, "212.34.100.0");
		Assert.assertFalse(notAllowed3);
	}
}
