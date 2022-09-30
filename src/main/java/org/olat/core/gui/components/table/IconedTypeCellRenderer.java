/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.core.gui.components.table;

import java.util.Locale;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public abstract class IconedTypeCellRenderer implements CustomCellRenderer {
	
	private static final String DOUBLE_QUOTES = "\"";
	
	@Override
	public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
		if (renderer == null) {
			// render icon path for export, ignore alt text
			String iconPath = getIconPath(val);
			sb.append(iconPath);
		} else {
			String iconPath = getIconPath(val);
			String altText = getAltText(val);
			if (iconPath != null) { // if null, no icon will be displayed
				sb.append("<img border=\"0\" src=\"");
				Renderer.renderStaticURI(sb, iconPath);
				sb.append(DOUBLE_QUOTES);
				sb.append(" alt=\"");
				if (altText != null) {
					sb.appendHtmlEscaped(altText);
				}
				// else: accessibility best practice: empty alt text when no alternative text available
				sb.append(DOUBLE_QUOTES);
			}
			sb.append(" />");			
		}
	}

	/**
	 * The hover text of the icon
	 * @param val
	 * @return
	 */
	protected abstract String getAltText(Object val);


	/**
	 * The relative path to the image, relativ to webapp/static/ 
	 * e.g. 'images/myimage.jpg'
	 * 
	 * @param val
	 * @return
	 */
	protected abstract String getIconPath(Object val);

}