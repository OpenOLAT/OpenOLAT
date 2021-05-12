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

package org.olat.course.run.preview;

import java.util.Date;

import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.folder.CourseContainerOptions;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCourseEnvironment implements CourseEnvironment {
	private final String title;
	private final Structure runStructure;
	private final LocalFolderImpl courseBaseContainer;
	private final VFSContainer courseFolderContainer;
	private final CoursePropertyManager cpm;
	private final CourseGroupManager cgm;
	private final UserNodeAuditManager auditman;
	private final AssessmentManager am;
	private final long simulatedDateTime;
	private Long resourceablId;
	private CourseConfig courseConfig;

	PreviewCourseEnvironment(String title, Structure runStructure, Date simulatedDateTime, VFSContainer courseFolderContainer,
			LocalFolderImpl courseBaseContainer, Long courseResourceableID, CoursePropertyManager cpm, CourseGroupManager cgm,
			UserNodeAuditManager auditman, AssessmentManager am, CourseConfig courseConfig) {
		super();
		this.title = title;
		this.simulatedDateTime = simulatedDateTime.getTime();
		this.courseFolderContainer = courseFolderContainer;
		this.courseBaseContainer = courseBaseContainer;
		this.runStructure = runStructure;
		this.cpm = cpm;
		this.cgm = cgm;
		this.auditman = auditman;
		this.am = am;
		this.resourceablId = courseResourceableID;
		this.courseConfig = courseConfig.clone();
	}

	@Override
	public long getCurrentTimeMillis() {
		return simulatedDateTime;
	}

	@Override
	public void updateCourseEntry(RepositoryEntry courseEntry) {
		//
	}

	@Override
	public ICourse updateCourse() {
		return null;
	}

	@Override
	public long getLastPublicationTimestamp() {
		return -1;
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
		//since OLAT 6.0.x: needed for SinglePage and hence for STCourseNode
		//introduced dependancy through iFrame refactoring of SinglePage and CP
		return resourceablId;
	}

	@Override
	public CoursePropertyManager getCoursePropertyManager() {
		return cpm;
	}

	@Override
	public AssessmentManager getAssessmentManager() {
		return am;
	}

	@Override
	public UserNodeAuditManager getAuditManager() {
		return auditman;
	}

	@Override
	public Structure getRunStructure() {
		return runStructure;
	}

	@Override
	public String getCourseTitle() {
		return title;
	}

	@Override
	public CourseConfig getCourseConfig() {
		return courseConfig;
	}

	public void setCourseConfig(CourseConfig cc) {
		courseConfig = (cc == null ? null : cc.clone());
	}

	@Override
	public VFSContainer getCourseFolderContainer() {
		return courseFolderContainer;
	}
	
	@Override
	public VFSContainer getCourseFolderContainer(CourseContainerOptions options) {
		return courseFolderContainer;
	}

	@Override
	public LocalFolderImpl getCourseBaseContainer() {
		return courseBaseContainer;
	}

}