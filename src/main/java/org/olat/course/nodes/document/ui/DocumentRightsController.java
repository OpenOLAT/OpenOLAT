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
package org.olat.course.nodes.document.ui;

import java.util.Collection;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.DocumentCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentRightsController extends FormBasicController {
	

	private static final String[] EDIT_KEYS = new String[] {
			DocumentCourseNode.CONFIG_KEY_EDIT_OWNER,
			DocumentCourseNode.CONFIG_KEY_EDIT_COACH,
			DocumentCourseNode.CONFIG_KEY_EDIT_PARTICIPANT,
			DocumentCourseNode.CONFIG_KEY_EDIT_GUEST
	};
	private static final String[] DOWNLOAD_KEYS = new String[] {
			DocumentCourseNode.CONFIG_KEY_DOWNLOAD_OWNER,
			DocumentCourseNode.CONFIG_KEY_DOWNLOAD_COACH,
			DocumentCourseNode.CONFIG_KEY_DOWNLOAD_PARTICIPANT,
			DocumentCourseNode.CONFIG_KEY_DOWNLOAD_GUEST
	};
	private static final String[] ROLE_I18N_KEYS = new String[] {
			"config.role.owner",
			"config.role.coach",
			"config.role.participant",
			"config.role.guest"
	};

	private MultipleSelectionElement editRolesEl;
	private MultipleSelectionElement downloadRolesEl;

	private final ModuleConfiguration config;
	
	@Autowired
	private DocEditorService documentEditorService;

	public DocumentRightsController(UserRequest ureq, WindowControl wControl, DocumentCourseNode courseNode) {
		super(ureq, wControl);
		config = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.rights.title");
		
		String[] roleValues = TranslatorHelper.translateAll(getTranslator(), ROLE_I18N_KEYS);
		
		editRolesEl = uifactory.addCheckboxesVertical("config.rights.edit", formLayout, EDIT_KEYS, roleValues, 1);
		editRolesEl.select(DocumentCourseNode.CONFIG_KEY_EDIT_OWNER, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_EDIT_OWNER));
		editRolesEl.select(DocumentCourseNode.CONFIG_KEY_EDIT_COACH, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_EDIT_COACH));
		editRolesEl.select(DocumentCourseNode.CONFIG_KEY_EDIT_PARTICIPANT, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_EDIT_PARTICIPANT));
		editRolesEl.select(DocumentCourseNode.CONFIG_KEY_EDIT_GUEST, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_EDIT_GUEST));
		editRolesEl.addActionListener(FormEvent.ONCHANGE);
		
		downloadRolesEl = uifactory.addCheckboxesVertical("config.rights.downdload", formLayout, DOWNLOAD_KEYS, roleValues, 1);
		downloadRolesEl.select(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_OWNER, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_OWNER));
		downloadRolesEl.select(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_COACH, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_COACH));
		downloadRolesEl.select(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_PARTICIPANT, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_PARTICIPANT));
		downloadRolesEl.select(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_GUEST, config.getBooleanSafe(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_GUEST));
		downloadRolesEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	public void setVfsLeaf(UserRequest ureq, VFSLeaf vfsLeaf) {
		boolean hasEditor = false;
		if (vfsLeaf != null) {
			String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
			hasEditor = documentEditorService.hasEditor(getIdentity(), ureq.getUserSession().getRoles(), suffix, Mode.EDIT, true, false);
		}
		editRolesEl.setVisible(hasEditor);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editRolesEl) {
			doUpdateExecutionRoles(ureq);
		} else if (source == downloadRolesEl) {
			doUpdateReportRoles(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doUpdateExecutionRoles(UserRequest ureq) {
		Collection<String> selectedKeys = editRolesEl.getSelectedKeys();
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_EDIT_OWNER, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_EDIT_OWNER));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_EDIT_COACH, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_EDIT_COACH));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_EDIT_PARTICIPANT, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_EDIT_PARTICIPANT));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_EDIT_GUEST, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_EDIT_GUEST));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUpdateReportRoles(UserRequest ureq) {
		Collection<String> selectedKeys = downloadRolesEl.getSelectedKeys();
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_OWNER, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_OWNER));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_COACH, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_COACH));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_PARTICIPANT, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_PARTICIPANT));
		config.setBooleanEntry(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_GUEST, selectedKeys.contains(DocumentCourseNode.CONFIG_KEY_DOWNLOAD_GUEST));
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
