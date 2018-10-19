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
package org.olat.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.EPSecurityCallback;
import org.olat.portfolio.EPSecurityCallbackImpl;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.EPFilterSettings;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.artefacts.collect.EPAddArtefactController;
import org.olat.portfolio.ui.artefacts.view.EPArtefactChoosenEvent;
import org.olat.portfolio.ui.artefacts.view.EPArtefactDeletedEvent;
import org.olat.portfolio.ui.artefacts.view.EPArtefactListChoosenEvent;
import org.olat.portfolio.ui.artefacts.view.EPMultiArtefactsController;
import org.olat.portfolio.ui.artefacts.view.EPMultipleArtefactPreviewController;
import org.olat.portfolio.ui.artefacts.view.EPMultipleArtefactsAsTableController;
import org.olat.portfolio.ui.artefacts.view.EPTagBrowseController;
import org.olat.portfolio.ui.artefacts.view.EPTagBrowseEvent;
import org.olat.portfolio.ui.filter.EPFilterSelectController;
import org.olat.portfolio.ui.filter.PortfolioFilterChangeEvent;
import org.olat.portfolio.ui.filter.PortfolioFilterController;
import org.olat.portfolio.ui.filter.PortfolioFilterEditEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Presents an overview of all artefacts of an user. 
 * 
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPArtefactPoolRunController extends BasicController implements Activateable2 {

	private VelocityContainer vC;
	private EPFilterSettings filterSettings = new EPFilterSettings();
	private EPAddArtefactController addArtefactCtrl;
	
	private SegmentViewComponent segmentView;
	private Link artefactsLink, browseLink, searchLink;
	private Controller filterSelectCtrl;
	private Filter previousFilterMode;
	private EPViewModeController viewModeCtrl;
	private EPMultiArtefactsController artCtrl;
	private String previousViewMode;
	private List<AbstractArtefact> previousArtefactsList;
	
	private final boolean artefactChooseMode;
	private final boolean canAddArtefacts;
	private final boolean importV2;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioModule portfolioModule;
	
	private PortfolioStructure preSelectedStruct;

	public EPArtefactPoolRunController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false, true, false);
	}
	
	public EPArtefactPoolRunController(UserRequest ureq, WindowControl wControl, boolean artefactChooseMode, boolean canAddArtefacts) {
		this(ureq, wControl, artefactChooseMode, canAddArtefacts, false);
	}
	
	public EPArtefactPoolRunController(UserRequest ureq, WindowControl wControl, boolean artefactChooseMode, boolean canAddArtefacts, boolean importV2) {
		super(ureq, wControl);
		this.importV2 = importV2;
		this.canAddArtefacts = canAddArtefacts;
		this.artefactChooseMode = artefactChooseMode;
		Component viewComp = new Panel("empty");
		Component filterPanel = new Panel("filter");

		if (portfolioModule.isEnabled()) {
			init(ureq);
			initViewModeController(ureq);
			viewComp = vC;
			vC.put("filterPanel", filterPanel);
			
			if (filterSettings.isFilterEmpty()) {
				initTPAllView(ureq);
			} else {
				initTPFilterView(ureq);
			}

			putInitialPanel(viewComp);
		} else {
			putInitialPanel(createVelocityContainer("portfolio_disabled"));
		}
	}

	/**
	 * create the velocity for the artefact-pool with a tabbed pane / segmented
	 * view this doesn't initialize anything, the panels first are empty!
	 * 
	 * @param ureq
	 */
	private void init(UserRequest ureq) {
		vC = createVelocityContainer("artefactsmain");
		vC.contextPut("artefactChooseMode", artefactChooseMode);
		vC.contextPut("importV2", importV2);
		
		segmentView = SegmentViewFactory.createSegmentView("segments", vC, this);
		artefactsLink = LinkFactory.createLink("viewTab.all", vC, this);
		segmentView.addSegment(artefactsLink, true);
		
		browseLink = LinkFactory.createLink("viewTab.browse", vC, this);
		segmentView.addSegment(browseLink, false);
		
		searchLink = LinkFactory.createLink("viewTab.search", vC, this);
		segmentView.addSegment(searchLink, false);
		
		if(canAddArtefacts) {
			addArtefactCtrl = new EPAddArtefactController(ureq, getWindowControl());
			listenTo(addArtefactCtrl);
			vC.put("addArtefactCtrl", addArtefactCtrl.getInitialComponent());
		}
	}
	
	/**
	 * switch between filter selection (drop down only) and the full filter-view
	 * and put this to the filter-panel
	 * @param ureq
	 * @param readOnlyMode
	 */
	private void initFilterPanel(UserRequest ureq, Filter filterMode){
		if (filterSelectCtrl == null || previousFilterMode != filterMode){
			removeAsListenerAndDispose(filterSelectCtrl);
			switch(filterMode) {
				case read_only:
					filterSelectCtrl = new EPFilterSelectController(ureq, getWindowControl(), filterSettings.getFilterId());
					break;
				case tags:
					filterSelectCtrl = new EPTagBrowseController(ureq, getWindowControl());
					break;
				case extended:
					filterSelectCtrl = new PortfolioFilterController(ureq, getWindowControl(), filterSettings);
					break;
			}

			previousFilterMode = filterMode;
			listenTo(filterSelectCtrl);
			vC.put("filterPanel", filterSelectCtrl.getInitialComponent());
		}
	}

	private void setSegmentContent(Controller ctrl){
		vC.put("segmentContent", ctrl.getInitialComponent());
		vC.setDirty(true);
	}

	private void initTPAllView(UserRequest ureq) {
		filterSettings = new EPFilterSettings();
		List<AbstractArtefact> artefacts = ePFMgr.getArtefactPoolForUser(getIdentity());
		initMultiArtefactCtrl(ureq, artefacts);
		initFilterPanel(ureq, Filter.read_only);
		setSegmentContent(artCtrl);
		addToHistory(ureq, OresHelper.createOLATResourceableType("All"), null);
	}

	
	private void initMultiArtefactCtrl(UserRequest ureq, List<AbstractArtefact> artefacts){
		// decide how to present artefacts depending on users settings
		String userPrefsMode = ePFMgr.getUsersPreferedArtefactViewMode(getIdentity(), EPViewModeController.VIEWMODE_CONTEXT_ARTEFACTPOOL);
		if (previousViewMode != null && !previousViewMode.equals(userPrefsMode)) {
			removeAsListenerAndDispose(artCtrl);
		}

		if (importV2  || (userPrefsMode != null && userPrefsMode.equals(EPViewModeController.VIEWMODE_TABLE))){
			EPSecurityCallback secCallback = new EPSecurityCallbackImpl(true, true);
			artCtrl = new EPMultipleArtefactsAsTableController(ureq, getWindowControl(), artefacts, null, artefactChooseMode, importV2, secCallback);
			viewModeCtrl.selectTable();
		} else {
			artCtrl = new EPMultipleArtefactPreviewController(ureq, getWindowControl(), artefacts, artefactChooseMode);
			viewModeCtrl.selectDetails();
		}
		previousViewMode = userPrefsMode;
		listenTo(artCtrl);
		previousArtefactsList = artefacts;
	}

	private void initTPFilterView(UserRequest ureq) {
		List<AbstractArtefact> filteredArtefacts = ePFMgr.filterArtefactsByFilterSettings(filterSettings, 
				getIdentity(), ureq.getUserSession().getRoles(), getLocale());
		initMultiArtefactCtrl(ureq, filteredArtefacts);
		initFilterPanel(ureq, Filter.extended);
		setSegmentContent(artCtrl);
		addToHistory(ureq, OresHelper.createOLATResourceableType("Search"), null);
	}

	private void initTPBrowseView(UserRequest ureq) {
		List<AbstractArtefact> artefacts = ePFMgr.getArtefactPoolForUser(getIdentity());
		initMultiArtefactCtrl(ureq, artefacts);
		initFilterPanel(ureq, Filter.tags);
		setSegmentContent(artCtrl);
		addToHistory(ureq, OresHelper.createOLATResourceableType("Browse"), null);
	}
	
	private void initViewModeController(UserRequest ureq){
		viewModeCtrl = new EPViewModeController(ureq, getWindowControl(), EPViewModeController.VIEWMODE_CONTEXT_ARTEFACTPOOL);
		listenTo(viewModeCtrl);
		vC.put("viewMode", viewModeCtrl.getInitialComponent());
	}

	public PortfolioStructure getPreSelectedStruct() {
		return preSelectedStruct;
	}

	public void setPreSelectedStruct(PortfolioStructure preSelectedStruct) {
		this.preSelectedStruct = preSelectedStruct;
		if(addArtefactCtrl != null) {
			addArtefactCtrl.setPreSelectedStruct(preSelectedStruct);
		}
	}

	@Override
	protected void doDispose() {
		// ctrls disposed by basicCtrl due to listenTo()'s
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = vC.getComponent(segmentCName);
				if (clickedLink == artefactsLink) {
					initTPAllView(ureq);
				} else if (clickedLink == browseLink){
					initTPBrowseView(ureq);
				} else if (clickedLink == searchLink){
					initTPFilterView(ureq);
				}
			}	
		}		
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		if(portfolioModule.isEnabled()) {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("All".equalsIgnoreCase(type)) {
				initTPAllView(ureq);
				segmentView.select(artefactsLink);
			} else if("Browse".equalsIgnoreCase(type)) {
				initTPBrowseView(ureq);
				segmentView.select(browseLink);
			} else if("Search".equalsIgnoreCase(type)) {
				initTPFilterView(ureq);
				segmentView.select(searchLink);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == addArtefactCtrl) {
			// some artefacts were added, refresh view
			if (event.equals(Event.DONE_EVENT)) {
				initTPAllView(ureq);
				fireEvent(ureq, event);
			}
		} else if (event instanceof EPArtefactChoosenEvent || event instanceof EPArtefactListChoosenEvent) {
			// an artefact was choosen, pass through the event until top
			fireEvent(ureq, event);
		} else if (source == filterSelectCtrl) {
			if (event instanceof PortfolioFilterChangeEvent) {
				PortfolioFilterChangeEvent pFEvent = (PortfolioFilterChangeEvent) event;
				filterSettings = pFEvent.getFilterList();
			} else if (event instanceof PortfolioFilterEditEvent) {
				PortfolioFilterEditEvent editEvent = (PortfolioFilterEditEvent)event;
				filterSettings = editEvent.getFilterList();
			}
			if (source instanceof EPFilterSelectController){
				if (event == Event.CHANGED_EVENT) {
					initTPFilterView(ureq);
				} else if (event instanceof PortfolioFilterChangeEvent) {
					// preset search was selected, apply it, but stay within first segment
					initTPFilterView(ureq);
					initFilterPanel(ureq, Filter.read_only);
				} else if (event instanceof PortfolioFilterEditEvent) {
					initTPFilterView(ureq);
					initFilterPanel(ureq, Filter.extended);
					segmentView.select(searchLink);
				}
			} else if (source instanceof EPTagBrowseController) {
				if (event instanceof EPTagBrowseEvent) {
					EPTagBrowseEvent found = (EPTagBrowseEvent)event;
					initMultiArtefactCtrl(ureq, found.getArtefacts());
					setSegmentContent(artCtrl);
				}
			} else if (source instanceof PortfolioFilterController){
				if(event instanceof PortfolioFilterChangeEvent) {
					initTPFilterView(ureq);
				}
			}
		} else if (source == viewModeCtrl && event.getCommand().equals(EPViewModeController.VIEWMODE_CHANGED_EVENT_CMD)){
			initMultiArtefactCtrl(ureq, previousArtefactsList);
			setSegmentContent(artCtrl);
		} else if (event instanceof EPArtefactDeletedEvent){
			EPArtefactDeletedEvent epDelEv = (EPArtefactDeletedEvent) event;
			previousArtefactsList.remove(epDelEv.getArtefact());
			initMultiArtefactCtrl(ureq, previousArtefactsList);
			setSegmentContent(artCtrl);
		}
	}

	private enum Filter {
		read_only,
		tags,
		extended
	}
}
