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

import org.junit.Test;


/**
 * Description:<br>
 * This test case tests the FileNameSuffixFilter methods
 * 
 * <P>
 * Initial Date:  10.03.2015 <br>
 * @author gnaegi
 */
public class FileNameSuffixFilterTest {
	
	@Test
	public void testNull() {
		FileNameSuffixFilter filter = new FileNameSuffixFilter(null);
		assertTrue(filter.accept(null, "bluber"));
		assertTrue(filter.accept(null, ""));
	}

	@Test
	public void testCaseSensitive() {
		FileNameSuffixFilter filter = new FileNameSuffixFilter(".Xml");
		assertFalse(filter.accept(null, "bluber.xml"));
		assertFalse(filter.accept(null, "bluber.xMl"));
		assertFalse(filter.accept(null, "bluber.XML"));
		assertFalse(filter.accept(null, "bluberXml"));
		assertTrue(filter.accept(null, "bluber.Xml"));
	}

	@Test
	public void testCaseInSensitive() {
		FileNameSuffixFilter filter = new FileNameSuffixFilter(".Xml", false);
		assertTrue(filter.accept(null, "bluber.xml"));
		assertTrue(filter.accept(null, "bluber.xMl"));
		assertTrue(filter.accept(null, "bluber.XML"));
		assertFalse(filter.accept(null, "bluberXml"));
		assertTrue(filter.accept(null, "bluber.Xml"));
	}

}
