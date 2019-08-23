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
package org.olat.course.nodes;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.pf.manager.FileSystemExport;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.ui.PFEditController;
import org.olat.course.nodes.pf.ui.PFPeekviewController;
import org.olat.course.nodes.pf.ui.PFPreviewController;
import org.olat.course.nodes.pf.ui.PFRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

public class PFCourseNode extends AbstractAccessableCourseNode {
	
	public static final String TYPE = "pf";
	
	public static final String CONFIG_KEY_PARTICIPANTBOX = "participantbox";
	public static final String CONFIG_KEY_COACHBOX = "coachbox";
	public static final String CONFIG_KEY_ALTERFILE = "alterfile";
	public static final String CONFIG_KEY_LIMITCOUNT = "limitcount";
	public static final String CONFIG_KEY_FILECOUNT = "filecount";
	public static final String CONFIG_KEY_TIMEFRAME = "timeframe";
	public static final String CONFIG_KEY_DATESTART = "datestart";
	public static final String CONFIG_KEY_DATEEND = "dateend";

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;

	public PFCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#updateModuleConfigDefaults(boolean)
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		if (isNewNode) {
			// default is to enable both boxes without restrictions
			updateModuleConfig(true,true,true, false, 0, false, null, null);
		}
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	
	public void updateModuleConfig(boolean participantbox, boolean coachbox, boolean alterfile,
			boolean limitcount,	int filecount, boolean timeframe, Date start, Date end) {
		ModuleConfiguration config = getModuleConfiguration();
		
		config.setBooleanEntry(CONFIG_KEY_PARTICIPANTBOX, participantbox);
		config.setBooleanEntry(CONFIG_KEY_COACHBOX, coachbox);
		config.setBooleanEntry(CONFIG_KEY_ALTERFILE, alterfile);
		config.setBooleanEntry(CONFIG_KEY_LIMITCOUNT, limitcount);
		if (limitcount){
			config.set(CONFIG_KEY_FILECOUNT, filecount);
		}
		
		config.setBooleanEntry(CONFIG_KEY_TIMEFRAME, timeframe);
		if (timeframe){
			config.set(CONFIG_KEY_DATESTART, start);
			config.set(CONFIG_KEY_DATEEND, end);	
		}
	}
	
	public boolean hasParticipantBoxConfigured() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
	}
	
	public boolean hasCoachBoxConfigured() {
		return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_COACHBOX);
	}
	
	public boolean hasAlterFileConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_ALTERFILE);
		}
		return false;
	}
	
	public boolean hasLimitCountConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_LIMITCOUNT);
		}
		return false;
	}
	
	public boolean isGreaterOrEqualToLimit (int count) {
		ModuleConfiguration config = getModuleConfiguration();
		int limit = config.getBooleanEntry(CONFIG_KEY_FILECOUNT) != null ? 
				(int) config.get(CONFIG_KEY_FILECOUNT) : 0;				
		return count >= limit;			
	}
	
	public boolean hasDropboxTimeFrameConfigured() {
		boolean hasStundentBox = getModuleConfiguration().getBooleanSafe(CONFIG_KEY_PARTICIPANTBOX);
		if (hasStundentBox) {
			return getModuleConfiguration().getBooleanSafe(CONFIG_KEY_TIMEFRAME);
		}
		return false;
	}
	
	public boolean isInDropboxTimeFrame () {
		ModuleConfiguration config = getModuleConfiguration();
		Date start = config.getBooleanEntry(CONFIG_KEY_DATESTART) != null ? 
				(Date) config.getDateValue(CONFIG_KEY_DATESTART) : new Date();
		Date end = config.getBooleanEntry(CONFIG_KEY_DATEEND) != null ? 
				(Date) config.getDateValue(CONFIG_KEY_DATEEND) : new Date();
		Date current = new Date();
		
		return start.before(current) && end.after(current);		
	}
	
	
	public int getLimitCount() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_FILECOUNT) != null ? 
				(int) config.get(CONFIG_KEY_FILECOUNT) : 0;
	}
	
	public Date getDateStart() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_DATESTART) != null ? 
				(Date) config.getDateValue(CONFIG_KEY_DATESTART) : new Date();
	}
	
	public Date getDateEnd() {
		ModuleConfiguration config = getModuleConfiguration();
		return config.getBooleanEntry(CONFIG_KEY_DATEEND) != null ? 
				(Date) config.getDateValue(CONFIG_KEY_DATEEND) : new Date();
	}
	

	@Override
	public StatusDescription isConfigValid() {
		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = hasCoachBoxConfigured() || hasParticipantBoxConfigured();
		if (!isValid) {
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { this.getShortTitle() };
			@SuppressWarnings("deprecation")
			String translPackage = Util.getPackageName(PFEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(CONFIG_KEY_PARTICIPANTBOX);
		}
		return sd;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		PFEditController ordnerCtr = new PFEditController(ureq, wControl, this, course, euce);
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, euce, ordnerCtr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		PFRunController runController = new PFRunController(ureq, wControl, this, userCourseEnv);
		return runController.createNodeRunConstructionResult(ureq);	
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		VFSContainer rootFolder = null;
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
		Path folderRelPath = null;
		LocalFolderImpl baseContainer = courseEnv.getCourseBaseContainer();
		PFManager pfManager = CoreSpringFactory.getImpl(PFManager.class);
		if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			folderRelPath = Paths.get(baseContainer.getBasefile().toPath().toString(), 
					PFManager.FILENAME_PARTICIPANTFOLDER, getIdent());
			rootFolder = new LocalFolderImpl(folderRelPath.toFile());
		} else if (userCourseEnv.isParticipant()) {
			folderRelPath = Paths.get(baseContainer.getBasefile().toPath().toString(), 
					PFManager.FILENAME_PARTICIPANTFOLDER, getIdent(), 
					pfManager.getIdFolderName(identity));
			rootFolder = new LocalFolderImpl(folderRelPath.toFile());
		}
		
		if (rootFolder == null) {
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
		} else {
			return new PFPeekviewController(ureq, wControl, rootFolder, getIdent(), 4);
		}
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return new PFPreviewController(ureq, wControl, this, userCourseEnv);

	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// mark the subscription to this node as deleted
		SubscriptionContext folderSubContext = CourseModule.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		NotificationsManager.getInstance().delete(folderSubContext);
		// delete filesystem
		
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		File root = Paths.get(courseEnv.getCourseBaseContainer().getRelPath(), 
				PFManager.FILENAME_PARTICIPANTFOLDER, getIdent()).toFile();
		if (root.exists()){
			FileUtils.deleteDirsAndFiles(root, true, true);		
		} 
	}
	
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		Path sourceFolder = Paths.get(courseEnv.getCourseBaseContainer().getBasefile().getAbsolutePath(),
				PFManager.FILENAME_PARTICIPANTFOLDER, getIdent()); 
		Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
		return FileSystemExport.fsToZip(exportStream, archivePath, sourceFolder, this, null, translator);		
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		return new StatusDescription[]{StatusDescription.NOERROR};
	}

}
