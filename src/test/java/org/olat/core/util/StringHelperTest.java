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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * Description:<br>
 * This test case tests the StringHelper methods
 * 
 * <P>
 * Initial Date:  13.07.2010 <br>
 * @author gnaegi
 */
public class StringHelperTest {
	
	@Test
	public void base64() throws Exception {
		String str = "this a super secret string avec un \u00E9 et encore quelques charact\u00E8res kanji \u30b0.";

		String xstream64 = new com.thoughtworks.xstream.core.util.Base64Encoder(true).encode(str.getBytes());
		String infinispan64 = org.infinispan.commons.util.Base64.encodeBytes(str.getBytes());
		String olat64 = StringHelper.encodeBase64(str);
		String olatBytes64 = StringHelper.encodeBase64(str.getBytes());
		//encode and compare
		Assert.assertEquals(xstream64, infinispan64);
		Assert.assertEquals(infinispan64, olat64);
		Assert.assertEquals(infinispan64, olatBytes64);
		
		//decode with the same coder
		Assert.assertEquals(str, new String(org.infinispan.commons.util.Base64.decode(infinispan64)));
		Assert.assertEquals(str, new String(new com.thoughtworks.xstream.core.util.Base64Encoder(true).decode(xstream64)));
		Assert.assertEquals(str, StringHelper.decodeBase64(olat64));
		Assert.assertEquals(str, StringHelper.decodeBase64(olatBytes64));
		
		//decode with an other decoder
		Assert.assertEquals(str, new String(org.infinispan.commons.util.Base64.decode(olat64)));
		Assert.assertEquals(str, new String(org.infinispan.commons.util.Base64.decode(olatBytes64)));
		Assert.assertEquals(str, new String(new com.thoughtworks.xstream.core.util.Base64Encoder(true).decode(olat64)));
		Assert.assertEquals(str, new String(new com.thoughtworks.xstream.core.util.Base64Encoder(true).decode(olatBytes64)));
		Assert.assertEquals(str, StringHelper.decodeBase64(infinispan64));
		Assert.assertEquals(str, StringHelper.decodeBase64(xstream64));
	}

	@Test
	public void testContainsNonWhitespace() {
		// positive tests
		assertTrue(StringHelper.containsNonWhitespace("asdf"));
		assertTrue(StringHelper.containsNonWhitespace("  asdf"));
		assertTrue(StringHelper.containsNonWhitespace("asdf  "));
		assertTrue(StringHelper.containsNonWhitespace("asdf  t\r"));
		assertTrue(StringHelper.containsNonWhitespace("hello world"));
		// negative tests
		assertFalse(StringHelper.containsNonWhitespace(null));
		assertFalse(StringHelper.containsNonWhitespace(""));
		assertFalse(StringHelper.containsNonWhitespace(" "));
		assertFalse(StringHelper.containsNonWhitespace("             "));
		assertFalse(StringHelper.containsNonWhitespace("  \t  \r"));
	}
	
	@Test
	public void transformDisplayNameToFileSystemName() {
		Assert.assertEquals("Webclass_Energie_2004_2005", StringHelper.transformDisplayNameToFileSystemName("Webclass Energie 2004/2005"));
		Assert.assertEquals("Webclass_Energie_2004_2005", StringHelper.transformDisplayNameToFileSystemName("Webclass Energie 2004\\2005"));
		Assert.assertEquals("Webclass_Energie_20042005", StringHelper.transformDisplayNameToFileSystemName("Webclass Energie 2004:2005"));
		Assert.assertEquals("Webclaess", StringHelper.transformDisplayNameToFileSystemName("Webcl\u00E4ss"));
	}
	
