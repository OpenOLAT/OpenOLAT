/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * This was the test code mebbed in the main method of the Formatter class
 * 
 */
public class FormatterTest {
	
	@Test
	public void testEscapeHtml() {
		Assert.assertEquals("abcdef&amp;&lt;&gt;", StringEscapeUtils.escapeHtml("abcdef&<>"));
		Assert.assertEquals("&amp;#256;&lt;ba&gt;abcdef&amp;&lt;&gt;", StringEscapeUtils.escapeHtml("&#256;<ba>abcdef&<>"));
		Assert.assertEquals("&amp;#256;\n&lt;ba&gt;\nabcdef&amp;&lt;&gt;", StringEscapeUtils.escapeHtml("&#256;\n<ba>\nabcdef&<>"));
	}
	
	@Test
	public void testTruncate() {
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", 0));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", 2));
		Assert.assertEquals("a...", Formatter.truncate("abcdef", 4));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", 6));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", 7));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", 8));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", -2));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", -4));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", -6));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", -7));
		Assert.assertEquals("abcdef", Formatter.truncate("abcdef", -8));
	}

	@Test
	public void testMakeStringFilesystemSave() {
		String ugly = "guido/\\:? .|*\"\"<><guidoöäü";
		Assert.assertEquals("guido%2F%5C%3A%3F+.%7C*%22%22%3C%3E%3Cguido%C3%B6%C3%A4%C3%BC", Formatter.makeStringFilesystemSave(ugly));
	}

	@Test
	public void testUpAndDown() {
		// only one key stroke
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("+").indexOf("<") == 0);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("-").indexOf("<") == 0);

		// space after +/- => should render up or down icon
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("+ ").indexOf("<") == 0);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("- ").indexOf("<") == 0);

		// text after +/- => should NOT render up or down icon, is probably an enumeration
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("+trallala").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("-lustig").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("+ trallala").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("- lustig").indexOf("<") == -1);

		// text before +/- => should NOT render up or down icon
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala-").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala- ").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala -").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala - ").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala-lustig").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala - lustig").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig+").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig+ ").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig +").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig + ").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig+trallala").indexOf("<") == -1);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig + trallala").indexOf("<") == -1);
		
		// in text, render only when in braces like this (+).
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("trallala (-) lustig").indexOf("<") == 9);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("I think it is (-).").indexOf("<") == 14);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("lustig (+) trallala").indexOf("<") == 7);
		Assert.assertTrue(Formatter.formatEmoticonsAsImages("I think it is (+).").indexOf("<") == 14);
	}


	@Test
	public void testFormatTimecode() {
		Assert.assertEquals("0:00", Formatter.formatTimecode(0l));
		Assert.assertEquals("0:01", Formatter.formatTimecode(1000l));
		Assert.assertEquals("0:10", Formatter.formatTimecode(10000l));
		Assert.assertEquals("1:10", Formatter.formatTimecode(70000l));
		Assert.assertEquals("9:59", Formatter.formatTimecode(599000l));
		Assert.assertEquals("13:45", Formatter.formatTimecode(825000l));
		Assert.assertEquals("1:01:01", Formatter.formatTimecode(3661000l));
		Assert.assertEquals("4:03:45", Formatter.formatTimecode(14625000l));
		Assert.assertEquals("4:23:45", Formatter.formatTimecode(15825000l));
		Assert.assertEquals("32:23:45", Formatter.formatTimecode(116625000l));
		Assert.assertEquals("532:23:45", Formatter.formatTimecode(1916625000l));
	}
	
	@Test
	public void elementLatexFormattingScript() {
		String domId = UUID.randomUUID().toString();
		String latextFormatterJs = Formatter.elementLatexFormattingScript(domId);
		Assert.assertNotNull(latextFormatterJs);
		Assert.assertTrue(latextFormatterJs.contains("o_info.latexit"));
	}
	
	@Test
	public void testMailTransformation() {
		// Valid Mails
		List<String> validMails = new ArrayList<>();
		validMails.add("abc.def@mail.cc");
		validMails.add("abc.def@mail-archive.com");
		validMails.add("abc.def@mail.org");
		validMails.add("abc.def@mail.com");
		validMails.add("abc.def@mail.com");
		validMails.add("abc@mail.com");
		validMails.add("abc_def@mail.com");
		validMails.add("abc-d@mail.com");			
		
		// Invalid Mails
		List<String> invalidMails = new ArrayList<>();
		invalidMails.add("abc.def@mail#archive.com");
		invalidMails.add("abc.def@mail");
		invalidMails.add("abc.def@mail..com");
		
		for (String validMail : validMails) {
			String valid = Formatter.formatMailsAsLinks(validMail, false);
			String validIcon = Formatter.formatMailsAsLinks(validMail, true);
			
			Assert.assertTrue(valid.contains("<a"));
			Assert.assertTrue(valid.contains("</a>"));
			
			Assert.assertTrue(validIcon.contains("<a"));
			Assert.assertTrue(validIcon.contains("</a>"));
			Assert.assertTrue(validIcon.contains("<i"));
			Assert.assertTrue(validIcon.contains("</i>"));
		}
		
		for (String invalidMail: invalidMails) {
			invalidMail = Formatter.formatMailsAsLinks(invalidMail, false);
			
			Assert.assertTrue(!invalidMail.contains("<a"));
			Assert.assertTrue(!invalidMail.contains("</a>"));
		}
	}
	
	@Test
	public void testFormatTwoDigitsYearsAsFourDigitsYears() {
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1.2.22"), "1.2.2022");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1/2/22"), "1/2/2022");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1.2.1922"), "1.2.1922");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1/2/1922"), "1/2/1922");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1.2.2023"), "1.2.2023");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1.2.0022"), "1.2.0022");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1/2/0022"), "1/2/0022");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("  1.2.22  "), "1.2.2022");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("1.2."), "1.2.");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("some other stuff"), "some other stuff");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears("some other stuff on 1.2.22"), "some other stuff on 1.2.22");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears(""), "");
		Assert.assertEquals(Formatter.formatTwoDigitsYearsAsFourDigitsYears(null), null);
	}
	
}
