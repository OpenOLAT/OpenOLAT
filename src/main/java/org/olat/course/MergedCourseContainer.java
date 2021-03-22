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

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.config.CourseConfig;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.folder.MergedCourseElementDataContainer;
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
	
	private final Long courseId;
	private boolean courseReadOnly = false;
	private boolean overrideReadOnly = false;
	private final IdentityEnvironment identityEnv;
	private final CourseContainerOptions options;
	
	public MergedCourseContainer(Long courseId, String name) {
		this(courseId, name, null, CourseContainerOptions.all(), false);
	}
	
	public MergedCourseContainer(Long courseId, String name, IdentityEnvironment identityEnv) {
		this(courseId, name, identityEnv, CourseContainerOptions.all(), false);
	}
	
	public MergedCourseContainer(Long courseId, String name, IdentityEnvironment identityEnv, CourseContainerOptions options, boolean overrideReadOnly) {
		super(null, name);
		this.options = options;
		this.courseId = courseId;
		this.identityEnv = identityEnv;
		this.overrideReadOnly = overrideReadOnly;
	}
	
	@Override
	protected void init() {
		ICourse course = CourseFactory.loadCourse(courseId);
		if(course instanceof PersistingCourseImpl) {
			init((PersistingCourseImpl)course);
		}
	}
	
	protected void init(PersistingCourseImpl persistingCourse) {
		super.init();
		
		RepositoryEntry courseRe = persistingCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		courseReadOnly = !overrideReadOnly && courseRe.getEntryStatus().decommissioned();
		if(courseReadOnly) {
			setLocalSecurityCallback(new ReadOnlyCallback());
		}
		
		if(options.withCourseFolder()) {
			if(identityEnv == null) {
				VFSContainer courseContainer = persistingCourse.getIsolatedCourseFolder();
				if(courseReadOnly) {
					courseContainer.setLocalSecurityCallback(new ReadOnlyCallback());
				}
				addContainersChildren(courseContainer, true);
			} else {
				RepositoryEntrySecurity reSecurity = RepositoryManager.getInstance()
						.isAllowed(identityEnv.getIdentity(), identityEnv.getRoles(), courseRe);
				if(reSecurity.isEntryAdmin()) {
					VFSContainer courseContainer = persistingCourse.getIsolatedCourseFolder();
					if(courseReadOnly) {
						courseContainer.setLocalSecurityCallback(new ReadOnlyCallback());
					}
					addContainersChildren(courseContainer, true);
				}
			}
		}
		
		if(options.withSharedResource()) {
			initSharedFolder(persistingCourse);
		}
		
		if(options.withCourseDocuments()) {
			initCourseDocuments(persistingCourse);
		}
			
		// add all course building blocks of type BC to a virtual folder
		if(options.withCourseElements()) {
			MergedCourseElementDataContainer nodesContainer = new MergedCourseElementDataContainer(courseId, identityEnv);
			if (!nodesContainer.isEmpty()) {
				addContainer(nodesContainer);
			}
		}
	}

	/**
	 * Grab any shared folder that is configured, but only when in unchecked
	 * security mode (no identity environment) or when the user has course
	 * admin rights
	 * 
	 * @param persistingCourse
	 */
	private void initSharedFolder(PersistingCourseImpl persistingCourse) {
		CourseConfig courseConfig = persistingCourse.getCourseConfig();
		String sfSoftkey = courseConfig.getSharedFolderSoftkey();
		if (StringHelper.containsNonWhitespace(sfSoftkey) && !CourseConfig.VALUE_EMPTY_SHAREDFOLDER_SOFTKEY.equals(sfSoftkey)) {
			RepositoryEntry re = persistingCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			
			if(identityEnv == null || repositoryService.hasRoleExpanded(identityEnv.getIdentity(), re,
					OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name(),
					GroupRoles.owner.name())) {
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
	}
	
	private void initCourseDocuments(PersistingCourseImpl persistingCourse) {
		if (identityEnv == null) return;
		if (!persistingCourse.getCourseConfig().isDocumentsEnabled()) return;
		
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, persistingCourse.getCourseEnvironment());
		VFSContainer documentsContainer = CourseDocumentsFactory.getFileContainer(persistingCourse.getCourseEnvironment());
		if (documentsContainer != null) {
			VFSSecurityCallback securityCallback = CourseDocumentsFactory.getSecurityCallback(userCourseEnv);
			documentsContainer.setLocalSecurityCallback(securityCallback);
			addContainer(new NamedContainerImpl("_documents", documentsContainer));
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
