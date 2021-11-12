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
package org.olat.core.gui.components.emptystate;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EmptyState extends AbstractComponent implements ComponentCollection, ComponentEventListener {
	
	public static Event EVENT = new Event("empty-state");
	
	private static final ComponentRenderer RENDERER = new EmptyStateRenderer();
	
	private String iconCss;
	private String indicatorIconCss;
	private String messageI18nKey;
	private String[] messageI18nArgs;
	private String hintI18nKey;
	private String[] hintI18nArgs;
	private String buttonI18nKey;
	private Link button;

	EmptyState(String name) {
		super(name);
		
		button = LinkFactory.createCustomLink("emptystate_" + CodeHelper.getRAMUniqueID(), "empty.state", "",
				Link.BUTTON, null, this);
		button.setDomReplacementWrapperRequired(false);
		button.setPrimary(true);
	}

	public String getIconCss() {
		return iconCss;
	}

	public void setIconCss(String iconCss) {
		this.iconCss = iconCss;
		setDirty(true);
	}

	public String getIndicatorIconCss() {
		return indicatorIconCss;
	}

	public void setIndicatorIconCss(String indicatorIconCss) {
		this.indicatorIconCss = indicatorIconCss;
	}

	public String getMessageI18nKey() {
		return messageI18nKey;
	}

	public void setMessageI18nKey(String messageI18nKey) {
		this.messageI18nKey = messageI18nKey;
		setDirty(true);
	}

	public String[] getMessageI18nArgs() {
		return messageI18nArgs;
	}

	public void setMessageI18nArgs(String[] messageI18nArgs) {
		this.messageI18nArgs = messageI18nArgs;
	}

	public String getHintI18nKey() {
		return hintI18nKey;
	}

	public void setHintI18nKey(String hintI18nKey) {
		this.hintI18nKey = hintI18nKey;
		setDirty(true);
	}

	public String[] getHintI18nArgs() {
		return hintI18nArgs;
	}

	public void setHintI18nArgs(String[] hintI18nArgs) {
		this.hintI18nArgs = hintI18nArgs;
	}

	public String getButtonI18nKey() {
		return buttonI18nKey;
	}

	public void setButtonI18nKey(String buttonI18nKey) {
		this.buttonI18nKey = buttonI18nKey;
		setDirty(true);
	}
	
	public Link getButton() {
		return button;
	}
	

	@Override
	public Component getComponent(String name) {
		if (name.equals(button.getComponentName())) {
			return button;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return Collections.singletonList(button);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == button) {
			fireEvent(ureq, EVENT);
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

}
