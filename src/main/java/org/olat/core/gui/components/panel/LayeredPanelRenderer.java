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
package org.olat.core.gui.components.panel;

import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.RenderingState;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Renderer for the LayeredPanel. Renders the layers with a class
 * "b_layeredPanel" and a z-index style starting with the configuration on the
 * LayeredPanel component.
 * 
 * <P>
 * Initial Date: 28.10.2010 <br>
 * 
 * @author gnaegi
 */
public class LayeredPanelRenderer extends PanelRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {

		LayeredPanel panel = (LayeredPanel) source;
		int layerLevel = panel.getStartLayerIndex();
		int increment = panel.getIndexIncrement();
		List<Component> layers = panel.getLayers();
		// Render lower layers first, highest last
		int level = 0;
		for (Component component : layers) {
			if (component.getSpanAsDomReplaceable()) {
				sb.append("<span");
			} else {
				sb.append("<div");
			}
			sb.append(" class=\"o_layered_panel o_layer_").append(level).append("\" style=\"z-index:")
					.append(layerLevel).append("\">");
			renderer.render(sb, component, args);
			if (component.getSpanAsDomReplaceable()) {
				sb.append("</span>");
			} else {
				sb.append("</div>");
			}

			// Increment layer z-index for next layer iteration
			layerLevel += increment;
			level++;
		}
	}

	@Override
	public void renderHeaderIncludes(Renderer renderer, StringOutput sb,
			Component source, URLBuilder ubu, Translator translator,
			RenderingState rstate) {
		LayeredPanel panel = (LayeredPanel) source;
		List<Component> layers = panel.getLayers();
		//
		for (Component component : layers) {
			if (component != null) {
				// delegate header rendering to the content
				renderer.renderHeaderIncludes(sb, component, rstate);
			}
		}
	}

	@Override
	public void renderBodyOnLoadJSFunctionCall(Renderer renderer,
			StringOutput sb, Component source, RenderingState rstate) {
		LayeredPanel panel = (LayeredPanel) source;
		List<Component> layers = panel.getLayers();
		//
		for (Component component : layers) {
			if (component != null) {
				// delegate header rendering to the content
				renderer.renderBodyOnLoadJSFunctionCall(sb, component, rstate);
			}
		}
	}
}