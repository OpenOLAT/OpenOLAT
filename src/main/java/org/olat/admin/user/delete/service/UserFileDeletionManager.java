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
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
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
import org.olat.user.UserDataDeletable;


/**
 * Manager to delete all files for user-deletion workflow.
 * 
 * @author Christian Guretzki  
 */
public class UserFileDeletionManager extends BasicManager implements UserDataDeletable{

	private TaskExecutorManager taskExecutorManager;


	/**
	 * [spring]
	 * @param userDeletionManager
	 */
	private UserFileDeletionManager() {
		//
	}

	/**
	 * [spring]
	 * @param taskExecutorManager
	 */
	public void setTaskExecutorManager(TaskExecutorManager taskExecutorManager) {
		this.taskExecutorManager = taskExecutorManager;
	}
	
	public static final AtomicInteger numberOfRunningFileDeletionThreads = new AtomicInteger(); 

  public void deleteUserData(final Identity identity, final String newDeletedUserName) {
  	numberOfRunningFileDeletionThreads.incrementAndGet();
  	logDebug("deleteUserData numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
  	taskExecutorManager.runTask(new Runnable() {

    	@Override
    	public void run() {
      	logDebug("run start numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
    		long startTime = 0 ;
    		logInfo("Start UserFileDeletionManager thread for identity=" + identity);
    		if (isLogDebugEnabled()) startTime = System.currentTimeMillis();
    		deleteAllDropboxReturnboxFilesOf(identity);
    		if (isLogDebugEnabled()) {
    			logDebug("User-Deletion: deleteAllDropboxFiles takes " + (System.currentTimeMillis() - startTime) + "ms");
    			startTime = System.currentTimeMillis();
    		}
    		deleteHomesMetaDataOf(identity, newDeletedUserName);
    		if (isLogDebugEnabled()) {
    			logDebug("User-Deletion: renameAllFileMetadata takes " + (System.currentTimeMillis() - startTime) + "ms");
    			startTime = System.currentTimeMillis();
    		}
    		deleteAllTempQtiEditorFilesOf(identity);
    		if (isLogDebugEnabled()) logDebug("User-Deletion: deleteAllTempQtiEditorFiles takes " + (System.currentTimeMillis() - startTime) + "ms");
    		logInfo("Finished UserFileDeletionManager thread for identity=" + identity);
    		UserFileDeletionManager.numberOfRunningFileDeletionThreads.decrementAndGet();
    		logDebug("run end numberOfRunningFileDeletionThreads.get()=" + numberOfRunningFileDeletionThreads.get());
   	}
    });
  }
  
  /**
   * 
   * @return true if no deletion-thread is running
   */
  public static boolean isReadyToDelete() {
  	return numberOfRunningFileDeletionThreads.get() == 0;
  }

	private void deleteAllTempQtiEditorFilesOf(Identity identity) {
		// Temp QTI-editor File path e.g. /usr/local/olatfs/olat/olatdata/tmp/qtieditor/schuessler
		File userTempQtiEditorDir = new File(QTIEditorPackageImpl.getQTIEditorBaseDir(),identity.getName());
		if (userTempQtiEditorDir.exists()) {
			FileUtils.deleteDirsAndFiles(userTempQtiEditorDir, true, true); 
			logAudit("User-Deletion: identity=" + identity.getName() +" : QTI editor temp files deleted under dir=" + userTempQtiEditorDir.getAbsolutePath());
		}
	}

	private void deleteHomesMetaDataOf(Identity identity, String newDeletedUserName) {
		String metaRootDirPath = FolderConfig.getCanonicalMetaRoot();
		File metaRootDir = new File(metaRootDirPath);
		File[] homesDirs = metaRootDir.listFiles( new UserFileFilter(FolderConfig.getUserHomes().substring(1)) );
		if ( (homesDirs != null) && (homesDirs.length == 1) ) {
			File[] userDirs = homesDirs[0].listFiles( new UserFileFilter(identity.getName()) );
			// userDirs can contain only home-dir of deleted user 
			if (userDirs.length > 0) {
				if (isLogDebugEnabled()) logDebug("deleteHomesMetaDataOf: Delete meta-data homes/" + identity.getName() + " dir, process file=" + userDirs[0].getAbsolutePath());
				// the meta-data under home/<USER> can be deleted and must not be renamed
				FileUtils.deleteDirsAndFiles(userDirs[0], true, true); 			
				logAudit("User-Deletion: Delete meta-data homes directory for identity=" + identity.getName()+ " directory=" + userDirs[0].getAbsolutePath());
			} else {
				logDebug("deleteHomesMetaDataOf: Found no '" + identity.getName() + "' directory at directory=" + homesDirs[0].getAbsolutePath());
			}
		}
	}

	/**
	 * Delete all 'dropboxes' or 'returnboxes' directories for certain user in the course-file structure.
	 * 
	 * @param identity
	 */
	private void deleteAllDropboxReturnboxFilesOf(Identity identity) {
		File courseBaseDir = getCourseBaseContainer();
		// loop over all courses path e.g. olatdata\bcroot\course\78931391428316\dropboxes\78933379704296\deltest 
		//                                                                       ^^^^^^^^^ dirTypeName
		File[] courseDirs = courseBaseDir.listFiles();
		// 1. loop over all course-id e.g. 78931391428316
		for (int courseIndex = 0; courseIndex < courseDirs.length; courseIndex++) {
			if (isLogDebugEnabled()) logDebug("process dir=" + courseDirs[courseIndex].getAbsolutePath());
			String currentCourseId = courseDirs[courseIndex].getName();
			if (isLogDebugEnabled()) logDebug("currentCourseId=" + currentCourseId);
			if (courseDirs[courseIndex].isDirectory()) {
				File[] dropboxReturnboxDirs = courseDirs[courseIndex].listFiles(dropboxReturnboxFilter);
				// 2. loop over all dropbox and returnbox  in course-folder
				for (int dropboxIndex = 0; dropboxIndex < dropboxReturnboxDirs.length; dropboxIndex++) {
					File[] nodeDirs = dropboxReturnboxDirs[dropboxIndex].listFiles();
					// 3. loop over all node-id e.g. 78933379704296
					for (int nodeIndex = 0; nodeIndex < nodeDirs.length; nodeIndex++) {
						if (isLogDebugEnabled())  logDebug("process dir=" + nodeDirs[nodeIndex].getAbsolutePath());
						String currentNodeId =  nodeDirs[nodeIndex].getName();
						if (isLogDebugEnabled()) logDebug("currentNodeId=" + currentNodeId);
						ICourse currentCourse = null;
						try {
							Long resId = Long.parseLong(currentCourseId);
							//check if the course exists
							OLATResource resource = OLATResourceManager.getInstance().findResourceable(resId, "CourseModule");
							if(resource != null) {
								currentCourse = CourseFactory.loadCourse(resId);
							} else {
								logWarn("course with resid=" + currentCourseId + " has a folder but no resource/repository entry", null);
							}
						} catch (Exception e) {
							logError("could not load course with resid="+currentCourseId,e);
						}
						if (currentCourse != null) {
							if (isTaskNode(currentCourse, currentNodeId)) {
								if (isLogDebugEnabled()) logDebug("found TACourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
								deleteUserDirectory(identity, nodeDirs[nodeIndex]);
							} else if (isProjectBrokerNode(currentCourse, currentNodeId)) {
								if (isLogDebugEnabled()) logDebug("found ProjectBrokerCourseNode path=" + nodeDirs[nodeIndex].getAbsolutePath());
								// addional loop over project-id
								File[] projectDirs = nodeDirs[nodeIndex].listFiles();
								for (int projectIndex = 0; projectIndex < projectDirs.length; projectIndex++) {
									deleteUserDirectory(identity, projectDirs[projectIndex]);
								}
							} else {
								logWarn("found dropbox or returnbox and node-type is NO Task- or ProjectBroker-Type courseId=" + currentCourseId + " nodeId=" + currentNodeId, null);
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
			if (isLogDebugEnabled()) logDebug("process dir=" + userDirs[0].getAbsolutePath());
			// ok found a directory of a user => delete it
			FileUtils.deleteDirsAndFiles(userDirs[0], true, true); 
			logAudit("User-Deletion: identity=" + identity.getName() +" : User file data deleted under dir=" + userDirs[0].getAbsolutePath());
			if (userDirs.length > 1) logError("Found more than one sub-dir for user=" + identity.getName() + " path=" + userDirs[0].getAbsolutePath(), null);
		}
	}

	private static FilenameFilter dropboxReturnboxFilter = new FilenameFilter() {
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
	};

	/**
	 * 
	 * @return e.g. olatdata\bcroot\course\
	 */
	private File getCourseBaseContainer() {
		OlatRootFolderImpl courseRootContainer = new OlatRootFolderImpl(File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME + File.separator, null);
		return courseRootContainer.getBasefile(); 
    }

}
	
class UserFileFilter implements FilenameFilter  {
	
	private String username;

	UserFileFilter(String username) {
		this.username = username;
	}

	@Override
	public boolean accept(File dir, String name) {
		// don't add overlayLocales as selectable availableLanguages
		// (LocaleStrings_de__VENDOR.properties)
		if (   name.equals(username) ) { 
			return true; 
		} else {
			return false;
		}
	}
}

