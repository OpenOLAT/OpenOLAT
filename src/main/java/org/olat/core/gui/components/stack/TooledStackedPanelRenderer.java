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
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.stack.TooledStackedPanel.Tool;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooledStackedPanelRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		TooledStackedPanel panel = (TooledStackedPanel) source;
		List<Link> breadCrumbs = panel.getBreadCrumbs();
		List<Tool> tools = panel.getTools();
		
		// panel div
		String mainCssClass = panel.getCssClass();
		sb.append("<div id='o_c").append(source.getDispatchID()).append("' class='").append(mainCssClass, mainCssClass != null).append("'>");
		
		if((panel.isBreadcrumbEnabled() && breadCrumbs.size() > panel.getInvisibleCrumb()) || (tools.size() > 0 && panel.isToolbarEnabled())) {
			sb.append("<div id='o_main_toolbar' class='o_toolbar'>");

			if(panel.isBreadcrumbEnabled() && breadCrumbs.size() > panel.getInvisibleCrumb()) {
				sb.append("<div class='o_breadcrumb'><ol class='breadcrumb'>");
				Link backLink = panel.getBackLink();
				int numOfCrumbs = breadCrumbs.size();
				if(backLink.isVisible() && numOfCrumbs > panel.getInvisibleCrumb()) {
					sb.append("<li class='o_breadcrumb_back'>");
					backLink.getHTMLRendererSingleton().render(renderer, sb, backLink, ubu, translator, renderResult, args);
					sb.append("</li>");
					
					for(Link crumb:breadCrumbs) {
						sb.append("<li").append(" class='active'", breadCrumbs.indexOf(crumb) == numOfCrumbs-1).append(">");
						renderer.render(crumb, sb, args);
						sb.append("</li>");
					}
				}

				Link closeLink = panel.getCloseLink();
				if (closeLink.isVisible()) {
					sb.append("<li class='o_breadcrumb_close'>");
					closeLink.getHTMLRendererSingleton().render(renderer, sb, closeLink, ubu, translator, renderResult, args);
					sb.append("</li>");				
				}	

				sb.append("</ol></div>"); // o_breadcrumb
			}
			
			System.out.println(panel.isToolbarAutoEnabled() + " " + panel.isToolbarEnabled());
			if (panel.isToolbarAutoEnabled() || panel.isToolbarEnabled()) {
				List<Tool> leftTools = getTools(tools, Align.left);
				List<Tool> rightEdgeTools = getTools(tools, Align.rightEdge);
				List<Tool> rightTools = getTools(tools, Align.right);
				List<Tool> segmentsTools = getTools(tools, Align.segment);
				List<Tool> notAlignedTools = getTools(tools, null);
				
				if(panel.isToolbarEnabled() || (panel.isToolbarAutoEnabled()
						&& (leftTools.size() > 0 || rightTools.size() > 0 || rightTools.size() > 0 || notAlignedTools.size() > 0 || segmentsTools.size() > 0))) {
					sb.append("<div class='o_tools_container'><div class='container-fluid'>");
					
					if(leftTools.size() > 0) {
						sb.append("<ul class='o_tools o_tools_left list-inline'>");
						renderTools(leftTools, renderer, sb, args);
						sb.append("</ul>");
					}
					
					if(rightEdgeTools.size() > 0) {
						sb.append("<ul class='o_tools o_tools_right_edge list-inline'>");
						renderTools(rightEdgeTools, renderer, sb, args);
						sb.append("</ul>");
					}

					if(rightTools.size() > 0) {
						sb.append("<ul class='o_tools o_tools_right list-inline'>");
						renderTools(rightTools, renderer, sb, args);
						sb.append("</ul>");
					}

					if(notAlignedTools.size() > 0) {
						sb.append("<ul class='o_tools o_tools_center list-inline'>");
						renderTools(notAlignedTools, renderer, sb, args);
						sb.append("</ul>");
					}
					sb.append("</div>"); // container-fluid,
					
					if(segmentsTools.size() > 0) {
						boolean segmentAlone = leftTools.isEmpty() && rightTools.isEmpty()
								&& rightTools.isEmpty() && notAlignedTools.isEmpty();
						sb.append("<ul class='o_tools o_tools_segments list-inline")
						  .append(" o_tools_segments_alone", segmentAlone).append("'>");
						
						Tool segmentTool = segmentsTools.get(segmentsTools.size() - 1);
						List<Tool> lastSegmentTool = Collections.singletonList(segmentTool);
						renderTools(lastSegmentTool, renderer, sb, args);
						
						sb.append("</ul>");
					}
					sb.append("</div>"); 
				}
			}
			sb.append("</div>"); // o_toolbar
		}
		
		if(StringHelper.containsNonWhitespace(panel.getMessage())) {
			sb.append("<div class='o_toolbar_message ");
			if(StringHelper.containsNonWhitespace(panel.getMessageCssClass())) {
				sb.append(panel.getMessageCssClass());
			}
			sb.append("'>").append(panel.getMessage()).append("</div>");
		}
		
		Component toRender = panel.getContent();
		if(toRender != null) {
			renderer.render(sb, toRender, args);
		}

		sb.append("</div>"); // end of panel div
	}
	
	private List<Tool> getTools(List<Tool> tools, Align alignement) {
		List<Tool> alignedTools = new ArrayList<>(tools.size());
		if(alignement == null) {
			for(Tool tool:tools) {
				if(tool.getAlign() == null && tool.getComponent().isVisible()) {
					alignedTools.add(tool);
				}
			}
		} else {
			for(Tool tool:tools) {
				if(alignement.equals(tool.getAlign()) && tool.getComponent().isVisible()) {
					alignedTools.add(tool);
				}
			}
		}
		return alignedTools;
	}
	
	private void renderTools(List<Tool> tools, Renderer renderer, StringOutput sb, String[] args) {
		int numOfTools = tools.size();
		for(int i=0; i<numOfTools; i++) {
			Tool tool = tools.get(i);
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
}