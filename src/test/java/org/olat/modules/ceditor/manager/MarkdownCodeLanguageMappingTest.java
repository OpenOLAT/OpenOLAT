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
import org.olat.modules.ceditor.model.CodeLanguage;

/**
 * Unit tests for MarkdownCodeLanguageMapping.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownCodeLanguageMappingTest {

	@Test
	public void testJava() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("java")).isEqualTo(CodeLanguage.java);
	}

	@Test
	public void testJs() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("js")).isEqualTo(CodeLanguage.javascript);
	}

	@Test
	public void testPython() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("python")).isEqualTo(CodeLanguage.python);
	}

	@Test
	public void testPy() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("py")).isEqualTo(CodeLanguage.python);
	}

	@Test
	public void testCaseInsensitive() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("JAVA")).isEqualTo(CodeLanguage.java);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("Java")).isEqualTo(CodeLanguage.java);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("PYTHON")).isEqualTo(CodeLanguage.python);
	}

	@Test
	public void testUnknown() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("brainfuck")).isEqualTo(CodeLanguage.auto);
	}

	@Test
	public void testNull() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage(null)).isEqualTo(CodeLanguage.auto);
	}

	@Test
	public void testEmpty() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("")).isEqualTo(CodeLanguage.auto);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("   ")).isEqualTo(CodeLanguage.auto);
	}

	@Test
	public void testInfoStringWithMetadata() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("js title")).isEqualTo(CodeLanguage.javascript);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("python file=test.py")).isEqualTo(CodeLanguage.python);
	}

	@Test
	public void testAllAliases() {
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("sh")).isEqualTo(CodeLanguage.bash);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("zsh")).isEqualTo(CodeLanguage.bash);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("shell")).isEqualTo(CodeLanguage.shell);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("c++")).isEqualTo(CodeLanguage.cpp);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("cs")).isEqualTo(CodeLanguage.csharp);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("golang")).isEqualTo(CodeLanguage.go);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("kt")).isEqualTo(CodeLanguage.kotlin);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("rb")).isEqualTo(CodeLanguage.ruby);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("rs")).isEqualTo(CodeLanguage.rust);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("ts")).isEqualTo(CodeLanguage.typescript);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("yml")).isEqualTo(CodeLanguage.yaml);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("html")).isEqualTo(CodeLanguage.xml);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("md")).isEqualTo(CodeLanguage.markdown);
		assertThat(MarkdownCodeLanguageMapping.mapToCodeLanguage("plaintext")).isEqualTo(CodeLanguage.plaintext);
	}
}
