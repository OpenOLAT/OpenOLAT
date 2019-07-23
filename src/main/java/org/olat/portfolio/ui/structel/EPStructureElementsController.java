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
package org.olat.portfolio.ui.structel;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.view.EPMultiArtefactsController;
import org.olat.portfolio.ui.structel.edit.EPCollectRestrictionResultController;

/**
 * Description:<br>
 * displays child structure elements on page or on a map
 * 
 * <P>
 * Initial Date:  24.08.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPStructureElementsController extends BasicController {

	private List<PortfolioStructure> structElements;
	List<Controller> tableCtrls;
	List<Controller> addBtnCtrls;
	private final EPSecurityCallback secCallback;
	private final EPFrontendManager ePFMgr;
	private boolean parentMapClosed;
	private int maxStructAmount;
	
	private final VelocityContainer flc;

	public EPStructureElementsController(UserRequest ureq, WindowControl wControl, List<PortfolioStructure> structElements,
			EPSecurityCallback secCallback, boolean parentMapClosed) {
		super(ureq, wControl);

		this.structElements = structElements;
		this.secCallback = secCallback;
		this.parentMapClosed = parentMapClosed;
		this.maxStructAmount = 1;
		
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		
		flc = createVelocityContainer("structElements");
		initForm(ureq);
		putInitialPanel(flc);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer, org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	protected void initForm(UserRequest ureq) {
		flc.contextPut("structElements", structElements);
		tableCtrls = new ArrayList<>();
		addBtnCtrls = new ArrayList<>();
		
		int i = 1;
		removeComponents();
		for (PortfolioStructure portStruct : structElements) {

			if(secCallback.isRestrictionsEnabled()) {
				List<CollectRestriction> restrictions = portStruct.getCollectRestrictions();
				if(!restrictions.isEmpty()) {
					boolean check = ePFMgr.checkCollectRestriction(portStruct);
					EPCollectRestrictionResultController resultCtrl = new EPCollectRestrictionResultController(ureq, getWindowControl());
					resultCtrl.setMessage(portStruct.getCollectRestrictions(), check);
					flc.put("checkResults" + i, resultCtrl.getInitialComponent());
					listenTo(resultCtrl);
				}
			}
			
			// get artefacts for this structure 
			List<AbstractArtefact> artefacts = ePFMgr.getArtefacts(portStruct);
			if (artefacts.size() != 0) {
				EPMultiArtefactsController artefactCtrl = 
					EPUIFactory.getConfigDependentArtefactsControllerForStructure(ureq, getWindowControl(), artefacts, portStruct, secCallback);
				flc.put("artefacts" + i, artefactCtrl.getInitialComponent());
				listenTo(artefactCtrl);
				tableCtrls.add(artefactCtrl);				
			}
			
			if(!parentMapClosed && secCallback.canAddArtefact()) {
				// get an addElement-button for each structure
				EPAddElementsController addButton = new EPAddElementsController(ureq, getWindowControl(), portStruct);
				listenTo(addButton);
				addButton.setShowLink(EPAddElementsController.ADD_ARTEFACT);
				flc.put("addButton" + i, addButton.getInitialComponent());
				addBtnCtrls.add(addButton);
			}			
			i++;
		}
		if (i!=maxStructAmount) maxStructAmount = i;
	}

	// remove components which were put before to be able to update flc by initForm
	private void removeComponents(){
		for (int j = 1; j < maxStructAmount; j++) {
			flc.remove(flc.getComponent("artefacts" + j));
			flc.remove(flc.getComponent("addButton" + j));
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (event instanceof EPStructureChangeEvent){
			//update the elements
			EPStructureChangeEvent changeEvent = (EPStructureChangeEvent)event;
			PortfolioStructure changedEl = changeEvent.getPortfolioStructure();
			if(changedEl != null) {
				int index = 0;
				for(PortfolioStructure strucEl:structElements) {
					if(changedEl.getKey().equals(strucEl.getKey())) {
						structElements.set(index, changedEl);
						break;
					}
					index++;
				}
			}
			
			// something changed
			initForm(ureq);

			//pass it on, parent controllers (EPPageViewController)  might need to update 
			fireEvent(ureq, changeEvent);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// dispose all in table-ctrls, button-ctrls
		List<Controller> allCtrls = new ArrayList<>();
		allCtrls.addAll(addBtnCtrls);
		allCtrls.addAll(tableCtrls);
		for (Controller ctrl : allCtrls) {
			removeAsListenerAndDispose(ctrl);
		}
		addBtnCtrls = null;
		tableCtrls = null;
	}

}
