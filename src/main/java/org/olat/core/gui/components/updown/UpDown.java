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
package org.olat.core.gui.components.updown;

import java.util.Arrays;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.updown.UpDownEvent.Direction;
import org.olat.core.gui.control.Event;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 7 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UpDown extends AbstractComponent implements ComponentCollection, ComponentEventListener {
	
	private static final ComponentRenderer RENDERER = new UpDownRenderer();
	
	private final Link up;
	private final Link down;

	private final long idPrefix;
	private final String downName;
	private final String upName;
	private boolean topmost = false;
	private boolean loweremost = false;
	private Object userObject;

	public UpDown(String name) {
		super(name);
		setDomReplacementWrapperRequired(false);
		idPrefix = CodeHelper.getRAMUniqueID();
		
		upName = idPrefix + "_up";
		this.up = LinkFactory.createCustomLink(upName, "up", "", Link.LINK | Link.NONTRANSLATED, null, this);
		up.setDomReplacementWrapperRequired(false);
		up.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		
		downName = idPrefix + "_down";
		this.down = LinkFactory.createCustomLink(downName, "down", "", Link.LINK | Link.NONTRANSLATED, null, this);
		down.setDomReplacementWrapperRequired(false);
		down.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
	}

	Link getUp() {
		return up;
	}

	Link getDown() {
		return down;
	}
	
	public void setTopmost(boolean topmost) {
		this.topmost = topmost;
	}
	
	boolean isTopmost() {
		return topmost;
	}

	public void setLowermost(boolean lowermost) {
		this.loweremost = lowermost;
	}

	boolean isLowermost() {
		return loweremost;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	public Component getComponent(String name) {
		if (upName.equals(name)) {
			return up;
		} else if (downName.equals(name)) {
			return down;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return Arrays.asList(up, down);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == up) {
			fireEvent(ureq, new UpDownEvent(Direction.UP, userObject));
		} else if (source == down) {
			fireEvent(ureq, new UpDownEvent(Direction.DOWN, userObject));
		}
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
