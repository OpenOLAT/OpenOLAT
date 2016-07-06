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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

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
		sb.append("<div id='timeline'></div>");
		sb.append("<script type='text/javascript'>\n")
		  .append("/* <![CDATA[ */ \n")
		  .append("jQuery(function() {\n");
		
		sb.append("	jQuery('#timeline').timeline({")
		  .append("    parentContainerId: '").append(cmp.getContainerId()).append("',\n")
		  .append("    values: [\n");
		if(cmp.getPoints() != null && cmp.getPoints().size() > 0) {
			int numOfPoints = cmp.getPoints().size();
			for(int i=0; i<numOfPoints; i++) {
				TimelinePoint point = cmp.getPoints().get(i);
				if(i > 0) sb.append(",");
				sb.append("{ id:'").append(point.getId()).append("', time:").append(point.getDate().getTime()).append(", status:'").append(point.getStatus()).append("'}");
			}
		}
		sb.append("    ]")
		  .append("  });");
		sb.append("});\n")
		  .append("/* ]]> */\n")
		  .append("</script>");	
	}
}
