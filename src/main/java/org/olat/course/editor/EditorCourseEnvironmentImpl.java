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
package org.olat.course.editor;

import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.PersistingCourseImpl;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.PersistingCourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 28 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditorCourseEnvironmentImpl implements CourseEnvironment {

	private PersistingCourseImpl course;
	private final CourseGroupManager cgm;

	public EditorCourseEnvironmentImpl(CourseEditorEnv courseEditorEnv) {
		cgm = courseEditorEnv.getCourseGroupManager();
		course = (PersistingCourseImpl)CourseFactory.loadCourse(cgm.getCourseEntry());
	}
	
	@Override
	public void updateCourseEntry(RepositoryEntry courseEntry) {
		if(cgm instanceof PersistingCourseGroupManager) {
			((PersistingCourseGroupManager)cgm).updateRepositoryEntry(courseEntry);
		}
	}

	@Override
	public ICourse updateCourse() {
		course = (PersistingCourseImpl)CourseFactory.loadCourse(cgm.getCourseEntry());
		return course;
	}
	@Override
	public long getLastPublicationTimestamp() {
		return course.getEditorTreeModel().getLatestPublishTimestamp();
	}

	@Override
	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	@Override
	public boolean isPreview() {
		return true;
	}

	@Override
	public CourseGroupManager getCourseGroupManager() {
		return cgm;
	}

	@Override
	public Long getCourseResourceableId() {
		return course.getResourceableId();
	}

	@Override
	public CoursePropertyManager getCoursePropertyManager() {
		return null;
	}

	@Override
	public AssessmentManager getAssessmentManager() {
		return null;
	}

	@Override
	public UserNodeAuditManager getAuditManager() {
		return null;
	}

	@Override
	public Structure getRunStructure() {
		return course.getRunStructure();
	}

	@Override
	public String getCourseTitle() {
		return course.getCourseTitle();
	}

	@Override
	public CourseConfig getCourseConfig() {
		return course.getCourseConfig();
	}

	@Override
	public VFSContainer getCourseFolderContainer() {
		return course.getCourseFolderContainer();
	}

	@Override
	public VFSContainer getCourseFolderContainer(CourseContainerOptions options) {
		return course.getCourseFolderContainer(options);
	}

	@Override
	public LocalFolderImpl getCourseBaseContainer() {
		return course.getCourseBaseContainer();
	}
}