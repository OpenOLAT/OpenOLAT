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
package org.olat.portfolio.ui.artefacts.collect;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.tree.TreeHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

/**
 * Description:<br>
 * controller to select a map as target for an artefact
 * 
 * <P>
 * Initial Date: 28.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPCollectStepForm04 extends StepFormBasicController {

	protected static final String NO_MAP_CHOOSEN = "noMapChoosen";
	protected static final String ROOT_NODE_IDENTIFIER = "rootMaps";
	private MenuTree mapsTreeController;
	private final EPFrontendManager ePFMgr;

	private AbstractArtefact artefact;
	private PortfolioStructure oldStructure;
	private PortfolioStructure preSelectedStructure;

	public EPCollectStepForm04(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout) {
		super(ureq, wControl, rootForm, runContext, layout, "step04selectmap");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		preSelectedStructure = (PortfolioStructure)runContext.get("preSelectedStructure");
		if(preSelectedStructure == null) {
			preSelectedStructure = ePFMgr.getUsersLastUsedPortfolioStructure(getIdentity());
		}
		initForm(flc, this, ureq);
	}

	public EPCollectStepForm04(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, PortfolioStructure oldStructure) {
		super(ureq, wControl, "step04selectmap");
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		this.artefact = artefact;
		this.oldStructure = oldStructure;
		initForm(this.flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<PortfolioStructure> structs = ePFMgr.getStructureElementsForUser(getIdentity());
		if (structs != null && structs.size() != 0) {
			TreeModel treeModel = new MapsTreeModel(getIdentity(), getTranslator());
			mapsTreeController = new MenuTree("my.maps");
			mapsTreeController.setTreeModel(treeModel);
			mapsTreeController.setSelectedNode(treeModel.getRootNode());
			mapsTreeController.setDragEnabled(false);
			mapsTreeController.setDropEnabled(false);
			mapsTreeController.setDropSiblingEnabled(false);
			mapsTreeController.addListener(this);
			mapsTreeController.setRootVisible(true);
			
			if(preSelectedStructure != null) {
				TreeNode node = TreeHelper.findNodeByUserObject(preSelectedStructure, treeModel.getRootNode());
				if(node != null) {
					mapsTreeController.setSelectedNode(node);
				}
			}
			flc.put("treeCtr", mapsTreeController);
		}
		
		if (!isUsedInStepWizzard()) {
			// add form buttons
			uifactory.addFormSubmitButton("stepform.submit", formLayout);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		PortfolioStructure selectedPortfolioStructure = preSelectedStructure;

		TreeNode node = mapsTreeController == null ? null : mapsTreeController.getSelectedNode();
		if(node != null) {
			Object obj = node.getUserObject();
			if(obj == null) {
				selectedPortfolioStructure = null;
			} else if (obj instanceof PortfolioStructure && !(obj instanceof EPAbstractMap)) {
				selectedPortfolioStructure = (PortfolioStructure)obj;
			}
		}

		if (selectedPortfolioStructure != null) {
			ePFMgr.setUsersLastUsedPortfolioStructure(getIdentity(), selectedPortfolioStructure);
		}
		if (isUsedInStepWizzard()) {
			addToRunContext("selectedStructure", selectedPortfolioStructure);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			if (selectedPortfolioStructure != null && !selectedPortfolioStructure.getKey().equals(oldStructure.getKey())) {
				ePFMgr.moveArtefactFromStructToStruct(artefact, oldStructure, selectedPortfolioStructure);
				// refresh ui
				fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.CHANGED, selectedPortfolioStructure));
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
