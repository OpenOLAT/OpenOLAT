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
package org.olat.group.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.progressbar.ProgressController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.main.DedupMembersConfirmationController;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupModuleAdminController extends FormBasicController implements ProgressDelegate {
	
	private FormLink dedupLink;
	private MultipleSelectionElement allowEl;

	private Panel mainPopPanel;
	private CloseableModalController cmc;
	private ProgressController progressCtrl;
	private DedupMembersConfirmationController dedupCtrl;
	
	private final BusinessGroupModule module;
	private final BusinessGroupService businessGroupService;
	private String[] onKeys = new String[]{"user","author"};
	
	public BusinessGroupModuleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "bg_admin");
		module = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer optionsContainer = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
		formLayout.add(optionsContainer);
		String[] values = new String[]{
				translate("user.allow.create"),
				translate("author.allow.create")
		};
		allowEl = uifactory.addCheckboxesVertical("module.admin.allow.create", optionsContainer, onKeys, values, null, 1);
		allowEl.select("user", module.isUserAllowedCreate());
		allowEl.select("author", module.isAuthorAllowedCreate());
		
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("module.buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("ok", "ok", formLayout);
		
		FormLayoutContainer dedupCont = FormLayoutContainer.createDefaultFormLayout("dedup", getTranslator());
		formLayout.add(dedupCont);
		dedupLink = uifactory.addFormLink("dedup.members", dedupCont, Link.BUTTON);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == dedupCtrl) {
			boolean coaches = dedupCtrl.isDedupCoaches();
			boolean participants = dedupCtrl.isDedupParticipants();
			if(event == Event.DONE_EVENT) {
				dedupMembers(ureq, coaches, participants);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(dedupCtrl);
		removeAsListenerAndDispose(progressCtrl);
		removeAsListenerAndDispose(cmc);
		progressCtrl = null;
		dedupCtrl = null;
		cmc = null;
	}

	@Override
	public void setActual(float value) {
		if(progressCtrl != null) {
			progressCtrl.setActual(value);
		}
	}

	@Override
	public void finished() {
		cmc.deactivate();
		cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == dedupLink) {
			doDedupMembers(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	protected void doDedupMembers(UserRequest ureq) {
		dedupCtrl = new DedupMembersConfirmationController(ureq, getWindowControl());
		listenTo(dedupCtrl);
		
		mainPopPanel = new Panel("dedup");
		mainPopPanel.setContent(dedupCtrl.getInitialComponent());
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainPopPanel, true, translate("dedup.members"), false);
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void dedupMembers(UserRequest ureq, final boolean coaches, final boolean participants) {
		progressCtrl = new ProgressController(ureq, getWindowControl());
		progressCtrl.setMessage(translate("dedup.running"));
		mainPopPanel.setContent(progressCtrl.getInitialComponent());
		listenTo(progressCtrl);
		
		Runnable worker = new Runnable() {
			@Override
			public void run() {
				businessGroupService.dedupMembers(getIdentity(), coaches, participants, BusinessGroupModuleAdminController.this);
			}
		};
		TaskExecutorManager.getInstance().runTask(worker);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		module.setUserAllowedCreate(allowEl.isSelected(0));
		module.setAuthorAllowedCreate(allowEl.isSelected(1));
	}
}