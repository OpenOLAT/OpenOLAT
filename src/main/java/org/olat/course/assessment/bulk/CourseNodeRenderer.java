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
package org.olat.course.assessment.bulk;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeRenderer implements FlexiCellRenderer {

	private static final String INDENT = "&nbsp;&nbsp;";
	private final boolean indentationEnabled = true;
	
	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue,
			int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		int indent = 0;
		CourseNode courseNode = null;
		if(cellValue instanceof SelectCourseNodeStepForm.Node) {
			SelectCourseNodeStepForm.Node node = (SelectCourseNodeStepForm.Node)cellValue;
			indent = node.getIndent();
			courseNode = node.getNode();
		} else if(cellValue instanceof CourseNode) {
			courseNode = (CourseNode)cellValue;
		}
			
		if(courseNode != null) {	
			String type = courseNode.getType();
			String cssClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type).getIconCSSClass();
			String title = courseNode.getShortTitle();
			String altText = courseNode.getLongTitle();
			
			if(indentationEnabled) {
				appendIndent(sb, indent);
			}
			
			sb.append("<i class='o_icon ").append(cssClass).append("'> </i> ").append("<span");
			if (altText != null) {
				sb.append(" title= \"").append(StringHelper.escapeHtml(altText)).append("\"");
			}
			sb.append(">").append(StringHelper.escapeHtml(title)).append("</span>");
		}
	}

  private void appendIndent(StringOutput sb, int indent) {
  	for (int i=indent; i-->0; ) {
			sb.append(INDENT);
		}
  }
}
