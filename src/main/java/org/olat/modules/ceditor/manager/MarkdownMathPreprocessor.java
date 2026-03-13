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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pre-processes LaTeX math blocks ($$...$$) before CommonMark parsing.
 * Replaces display math blocks with unique placeholders so the CommonMark
 * parser doesn't mangle the LaTeX content.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownMathPreprocessor {

	static final String PLACEHOLDER_PREFIX = "MATHBLOCK_";

	// Match $$ on its own line, content, $$ on its own line
	private static final Pattern DISPLAY_MATH_PATTERN =
		Pattern.compile("^\\$\\$\\s*$\\n(.*?)^\\$\\$\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

	public record PreprocessResult(String text, Map<String, String> mathBlocks) {}

	/**
	 * Replace $$...$$ blocks with placeholder paragraphs and return
	 * the modified text along with a map of placeholder ID -> LaTeX content.
	 */
	public static PreprocessResult preprocess(String markdown) {
		if (markdown == null || !markdown.contains("$$")) {
			return new PreprocessResult(markdown, Map.of());
		}

		Map<String, String> mathBlocks = new HashMap<>();
		int counter = 0;

		Matcher matcher = DISPLAY_MATH_PATTERN.matcher(markdown);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			String latex = matcher.group(1).strip();
			String id = PLACEHOLDER_PREFIX + counter++;
			mathBlocks.put(id, latex);
			matcher.appendReplacement(sb, "\n" + id + "\n");
		}
		matcher.appendTail(sb);

		return new PreprocessResult(sb.toString(), mathBlocks);
	}

	private MarkdownMathPreprocessor() {
		// utility class
	}
}
