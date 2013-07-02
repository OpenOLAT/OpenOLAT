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
package org.olat.admin.user.delete.service;

import java.io.File;
import java.io.FilenameFilter;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.taskExecutor.LongRunnable;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.ims.qti.editor.QTIEditorPackageImpl;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Initial date: 02.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeleteUserDataTask implements LongRunnable {
	private static final long serialVersionUID = 4278304131373256050L;

	private static final OLog log = Tracing.createLoggerFor(DeleteUserDataTask.class);

	private Long identityKey;
	private String newDeletedUserName;
	
	public DeleteUserDataTask(Long identityKey, String newDeletedUserName) {
		this.identityKey = identityKey;
		this.newDeletedUserName = newDeletedUserName;
	}
	
	@Override
	public void run() {
		long startTime = System.currentTimeMillis();
		Identity identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		deleteAllDropboxReturnboxFilesOf(identity);
		deleteHomesMetaDataOf(identity, newDeletedUserName);
		deleteAllTempQtiEditorFilesOf(identity);
		log.info("Finished UserFileDeletionManager thread for identity=" + identity + " in " + (System.currentTimeMillis() - startTime) + " (ms)");
	}
	
	private void deleteAllTempQtiEditorFilesOf(Identity identity) {
		// Temp QTI-editor File path e.g. /usr/local/olatfs/olat/olatdata/tmp/qtieditor/schuessler
		File userTempQtiEditorDir = new File(QTIEditorPackageImpl.getQTIEditorBaseDir(),identity.getName());
		if (userTempQtiEditorDir.exists()) {
			FileUtils.deleteDirsAndFiles(userTempQtiEditorDir, true, true); 
			log.audit("User-Deletion: identity=" + identity.getName() +" : QTI editor temp files deleted under dir=" + userTempQtiEditorDir.getAbsolutePath());
		}
	}

	private void deleteHomesMetaDataOf(Identity identity, String newDeletedUserName) {
		final boolean debug = log.isDebug();
		String metaRootDirPath = FolderConfig.getCanonicalMetaRoot();
		File metaRootDir = new File(metaRootDirPath);
		File[] homesDirs = metaRootDir.listFiles( new UserFileFilter(FolderConfig.getUserHomes().substring(1)) );
		if ( (homesDirs != null) && (homesDirs.length == 1) ) {
			File[] userDirs = homesDirs[0].listFiles( new UserFileFilter(identity.getName()) );
			// userDirs can contain only home-dir of deleted user 
			if (userDirs.length > 0) {
				if (debug) log.debug("deleteHomesMetaDataOf: Delete meta-data homes/" + identity.getName() + " dir, process file=" + userDirs[0].getAbsolutePath());
				// the meta-data under home/<USER> can be deleted and must not be renamed
				FileUtils.deleteDirsAndFiles(userDirs[0], true, true); 			
				log.audit("User-Deletion: Delete meta-data homes directory for identity=" + identity.getName()+ " directory=" + userDirs[0].getAbsolutePath());
			} else {
				log.debug("deleteHomesMetaDataOf: Found no '" + identity.getName() + "' directory at directory=" + homesDirs[0].getAbsolutePath());
			}
		}
	}

	/**
	 * Delete all 'dropboxes' or 'returnboxes' directories for certain user in the course-file structure.
	 * 
	 * @param identity
	 */
	private void deleteAllDropboxReturnboxFilesOf(Identity identity) {
		final boolean debug = log.isDebug();
		
		File courseBaseDir = getCourseBaseContainer();
		// loop over all courses path e.g. olatdata\bcroot\course\78931391428316\dropboxes\78933379704296\deltest 
		//                                                                       ^^^^^^^^^ dirTypeName
		File[] courseDirs = courseBaseDir.listFiles();
		// 1. loop over all course-id e.g. 78931391428316
		for (int courseIndex = 0; courseIndex < courseDirs.length; courseIndex++) {
			if (debug) log.debug("process dir=" + courseDirs[courseIndex].getAbsolutePath());
			String currentCourseId = courseDirs[courseIndex].getName();
			if (debug) log.debug("currentCourseId=" + currentCourseId);
			if (courseDirs[courseIndex].isDirectory()) {
				File[] dropboxReturnboxDirs = courseDirs[courseIndex].listFiles(new DropboxReturnboxFilter());
				// 2. loop over all dropbox and returnbox  in course-folder
				for (int dropboxIndex = 0; dropboxIndex < dropboxReturnboxDirs.length; dropboxIndex++) {
					File[] nodeDirs = dropboxReturnboxDirs[dropboxIndex].listFiles();
					// 3. loop over all node-id e.g. 78933379704296
					for (int nodeIndex = 0; nodeIndex < nodeDirs.length; nodeIndex++) {
						if (debug)  log.debug("process dir=" + nodeDirs[nodeIndex].getAbsolutePath());
						String currentNodeId =  nodeDirs[nodeIndex].getName();
						if (debug) log.debug("currentNodeId=" + currentNodeId);
						ICourse currentCourse = null;
						try {
							Long resId = Long.parseLong(currentCourseId);
							//check if the course exists
							OLATResource resource = OLATResourceManager.getInstance().findResourceable(resId, "CourseModule");
							if(resource != null) {
								currentCourse = CourseFactory.loadCourse(resId);
							} else {
								log.warn("course with resid=" + currentCourseId + " has a folder but no resource/repository entry", null);
							}
						} catch (Exception e) {
							log.error("could not load course with resid="+currentCourseId,e);
						}
						if (currentCourse != null) {
							if (isTaskNode(currentCourse, currentNodeId)) {
								if (debug) log.debug("found TACourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
								deleteUserDirectory(identity, nodeDirs[nodeIndex]);
							} else if (isProjectBrokerNode(currentCourse, currentNodeId)) {
								if (debug) log.debug("found ProjectBrokerCourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
								// addional loop over project-id
								File[] projectDirs = nodeDirs[nodeIndex].listFiles();
								for (int projectIndex = 0; projectIndex < projectDirs.length; projectIndex++) {
									deleteUserDirectory(identity, projectDirs[projectIndex]);
								}
							} else {
								log.warn("found dropbox or returnbox and node-type is NO Task- or ProjectBroker-Type courseId=" + currentCourseId + " nodeId=" + currentNodeId, null);
							}
						}
					}
				}
			}
		}
	}
	
	private boolean isProjectBrokerNode(ICourse currentCourse, String currentNodeId) {
		return currentCourse.getRunStructure().getNode(currentNodeId) instanceof ProjectBrokerCourseNode;
	}

	private boolean isTaskNode(ICourse currentCourse, String currentNodeId) {
		return currentCourse.getRunStructure().getNode(currentNodeId) instanceof TACourseNode;
	}

	private void deleteUserDirectory(Identity identity, File directory) {
		File[] userDirs = directory.listFiles( new UserFileFilter(identity.getName()) );
		// 4. loop over all user-dir e.g. deltest (only once)
		if (userDirs.length > 0) {
			if (log.isDebug()) log.debug("process dir=" + userDirs[0].getAbsolutePath());
			// ok found a directory of a user => delete it
			FileUtils.deleteDirsAndFiles(userDirs[0], true, true); 
			log.audit("User-Deletion: identity=" + identity.getName() +" : User file data deleted under dir=" + userDirs[0].getAbsolutePath());
			if (userDirs.length > 1) log.error("Found more than one sub-dir for user=" + identity.getName() + " path=" + userDirs[0].getAbsolutePath(), null);
		}
	}
	
	/**
	 * 
	 * @return e.g. olatdata\bcroot\course\
	 */
	private File getCourseBaseContainer() {
		OlatRootFolderImpl courseRootContainer = new OlatRootFolderImpl(File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME + File.separator, null);
		return courseRootContainer.getBasefile(); 
	}
	
	private static class DropboxReturnboxFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			// don't add overlayLocales as selectable availableLanguages
			// (LocaleStrings_de__VENDOR.properties)
			if (   name.equals(ReturnboxController.RETURNBOX_DIR_NAME) 
					|| name.equals(DropboxController.DROPBOX_DIR_NAME)) { 
				return true; 
			} else {
				return false;
			}
		}
	}
}
