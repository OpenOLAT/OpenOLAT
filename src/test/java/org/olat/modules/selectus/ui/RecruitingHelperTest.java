/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RecruitingHelperTest {
	
	@Test
	public void testUmlaut() {
		String test = RecruitingHelper.normalizeFilename("öüä");
		Assert.assertNotNull(test);
		Assert.assertEquals("oeueae", test);
		
		String test2 = RecruitingHelper.normalizeFilename("ÖÜÄ");
		Assert.assertNotNull(test2);
		Assert.assertEquals("OeUeAe", test2);
	}
	
	@Test
	public void testEmailSplitters() {
		String text = "support@frentix.com;admin@frentix.com,,selectus@frentix.com;";
		List<String> emails = RecruitingHelper.splitEmails(text);
		Assert.assertEquals(3, emails.size());
		Assert.assertEquals("support@frentix.com", emails.get(0));
		Assert.assertEquals("admin@frentix.com", emails.get(1));
		Assert.assertEquals("selectus@frentix.com", emails.get(2));
	}
}
