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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 19 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextModeTest {
	
	@Test
	public void textModeOneLine_text() {
		String text = "bla bla";
		TextMode mode1 = TextMode.guess(text);
		Assert.assertEquals(TextMode.oneLine, mode1);
		
		String fromOneLine = TextMode.fromOneLine(text);
		Assert.assertEquals("<p>bla bla</p>", fromOneLine);

		String toOneLine = TextMode.toOneLine(text);
		Assert.assertEquals("bla bla", toOneLine);
	}
	
	@Test
	public void textModeOneLine_paragraph() {
		String paragraph = "<p>bla bla</p>"; 
		
		TextMode mode2 = TextMode.guess("paragraph");
		Assert.assertEquals(TextMode.oneLine, mode2);

		String toOneLine = TextMode.toOneLine(paragraph);
		Assert.assertEquals("bla bla", toOneLine);
	}
	
	@Test
	public void guessMultiLine() {
		TextMode mode1 = TextMode.guess("bla<br>bla");
		Assert.assertEquals(TextMode.multiLine, mode1);
		TextMode mode1alt = TextMode.guess("bla<br/>bla");
		Assert.assertEquals(TextMode.multiLine, mode1alt);
		TextMode mode1alt_b = TextMode.guess("bla<br />bla");
		Assert.assertEquals(TextMode.multiLine, mode1alt_b);
		
		TextMode mode2 = TextMode.guess("<p>bla</p><p>bla</p>");
		Assert.assertEquals(TextMode.multiLine, mode2);
		
		TextMode mode3 = TextMode.guess("<p>bla<br>bla</p>");
		Assert.assertEquals(TextMode.multiLine, mode3);
	}	
	
	@Test
	public void guessFormatted() {
		TextMode mode = TextMode.guess("bla<img src='openolat.png'>bla");
		Assert.assertEquals(TextMode.formatted, mode);
	}
	
	@Test
	public void toMultiLine() {
		String linedText1 = TextMode.toMultiLine("<p>Lorem ipsum </p>\n   <p>dolor sit amet </p>");
		Assert.assertEquals("Lorem ipsum\ndolor sit amet", linedText1);
		
		String linedText2 = TextMode.toMultiLine("<p>Lorem</p>ipsum<br>dolor<p>sit amet</p>");
		Assert.assertEquals("Lorem\nipsum\ndolor\nsit amet", linedText2);
	}
}
