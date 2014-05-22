/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Description:<br>
 * Controller for the toolbar
 * <P>
 * Initial Date: 26 jul. 2010 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ToolbarController extends BasicController {

	private final Map<Component, Object> ownershipMap = new HashMap<Component, Object>();

	private final List<Component> leftComponents = new ArrayList<Component>();
	private final List<Component> centeredComponents = new ArrayList<Component>();
	private final List<Component> rightComponents = new ArrayList<Component>();

	private VelocityContainer toolbarVC;

	public ToolbarController(UserRequest ureq, WindowControl wControl, Translator translator) {
		super(ureq, wControl, translator);

		toolbarVC = createVelocityContainer("toolbar");
		toolbarVC.contextPut("toolbarItems", leftComponents);
		toolbarVC.contextPut("toolbarCenteredItems", centeredComponents);
		toolbarVC.contextPut("toolbarItemsAtRight", rightComponents);
		putInitialPanel(toolbarVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	public List<Component> getToolbarItemsAtLeft() {
		return leftComponents;
	}

	public List<Component> getToolbarItemsAtRight() {
		return rightComponents;
	}

	public Component addToolbarComponent(Component cmp, Object owner, Position pos) {
		addToolbarComponentToMapAndList(cmp, owner, pos);
		toolbarVC.put(cmp.getComponentName(), cmp);
		return cmp;
	}

	/**
	 * adds the given component to the owner-map and to the correct
	 * component-list (according to position) does _NOT_ add the component to
	 * velocity-container
	 * 
	 * @param cmp
	 * @param owner
	 * @param pos
	 * @return
	 */
	private Component addToolbarComponentToMapAndList(Component cmp, Object owner, Position pos) {
		ownershipMap.put(cmp, owner);
		switch (pos) {
		case left:
			leftComponents.add(cmp);
			break;
		case center:
			centeredComponents.add(cmp);
			break;
		case right:
			rightComponents.add(cmp);
			break;
		}
		return cmp;
	}

	public TextComponent addToolbarText(String name, Object owner, Position pos) {
		return addToolbarText(name, name, owner, pos);
	}

	public TextComponent addToolbarText(String name, String text, Object owner, Position pos) {
		TextComponent textCmp = TextFactory.createTextComponentFromString(name, text, null, true, toolbarVC);
		textCmp.setDomReplacementWrapperRequired(false);
		textCmp.setCssClass("navbar-text");
		addToolbarComponentToMapAndList(textCmp, owner, pos);
		return textCmp;
	}

	public Link addToolbarLink(String name, Object owner, Position pos) {
		Link link = LinkFactory.createLink(name, toolbarVC, this);
		addToolbarComponentToMapAndList(link, owner, pos);
		return link;
	}

	public Link addToolbarLink(String name, String displayText, Object owner, Position pos) {
		Link link = LinkFactory.createLink(name, toolbarVC, this);
		link.setCustomDisplayText(displayText);
		link.setTooltip(name);
		addToolbarComponentToMapAndList(link, owner, pos);
		return link;
	}

	public Link addToolbarButton(String name, Object owner, Position pos) {
		Link link = LinkFactory.createButton(name, toolbarVC, this);
		addToolbarComponentToMapAndList(link, owner, pos);
		return link;
	}

	public Link addToolbarButton(String name, String displayText, Object owner, Position pos) {
		Link link = LinkFactory.createButton(name, toolbarVC, this);
		link.setCustomDisplayText(displayText);
		link.setTooltip(name);
		addToolbarComponentToMapAndList(link, owner, pos);
		return link;
	}

	public void removeToolbarItem(String name) {
		removeToolbarItem(name, leftComponents);
		removeToolbarItem(name, centeredComponents);
		removeToolbarItem(name, rightComponents);
	}

	private void removeToolbarItem(String name, List<Component> cmps) {
		for (Component component : cmps) {
			if (name.equals(component.getComponentName())) {
				cmps.remove(component);
				ownershipMap.remove(component);
				toolbarVC.remove(component);
				return;
			}
		}
	}

	public void removeAllToolbarItemOf(Object owner) {
		removeAllToolbarItemOf(owner, leftComponents);
		removeAllToolbarItemOf(owner, centeredComponents);
		removeAllToolbarItemOf(owner, rightComponents);

	}

	private void removeAllToolbarItemOf(Object owner, List<Component> cmps) {
		for (Iterator<Component> linkIt = cmps.iterator(); linkIt.hasNext();) {
			Component component = linkIt.next();
			if (owner.equals(ownershipMap.get(component))) {
				linkIt.remove();
				toolbarVC.remove(component);
				ownershipMap.remove(component);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (leftComponents.contains(source) || rightComponents.contains(source) || centeredComponents.contains(source)) {
			fireEvent(ureq, event);
		}
	}

	public enum Position {
		left, center, right,
	}
}
