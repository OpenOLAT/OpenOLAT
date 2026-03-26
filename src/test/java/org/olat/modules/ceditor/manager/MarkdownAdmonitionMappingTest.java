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
package org.olat.modules.ceditor.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.commonmark.node.BlockQuote;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.junit.Test;
import org.olat.modules.ceditor.manager.MarkdownAdmonitionMapping.AdmonitionResult;
import org.olat.modules.ceditor.model.AlertBoxType;

/**
 * Unit tests for MarkdownAdmonitionMapping.
 *
 * Initial date: 2026-03-16<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownAdmonitionMappingTest {

	private final Parser parser = Parser.builder()
		.extensions(MarkdownImportService.markdownExtensions())
		.build();

	private BlockQuote parseBlockQuote(String markdown) {
		Node doc = parser.parse(markdown);
		Node child = doc.getFirstChild();
		assertThat(child).isInstanceOf(BlockQuote.class);
		return (BlockQuote) child;
	}

	@Test
	public void testDetectNote() {
		BlockQuote bq = parseBlockQuote("> [!NOTE]\n> Some note text");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.note);
	}

	@Test
	public void testDetectWarning() {
		BlockQuote bq = parseBlockQuote("> [!WARNING]\n> Be careful");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.warning);
	}

	@Test
	public void testDetectCautionMapsToError() {
		BlockQuote bq = parseBlockQuote("> [!CAUTION]\n> Danger zone");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.error);
	}

	@Test
	public void testDetectTip() {
		BlockQuote bq = parseBlockQuote("> [!TIP]\n> A helpful tip");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.tip);
	}

	@Test
	public void testDetectImportant() {
		BlockQuote bq = parseBlockQuote("> [!IMPORTANT]\n> Read this");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.important);
	}

	@Test
	public void testDetectInfo() {
		BlockQuote bq = parseBlockQuote("> [!INFO]\n> Information");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.info);
	}

	@Test
	public void testDetectSuccess() {
		BlockQuote bq = parseBlockQuote("> [!SUCCESS]\n> It worked");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.success);
	}

	@Test
	public void testDetectError() {
		BlockQuote bq = parseBlockQuote("> [!ERROR]\n> Something failed");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.error);
	}

	@Test
	public void testDetectCaseInsensitive() {
		BlockQuote bq = parseBlockQuote("> [!note]\n> lowercase");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNotNull();
		assertThat(result.type()).isEqualTo(AlertBoxType.note);
	}

	@Test
	public void testDetectUnknownReturnsNull() {
		BlockQuote bq = parseBlockQuote("> [!CUSTOM]\n> custom type");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNull();
	}

	@Test
	public void testPlainBlockquoteReturnsNull() {
		BlockQuote bq = parseBlockQuote("> Just a plain quote");
		AdmonitionResult result = MarkdownAdmonitionMapping.detectAdmonition(bq);

		assertThat(result).isNull();
	}

	@Test
	public void testMarkerRemovedFromAst() {
		BlockQuote bq = parseBlockQuote("> [!WARNING]\n> Remaining text");
		MarkdownAdmonitionMapping.detectAdmonition(bq);

		// After detection, the [!WARNING] text and SoftLineBreak should be removed.
		// The first paragraph should now start with "Remaining text".
		Node firstChild = bq.getFirstChild();
		assertThat(firstChild).isInstanceOf(Paragraph.class);
		Node firstInline = firstChild.getFirstChild();
		assertThat(firstInline).isInstanceOf(Text.class);
		assertThat(((Text) firstInline).getLiteral()).isEqualTo("Remaining text");
	}
}
