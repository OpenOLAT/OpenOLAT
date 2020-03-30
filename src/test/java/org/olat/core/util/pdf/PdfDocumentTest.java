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
package org.olat.core.util.pdf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 23 nov. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class PdfDocumentTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "Hello world", "Hello world" },
                { "Hello\nworld", "Hello world" },
                { "Hello\r\nworld", "Hello  world" },
                { "Hello\tworld", "Hello world" },
                { "Hello\n\tworld", "Hello  world" },
                { "Hello\n\tworld\u00A0", "Hello  world " },
                { "Hello \u3044", "Hello \u3044" },
                { "Hello \u2212", "Hello -" }
        });
    }
    
    private final String string;
    private final String cleanedString;
    
    public PdfDocumentTest(String string, String cleanedString) {
    	this.string = string;
    	this.cleanedString = cleanedString;
    }
	
	@Test
	public void cleanString() {
		String val = PdfDocument.cleanString(string);
		Assert.assertEquals(cleanedString, val);
	}
	
	/**
	 * check if the 
	 * @throws IOException
	 */
	@Test
	public void getStringWidth() throws IOException {
		PdfDocument doc = new PdfDocument(Locale.ENGLISH);
		float width = doc.getStringWidth(string, 12f);
		Assert.assertTrue(width > 0.0f);
	}
}
