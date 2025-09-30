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
package org.olat.core.gui.components.expand;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: Sep 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExpandButton extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new ExpandButtonRenderer();
	
	public static final String ARIA_HASPOPUP_DIALOG = "dialog";
	static final String CMD_TOGGLE = "toggle";
	
	private final FormItem element;
	
	private String text;
	private EscapeMode escapeMode = EscapeMode.html;
	private String title;
	private String cssClass;
	private String iconLeftExpandedCss;
	private String iconLeftCollapsedCss;
	private String iconRightExpandedCss;
	private String iconRightCollapsedCss;
	private String ariaLabel;
	private String ariaHasPopup;
	private String ariaControls;
	private boolean disabledAsText;
	
	private boolean expanded = false;
	
	public ExpandButton() {
		this(null);
	}

	public ExpandButton(FormItem element) {
		super(element.getFormItemId(), element.getName());
		this.element = element;
		
		setSpanAsDomReplaceable(true);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public FormItem getFormItem() {
		return element;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if(CMD_TOGGLE.equals(cmd)) {
			setExpanded(!isExpanded());
			fireEvent(ureq, new Event(cmd));
		}
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		setDirty(true);
	}

	public EscapeMode getEscapeMode() {
		return escapeMode;
	}

	public void setEscapeMode(EscapeMode escapeMode) {
		this.escapeMode = escapeMode;
		setDirty(true);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		setDirty(true);
	}

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
		setDirty(true);
	}

	public String getIconLeftExpandedCss() {
		return iconLeftExpandedCss;
	}

	public void setIconLeftExpandedCss(String iconLeftExpandedCss) {
		this.iconLeftExpandedCss = iconLeftExpandedCss;
		setDirty(true);
	}

	public String getIconLeftCollapsedCss() {
		return iconLeftCollapsedCss;
	}

	public void setIconLeftCollapsedCss(String iconLeftCollapsedCss) {
		this.iconLeftCollapsedCss = iconLeftCollapsedCss;
		setDirty(true);
	}

	public String getIconRightExpandedCss() {
		return iconRightExpandedCss;
	}

	public void setIconRightExpandedCss(String iconRightExpandedCss) {
		this.iconRightExpandedCss = iconRightExpandedCss;
		setDirty(true);
	}

	public String getIconRightCollapsedCss() {
		return iconRightCollapsedCss;
	}

	public void setIconRightCollapsedCss(String iconRightCollapsedCss) {
		this.iconRightCollapsedCss = iconRightCollapsedCss;
		setDirty(true);
	}

	public String getAriaLabel() {
		return ariaLabel;
	}

	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
		setDirty(true);
	}

	public String getAriaHasPopup() {
		return ariaHasPopup;
	}

	public void setAriaHasPopup(String ariaHasPopup) {
		this.ariaHasPopup = ariaHasPopup;
		setDirty(true);
	}

	public String getAriaControls() {
		return ariaControls;
	}

	public void setAriaControls(String ariaControls) {
		this.ariaControls = ariaControls;
		setDirty(true);
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
		setDirty(true);
	}

	public boolean isDisabledAsText() {
		return disabledAsText;
	}

	public void setDisabledAsText(boolean disabledAsText) {
		this.disabledAsText = disabledAsText;
		setDirty(true);
	}

}
