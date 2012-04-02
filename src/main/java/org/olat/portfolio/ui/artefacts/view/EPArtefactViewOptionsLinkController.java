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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm04;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

/**
 * Description: Displays the options-Link for an artefact. handles displaying of the callout and its links 
 * (remove artefact from map, reflexion, move artefact within map)<br>
 * <P>
 * Initial Date: 14.07.2011 <br>
 * 
 * @author Sergio Trentini, sergio.trentini@frentix.com, http://www.frentix.com
 */
public class EPArtefactViewOptionsLinkController extends BasicController {

	
	private final AbstractArtefact artefact;
	private PortfolioStructure struct;
	private final EPSecurityCallback secCallback;
	private final VelocityContainer vC;
	private final EPFrontendManager ePFMgr;
	
	//controllers
	private EPCollectStepForm04 moveTreeCtrl;
	private CloseableModalWindowWrapperController moveTreeBox;
	private Controller reflexionCtrl;
	private CloseableCalloutWindowController artefactOptionCalloutCtrl;
	
	

	// the link that triggers the callout
	private Link optionLink;

	// the links within the callout
	private Link unlinkLink;
	private Link moveLink;
	private Link reflexionLink;
	
	
	public EPArtefactViewOptionsLinkController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact,
			final EPSecurityCallback secCallback, final PortfolioStructure struct){
		super(ureq,wControl);
		this.artefact = artefact;
		this.struct = struct;
		this.secCallback = secCallback;
		
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		vC = createVelocityContainer("optionsLink");
		
		optionLink = LinkFactory.createCustomLink("option.link", "option", "&nbsp;&nbsp;", Link.NONTRANSLATED, vC, this);
		optionLink.setCustomEnabledLinkCSS("b_ep_options");
		optionLink.setTooltip(translate("option.link"), false);
		
		putInitialPanel(vC);
	}
	
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == optionLink){
			popUpArtefactOptionsBox(ureq);
		}else if (source == unlinkLink) {
			closeArtefactOptionsCallout();
			struct = ePFMgr.removeArtefactFromStructure(artefact, struct);
			fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.REMOVED, struct)); // refresh ui
		} else if (source == moveLink){
			closeArtefactOptionsCallout();
			showMoveTree(ureq);
		} else if (source == reflexionLink) {
			closeArtefactOptionsCallout();
			reflexionCtrl = EPUIFactory.getReflexionPopup(ureq, getWindowControl(), secCallback, artefact, struct);
			listenTo(reflexionCtrl);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == moveTreeCtrl && event.getCommand().equals(EPStructureChangeEvent.CHANGED)){
			EPStructureChangeEvent epsEv = (EPStructureChangeEvent) event;
			PortfolioStructure newStruct = epsEv.getPortfolioStructure();
			showInfo("artefact.moved", newStruct.getTitle());
			moveTreeBox.deactivate();
		}
		fireEvent(ureq, event);
	}
	
	/**
	 * opens a modalWindow that displays the "move-tree"
	 * 
	 * @param ureq
	 */
	private void showMoveTree(UserRequest ureq){
		moveTreeCtrl = new EPCollectStepForm04(ureq, getWindowControl(), artefact, struct);
		listenTo(moveTreeCtrl);
		String title = translate("artefact.move.title");
		moveTreeBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, moveTreeCtrl.getInitialComponent(), "moveTreeBox");
		listenTo(moveTreeBox);
		moveTreeBox.setInitialWindowSize(450, 300);
		moveTreeBox.activate();
	}
	
	
	/**
	 * closes the callout
	 */
	private void closeArtefactOptionsCallout() {
		if (artefactOptionCalloutCtrl != null){
			artefactOptionCalloutCtrl.deactivate();
			removeAsListenerAndDispose(artefactOptionCalloutCtrl);
			artefactOptionCalloutCtrl = null;
		}
	}
	
	/**
	 * opens the callout
	 * @param ureq
	 */
	private void popUpArtefactOptionsBox(UserRequest ureq) {
		VelocityContainer artOptVC = createVelocityContainer("artefactOptions");
		if (secCallback.canRemoveArtefactFromStruct()){
			unlinkLink = LinkFactory.createCustomLink("unlink.link", "remove", "remove.from.map", Link.LINK, artOptVC, this);
		}		
		if (secCallback.canAddArtefact() && secCallback.canRemoveArtefactFromStruct() && secCallback.isOwner()){ // isOwner: don't show move in group maps!
			moveLink = LinkFactory.createCustomLink("move.link", "move", "artefact.options.move", Link.LINK, artOptVC, this);
		}
		reflexionLink = LinkFactory.createCustomLink("reflexion.link", "reflexion", "table.header.reflexion", Link.LINK, artOptVC, this);
		String title = translate("option.link");
		removeAsListenerAndDispose(artefactOptionCalloutCtrl);
		artefactOptionCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), artOptVC, optionLink, title, true, null);
		listenTo(artefactOptionCalloutCtrl);
		artefactOptionCalloutCtrl.activate();
	}
	

	@Override
	protected void doDispose() {
		closeArtefactOptionsCallout();
	}

}
