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

import java.util.List;
import java.util.Map;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.junit.Before;
import org.junit.Test;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.PagePart;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.AlertBoxType;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.model.TableContent;
import org.olat.modules.ceditor.model.TextSettings;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.model.jpa.CodePart;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.model.jpa.SpacerPart;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.model.jpa.TitlePart;

/**
 * Pure unit tests for MarkdownPagePartVisitor (no Spring context, no database).
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownPagePartVisitorTest {

	private Parser parser;

	@Before
	public void setUp() {
		parser = Parser.builder()
			.extensions(MarkdownImportService.markdownExtensions())
			.build();
	}

	private List<PagePart> convert(String markdown) {
		return convert(markdown, Map.of());
	}

	private List<PagePart> convert(String markdown, Map<String, String> mathBlocks) {
		return convertWithWarnings(markdown, mathBlocks).parts();
	}

	private VisitorResult convertWithWarnings(String markdown) {
		return convertWithWarnings(markdown, Map.of());
	}

	private VisitorResult convertWithWarnings(String markdown, Map<String, String> mathBlocks) {
		Node document = parser.parse(markdown);
		MarkdownPagePartVisitor visitor = new MarkdownPagePartVisitor(null, null, null, null, mathBlocks, null);
		document.accept(visitor);
		return new VisitorResult(visitor.getParts(), visitor.getWarnings());
	}

	private record VisitorResult(List<PagePart> parts, List<String> warnings) {}

	// --- Heading tests ---

	@Test
	public void testHeadingH1() {
		List<PagePart> parts = convert("# Title");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(TitlePart.class);
		TitlePart title = (TitlePart) parts.get(0);
		assertThat(title.getContent()).isEqualTo("Title");
		TitleSettings settings = ContentEditorXStream.fromXml(title.getLayoutOptions(), TitleSettings.class);
		assertThat(settings.getSize()).isEqualTo(1);
	}

	@Test
	public void testHeadingH3WithInline() {
		List<PagePart> parts = convert("### Bold **heading**");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(TitlePart.class);
		TitlePart title = (TitlePart) parts.get(0);
		assertThat(title.getContent()).isEqualTo("Bold heading");
		TitleSettings settings = ContentEditorXStream.fromXml(title.getLayoutOptions(), TitleSettings.class);
		assertThat(settings.getSize()).isEqualTo(3);
	}

	@Test
	public void testHeadingLevels() {
		List<PagePart> parts = convert("# H1\n\n## H2\n\n### H3\n\n#### H4\n\n##### H5\n\n###### H6");

		assertThat(parts).hasSize(6);
		for (int i = 0; i < 6; i++) {
			assertThat(parts.get(i)).isInstanceOf(TitlePart.class);
			TitleSettings settings = ContentEditorXStream.fromXml(parts.get(i).getLayoutOptions(), TitleSettings.class);
			assertThat(settings.getSize()).isEqualTo(i + 1);
		}
	}

	// --- Paragraph tests ---

	@Test
	public void testParagraphWithFormatting() {
		List<PagePart> parts = convert("Hello **world** and *italic*");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<strong>world</strong>");
		assertThat(content).contains("<em>italic</em>");
	}

	@Test
	public void testParagraphWithLink() {
		List<PagePart> parts = convert("[OpenOlat](https://www.openolat.org)");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(0).getContent()).contains("href=\"https://www.openolat.org\"");
		assertThat(parts.get(0).getContent()).contains("OpenOlat</a>");
	}

	@Test
	public void testParagraphWithInlineCode() {
		List<PagePart> parts = convert("Use `System.out.println()` for output");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(0).getContent()).contains("<code>System.out.println()</code>");
	}

	@Test
	public void testConsecutiveParagraphsMerged() {
		String md = "First paragraph.\n\nSecond paragraph.\n\nThird paragraph.";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).contains("First paragraph.");
		assertThat(content).contains("Second paragraph.");
		assertThat(content).contains("Third paragraph.");
	}

	@Test
	public void testParagraphsNotMergedAcrossOtherElements() {
		String md = "Before heading.\n\n# Title\n\nAfter heading.";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(3);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(1)).isInstanceOf(TitlePart.class);
		assertThat(parts.get(2)).isInstanceOf(ParagraphPart.class);
	}

	@Test
	public void testParagraphNotMergedWithBlockquote() {
		// Blockquote paragraph has layoutOptions set (AlertBoxSettings), so next paragraph should NOT merge
		String md = "> A quote\n\nA normal paragraph.";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(2);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class); // blockquote
		assertThat(parts.get(1)).isInstanceOf(ParagraphPart.class); // normal
	}

	// --- Code block tests ---

	@Test
	public void testFencedCodeBlockJava() {
		List<PagePart> parts = convert("```java\npublic class X {}\n```");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(CodePart.class);
		CodePart code = (CodePart) parts.get(0);
		assertThat(code.getContent()).isEqualTo("public class X {}");
		CodeSettings settings = ContentEditorXStream.fromXml(code.getLayoutOptions(), CodeSettings.class);
		assertThat(settings.getCodeLanguage()).isEqualTo(CodeLanguage.java);
	}

	@Test
	public void testFencedCodeBlockUnknownLang() {
		List<PagePart> parts = convert("```foobar\ncode\n```");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(CodePart.class);
		CodeSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), CodeSettings.class);
		assertThat(settings.getCodeLanguage()).isEqualTo(CodeLanguage.auto);
	}

	@Test
	public void testFencedCodeBlockNoLang() {
		List<PagePart> parts = convert("```\nsome code\n```");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(CodePart.class);
		CodeSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), CodeSettings.class);
		assertThat(settings.getCodeLanguage()).isEqualTo(CodeLanguage.auto);
	}

	@Test
	public void testIndentedCodeBlock() {
		List<PagePart> parts = convert("    indented code\n    second line");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(CodePart.class);
		CodePart code = (CodePart) parts.get(0);
		assertThat(code.getContent()).contains("indented code");
		CodeSettings settings = ContentEditorXStream.fromXml(code.getLayoutOptions(), CodeSettings.class);
		assertThat(settings.getCodeLanguage()).isEqualTo(CodeLanguage.plaintext);
	}

	// --- Thematic break ---

	@Test
	public void testThematicBreak() {
		List<PagePart> parts = convert("---");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(SpacerPart.class);
	}

	// --- BlockQuote ---

	@Test
	public void testBlockQuote() {
		List<PagePart> parts = convert("> Important note");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		ParagraphPart para = (ParagraphPart) parts.get(0);
		assertThat(para.getContent()).contains("Important note");

		TextSettings settings = ContentEditorXStream.fromXml(para.getLayoutOptions(), TextSettings.class);
		assertThat(settings).isNotNull();
		AlertBoxSettings alertBox = settings.getAlertBoxSettings();
		assertThat(alertBox).isNotNull();
		assertThat(alertBox.isShowAlertBox()).isTrue();
	}

	@Test
	public void testNestedBlockQuote() {
		List<PagePart> parts = convert(">> nested quote");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings()).isNotNull();
		assertThat(settings.getAlertBoxSettings().isShowAlertBox()).isTrue();
	}

	// --- BlockQuote admonition tests ---

	@Test
	public void testBlockQuoteAdmonitionNote() {
		List<PagePart> parts = convert("> [!NOTE]\n> This is a note");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		AlertBoxSettings alertBox = settings.getAlertBoxSettings();
		assertThat(alertBox.getType()).isEqualTo(AlertBoxType.note);
		assertThat(alertBox.isWithIcon()).isTrue();
		// Marker should not appear in content
		assertThat(parts.get(0).getContent()).doesNotContain("[!NOTE]");
		assertThat(parts.get(0).getContent()).contains("This is a note");
	}

	@Test
	public void testBlockQuoteAdmonitionWarning() {
		List<PagePart> parts = convert("> [!WARNING]\n> Be careful");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.warning);
	}

	@Test
	public void testBlockQuoteAdmonitionTip() {
		List<PagePart> parts = convert("> [!TIP]\n> A helpful tip");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.tip);
	}

	@Test
	public void testBlockQuoteAdmonitionImportant() {
		List<PagePart> parts = convert("> [!IMPORTANT]\n> Read this");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.important);
	}

	@Test
	public void testBlockQuoteAdmonitionCaution() {
		List<PagePart> parts = convert("> [!CAUTION]\n> Danger zone");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.error);
	}

	@Test
	public void testBlockQuoteAdmonitionInfo() {
		List<PagePart> parts = convert("> [!INFO]\n> Information");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.info);
	}

	@Test
	public void testBlockQuoteAdmonitionSuccess() {
		List<PagePart> parts = convert("> [!SUCCESS]\n> It worked");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.success);
	}

	@Test
	public void testBlockQuotePlainFallback() {
		List<PagePart> parts = convert("> Just a plain quote");

		assertThat(parts).hasSize(1);
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.note);
		assertThat(settings.getAlertBoxSettings().getTitle()).isNull();
	}

	@Test
	public void testBlockQuoteAdmonitionMultiline() {
		String md = "> [!WARNING]\n> First line\n> Second line\n> Third line";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("[!WARNING]");
		assertThat(content).contains("First line");
		assertThat(content).contains("Second line");
		assertThat(content).contains("Third line");
		TextSettings settings = ContentEditorXStream.fromXml(parts.get(0).getLayoutOptions(), TextSettings.class);
		assertThat(settings.getAlertBoxSettings().getType()).isEqualTo(AlertBoxType.warning);
	}

	// --- Extension tests ---

	@Test
	public void testStrikethrough() {
		List<PagePart> parts = convert("This is ~~deleted~~ text");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(0).getContent()).contains("<del>deleted</del>");
	}

	@Test
	public void testTaskList() {
		String md = "- [x] Done task\n- [ ] Open task";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).contains("type=\"checkbox\"");
		assertThat(content).contains("checked");
		assertThat(content).contains("Done task");
		assertThat(content).contains("Open task");
	}

	@Test
	public void testAutolink() {
		List<PagePart> parts = convert("Visit https://www.openolat.org for more");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(0).getContent()).contains("href=\"https://www.openolat.org\"");
	}

	@Test
	public void testFootnotes() {
		String md = "Text with a footnote[^1].\n\n[^1]: This is the footnote.";
		List<PagePart> parts = convert(md);

		assertThat(parts).isNotEmpty();
		// Footnote reference should produce sup element or footnote markup
		String allContent = parts.stream()
			.map(PagePart::getContent)
			.reduce("", (a, b) -> a + " " + b);
		assertThat(allContent).contains("footnote");
	}

	// --- Lists ---

	@Test
	public void testBulletList() {
		List<PagePart> parts = convert("- item 1\n- item 2\n- item 3");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<ul>");
		assertThat(content).contains("<li>");
		assertThat(content).contains("item 1");
		assertThat(content).contains("item 2");
	}

	@Test
	public void testOrderedList() {
		List<PagePart> parts = convert("1. first\n2. second\n3. third");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<ol>");
		assertThat(content).contains("<li>");
		assertThat(content).contains("first");
	}

	@Test
	public void testListWithNestedFormatting() {
		List<PagePart> parts = convert("- **bold** item\n- *italic* item");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<strong>bold</strong>");
		assertThat(content).contains("<em>italic</em>");
	}

	// --- HTML block (skipped for security) ---

	@Test
	public void testHtmlBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<div>raw html</div>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	// --- Math blocks ---

	@Test
	public void testMathBlock() {
		Map<String, String> mathBlocks = Map.of("MATHBLOCK_0", "E=mc^2");
		List<PagePart> parts = convert("MATHBLOCK_0", mathBlocks);

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(MathPart.class);
		assertThat(parts.get(0).getContent()).isEqualTo("E=mc^2");
	}

	// --- GFM Table ---

	@Test
	public void testGfmTable() {
		String md = "| Col A | Col B |\n|-------|-------|\n| a1    | b1    |\n| a2    | b2    |";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(TablePart.class);
		TablePart tablePart = (TablePart) parts.get(0);

		TableContent tc = ContentEditorXStream.fromXml(tablePart.getContent(), TableContent.class);
		assertThat(tc).isNotNull();
		assertThat(tc.getNumOfRows()).isEqualTo(3);
		assertThat(tc.getNumOfColumns()).isEqualTo(2);
		assertThat(tc.getContent(0, 0)).isEqualTo("Col A");
		assertThat(tc.getContent(0, 1)).isEqualTo("Col B");
		assertThat(tc.getContent(1, 0)).isEqualTo("a1");
		assertThat(tc.getContent(2, 1)).isEqualTo("b2");
	}

	// --- Multiple blocks ---

	@Test
	public void testMultipleBlocks() {
		String md = "# Title\n\nA paragraph.\n\n```java\ncode\n```\n\n---";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(4);
		assertThat(parts.get(0)).isInstanceOf(TitlePart.class);
		assertThat(parts.get(1)).isInstanceOf(ParagraphPart.class);
		assertThat(parts.get(2)).isInstanceOf(CodePart.class);
		assertThat(parts.get(3)).isInstanceOf(SpacerPart.class);
	}

	// --- Edge cases ---

	@Test
	public void testEmptyInput() {
		List<PagePart> parts = convert("");

		assertThat(parts).isEmpty();
	}

	// --- Standalone image without handler ---

	@Test
	public void testStandaloneImageWithoutHandler() {
		// Without an ImageHandler, standalone images produce warnings
		List<PagePart> parts = convert("![alt text](./image.png)");

		assertThat(parts).hasSize(1);
		// Falls back to paragraph with alt text since no handler and no file
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
	}

	// --- Complex document ---

	@Test
	public void testComplexDocument() {
		String md = """
			# Welcome

			This is a **bold** introduction with a [link](https://example.com).

			## Features

			- Item one
			- Item two
			- Item three

			> Note: This is important

			```python
			def hello():
			    print("Hello")
			```

			---

			| Name | Value |
			|------|-------|
			| A    | 1     |
			| B    | 2     |

			Final paragraph.
			""";

		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(9);
		assertThat(parts.get(0)).isInstanceOf(TitlePart.class);  // # Welcome
		assertThat(parts.get(1)).isInstanceOf(ParagraphPart.class);  // intro paragraph
		assertThat(parts.get(2)).isInstanceOf(TitlePart.class);  // ## Features
		assertThat(parts.get(3)).isInstanceOf(ParagraphPart.class);  // bullet list
		assertThat(parts.get(4)).isInstanceOf(ParagraphPart.class);  // blockquote
		assertThat(parts.get(5)).isInstanceOf(CodePart.class);  // python code
		assertThat(parts.get(6)).isInstanceOf(SpacerPart.class);  // ---
		assertThat(parts.get(7)).isInstanceOf(TablePart.class);  // table
		assertThat(parts.get(8)).isInstanceOf(ParagraphPart.class);  // final paragraph
	}

	// ======================================================================
	// Security tests: XSS and HTML injection prevention
	// ======================================================================

	// --- Script tag injection ---

	@Test
	public void testScriptTagBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<script>alert('xss')</script>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	@Test
	public void testScriptTagInParagraphIsEscaped() {
		List<PagePart> parts = convert("Hello <script>alert('xss')</script> world");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(ParagraphPart.class);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<script>");
		assertThat(content).doesNotContain("</script>");
		assertThat(content).contains("&lt;script&gt;");
	}

	@Test
	public void testScriptTagInHeadingIsStripped() {
		List<PagePart> parts = convert("# Title <script>alert(1)</script>");

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(TitlePart.class);
		String content = parts.get(0).getContent();
		// HtmlInline nodes (<script>, </script>) are skipped in plain text extraction.
		// The text between tags ("alert(1)") is harmless plain text in a TitlePart.
		assertThat(content).doesNotContain("<script>");
		assertThat(content).doesNotContain("</script>");
	}

	// --- Event handler injection ---

	@Test
	public void testOnClickInlineIsEscaped() {
		List<PagePart> parts = convert("Click <img src=x onerror=alert(1)> here");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// Inline HTML is entity-escaped, not rendered as HTML
		assertThat(content).doesNotContain("<img");
		assertThat(content).contains("&lt;img");
	}

	@Test
	public void testOnMouseOverInlineIsEscaped() {
		List<PagePart> parts = convert("Text <div onmouseover=\"alert('xss')\">hover</div> end");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// Inline HTML is entity-escaped, safe even if attribute names appear as text
		assertThat(content).doesNotContain("<div");
		assertThat(content).contains("&lt;div");
	}

	// --- HTML block injection ---

	@Test
	public void testDivBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<div class=\"evil\">content</div>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	@Test
	public void testIframeBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<iframe src=\"https://evil.com\"></iframe>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	@Test
	public void testFormBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<form action=\"https://evil.com\"><input type=\"text\"></form>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	@Test
	public void testObjectEmbedInlineIsEscaped() {
		// <object> is not a CommonMark HTML block tag, so it's parsed as inline HTML and escaped
		List<PagePart> parts = convert("<object data=\"evil.swf\"></object>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<object");
		assertThat(content).contains("&lt;object");
	}

	// --- Inline HTML injection ---

	@Test
	public void testInlineHtmlTagIsEscaped() {
		List<PagePart> parts = convert("Hello <b>bold</b> world");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<b>");
		assertThat(content).contains("&lt;b&gt;");
	}

	@Test
	public void testInlineAnchorWithJavascriptIsEscaped() {
		List<PagePart> parts = convert("Click <a href=\"javascript:alert(1)\">here</a>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// Inline HTML anchor is entity-escaped
		assertThat(content).doesNotContain("<a ");
		assertThat(content).contains("&lt;a");
	}

	@Test
	public void testInlineStyleTagIsEscaped() {
		List<PagePart> parts = convert("Text <style>body{display:none}</style> more");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<style>");
		assertThat(content).contains("&lt;style&gt;");
	}

	// --- SVG/MathML injection ---

	@Test
	public void testSvgInlineIsEscaped() {
		// <svg> is not a CommonMark HTML block tag, so it's parsed as inline HTML and escaped
		List<PagePart> parts = convert("<svg onload=\"alert('xss')\"><circle r=\"50\"/></svg>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<svg");
		assertThat(content).contains("&lt;svg");
	}

	@Test
	public void testSvgOnloadInlineIsEscaped() {
		List<PagePart> parts = convert("Image: <svg/onload=alert(1)>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<svg");
		assertThat(content).contains("&lt;svg");
	}

	// --- Mixed content attacks ---

	@Test
	public void testMarkdownWithInterspersedScriptBlocks() {
		String md = "# Safe title\n\n<script>alert('xss')</script>\n\nSafe paragraph.";
		VisitorResult result = convertWithWarnings(md);

		assertThat(result.parts()).hasSize(2);
		assertThat(result.parts().get(0)).isInstanceOf(TitlePart.class);
		assertThat(result.parts().get(1)).isInstanceOf(ParagraphPart.class);
		assertThat(result.parts().get(1).getContent()).contains("Safe paragraph");
		// Script block was skipped
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
		// No part contains script
		for (PagePart part : result.parts()) {
			assertThat(part.getContent()).doesNotContain("<script>");
		}
	}

	@Test
	public void testMarkdownLinkWithJavascriptProtocol() {
		List<PagePart> parts = convert("[click](javascript:alert(1))");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// sanitizeUrls(true) blocks javascript: protocol in markdown links
		assertThat(content).doesNotContain("href=\"javascript:");
	}

	@Test
	public void testMarkdownImageWithJavascriptSrc() {
		List<PagePart> parts = convert("![xss](javascript:alert(1))");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("src=\"javascript:");
	}

	@Test
	public void testDataProtocolInLink() {
		List<PagePart> parts = convert("[click](data:text/html,<script>alert(1)</script>)");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// sanitizeUrls blocks data: protocol, escapeHtml escapes any HTML in the URL
		assertThat(content).doesNotContain("href=\"data:");
		assertThat(content).doesNotContain("<script>");
	}

	@Test
	public void testVbscriptProtocolInLink() {
		List<PagePart> parts = convert("[click](vbscript:MsgBox(1))");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// sanitizeUrls blocks vbscript: protocol
		assertThat(content).doesNotContain("href=\"vbscript:");
	}

	// --- Encoded/obfuscated attacks ---

	@Test
	public void testHtmlEntityEncodedScript() {
		// HTML entities in raw HTML inline
		List<PagePart> parts = convert("Text <&#115;&#99;&#114;&#105;&#112;&#116;>alert(1)</script> end");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<script>");
	}

	@Test
	public void testMultilineHtmlBlockIsSkipped() {
		String md = "<div>\n  <script>alert('xss')</script>\n  <p>sneaky</p>\n</div>";
		VisitorResult result = convertWithWarnings(md);

		for (PagePart part : result.parts()) {
			assertThat(part.getContent()).doesNotContain("<script>");
			assertThat(part.getContent()).doesNotContain("<div>");
		}
	}

	// --- Style-based attacks ---

	@Test
	public void testStyleBlockIsSkipped() {
		VisitorResult result = convertWithWarnings("<style>body{background:url('javascript:alert(1)')}</style>");

		assertThat(result.parts()).isEmpty();
		assertThat(result.warnings()).anyMatch(w -> w.contains("HTML block skipped"));
	}

	// --- Markdown formatting is preserved ---

	@Test
	public void testBoldAndItalicArePreserved() {
		List<PagePart> parts = convert("This is **bold** and *italic* text.");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<strong>bold</strong>");
		assertThat(content).contains("<em>italic</em>");
	}

	@Test
	public void testMarkdownLinksArePreserved() {
		List<PagePart> parts = convert("[OpenOlat](https://www.openolat.org)");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).contains("href=\"https://www.openolat.org\"");
		assertThat(content).contains("OpenOlat</a>");
	}

	@Test
	public void testMarkdownCodeSpanIsPreserved() {
		List<PagePart> parts = convert("Use `<script>` in code");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// Code span content is escaped by CommonMark
		assertThat(content).contains("<code>");
		assertThat(content).doesNotContain("<script>");
	}

	// --- Extension security tests ---

	@Test
	public void testStrikethroughWithScriptIsEscaped() {
		List<PagePart> parts = convert("~~<script>alert('xss')</script>~~");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).contains("<del>");
		assertThat(content).doesNotContain("<script>");
		assertThat(content).contains("&lt;script&gt;");
	}

	@Test
	public void testTaskListWithScriptIsEscaped() {
		List<PagePart> parts = convert("- [x] <script>alert('xss')</script>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<script>");
		assertThat(content).contains("&lt;script&gt;");
	}

	@Test
	public void testAutolinkDoesNotLinkJavascript() {
		List<PagePart> parts = convert("Visit javascript:alert(1) for fun");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		// javascript: must not become a clickable link
		assertThat(content).doesNotContain("href=\"javascript:");
		assertThat(content).doesNotContain("<a");
	}

	@Test
	public void testBlockQuoteAdmonitionWithScriptIsEscaped() {
		List<PagePart> parts = convert("> [!WARNING]\n> <script>alert('xss')</script>");

		assertThat(parts).hasSize(1);
		String content = parts.get(0).getContent();
		assertThat(content).doesNotContain("<script>");
		assertThat(content).contains("&lt;script&gt;");
	}

	@Test
	public void testFootnoteWithScriptIsEscaped() {
		String md = "Text[^1].\n\n[^1]: <script>alert('xss')</script>";
		List<PagePart> parts = convert(md);

		String allContent = parts.stream()
			.map(PagePart::getContent)
			.reduce("", (a, b) -> a + " " + b);
		assertThat(allContent).doesNotContain("<script>");
	}

	@Test
	public void testFencedCodeBlockPreservesHtmlLiterally() {
		String md = "```html\n<script>alert('xss')</script>\n```";
		List<PagePart> parts = convert(md);

		assertThat(parts).hasSize(1);
		assertThat(parts.get(0)).isInstanceOf(CodePart.class);
		// Code blocks store raw text content, not executed HTML
		assertThat(parts.get(0).getContent()).contains("<script>alert('xss')</script>");
	}
}
