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
package org.olat.user.manager.lifecycle;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.CourseXStreamAliases;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.Structure;
import org.olat.course.assessment.manager.CourseAssessmentManagerImpl;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.ta.DropboxController;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataDelete;
import org.olat.user.UserDataDeleteManager;
import org.olat.user.manager.UserDataDeleteDAO;
import org.olat.user.model.UserData;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

/**
 * 
 * Initial date: 30 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class UserDataDeleteManagerImpl implements UserDataDeleteManager, UserDataDeletable, DisposableBean {
	
	private static final Logger log = Tracing.createLoggerFor(UserDataDeleteManagerImpl.class);
	
	private static final XStream configXstream = XStreamHelper.createXStreamInstance();
	static {
		Class<?>[] types = new Class[] { UserData.class };
		configXstream.addPermission(new ExplicitTypePermission(types));
		configXstream.alias("userData", UserData.class);
	}

	private boolean interrupt = false;
	private final Deque<Future<Boolean>> allFutures = new ArrayDeque<>();
	private final Deque<Future<Boolean>> resourcesFutures = new ArrayDeque<>();
	private final ExecutorService taskAllExecutor = Executors.newSingleThreadExecutor();
	private final ExecutorService taskResourcesExecutor = Executors.newSingleThreadExecutor();
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserDataDeleteDAO userDataDeleteDao;
	@Autowired
	private AssessmentEntryDAO assessmentEntryDao;
	
	@Override
	@SuppressWarnings("unchecked")
	public List<UserData> fromXML(String xml) {
		return (List<UserData>)configXstream.fromXML(xml);
	}

	@Override
	public String toXML(List<UserData> data) {
		return configXstream.toXML(data);
	}

	@Override
	public int deleteUserDataPriority() {
		return 950;// before repository
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		List<Long> resourceIds = assessmentEntryDao.loadResourceIds(identity);
		if(resourceIds.isEmpty()) {
			return;// nothing to delete
		}

		StringBuilder sb = new StringBuilder();
		for(Long resourceId:resourceIds) {
			if(sb.length() > 0) sb.append(",");
			sb.append(resourceId);
		}
		
		List<UserData> userDatas = new ArrayList<>();
		userDatas.add(new UserData(identity.getKey(), identity.getName()));
		String userDatasXml = toXML(userDatas);
		userDataDeleteDao.create(userDatasXml, sb.toString());
	}
	
	@Override
	public void interrupt() {
		interrupt = true;
	}
	
	@Override
	public void destroy() {
		interrupt();
		taskAllExecutor.shutdownNow();
		taskResourcesExecutor.shutdownNow();
	}

	private boolean hasFuture(Deque<Future<Boolean>> futures) {
		List<Future<Boolean>> futureList = new ArrayList<>(futures);
		for(Future<Boolean> f:futureList) {
			if(f.isCancelled() || f.isDone()) {
				futures.remove(f);
			}
		}
		return !futures.isEmpty();
	}

	@Override
	public boolean startAll() {
		boolean isWorking = hasFuture(allFutures);
		if(isWorking) {
			return false;
		}

		final List<UserDataDelete> userDatas = userDataDeleteDao.getWithoutResourceIds();
		if(!userDatas.isEmpty()) {
			Future<Boolean> f = taskAllExecutor.submit(() -> {
				
				try {
					WorkThreadInformations.set("Delete user data in all courses");
					long start = System.currentTimeMillis();
					List<UserDataDelete> deletionList = userDataDeleteDao.getWithoutResourceIds();
					dbInstance.commitAndCloseSession();
					
					File coursesBaseDir = getCoursesBaseContainer();
					List<UserData> identities = extractUserData(deletionList);
					String[] courseDirNames = coursesBaseDir.list(new ResourceFilenameFilter());
					String currentCourseDir = getCurrentCourseDir(deletionList);
					List<String> courseDirList = sortedList(courseDirNames, currentCourseDir);

					log.info("Delete user data in all courses ({}) for {} identities.", courseDirList.size(), identities.size());
					
					int counter = 0;
					for (String courseDirName:courseDirList) {
						if(interrupt) {
							return Boolean.FALSE;
						}
						deleteCoursesUserFilesOf(courseDirName, identities, coursesBaseDir);
						if(counter++ % 25 == 0) {
							userDataDeleteDao.updateCurrentCourseDir(deletionList, courseDirName);
							dbInstance.commitAndCloseSession();
						}
					}
					
					for(UserDataDelete data:deletionList) {
						userDataDeleteDao.deleteUserData(data);
					}
					dbInstance.commitAndCloseSession();
					
					long takes = (System.currentTimeMillis() - start) / 1000;
					log.info("Delete user data in all courses finished for {} courses and {} identities in {}s", courseDirList.size(), identities.size(), takes);
				} catch (Exception e) {
					log.error("", e);
				} finally {
					WorkThreadInformations.unset();
					dbInstance.commitAndCloseSession();
				}
				
				return Boolean.TRUE;
			});
			
			allFutures.add(f);
		}
		return !userDatas.isEmpty();
	}
	
	private String getCurrentCourseDir(List<UserDataDelete> deletionList) {
		List<String> currentCourseDirList = new ArrayList<>();
		for(UserDataDelete deleteData:deletionList) {
			String currentCourseDir = deleteData.getCurrentResourceId();
			if(StringHelper.containsNonWhitespace(currentCourseDir)) {
				currentCourseDirList.add(currentCourseDir);
			} else {
				return null;
			}
		}
		
		if(currentCourseDirList.isEmpty()) {
			return null;
		}
		Collections.sort(currentCourseDirList);
		return currentCourseDirList.get(0);
	}
	
	@Override
	public boolean startResources() {
		boolean isWorking = hasFuture(resourcesFutures);
		if(isWorking) {
			return false;
		}

		final List<UserDataDelete> userDatas = userDataDeleteDao.getWithResourceIds();
		if(!userDatas.isEmpty()) {
			Future<Boolean> f = taskResourcesExecutor.submit(() -> {

				try {
					WorkThreadInformations.set("Delete user data in some courses");
					long start = System.currentTimeMillis();
					log.info("Delete user data in all courses");
					
					List<UserDataDelete> deletionList = userDataDeleteDao.getWithResourceIds();
					dbInstance.commitAndCloseSession();
					
					File coursesBaseDir = getCoursesBaseContainer();
					List<UserData> identities = extractUserData(deletionList);
					String[] courseDirNames = coursesToScan(userDatas);
					List<String> courseDirList = sortedList(courseDirNames, null);

					log.info("Delete user data in {} courses for {} identities.", courseDirList.size(), identities.size());
					
					for (String courseDirName:courseDirList) {
						if(interrupt) {
							return Boolean.FALSE;
						}
						deleteCoursesUserFilesOf(courseDirName, identities, coursesBaseDir);
					}
					
					for(UserDataDelete data:deletionList) {
						userDataDeleteDao.deleteUserData(data);
					}
					dbInstance.commitAndCloseSession();
					
					long takes = (System.currentTimeMillis() - start) / 1000;
					log.info("Delete user data in {} courses finished for {} identities in {}s", courseDirList.size(), identities.size(), takes);
				} catch (Exception e) {
					log.error("", e);
				} finally {
					WorkThreadInformations.unset();
					dbInstance.commitAndCloseSession();
				}
				
				return Boolean.TRUE;
			});
			
			resourcesFutures.add(f);
		}
		return !userDatas.isEmpty();
	}
	
	private List<UserData> extractUserData(List<UserDataDelete> identitiesToDelete) {
		List<UserData> data = new ArrayList<>();
		for(UserDataDelete identityToDelete:identitiesToDelete) {
			List<UserData> d = fromXML(identityToDelete.getUserData());
			if(d != null && !d.isEmpty()) {
				data.addAll(d);
			}
		}
		return data;
	}
	
	private void deleteCoursesUserFilesOf(String courseDirName, List<UserData> identities, File coursesBaseDir) {
		if(!StringHelper.isLong(courseDirName)) return;

		File courseDir = new File(coursesBaseDir, courseDirName);
		if (courseDir.isDirectory()) {
			deleteAssessmentDocuments(identities, courseDir);
			deleteDropboxReturnbox(identities, courseDir);
			deleteParticipantFolder(identities, courseDir);
			deleteGTasks(identities, courseDir);
		}
	}
	
	private String[] coursesToScan(List<UserDataDelete> identities) {
		Set<String> resourceIds = new HashSet<>();
		for(UserDataDelete identity:identities) {
			String resourceIdsBulk = identity.getResourceIds();
			String[] resourceIdsArray = resourceIdsBulk.split("[,]");
			for(String resourceId:resourceIdsArray) {
				if(StringHelper.isLong(resourceId)) {
					resourceIds.add(resourceId);
				}	
			}
		}
		return resourceIds.toArray(new String[resourceIds.size()]);
	}
	
	private List<String> sortedList(String[] courseDirNames, String currentCourseDir) {
		List<String> list = new ArrayList<>(courseDirNames.length);
		Collections.addAll(list, courseDirNames);
		Collections.sort(list);
		
		if(StringHelper.containsNonWhitespace(currentCourseDir)) {
			int index = list.indexOf(currentCourseDir);
			if(index > 0) {
				List<String> truncatedList = list.subList(index, list.size());
				list = new ArrayList<>(truncatedList);
			}
		}
		
		return list;
	}
	
	private static final class ResourceFilenameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			return StringHelper.isLong(name);
		}
	}

	/**
	 * /coursedir/participantfolder/{node}/{identityKey}
	 * @param identity
	 * @param courseDir
	 */
	private void deleteParticipantFolder(List<UserData> identities, File courseDir) {
		File participantFoldersDir = new File(courseDir, PFManager.FILENAME_PARTICIPANTFOLDER);
		if(participantFoldersDir.exists()) {
			File[] nodeDirs = participantFoldersDir.listFiles(new SystemFileFilter(false, true));
			for(File nodeDir:nodeDirs) {
				for(UserData identity:identities) {
					deleteUserDirectoryBy(identity, identity.getIdentityKey().toString(), nodeDir);
				}
			}
		}
	}
	
	/**
	 * /coursedir/gtasks/{nodeId}/revisions/person_{identityKey}
	 * @param identity
	 * @param courseDir
	 */
	private void deleteGTasks(List<UserData> identities, File courseDir) {
		File gtasksDir = new File(courseDir, "gtasks");
		if(gtasksDir.exists()) {
			File[] nodeDirs = gtasksDir.listFiles(new SystemFileFilter(false, true));
			for(File nodeDir:nodeDirs) {
				File[] boxes = nodeDir.listFiles(new SystemFileFilter(false, true));
				for(File box:boxes) {
					for(UserData identity:identities) {
						deleteUserDirectoryBy(identity, "person_" + identity.getIdentityKey(), box);
					}
				}
			}
		}
	}

	private void deleteAssessmentDocuments(List<UserData> identities, File courseDir) {
		File assessmentDocsDir = new File(courseDir, CourseAssessmentManagerImpl.ASSESSMENT_DOCS_DIR);
		if(assessmentDocsDir.exists()) {
			File[] nodeDirs = assessmentDocsDir.listFiles(new SystemFileFilter(false, true));
			for(File nodeDir:nodeDirs) {
				for(UserData identity:identities) {
					deleteUserDirectoryBy(identity, "person_" + identity.getIdentityKey(), nodeDir);
				}
			}
		}
	}
	
	private void deleteDropboxReturnbox(List<UserData> identities, File courseDir) {
		File returnboxDir = new File(courseDir, ReturnboxController.RETURNBOX_DIR_NAME);
		File dropboxDir = new File(courseDir, DropboxController.DROPBOX_DIR_NAME);
		if(returnboxDir.exists() || dropboxDir.exists()) {
			Structure currentCourse = null;
			File[] boxDirs = new File[]{ returnboxDir, dropboxDir};
			for (File boxDir: boxDirs) {
				if(!boxDir.exists()) {
					continue;
				}
				
				File[] nodeDirs = boxDir.listFiles(new SystemFileFilter(false, true));
				// 3. loop over all node-id e.g. 78933379704296
				for (File nodeDir:nodeDirs) {
					String currentNodeId = nodeDir.getName();
					if(currentCourse == null) {
						currentCourse = loadCourse(courseDir);
					}
					
					if(currentCourse == null) {
						return;//corrupted course
					} else if (isTaskNode(currentCourse, currentNodeId)) {
						for(UserData identity: identities) {
							deleteUserDirectoryByName(identity, nodeDir);
						}
					} else if (isProjectBrokerNode(currentCourse, currentNodeId)) {
						// additional loop over project-id
						File[] projectDirs = nodeDir.listFiles(new SystemFileFilter(false, true));
						for (File projectDir:projectDirs) {
							for(UserData identity: identities) {
								deleteUserDirectoryByName(identity, projectDir);
							}
						}
					}
				}
			}
		}
	}
	
	private Structure loadCourse(File courseDir) {
		try {
			File runStructureXml = new File(courseDir, PersistingCourseImpl.RUNSTRUCTURE_XML);
			Object obj = XStreamHelper.readObject(CourseXStreamAliases.getReadCourseXStream(), runStructureXml);
			if(obj instanceof Structure) {
				return (Structure)obj;
			} else {
				log.warn("course with resid={} has a folder but no resource/repository entry", courseDir.getName());
			}
		} catch (Exception e) {
			log.error("could not load course with resid={}", courseDir.getName(), e);
		}
		return null;
	}
	
	private boolean isProjectBrokerNode(Structure runStructure, String currentNodeId) {
		return runStructure.getNode(currentNodeId) instanceof ProjectBrokerCourseNode;
	}

	private boolean isTaskNode(Structure runStructure, String currentNodeId) {
		return runStructure.getNode(currentNodeId) instanceof TACourseNode;
	}

	private void deleteUserDirectoryByName(UserData identity, File directory) {
		File userDir = new File(directory, identity.getIdentityName());
		deleteUserDirectory(identity, userDir);
	}
	
	private void deleteUserDirectoryBy(UserData identity, String identifier, File directory) {
		File userDir = new File(directory, identifier);
		deleteUserDirectory(identity, userDir);
	}
	
	private void deleteUserDirectory(UserData identity, File userDir) {
		if (userDir.exists()) {
			// ok found a directory of a user => delete it
			FileUtils.deleteDirsAndFiles(userDir, true, true);
			log.info(Tracing.M_AUDIT, "User-Deletion: identity={} : User file data deleted under dir={}", identity.getIdentityKey(), userDir.getAbsolutePath());
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	/**
	 * 
	 * @return e.g. olatdata\bcroot\course\
	 */
	private File getCoursesBaseContainer() {
		LocalFolderImpl courseRootContainer = VFSManager.olatRootContainer(File.separator + PersistingCourseImpl.COURSE_ROOT_DIR_NAME + File.separator, null);
		return courseRootContainer.getBasefile(); 
	}

}
