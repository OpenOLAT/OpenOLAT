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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.portfolio.ui.artefacts.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 08.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class EPMultipleArtefactPreviewController extends BasicController implements EPMultiArtefactsController {

	private VelocityContainer vC;
	private Link artAttribBtn;
	private List<Controller> artefactCtrls;
	@Autowired
	private EPFrontendManager ePFMgr;
	private EPArtefactAttributeSettingController artAttribCtlr;
	private Map<String, Boolean> artAttribConfig;
	private boolean artefactChooseMode;
	private static final int artefactsPerPage = 4;
	private List<AbstractArtefact> artefactsFullList;
	private CloseableCalloutWindowController artAttribCalloutCtr;

	public EPMultipleArtefactPreviewController(UserRequest ureq, WindowControl wControl, List<AbstractArtefact> artefacts) {
		this(ureq, wControl, artefacts, false);
	}

	public EPMultipleArtefactPreviewController(UserRequest ureq, WindowControl wControl, List<AbstractArtefact> artefacts, boolean artefactChooseMode) {
		super(ureq, wControl);
		this.artefactChooseMode = artefactChooseMode;
		vC = createVelocityContainer("multiArtefact");
		if (!artefactChooseMode) {
			artAttribBtn = LinkFactory.createCustomLink("detail.options", "detail.options", "", Link.BUTTON + Link.NONTRANSLATED, vC, this);
			artAttribBtn.setTooltip(translate("detail.options"));
			artAttribBtn.setTitle(translate("detail.options"));
			artAttribBtn.setIconLeftCSS("o_icon o_icon_customize");
		}

		setNewArtefactsList(ureq, artefacts);
		
		putInitialPanel(vC);
	}

	public EPMultipleArtefactPreviewController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, null);
	}

	public void setNewArtefactsList(UserRequest ureq, List<AbstractArtefact> artefacts) {
		this.artefactsFullList = artefacts;
		if (artefacts != null) {
			preparePaging(ureq, 1);
		}
	}

	
	private void preparePaging(UserRequest ureq, int actualPage){
		int nrOfArtefacts = artefactsFullList.size(); 
		vC.contextPut("artefactAmnt", Integer.toString(nrOfArtefacts));
		if (nrOfArtefacts > artefactsPerPage){
			int divRest = (nrOfArtefacts % artefactsPerPage);
			int nrOfPages = (nrOfArtefacts / artefactsPerPage) + (divRest > 0 ? 1 : 0);
			ArrayList<Link> pageLinkList = new ArrayList<>();
			for (int i = 1; i < nrOfPages + 1; i++) {
				Link pageLink = LinkFactory.createCustomLink("pageLink" + i, "pageLink" + i, String.valueOf(i), Link.LINK + Link.NONTRANSLATED, vC, this);
				pageLink.setUserObject(i);
				if (actualPage == i) pageLink.setEnabled(false);
				pageLinkList.add(pageLink);
			}
			int fromIndex = (actualPage-1) * artefactsPerPage;
			int toIndex = actualPage * artefactsPerPage;
			if (toIndex > nrOfArtefacts) toIndex = nrOfArtefacts;
			List<AbstractArtefact> artefactsToShow = artefactsFullList.subList(fromIndex, toIndex);
			vC.contextPut("pageLinkList", pageLinkList);
			initOrUpdateArtefactControllers(ureq, artefactsToShow);
		} else {
			// no paging needed
			vC.contextRemove("pageLinkList");
			initOrUpdateArtefactControllers(ureq, artefactsFullList);
		}		
	}
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param artefacts
	 */
	private void initOrUpdateArtefactControllers(UserRequest ureq, List<AbstractArtefact> artefacts) {
		vC.contextPut("artefacts", artefacts);
		if (artefactCtrls != null) disposeArtefactControllers();
		artefactCtrls = new ArrayList<>();
		ArrayList<Component> artefactCtrlComps = new ArrayList<>();
		int i = 1;
		getArtefactAttributeDisplayConfig(ureq.getIdentity());
		if (artefacts != null) {
			for (AbstractArtefact abstractArtefact : artefacts) {
				Controller artCtrl = new EPArtefactViewController(ureq, getWindowControl(), abstractArtefact, artAttribConfig, artefactChooseMode, false, true);
				artefactCtrls.add(artCtrl);
				Component artefactCtrlComponent = artCtrl.getInitialComponent();
				listenTo(artCtrl);
				artefactCtrlComps.add(artefactCtrlComponent);
				vC.put("artCtrl" + i, artefactCtrlComponent);
				i++;
			}
		}
		vC.contextPut("artefactCtrlComps", artefactCtrlComps);
	}

	// dispose all artefact controllers
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
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		disposeArtefactControllers();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == artAttribBtn) {
			if (artAttribCalloutCtr == null){
				popupArtAttribBox(ureq);
			} else {
				// close on second click
				closeArtAttribBox();
			}
		} else if (source instanceof Link) {
			Link link = (Link) source;
			int pageNum = (Integer) link.getUserObject();
			preparePaging(ureq, pageNum);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == artAttribCtlr) {
			if (event.equals(Event.DONE_EVENT)) {
				closeArtAttribBox();
				// set new display config for each artefact controller
				vC.setDirty(true);
			}
		} else if (source instanceof EPArtefactViewController) {

			if (event.getCommand().equals(EPArtefactDeletedEvent.ARTEFACT_DELETED)) {
				// an artefact has been deleted, so refresh
				EPArtefactDeletedEvent epDelEv = (EPArtefactDeletedEvent) event;
				// only refresh whats needed, dont load all artefacts!
				artefactsFullList.remove(epDelEv.getArtefact());
				setNewArtefactsList(ureq, artefactsFullList);
				fireEvent(ureq, event); // pass to EPArtefactPoolRunCtrl
			}
		}
		if (event instanceof EPArtefactChoosenEvent) {
			// an artefact was choosen, pass through the event until top
			fireEvent(ureq, event);
		}
		if (source == artAttribCalloutCtr && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(artAttribCalloutCtr);
			artAttribCalloutCtr = null;
		} 

	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		OLATResourceable ores = entries.get(0).getOLATResourceable();
		if("AbstractArtefact".equals(ores.getResourceableTypeName())) {
			Long resId = ores.getResourceableId();
			
			int index = 0;
			for(AbstractArtefact artefact: artefactsFullList) {
				if(artefact.getKey().equals(resId) || artefact.getResourceableId().equals(resId)) {
					int rest = (index % artefactsPerPage);
					int page = (index - rest) / artefactsPerPage;
					preparePaging(ureq, page + 1);
					break;
				}
				index++;
			}
		}
	}

	private Map<String, Boolean> getArtefactAttributeDisplayConfig(Identity ident) {
		if (artAttribConfig == null) {
			artAttribConfig = ePFMgr.getArtefactAttributeConfig(ident);
		}
		return artAttribConfig;
	}

	/**
	 * @param ureq
	 */
	private void popupArtAttribBox(UserRequest ureq) {
		String title = translate("display.option.title");
		if (artAttribCtlr == null) {
			artAttribCtlr = new EPArtefactAttributeSettingController(ureq, getWindowControl(),
					getArtefactAttributeDisplayConfig(ureq.getIdentity()));
			listenTo(artAttribCtlr);
		}
		removeAsListenerAndDispose(artAttribCalloutCtr);
		artAttribCalloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), artAttribCtlr.getInitialComponent(), artAttribBtn, title, true, null);
		listenTo(artAttribCalloutCtr);
		artAttribCalloutCtr.activate();
	}

	private void closeArtAttribBox() {
		if (artAttribCalloutCtr!= null){
			artAttribCalloutCtr.deactivate();
			removeAsListenerAndDispose(artAttribCalloutCtr);
			artAttribCalloutCtr = null;
		}
	}

}
