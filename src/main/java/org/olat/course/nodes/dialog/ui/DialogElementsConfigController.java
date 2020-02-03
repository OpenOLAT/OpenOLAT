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
package org.olat.course.nodes.dialog.ui;

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
import org.olat.course.nodes.DialogCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 31 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementsConfigController extends FormBasicController {
	
	private static final String ROLE_COACH = "edit.role.coach";
	private static final String ROLE_PARTICIPANT = "edit.role.participant";
	private static final String[] UPLOAD_KEYS = new String[] {
			ROLE_COACH,
			ROLE_PARTICIPANT
	};
	private static final String[] MODERATOR_KEYS = new String[] {
			ROLE_COACH
	};
	private static final String[] POSTER_KEYS = new String[] {
			ROLE_COACH,
			ROLE_PARTICIPANT
	};
	
	private MultipleSelectionElement uploadRolesEl;
	private MultipleSelectionElement moderatorRolesEl;
	private MultipleSelectionElement posterRolesEl;
	
	private final DialogCourseNode node;
	private final ModuleConfiguration moduleConfig;

	public DialogElementsConfigController(UserRequest ureq, WindowControl wControl, DialogCourseNode node) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.node = node;
		moduleConfig = node.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (!node.hasCustomPreConditions()) {
			FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
			formLayout.add(rightsCont);
			rightsCont.setFormTitle(translate("edit.rights"));
			
			uploadRolesEl = uifactory.addCheckboxesVertical("edit.upload", rightsCont, UPLOAD_KEYS, translateAll(getTranslator(), UPLOAD_KEYS), 1);
			uploadRolesEl.select(ROLE_COACH, moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_COACH));
			uploadRolesEl.select(ROLE_PARTICIPANT, moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_PARTICIPANT));
			uploadRolesEl.addActionListener(FormEvent.ONCHANGE);
			
			moderatorRolesEl = uifactory.addCheckboxesVertical("edit.moderator", rightsCont, MODERATOR_KEYS,
					translateAll(getTranslator(), MODERATOR_KEYS), 1);
			moderatorRolesEl.select(ROLE_COACH, moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_MODERATE_BY_COACH));
			moderatorRolesEl.addActionListener(FormEvent.ONCHANGE);
			
			posterRolesEl = uifactory.addCheckboxesVertical("edit.poster", rightsCont, POSTER_KEYS,
					translateAll(getTranslator(), POSTER_KEYS), 1);
			posterRolesEl.setElementCssClass("o_sel_course_forum_poster");
			posterRolesEl.select(ROLE_COACH, moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_POST_BY_COACH));
			posterRolesEl.select(ROLE_PARTICIPANT,
					moduleConfig.getBooleanSafe(DialogCourseNode.CONFIG_KEY_POST_BY_PARTICIPANT));
			posterRolesEl.addActionListener(FormEvent.ONCHANGE);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadRolesEl) {
			doUpdateUploadRoles(ureq);
		} else if (source == moderatorRolesEl) {
			doUpdateModeratorRoles(ureq);
		} else if (source == posterRolesEl) {
			doUpdatePosterRoles(ureq);
		}
	}
	
	private void doUpdateUploadRoles(UserRequest ureq) {
		Collection<String> selectedKeys = uploadRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_COACH, selectedKeys.contains(ROLE_COACH));
		moduleConfig.setBooleanEntry(DialogCourseNode.CONFIG_KEY_UPLOAD_BY_PARTICIPANT, selectedKeys.contains(ROLE_PARTICIPANT));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdateModeratorRoles(UserRequest ureq) {
		Collection<String> selectedKeys = moderatorRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(DialogCourseNode.CONFIG_KEY_MODERATE_BY_COACH, selectedKeys.contains(ROLE_COACH));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdatePosterRoles(UserRequest ureq) {
		Collection<String> selectedKeys = posterRolesEl.getSelectedKeys();
		moduleConfig.setBooleanEntry(DialogCourseNode.CONFIG_KEY_POST_BY_COACH, selectedKeys.contains(ROLE_COACH));
		moduleConfig.setBooleanEntry(DialogCourseNode.CONFIG_KEY_POST_BY_PARTICIPANT, selectedKeys.contains(ROLE_PARTICIPANT));
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
