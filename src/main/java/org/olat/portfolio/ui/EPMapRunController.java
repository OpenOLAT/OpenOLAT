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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.EPCreateMapController;
import org.olat.portfolio.ui.structel.EPMapCreatedEvent;
import org.olat.portfolio.ui.structel.EPMapEvent;
import org.olat.portfolio.ui.structel.EPMultipleMapController;
import org.olat.portfolio.ui.structel.EPStructureEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Shows all Maps of a user.
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPMapRunController extends BasicController implements Activateable2 {

	private VelocityContainer vC;
	private Link createMapLink;
	private Link createMapFromTemplateLink;
	private EPCreateMapController createMapCtrl;
	private CloseableModalController createMapBox;
	private ReferencableEntriesSearchController searchTemplateCtrl;
	private EPMultipleMapController multiMapCtrl;
	private SearchInputController searchController;
	
	private final boolean create;
	private final Identity choosenOwner;
	private final EPMapRunViewOption option;
	@Autowired
	private EPFrontendManager ePFMgr;
	private Link createMapCalloutLink;
	private CloseableCalloutWindowController mapCreateCalloutCtrl;

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param create Can user create new maps in this context
	 * @param option Select the view option from the maps
	 * @param choosenOwner Limit the list to maps from one specific owner
	 * @param types
	 */
	public EPMapRunController(UserRequest ureq, WindowControl wControl, boolean create, EPMapRunViewOption option,
			Identity choosenOwner) {	
		super(ureq, wControl);
		this.create = create;
		this.option = option;
		this.choosenOwner = choosenOwner;
		
		Component viewComp = init(ureq);
		putInitialPanel(viewComp);
	}

	private VelocityContainer init(UserRequest ureq) {
		vC = createVelocityContainer("mymapsmain");
		vC.contextPut("overview", Boolean.TRUE);
		if(create) {
			createMapLink = LinkFactory.createButton("create.map", vC, this);	
			createMapLink.setElementCssClass("o_sel_create_map");
		}
		
		String documentType;
		switch(option) {
			case MY_DEFAULTS_MAPS:
				documentType = "type.d*." + EPDefaultMap.class.getSimpleName();
				break;
			case MY_EXERCISES_MAPS:
				documentType = "type.*." + EPStructuredMap.class.getSimpleName();
				break;
			default:
				documentType = null;
				break;
		}
		
		if(documentType != null) {
			SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
			searchController = searchServiceUIFactory.createInputController(ureq, getWindowControl(), DisplayOption.STANDARD, null);
			listenTo(searchController);
			vC.put("search_input", searchController.getInitialComponent());
			
			searchController.setDocumentType(documentType);
			searchController.setResourceContextEnable(true);
			searchController.setResourceUrl(null);
		}
		
		
		initTitle(vC);
		removeAsListenerAndDispose(multiMapCtrl);
		multiMapCtrl = new EPMultipleMapController(ureq, getWindowControl(), option, choosenOwner);
		listenTo(multiMapCtrl);
		vC.put("mapCtrl", multiMapCtrl.getInitialComponent());
		return vC;
	}
	
	private void initTitle(VelocityContainer container) {
		String titleKey;
		String descriptionKey;
		switch(option) {
			case OTHER_MAPS:
				titleKey = "othermap.title";
				descriptionKey = "othermap.intro";
				break;
			case OTHERS_MAPS:
				titleKey = "othermaps.title";
				descriptionKey = "othermaps.intro";
				break;
			case MY_EXERCISES_MAPS:
				titleKey = "mystructuredmaps.title";
				descriptionKey = "mystructuredmaps.intro";
				break;
			default:// MY_DEFAULTS_MAPS:
				titleKey = "mymaps.title";
				descriptionKey = "mymaps.intro";
				break;
		}

		container.contextPut("title", titleKey);
		container.contextPut("description", descriptionKey);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		multiMapCtrl.activate(ureq, entries, state);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == createMapLink){
			// if only normal maps can be created, show popup immediately, else present selection in callout
			if(option.equals(EPMapRunViewOption.MY_DEFAULTS_MAPS)) {
				VelocityContainer mapCreateVC = createVelocityContainer("createMapCallout");
				createMapCalloutLink = LinkFactory.createLink("create.map.default", mapCreateVC, this);	
				createMapCalloutLink.setElementCssClass("o_sel_create_default_map");
				createMapFromTemplateLink = LinkFactory.createLink("create.map.fromTemplate", mapCreateVC, this);
				createMapFromTemplateLink.setElementCssClass("o_sel_create_template_map");
				String title = translate("create.map");
				
				removeAsListenerAndDispose(mapCreateCalloutCtrl);
				mapCreateCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), mapCreateVC, createMapLink, title, true, null);
				listenTo(mapCreateCalloutCtrl);
				mapCreateCalloutCtrl.activate();
			} else {
				popUpCreateMapBox(ureq);
			}
		} else if (source == createMapFromTemplateLink) {
			closeCreateMapCallout();
			popUpCreateMapFromTemplateBox(ureq);
		} else if (source == createMapCalloutLink){
			closeCreateMapCallout();
			popUpCreateMapBox(ureq);
		}
	}
	
	private void closeCreateMapCallout() {
		if (mapCreateCalloutCtrl != null){
			mapCreateCalloutCtrl.deactivate();
			removeAsListenerAndDispose(mapCreateCalloutCtrl);
			mapCreateCalloutCtrl = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == createMapBox) {
			popDownCreateMapBox();
		} else if (source == createMapCtrl){
			if (event instanceof EPMapCreatedEvent){
				PortfolioStructureMap newMap = ((EPMapCreatedEvent) event).getPortfolioStructureMap();
				multiMapCtrl.activateMap(ureq, newMap);
			}
			createMapBox.deactivate();
			popDownCreateMapBox();
			toogleHeader(false);
		} else if (source == searchTemplateCtrl) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = searchTemplateCtrl.getSelectedEntry();
				PortfolioStructureMap newMap = createMapFromTemplate(repoEntry);
				multiMapCtrl.activateMap(ureq, newMap);
			}
			createMapBox.deactivate();
			popDownCreateMapBox();
		} else if (source == multiMapCtrl) {
			if(event instanceof EPMapEvent) {
				String cmd = event.getCommand();
				if(EPStructureEvent.SELECT.equals(cmd)) {
					toogleHeader(false);
				} else if(EPStructureEvent.CLOSE.equals(cmd)) {
					toogleHeader(true);
				}
			}
		} else if (source == mapCreateCalloutCtrl && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(mapCreateCalloutCtrl);
			mapCreateCalloutCtrl = null;
		}
	}
	
	private void toogleHeader(boolean enable) {
		if(vC != null) {
			vC.contextPut("overview", new Boolean(enable));
		}
	}
	
	private void popDownCreateMapBox() {
		removeAsListenerAndDispose(createMapCtrl);
		createMapCtrl = null;
		createMapBox.dispose();
		createMapBox = null;
	}

	/**
	 * @param ureq
	 */
	private void popUpCreateMapBox(UserRequest ureq) {
		String title = translate("create.map");
		createMapCtrl = new EPCreateMapController(ureq, getWindowControl());
		listenTo(createMapCtrl);
		createMapBox = new CloseableModalController(getWindowControl(), title, createMapCtrl.getInitialComponent(), true, title);
		createMapBox.setCustomWindowCSS("o_sel_add_map_window");
		listenTo(createMapBox);
		createMapBox.activate();
	}
	
	private void popUpCreateMapFromTemplateBox(UserRequest ureq) {
		String title = translate("create.map");
		String commandLabel = translate("create.map.selectTemplate");
		removeAsListenerAndDispose(searchTemplateCtrl);
		searchTemplateCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				new String[]{EPTemplateMapResource.TYPE_NAME}, commandLabel, false, false, false, false, false);			
		listenTo(searchTemplateCtrl);
		createMapBox = new CloseableModalController(getWindowControl(), title, searchTemplateCtrl.getInitialComponent(), true, title);
		createMapBox.setCustomWindowCSS("o_sel_add_map_template_window");
		listenTo(createMapBox);
		createMapBox.activate();
	}
	
	private PortfolioStructureMap createMapFromTemplate(RepositoryEntry repoEntry) {
		PortfolioStructureMap template = (PortfolioStructureMap)ePFMgr.loadPortfolioStructure(repoEntry.getOlatResource());
		PortfolioStructureMap copy = ePFMgr.createAndPersistPortfolioDefaultMap(getIdentity(), template.getTitle(), template.getDescription());
		ePFMgr.copyStructureRecursively(template, copy, true);
		return copy;
	}

	@Override
	protected void doDispose() {
		if (createMapBox != null) {
			createMapBox.dispose();
			createMapBox = null;
		}		
	}
}
