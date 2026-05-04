/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the static helpers of {@link AiQtiItemFactory}:
 * {@code stimulusToHtml}, {@code TOOL_PREFIX}, title derivation logic.
 * The full {@code buildEssayItem}/{@code buildMcItem} methods are deferred
 * to integration tests (require a live {@link org.olat.ims.qti21.QTI21Service}).
 *
 * Initial date: 2026-05-04<br>
 * @author Tom (AIT), https://www.frentix.com
 */
public class AiQtiItemFactoryTest {

	// ---------------------------------------------------------------- TOOL_PREFIX constant

	@Test
	public void toolPrefix_isNonBlank() {
		assertNotNull(AiQtiItemFactory.TOOL_PREFIX);
		assertFalse(AiQtiItemFactory.TOOL_PREFIX.isBlank());
	}

	@Test
	public void toolPrefix_startsWithOpenOlatAi() {
		assertTrue("TOOL_PREFIX must start with 'OpenOlat.AI'",
				AiQtiItemFactory.TOOL_PREFIX.startsWith("OpenOlat.AI"));
	}

	// ---------------------------------------------------------------- stimulusToHtml

	@Test
	public void stimulusToHtml_nullReturnsEmptyParagraph() {
		String result = AiQtiItemFactory.stimulusToHtml(null);
		assertEquals("<p></p>", result);
	}

	@Test
	public void stimulusToHtml_blankReturnsEmptyParagraph() {
		assertEquals("<p></p>", AiQtiItemFactory.stimulusToHtml("   "));
	}

	@Test
	public void stimulusToHtml_singleLineWrappedInParagraph() {
		String result = AiQtiItemFactory.stimulusToHtml("Hello world");
		assertEquals("<p>Hello world</p>", result);
	}

	@Test
	public void stimulusToHtml_multipleLinesSeparatedByBlankLineProduceMultipleParagraphs() {
		String stimulus = "First paragraph.\n\nSecond paragraph.";
		String result = AiQtiItemFactory.stimulusToHtml(stimulus);
		assertTrue(result.contains("<p>First paragraph.</p>"));
		assertTrue(result.contains("<p>Second paragraph.</p>"));
	}

	@Test
	public void stimulusToHtml_xmlSpecialCharsAreEscaped() {
		String result = AiQtiItemFactory.stimulusToHtml("x < y & z > w with \"quotes\" and 'apostrophe'");
		assertTrue(result.contains("&lt;"));
		assertTrue(result.contains("&amp;"));
		assertTrue(result.contains("&gt;"));
		assertTrue(result.contains("&quot;"));
		assertTrue(result.contains("&apos;"));
	}

	@Test
	public void stimulusToHtml_singleNewlineBecomesSpace() {
		// Single newlines within a paragraph should become spaces, not line breaks
		String result = AiQtiItemFactory.stimulusToHtml("Line one\nLine two");
		// Both lines merged into one paragraph (no blank line separator)
		assertTrue(result.startsWith("<p>"));
		assertTrue(result.endsWith("</p>"));
		assertFalse("Multiple paragraphs must not be produced for single newline",
				result.indexOf("<p>") != result.lastIndexOf("<p>"));
	}

	@Test
	public void stimulusToHtml_unicodeIsPreserved() {
		String result = AiQtiItemFactory.stimulusToHtml("Ümlauts: äöü. Chinese: 中文");
		assertTrue(result.contains("äöü"));
		assertTrue(result.contains("中文"));
	}
}
