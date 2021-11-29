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

package org.olat.course.assessment;

import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Description:<BR/>
 * Renders a node in a table using the node icon and indent.
 * 
 * <P/>
 * Initial Date:  Nov 23, 2004
 *
 * @author gnaegi 
 */
public class IndentedNodeRenderer implements CustomCellRenderer, FlexiCellRenderer {

	private static final String INDENT = "&nbsp;&nbsp;";
	private boolean indentationEnabled = true;
	
	public boolean isIndentationEnabled() {
		return indentationEnabled;
	}
	
	public void setIndentationEnabled(boolean indentationEnabled) {
		this.indentationEnabled = indentationEnabled;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof IndentedCourseNode) {
			render(target, (IndentedCourseNode)cellValue);
		}
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof IndentedCourseNode) {
			render(sb, (IndentedCourseNode)val);
		}
	}
	
	private void render(StringOutput sb, IndentedCourseNode row) {
		String type = row.getType();
		String title = row.getShortTitle();
		String altText = row.getLongTitle();
		String cssClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type).getIconCSSClass();
		if(isIndentationEnabled()) {
			appendIndent(sb, row.getRecursionLevel());
		}
		
		sb.append("<i class=\"o_icon o_icon_fw ").append(cssClass).append("\"> </i> <span");
		if (altText != null) {
			sb.append(" title= \"").append(StringHelper.escapeHtml(altText)).append("\"");
		}
		sb.append(">").append(StringHelper.escapeHtml(title)).append("</span>");
	}
	
	private void appendIndent(StringOutput sb, int indent) {
		for (int i = 0; i < indent; i++) {
			sb.append(INDENT);
		}
	}
	
	public static interface IndentedCourseNode {

		String getType();

		String getShortTitle();

		String getLongTitle();

		int getRecursionLevel();
	}
	
	public static class IndentedCourseNodeData implements IndentedCourseNode {
		
		private final String type;
		private final String shortTitle;
		private final String longTitle;
		private final int recursionLevel;
		
		private IndentedCourseNodeData(String type, String shortTitle, String longTitle, int recursionLevel) {
			this.type = type;
			this.shortTitle = shortTitle;
			this.longTitle = longTitle;
			this.recursionLevel = recursionLevel;
		}

		@Override
		public String getType() {
			return type;
		}

		@Override
		public String getShortTitle() {
			return shortTitle;
		}

		@Override
		public String getLongTitle() {
			return longTitle;
		}

		@Override
		public int getRecursionLevel() {
			return recursionLevel;
		}
		
	}
}