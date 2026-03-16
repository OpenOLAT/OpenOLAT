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
 * Unit tests for MarkdownMathPreprocessor.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownMathPreprocessorTest {

	@Test
	public void testSimpleMathBlock() {
		String input = "Some text\n\n$$\nE=mc^2\n$$\n\nMore text";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).hasSize(1);
		assertThat(result.mathBlocks()).containsValue("E=mc^2");
		assertThat(result.text()).contains("MATHBLOCK_0");
		assertThat(result.text()).doesNotContain("$$");
	}

	@Test
	public void testMultipleMathBlocks() {
		String input = "$$\na+b\n$$\n\nText\n\n$$\nx^2\n$$";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).hasSize(2);
		assertThat(result.mathBlocks()).containsEntry("MATHBLOCK_0", "a+b");
		assertThat(result.mathBlocks()).containsEntry("MATHBLOCK_1", "x^2");
	}

	@Test
	public void testNoMathBlocks() {
		String input = "Just regular text\n\nNo math here";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).isEmpty();
		assertThat(result.text()).isEqualTo(input);
	}

	@Test
	public void testMathWithSurroundingText() {
		String input = "# Heading\n\nSome paragraph.\n\n$$\n\\frac{a}{b}\n$$\n\nAnother paragraph.";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).hasSize(1);
		assertThat(result.mathBlocks()).containsValue("\\frac{a}{b}");
		assertThat(result.text()).contains("# Heading");
		assertThat(result.text()).contains("Some paragraph.");
		assertThat(result.text()).contains("Another paragraph.");
	}

	@Test
	public void testNullInput() {
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(null);

		assertThat(result.mathBlocks()).isEmpty();
		assertThat(result.text()).isNull();
	}

	@Test
	public void testNoDollarSigns() {
		String input = "No dollars";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).isEmpty();
		assertThat(result.text()).isEqualTo(input);
	}

	@Test
	public void testMultilineMath() {
		String input = "$$\n\\begin{align}\na &= b \\\\\nc &= d\n\\end{align}\n$$";
		MarkdownMathPreprocessor.PreprocessResult result = MarkdownMathPreprocessor.preprocess(input);

		assertThat(result.mathBlocks()).hasSize(1);
		String latex = result.mathBlocks().values().iterator().next();
		assertThat(latex).contains("\\begin{align}");
		assertThat(latex).contains("\\end{align}");
	}
}
