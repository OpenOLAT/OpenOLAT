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
package org.olat.modules.ceditor.model;

import java.util.Locale;

import org.olat.core.util.Util;
import org.olat.modules.ceditor.ui.CodeEditorController;

/**
 * Initial date: 2023-12-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public enum CodeLanguage {
	auto("Auto"),
	plaintext("Plain text"),
	bash("Bash"),
	c("C"),
	cpp("C++"),
	csharp("C#"),
	css("CSS"),
	go("Go"),
	java("Java"),
	javascript("JavaScript"),
	json("JSON"),
	kotlin("Kotlin"),
	lua("Lua"),
	markdown("Markdown"),
	objectivec("Objective-C"),
	perl("Perl"),
	php("PHP"),
	python("Python"),
	r("R"),
	ruby("Ruby"),
	rust("Rust"),
	scss("SCSS"),
	shell("Shell Session"),
	sql("SQL"),
	swift("Swift"),
	typescript("TypeScript"),
	vbnet("Visual Basic .NET"),
	wasm("WebAssembly"),
	xml("HTML, XML"),
	yaml("YAML");

	private final String displayText;

	CodeLanguage(String displayText) {
		this.displayText = displayText;
	}

	public String getDisplayText(Locale locale) {
		if (this.equals(auto) || this.equals(plaintext)) {
			String key = "code." + this.name();
			return Util.createPackageTranslator(CodeEditorController.class, locale).translate(key);
		}
		return displayText;
	}
}
