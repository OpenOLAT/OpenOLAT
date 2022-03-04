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
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 4 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfDocumentSplitTest {
	
	@Test
	public void splitTextInPartsDegenerated() throws IOException {
		PdfDocument doc = new PdfDocument(Locale.ENGLISH);
		String[] splittedTexts = doc.splitTextInParts("", 210.0f, 10.0f);
		Assert.assertEquals(1, splittedTexts.length);
	}
	
	@Test
	public void splitTextInPartsSingleWord() throws IOException {
		PdfDocument doc = new PdfDocument(Locale.ENGLISH);
		String[] splittedTexts = doc.splitTextInParts("Hello", 210.0f, 10.0f);
		Assert.assertEquals(1, splittedTexts.length);
		Assert.assertEquals("Hello", splittedTexts[0]);
	}
	
	@Test
	public void splitTextInParts() throws IOException {
		PdfDocument doc = new PdfDocument(Locale.ENGLISH);
		String text = "Hiermit erkl\u00E4re ich mich damit einverstanden, dass bei der Pr\u00FCfung Regelungstechnik eine Videoaufsicht zu den im Vorfeld genannten Bedingungen durchgef\u00FChrt wird.";
		String[] splittedTexts = doc.splitTextInParts(text, 210.0f, 10.0f);
		Assert.assertEquals(4, splittedTexts.length);
	}

}
