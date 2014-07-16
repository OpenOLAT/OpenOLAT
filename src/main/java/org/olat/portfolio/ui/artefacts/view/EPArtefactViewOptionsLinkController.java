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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm04;
import org.olat.portfolio.ui.artefacts.edit.EPReflexionWrapperController;
import org.olat.portfolio.ui.artefacts.edit.EPTagsController;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

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
	@Autowired
	private EPFrontendManager ePFMgr;
	
	//controllers
	private EPCollectStepForm04 moveTreeCtrl;
	private CloseableModalController moveTreeBox;
	private Controller tagsCtrl;
	private EPReflexionWrapperController reflexionCtrl;
	private CloseableCalloutWindowController artefactOptionCalloutCtrl;
	
	// the link that triggers the callout
	private Link optionLink;

	// the links within the callout
	private Link unlinkLink;
	private Link moveLink;
	private Link reflexionLink;
	private Link tagsLink;
	
	public EPArtefactViewOptionsLinkController(final UserRequest ureq, final WindowControl wControl, final AbstractArtefact artefact,
			final EPSecurityCallback secCallback, final PortfolioStructure struct){
		super(ureq,wControl);
		this.artefact = artefact;
		this.struct = struct;
		this.secCallback = secCallback;
		
		vC = createVelocityContainer("optionsLink");
		
		optionLink = LinkFactory.createCustomLink("option.link", "option", " ", Link.NONTRANSLATED, vC, this);
		optionLink.setIconLeftCSS("o_icon o_icon_actions");
		optionLink.setTooltip(translate("option.link"));
		
		putInitialPanel(optionLink);
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
			reflexionCtrl = new EPReflexionWrapperController(ureq, getWindowControl(), secCallback, artefact, struct);
			listenTo(reflexionCtrl);
		} else if (source == tagsLink) {
			closeArtefactOptionsCallout();
			tagsCtrl = new EPTagsController(ureq, getWindowControl(), secCallback, artefact, struct);
			listenTo(tagsCtrl);
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
		} else if (source == artefactOptionCalloutCtrl) {
			removeAsListenerAndDispose(artefactOptionCalloutCtrl);
			artefactOptionCalloutCtrl = null;
		} else if (source == tagsCtrl) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			removeAsListenerAndDispose(tagsCtrl);
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
		moveTreeBox = new CloseableModalController(getWindowControl(), title, moveTreeCtrl.getInitialComponent());
		listenTo(moveTreeBox);
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
		if (secCallback.canAddArtefact() && secCallback.canRemoveArtefactFromStruct() && secCallback.isOwner()) { // isOwner: don't show move in group maps!
			moveLink = LinkFactory.createCustomLink("move.link", "move", "artefact.options.move", Link.LINK, artOptVC, this);
		}
		reflexionLink = LinkFactory.createCustomLink("reflexion.link", "reflexion", "table.header.reflexion", Link.LINK, artOptVC, this);
		if(secCallback.canEditTags()) {
			tagsLink = LinkFactory.createCustomLink("tags.link", "tags", "artefact.tags", Link.LINK, artOptVC, this);
		}
		
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