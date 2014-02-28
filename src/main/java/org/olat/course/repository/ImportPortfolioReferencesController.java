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

package org.olat.course.repository;

import java.io.File;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeEditController;
import org.olat.portfolio.EPTemplateMapResource;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPXStreamHandler;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.repository.DetailsReadOnlyForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.RepositoryTableModel;
import org.olat.repository.controllers.RepositorySearchController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Description:<br>
 * Import portfolio map in the DB
 * 
 * <P>
 * Initial Date:  7 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportPortfolioReferencesController extends BasicController {
	
	private static final OLog log = Tracing.createLoggerFor(ImportPortfolioReferencesController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ImportPortfolioReferencesController.class);

	private CourseNode node;
	private VelocityContainer main;
	private Link importButton;
	private Link reattachButton;
	private Link noopButton;
	private Link cancelButton;
	private Link continueButton;
	private Link importYesModeButton;
	private RepositorySearchController searchController;
	private RepositoryEntryImportExport importExport;
	private DetailsReadOnlyForm repoDetailsForm;
	private Panel mainPanel;


	public ImportPortfolioReferencesController(UserRequest ureq, WindowControl wControl, CourseNode node, RepositoryEntryImportExport importExport) {
		super(ureq, wControl);
		this.node = node;
		this.importExport = importExport;

		main = this.createVelocityContainer("import_repo");
		importButton = LinkFactory.createButton("import.import.action", main, this);
		reattachButton = LinkFactory.createButton("import.reattach.action", main, this);
		noopButton = LinkFactory.createButton("import.noop.action", main, this);
		cancelButton = LinkFactory.createButton("cancel", main, this);
		importYesModeButton = LinkFactory.createButton("import.yesmode.action", main, this);
		main.contextPut("nodename", node.getShortTitle());
		main.contextPut("type", translate("node." + node.getType()));
		main.contextPut("displayname", importExport.getDisplayName());
		main.contextPut("resourcename", importExport.getResourceName());
		main.contextPut("description", importExport.getDescription());
		mainPanel = new Panel("mainPanel");
		mainPanel.setContent(main);

		putInitialPanel(mainPanel);
	}

	public void importWithoutAsking (UserRequest ureq) {
		event (ureq, importButton, Event.DONE_EVENT);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == reattachButton) {
			String type = EPTemplateMapResource.TYPE_NAME;
			searchController = new RepositorySearchController(translate("command.linkresource"), ureq, getWindowControl(), true,
					false, false, type);
			searchController.addControllerListener(this);
			searchController.doSearchByOwnerLimitType(ureq.getIdentity(), type);
			mainPanel.setContent(searchController.getInitialComponent());
		} else if (source == importButton) {
			RepositoryEntry importedRepositoryEntry = doImport(importExport, node, false, ureq.getIdentity());
			// If not successfull, return. Any error messages have bean already set.
			if (importedRepositoryEntry == null) {
				getWindowControl().setError("Import failed.");
				return;
			}
			String typeName = EPTemplateMapResource.TYPE_NAME;
			removeAsListenerAndDispose(repoDetailsForm);
			repoDetailsForm = new DetailsReadOnlyForm(ureq, getWindowControl(), importedRepositoryEntry, typeName);
			listenTo(repoDetailsForm);
			main.put("repoDetailsForm", repoDetailsForm.getInitialComponent());
			main.setPage(VELOCITY_ROOT + "/import_repo_details.html");
			continueButton = LinkFactory.createButton("import.redetails.continue", main, this);
			return;
		} else if (source == noopButton) {
			// delete reference
			PortfolioCourseNodeEditController.removeReference(node.getModuleConfiguration());
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else if (source == continueButton){
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == importYesModeButton) {
			fireEvent(ureq, new Event("importYesMode"));
		}
	}

	/**
	 * Import a referenced repository entry.
	 * 
	 * @param importExport
	 * @param node
	 * @param importMode Type of import.
	 * @param keepSoftkey If true, no new softkey will be generated.
	 * @param owner
	 * @return
	 */
	public static RepositoryEntry doImport(RepositoryEntryImportExport importExport, CourseNode node, boolean keepSoftkey,
			Identity owner) {
		File fExportedFile = importExport.importGetExportedFile();
		PortfolioStructure structure = EPXStreamHandler.getAsObject(fExportedFile, false);
		if(structure == null) {
			log.warn("Error adding portfolio map resource during repository reference import: " + importExport.getDisplayName());
			return null;
		}
		
		
		// create repository entry
		RepositoryManager rm = RepositoryManager.getInstance();
		EPFrontendManager ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		
		PortfolioStructure map = ePFMgr.importPortfolioMapTemplate(structure, owner);
		OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(map.getOlatResource());
		RepositoryEntry importedRepositoryEntry = repositoryService.create(owner, importExport.getResourceName(),
				importExport.getDisplayName(), importExport.getDescription(), ores);
		if (keepSoftkey) {
			String theSoftKey = importExport.getSoftkey();
			if (rm.lookupRepositoryEntryBySoftkey(theSoftKey, false) != null) {
				/*
				 * keepSoftKey == true -> is used for importing in unattended mode.
				 * "Importing and keeping the soft key" only works if there is not an
				 * already existing soft key. In the case both if's are taken the
				 * respective IMS resource is not imported. It is important to be aware
				 * that the resource which triggered the import process still keeps the
				 * soft key reference, and thus points to the already existing resource.
				 */
				return null;
			}
			importedRepositoryEntry.setSoftkey(importExport.getSoftkey());
		}

		RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
		importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));
		
		//map.setGroup();
		

		if (!keepSoftkey) {
			setReference(importedRepositoryEntry, map, node);
		}

		return importedRepositoryEntry;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchController) {
			mainPanel.setContent(main);
			if (event.getCommand().equals(RepositoryTableModel.TABLE_ACTION_SELECT_LINK)) {
				// repository search controller done
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					EPFrontendManager ePFMgr = (EPFrontendManager)CoreSpringFactory.getBean("epFrontendManager");
					PortfolioStructureMap map = (PortfolioStructureMap)ePFMgr.loadPortfolioStructure(re.getOlatResource());
					setReference(re, map, node);
					getWindowControl().setInfo(translate("import.reattach.success"));
					fireEvent(ureq, Event.DONE_EVENT);
				}
				// else cancelled repo search, display import options again.
			}
		}
	}

	private static void setReference(RepositoryEntry entry, PortfolioStructure map, CourseNode node) {
		// attach repository entry to the node
		PortfolioCourseNodeEditController.setReference(entry, map, node.getModuleConfiguration());
	}

	protected void doDispose() {
		if (searchController != null) {
			searchController.dispose();
			searchController = null;
		}
	}
}
