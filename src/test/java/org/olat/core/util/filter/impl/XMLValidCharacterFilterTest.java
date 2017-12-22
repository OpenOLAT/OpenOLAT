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
package org.olat.core.util.filter.impl;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.core.util.filter.Filter;

/**
 * 
 * Initial date: 16 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */

@RunWith(Parameterized.class)
public class XMLValidCharacterFilterTest {
	
	private static final Filter xmlValidCharacterFilter = new XMLValidCharacterFilter();
	
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Bla blu blo", "Bla blu blo" },
                { "Bl\u00CB bl\u00EA bl\u00FC", "Bl\u00CB bl\u00EA bl\u00FC" },
                { "\u3042\u306A\u305F\u306E\u4FEE\u6B63\u304C\u4FDD\u5B58\u3055\u308C\u307E\u3057\u305F\u3002",
                		"\u3042\u306A\u305F\u306E\u4FEE\u6B63\u304C\u4FDD\u5B58\u3055\u308C\u307E\u3057\u305F\u3002" },//japan
                { "\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F",
                		"\u0418\u043D\u0444\u043E\u0440\u043C\u0430\u0446\u0438\u044F" }, //russian
                { "Hello\u0018world", "Helloworld"},// u0018 is invalid
                { null, null },// edge cases
                { "", "" }
        });
    }
    
    private String text;
    private String filteredText;
    
    public XMLValidCharacterFilterTest(String text, String filteredText) {
        this.text = text;
        this.filteredText = filteredText;
    }
	
	@Test
	public void testAscii() {
		String filtered = xmlValidCharacterFilter.filter(text);
		Assert.assertEquals(filteredText, filtered);
	}

}
