/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
* <p>
*/
package org.olat.portfolio.ui.artefacts.view;

import java.util.List;

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
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPUIFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm04;
import org.olat.portfolio.ui.structel.EPStructureChangeEvent;

/**
 * Description:<br>
 * simple artefact read-only controller
 * 
 * <P>
 * Initial Date:  17.11.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactViewReadOnlyController extends BasicController {

	private VelocityContainer vC;
	private EPFrontendManager ePFMgr;
	private Link detailsLink;
	private AbstractArtefact artefact;
	private PortfolioStructure struct;
	private EPSecurityCallback secCallback;
	private Link unlinkLink;
	private Link optionLink;
	private Link moveLink;
	private CloseableCalloutWindowController artefactOptionCalloutCtrl;
	private Link reflexionLink;
	private EPCollectStepForm04 moveTreeCtrl;
	private CloseableModalWindowWrapperController moveTreeBox;
	private Controller reflexionCtrl;

	protected EPArtefactViewReadOnlyController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, EPSecurityCallback secCallback, PortfolioStructure struct) {
		super(ureq, wControl);
		this.artefact = artefact;
		this.struct = struct;
		this.secCallback = secCallback;
		vC = createVelocityContainer("smallSingleArtefact");
		vC.contextPut("artefact", artefact);
		Identity artIdent = artefact.getAuthor();
		String fullName = artIdent.getUser().getProperty(UserConstants.FIRSTNAME, null)+" "+artIdent.getUser().getProperty(UserConstants.LASTNAME, null);
		
		String description = FilterFactory.getHtmlTagAndDescapingFilter().filter(artefact.getDescription());
		description = Formatter.truncate(description, 50);
		vC.contextPut("description", description);
		vC.contextPut("authorName", fullName);
		if (secCallback.canView()){
			detailsLink = LinkFactory.createCustomLink("small.details.link", "open", "small.details.link", Link.LINK, vC, this);
		}
		optionLink = LinkFactory.createCustomLink("option.link", "option", "&nbsp;&nbsp;", Link.NONTRANSLATED, vC, this);
		optionLink.setCustomEnabledLinkCSS("b_ep_options");
		optionLink.setTooltip(translate("option.link"), false);
		
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		List<String> tags = ePFMgr.getArtefactTags(artefact);
		vC.contextPut("tags", StringHelper.formatAsCSVString(tags));
		
		putInitialPanel(vC);	
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@SuppressWarnings("unused")
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == detailsLink && secCallback.canView()){
			String title = translate("view.artefact.header");
			CloseableModalWindowWrapperController artDetails = EPUIFactory.getAndActivatePopupArtefactController(artefact, ureq, getWindowControl(), title);
			listenTo(artDetails);
		} else if (source == unlinkLink){
			closeArtefactOptionsCallout();
			struct = ePFMgr.removeArtefactFromStructure(artefact, struct);
			fireEvent(ureq, new EPStructureChangeEvent(EPStructureChangeEvent.ADDED, struct)); // refresh ui
		} else if (source == optionLink){
			popUpArtefactOptionsBox(ureq);
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
			fireEvent(ureq, event);
			moveTreeBox.deactivate();
		}
	}

	private void showMoveTree(UserRequest ureq){
		moveTreeCtrl = new EPCollectStepForm04(ureq, getWindowControl(), artefact, struct);
		listenTo(moveTreeCtrl);
		String title = translate("artefact.move.title");
		moveTreeBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, moveTreeCtrl.getInitialComponent(), "moveTreeBox");
		listenTo(moveTreeBox);
		moveTreeBox.setInitialWindowSize(450, 300);
		moveTreeBox.activate();
	}
	
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
	
	private void closeArtefactOptionsCallout() {
		if (artefactOptionCalloutCtrl != null){
			artefactOptionCalloutCtrl.deactivate();
			removeAsListenerAndDispose(artefactOptionCalloutCtrl);
			artefactOptionCalloutCtrl = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		closeArtefactOptionsCallout();
	}

}
