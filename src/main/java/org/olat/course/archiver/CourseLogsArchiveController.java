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

package org.olat.course.archiver;

import java.io.File;
import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.modules.bc.FolderRunController;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.statistic.AsyncExportManager;
import org.olat.course.statistic.CourseLogRunEvent;
import org.olat.course.statistic.CourseLogRunnable;
import org.olat.home.HomeMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: Archives the user chosen courselogfiles
 * 
 * Initial Date: Dec 6, 2004
 * @author Alex
 */
public class CourseLogsArchiveController extends BasicController implements GenericEventListener {
	
	private Panel myPanel;
	private VelocityContainer myContent;
	
	private Link showFileButton;
	private OLATResourceable ores;
	private final OLATResourceable logOres;
	
	private CloseableModalController cmc;
	private LogFileChooserForm logFileChooserForm;
	
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AsyncExportManager asyncExportManager;
	@Autowired
	private RepositoryService repositoryService;
	
	/**
	 * Constructor for the course logs archive controller
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CourseLogsArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores) {
		super(ureq, wControl);
		this.ores = ores;
		logOres = OresHelper.createOLATResourceableInstance(CourseLogRunnable.class, ores.getResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, null, logOres);
		
		myPanel = new Panel("myPanel");
		myPanel.addListener(this);

		myContent = createVelocityContainer("start_courselogs");
		
		Roles roles = ureq.getUserSession().getRoles();
		
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(ores, false);
		boolean isAdministrator = roles.isAdministrator()
				&& repositoryService.hasRoleExpanded(getIdentity(), re, OrganisationRoles.administrator.name());
		boolean isOresOwner = repositoryService.hasRole(getIdentity(), re, GroupRoles.owner.name());
		boolean isOresInstitutionalManager = roles.isLearnResourceManager()
				&& repositoryService.hasRoleExpanded(getIdentity(), re, OrganisationRoles.learnresourcemanager.name());
		boolean aLogV = isOresOwner || isOresInstitutionalManager;
		boolean uLogV = isAdministrator;
		boolean sLogV = isOresOwner || isOresInstitutionalManager;
		
		if (asyncExportManager.asyncArchiveCourseLogOngoingFor(getIdentity())) {
			// then show the ongoing feedback
			showExportOngoing(false);
		} else if (isAdministrator || aLogV || uLogV || sLogV){
			myContent.contextPut("hasLogArchiveAccess", true);
			logFileChooserForm = new LogFileChooserForm(ureq, wControl, isAdministrator, aLogV, uLogV, sLogV);
			listenTo(logFileChooserForm);
			myContent.put("logfilechooserform",logFileChooserForm.getInitialComponent());
			ICourse course = CourseFactory.loadCourse(ores);
			myContent.contextPut("body", translate("course.logs.existingarchiveintro", course.getCourseTitle()));
			showFileButton = LinkFactory.createButton("showfile", myContent, this);
			File exportDir = CourseFactory.getDataExportDirectory(ureq.getIdentity(), course.getCourseTitle());
			boolean exportDirExists = false;
			if (exportDir!=null && exportDir.exists() && exportDir.isDirectory()) {
				exportDirExists = true;
			}
			myContent.contextPut("hascourselogarchive", Boolean.valueOf(exportDirExists));
			myPanel.setContent(myContent);
		} else {
		    myContent.contextPut("hasLogArchiveAccess", Boolean.valueOf(false));
			myPanel.setContent(myContent);
		}

		putInitialPanel(myPanel);
	}
	


	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, logOres);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showFileButton){
			doShowFiles(ureq);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == logFileChooserForm) {
			if (event == Event.DONE_EVENT) {
				doStartLog();
				showExportOngoing(true);
			}
		}
	}

	@Override
	public void event(Event event) {
		if(event instanceof CourseLogRunEvent) {
			CourseLogRunEvent clre = (CourseLogRunEvent)event;
			if(getIdentity().getKey().equals(clre.getIdentityKey())
					&& ores.getResourceableId().equals(clre.getOresId())) {
				showExportFinished();
			}
		}
	}

	private void doStartLog() {
		final boolean logAdminChecked = logFileChooserForm.logAdminChecked();
	    final boolean logUserChecked = logFileChooserForm.logUserChecked();
	    final boolean logStatisticChecked = logFileChooserForm.logStatChecked();
	    final Date begin = logFileChooserForm.getBeginDate();
		final Date end = logFileChooserForm.getEndDate();
    	
		if (end != null) {
			//shift time from beginning to end of day
			end.setTime(end.getTime() + 24 * 60 * 60 * 1000);
		}
	    
		ICourse course = CourseFactory.loadCourse(ores);
	    final String courseTitle = course.getCourseTitle();
	    final String targetDir = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), courseTitle).getPath();
	    
	    final Long resId = ores.getResourceableId();
	    final Locale theLocale = getLocale();
	    final String email = getIdentity().getUser().getProperty(UserConstants.EMAIL, getLocale());

	    asyncExportManager.asyncArchiveCourseLogFiles(getIdentity(),
	    		resId, targetDir, begin, end, logAdminChecked, logUserChecked, logStatisticChecked, theLocale, email);
	}
	
	private void doShowFiles(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(ores);
		String personalFolderDir = CourseFactory.getPersonalDirectory(getIdentity()).getPath();
		String targetDir = CourseFactory.getOrCreateDataExportDirectory(getIdentity(), course.getCourseTitle()).getPath();
	
		String relPath = "";
		if (targetDir.startsWith(personalFolderDir)) {
			// that should always be the case
			relPath = targetDir.substring(personalFolderDir.length()).replace("\\", "/");
			targetDir = targetDir.substring(0, personalFolderDir.length());
		}
	
		VFSContainer targetFolder = new LocalFolderImpl(new File(targetDir));
		FolderRunController bcrun = new FolderRunController(targetFolder, true, ureq, getWindowControl());
		Component folderComponent = bcrun.getInitialComponent();
		if (relPath.length()!=0) {
			if (!relPath.endsWith("/")) {
				relPath = relPath + "/";
			}
			bcrun.activatePath(ureq, relPath);
		}
			
		String personalFolder = Util.createPackageTranslator(HomeMainController.class, ureq.getLocale(), null).translate("menu.bc");
			
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), folderComponent, true, personalFolder);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void showExportOngoing(boolean thisCourse) {
		VelocityContainer vcOngoing = createVelocityContainer("courselogs_ongoing");
		if (thisCourse) {
			vcOngoing.contextPut("body", translate("course.logs.ongoing"));			
		} else {
			// more generic message that makes also sense in other courses
			vcOngoing.contextPut("body", translate("course.logs.busy"));			
		}
		myPanel.setContent(vcOngoing);
	}

	protected void showExportFinished() {
		ICourse course = CourseFactory.loadCourse(ores);
		VelocityContainer vcFeedback = createVelocityContainer("courselogs_feedback");
		showFileButton = LinkFactory.createButton("showfile", vcFeedback, this);
		vcFeedback.contextPut("body", translate("course.logs.feedback", course.getCourseTitle()));
		myPanel.setContent(vcFeedback);

		// note: polling can't be switched off unfortunatelly
		//       this is due to the fact that the jsandcsscomponent can only modify
		//       certain parts of the page and it would require a full page refresh
		//       to get rid of the poller - and that's not possible currently

		showInfo("course.logs.finished", course.getCourseTitle());
	}
}
