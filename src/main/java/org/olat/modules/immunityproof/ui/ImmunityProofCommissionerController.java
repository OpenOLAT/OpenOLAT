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
package org.olat.modules.immunityproof.ui;

import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ui.event.ImmunityProofAddedEvent;

/**
 * Initial date: 09.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofCommissionerController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	
	private UserSearchFlexiController userSearchController;
	
	private ImmunityProofCreateController immunityProofCreateController;
	private ImmunityProofCardController cardController;
	private CloseableModalController cmc;
	
	public ImmunityProofCommissionerController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		userSearchController = new UserSearchFlexiController(ureq, getWindowControl(), null, false);
		listenTo(userSearchController);
		
		mainVC = createVelocityContainer("immunity_proof_commissioner");
		mainVC.put("userSearch", userSearchController.getInitialComponent());
		
		putInitialPanel(mainVC);		
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (userSearchController == null) {
			userSearchController = new UserSearchFlexiController(ureq, getWindowControl(), null, false);
			listenTo(userSearchController);
		}
		
		mainVC.put("userSearch", userSearchController.getInitialComponent());
		
		if (getInitialComponent() == null) {
			putInitialPanel(mainVC);
		}
	}
	
	@Override
	protected void doDispose() {
		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userSearchController) {
			if (event instanceof SingleIdentityChosenEvent) {
				doAddImmunityProof(ureq, ((SingleIdentityChosenEvent) event).getChosenIdentity());
			}
		} else if (source == immunityProofCreateController) {
			cleanUp();
			
			if (event instanceof ImmunityProofAddedEvent) {
				doCardPreview(ureq, ((ImmunityProofAddedEvent) event).getIdentity());
			} 
		} else if (source == cardController) {
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
	}
	
	private void doAddImmunityProof(UserRequest ureq, Identity identity) {
		immunityProofCreateController = new ImmunityProofCreateController(ureq, getWindowControl(), identity, true);
		listenTo(immunityProofCreateController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("cancel"), immunityProofCreateController.getInitialComponent(), true, translate("add.immunity.proof"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCardPreview(UserRequest ureq, Identity identity) {
		cardController = new ImmunityProofCardController(ureq, getWindowControl(), identity, false);
		listenTo(cardController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("cancel"), cardController.getInitialComponent(), true, translate("preview.immunity.proof"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void cleanUp() {
		if (cmc != null && cmc.isCloseable()) {
			cmc.deactivate();
		}
		
		removeAsListenerAndDispose(immunityProofCreateController);
		removeAsListenerAndDispose(cmc);
		
		immunityProofCreateController = null;
		cmc = null;
	}
	

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do here		
	}

}
