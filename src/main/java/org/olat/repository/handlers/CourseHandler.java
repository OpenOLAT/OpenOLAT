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

package org.olat.repository.handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.layout.MainLayoutController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.CleanupAfterDeliveryFileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.repository.CreateNewCourseController;
import org.olat.course.repository.ImportCourseController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;
import org.olat.repository.controllers.WizardCloseCourseController;
import org.olat.repository.controllers.WizardCloseResourceController;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.resource.accesscontrol.ui.RepositoryMainAccessControllerWrapper;
import org.olat.resource.references.ReferenceManager;
import org.olat.user.UserManager;

import de.tuchemnitz.wizard.workflows.coursecreation.CourseCreationHelper;
import de.tuchemnitz.wizard.workflows.coursecreation.CourseCreationMailHelper;
import de.tuchemnitz.wizard.workflows.coursecreation.model.CourseCreationConfiguration;
import de.tuchemnitz.wizard.workflows.coursecreation.steps.CcStep00;


/**
 * Initial Date: Apr 15, 2004
 *
 * @author 
 * 
 * Comment: Mike Stock
 * 
 */
public class CourseHandler implements RepositoryHandler {
	
	private static final OLog log = Tracing.createLoggerFor(CourseHandler.class);

	/**
	 * Command to add (i.e. import) a course.
	 */
	public static final String PROCESS_IMPORT = "add";
	/**
	 * Command to create a new course.
	 */
	public static final String PROCESS_CREATENEW = "new";
	
	private static final boolean LAUNCHEABLE = true;
	private static final boolean DOWNLOADEABLE = true;
	private static final boolean EDITABLE = true;
	private static final boolean WIZARD_SUPPORT = true;
	private static final List<String> supportedTypes;

