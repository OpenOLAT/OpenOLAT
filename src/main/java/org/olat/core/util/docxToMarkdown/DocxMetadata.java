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

import java.util.List;

/**
 * Document metadata extracted from docProps/core.xml and docProps/app.xml.
 *
 * @author gnaegi, https://www.frentix.com
 */
record DocxMetadata(
	String title,
	String author,
	String subject,
	List<String> keywords,
	String description,
	String created,
	String modified,
	String lastModifiedBy,
	String category,
	String revision
) {

	public boolean hasContent() {
		return isNotEmpty(title) || isNotEmpty(author) || isNotEmpty(subject)
			|| (keywords != null && !keywords.isEmpty())
			|| isNotEmpty(description) || isNotEmpty(created) || isNotEmpty(modified)
			|| isNotEmpty(lastModifiedBy) || isNotEmpty(category);
	}

	public String toYamlFrontMatter() {
		if (!hasContent()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("---\n");
		appendField(sb, "title", title);
		appendField(sb, "author", author);
		appendField(sb, "subject", subject);
		if (keywords != null && !keywords.isEmpty()) {
			sb.append("keywords:\n");
			for (String kw : keywords) {
				if (isNotEmpty(kw)) {
					sb.append("  - ").append(kw.trim()).append('\n');
				}
			}
		}
		appendField(sb, "description", description);
		appendDateField(sb, "date", created);
		appendDateField(sb, "last_modified", modified);
		appendField(sb, "last_modified_by", lastModifiedBy);
		appendField(sb, "category", category);
		sb.append("---\n");
		return sb.toString();
	}

	private void appendField(StringBuilder sb, String key, String value) {
		if (isNotEmpty(value)) {
			sb.append(key).append(": \"").append(escapeYaml(value)).append("\"\n");
		}
	}

	private void appendDateField(StringBuilder sb, String key, String isoDateTime) {
		if (isNotEmpty(isoDateTime)) {
			String dateOnly = isoDateTime.length() >= 10 ? isoDateTime.substring(0, 10) : isoDateTime;
			sb.append(key).append(": \"").append(dateOnly).append("\"\n");
		}
	}

	private static String escapeYaml(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private static boolean isNotEmpty(String s) {
		return s != null && !s.isBlank();
	}
}
