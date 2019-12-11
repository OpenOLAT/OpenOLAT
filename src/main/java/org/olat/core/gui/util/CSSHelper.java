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
package org.olat.core.gui.util;

/**
 * Description:<br>
 * Helper to create css classes
 * 
 * <P>
 * Initial Date: 08.01.2010 <br>
 * 
 * @author gnaegi
 */
public class CSSHelper {
	// Filetype icons
	private static final String CSS_CLASS_FILETYPE_FILE_PREFIX = "o_filetype_";
	public static final String CSS_CLASS_FILETYPE_FILE = CSS_CLASS_FILETYPE_FILE_PREFIX + "file";
	public static final String CSS_CLASS_FILETYPE_FOLDER = CSS_CLASS_FILETYPE_FILE_PREFIX + "folder";
	// Standard icons
	public static final String CSS_CLASS_USER = "o_icon_user";
	public static final String CSS_CLASS_GROUP = "o_icon_group";
	// Message icons
	public static final String CSS_CLASS_ERROR = "o_icon_error";
	public static final String CSS_CLASS_WARN = "o_icon_warn";
	public static final String CSS_CLASS_INFO = "o_icon_info";
	public static final String CSS_CLASS_NEW = "o_icon_new";
	// Various icons
	public static final String CSS_CLASS_CIRCLE_COLOR = "o_icon_circle_color";
	public static final String CSS_CLASS_TRASHED = "o_icon_deleted";
	public static final String CSS_CLASS_VERSION = "o_icon_version";
	public static final String CSS_CLASS_LOCKED = "o_icon_locked";
	public static final String CSS_CLASS_REVISION = "o_icon_version";
	public static final String CSS_CLASS_THUMBNAIL = "o_icon_layout";
	public static final String CSS_CLASS_GLOBE = "o_icon_origin";
	

	public static final String CSS_CLASS_DISABLED = "o_disabled";

	/**
	 * Get the icon css class for a file based on the file ending (e.g. hello.pdf)
	 * 
	 * @param fileName
	 * @return
	 */
	public static String createFiletypeIconCssClassFor(String fileName) {
		// fallback to standard file icon in case the next class does not exist
		StringBuilder cssClass = new StringBuilder(CSS_CLASS_FILETYPE_FILE);
		int typePos = fileName.lastIndexOf('.');
		if (typePos > 0) {
			cssClass.append(' ').append(CSS_CLASS_FILETYPE_FILE_PREFIX).append(fileName.substring(typePos + 1).toLowerCase());
		}
		return cssClass.toString();
	}
	
	public static String getIconCssClassFor(String icon) {
		return "o_icon o_icon-fw " + icon;
	}
	
	public static String getIcon(String icon) {
		return "<i class='" + getIconCssClassFor(icon) + "'></i> ";
	}
}