	static { // initialize supported types
		supportedTypes = new ArrayList<String>(1);
		supportedTypes.add(CourseModule.getCourseTypeName());
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getSupportedTypes()
	 */
	public List<String> getSupportedTypes() {	return supportedTypes; }
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsDownload()
	 */
	public boolean supportsDownload(RepositoryEntry repoEntry) {	return DOWNLOADEABLE;	}
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsLaunch()
	 */
	public boolean supportsLaunch(RepositoryEntry repoEntry) {	return LAUNCHEABLE;	}
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsEdit()
	 */
	public boolean supportsEdit(RepositoryEntry repoEntry) {	return EDITABLE; }
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#supportsWizard(org.olat.repository.RepositoryEntry)
	 */
	public boolean supportsWizard(RepositoryEntry repoEntry) { return WIZARD_SUPPORT; }
	
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getLaunchController(org.olat.core.id.OLATResourceable java.lang.String, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public MainLayoutController createLaunchController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		MainLayoutController courseCtrl = CourseFactory.createLaunchController(ureq, wControl, re);
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, courseCtrl);
		return wrapper;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAsMediaResource(org.olat.core.id.OLATResourceable
	 */
	@Override
	public MediaResource getAsMediaResource(OLATResourceable res, boolean backwardsCompatible) {
		RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(res, true);
		String exportFileName = StringHelper.transformDisplayNameToFileSystemName(re.getDisplayname()) + ".zip";
		File fExportZIP = new File(WebappHelper.getTmpDir(), exportFileName);
		CourseFactory.exportCourseToZIP(res, fExportZIP, false, backwardsCompatible);
		return new CleanupAfterDeliveryFileMediaResource(fExportZIP);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getEditorController(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	@Override
	public Controller createEditorController(RepositoryEntry re, UserRequest ureq, WindowControl wControl) {
		//run + activate
		MainLayoutController courseCtrl = CourseFactory.createLaunchController(ureq, wControl, re);
		RepositoryMainAccessControllerWrapper wrapper = new RepositoryMainAccessControllerWrapper(ureq, wControl, re, courseCtrl);
		return wrapper;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getAddController(org.olat.repository.controllers.RepositoryAddCallback, java.lang.Object, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public IAddController createAddController(RepositoryAddCallback callback, Object userObject, UserRequest ureq, WindowControl wControl) {
		if (userObject == null || userObject.equals(PROCESS_CREATENEW))
			return new CreateNewCourseController(callback, ureq, wControl);
		else if (userObject.equals(PROCESS_IMPORT))
			return new ImportCourseController(callback, ureq, wControl);
		else throw new AssertException("Command " + userObject + " not supported by CourseHandler.");
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#getCreateWizardController(org.olat.core.id.OLATResourceable, org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public Controller createWizardController(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		// load the course structure
		final RepositoryEntry repoEntry = (RepositoryEntry) res;
		ICourse course = CourseFactory.loadCourse(repoEntry.getOlatResource());
		Translator cceTranslator = Util.createPackageTranslator(CourseCreationHelper.class, ureq.getLocale());
		final CourseCreationConfiguration courseConfig = new CourseCreationConfiguration(course.getCourseTitle(), Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repoEntry.getKey());
		// wizard finish callback called after "finish" is called
		final CourseCreationHelper ccHelper = new CourseCreationHelper(ureq.getLocale(), repoEntry, courseConfig , course);
		StepRunnerCallback finishCallback = new StepRunnerCallback() {
			public Step execute(UserRequest ureq, WindowControl control, StepsRunContext runContext) {
				// here goes the code which reads out the wizards data from the runContext and then does some wizardry
				ccHelper.finalizeWorkflow(ureq);
				control.setInfo(CourseCreationMailHelper.getSuccessMessageString(ureq));
				// send notification mail
				final MailerResult mr = CourseCreationMailHelper.sentNotificationMail(ureq, ccHelper.getConfiguration());
				MailHelper.printErrorsAndWarnings(mr, control, ureq.getLocale());
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		Step start  = new CcStep00(ureq, courseConfig, repoEntry);
		StepsMainRunController ccSMRC = new StepsMainRunController(ureq, wControl, start, finishCallback, null, cceTranslator.translate("coursecreation.title"), "o_sel_course_create_wizard");
		return ccSMRC;
	}


	public Controller createDetailsForm(UserRequest ureq, WindowControl wControl, OLATResourceable res) {
		return CourseFactory.getDetailsForm(ureq, wControl, res);
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#cleanupOnDelete(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean cleanupOnDelete(OLATResourceable res) {
		// notify all current users of this resource (course) that it will be deleted now.
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new OLATResourceableJustBeforeDeletedEvent(res), res);
		//archiving is done within readyToDelete		
		CourseFactory.deleteCourse(res);
		// delete resourceable
		OLATResourceManager rm = OLATResourceManager.getInstance();
		OLATResource ores = rm.findResourceable(res);
		rm.deleteOLATResource(ores);
		return true;
	}

	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#readyToDelete(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest, org.olat.core.gui.control.WindowControl)
	 */
	public boolean readyToDelete(OLATResourceable res, UserRequest ureq, WindowControl wControl) {
		ReferenceManager refM = ReferenceManager.getInstance();
		String referencesSummary = refM.getReferencesToSummary(res, ureq.getLocale());
		if (referencesSummary != null) {
			Translator translator = Util.createPackageTranslator(RepositoryManager.class, ureq.getLocale());
			wControl.setError(translator.translate("details.delete.error.references",
					new String[] { referencesSummary }));
			return false;
		}
		/*
		 * make an archive of the course nodes with valuable data
		 */
		UserManager um = UserManager.getInstance();
		String charset = um.getUserCharset(ureq.getIdentity());
		try {
			CourseFactory.archiveCourse(res,charset, ureq.getLocale(), ureq.getIdentity(), ureq.getUserSession().getRoles());
		} catch (CorruptedCourseException e) {
			log.error("The course is corrupted, cannot archive it: " + res, e);
		}
		return true;
	}
	
	/**
	 * @see org.olat.repository.handlers.RepositoryHandler#createCopy(org.olat.core.id.OLATResourceable org.olat.core.gui.UserRequest)
	 */
	public OLATResourceable createCopy(OLATResourceable res, UserRequest ureq) {
		return CourseFactory.copyCourse(res, ureq);
	}
	
	/**
	 * Archive the hole course with runtime-data and course-structure-data.
	 * @see org.olat.repository.handlers.RepositoryHandler#archive(java.lang.String, org.olat.repository.RepositoryEntry)
	 */
	public String archive(Identity archiveOnBehalfOf, String archivFilePath, RepositoryEntry entry) {
		ICourse course = CourseFactory.loadCourse(entry.getOlatResource() );
		// Archive course runtime data (like delete course, archive e.g. logfiles, node-data)
		File tmpExportDir = new File(WebappHelper.getTmpDir(), CodeHelper.getUniqueID());
		tmpExportDir.mkdirs();
		CourseFactory.archiveCourse(archiveOnBehalfOf, course, WebappHelper.getDefaultCharset(), I18nModule.getDefaultLocale(), tmpExportDir , true);
		// Archive course run structure (like course export)
		String courseExportFileName = "course_export.zip";
		File courseExportZIP = new File(tmpExportDir, courseExportFileName);
		CourseFactory.exportCourseToZIP(entry.getOlatResource(), courseExportZIP, true, false);
		// Zip runtime data and course run structure data into one zip-file
		String completeArchiveFileName = "del_course_" + entry.getOlatResource().getResourceableId() + ".zip";
		String completeArchivePath = archivFilePath + File.separator + completeArchiveFileName;
		ZipUtil.zipAll(tmpExportDir, new File(completeArchivePath), false);
		FileUtils.deleteDirsAndFiles(tmpExportDir, true, true);
		return completeArchiveFileName;
	}

	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#acquireLock(org.olat.core.id.OLATResourceable, org.olat.core.id.Identity)
	 */
	public LockResult acquireLock(OLATResourceable ores, Identity identity) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().acquireLock(ores, identity, CourseFactory.COURSE_EDITOR_LOCK);
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#releaseLock(org.olat.core.util.coordinate.LockResult)
	 */
	public void releaseLock(LockResult lockResult) {
		if(lockResult!=null) {
		  CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockResult);
		}
	}
	
	/**
	 * 
	 * @see org.olat.repository.handlers.RepositoryHandler#isLocked(org.olat.core.id.OLATResourceable)
	 */
	public boolean isLocked(OLATResourceable ores) {
		return CoordinatorManager.getInstance().getCoordinator().getLocker().isLocked(ores, CourseFactory.COURSE_EDITOR_LOCK);
	}
	
	public WizardCloseResourceController createCloseResourceController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		return new WizardCloseCourseController(ureq, wControl, repositoryEntry);
	}
	
}
