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
package org.olat.portfolio.ui.artefacts.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * show minimal set of artefact details in small preview controllers.
 * if an artefact handler provides a special preview, use this instead the generic artefact-view used inside maps.
 * <P>
 * Initial Date: 17.11.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMultipleArtefactSmallReadOnlyPreviewController extends BasicController implements EPMultiArtefactsController {

	private List<AbstractArtefact> artefacts;
	@Autowired
	private PortfolioModule portfolioModule;
	private ArrayList<Controller> artefactCtrls;
	private ArrayList<Controller> optionLinkCtrls;
	private VelocityContainer vC;
	private PortfolioStructure struct;
	private EPSecurityCallback secCallback;
	
	public EPMultipleArtefactSmallReadOnlyPreviewController(UserRequest ureq, WindowControl wControl, List<AbstractArtefact> artefacts, PortfolioStructure struct, EPSecurityCallback secCallback) {
		super(ureq, wControl);
		this.artefacts = artefacts;
		this.struct = struct;
		this.secCallback = secCallback;
		vC = createVelocityContainer("smallMultiArtefactPreview");
		
		init(ureq);
		putInitialPanel(vC);
	}
	
	private void init(UserRequest ureq) {
		if (artefactCtrls != null) disposeArtefactControllers();
		if( optionLinkCtrls != null) disposeOptionLinkControllers();
		optionLinkCtrls = new ArrayList<>();
		artefactCtrls = new ArrayList<>();
		List<List<Panel>> artefactCtrlCompLines = new ArrayList<>();
		List<Panel> artefactCtrlCompLine = new ArrayList<>();
		int i = 1;
		for (AbstractArtefact artefact : artefacts) {
			EPArtefactHandler<?> artHandler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());
			Controller artCtrl;
			// check for special art-display:
			boolean special = artHandler.isProvidingSpecialMapViewController();
			if (special) {
				artCtrl = artHandler.getSpecialMapViewController(ureq, getWindowControl(), artefact);
				if(artCtrl != null) { 
					//add the optionsLink to the artefact
					EPArtefactViewOptionsLinkController optionsLinkCtrl = new EPArtefactViewOptionsLinkController(ureq, getWindowControl(), artefact, secCallback, struct);
					vC.put("optionsLink"+i,optionsLinkCtrl.getInitialComponent());
					listenTo(optionsLinkCtrl);
					optionLinkCtrls.add(optionsLinkCtrl);
				}
			} else {
				artCtrl = new EPArtefactViewReadOnlyController(ureq, getWindowControl(), artefact, struct, secCallback, true);
			}
			if (artCtrl != null){
				artefactCtrls.add(artCtrl);
				Component artefactCtrlComponent = artCtrl.getInitialComponent();
				listenTo(artCtrl);
				
				Panel namedPanel = new Panel("artCtrl" + i);
				namedPanel.setContent(artefactCtrlComponent);

				if(special) {
					if(!artefactCtrlCompLine.isEmpty()) {
						artefactCtrlCompLines.add(artefactCtrlCompLine);
					}
					artefactCtrlCompLines.add(Collections.singletonList(namedPanel));
					artefactCtrlCompLine = new ArrayList<>();
				} else {
					if(artefactCtrlCompLine.size() == 3) {
						if(!artefactCtrlCompLine.isEmpty()) {
							artefactCtrlCompLines.add(artefactCtrlCompLine);
						}
						artefactCtrlCompLine = new ArrayList<>();
					}
					artefactCtrlCompLine.add(namedPanel);
				}
				vC.put("artCtrl" + i, namedPanel);
				if(special) {//need a flag in a lopp for the velociy template
					vC.put("specialartCtrl" + i, artefactCtrlComponent);
				}
				i++;
			}
		}
		if(!artefactCtrlCompLine.isEmpty()) {
			artefactCtrlCompLines.add(artefactCtrlCompLine);
		}
		
		vC.contextPut("artefactCtrlCompLines", artefactCtrlCompLines);
	}
	
	private void disposeArtefactControllers() {
		if (artefactCtrls != null){
			for (Controller artefactCtrl : artefactCtrls) {
				removeAsListenerAndDispose(artefactCtrl);
				artefactCtrl = null;
			}
			artefactCtrls = null;
		}
	}

	/**
	 * dispose the list that holds optionLinkControlllers
	 */
	private void disposeOptionLinkControllers(){
		if (optionLinkCtrls != null) {
			for (Controller optionCtrl : optionLinkCtrls) {
				removeAsListenerAndDispose(optionCtrl);
				optionCtrl = null;
			}
			optionLinkCtrls = null;
		}
	}
	
	/**
	 * @see org.olat.portfolio.ui.artefacts.view.EPMultiArtefactsController#setNewArtefactsList(org.olat.core.gui.UserRequest, java.util.List)
	 */
	@Override
	public void setNewArtefactsList(UserRequest ureq, List<AbstractArtefact> artefacts) {
		this.artefacts = artefacts;
		init(ureq);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// no events to handle yet
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		fireEvent(ureq, event); // pass to others
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		if("AbstractArtefact".equals(ores.getResourceableTypeName())) {
			Long resId = ores.getResourceableId();
			for(AbstractArtefact artefact: artefacts) {
				if(artefact.getKey().equals(resId) || artefact.getResourceableId().equals(resId)) {
					System.out.println("Match");
				}
			}
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		disposeArtefactControllers();
		disposeOptionLinkControllers();
	}

}
