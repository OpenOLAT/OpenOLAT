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
package org.olat.course.folder;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.MergeSource;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;

/**
 * 
 * This container lazy load the participant folders.
 * 
 * Initial date: 29 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MergedPFCourseNodeContainer extends MergeSource {

	private boolean initialized = false;
	
	private final Long courseId;
	private final PFCourseNode pfNode;
	private final boolean admin;
	private final boolean courseReadOnly;
	private final IdentityEnvironment identityEnv;
	
	public MergedPFCourseNodeContainer(VFSContainer parentContainer, String folderName,
			Long courseId, PFCourseNode pfNode,
			IdentityEnvironment identityEnv, boolean courseReadOnly, boolean admin) {
		super(parentContainer, folderName);
		this.courseId = courseId;
		this.pfNode = pfNode;
		this.admin = admin;
		this.courseReadOnly = courseReadOnly;
		this.identityEnv = identityEnv;
	}

	@Override
	protected void init() {
		if(initialized) return;

		PFManager pfManager = CoreSpringFactory.getImpl(PFManager.class);
		ICourse course = CourseFactory.loadCourse(courseId);
		if(admin) {
			VFSContainer rootFolder = pfManager.provideAdminContainer(pfNode, course.getCourseEnvironment());
			VFSContainer nodeContentContainer = new NamedContainerImpl(getName(), rootFolder);
			addContainersChildren(nodeContentContainer, true);
		} else {
			UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
			VFSContainer rootFolder = pfManager.provideCoachOrParticipantContainer(pfNode, userCourseEnv,
					identityEnv.getIdentity(), courseReadOnly);
			VFSContainer nodeContentContainer = new NamedContainerImpl(getName(), rootFolder);
			addContainersChildren(nodeContentContainer, true);
		}
		
		super.init();
	}

	@Override
	public List<VFSItem> getItems(VFSItemFilter filter) {
		if(!initialized) {
			init();
			initialized = true;
		}
		return super.getItems(filter);
	}

	@Override
	public VFSItem resolve(String path) {
		if(!initialized) {
			init();
			initialized = true;
		}
		return super.resolve(path);
	}
}