	@Test
	public void filterPrintControlCharacter() {
		String value1 = StringHelper.cleanUTF8ForXml("Hello world");
		Assert.assertEquals("Dummy test", "Hello world", value1);
		
		//print control
		String value2 = StringHelper.cleanUTF8ForXml("Hello\u0002 world");
		Assert.assertEquals("Print \\x02 test", "Hello world", value2);

		//print control
		String value3 = StringHelper.cleanUTF8ForXml("Hello\u001F world");
		Assert.assertEquals("Print \\x02 like test", "Hello world", value3);

		//it's a 0
		String value4 = StringHelper.cleanUTF8ForXml("Hello\u0030 world");
		Assert.assertEquals("Zero test", "Hello0 world", value4);
			
		//it's a u umlaut
		String value5 = StringHelper.cleanUTF8ForXml("Hello\u00FC world");
		Assert.assertEquals("Umlaut test", "Hello\u00FC world", value5);
					
		//it's a kanji
		String value6 = StringHelper.cleanUTF8ForXml("Hello\u30b0 world");
		Assert.assertEquals("Kanji test", "Hello\u30b0 world", value6);
		
		//it's a return
		String value7 = StringHelper.cleanUTF8ForXml("Hello\n world");
		Assert.assertEquals("Return test", "Hello\n world", value7);
		
		//it's a tab
		String value8 = StringHelper.cleanUTF8ForXml("Hello\t world");
		Assert.assertEquals("Tab test", "Hello\t world", value8);
		
		//it's a carriage
		String value9 = StringHelper.cleanUTF8ForXml("Hello\r world");
		Assert.assertEquals("Carriage test", "Hello\r world", value9);
		
		//it's a unicode emoticons
		String value10 = StringHelper.cleanUTF8ForXml("Hello\u1F605 world");
		Assert.assertEquals("Emoticons test", "Hello\u1F605 world", value10);
		
		//it's phoenician \u1090x
		String value11 = StringHelper.cleanUTF8ForXml("Hello\u1090x phoenician");
		Assert.assertEquals("Phoenician test", "Hello\u1090x phoenician", value11);
		
		//it's pahlavi \u10B7x
		String value12 = StringHelper.cleanUTF8ForXml("Hello\u10B7x pahlavi");
		Assert.assertEquals("Pahlavi test", "Hello\u10B7x pahlavi", value12);

		// smile
		String value13 = StringHelper.cleanUTF8ForXml("Smile \uD83D\uDE00x");
		Assert.assertEquals("Smile test", "Smile \uD83D\uDE00x", value13);
	}
	
	@Test
	public void isLong() {
		Assert.assertTrue(StringHelper.isLong("234"));
		Assert.assertTrue(StringHelper.isLong("0123456789"));
		Assert.assertTrue(StringHelper.isLong("9223372036854775807"));
		Assert.assertTrue(StringHelper.isLong("-9223372036854775807"));

		//check some unacceptable strings
		Assert.assertFalse(StringHelper.isLong("10223372036854775807"));
		Assert.assertFalse(StringHelper.isLong("-dru"));
		Assert.assertFalse(StringHelper.isLong("OpenOLAT"));
		Assert.assertFalse(StringHelper.isLong("A very long number with a lot of characters"));
		
		//check ascii range
		Assert.assertFalse(StringHelper.isLong("/"));
		Assert.assertFalse(StringHelper.isLong(":"));
		Assert.assertFalse(StringHelper.isLong("."));
		Assert.assertFalse(StringHelper.isLong(";"));
	}
	
	@Test
	public void isHtml() {
		//simply text
		Assert.assertFalse(StringHelper.isHtml("Hello world"));
		Assert.assertFalse(StringHelper.isHtml("Hello > world"));
		Assert.assertFalse(StringHelper.isHtml("Mathemtics 5<6"));
		Assert.assertFalse(StringHelper.isHtml("Du & ich"));
		Assert.assertFalse(StringHelper.isHtml("http://a.link.to.some.where/bla.html?key=arg&umen=ts"));
		Assert.assertFalse(StringHelper.isHtml("http://some.domain:8080/olat/dmz/registration/index.html?key=b67a28bd5e5820155b3ba496ef16d1d9&language=de"));
		//was recognized as HTML because &lang; is an entity
		Assert.assertFalse(StringHelper.isHtml("http://some.domain:8080/olat/dmz/registration/index.html?key=b67a28bd5e5820155b3ba496ef16d1d9&lang=de"));
		
		//good and bad html code
		Assert.assertTrue(StringHelper.isHtml("Hello <p>world</p>"));
		Assert.assertTrue(StringHelper.isHtml("<ul><li>Hello<li>world</ul>"));
		Assert.assertTrue(StringHelper.isHtml("Hello<br>world"));
		Assert.assertTrue(StringHelper.isHtml("<html><head></head><body>Hello world</body></html>"));
		Assert.assertTrue(StringHelper.isHtml("<html><head></head><body><p>Hello world</p></body></html>"));
	}
	
	@Test
	public void formatAsCSVString() {
		List<String> entries = new ArrayList<>();
		entries.add("Hell\"o\"");
		entries.add("Test,dru,");
		entries.add("Final");
		String csv = StringHelper.formatAsCSVString(entries);
		Assert.assertEquals("Hell\"o\",Test,dru,,Final", csv);
	}
	
	@Test
	public void formatAsEscapedCSVString() {
		List<String> entries = new ArrayList<>();
		entries.add("Hell\"o\"");
		entries.add("Test,dru,");
		entries.add("Final");
		String csv = StringHelper.formatAsEscapedCSVString(entries);
		Assert.assertEquals("Hell\"\"o\"\",\"Test,dru,\",Final", csv);
	}
}
