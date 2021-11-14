/**
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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.ForumModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FOConfigController extends FormBasicController {
	
	private static final String[] allowKeys = new String[] { "on" };
	
	private MultipleSelectionElement allowPseudonymEl;
	private MultipleSelectionElement pseudonymAsDefaultEl;
	private MultipleSelectionElement allowGuestEl;

	private final FOCourseNode foNode;
	private final ModuleConfiguration moduleConfig;
	
	@Autowired
	private ForumModule forumModule;
	
	public FOConfigController(UserRequest ureq, WindowControl wControl, FOCourseNode foNode) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.foNode = foNode;
		this.moduleConfig = foNode.getModuleConfiguration();
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_forum_settings");
		
		if(forumModule.isAnonymousPostingWithPseudonymEnabled()) {
			FormLayoutContainer anonymousCont = FormLayoutContainer.createDefaultFormLayout("anonymous", getTranslator());
			formLayout.add(anonymousCont);
			anonymousCont.setFormTitle(translate("anonymous.title"));
			anonymousCont.setFormContextHelp("Communication and Collaboration#_forumkonfig");

			allowPseudonymEl = uifactory.addCheckboxesHorizontal("allow.pseudonym.post", anonymousCont, allowKeys,
					translateAll(getTranslator(), allowKeys));
			allowPseudonymEl.setElementCssClass("o_sel_course_forum_allow_pseudo");
			allowPseudonymEl.addActionListener(FormEvent.ONCHANGE);
			if ("true".equals(moduleConfig.getStringValue(FOCourseNode.CONFIG_PSEUDONYM_POST_ALLOWED))) {
				allowPseudonymEl.select(allowKeys[0], true);
			}
			
			pseudonymAsDefaultEl = uifactory.addCheckboxesHorizontal("pseudonym.default", anonymousCont, allowKeys,
					translateAll(getTranslator(), allowKeys));
			pseudonymAsDefaultEl.setElementCssClass("o_sel_course_forum_pseudo_default");
			pseudonymAsDefaultEl.addActionListener(FormEvent.ONCHANGE);

			boolean defaultPseudonym = moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_PSEUDONYM_POST_DEFAULT,
					forumModule.isPseudonymForMessageEnabledByDefault());
			if (defaultPseudonym) {
				pseudonymAsDefaultEl.select(allowKeys[0], true);
			}
		}
		
		if (foNode.hasCustomPreConditions()) {
			FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
			formLayout.add(rightsCont);
			rightsCont.setFormTitle(translate("user.rights"));
			if(!forumModule.isAnonymousPostingWithPseudonymEnabled()) {
				rightsCont.setFormContextHelp("Communication and Collaboration#_forumkonfig");
			}
		
			allowGuestEl = uifactory.addCheckboxesHorizontal("allow.guest.post", rightsCont, allowKeys,
					translateAll(getTranslator(), allowKeys));
			allowGuestEl.setElementCssClass("o_sel_course_forum_allow_guest");
			allowGuestEl.addActionListener(FormEvent.ONCHANGE);
			if ("true".equals(moduleConfig.getStringValue(FOCourseNode.CONFIG_GUEST_POST_ALLOWED))) {
				allowGuestEl.select(allowKeys[0], true);
			}
		}
	}
	
	private void updateUI() {
		if(pseudonymAsDefaultEl != null && allowPseudonymEl != null) {
			boolean isPseudonymPostAllowed = allowPseudonymEl.isAtLeastSelected(1);
			pseudonymAsDefaultEl.setVisible(isPseudonymPostAllowed);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allowPseudonymEl == source || pseudonymAsDefaultEl == source) {
			doUpdatePseudonym(ureq);
			updateUI();
		} else if(allowGuestEl == source) {
			doUpdatePosterGuest(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doUpdatePseudonym(UserRequest ureq) {
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_PSEUDONYM_POST_ALLOWED, allowPseudonymEl.isAtLeastSelected(1));
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_PSEUDONYM_POST_DEFAULT, pseudonymAsDefaultEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdatePosterGuest(UserRequest ureq) {
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_GUEST_POST_ALLOWED, allowGuestEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}