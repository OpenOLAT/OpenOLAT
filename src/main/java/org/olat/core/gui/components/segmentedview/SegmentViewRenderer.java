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
import java.util.stream.Collectors;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

class SegmentViewRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		SegmentViewComponent component = (SegmentViewComponent)source;
		List<Component> visibleComponents = component.getSegments()
				.stream()
				.filter(Component::isVisible)
				.collect(Collectors.toList());
		
		if(visibleComponents.isEmpty() || (component.isDontShowSingleSegment() && visibleComponents.size() == 1)) {
			return;
		}

		sb.append("<div class='o_segments btn-group btn-group-justified' role='navigation'>");
		for(Component segment:visibleComponents) {
			ComponentRenderer subRenderer = segment.getHTMLRendererSingleton();
			Translator subTranslator = segment.getTranslator();
			subRenderer.render(renderer, sb, segment, ubu, subTranslator, renderResult, args);
			segment.setDirty(false);
		}
		sb.append("</div>");
	}
}