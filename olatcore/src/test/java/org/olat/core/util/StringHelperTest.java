/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Description:<br>
 * This test case tests the StringHelper methods
 * 
 * <P>
 * Initial Date:  13.07.2010 <br>
 * @author gnaegi
 */
@RunWith(JUnit4.class)
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
}
