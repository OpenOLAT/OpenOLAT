/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.taxonomy.manager.importexport;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * Constants and helpers of the taxonomy import/export file format (format version 2):
 * a ZIP with the workbook <code>taxonomy.xlsx</code> (sheets levels, level
 * types, information), the media files and a readme. The workbook alone or a
 * CSV with the columns of the levels sheet can be imported as well.
 *
 * Initial date: 30 Sept 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 */
public class TaxonomyFileFormat {

	public static final int FORMAT_VERSION = 2;

	public static final String WORKBOOK_NAME = "taxonomy.xlsx";
	public static final String README_NAME = "readme.md";
	public static final String MEDIA_FOLDER = "media";
	public static final String TEASER = "teaser";
	public static final String BACKGROUND = "background";

	public static final String ON = "ON";
	public static final String OFF = "OFF";

	public static final int SHEET_LEVELS = 0;
	public static final int SHEET_TYPES = 1;
	public static final int SHEET_INFORMATION = 2;

	// Levels sheet
	public static final int LEVEL_PATH = 0;
	public static final int LEVEL_IDENTIFIER = 1;
	public static final int LEVEL_EXTERNAL_ID = 2;
	public static final int LEVEL_TYPE = 3;
	public static final int LEVEL_SORT_ORDER = 4;
	public static final int LEVEL_MANAGED_FLAGS = 5;
	public static final int LEVEL_TEASER = 6;
	public static final int LEVEL_BACKGROUND = 7;
	public static final int LEVEL_FIRST_LANGUAGE = 8;
	public static final int LEVEL_LANGUAGE_GROUP_SIZE = 3;

	/** First language column of the format version 1 (export and template before 21.2) */
	public static final int LEGACY_LEVEL_FIRST_LANGUAGE = 4;

	// Level types sheet
	public static final int TYPE_IDENTIFIER = 0;
	public static final int TYPE_DISPLAY_NAME = 1;
	public static final int TYPE_DESCRIPTION = 2;
	public static final int TYPE_EXTERNAL_ID = 3;
	public static final int TYPE_CSS_CLASS = 4;
	public static final int TYPE_VISIBLE = 5;
	public static final int TYPE_ALLOWED_AS_SUBJECT = 6;
	public static final int TYPE_ALLOWED_AS_COMPETENCE = 7;
	public static final int TYPE_SUB_TYPES = 8;
	public static final int TYPE_MANAGED_FLAGS = 9;
	public static final int TYPE_LIBRARY_ENABLED = 10;
	public static final int TYPE_LIBRARY_MANAGE = 11;
	public static final int TYPE_LIBRARY_TEACH_READ = 12;
	public static final int TYPE_LIBRARY_TEACH_READ_PARENT_LEVELS = 13;
	public static final int TYPE_LIBRARY_TEACH_WRITE = 14;
	public static final int TYPE_LIBRARY_HAVE_READ = 15;
	public static final int TYPE_LIBRARY_TARGET_READ = 16;

	// Information sheet: key, label, value
	public static final int INFO_KEY = 0;
	public static final int INFO_LABEL = 1;
	public static final int INFO_VALUE = 2;

	public static final String INFO_EXPORT_URL = "export.url";
	public static final String INFO_EXPORT_VERSION = "export.version";
	public static final String INFO_EXPORT_LANGUAGE = "export.language";
	public static final String INFO_EXPORT_DATE = "export.date";
	public static final String INFO_EXPORT_BY = "export.by";
	public static final String INFO_FORMAT = "export.format";
	public static final String INFO_TAXONOMY_USAGE = "taxonomy.usage";
	public static final String INFO_TAXONOMY_IDENTIFIER = "taxonomy.identifier";
	public static final String INFO_TAXONOMY_DISPLAY_NAME = "taxonomy.displayname";
	public static final String INFO_TAXONOMY_DESCRIPTION = "taxonomy.description";
	public static final String INFO_TAXONOMY_EXTERNAL_ID = "taxonomy.externalid";
	public static final String INFO_TAXONOMY_MANAGED_FLAGS = "taxonomy.managedflags";

	/** Separator of multiple values in one cell, e.g. the allowed sub-types */
	public static final String MULTI_VALUE_SEPARATOR = "\n";

	private TaxonomyFileFormat() {
		//
	}

	public static String toOnOff(boolean value) {
		return value ? ON : OFF;
	}

	/**
	 * @param value The value of the cell
	 * @return true for ON, false for OFF, null if the value is not valid
	 */
	public static Boolean parseOnOff(String value) {
		if(value == null) return null;
		String val = value.trim();
		if(ON.equalsIgnoreCase(val) || "true".equalsIgnoreCase(val) || "1".equals(val)) {
			return Boolean.TRUE;
		}
		if(OFF.equalsIgnoreCase(val) || "false".equalsIgnoreCase(val) || "0".equals(val)) {
			return Boolean.FALSE;
		}
		return null;
	}

	/**
	 * Build the path of a level from the identifiers of its parent line. A slash
	 * or a backslash in an identifier is escaped with a backslash.
	 *
	 * @param identifiers The identifiers from the root to the level
	 * @return The path, e.g. <code>/MATH/GEO/</code>
	 */
	public static String toPath(List<String> identifiers) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("/");
		for(String identifier:identifiers) {
			sb.append(escapeSegment(identifier)).append("/");
		}
		return sb.toString();
	}

	public static String escapeSegment(String identifier) {
		if(identifier == null) return "";
		return identifier.replace("\\", "\\\\").replace("/", "\\/");
	}

	/**
	 * Split a path in its identifiers. Leading and trailing slashes are optional.
	 *
	 * @param path The path, e.g. <code>/MATH/GEO/</code>
	 * @return The unescaped identifiers from the root to the level
	 */
	public static List<String> splitPath(String path) {
		List<String> segments = new ArrayList<>();
		if(!StringHelper.containsNonWhitespace(path)) {
			return segments;
		}

		StringBuilder current = new StringBuilder();
		boolean escape = false;
		for(int i=0; i<path.length(); i++) {
			char ch = path.charAt(i);
			if(escape) {
				current.append(ch);
				escape = false;
			} else if(ch == '\\') {
				escape = true;
			} else if(ch == '/') {
				addSegment(segments, current);
			} else {
				current.append(ch);
			}
		}
		if(escape) {
			current.append('\\');
		}
		addSegment(segments, current);
		return segments;
	}

	/**
	 * Add the segment trimmed, like the identifiers of the levels.
	 */
	private static void addSegment(List<String> segments, StringBuilder current) {
		String segment = current.toString().trim();
		if(!segment.isEmpty()) {
			segments.add(segment);
		}
		current.setLength(0);
	}

	public static String joinMultiValues(List<String> values) {
		if(values == null || values.isEmpty()) return null;
		return String.join(MULTI_VALUE_SEPARATOR, values);
	}

	public static List<String> splitMultiValues(String value) {
		List<String> values = new ArrayList<>();
		if(!StringHelper.containsNonWhitespace(value)) return values;
		for(String val:value.split("\r?\n")) {
			if(StringHelper.containsNonWhitespace(val)) {
				values.add(val.trim());
			}
		}
		return values;
	}

	public static boolean hasControlCharacters(String value) {
		if(value == null) return false;
		for(int i=0; i<value.length(); i++) {
			if(Character.isISOControl(value.charAt(i))) {
				return true;
			}
		}
		return false;
	}
}
