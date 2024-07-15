/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.emptystate;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 22 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EmptyState extends AbstractComponent implements FormBaseComponent, ComponentCollection, ComponentEventListener {
	
	public static Event EVENT = new Event("empty-state");
	public static Event SECONDARY_EVENT = new Event("secondary-empty-state");
	
	private static final ComponentRenderer RENDERER = new EmptyStateRenderer();
	
	private String iconCss;
	private String indicatorIconCss;
	private String messageI18nKey;
	private String[] messageI18nArgs;
	private String messageTranslated;
	private String hintI18nKey;
	private String[] hintI18nArgs;
	private String hintTranslated;
	private String descI18nKey;
	private String[] descI18nArgs;
	private String descTranslated;
	private String buttonI18nKey;
	private String secondaryButtonI18nKey;
	private Link button;
	private Link secondaryButton;
	
	private final EmptyStateItem emptyStateItem;
	
	EmptyState(String name) {
		this(name, null);
		
		button = LinkFactory.createCustomLink("emptystate_" + CodeHelper.getRAMUniqueID(), "empty.state", "",
				Link.BUTTON, null, this);
		button.setDomReplacementWrapperRequired(false);
		button.setPrimary(true);
		
		secondaryButton= LinkFactory.createCustomLink("secondaryemptystate_" + CodeHelper.getRAMUniqueID(), "secondary.empty.state", "",
				Link.BUTTON, null, this);
		secondaryButton.setDomReplacementWrapperRequired(false);
	}

	EmptyState(String name, EmptyStateItem emptyStateItem) {
		super(name);
		this.emptyStateItem = emptyStateItem;
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

	public String getMessageTranslated() {
		return messageTranslated;
	}

	public void setMessageTranslated(String messageTranslated) {
		this.messageTranslated = messageTranslated;
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

	public String getHintTranslated() {
		return hintTranslated;
	}

	public void setHintTranslated(String hintTranslated) {
		this.hintTranslated = hintTranslated;
	}

	public String getDescI18nKey() {
		return descI18nKey;
	}

	public void setDescI18nKey(String descI18nKey) {
		this.descI18nKey = descI18nKey;
	}

	public String[] getDescI18nArgs() {
		return descI18nArgs;
	}

	public void setDescI18nArgs(String[] descI18nArgs) {
		this.descI18nArgs = descI18nArgs;
	}

	public String getDescTranslated() {
		return descTranslated;
	}

	public void setDescTranslated(String descTranslated) {
		this.descTranslated = descTranslated;
	}

	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}

	@Override
	public FormItem getFormItem() {
		return emptyStateItem;
	}

	public String getButtonI18nKey() {
		return buttonI18nKey;
	}

	public void setButtonI18nKey(String buttonI18nKey) {
		this.buttonI18nKey = buttonI18nKey;
		setDirty(true);
	}

	public void setButtonLeftIconCss(String buttonLeftIconCss) {
		this.button.setIconLeftCSS(buttonLeftIconCss);
	}

	public String getSecondaryButtonI18nKey() {
		return secondaryButtonI18nKey;
	}

	public void setSecondaryButtonI18nKey(String secondaryButtonI18nKey) {
		this.secondaryButtonI18nKey = secondaryButtonI18nKey;
		setDirty(true);
	}
	
	public Link getButton() {
		return button;
	}

	public Link getSecondaryButton() {
		return secondaryButton;
	}

	@Override
	public Component getComponent(String name) {
		if (name.equals(button.getComponentName())) {
			return button;
		}
		if (name.equals(secondaryButton.getComponentName())) {
			return secondaryButton;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return List.of(button, secondaryButton);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == button) {
			fireEvent(ureq, EVENT);
		} else if (source == secondaryButton) {
			fireEvent(ureq, SECONDARY_EVENT);
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
