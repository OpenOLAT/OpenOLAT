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
package org.olat.core.commons.services.webdav.ui;

import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{"xx"};
	
	private TextElement excludeUserAgentsClientsEl;
	private MultipleSelectionElement excludeClientsEl;
	private MultipleSelectionElement enableModuleEl;
	private MultipleSelectionElement enableLinkEl;
	private MultipleSelectionElement enableDigestEl;
	private MultipleSelectionElement enableTermsFoldersEl;
	private MultipleSelectionElement enableManagedFoldersEl;
	private MultipleSelectionElement enableCurriculumElementFoldersEl;
	private MultipleSelectionElement learnersAsParticipantEl;
	private MultipleSelectionElement learnersBookmarkEl;
	private MultipleSelectionElement prependReferenceEl;
	private SpacerElement spacer1;
	private SpacerElement spacer2;
	

	@Autowired
	private WebDAVModule webDAVModule;
	
	public WebDAVAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
		updateEnabledDisabled();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("admin.menu.title.alt");
		setFormDescription("admin.webdav.description");
		setFormContextHelp("WebDAV");

		boolean enabled = webDAVModule.isEnabled();
		String[] values = new String[] { getTranslator().translate("webdav.on") };
		enableModuleEl = uifactory.addCheckboxesHorizontal("webdavModule", "webdav.module", formLayout, onKeys, values);
		enableModuleEl.addActionListener(FormEvent.ONCHANGE);
		enableModuleEl.select("xx", enabled);
		
		enableLinkEl = uifactory.addCheckboxesHorizontal("webdavLink", "webdav.link", formLayout, onKeys, values);
		enableLinkEl.select("xx", webDAVModule.isLinkEnabled());
		
		enableDigestEl = uifactory.addCheckboxesHorizontal("webdavDigest", "webdav.digest", formLayout, onKeys, values);
		enableDigestEl.select("xx", webDAVModule.isDigestAuthenticationEnabled());

		String excludedUserAgents = webDAVModule.getUserAgentExclusionList();
		excludeClientsEl = uifactory.addCheckboxesHorizontal("webdavExclusion", "webdav.client.exclusion", formLayout, onKeys, values);
		excludeClientsEl.select("xx", StringHelper.containsNonWhitespace(excludedUserAgents));
		excludeClientsEl.addActionListener(FormEvent.ONCHANGE);
		
		excludeUserAgentsClientsEl = uifactory.addTextElement("webdav.user.agent.exclusion", 4096, excludedUserAgents, formLayout);
		excludeUserAgentsClientsEl.setVisible(excludeClientsEl.isAtLeastSelected(1));
		
		spacer1 = uifactory.addSpacerElement("spacer1", formLayout, false);
		
		enableTermsFoldersEl = uifactory.addCheckboxesHorizontal("webdavTermsFolders", "webdav.termsfolders", formLayout, onKeys, values);
		enableTermsFoldersEl.select("xx", webDAVModule.isTermsFoldersEnabled());
		
		enableCurriculumElementFoldersEl = uifactory.addCheckboxesHorizontal("webdavCurriculumsElementsFolders", "webdav.curriculumelementsfolders", formLayout, onKeys, values);
		enableCurriculumElementFoldersEl.select("xx", webDAVModule.isCurriculumElementFoldersEnabled());
		
		enableManagedFoldersEl = uifactory.addCheckboxesHorizontal("webdavManagedFolders", "webdav.managedfolders", formLayout, onKeys, values);
		enableManagedFoldersEl.select("xx", webDAVModule.isManagedFoldersEnabled());
		
		prependReferenceEl = uifactory.addCheckboxesHorizontal("webdavPrepend", "webdav.prepend.reference", formLayout, onKeys, values);
		prependReferenceEl.select("xx", webDAVModule.isPrependCourseReferenceToTitle());
		
		spacer2 = uifactory.addSpacerElement("spacer2", formLayout, false);
		
		learnersAsParticipantEl = uifactory.addCheckboxesHorizontal("learnersParticipants", "webdav.for.learners.participants", formLayout, onKeys, values);
		learnersAsParticipantEl.select("xx", webDAVModule.isEnableLearnersParticipatingCourses());
		
		learnersBookmarkEl = uifactory.addCheckboxesHorizontal("learnerBookmarks", "webdav.for.learners.bookmarks", formLayout, onKeys, values);
		learnersBookmarkEl.select("xx", webDAVModule.isEnableLearnersBookmarksCourse());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableModuleEl) {
			boolean enabled = enableModuleEl.isAtLeastSelected(1);
			webDAVModule.setEnabled(enabled);
			updateEnabledDisabled();
		} else if(source == excludeClientsEl) {
			excludeUserAgentsClientsEl.setVisible(excludeClientsEl.isAtLeastSelected(1));
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateEnabledDisabled() {
		boolean enabled = enableModuleEl.isAtLeastSelected(1);
		
		enableLinkEl.setVisible(enabled);
		enableDigestEl.setVisible(enabled);
		enableTermsFoldersEl.setVisible(enabled);
		learnersAsParticipantEl.setVisible(enabled);
		learnersBookmarkEl.setVisible(enabled);
		enableCurriculumElementFoldersEl.setVisible(enabled);
		enableManagedFoldersEl.setVisible(enabled);
		prependReferenceEl.setVisible(enabled);
		excludeClientsEl.setVisible(enabled);
		excludeUserAgentsClientsEl.setVisible(enabled && excludeClientsEl.isAtLeastSelected(1));
		spacer1.setVisible(enabled);
		spacer2.setVisible(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		webDAVModule.setLinkEnabled(enableLinkEl.isAtLeastSelected(1));
		webDAVModule.setDigestAuthenticationEnabled(enableDigestEl.isAtLeastSelected(1));
		webDAVModule.setTermsFoldersEnabled(enableTermsFoldersEl.isAtLeastSelected(1));
		webDAVModule.setCurriculumElementFoldersEnabled(enableCurriculumElementFoldersEl.isAtLeastSelected(1));
		webDAVModule.setEnableLearnersParticipatingCourses(learnersAsParticipantEl.isAtLeastSelected(1));
		webDAVModule.setEnableLearnersBookmarksCourse(learnersBookmarkEl.isAtLeastSelected(1));
		webDAVModule.setPrependCourseReferenceToTitle(prependReferenceEl.isAtLeastSelected(1));
		webDAVModule.setManagedFoldersEnabled(enableManagedFoldersEl.isAtLeastSelected(1));
		if(excludeClientsEl.isAtLeastSelected(1)) {
			webDAVModule.setUserAgentExclusionList(excludeUserAgentsClientsEl.getValue());
		} else {
			webDAVModule.setUserAgentExclusionList(null);
		}
	}
}
