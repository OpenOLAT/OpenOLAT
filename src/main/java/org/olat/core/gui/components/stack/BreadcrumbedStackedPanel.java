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
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 25.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BreadcrumbedStackedPanel extends Panel implements StackedPanel, BreadcrumbPanel, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new BreadcrumbedStackedPanelRenderer();
	
	protected final List<Link> stack = new ArrayList<>(3);
	
	protected final Link backLink;
	protected final Link closeLink;
	
	private String cssClass;
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener) {
		this(name, translator, listener, null);
	}
	
	public BreadcrumbedStackedPanel(String name, Translator translator, ComponentEventListener listener, String cssClass) {
		super(name);
		setTranslator(Util.createPackageTranslator(BreadcrumbedStackedPanel.class, translator.getLocale(), translator));
		addListener(listener);
		
		this.cssClass = cssClass;
		
		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		backLink = LinkFactory.createCustomLink("back", "back", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		backLink.setCustomEnabledLinkCSS("b_breadcumb_back");
		backLink.setCustomDisplayText("&#x25C4;"); // unicode back arrow (black left pointer symbol)
		backLink.setTitle(translator.translate("back"));
		backLink.setAccessKey("b"); // allow navigation using keyboard

		// Add back link before the bread crumbs, when pressed delegates click to current bread-crumb - 1
		closeLink = LinkFactory.createCustomLink("close", "close", null, Link.NONTRANSLATED + Link.LINK_CUSTOM_CSS, null, this);
		closeLink.setIconLeftCSS("o_icon o_icon_close_tab");
		closeLink.setCustomDisplayText(translator.translate("doclose"));
		closeLink.setAccessKey("x"); // allow navigation using keyboard
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public Link getBackLink() {
		return backLink;
	}
	
	public Link getCloseLink() {
		return closeLink;
	}
	
	public List<Link> getBreadCrumbs() {
		return stack;
	}

	@Override
	public Component getComponent(String name) {
		return super.getComponent(name);
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>(3 + stack.size());
		cmps.add(backLink);
		cmps.add(closeLink);
		cmps.add(getContent());
		for(Link crumb:stack) {
			cmps.add(crumb);
		}
		return cmps;
	}

	@Override
	public Map<String, Component> getComponentMap() {
		return super.getComponentMap();
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
		if (source.equals(backLink) || source.equals(closeLink)) {
			if (stack.size() > 1) {
				// back means to one level down, change source to the stack item one below current
				source = stack.get(stack.size()-2);
				// now continue as if user manually pressed a stack item in the list
			}
		}
		
		if(stack.contains(source)) {
			Controller popedCtrl = popController(source);
			if(popedCtrl != null) {
				fireEvent(ureq, new PopEvent(popedCtrl));
			}
		}
	}

	@Override
	public void popContent() {
		if(stack.size() > 1) {
			Link link = stack.remove(stack.size() - 1);
			BreadCrumb crumb = (BreadCrumb)link.getUserObject();
			crumb.dispose();
		}
	}

	@Override
	public void pushContent(Component newContent) {
		setContent(newContent);
	}
	
	public void popController(Controller controller) {
		for(Link link:stack) {
			BreadCrumb crumb = (BreadCrumb)link.getUserObject();
			if(crumb.getController() == controller) {
				popController(link);
			}
		}
	}
	
	private Controller popController(Component source) {
		int index = stack.indexOf(source);
		if(index < (stack.size() - 1)) {
			
			BreadCrumb popedCrumb = null;
			for(int i=stack.size(); i-->(index+1); ) {
				Link link = stack.remove(i);
				popedCrumb = (BreadCrumb)link.getUserObject();
				popedCrumb.dispose();
			}

			Link currentLink = stack.get(index);
			BreadCrumb crumb  = (BreadCrumb)currentLink.getUserObject();
			setContent(crumb.getController());
			updateCloseLinkTitle();
			return popedCrumb.getController();
		}
		return null;
	}

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
			setContent(rootCrumb.getController()); 
			updateCloseLinkTitle();
			fireEvent(ureq, new PopEvent(rootCrumb.getController()));
		}
	}

	@Override
	public void pushController(String displayName, Controller controller) {
		Link link = LinkFactory.createLink("crumb_" + stack.size(), (Translator)null, this);
		link.setCustomDisplayText(StringHelper.escapeHtml(displayName));
		link.setDomReplacementWrapperRequired(false);
		link.setUserObject(createCrumb(controller));
		stack.add(link);
		setContent(controller);
		updateCloseLinkTitle();
	}
	
	protected BreadCrumb createCrumb(Controller controller) {
		return new BreadCrumb(controller);
	}
	
	private void setContent(Controller ctrl) {
		super.setContent(ctrl.getInitialComponent());
	}
	
	/**
	 * Update the close link title to match the name of the last visible item
	 */
	private void updateCloseLinkTitle() {
		if(stack.size() < 2) { 
			// special case: don't show close for last level
			closeLink.setVisible(false);								
		} else {
			Link link = stack.get(stack.size()-1);
			closeLink.setCustomDisplayText(getTranslator().translate("doclose", new String[] { link.getCustomDisplayText() }));	
			closeLink.setVisible(true);								
		}
	}
	
	public static class BreadCrumb {
		private final Controller controller;
		
		public BreadCrumb(Controller controller) {
			this.controller = controller;
		}

		public Controller getController() {
			return controller;
		}

		public void dispose() {
			controller.dispose();
		}
	}
}