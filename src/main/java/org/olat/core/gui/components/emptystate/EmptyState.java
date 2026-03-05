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

import java.util.ArrayList;
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
	public static final int MAX_SECONDARY_BUTTONS = 3;
	
	public class PrimaryEvent extends Event {
		public PrimaryEvent() {
			super("empty-state-primary");
		}
	}

	public class SecondaryEvent extends Event {
		private final String action;

		public SecondaryEvent(String action) {
			super("empty-state-secondary-" + action);
			this.action = action;
		}

		public String getAction() {
			return action;
		}
	}
	
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
	private String helpTranslated;
	private String helpPage;
	private final Link primaryButtonLink;
	private final List<Link> secondaryButtonLinks = new ArrayList<>();
	private EmptyStateButton primaryButton;
	private List<EmptyStateButton> secondaryButtons;

	EmptyState(String name) {
		super(name);
		
		primaryButtonLink = LinkFactory.createCustomLink("emptystate_" + CodeHelper.getRAMUniqueID(), 
				"empty.state.primary", "", Link.BUTTON, null, this);
		primaryButtonLink.setDomReplacementWrapperRequired(false);
		primaryButtonLink.setPrimary(true);
		
		for (int i = 0; i < MAX_SECONDARY_BUTTONS; i++) {
			Link secondaryButtonLink = 
					LinkFactory.createCustomLink("secondaryemptystate_" + CodeHelper.getRAMUniqueID(), 
							"secondary.empty.state." + i, "", Link.BUTTON, null, this);
			secondaryButtonLink.setDomReplacementWrapperRequired(false);
			secondaryButtonLinks.add(secondaryButtonLink);
		}
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
	
	public String getHelpTranslated() {
		return helpTranslated;
	}

	public void setHelpTranslated(String helpTranslated) {
		this.helpTranslated = helpTranslated;
	}
	
	public String getHelpPage() {
		return helpPage;
	}

	public void setHelpPage(String helpPage) {
		this.helpPage = helpPage;
	}

	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}

	@Override
	public FormItem getFormItem() {
		return null;
	}

	public Link getPrimaryButtonLink() {
		return primaryButtonLink;
	}

	public List<Link> getSecondaryButtonLinks() {
		return secondaryButtonLinks;
	}

	@Override
	public Component getComponent(String name) {
		if (name.equals(primaryButtonLink.getComponentName())) {
			return primaryButtonLink;
		}
		for (Link link : secondaryButtonLinks) {
			if (name.equals(link.getComponentName())) {
				return link;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		ArrayList<Component> components = new ArrayList<>();
		components.add(primaryButtonLink);
		components.addAll(secondaryButtonLinks);
		return components;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source == primaryButtonLink) {
			fireEvent(ureq, new PrimaryEvent());
		} else if (source instanceof Link link) {
			int index = secondaryButtonLinks.indexOf(link);
			if (index >= 0) {
				fireEvent(ureq, new SecondaryEvent(secondaryButtons.get(index).action()));
			}
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

	public void setPrimaryButton(EmptyStateButton primaryButton) {
		this.primaryButton = primaryButton;
		setDirty(true);
	}
	
	public void setPrimaryButton(String iconCss, String i18nKey, String translated) {
		this.primaryButton = new EmptyStateButton(iconCss, i18nKey, translated, "primary");
		setDirty(true);
	}

	public EmptyStateButton getPrimaryButton() {
		return primaryButton;
	}

	public void setSecondaryButtons(List<EmptyStateButton> secondaryButtons) {
		this.secondaryButtons = secondaryButtons;
		setDirty(true);
	}

	public List<EmptyStateButton> getSecondaryButtons() {
		return secondaryButtons;
	}
}
