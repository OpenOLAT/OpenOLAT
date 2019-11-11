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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;

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
	private static final String MODERATOR_COACH = "edit.moderator.coach";
	private static final String[] MODERATOR_KEYS = new String[] { MODERATOR_COACH };
	private static final String POSTER_COACH = "edit.poster.coach";
	private static final String POSTER_PARTICIPANT = "edit.poster.participant";
	private static final String POSTER_GUEST = "edit.poster.guest";
	private static final String[] POSTER_KEYS = new String[] {
			POSTER_COACH,
			POSTER_PARTICIPANT,
			POSTER_GUEST
	};
	
	private MultipleSelectionElement allowPseudonymEl;
	private MultipleSelectionElement pseudonymAsDefaultEl;
	private MultipleSelectionElement allowGuestEl;
	private MultipleSelectionElement moderatorRolesEl;
	private MultipleSelectionElement posterRolesEl;

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
		setFormContextHelp("Communication and Collaboration#_forumkonfig");
		formLayout.setElementCssClass("o_sel_course_forum_settings");
		
		if(forumModule.isAnonymousPostingWithPseudonymEnabled()) {
			FormLayoutContainer anonymousCont = FormLayoutContainer.createDefaultFormLayout("anonymous", getTranslator());
			formLayout.add(anonymousCont);
			anonymousCont.setFormTitle(translate("anonymous.title"));

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
		
		FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
		formLayout.add(rightsCont);
		rightsCont.setFormTitle(translate("user.rights"));
		
		if (foNode.hasCustomPreConditions()) {
			allowGuestEl = uifactory.addCheckboxesHorizontal("allow.guest.post", rightsCont, allowKeys,
					translateAll(getTranslator(), allowKeys));
			allowGuestEl.setElementCssClass("o_sel_course_forum_allow_guest");
			allowGuestEl.addActionListener(FormEvent.ONCHANGE);
			if ("true".equals(moduleConfig.getStringValue(FOCourseNode.CONFIG_GUEST_POST_ALLOWED))) {
				allowGuestEl.select(allowKeys[0], true);
			}
		} else {
			moderatorRolesEl = uifactory.addCheckboxesVertical("edit.moderator", rightsCont, MODERATOR_KEYS,
					translateAll(getTranslator(), MODERATOR_KEYS), 1);
			moderatorRolesEl.select(MODERATOR_COACH, moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_COACH_MODERATE_ALLOWED));
			moderatorRolesEl.addActionListener(FormEvent.ONCHANGE);
			
			posterRolesEl = uifactory.addCheckboxesVertical("edit.poster", rightsCont, POSTER_KEYS,
					translateAll(getTranslator(), POSTER_KEYS), 1);
			posterRolesEl.setElementCssClass("o_sel_course_forum_poster");
			posterRolesEl.select(POSTER_COACH, moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_COACH_POST_ALLOWED));
			posterRolesEl.select(POSTER_PARTICIPANT,
					moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_PARTICIPANT_POST_ALLOWED));
			posterRolesEl.select(POSTER_GUEST, moduleConfig.getBooleanSafe(FOCourseNode.CONFIG_GUEST_POST_ALLOWED));
			posterRolesEl.addActionListener(FormEvent.ONCHANGE);
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
		} else if (source == moderatorRolesEl) {
			doUpdateModeratorRoles(ureq);
		} else if (source == posterRolesEl) {
			doUpdatePosterRoles(ureq);
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

	private void doUpdateModeratorRoles(UserRequest ureq) {
		Collection<String> selectedKeys = moderatorRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_COACH_MODERATE_ALLOWED, selectedKeys.contains(MODERATOR_COACH));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdatePosterRoles(UserRequest ureq) {
		Collection<String> selectedKeys = posterRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_COACH_POST_ALLOWED, selectedKeys.contains(POSTER_COACH));
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_PARTICIPANT_POST_ALLOWED, selectedKeys.contains(POSTER_PARTICIPANT));
		moduleConfig.setBooleanEntry(FOCourseNode.CONFIG_GUEST_POST_ALLOWED, selectedKeys.contains(POSTER_GUEST));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void doDispose() {
		//
	}
}