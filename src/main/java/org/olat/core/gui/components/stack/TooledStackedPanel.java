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
import java.util.Iterator;
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
 * They are only 1 segment at once.
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooledStackedPanel extends BreadcrumbedStackedPanel implements StackedPanel, BreadcrumbPanel, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new TooledStackedPanelRenderer();
	private boolean toolbarEnabled = true;
	private boolean toolbarAutoEnabled = false;
	private boolean breadcrumbEnabled = true;
	
	private String message;
	private String messageCssClass;
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name, translator, listener, cssClass);
		this.setDomReplacementWrapperRequired(false); // renders own div in Renderer
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
	protected BreadCrumb createCrumb(Controller controller, Object uobject) {
		return new TooledBreadCrumb(controller, uobject);
	}

	/**
	 * If the component is null, it will simply not be added,
	 * @param toolComponent
	 */
	public void addTool(Component toolComponent) {
		addTool(toolComponent, null, false, null);
	}
	
	/**
	 * 
	 * @param toolComponent
	 * @param inherit The tool stay visible if other components are pushed.
	 */
	public void addTool(ButtonGroupComponent toolComponent, boolean inherit) {
		addTool(toolComponent, Align.segment, inherit, null);
	}
	
	/**
	 * 
	 * @param toolComponent
	 * @param inherit The tool stay visible if other components are pushed.
	 */
	public void addTool(Component toolComponent, boolean inherit) {
		addTool(toolComponent, null, inherit, null);
	}
	
	public void addTool(Component toolComponent, Align align) {
		addTool(toolComponent, align, false, null);
	}
	public void addTool(Component toolComponent, Align align, boolean inherit) {
		addTool(toolComponent, align, inherit, null);
	}
	
	public void removeTool(Component toolComponent) {
		if(toolComponent == null) return;
		
		TooledBreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			for(Iterator<Tool> it=breadCrumb.getTools().iterator(); it.hasNext(); ) {
				if(toolComponent == it.next().getComponent()) {
					it.remove();
				}
			}
		}
		setDirty(true);
	}
	
	public void removeAllTools() {
		TooledBreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			breadCrumb.getTools().clear();
		}
		setDirty(true);
	}

	/**
	 * If the component is null, it will simply not be added,
	 * @param toolComponent
	 */
	public void addTool(Component toolComponent, Align align, boolean inherit, String css) {
		if(toolComponent == null) return;
		
		Tool tool = new Tool(toolComponent, align, inherit, css);
		TooledBreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			breadCrumb.addTool(tool);
		}
		setDirty(true);
	}
	
	public List<Tool> getTools() {
		List<Tool> currentTools = new ArrayList<>();
		
		int lastStep = stack.size() - 1;
		for(int i=0; i<lastStep; i++) {
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
		
		TooledBreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			currentTools.addAll(breadCrumb.getTools());
		}
		return currentTools;
	}
	
	private TooledBreadCrumb getCurrentCrumb() {
		if(stack.isEmpty()) {
			return null;
		}
		return (TooledBreadCrumb)stack.get(stack.size() - 1).getUserObject();
	}
	
	@Override
	public void pushController(String displayName, Controller controller) {
		pushController(displayName, null, controller);
	}

	@Override
	public void pushController(String displayName, String iconLeftCss, Controller controller) {
		TooledBreadCrumb currentCrumb = getCurrentCrumb();
		if(currentCrumb == null || currentCrumb.getController() != controller) {
			super.pushController(displayName, iconLeftCss, controller);
			if(controller instanceof TooledController) {
				((TooledController)controller).initTools();
			}
		}
	}

	/**
	 * By default, the toolbar is enabled, using this method it can be disable to just show
	 * the bread crumb path to the user (e.g. course site)
	 * @param enableToolbar
	 */
	public void setToolbarEnabled(boolean enableToolbar) {
		toolbarEnabled = enableToolbar;		
	}
	
	/**
	 * @return true: toolbar is visible ; false: toolbar is not displayed to
	 *         user, but breadcrumb is
	 */
	public boolean isToolbarEnabled() {
		return toolbarEnabled;
	}
	
	public boolean isToolbarAutoEnabled() {
		return toolbarAutoEnabled;
	}
	
	/**
	 * By default, the toolbar is always enabled. Using this method, and setting the
	 * parameter to true, the toolbar will only appear if there is a tool.
	 * 
	 * @param enable
	 */
	public void setToolbarAutoEnabled(boolean enable) {
		toolbarAutoEnabled = enable;
		if(enable) {
			toolbarEnabled = false;
		}
	}

	public boolean isBreadcrumbEnabled() {
		return breadcrumbEnabled;
	}

	public void setBreadcrumbEnabled(boolean breadcrumbEnabled) {
		this.breadcrumbEnabled = breadcrumbEnabled;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageCssClass() {
		return messageCssClass;
	}

	public void setMessageCssClass(String messageCssClass) {
		this.messageCssClass = messageCssClass;
	}



	public static class Tool {
		private final  Align align;
		private final boolean inherit;
		private final Component component;
		private String toolCss;
		
		public Tool(Component component, Align align, boolean inherit, String toolCss) {
			this.align = align;
			this.inherit = inherit;
			this.component = component;
			this.toolCss = toolCss;
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
		
		public String getToolCss() {
			return toolCss;
		}
		
	}
	
	public static class TooledBreadCrumb extends BreadCrumb {
		private final List<Tool> tools = new ArrayList<>(5);

		public TooledBreadCrumb(Controller controller, Object uobject) {
			super(controller, uobject);
		}
		
		public List<Tool> getTools() {
			return tools;
		}
		
		public void addTool(Tool tool) {
			tools.add(tool);
		}
		
		public void removeTool(Tool tool) {
			tools.remove(tool);
		}
	}
	
	public enum Align {
		left,
		right,
		rightEdge,
		segment
	}
	
}