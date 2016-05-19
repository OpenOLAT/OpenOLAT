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
package org.olat.repository.ui;

import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.repository.ui.author.ConfirmDeleteController;
import org.olat.repository.ui.author.wizard.CloseResourceCallback;
import org.olat.repository.ui.author.wizard.Close_1_ExplanationStep;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Offers a way to change the repo entry life cycle / status: close and delete a
 * resource 
 * 
 * Initial date: 19.05.2016<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifeCycleChangeController extends BasicController{
	
	public static final Event closedEvent = new Event("closed");
	public static final Event deletedEvent = new Event("deleted");
	
	private Link closeLink, deleteLink;
	private VelocityContainer lifeCycleVC;

	private RepositoryEntry re;
	private final RepositoryEntrySecurity reSecurity;
	
	private StepsMainRunController closeCtrl;
	private ConfirmDeleteController confirmDeleteCtrl;
	private CloseableModalController cmc;

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	protected RepositoryEntryLifeCycleChangeController(UserRequest ureq, WindowControl wControl, RepositoryEntry re, RepositoryEntrySecurity reSecurity, RepositoryHandler handler) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.re = re;
		this.reSecurity = reSecurity;		
		
		lifeCycleVC = createVelocityContainer("lifecycle_change");
		putInitialPanel(lifeCycleVC);
		

		boolean isClosed = repositoryManager.createRepositoryEntryStatus(re.getStatusCode()).isClosed();
		boolean closeManged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.close);
		if (!isClosed && !closeManged) {
			closeLink = LinkFactory.createButton("close", lifeCycleVC, this);
			closeLink.setTitle("details.close.ressoure");
			closeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_close_resource");
			closeLink.setElementCssClass("o_sel_repo_close");
		}

		boolean deleteManaged = RepositoryEntryManagedFlag.isManaged(re, RepositoryEntryManagedFlag.delete);
		if (!deleteManaged) {
			deleteLink = LinkFactory.createButton("delete", lifeCycleVC, this);
			String type = translate(handler.getSupportedType());
			deleteLink.setCustomDisplayText(translate("details.delete.alt", new String[]{ type }));
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			deleteLink.setElementCssClass("o_sel_repo_close");
		}
		
	}

	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == deleteLink) {
			doDelete(ureq);
		} else if (source == closeLink) {
			doCloseResourceWizard(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(closeCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(closeCtrl);
				closeCtrl = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					doCloseResource(ureq);
				}
			}
		} else if(confirmDeleteCtrl == source) {
			cmc.deactivate();
			if(event == Event.CANCELLED_EVENT) {
				cleanUp();
			} else if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				cleanUp();
				fireEvent(ureq, deletedEvent);
			}
			
		} else if(cmc == source) {
			cleanUp();
		} 
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		cmc = null;
	}
	
	private void doCloseResourceWizard(UserRequest ureq) {
		removeAsListenerAndDispose(closeCtrl);

		Step start = new Close_1_ExplanationStep(ureq, re);
		StepRunnerCallback finish = new CloseResourceCallback(re);
		closeCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("wizard.closecourse.title"), "o_sel_checklist_wizard");
		listenTo(closeCtrl);
		getWindowControl().pushAsModalDialog(closeCtrl.getInitialComponent());
	}
	
	/**
	 * Remove close and edit tools, if in edit mode, pop-up-to root
	 * @param ureq
	 */
	private void doCloseResource(UserRequest ureq) {
		re = repositoryService.loadByKey(re.getKey());
		lifeCycleVC.remove(closeLink);
		closeLink = null;
		fireEvent(ureq, closedEvent);
	}
	
	private void doDelete(UserRequest ureq) {
		if (!reSecurity.isEntryAdmin()) {
			throw new OLATSecurityException("Trying to delete, but not allowed: user = " + ureq.getIdentity());
		}

		List<RepositoryEntry> entryToDelete = Collections.singletonList(re);
		confirmDeleteCtrl = new ConfirmDeleteController(ureq, getWindowControl(), entryToDelete, false);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("del.header", re.getDisplayname());
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	
	@Override
	protected void doDispose() {
		// nothing to dispose
	}

}
