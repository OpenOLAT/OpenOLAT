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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm03;
import org.olat.portfolio.ui.artefacts.collect.EPReflexionChangeEvent;
import org.olat.portfolio.ui.artefacts.view.EPArtefactViewController;
import org.olat.portfolio.ui.artefacts.view.EPReflexionViewController;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Wrapper controller, to instantiate a reflexion-editor or viewer depending on config
 * 
 * <P>
 * Initial Date: 21.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPReflexionWrapperController extends BasicController {
	@Autowired
	private EPFrontendManager ePFMgr;
	private Controller reflexionCtrl;
	private boolean mapClosed;
	private EPSecurityCallback secCallback;
	private AbstractArtefact artefact;
	private PortfolioStructure struct;
	private CloseableModalController reflexionBox;

	public EPReflexionWrapperController(UserRequest ureq, WindowControl wControl, EPSecurityCallback secCallback,
			AbstractArtefact artefact, PortfolioStructure struct) {
		super(ureq, wControl);
		if (struct != null && struct.getRoot() instanceof PortfolioStructureMap) {
			mapClosed = StructureStatusEnum.CLOSED.equals(((PortfolioStructureMap) struct.getRoot()).getStatus());
		} else {
			mapClosed = false;
		}
		this.secCallback = secCallback;
		this.artefact = artefact;
		this.struct = struct;
		setTranslator(Util.createPackageTranslator(EPArtefactViewController.class, ureq.getLocale(), getTranslator()));

		init(ureq);
	}

	private void init(UserRequest ureq) {
		removeAsListenerAndDispose(reflexionCtrl);
		String title = "";
		boolean artClosed = ePFMgr.isArtefactClosed(artefact);
		if (mapClosed || !secCallback.canEditReflexion() || (artClosed && struct == null)) {
			// reflexion cannot be edited, view only!
			reflexionCtrl = new EPReflexionViewController(ureq, getWindowControl(), artefact, struct);
		} else {
			// check for an existing reflexion on the artefact <-> struct link
			String reflexion = ePFMgr.getReflexionForArtefactToStructureLink(artefact, struct);
			if (StringHelper.containsNonWhitespace(reflexion)) {
				// edit an existing reflexion
				reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact, reflexion);
				title = translate("title.reflexion.link");
			} else if (struct != null) {
				// no reflexion on link yet, show warning and preset with
				// artefacts-reflexion
				reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact, true);
				title = translate("title.reflexion.artefact");
			} else {
				// preset controller with reflexion of the artefact. used by
				// artefact-pool
				reflexionCtrl = new EPCollectStepForm03(ureq, getWindowControl(), artefact);
				title = translate("title.reflexion.artefact");
			}
		}
		listenTo(reflexionCtrl);
		removeAsListenerAndDispose(reflexionBox);
		reflexionBox = new CloseableModalController(getWindowControl(), title, reflexionCtrl.getInitialComponent());
		listenTo(reflexionBox);
		reflexionBox.activate();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == reflexionCtrl && event instanceof EPReflexionChangeEvent) {
			EPReflexionChangeEvent refEv = (EPReflexionChangeEvent) event;
			if (struct != null) {
				ePFMgr.setReflexionForArtefactToStructureLink(refEv.getRefArtefact(), struct, refEv.getReflexion());
				reflexionBox.deactivate();
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, struct));
			} else {
				AbstractArtefact refArtefact = refEv.getRefArtefact();
				refArtefact.setReflexion(refEv.getReflexion());
				ePFMgr.updateArtefact(refArtefact);
				reflexionBox.deactivate();
				fireEvent(ureq, Event.DONE_EVENT);
			}
			removeAsListenerAndDispose(reflexionBox);
		} else if (source == reflexionBox && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(reflexionBox);
			reflexionBox = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
