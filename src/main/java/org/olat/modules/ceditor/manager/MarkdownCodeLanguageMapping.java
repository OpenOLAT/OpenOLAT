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

import org.olat.modules.ceditor.model.CodeLanguage;

/**
 * Maps CommonMark fenced code block info strings to the CodeLanguage enum.
 *
 * Initial date: 2026-03-11<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class MarkdownCodeLanguageMapping {

	private static final Map<String, CodeLanguage> MAPPING = new HashMap<>();

	static {
		MAPPING.put("bash",        CodeLanguage.bash);
		MAPPING.put("sh",          CodeLanguage.bash);
		MAPPING.put("shell",       CodeLanguage.shell);
		MAPPING.put("zsh",         CodeLanguage.bash);
		MAPPING.put("c",           CodeLanguage.c);
		MAPPING.put("cpp",         CodeLanguage.cpp);
		MAPPING.put("c++",         CodeLanguage.cpp);
		MAPPING.put("csharp",      CodeLanguage.csharp);
		MAPPING.put("cs",          CodeLanguage.csharp);
		MAPPING.put("c#",          CodeLanguage.csharp);
		MAPPING.put("css",         CodeLanguage.css);
		MAPPING.put("go",          CodeLanguage.go);
		MAPPING.put("golang",      CodeLanguage.go);
		MAPPING.put("java",        CodeLanguage.java);
		MAPPING.put("javascript",  CodeLanguage.javascript);
		MAPPING.put("js",          CodeLanguage.javascript);
		MAPPING.put("json",        CodeLanguage.json);
		MAPPING.put("kotlin",      CodeLanguage.kotlin);
		MAPPING.put("kt",          CodeLanguage.kotlin);
		MAPPING.put("lua",         CodeLanguage.lua);
		MAPPING.put("markdown",    CodeLanguage.markdown);
		MAPPING.put("md",          CodeLanguage.markdown);
		MAPPING.put("objectivec",  CodeLanguage.objectivec);
		MAPPING.put("objc",        CodeLanguage.objectivec);
		MAPPING.put("objective-c", CodeLanguage.objectivec);
		MAPPING.put("perl",        CodeLanguage.perl);
		MAPPING.put("pl",          CodeLanguage.perl);
		MAPPING.put("php",         CodeLanguage.php);
		MAPPING.put("python",      CodeLanguage.python);
		MAPPING.put("py",          CodeLanguage.python);
		MAPPING.put("r",           CodeLanguage.r);
		MAPPING.put("ruby",        CodeLanguage.ruby);
		MAPPING.put("rb",          CodeLanguage.ruby);
		MAPPING.put("rust",        CodeLanguage.rust);
		MAPPING.put("rs",          CodeLanguage.rust);
		MAPPING.put("scss",        CodeLanguage.scss);
		MAPPING.put("sass",        CodeLanguage.scss);
		MAPPING.put("sql",         CodeLanguage.sql);
		MAPPING.put("swift",       CodeLanguage.swift);
		MAPPING.put("typescript",  CodeLanguage.typescript);
		MAPPING.put("ts",          CodeLanguage.typescript);
		MAPPING.put("vbnet",       CodeLanguage.vbnet);
		MAPPING.put("vb",          CodeLanguage.vbnet);
		MAPPING.put("wasm",        CodeLanguage.wasm);
		MAPPING.put("xml",         CodeLanguage.xml);
		MAPPING.put("html",        CodeLanguage.xml);
		MAPPING.put("svg",         CodeLanguage.xml);
		MAPPING.put("yaml",        CodeLanguage.yaml);
		MAPPING.put("yml",         CodeLanguage.yaml);
		MAPPING.put("text",        CodeLanguage.plaintext);
		MAPPING.put("plain",       CodeLanguage.plaintext);
		MAPPING.put("plaintext",   CodeLanguage.plaintext);
	}

	/**
	 * Map a fenced code block info string to a CodeLanguage.
	 * The info string is lowercased and trimmed. If no match is found,
	 * returns CodeLanguage.auto which enables highlight.js auto-detection.
	 *
	 * @param infoString The info string from the fenced code block (may be null or empty)
	 * @return The matching CodeLanguage, or CodeLanguage.auto if unknown
	 */
	public static CodeLanguage mapToCodeLanguage(String infoString) {
		if (infoString == null || infoString.isBlank()) {
			return CodeLanguage.auto;
		}
		// CommonMark spec: info string may contain spaces; only first word is the language
		String lang = infoString.strip().split("\\s+")[0].toLowerCase();
		return MAPPING.getOrDefault(lang, CodeLanguage.auto);
	}

	private MarkdownCodeLanguageMapping() {
		// utility class
	}
}
