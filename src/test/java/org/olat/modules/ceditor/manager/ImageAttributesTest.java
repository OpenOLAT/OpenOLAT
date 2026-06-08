/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.ceditor.manager;

import static org.junit.Assert.*;

import org.commonmark.ext.image.attributes.ImageAttributes;
import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.junit.Test;

public class ImageAttributesTest {

	@Test
	public void imageAttributesParsedAsLastChild() {
		Parser parser = Parser.builder()
			.extensions(MarkdownImportService.markdownExtensions())
			.build();

		Node doc = parser.parse("![](media/shape.svg){width=88 height=43}");
		Paragraph para = (Paragraph) doc.getFirstChild();
		Image image = (Image) para.getFirstChild();

		// ImageAttributes must be last child of Image
		Node lastChild = image.getLastChild();
		assertNotNull("Image must have children", lastChild);
		assertTrue("Last child must be ImageAttributes, was: " + lastChild.getClass().getSimpleName(),
			lastChild instanceof ImageAttributes);

		ImageAttributes attrs = (ImageAttributes) lastChild;
		assertEquals("88", attrs.getAttributes().get("width"));
		assertEquals("43", attrs.getAttributes().get("height"));
	}

	@Test
	public void imageAttributesSurviveRenderChildrenToPlainText() {
		Parser parser = Parser.builder()
			.extensions(MarkdownImportService.markdownExtensions())
			.build();

		Node doc = parser.parse("![alt text](media/img.jpg){width=280 height=186}");
		Paragraph para = (Paragraph) doc.getFirstChild();
		Image image = (Image) para.getFirstChild();

		// Simulate what handleStandaloneImage does: renderChildrenToPlainText first
		String altText = renderPlainText(image);
		assertEquals("alt text", altText);

		// ImageAttributes must still be accessible after plaintext rendering
		Node lastChild = image.getLastChild();
		assertTrue("ImageAttributes must survive plaintext rendering",
			lastChild instanceof ImageAttributes);
		assertEquals("280", ((ImageAttributes) lastChild).getAttributes().get("width"));
	}

	@Test
	public void imageFromActualDocxOutput() {
		// Simulate the exact flow: markdown from DOCX converter → preprocessor → parser
		String markdown = "![](media/image1.jpg){width=280 height=186}\n\n"
			+ "Some text after\n\n"
			+ "![](media/shape.svg){width=88 height=43}\n\n";

		// Run through the preprocessor like the real import does
		MarkdownMathPreprocessor.PreprocessResult preprocessed =
			MarkdownMathPreprocessor.preprocess(markdown);

		Parser parser = Parser.builder()
			.extensions(MarkdownImportService.markdownExtensions())
			.build();
		Node doc = parser.parse(preprocessed.text());

		// Walk paragraphs and check images
		Node child = doc.getFirstChild();
		int imageCount = 0;
		while (child != null) {
			if (child instanceof Paragraph para) {
				Node paraChild = para.getFirstChild();
				if (paraChild instanceof Image image) {
					imageCount++;
					Node lastChild = image.getLastChild();
					System.out.println("Image " + imageCount + ": dest=" + image.getDestination()
						+ " lastChild=" + (lastChild != null ? lastChild.getClass().getSimpleName() : "null"));
					if (lastChild instanceof ImageAttributes ia) {
						System.out.println("  attrs=" + ia.getAttributes());
					}
				}
			}
			child = child.getNext();
		}
		assertEquals("Should find 2 images", 2, imageCount);
	}

	@Test
	public void imageWithoutAttributes() {
		Parser parser = Parser.builder()
			.extensions(MarkdownImportService.markdownExtensions())
			.build();

		Node doc = parser.parse("![alt](media/img.jpg)");
		Paragraph para = (Paragraph) doc.getFirstChild();
		Image image = (Image) para.getFirstChild();

		Node lastChild = image.getLastChild();
		// Without {}, last child is the Text node (alt text), not ImageAttributes
		assertFalse("Without {} syntax, no ImageAttributes expected",
			lastChild instanceof ImageAttributes);
	}

	private String renderPlainText(Node node) {
		StringBuilder sb = new StringBuilder();
		Node child = node.getFirstChild();
		while (child != null) {
			if (child instanceof org.commonmark.node.Text text) {
				sb.append(text.getLiteral());
			} else {
				sb.append(renderPlainText(child));
			}
			child = child.getNext();
		}
		return sb.toString();
	}
}
