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
package org.olat.course;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSStatus;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.archiver.webdav.CourseArchiveWebDAVSource;
import org.olat.course.config.CourseConfig;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.folder.MergedCourseElementDataContainer;
import org.olat.course.nodes.bc.CoachFolderFactory;
import org.olat.course.nodes.bc.CourseDocumentsFactory;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.sharedfolder.SharedFolderManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;

/**
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MergedCourseContainer extends MergeSource {
	
	private static final Logger log = Tracing.createLoggerFor(MergedCourseContainer.class);
	
	private final Boolean entryAdmin;
	private final boolean courseReadOnly;
	private final RepositoryEntry courseEntry;
	private final IdentityEnvironment identityEnv;
	private final CourseContainerOptions options;
	
	private boolean initialized;
	
	public MergedCourseContainer(RepositoryEntry courseEntry, String name, IdentityEnvironment identityEnv,
			Boolean entryAdmin, CourseContainerOptions options, boolean overrideReadOnly) {
		super(null, name);
		this.options = options;
		this.courseEntry = courseEntry;
		this.entryAdmin = entryAdmin;
		this.identityEnv = identityEnv;
		courseReadOnly = !overrideReadOnly && courseEntry.getEntryStatus().decommissioned();
	}
	
	@Override
	public VFSStatus canWrite() {
		boolean writable = options.withCourseFolder() && entryAdmin && !courseReadOnly;
		return writable ? VFSStatus.YES : VFSStatus.NO;
	}
	
	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			init();
		}
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		if(!initialized) {
			init();
		}
		return super.resolve(path);
	}
	
	@Override
	public VFSContainer createChildContainer(String name) {
		if(!initialized) {
			init();
		}
		return super.createChildContainer(name);
	}
	
	@Override
	public VFSLeaf createChildLeaf(String name) {
		if(!initialized) {
			init();
		}
		return super.createChildLeaf(name);
	}
	
	@Override
	public VFSContainer getRootWriteContainer() {
		if(!initialized) {
			init();
		}
		return super.getRootWriteContainer();
	}

	@Override
	protected void init() {
		initialized = true;
		super.init();
		
		try {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			if(course instanceof PersistingCourseImpl persistedCourse) {
				init(persistedCourse);
			}
		} catch (CorruptedCourseException e) {
			log.warn("Error loading course: {}", e.getMessage());
		}
	}
	
	private void init(PersistingCourseImpl persistingCourse) {
		if(courseReadOnly) {
			setLocalSecurityCallback(new ReadOnlyCallback());
		}
		
		boolean isAdmin;
		RepositoryEntrySecurity reSecurity = null;
		if(entryAdmin != null) {
			isAdmin = entryAdmin.booleanValue();
		} else if(identityEnv != null) {
			reSecurity = RepositoryManager.getInstance()
					.isAllowed(identityEnv.getIdentity(), identityEnv.getRoles(), courseEntry);
			isAdmin = reSecurity.isEntryAdmin();
		} else {
			isAdmin = false;
		}

		if(options.withCourseFolder() && isAdmin) {
			VFSContainer courseContainer = persistingCourse.getIsolatedCourseFolder();
			if(courseReadOnly) {
				courseContainer.setLocalSecurityCallback(new ReadOnlyCallback());
			}
			addContainersChildren(courseContainer, true);
		}
		
		if(options.withSharedResource()) {
			initSharedFolder(persistingCourse, isAdmin);
		}
		
		if(options.withCourseDocuments()) {
			initCourseDocuments(persistingCourse, isAdmin);
		}
		
		if(options.withCoachFolder()) {
			initCoachFolder(persistingCourse, reSecurity, isAdmin);
		}
			
		// add all course building blocks of type BC to a virtual folder
		if(options.withCourseElements()) {
			MergedCourseElementDataContainer nodesContainer = new MergedCourseElementDataContainer(courseEntry, identityEnv, courseReadOnly, isAdmin);
			if (!nodesContainer.isEmpty()) {
				addContainer(nodesContainer);
			}
		}

		if(options.withArchives() && identityEnv != null && isAdmin) {
			CourseArchiveWebDAVSource archivesContainer = new CourseArchiveWebDAVSource(courseEntry, identityEnv);
			addContainer(archivesContainer);
		}
	}

	/**
	 * Grab any shared folder that is configured, but only when in unchecked
	 * security mode (no identity environment) or when the user has course
	 * admin rights
	 * 
	 * @param persistingCourse
	 */
	private void initSharedFolder(PersistingCourseImpl persistingCourse, boolean isAdmin) {
		CourseConfig courseConfig = persistingCourse.getCourseConfig();
		String sfSoftkey = courseConfig.getSharedFolderSoftkey();
		if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey) && isAdmin) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			OLATResource sharedResource = repositoryService.loadRepositoryEntryResourceBySoftKey(sfSoftkey);
			if (sharedResource != null) {
				VFSContainer sharedFolder = SharedFolderManager.getInstance().getSharedFolder(sharedResource);
				if (sharedFolder != null) {
					if(courseConfig.isSharedFolderReadOnlyMount() || courseReadOnly) {
						sharedFolder.setLocalSecurityCallback(new ReadOnlyCallback());
					}
					//add local course folder's children as read/write source and any sharedfolder as subfolder
					addContainer(new NamedContainerImpl("_sharedfolder", sharedFolder));
				}
			}
		}
	}
	
	private void initCourseDocuments(PersistingCourseImpl persistingCourse, boolean isAdmin) {
		if (!persistingCourse.getCourseConfig().isDocumentsEnabled()) return;
		// Don't add a linked resource folder
		if (StringHelper.containsNonWhitespace(persistingCourse.getCourseConfig().getDocumentsPath())) return;
		
		VFSContainer documentsContainer = CourseDocumentsFactory.getFileContainer(persistingCourse.getCourseBaseContainer());
		if (documentsContainer != null) {
			VFSSecurityCallback securityCallback = null;
			if (isAdmin) {
				if (courseReadOnly) {
					securityCallback = CourseDocumentsFactory.createReadOnlyCallback(null);
				} else {
					securityCallback = CourseDocumentsFactory.createReadWriteCallback(null,
							CourseDocumentsFactory.getFileDirectory(persistingCourse.getCourseBaseContainer()));
				}
			} else if(identityEnv != null) {
				UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
				securityCallback = CourseDocumentsFactory.getSecurityCallback(userCourseEnv);
			}
			
			if(securityCallback != null) {
				documentsContainer.setLocalSecurityCallback(securityCallback);
				addContainer(new NamedContainerImpl("_documents", documentsContainer));
			}
		}
	}
	
	private void initCoachFolder(PersistingCourseImpl persistingCourse, RepositoryEntrySecurity reSecurity, boolean isAdmin) {
		if (!persistingCourse.getCourseConfig().isCoachFolderEnabled()) return;
		// Don't add a linked resource folder
		if (StringHelper.containsNonWhitespace(persistingCourse.getCourseConfig().getCoachFolderPath())) return;
		
		VFSContainer documentsContainer = CoachFolderFactory.getFileContainer(persistingCourse.getCourseBaseContainer());
		if (documentsContainer != null) {
			VFSSecurityCallback securityCallback = null;
			if (isAdmin) {
				if (courseReadOnly) {
					securityCallback = CoachFolderFactory.createReadOnlyCallback(null);
				} else {
					securityCallback = CoachFolderFactory.createReadWriteCallback(null,
							CoachFolderFactory.getFileDirectory(persistingCourse.getCourseBaseContainer()));
				}
			} else {
				if(reSecurity == null && identityEnv != null) {
					reSecurity = RepositoryManager.getInstance()
							.isAllowed(identityEnv.getIdentity(), identityEnv.getRoles(), courseEntry);
				}
				
				if(reSecurity != null && reSecurity.isCoach()) {
					UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
					securityCallback = CoachFolderFactory.getSecurityCallback(userCourseEnv);
				} else {
					// Do not show the folder to participants
					return;
				}
			}
			documentsContainer.setLocalSecurityCallback(securityCallback);
			addContainer(new NamedContainerImpl("_coachdocuments", documentsContainer));
		}
	}

	private Object readResolve() {
		try {
			init();
			return this;
		} catch (Exception e) {
			log.error("Cannot init the merged container of a course after deserialization", e);
			return null;
		}
	}
}
