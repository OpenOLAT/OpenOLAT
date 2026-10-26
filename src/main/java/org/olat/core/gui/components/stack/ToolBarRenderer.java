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
package org.olat.core.gui.components.stack;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel.Tool;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.stack.TooledStackedPanel.ToolBar;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 7 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToolBarRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		ToolBar toolBar = (ToolBar)source;
		TooledStackedPanel panel = toolBar.getPanel();
		List<Tool> leftTools = getVisibleTools(panel, Align.left);
		List<Tool> rightEdgeTools = getVisibleTools(panel, Align.rightEdge);
		List<Tool> rightTools = getVisibleTools(panel, Align.right);
		List<Tool> segmentsTools = getVisibleTools(panel, Align.segment);
		List<Tool> centerTools = getVisibleTools(panel, Align.center);
		
		if(panel.isToolbarEnabled() || (panel.isToolbarAutoEnabled()
				&& (!leftTools.isEmpty() || !rightTools.isEmpty() || !rightEdgeTools.isEmpty() || !centerTools.isEmpty() || !segmentsTools.isEmpty()))) {
			sb.append("<div id='o_c").append(source.getDispatchID()).append("' class='o_tools_container'><div class='container-fluid'>");

			renderTools(leftTools, Align.left, renderer, sb, args);
			renderTools(centerTools, Align.center, renderer, sb, args);
			renderTools(rightTools, Align.right, renderer, sb, args);
			renderTools(rightEdgeTools, Align.rightEdge, renderer, sb, args);

			sb.append("</div>"); // container-fluid,
			
			if(!segmentsTools.isEmpty()) {
				boolean segmentAlone = leftTools.isEmpty() && rightTools.isEmpty() && centerTools.isEmpty() && rightEdgeTools.isEmpty();
				sb.append("<ul class='o_tools o_tools_segments list-inline")
				  .append(" o_tools_segments_alone", segmentAlone).append("'>");
				
				Tool segmentTool = segmentsTools.get(segmentsTools.size() - 1);
				renderTool(segmentTool, renderer, sb, args);
				
				sb.append("</ul>");
			}
			sb.append("</div>"); 
		} else {
			sb.append("<div id='o_c").append(source.getDispatchID()).append("'></div>");
		}
	}
	
	private List<Tool> getVisibleTools(TooledStackedPanel panel, Align align) {
		List<Tool> tools = panel.getTools(align);
		List<Tool> visibleTools = new ArrayList<>(tools.size());
		for(Tool tool:tools) {
			if(tool.getComponent().isVisible()) {
				visibleTools.add(tool);
			} else {
				tool.getComponent().setDirty(false);
			}
		}
		return visibleTools;
	}
	
	private void renderTools(List<Tool> tools, Align align, Renderer renderer, StringOutput sb, String[] args) {
		if(!tools.isEmpty()) {
			sb.append("<ul class='o_tools ").append(align.cssClass()).append(" list-inline'>");
			for(Tool tool:tools) {
				renderTool(tool, renderer, sb, args);
			}
			sb.append("</ul>");
		}
	}
	
	private void renderTool(Tool tool, Renderer renderer, StringOutput sb, String[] args) {
		Component cmp = tool.getComponent();
		String cssClass = tool.getToolCss();
		if (cssClass == null) {
			// use defaults
			if(cmp instanceof Dropdown) {
				cssClass = "o_tool_dropdown dropdown";
			} else if(cmp instanceof Link && !cmp.isEnabled()) {
				cssClass = "o_text";
			} else {
				cssClass = "o_tool";
			}				
		}
		sb.append("<li class='").append(cssClass).append("'>");
		renderer.render(cmp, sb, args);
		sb.append("</li>");
	}
}
