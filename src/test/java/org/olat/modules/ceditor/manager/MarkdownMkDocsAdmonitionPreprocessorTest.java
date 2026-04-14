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

import org.junit.Test;

/**
 * White-box tests for {@link MarkdownMkDocsAdmonitionPreprocessor}: verify the
 * generated blockquote form directly, independent of the CommonMark parser and
 * the visitor.
 *
 * Initial date: 2026-04-13<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownMkDocsAdmonitionPreprocessorTest {

	// --- Basic transforms ---

	@Test
	public void testNullInputReturnsNull() {
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(null)).isNull();
	}

	@Test
	public void testTextWithoutMarkerIsUnchanged() {
		String input = "# Heading\n\nParagraph text.";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(input);
	}

	@Test
	public void testBasicAdmonitionWithTitle() {
		String input = "!!! info \"Heads up\"\n    Body line.";
		// A blank-line separator is appended so adjacent blockquotes don't merge
		String expected = "> [!INFO|Heads up]\n> Body line.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testAdmonitionWithoutTitle() {
		String input = "!!! warning\n    Be careful.";
		String expected = "> [!WARNING]\n> Be careful.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testAdmonitionWithEmptyTitleIsPreserved() {
		// Empty quoted title must round-trip so the visitor can suppress the title
		String input = "!!! note \"\"\n    Body.";
		String expected = "> [!NOTE|]\n> Body.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testTypeIsUppercased() {
		String input = "!!! Info \"Mixed Case\"\n    x.";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input))
			.startsWith("> [!INFO|Mixed Case]");
	}

	// --- Content / indentation ---

	@Test
	public void testTabIndentedContent() {
		String input = "!!! tip\n\tTab body.";
		String expected = "> [!TIP]\n> Tab body.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testMultipleContentLines() {
		String input = "!!! info \"T\"\n    Line one.\n    Line two.";
		String expected = "> [!INFO|T]\n> Line one.\n> Line two.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testBlankLineInsideBlockIsKept() {
		String input = "!!! info \"T\"\n    Para 1.\n\n    Para 2.";
		String expected = "> [!INFO|T]\n> Para 1.\n> \n> Para 2.\n\n";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(expected);
	}

	@Test
	public void testNonIndentedLineEndsBlock() {
		String input = "!!! info \"T\"\n    Indented.\nNormal paragraph.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).contains("> [!INFO|T]\n> Indented.\n");
		assertThat(result).contains("Normal paragraph.");
		// The normal paragraph must not be included in the blockquote
		assertThat(result).doesNotContain("> Normal paragraph.");
	}

	@Test
	public void testTrailingBlankLinesBeforeFollowingContent() {
		String input = "!!! info \"T\"\n    Indented.\n\n\nFollowing.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		// The trailing blanks that were consumed but had no further content
		// are restored as plain blank lines outside the blockquote.
		assertThat(result).contains("> [!INFO|T]\n> Indented.\n");
		assertThat(result).contains("Following.");
	}

	// --- Adjacent admonitions ---

	@Test
	public void testAdjacentAdmonitionsWithoutBlankLine() {
		String input = "!!! info \"A\"\n    x.\n!!! warning \"B\"\n    y.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).contains("> [!INFO|A]\n> x.\n");
		assertThat(result).contains("> [!WARNING|B]\n> y.\n");
	}

	// --- Code fence protection ---

	@Test
	public void testMarkerInsideBacktickFenceNotTransformed() {
		String input = "```\n!!! info \"inside\"\n    x\n```";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(input);
	}

	@Test
	public void testMarkerInsideTildeFenceNotTransformed() {
		String input = "~~~\n!!! info \"inside\"\n    x\n~~~";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(input);
	}

	@Test
	public void testMarkerAfterFenceIsTransformed() {
		String input = "```\ncode\n```\n!!! info \"After\"\n    body.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).startsWith("```\ncode\n```\n");
		assertThat(result).contains("> [!INFO|After]\n> body.\n");
	}

	// --- Title sanitization ---

	@Test
	public void testTitleHtmlTagsAreStripped() {
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(
			"!!! info \"Hello <b>world</b>\"\n    body.");
		assertThat(result).startsWith("> [!INFO|Hello world]\n");
	}

	@Test
	public void testTitleScriptTagsAreRemoved() {
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(
			"!!! info \"<script>alert(1)</script>Safe\"\n    body.");
		// Script tag AND its content (via the `<[^>]*>` then remaining text)
		// The <script> opening and </script> closing are removed as tags, and
		// the inner "alert(1)" remains as plain text — but `(` and `)` are
		// safe, so it stays; we just verified no tags.
		assertThat(result).doesNotContain("<script");
		assertThat(result).doesNotContain("</script");
	}

	@Test
	public void testTitleMarkdownInlineCharsStripped() {
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(
			"!!! info \"a*b_c`d[e]f~g=h\"\n    body.");
		assertThat(result).startsWith("> [!INFO|abcdefgh]\n");
	}

	@Test
	public void testTitleSeparatorBracketStripped() {
		// ']' in the title would break the admonition regex — must be stripped
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(
			"!!! info \"Not a ]link\"\n    body.");
		assertThat(result).doesNotContain("]link");
		assertThat(result).startsWith("> [!INFO|Not a link]\n");
	}

	@Test
	public void testSanitizeTitleWithNullReturnsNull() {
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.sanitizeTitle(null)).isNull();
	}

	@Test
	public void testSanitizeTitlePreservesSafePunctuation() {
		String result = MarkdownMkDocsAdmonitionPreprocessor.sanitizeTitle(
			"Version 2.0 (beta): check it out!");
		assertThat(result).isEqualTo("Version 2.0 (beta): check it out!");
	}

	// --- Negatives / edge cases ---

	@Test
	public void testMarkerWithoutIndentedContentProducesEmptyBlockquote() {
		String input = "!!! info \"T\"\n\nNext paragraph.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).startsWith("> [!INFO|T]\n");
		assertThat(result).contains("Next paragraph.");
	}

	@Test
	public void testThreeExclamationMarksAlonePreserved() {
		// Just "!!!" with nothing else is not a valid MkDocs admonition opener
		String input = "!!!\nSome text.";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).startsWith("!!!\n");
	}

	@Test
	public void testMarkerWithTooMuchLeadingIndentNotMatched() {
		// More than 3 leading spaces turns the line into an indented code block
		String input = "    !!! info \"T\"\n    body.";
		assertThat(MarkdownMkDocsAdmonitionPreprocessor.preprocess(input)).isEqualTo(input);
	}

	@Test
	public void testFinalNewlinePreserved() {
		String input = "!!! info \"T\"\n    body.\n";
		String result = MarkdownMkDocsAdmonitionPreprocessor.preprocess(input);
		assertThat(result).endsWith("\n");
	}
}
