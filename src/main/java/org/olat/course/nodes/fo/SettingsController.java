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
package org.olat.course.nodes.fo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.fo.ForumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SettingsController extends FormBasicController {
	
	private static final String[] allowKeys = new String[] { "on" };
	
	private MultipleSelectionElement allowPseudonymEl, pseudonymAsDefaultEl, allowGuestEl;

	private final FOCourseNode foNode;
	
	@Autowired
	private ForumModule forumModule;
	
	public SettingsController(UserRequest ureq, WindowControl wControl, FOCourseNode foNode) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.foNode = foNode;
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.title");
		setFormContextHelp("Communication and Collaboration#_forumkonfig");
		formLayout.setElementCssClass("o_sel_course_forum_settings");
		
		if(forumModule.isAnonymousPostingWithPseudonymEnabled()) {
			String[] allowPseudonymValues = new String[] { translate("allow.pseudonym.post") };
			allowPseudonymEl = uifactory.addCheckboxesHorizontal("allow.pseudonym", null, formLayout,
					allowKeys, allowPseudonymValues);
			allowPseudonymEl.setElementCssClass("o_sel_course_forum_allow_pseudo");
			allowPseudonymEl.setLabel(null, null);
			allowPseudonymEl.addActionListener(FormEvent.ONCHANGE);
			
			if("true".equals(foNode.getModuleConfiguration().getStringValue(FOCourseNode.CONFIG_PSEUDONYM_POST_ALLOWED))) {
				allowPseudonymEl.select(allowKeys[0], true);
			}

			String[] defaultPseudonymValues = new String[] { translate("pseudonym.default") };
			pseudonymAsDefaultEl = uifactory.addCheckboxesHorizontal("pseudonym.default", null, formLayout,
					allowKeys, defaultPseudonymValues);
			pseudonymAsDefaultEl.setElementCssClass("o_sel_course_forum_pseudo_default");
			pseudonymAsDefaultEl.setLabel(null, null);
			pseudonymAsDefaultEl.addActionListener(FormEvent.ONCHANGE);
			
			boolean defaultPseudonym = foNode.getModuleConfiguration().getBooleanSafe(FOCourseNode.CONFIG_PSEUDONYM_POST_DEFAULT,
					forumModule.isPseudonymForMessageEnabledByDefault());
			if(defaultPseudonym) {
				pseudonymAsDefaultEl.select(allowKeys[0], true);
			}
		}
		
		String[] allowGuestValues = new String[] { translate("allow.guest.post") };
		allowGuestEl = uifactory.addCheckboxesHorizontal("allow.guest", null, formLayout,
				allowKeys, allowGuestValues);
		allowGuestEl.setElementCssClass("o_sel_course_forum_allow_guest");
		allowGuestEl.setLabel(null, null);
		allowGuestEl.addActionListener(FormEvent.ONCHANGE);
		if("true".equals(foNode.getModuleConfiguration().getStringValue(FOCourseNode.CONFIG_GUEST_POST_ALLOWED))) {
			allowGuestEl.select(allowKeys[0], true);
		}
	}
	
	private void updateUI() {
		if(pseudonymAsDefaultEl != null && allowPseudonymEl != null) {
			pseudonymAsDefaultEl.setVisible(isPseudonymPostAllowed());
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public boolean isPseudonymPostAllowed() {
		return allowPseudonymEl == null ? false : allowPseudonymEl.isAtLeastSelected(1);
	}
	
	public boolean isDefaultPseudonym() {
		return pseudonymAsDefaultEl == null ? false : pseudonymAsDefaultEl.isAtLeastSelected(1);
	}
	
	public boolean isGuestPostAllowed() {
		return allowGuestEl.isAtLeastSelected(1);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allowPseudonymEl == source || pseudonymAsDefaultEl == source) {
			fireEvent(ureq, Event.CHANGED_EVENT);
			updateUI();
		} else if(allowGuestEl == source) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}