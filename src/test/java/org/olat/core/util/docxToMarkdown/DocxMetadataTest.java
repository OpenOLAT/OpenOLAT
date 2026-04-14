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
package org.olat.core.util.docxToMarkdown;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for the {@link DocxMetadata} record.
 * <p>
 * Covers {@code hasContent()} and {@code toYamlFrontMatter()} for a range of
 * field combinations: empty, full, partial, and edge-case inputs.
 *
 * @author frentix GmbH
 */
public class DocxMetadataTest {

	// -----------------------------------------------------------------------
	// hasContent()
	// -----------------------------------------------------------------------

	@Test
	public void emptyMetadataHasNoContent() {
		DocxMetadata meta = new DocxMetadata(
			null, null, null, null, null, null, null, null, null, null);

		assertFalse("all-null metadata must report hasContent() == false", meta.hasContent());
		assertEquals("all-null metadata must produce an empty YAML string",
				"", meta.toYamlFrontMatter());
	}

	@Test
	public void hasContentWithOnlyTitle() {
		DocxMetadata meta = new DocxMetadata(
			"My Title", null, null, null, null, null, null, null, null, null);

		assertTrue("metadata with only title must report hasContent() == true", meta.hasContent());
	}

	// -----------------------------------------------------------------------
	// toYamlFrontMatter() — full document
	// -----------------------------------------------------------------------

	@Test
	public void fullMetadataYaml() {
		List<String> keywords = Arrays.asList("learning", "openolat", "lms");
		DocxMetadata meta = new DocxMetadata(
			"Full Title",
			"Jane Author",
			"The Subject",
			keywords,
			"A description",
			"2026-03-15T10:30:00Z",
			"2026-03-20T08:00:00Z",
			"John Editor",
			"Education",
			"3");

		String yaml = meta.toYamlFrontMatter();

		assertTrue("YAML must open with ---", yaml.startsWith("---\n"));
		assertTrue("YAML must close with ---", yaml.endsWith("---\n"));
		assertTrue("YAML must contain title field", yaml.contains("title:"));
		assertTrue("YAML must contain the title value", yaml.contains("Full Title"));
		assertTrue("YAML must contain author field", yaml.contains("author:"));
		assertTrue("YAML must contain the author value", yaml.contains("Jane Author"));
		assertTrue("YAML must contain subject field", yaml.contains("subject:"));
		assertTrue("YAML must contain keywords block", yaml.contains("keywords:"));
		assertTrue("YAML must contain date field", yaml.contains("date:"));
		assertTrue("YAML must contain last_modified field", yaml.contains("last_modified:"));
		assertTrue("YAML must contain last_modified_by field", yaml.contains("last_modified_by:"));
		assertTrue("YAML must contain category field", yaml.contains("category:"));
	}

	// -----------------------------------------------------------------------
	// toYamlFrontMatter() — partial document
	// -----------------------------------------------------------------------

	@Test
	public void partialMetadataYaml() {
		DocxMetadata meta = new DocxMetadata(
			"Only Title", null, null, null, null, null, null, null, null, null);

		String yaml = meta.toYamlFrontMatter();

		assertTrue("YAML must contain title", yaml.contains("title:"));
		assertTrue("YAML must contain the title value", yaml.contains("Only Title"));
		assertFalse("YAML must not contain author line when author is null",
				yaml.contains("author:"));
		assertFalse("YAML must not contain subject line when subject is null",
				yaml.contains("subject:"));
		assertFalse("YAML must not contain keywords block when keywords are null",
				yaml.contains("keywords:"));
	}

	// -----------------------------------------------------------------------
	// toYamlFrontMatter() — keywords as YAML list
	// -----------------------------------------------------------------------

	@Test
	public void keywordsAsList() {
		List<String> keywords = Arrays.asList("a", "b", "c");
		DocxMetadata meta = new DocxMetadata(
			null, null, null, keywords, null, null, null, null, null, null);

		String yaml = meta.toYamlFrontMatter();

		assertTrue("YAML must contain keywords block header",
				yaml.contains("keywords:\n"));
		assertTrue("YAML must list keyword 'a'", yaml.contains("  - a\n"));
		assertTrue("YAML must list keyword 'b'", yaml.contains("  - b\n"));
		assertTrue("YAML must list keyword 'c'", yaml.contains("  - c\n"));
	}

	@Test
	public void emptyKeywordsListProducesNoKeywordsBlock() {
		DocxMetadata meta = new DocxMetadata(
			"Title Only", null, null, Collections.emptyList(),
			null, null, null, null, null, null);

		String yaml = meta.toYamlFrontMatter();

		assertFalse("an empty keywords list must not produce a keywords block",
				yaml.contains("keywords:"));
	}

	// -----------------------------------------------------------------------
	// toYamlFrontMatter() — date extraction
	// -----------------------------------------------------------------------

	@Test
	public void dateFieldExtractsDateOnly() {
		DocxMetadata meta = new DocxMetadata(
			null, null, null, null, null,
			"2026-03-15T10:30:00Z",   // created
			null, null, null, null);

		String yaml = meta.toYamlFrontMatter();

		assertTrue("YAML must contain date field", yaml.contains("date:"));
		assertTrue("date value must be date-only (no time component)",
				yaml.contains("\"2026-03-15\""));
		assertFalse("date value must not contain the time component",
				yaml.contains("T10:30"));
	}

	// -----------------------------------------------------------------------
	// toYamlFrontMatter() — YAML escaping
	// -----------------------------------------------------------------------

	@Test
	public void yamlEscapesQuotes() {
		DocxMetadata meta = new DocxMetadata(
			"Title with \"quotes\" inside", null, null, null,
			null, null, null, null, null, null);

		String yaml = meta.toYamlFrontMatter();

		assertTrue("YAML must contain the title field", yaml.contains("title:"));
		// The double quotes in the value must be escaped as \"
		assertTrue("double quotes inside the title must be escaped",
				yaml.contains("\\\"quotes\\\""));
		// Verify the line looks like: title: "Title with \"quotes\" inside"
		// The escaped form \" must be present, meaning backslash-quote pairs exist
		String titleLine = yaml.lines()
			.filter(l -> l.startsWith("title:"))
			.findFirst()
			.orElse("");
		assertTrue("title line must contain backslash-escaped quotes",
				titleLine.contains("\\\""));
	}
}
