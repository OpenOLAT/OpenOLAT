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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class LinkedViewRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator, RenderResult renderResult, String[] args) {
		SegmentViewComponent component = (SegmentViewComponent)source;
		if(component.isEmpty() || (component.isDontShowSingleSegment() && component.getSegments().size() == 1)) {
			return;
		}

		sb.append("<nav class='o_segments o_segments_linked'>");
		Component selectedComponent = component.getSelectedComponent();
		if(selectedComponent != null) {
			selectedComponent.getHTMLRendererSingleton().render(renderer, sb, selectedComponent, ubu, selectedComponent.getTranslator(), renderResult, args);
			selectedComponent.setDirty(true);
		}

		boolean first = true;
		for(Component segment:component.getSegments()) {
			if(segment != selectedComponent) {
				if(!first) {
					sb.append("<span class='o_segment_separator'>\u007C</span>");
				} else {
					first = false;
				}
				segment.getHTMLRendererSingleton().render(renderer, sb, segment, ubu, segment.getTranslator(), renderResult, args);
				segment.setDirty(false);
			}
		}
		sb.append("</nav>");
	}
}