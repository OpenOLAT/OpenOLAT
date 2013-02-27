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
}
