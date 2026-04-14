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

import java.io.File;
import java.util.List;

import org.olat.core.gui.translator.Translator;

/**
 * Result of a DOCX-to-Markdown conversion.
 *
 * @param markdown the converted markdown (front matter + body), never null
 * @param basePath temp directory containing the markdown's media files (may be null)
 * @param messages ordered list of conversion messages (warnings/errors)
 * @author gnaegi, https://www.frentix.com
 */
public record DocxToMarkdownResult(
	String markdown,
	File basePath,
	List<DocxConversionMessage> messages
) {

	public boolean hasMessages() {
		return messages != null && !messages.isEmpty();
	}

	/**
	 * Render all messages as a localized HTML unordered list for display in the UI.
	 * Output format: {@code <ul><li>Warning: image skipped</li>...</ul>}
	 * All message text is HTML-escaped to prevent injection.
	 *
	 * @param translator translator for resolving i18n keys
	 * @return HTML string, or empty string if no messages
	 */
	public String renderMessagesAsHtml(Translator translator) {
		if (!hasMessages()) return "";
		StringBuilder sb = new StringBuilder("<ul>");
		for (DocxConversionMessage msg : messages) {
			String text = msg.translate(translator);
			// Escape to prevent HTML injection from message arguments
			text = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
			sb.append("<li>").append(text).append("</li>");
		}
		sb.append("</ul>");
		return sb.toString();
	}

	/**
	 * Render all messages as localized plain-text lines for logging or non-HTML contexts.
	 * Output format: {@code - Warning: image skipped\n- Info: metadata partial}
	 * Each message is prefixed with "- " and separated by newlines.
	 * No HTML escaping is applied — output is plain text.
	 *
	 * @param translator translator for resolving i18n keys
	 * @return plain-text string, or empty string if no messages
	 */
	public String renderMessagesAsText(Translator translator) {
		if (!hasMessages()) return "";
		StringBuilder sb = new StringBuilder();
		for (DocxConversionMessage msg : messages) {
			if (sb.length() > 0) sb.append('\n');
			sb.append("- ").append(msg.translate(translator));
		}
		return sb.toString();
	}
}
