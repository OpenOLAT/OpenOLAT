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
* <p>
* Initial code contributed and copyrighted by<br>
* frentix GmbH, http://www.frentix.com
* <p>
*/
package org.olat.course.repository;

import java.io.File;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigManager;
import org.olat.course.config.CourseConfigManagerImpl;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.DetailsReadOnlyForm;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.references.ReferenceManager;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  9 déc. 2011 <br>
 *
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class ImportGlossaryReferencesController extends BasicController {

	private VelocityContainer importGlossaryVC;
	private Link importButton;
	private Link reattachButton;
	private Link noopButton;
	private Link continueButton;
	private ReferencableEntriesSearchController searchController;
	private RepositoryEntryImportExport importExport;
	private DetailsReadOnlyForm repoDetailsForm;
	private Panel main;
	private OLATResourceable ores;
	
	/**
	 * Constructor for the import workflow. Use this only in the repository course
	 * import workflow as a subworkflow
	 * 
	 * @param importExport
	 * @param course
	 * @param ureq
	 * @param wControl
	 */
	public ImportGlossaryReferencesController(RepositoryEntryImportExport importExport, OLATResourceable ores, UserRequest ureq,
			WindowControl wControl) {
		super(ureq, wControl);
		this.ores = ores;
		this.importExport = importExport;
		importGlossaryVC = createVelocityContainer("import_glossary");
		importButton = LinkFactory.createButton("import.import.action", importGlossaryVC, this);
		reattachButton = LinkFactory.createButton("import.reattach.action", importGlossaryVC, this);
		noopButton = LinkFactory.createButton("import.noop.action", importGlossaryVC, this);

		importGlossaryVC.contextPut("displayname", importExport.getDisplayName());
		importGlossaryVC.contextPut("resourcename", importExport.getResourceName());
		importGlossaryVC.contextPut("description", importExport.getDescription());
		main = new Panel("main");
		main.setContent(importGlossaryVC);
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		ICourse course = CourseFactory.loadCourse(ores);
		if (source == reattachButton) {
			removeAsListenerAndDispose(searchController);
			searchController = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, getTranslator()
					.translate("command.linkresource"));
			listenTo(searchController);
			main.setContent(ComponentUtil.createTitledComponent("command.linkresource", null, getTranslator(), searchController.getInitialComponent()));
		} else if (source == importButton) {
			RepositoryEntry importedRepositoryEntry = doImport(importExport, course, false, ureq.getIdentity());
			// If not successfull, return. Any error messages have baan already set.
			if (importedRepositoryEntry == null) {
				getWindowControl().setError("Import failed.");
				return;
			}
			//Translator repoTranslator = new PackageTranslator(Util.getPackageName(RepositoryManager.class), ureq.getLocale());
			removeAsListenerAndDispose(repoDetailsForm);
			repoDetailsForm = new DetailsReadOnlyForm(ureq, getWindowControl(), importedRepositoryEntry, GlossaryResource.TYPE_NAME);
			listenTo(repoDetailsForm);
			importGlossaryVC.put("repoDetailsForm", repoDetailsForm.getInitialComponent());
			String VELOCITY_ROOT = Util.getPackageVelocityRoot(this.getClass());
			importGlossaryVC.setPage(VELOCITY_ROOT + "/import_repo_details.html");
			continueButton = LinkFactory.createButton("import.redetails.continue", importGlossaryVC, this);
			return;
		} else if (source == noopButton) {
			// delete reference
			CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			courseConfig.setGlossarySoftKey(null);
			ccm.saveConfigTo(course, courseConfig);
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == continueButton) {
			fireEvent(ureq, Event.DONE_EVENT);
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
	public static RepositoryEntry doImport(RepositoryEntryImportExport importExport, ICourse course, boolean keepSoftkey, Identity owner) {
		GlossaryManager gm = GlossaryManager.getInstance();
		GlossaryResource resource = gm.createGlossary();
		if (resource == null) {
			Tracing.logError("Error adding glossary directry during repository reference import: " + importExport.getDisplayName(),
					ImportGlossaryReferencesController.class);
			return null;
		}

		// unzip contents
		VFSContainer glossaryContainer = gm.getGlossaryRootFolder(resource);
		File fExportedFile = importExport.importGetExportedFile();
		if (fExportedFile.exists()) ZipUtil.unzip(new LocalFileImpl(fExportedFile), glossaryContainer);
		else Tracing.logWarn("The actual contents of the glossary were not found in the export.", ImportGlossaryReferencesController.class);

		// create repository entry
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry importedRepositoryEntry = rm.createRepositoryEntryInstance(owner.getName());
		importedRepositoryEntry.setDisplayname(importExport.getDisplayName());
		importedRepositoryEntry.setResourcename(importExport.getResourceName());
		importedRepositoryEntry.setDescription(importExport.getDescription());
		if (keepSoftkey) importedRepositoryEntry.setSoftkey(importExport.getSoftkey());

		// Set the resource on the repository entry.
		OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(resource);
		importedRepositoryEntry.setOlatResource(ores);
		RepositoryHandler rh = RepositoryHandlerFactory.getInstance().getRepositoryHandler(importedRepositoryEntry);
		importedRepositoryEntry.setCanLaunch(rh.supportsLaunch(importedRepositoryEntry));

		// create security group
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
		// members of this group are always authors also
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
		securityManager.addIdentityToSecurityGroup(owner, newGroup);
		importedRepositoryEntry.setOwnerGroup(newGroup);
		
		//fxdiff VCRP-1,2: access control of resources
		// security group for tutors / coaches
		SecurityGroup tutorGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_ACCESS, importedRepositoryEntry.getOlatResource());
		// members of this group are always tutors also
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_TUTOR);
		importedRepositoryEntry.setTutorGroup(tutorGroup);
		
		// security group for participants
		SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_ACCESS, importedRepositoryEntry.getOlatResource());
		// members of this group are always participants also
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_PARTICIPANT);
		importedRepositoryEntry.setParticipantGroup(participantGroup);
		
		rm.saveRepositoryEntry(importedRepositoryEntry);

		if (!keepSoftkey) {
			// set the new glossary reference
			CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
			courseConfig.setGlossarySoftKey(importedRepositoryEntry.getSoftkey());
			ReferenceManager.getInstance().addReference(course, importedRepositoryEntry.getOlatResource(),
					GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);			
			CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		}

		return importedRepositoryEntry;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == searchController) {
			ICourse course = CourseFactory.loadCourse(ores);
			main.setContent(importGlossaryVC);
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				// repository search controller done
				RepositoryEntry re = searchController.getSelectedEntry();
				if (re != null) {
					CourseConfigManager ccm = CourseConfigManagerImpl.getInstance();
					CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
					courseConfig.setGlossarySoftKey(re.getSoftkey());
					ccm.saveConfigTo(course, courseConfig);
					ReferenceManager.getInstance().addReference(course, re.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER);
					getWindowControl().setInfo(getTranslator().translate("import.reattach.success"));
					fireEvent(ureq, Event.DONE_EVENT);
				}
				// else cancelled repo search, display import options again.
			}
		}
	}
	
	protected void importWithoutAsking (UserRequest ureq) {
        event (ureq, importButton, Event.DONE_EVENT);
        fireEvent(ureq, Event.DONE_EVENT);
	}

	protected void doDispose() {
		// Controllers autodisposed by basic controller
	}

}
