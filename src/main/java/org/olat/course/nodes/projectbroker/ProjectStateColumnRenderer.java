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

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Util;
import org.olat.course.nodes.projectbroker.datamodel.Project;

/**
 * This renderer is used by the ProjectListController to render the 'Project-State' column. 
 * The renderer distinguish between render for table content (with HTML) and render for export (no HTML code).
 * 
 * @author Christian Guretzki
 */
public class ProjectStateColumnRenderer implements CustomCellRenderer {

	/**
	 * Renderer for project-broker state-column. Render the following state as bold : STATE_FINAL_ENROLLED,
	 * STATE_PROV_ENROLLED and STATE_ENROLLED e.g. '<b>eingeschrieben</b>'. 
	 * When the renderer is null, no HTML tags will be added. The 'val' will be translated in any case.
	 * 
	 * @param val  must be from type String and one of the state-i18n keys
	 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
	 */
	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		String projectState;
		Translator translator = Util.createPackageTranslator(this.getClass(), locale);
		if (val==null) {
			// don't render nulls
			return;
		}
		if (val instanceof String) {
			projectState = (String)val;
		} else {
			throw new AssertException("ProjectStateColumnRenderer: Wrong object type, could only render String");
		}
		if (renderer==null) {
			// if no renderer is set, then we assume it's a table export - in which case we don't want the htmls (<b>)
			sb.append(translator.translate(projectState));
		} else {
			// add <b> for certain values
			if (   projectState.equals(Project.STATE_FINAL_ENROLLED)
				  || projectState.equals(Project.STATE_PROV_ENROLLED)
				  || projectState.equals(Project.STATE_ENROLLED) ) {
				sb.append("<b>");
				sb.append(translator.translate(projectState));
				sb.append("</b>");
			} else {
				sb.append(translator.translate(projectState));
			}
		}
		
	}

}
