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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * 
 * They are only 1 segment at once.
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TooledStackedPanel extends BreadcrumbedStackedPanel {
	
	private static final ComponentRenderer RENDERER = new TooledStackedPanelRenderer();
	private static final ComponentRenderer TOOLS_RENDERER = new ToolBarRenderer();
	private boolean toolbarEnabled = true;
	private boolean toolbarAutoEnabled = false;
	private boolean breadcrumbEnabled = true;
	
	private String message;
	private String messageCssClass;
	private Component navigationCmp;
	private Controller navigationBindController;
	private final ToolBar toolBar;
	private final SimpleStackedPanel messageCmp;
	private final EnumMap<Align,ToolsSlot> toolsSlots;
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public TooledStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name, translator, listener, cssClass);
		setDomReplacementWrapperRequired(false); // renders own div in Renderer
		toolsSlots = new EnumMap<>(Align.class);
		for(Align val:Align.values()) {
			toolsSlots.put(val, new ToolsSlot(val));
		}
		toolBar = new ToolBar(getDispatchID().concat("_tbar"));
		messageCmp = new SimpleStackedPanel(getDispatchID().concat("_tmsg")) ;
	}
	
	public ToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>();
		cmps.add(getBreadcrumbBar());
		cmps.add(getContent());
		cmps.add(toolBar);
		if(messageCmp != null) {
			cmps.add(messageCmp);
		}
		if(navigationCmp != null) {
			cmps.add(navigationCmp);
		}
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected BreadCrumb createCrumb(Controller controller, Object uobject) {
		return new BreadCrumb(controller, uobject);
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
		
		BreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			removeTool(toolComponent, breadCrumb);
		}
	}
	
	public void removeTool(Component toolComponent, Controller controller) {
		for(int i=0; i<stack.size(); i++) {
			Object uo = stack.get(i).getUserObject();
			if(uo instanceof BreadCrumb) {
				BreadCrumb crumb = (BreadCrumb)uo;
				if (controller.equals(crumb.getController())) {
					removeTool(toolComponent, crumb);
				}
			}
		}
	}
	
	private void removeTool(Component toolComponent, BreadCrumb breadCrumb) {
		for(Iterator<Tool> it=breadCrumb.getTools().iterator(); it.hasNext(); ) {
			if(toolComponent == it.next().getComponent()) {
				it.remove();
				toolBar.setDirty(true);
			}
		}
	}
	
	public void removeAllTools() {
		BreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			breadCrumb.getTools().clear();
		}
		toolBar.setDirty(true);
	}

	/**
	 * If the component is null, it will simply not be added,
	 * @param toolComponent
	 */
	public void addTool(Component toolComponent, Align align, boolean inherit, String css) {
		addTool(toolComponent, align, inherit, css, null);
	}
	
	public void addTool(Component toolComponent, Align align, boolean inherit, String css, Controller controller) {
		if(toolComponent == null) return;
		
		align = align == null ? Align.center : align;
		Tool tool = new Tool(toolComponent, align, inherit, css);
		BreadCrumb breadCrumb = controller == null
				? getCurrentCrumb()
				: getBreadCrumb(controller);
		if(breadCrumb != null) {
			breadCrumb.addTool(tool);
		}
		toolBar.setDirty(true);
	}

	private BreadCrumb getBreadCrumb(Controller controller) {
		for(int i=0; i<stack.size(); i++) {
			Object uo = stack.get(i).getUserObject();
			if(uo instanceof BreadCrumb) {
				BreadCrumb crumb = (BreadCrumb)uo;
				if (controller.equals(crumb.getController())) {
					return crumb;
				}
			}
		}
		return null;
	}
	
	public List<Tool> getTools() {
		List<Tool> currentTools = new ArrayList<>();
		
		int lastStep = stack.size() - 1;
		for(int i=0; i<lastStep; i++) {
			Object uo = stack.get(i).getUserObject();
			if(uo instanceof BreadCrumb) {
				BreadCrumb crumb = (BreadCrumb)uo;
				List<Tool> tools = crumb.getTools();
				for(Tool tool:tools) {
					if(tool.isInherit()) {
						currentTools.add(tool);
					}
				}
			}
		}
		
		BreadCrumb breadCrumb = getCurrentCrumb();
		if(breadCrumb != null) {
			currentTools.addAll(breadCrumb.getTools());
		}
		return currentTools;
	}
	
	public List<Tool> getTools(Align alignement) {
		List<Tool> tools = getTools();
		return tools.stream()
			.filter(tool -> alignement.equals(tool.getAlign()))
			.collect(Collectors.toList());
	}
	
	public boolean hasVisibleTool(Align alignement) {
		List<Tool> tools = getTools();
		return tools.stream()
			.anyMatch(tool -> alignement.equals(tool.getAlign()) && tool.getComponent().isVisible());
	}
	
	private BreadCrumb getCurrentCrumb() {
		if(stack.isEmpty()) {
			return null;
		}
		return (BreadCrumb)stack.get(stack.size() - 1).getUserObject();
	}
	
	@Override
	public void pushController(String displayName, Controller controller) {
		pushController(displayName, null, controller);
	}

	@Override
	public void pushController(String displayName, String iconLeftCss, Controller controller) {
		BreadCrumb currentCrumb = getCurrentCrumb();
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
	
	public void setToolsLimit(Align slot, int maxNumberOfTools, String dropdownI18nKey) {
		setToolsLimit(slot, maxNumberOfTools, dropdownI18nKey, null);
	}

	/**
	 * 
	 * @param slot The slot to limit
	 * @param maxNumberOfTools The maximum of tools visible in the toolbar
	 * @param dropdownI18nKey The i18n key to label the dropdown
	 * @param dropdownIconCss The CSS class to decorate the dropdown
	 */
	public void setToolsLimit(Align slot, int maxNumberOfTools, String dropdownI18nKey, String dropdownIconCss) {
		ToolsSlot config = toolsSlots.get(slot);
		config.setLimitOfTools(maxNumberOfTools);
		config.setToolDropdownI18nKey(dropdownI18nKey);
		if(StringHelper.containsNonWhitespace(dropdownIconCss)) {
			config.setToolDropdownIconCss(dropdownIconCss);
		} else {
			config.setToolDropdownIconCss("o_icon o_icon_menuhandel");
		}
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
	
	public boolean hasMessage() {
		return messageCmp.getContent() != null;
	}

	/**
	 * 
	 * @return The container of the message
	 */
	public StackedPanel getMessagePanel() {
		return messageCmp;
	}

	public void setMessageComponent(Component cmp) {
		if(messageCmp.getContent() != messageCmp) {
			messageCmp.setContent(cmp);
		}
	}
	
	public void removeMessageComponent() {
		messageCmp.setContent(null);
	}
	
	public Component getNavigationComponent() {
		return navigationCmp;
	}
	
	public Controller getNavigationBindController() {
		return navigationBindController;
	}

	/**
	 * Set a navigation component.
	 * If a navigationBindController is set, the navigation component is only visible if the navigationBindController is visible.
	 *
	 * @param navigationCmp
	 * @param navigationBindController
	 */
	public void setNavigationComponent(Component navigationCmp, Controller navigationBindController) {
		if(this.navigationCmp != navigationCmp) {
			this.navigationCmp = navigationCmp;
			this.navigationBindController = navigationBindController;
			setDirty(true);
		}
	}
	
	public void removeNavigationComponent() {
		navigationCmp = null;
		navigationBindController = null;
		setDirty(true);
	}
	
	public enum Align {
		left("o_tools_left"),
		center("o_tools_center"),
		right("o_tools_right"),
		rightEdge("o_tools_right_edge"),
		segment("o_tools_segments");
		
		private final String cssClass;
		
		private Align(String cssClass) {
			this.cssClass = cssClass;
		}
		
		public String cssClass() {
			return cssClass;
		}
	}
	
	public class ToolsSlot {
		
		private final Align slot;
		private int limitOfTools = 32;
		private String toolDropdownI18nKey;
		private String toolDropdownIconCss;
		
		public ToolsSlot(Align slot) {
			this.slot = slot;
		}

		public int getLimitOfTools() {
			return limitOfTools;
		}

		public void setLimitOfTools(int limitOfTools) {
			this.limitOfTools = limitOfTools;
		}

		public Align getSlot() {
			return slot;
		}

		public String getToolDropdownI18nKey() {
			return toolDropdownI18nKey;
		}

		public void setToolDropdownI18nKey(String i18nKey) {
			toolDropdownI18nKey = i18nKey;
		}

		public String getToolDropdownIconCss() {
			return toolDropdownIconCss;
		}

		public void setToolDropdownIconCss(String toolDropdownIconCss) {
			this.toolDropdownIconCss = toolDropdownIconCss;
		}
	}
	
	public class ToolBar extends AbstractComponent implements ComponentCollection {
		
		public ToolBar(String id) {
			super(id, null, null);
			setDomReplacementWrapperRequired(false);
		}
		
		public TooledStackedPanel getPanel() {
			return TooledStackedPanel.this;
		}
		
		public ToolsSlot getSlot(Align align) {
			return toolsSlots.get(align);
		}
		
		@Override
		public Translator getTranslator() {
			return TooledStackedPanel.this.getTranslator();
		}
		
		@Override
		public Component getComponent(String name) {
			for(Tool tool:getTools()) {
				Component cmp = tool.getComponent();
				if(cmp != null && cmp.getComponentName().equals(name)) {
					return cmp;
				}
			}
			return null;
		}

		@Override
		public Iterable<Component> getComponents() {
			List<Component> cmps = new ArrayList<>();
			for(Tool tool:getTools()) {
				cmps.add(tool.getComponent());
			}
			return cmps;
		}

		@Override
		protected void doDispatchRequest(UserRequest ureq) {
			//
		}

		@Override
		public ComponentRenderer getHTMLRendererSingleton() {
			return TOOLS_RENDERER;
		}
	}
}