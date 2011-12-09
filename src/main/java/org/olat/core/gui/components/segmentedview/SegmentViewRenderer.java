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
package org.olat.core.gui.components.segmentedview;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

public class SegmentViewRenderer implements ComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		SegmentViewComponent component = (SegmentViewComponent)source;
		if(component.isEmpty()) return;

		sb.append("<div class=\"b_segments_container\">");//start b_segments_container
		sb.append("<div class=\"b_segments\">");//start b_segments	
		sb.append("<ul>");
		
		List<Component> segments = component.getSegments();
		int count = 0;
		int numOfSegments = segments.size();
		for(Component segment:segments) {
			sb.append("<li class=\"b_segment");
			if(component.isSelected(segment)) {
				sb.append(" b_segment_selected");
			}
			if(count == 0) {
				sb.append(" b_segment_first");
			}
			if(count == (numOfSegments - 1)) {
				sb.append(" b_segment_last");
			}
			sb.append("\">");
			
			ComponentRenderer subRenderer = segment.getHTMLRendererSingleton();
			Translator subTranslator = segment.getTranslator();
			subRenderer.render(renderer, sb, segment, ubu, subTranslator, renderResult, args);
			sb.append("</li>");
			count++;
		}

		sb.append("</ul>");
		sb.append("<div class=\"b_clearfix\"></div>");
		sb.append("</div>");//end b_segments
		sb.append("</div>");//end b_segments_container
		
		
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderingState rstate) {
		//
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer, StringOutput sb, Component source, RenderingState rstate) {
		//
	}

	
}
