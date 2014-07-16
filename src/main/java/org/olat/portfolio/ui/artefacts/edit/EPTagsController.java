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
package org.olat.portfolio.ui.artefacts.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.util.Util;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm01;
import org.olat.portfolio.ui.artefacts.view.EPArtefactViewController;
import org.olat.portfolio.ui.artefacts.view.EPTagViewController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.07.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EPTagsController extends StepFormBasicController {

	private boolean mapClosed;
	private EPSecurityCallback secCallback;
	private PortfolioStructure struct;
	private AbstractArtefact artefact;
	
	private Controller tagsCtrl;
	private CloseableModalController tagsModalCtrl;
	
	@Autowired
	private EPFrontendManager ePFMgr;

	public EPTagsController(UserRequest ureq, WindowControl wControl, EPSecurityCallback secCallback,
			AbstractArtefact artefact, PortfolioStructure struct) {
		super(ureq, wControl);
		this.artefact = artefact;
		
		if (struct != null && struct.getRoot() instanceof PortfolioStructureMap) {
			mapClosed = StructureStatusEnum.CLOSED.equals(((PortfolioStructureMap) struct.getRoot()).getStatus());
		} else {
			mapClosed = false;
		}
		this.secCallback = secCallback;
		this.artefact = artefact;
		this.struct = struct;
		setTranslator(Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		removeAsListenerAndDispose(tagsCtrl);
		removeAsListenerAndDispose(tagsModalCtrl);
		
		boolean artClosed = ePFMgr.isArtefactClosed(artefact);
		if ( mapClosed || !secCallback.canEditTags() || (artClosed && struct == null)) {
			// reflexion cannot be edited, view only!
			tagsCtrl = new EPTagViewController(ureq, getWindowControl(), artefact);
		} else {
			tagsCtrl = new EPCollectStepForm01(ureq, getWindowControl(), artefact);
		}
		listenTo(tagsCtrl);

		String title = translate("artefact.tags.title");
		tagsModalCtrl = new CloseableModalController(getWindowControl(), title, tagsCtrl.getInitialComponent(), true, title);
		listenTo(tagsModalCtrl);
		tagsModalCtrl.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(tagsModalCtrl == source) {
			cleanUp();
		} else if(tagsCtrl == source) {
			tagsModalCtrl.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(tagsCtrl);
		removeAsListenerAndDispose(tagsModalCtrl);
		tagsCtrl = null;
		tagsModalCtrl = null;
	}

	@Override
	protected void doDispose() {
		// nothing
	}
}
