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
public class XMLValidEntityFilterTest {
	
	private static final Filter xmlValidEntityFilter = new XMLValidEntityFilter();
	
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Bla blu blo", "Bla blu blo" },
                { "Hello&#23;world", "Helloworld"},// u0018 is invalid
                { "Hello&&cc;world", "Hello&&cc;world"},// this is not an entity
                { "Hello&#23;wor&#23;ld", "Helloworld"},// 2x u0018 invalid
                { "Hello&amp;world", "Hello&amp;world"},// valid entity
                { "Hello&#234;world", "Hello&#234;world"},// valid entity
                { "Hello&#xA;world", "Hello&#xA;world"},// valid entity
                { "Hello&#x3;world", "Helloworld"},// invalid entity
                { "&#12470;&#x30B6;", "&#12470;&#x30B6;"},// japanese entities
                { null, null },// edge cases
                { "", "" }
        });
    }
    
    private String text;
    private String filteredText;
    
    public XMLValidEntityFilterTest(String text, String filteredText) {
        this.text = text;
        this.filteredText = filteredText;
    }
	
	@Test
	public void testAscii() {
		String filtered = xmlValidEntityFilter.filter(text);
		Assert.assertEquals(filteredText, filtered);
	}

}
