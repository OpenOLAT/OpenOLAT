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
package org.olat.modules.portfolio.ui.component;

import org.json.JSONObject;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 05.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TimelineComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {

		TimelineComponent cmp = (TimelineComponent)source;
		String dispatchId = cmp.getDispatchID();
		sb.append("<div id='timeline_").append(dispatchId).append("'></div>");
		sb.append("<script>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n")
		  .append(" jQuery('#timeline_").append(dispatchId).append("').timeline({")
		  .append("   parentContainerId: '").append(cmp.getContainerId()).append("',\n");
		if(cmp.getStartTime() != null && cmp.getEndTime() != null) {
			sb.append(" startTime: ").append(cmp.getStartTime().getTime()).append(",\n")
			  .append(" endTime: ").append(cmp.getEndTime().getTime()).append(",\n");
		}
		//status translations
		sb.append("   status: { draft: '").append(translator.translate("status.draft"))
		  .append("', published: '").append(translator.translate("status.published"))
		  .append("', inRevision: '").append(translator.translate("status.in.revision"))
		  .append("', closed: '").append(translator.translate("status.closed"))
		  .append("', deleted: '").append(translator.translate("status.deleted"))
		  .append("'},\n");
		//date format
		String dateFormat = cmp.getD3DateFormat(translator.getLocale());
		sb.append("  dateFormat: '").append(dateFormat).append("',\n");
		//values
		sb.append("   values: [");
		if(cmp.getPoints() != null && !cmp.getPoints().isEmpty()) {
			int numOfPoints = cmp.getPoints().size();
			for(int i=0; i<numOfPoints; i++) {
				TimelinePoint point = cmp.getPoints().get(i);
				if(i > 0) sb.append(",");
				sb.append("{ id:\"").append(point.getId()).append("\"")
				  .append(", \"title\":").append(JSONObject.quote(StringHelper.escapeHtml(point.getTitle())))
				  .append(", \"time\":").append(point.getDate().getTime()).append("")
				  .append(", \"status\":\"").append(point.getStatus()).append("\"}");
			}
		}
		sb.append("]\n")
		  .append("  });\n");
		sb.append("});\n")
		  .append("/* ]]> */\n")
		  .append("</script>\n");	
	}
}
