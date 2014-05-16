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
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooledStackedPanel extends BreadcrumbedStackedPanel implements StackedPanel, BreadcrumbPanel, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new TooledStackedPanelRenderer();
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name, translator, listener, cssClass);
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>();
		cmps.add(getBackLink());
		cmps.add(getContent());
		for(Link crumb:stack) {
			cmps.add(crumb);
		}
		for(Tool tool:getTools()) {
			cmps.add(tool.getComponent());
		}
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected BreadCrumb createCrumb(Controller controller) {
		return new TooledBreadCrumb(controller);
	}
	
	/**
	 * If the component is null, it will simply not be added,
	 * @param toolComponent
	 */
	public void addTool(Component toolComponent) {
		addTool(toolComponent, null, false);
	}
	
	public void addTool(Component toolComponent, Align align) {
		addTool(toolComponent, align, false);
	}

	/**
	 * If the component is null, it will simply not be added,
	 * @param toolComponent
	 */
	public void addTool(Component toolComponent, Align align, boolean inherit) {
		if(toolComponent == null) return;
		
		Tool tool = new Tool(toolComponent, align, inherit);
		getCurrentCrumb().addTool(tool);
	}
	
	public List<Tool> getTools() {
		List<Tool> currentTools = new ArrayList<>();
		for(int i=0; i<stack.size(); i++) {
			Object uo = stack.get(i).getUserObject();
			if(uo instanceof TooledBreadCrumb) {
				TooledBreadCrumb crumb = (TooledBreadCrumb)uo;
				List<Tool> tools = crumb.getTools();
				for(Tool tool:tools) {
					if(tool.isInherit()) {
						currentTools.add(tool);
					}
				}
			}
		}
		currentTools.addAll(getCurrentCrumb().getTools());
		if(isShowCloseLink()) {
			currentTools.add(new Tool(getCloseLink(), Align.right, false));
		}
		return currentTools;
	}
	
	private TooledBreadCrumb getCurrentCrumb() {
		if(stack.isEmpty()) {
			return null;
		}
		return (TooledBreadCrumb)stack.get(stack.size() - 1).getUserObject();
	}
	
	public static class Tool {
		private final  Align align;
		private final boolean inherit;
		private final Component component;
		
		public Tool(Component component, Align align, boolean inherit) {
			this.align = align;
			this.inherit = inherit;
			this.component = component;
		}
		
		public boolean isInherit() {
			return inherit;
		}

		public Align getAlign() {
			return align;
		}

		public Component getComponent() {
			return component;
		}
	}
	
	public static class TooledBreadCrumb extends BreadCrumb {
		private final List<Tool> tools = new ArrayList<>(5);

		public TooledBreadCrumb(Controller controller) {
			super(controller);
		}
		
		public List<Tool> getTools() {
			return tools;
		}
		
		public void addTool(Tool tool) {
			tools.add(tool);
		}
	}
	
	public enum Align {
		left,
		right
	}
}