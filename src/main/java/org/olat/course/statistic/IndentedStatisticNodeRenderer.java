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

package org.olat.course.statistic;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.CourseNodeFactory;

/**
 * Copy/Pasted class from assessment.IndentedNodeRenderer.
 * <p>
 * The idea was not to disturb anything in the assessment area - but it is 
 * probably worth some refactoring and generalization - taking the IndendedNodeRenderer
 * outside of assessment.
 * <p>
 * Another aspect of this class is that it differentiates between rendering
 * on screen and rendering for the export. It does not render any html when
 * used for the export (which currently can only be distinguished from 
 * the other rendering by checking renderer==null - also an area of 
 * possible improvement.. but that would be affecting quite some parts of Olat)
 * <P>
 * Initial Date:  16.02.2010 <br>
 * @author Stefan
 */
public class IndentedStatisticNodeRenderer implements CustomCellRenderer {

	private static final String INDENT = "&nbsp;&nbsp;";

	/** when this flag is true, do a simple rendering in render() when renderer is null (interpreted as export) **/
	private boolean simpleRenderingOnExport_ = false;

	private final Translator translator_;

	/**
	 * 
	 */
	public IndentedStatisticNodeRenderer(Translator translator) {
		if (translator==null) {
			throw new IllegalArgumentException("translator must not be null");
		}
		translator_ = translator;
	}
	
	public void setSimpleRenderingOnExport(boolean simpleRenderingOnExport) {
		simpleRenderingOnExport_ = simpleRenderingOnExport;
	}

	/** 
	 * @see org.olat.core.gui.components.table.CustomCellRenderer#render(org.olat.core.gui.render.StringOutput, org.olat.core.gui.render.Renderer, java.lang.Object, java.util.Locale, int, java.lang.String)
	 */
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		final boolean renderForExport = simpleRenderingOnExport_ && renderer==null;

		if (val==StatisticResult.TOTAL_ROW_TITLE_CELL) {
			String totalTitle = translator_.translate("stat.table.header.total");
			if (renderForExport) {
				sb.append(totalTitle);
			} else {
				TotalRendererHelper.renderTotalTitle(sb, totalTitle);
			}
			return;
		}
		Map nodeData = (Map) val;
		String title = (String)  nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
		String altText = (String)  nodeData.get(AssessmentHelper.KEY_TITLE_LONG);

		if (renderForExport) {
			sb.append(title);
			return;
		}

		Integer indent = (Integer) nodeData.get(AssessmentHelper.KEY_INDENT);
		String type = (String)  nodeData.get(AssessmentHelper.KEY_TYPE);
		if (type==null) {
			sb.appendHtmlEscaped(title);
			return;
		}
		appendIndent(sb,indent);
		
		String cssClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(type).getIconCSSClass();
		sb.append("<i class='o_icon ").append(cssClass).append("'> </i> <span ");
		if (altText != null) {
			sb.append("title= \"").appendHtmlEscaped(altText);
		}
		sb.append("\">").appendHtmlEscaped(title).append("</span>");
	}
	
	private void appendIndent(StringOutput sb, Integer indent) {
		for (int i = 0; i < indent.intValue(); i++) {
			sb.append(INDENT);
		}
	}
}