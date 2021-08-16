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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.AssessableResource;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessedIdentityListController extends AssessedIdentityListController {
	
	private FormLink pullButton;
	private FormLink resetButton;
	
	private CloseableModalController cmc;
	private QTI21ResetDataController resetDataCtrl;
	private QTI21RetrieveTestsController pullSessionCtrl;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21AssessedIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry testEntry, AssessableResource element, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, stackPanel, testEntry, element, assessmentCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		resetButton = uifactory.addFormLink("reset.test.data.title", formLayout, Link.BUTTON);
		resetButton.setIconLeftCSS("o_icon o_icon_delete_item");
		
		pullButton = uifactory.addFormLink("menu.retrieve.tests.title", formLayout, Link.BUTTON);
		pullButton.setIconLeftCSS("o_icon o_icon_pull");
	}
	
	@Override
	protected void updateTools(List<Identity> assessedIdentities) {
		RepositoryEntry assessedEntry = getRepositoryEntry();
		boolean enabled = false;
		if(assessedIdentities != null && !assessedIdentities.isEmpty()) {
			enabled = qtiService.isRunningAssessmentTestSession(assessedEntry, null, assessedEntry, assessedIdentities);
		}
		pullButton.setEnabled(enabled);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(resetDataCtrl == source || pullSessionCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(resetDataCtrl);
		removeAsListenerAndDispose(cmc);
		resetDataCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(resetButton == source) {
			doResetData(ureq);
		} else if(pullButton == source) {
			doPullSessions(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doResetData(UserRequest ureq) {
		if(guardModalController(resetDataCtrl)) return;
	
		AssessmentToolOptions asOptions = getOptions();
		resetDataCtrl = new QTI21ResetDataController(ureq, getWindowControl(), this.getRepositoryEntry(), asOptions);
		listenTo(resetDataCtrl);
		
		String title = translate("reset.test.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPullSessions(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		pullSessionCtrl = new QTI21RetrieveTestsController(ureq, getWindowControl(), getRepositoryEntry(), asOptions);
		listenTo(pullSessionCtrl);
		
		String title = translate("retrievetest.confirm.title");
		cmc = new CloseableModalController(getWindowControl(), null, pullSessionCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private AssessmentToolOptions getOptions() {
		AssessmentToolOptions asOptions = new AssessmentToolOptions();
		asOptions.setAdmin(assessmentCallback.isAdmin());
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), getSearchParameters());
		asOptions.setIdentities(assessedIdentities);
		return asOptions;
	}
}
