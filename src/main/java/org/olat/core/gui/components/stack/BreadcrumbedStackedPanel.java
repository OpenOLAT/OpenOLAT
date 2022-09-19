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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.VetoableCloseController;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreadcrumbedStackedPanel extends Panel implements BreadcrumbPanel, ComponentEventListener {
	private static final Logger log = Tracing.createLoggerFor(BreadcrumbedStackedPanel.class);
	private static final ComponentRenderer BAR_RENDERER = new BreadcrumbBarRenderer();
	private static final ComponentRenderer RENDERER = new BreadcrumbedStackedPanelRenderer();
	
	protected final List<Link> stack = new ArrayList<>(3);
	
	protected final Link backLink;
	protected final Link closeLink;
	protected final BreadcrumbBar breadcrumbBar;
	
	private int invisibleCrumb = 1;
	private String cssClass;
	private boolean showCloseLink = false;
	private boolean showCloseLinkForRootCrumb = false;
	private boolean neverDisposeRootController = false;
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name);
		setTranslator(Util.createPackageTranslator(BreadcrumbedStackedPanel.class, translator.getLocale(), translator));
		addListener(listener);
		
		this.cssClass = cssClass;
		
		String barId = getDispatchID().concat("_bbar");
		breadcrumbBar = new BreadcrumbBar(barId);
		
		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		backLink = LinkFactory.createCustomLink("back", "back", "\u00A0", Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		backLink.setIconLeftCSS("o_icon o_icon_back");
		backLink.setTitle(translator.translate("back"));
		backLink.setAriaLabel(translator.translate("back"));
		backLink.setAccessKey("b"); // allow navigation using keyboard

		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		closeLink = LinkFactory.createCustomLink("close", "close", "\u00A0", Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		closeLink.setIconLeftCSS("o_icon o_icon_close_tool");
		closeLink.setCustomDisplayText(translator.translate("close"));
		closeLink.setAriaLabel(translator.translate("close"));
		closeLink.setAccessKey("x"); // allow navigation using keyboard
		
		setDomReplacementWrapperRequired(false);
	}

	/**
	 * Get a string with all css classes to be applied to this DOM element
	 * @return
	 */
	@Override
	public String getCssClass() {
		return cssClass;
	}

	/**
	 * Set and overwrite any existing cssClasses. Use addCssClass to just add a
	 * class
	 * 
	 * @param cssClass
	 */
	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	/**
	 * Add css class to this DOM element. Does not overwrite other classes
	 * @param cssClassToAdd
	 */
	public void addCssClass(String cssClassToAdd) {
		if (this.cssClass == null) {
			setCssClass(cssClassToAdd);							
		} else if (cssClassToAdd != null && !this.cssClass.contains(cssClassToAdd)) {
			setCssClass(this.cssClass + " " + cssClassToAdd);
		}		
	}

	/**
	 * Remove the css class from this DOM element, but keep all the other
	 * classes
	 * 
	 * @param cssClassToRemove
	 */
	public void removeCssClass(String cssClassToRemove) {	
		if (this.cssClass != null && cssClassToRemove != null) {
			setCssClass(this.cssClass.replace(cssClassToRemove, ""));
		}
	}
	
	public int getInvisibleCrumb() {
		return invisibleCrumb;
	}

	public void setInvisibleCrumb(int invisibleCrumb) {
		this.invisibleCrumb = invisibleCrumb;
	}

	public Link getBackLink() {
		return backLink;
	}
	
	public Link getCloseLink() {
		return closeLink;
	}
	
	public boolean isShowCloseLink() {
		return showCloseLink;
	}

	public boolean isShowCloseLinkForRootCrumb() {
		return showCloseLinkForRootCrumb;
	}

	public void setShowCloseLink(boolean showCloseLinkForCrumbs, boolean showCloseLinkForRootCrumb) {
		this.showCloseLink = showCloseLinkForCrumbs;
		this.showCloseLinkForRootCrumb = showCloseLinkForRootCrumb;
	}
	
	public boolean isNeverDisposeRootController() {
		return neverDisposeRootController;
	}

	public void setNeverDisposeRootController(boolean neverDisposeRootController) {
		this.neverDisposeRootController = neverDisposeRootController;
	}
	
	public List<Link> getBreadCrumbs() {
		return stack;
	}
	
	public BreadcrumbBar getBreadcrumbBar() {
		return breadcrumbBar;
	}
	
	@Override
	public Component getComponent(String name) {
		if(breadcrumbBar.getComponentName().equals(name)) {
			return breadcrumbBar;
		}
		return super.getComponent(name);
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>(3 + stack.size());
		cmps.add(breadcrumbBar);
		Component content = getContent();
		if(content != null && content != this) {
			cmps.add(getContent());
		}
		return cmps;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if(cmd != null) {
			if(backLink.getCommand().equals(cmd)) {
				dispatchEvent(ureq, backLink, null);
			} else if(closeLink.getCommand().equals(cmd)) {
				dispatchEvent(ureq, closeLink, null);
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		boolean closeEvent = source.equals(closeLink);
		boolean backEvent = source.equals(backLink);
		if (backEvent || closeEvent) {
			if (stack.size() > 1) {
				// back means to one level down, change source to the stack item one below current
				source = stack.get(stack.size()-2);
				// now continue as if user manually pressed a stack item in the list
			} else {
				// notify listeners that back or link beyond breadcrumb has been called
				fireEvent(ureq, Event.CLOSE_EVENT);
			}
		}
		
		if(stack.contains(source)) {
			Controller controllerToPop = getControllerToPop(source);
			//part of a hack for QTI editor
			if(controllerToPop instanceof VetoableCloseController
					&& !((VetoableCloseController)controllerToPop).requestForClose(ureq)) {
				// not my problem anymore, I have done what I can
				fireEvent(ureq, new VetoPopEvent());
				return;
			}
			BreadCrumb popedCrumb = popController(source);
			if(popedCrumb != null) {
				Controller last = getLastController();
				if(last != null) {
					addToHistory(ureq, last);
				}
				
				if(popedCrumb.getController() != null) {
					fireEvent(ureq, new PopEvent(popedCrumb.getController(), popedCrumb.getUserObject(), closeEvent));
				} else if(popedCrumb.getUserObject() != null) {
					fireEvent(ureq, new PopEvent(popedCrumb.getUserObject(), closeEvent));
				}
			} else if(stack.indexOf(source) == 0) {
				fireEvent(ureq, new RootEvent());
			}
		}
	}
	
	private void addToHistory(UserRequest ureq, Controller controller) {
		WindowControl wControl = controller.getWindowControlForDebug();
		BusinessControlFactory.getInstance().addToHistory(ureq, wControl);
	}

	@Override
	public int size() {
		return stack.size();
	}
	
	@Override
	public Controller getRootController() {
		Controller controller = null;
		if(!stack.isEmpty()) {
			Link lastPath = stack.get(0);
			BreadCrumb crumb = (BreadCrumb)lastPath.getUserObject();
			controller = crumb.getController();
		}
		return controller;
	}
	
	public Controller getLastController() {
		Controller controller = null;
		if(!stack.isEmpty()) {
			Link lastPath = stack.get(stack.size() - 1);
			BreadCrumb crumb = (BreadCrumb)lastPath.getUserObject();
			controller = crumb.getController();
		}
		return controller;
	}
	
	public boolean hasController(Controller controller) {
		return getIndex(controller) >= 0;
	}

	@Override
	public Component popContent() {
		Component component = null;
		if(stack.size() > 1) {
			Link link = stack.remove(stack.size() - 1);
			BreadCrumb crumb = (BreadCrumb)link.getUserObject();
			if(crumb.getController() != null) {
				component = crumb.getController().getInitialComponent();
			}
			crumb.dispose();
		}
		return component;
	}

	@Override
	public boolean popUpToController(Controller controller) {
		int index = getIndex(controller);
		if(index > 0 && index < stack.size() - 1) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}

			setContent(index);
			updateCloseLinkTitle();
			return true;
		}
		return false;
	}

	@Override
	public void popController(Controller controller) {
		int index = getIndex(controller);
		if(index > 0 && index < stack.size()) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i--> index; ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}
			
			setContent(index - 1);
			updateCloseLinkTitle();
		}
	}
	
	@Override
	public void popUserObject(Object uobject) {
		int index = getIndex(uobject);
		if(index > 0 && index < stack.size()) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i--> index; ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}
			
			setContent(index - 1);
			updateCloseLinkTitle();
		}
	}

	@Override
	public void pushContent(Component newContent) {
		setContent(newContent);
	}

	private int getIndex(Controller controller) {
		int index = -1;
		for(int i=0; i<stack.size(); i++) {
			BreadCrumb crumb = (BreadCrumb)stack.get(i).getUserObject();
			if(crumb.getController() == controller) {
				index = i;
			}
		}
		return index;
	}
	
	private int getIndex(Object uobject) {
		int index = -1;
		for(int i=0; i<stack.size(); i++) {
			BreadCrumb crumb = (BreadCrumb)stack.get(i).getUserObject();
			if(crumb.getUserObject() != null && crumb.getUserObject().equals(uobject)) {
				index = i;
			}
		}
		return index;
	}
	
	private Controller getControllerToPop(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.get(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
			}
			return popedCrumb == null ? null : popedCrumb.getController();
		}
		return null;
	}
	
	private BreadCrumb popController(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}

			setContent(index);
			updateCloseLinkTitle();
			return popedCrumb;
		}
		return null;
	}
	
	@Override
	public void rootController(String displayName, Controller controller) {
		if(!stack.isEmpty()) {
			for(int i=stack.size(); i-->0; ) {
				Link link = stack.remove(i);
				BreadCrumb crumb = (BreadCrumb)link.getUserObject();
				
				if(neverDisposeRootController && i == 0) {
					continue;
				}
				crumb.dispose();
			}
		}
		
		pushController(displayName, controller);
	}

	@Override
	public void popUpToRootController(UserRequest ureq) {
		if(stack.size() > 1) {
			for(int i=stack.size(); i-->1; ) {
				Link link = stack.remove(i);
				BreadCrumb crumb = (BreadCrumb)link.getUserObject();
				crumb.dispose();
			}
			
			//set the root controller
			Link rootLink = stack.get(0);
			BreadCrumb rootCrumb  = (BreadCrumb)rootLink.getUserObject();
			if(rootCrumb.getController() != null) {
				setContent(rootCrumb.getController());
			} else {
				setContent((Component)null);
			}
			
			updateCloseLinkTitle();
			fireEvent(ureq, new PopEvent(rootCrumb.getController(), false));
		}
	}

	@Override
	public void pushController(String displayName, Controller controller) {
		pushController(displayName, null, controller, null);
	}

	@Override
	public void pushController(String displayName, String iconLeftCss, Controller controller) {
		pushController(displayName, iconLeftCss, controller, null);
	}
	
	@Override
	public void pushController(String displayName, String iconLeftCss, Object uobject) {
		pushController(displayName, iconLeftCss, null, uobject);
	}

	/**
	 * Push the controller in the stack. If the breadcrumb has no controller, the method
	 * prevent the last breadcrumb to be the same has the new one and be same, it's mean
	 * the same uobject.
	 * 
	 * @param displayName
	 * @param iconLeftCss
	 * @param controller
	 * @param uobject
	 */
	public void pushController(String displayName, String iconLeftCss, Controller controller, Object uobject) {
		//deduplicate last crumb
		if(uobject != null && controller == null && !stack.isEmpty()) {
			Link lastLink = stack.get(stack.size() - 1);
			BreadCrumb lastCrumb = (BreadCrumb)lastLink.getUserObject();
			if(lastCrumb.getController() == null && lastCrumb.getUserObject() != null && lastCrumb.getUserObject().equals(uobject)) {
				stack.remove(lastLink);
			}
		}

		Link link = LinkFactory.createLink("crumb_" + stack.size(), (Translator)null, this);
		link.setCustomDisplayText(StringHelper.escapeHtml(displayName));
		if(StringHelper.containsNonWhitespace(iconLeftCss)) {
			link.setIconLeftCSS(iconLeftCss);
		}
		link.setDomReplacementWrapperRequired(false);
		link.setUserObject(createCrumb(controller, uobject));
		stack.add(link);
		if(controller != null) {
			setContent(controller);
		}
		updateCloseLinkTitle();
	}

	@Override
	public void changeDisplayname(String diplayName) {
		stack.get(stack.size() - 1).setCustomDisplayText(diplayName);
		breadcrumbBar.setDirty(true);
	}

	@Override
	public void changeDisplayname(String displayName, String iconLeftCss, Controller ctrl) {
		for(int i=stack.size(); i-->1; ) {
			Link link = stack.get(i);
			BreadCrumb crumb = (BreadCrumb)link.getUserObject();
			if(crumb.getController() == ctrl) {
				link.setCustomDisplayText(StringHelper.escapeHtml(displayName));
				if(StringHelper.containsNonWhitespace(iconLeftCss)) {
					link.setIconLeftCSS(iconLeftCss);
				} else {
					link.setIconLeftCSS(null);
				}
			}
		}
	}
	
	protected BreadCrumb createCrumb(Controller controller, Object uobject) {
		return new BreadCrumb(controller, uobject);
	}
	
	private void setContent(int crumbIndex) {
		Link currentLink = stack.get(crumbIndex);
		BreadCrumb crumb  = (BreadCrumb)currentLink.getUserObject();
		if(crumb.getController() == null) {
			if(crumbIndex - 1 >= 0) {
				setContent(crumbIndex - 1);
			}
		} else {
			setContent(crumb.getController());
		}
	}
	
	private void setContent(Controller ctrl) {
		Component cmp = ctrl.getInitialComponent();
		if(cmp == this) {
			log.error("Set itself as content is forbidden");
			throw new AssertException("Set itself as content is forbidden");
		}
		setContent(cmp);
	}

	@Override
	public void setContent(Component newContent) {
		// 1: remove any stack css from current active stack
		Component currentComponent = getContent();
		if (currentComponent != null) {
			if (currentComponent instanceof StackedPanel) {
				StackedPanel currentPanel = (StackedPanel) currentComponent;
				String currentStackCss = currentPanel.getCssClass();
				removeCssClass(currentStackCss);
			}
		}
		// 2: update stack with new component on standard Panel
		super.setContent(newContent);
		// 3: add new stack css  
		if (newContent != null) {
			if (newContent instanceof StackedPanel) {
				StackedPanel newPanel = (StackedPanel) newContent;
				String newStackCss = newPanel.getCssClass();
				addCssClass(newStackCss);
			}
		}		

	}
	
	/**
	 * Update the close link title to match the name of the last visible item
	 */
	private void updateCloseLinkTitle() {
		String closeText;
		boolean showClose; 
		if(stack.size() < 2) { 
			// special case: root crumb
			Link link = stack.get(0);
			String unescapedText = StringHelper.unescapeHtml(link.getCustomDisplayText());
			unescapedText = StringHelper.xssScan(unescapedText);
			closeText = getTranslator().translate("doclose", unescapedText);
			showClose = isShowCloseLinkForRootCrumb();
			backLink.setTitle(closeText);
			backLink.setAriaLabel(closeText);
		} else {
			Link link = stack.get(stack.size()-1);
			String unescapedText = StringHelper.unescapeHtml(link.getCustomDisplayText());
			unescapedText = StringHelper.xssScan(unescapedText);
			closeText = getTranslator().translate("doclose", unescapedText);
			showClose = isShowCloseLink();
			backLink.setTitle(getTranslator().translate("back"));
			backLink.setAriaLabel(getTranslator().translate("back"));
		}
		closeLink.setCustomDisplayText(closeText);
		closeLink.setTitle(closeText);
		closeLink.setVisible(showClose);								
	}
	
	public class BreadcrumbBar extends AbstractComponent implements ComponentCollection {
		
		public BreadcrumbBar(String id) {
			super(id, null, null);
			setDomReplacementWrapperRequired(false);
		}
		
		public BreadcrumbedStackedPanel getPanel() {
			return BreadcrumbedStackedPanel.this;
		}
		
		@Override
		public Translator getTranslator() {
			return BreadcrumbedStackedPanel.this.getTranslator();
		}

		@Override
		protected void doDispatchRequest(UserRequest ureq) {
			String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
			if(cmd != null) {
				if(backLink.getCommand().equals(cmd)) {
					dispatchEvent(ureq, backLink, null);
				} else if(closeLink.getCommand().equals(cmd)) {
					dispatchEvent(ureq, closeLink, null);
				}
			}
		}

		@Override
		public ComponentRenderer getHTMLRendererSingleton() {
			return BAR_RENDERER;
		}

		@Override
		public Component getComponent(String name) {
			if(backLink.getComponentName().equals(name)) {
				return backLink;
			}
			if(closeLink.getComponentName().equals(name)) {
				return closeLink;
			}
			for(Link crumb:stack) {
				if(crumb != null && crumb.getComponentName().equals(name)) {
					return crumb;
				}
			}
			return null;
		}

		@Override
		public Iterable<Component> getComponents() {
			List<Component> cmps = new ArrayList<>(3 + stack.size());
			cmps.add(backLink);
			cmps.add(closeLink);
			for(Link crumb:stack) {
				cmps.add(crumb);
			}
			return cmps;
		}
	}

	public static class BreadCrumb {
		
		private final Object uobject;
		private final Controller controller;
		private final List<Tool> tools = new ArrayList<>(5);
		
		public BreadCrumb(Controller controller, Object uobject) {
			this.uobject = uobject;
			this.controller = controller;
		}
		
		public Object getUserObject() {
			return uobject;
		}
	
		public Controller getController() {
			return controller;
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
	
		public void dispose() {
			if(controller != null) {
				controller.dispose();
			}
		}
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
}