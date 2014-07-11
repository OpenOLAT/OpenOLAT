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
*/
package org.olat.course.nodes.projectbroker;

import java.util.ArrayList;
import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;

/**
 * Description:<br>
 * wrapper for multiple identities in one cell.
 * needed to code the row-id into the cell for later callout-opening.
 * 
 * <P>
 * Initial Date:  01.04.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ProjectManagerColumnRenderer implements CustomCellRenderer {

	public static final String PROJECTMANAGER_COLUMN_ROW_IDENT = "pmrow";
	
	/**
	 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
	 */
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {			
		if (val instanceof ArrayList){
			@SuppressWarnings("unchecked")
			ArrayList<Identity> allIdents = (ArrayList<Identity>) val;
			Integer row = null;
			try {
				row = Integer.parseInt(action);
			} catch (Exception e) {
				// do nothing with that
			} 
			
			StringBuilder buf = new StringBuilder();
			
			if (renderer!=null && row != null) {
				// if no renderer is set, then we assume it's a table export - in which case we don't want the htmls (<b>)
				// no row might occur during table-search
				sb.append("<span class='projmgrrowcontent' id='")
				  .append(PROJECTMANAGER_COLUMN_ROW_IDENT).append(row)
				  .append("'>");
			}
			
			for (Identity identity : allIdents) {
				String last = identity.getUser().getProperty(UserConstants.LASTNAME, locale);
				String first= identity.getUser().getProperty(UserConstants.FIRSTNAME, locale); 
				buf.append(last).append(" ").append(first).append(", ");				
			}
			sb.append(buf.substring(0, buf.length() - 2));
			
			if (renderer!=null && row != null) {
				sb.append("</span>");
			}			
		}
	}
}
