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

import org.olat.core.gui.translator.Translator;

/**
 * A single conversion message with i18n key and substitution arguments.
 * The service never produces user-visible strings — only i18n keys.
 *
 * @param level   severity (INFO, WARNING, ERROR)
 * @param i18nKey the i18n key, e.g. "docx.convert.warn.image.skipped"
 * @param args    substitution arguments for {0}, {1}, ... in the i18n string
 * @author gnaegi, https://www.frentix.com
 */
public record DocxConversionMessage(
	Level level,
	String i18nKey,
	String[] args
) {

	public enum Level { INFO, WARNING, ERROR }

	public DocxConversionMessage(Level level, String i18nKey) {
		this(level, i18nKey, new String[0]);
	}

	public String translate(Translator translator) {
		return translator.translate(i18nKey, args);
	}
}
