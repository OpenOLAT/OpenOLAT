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

import org.olat.core.id.OLATResourceable;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntryRef;

/**
 * Wrapper to lazy load the key of the repository entry.
 * 
 * Initial date: 14 Apr 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseEntryRef implements RepositoryEntryRef {
	
	private Long key;
	private CourseGroupManager courseGroupManager;
	private CourseEnvironment courseEnv;
	private CourseEditorEnv courseEditorEnv;
	private UserCourseEnvironment userCourseEnv;
	private ICourse course;
	private OLATResourceable courseRes;

	public CourseEntryRef(CourseGroupManager courseGroupManager) {
		this.courseGroupManager = courseGroupManager;
	}
	
	public CourseEntryRef(CourseEnvironment courseEnv) {
		this.courseEnv = courseEnv;
	}
	
	public CourseEntryRef(CourseEditorEnv courseEditorEnv) {
		this.courseEditorEnv = courseEditorEnv;
	}
	
	public CourseEntryRef(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}
	
	public CourseEntryRef(ICourse course) {
		this.course = course;
	}
	
	public CourseEntryRef(OLATResourceable courseRes) {
		this.courseRes = courseRes;
	}

	@Override
	public Long getKey() {
		if (key == null) {
			if (courseGroupManager != null) {
				key = courseGroupManager.getCourseEntry().getKey();
				courseGroupManager = null;
			} else if (courseEnv != null) {
				key = courseEnv.getCourseGroupManager().getCourseEntry().getKey();
				courseEnv = null;
			} else if (courseEditorEnv != null) {
				key = courseEditorEnv.getCourseGroupManager().getCourseEntry().getKey();
				courseEditorEnv = null;
			} else if (userCourseEnv != null) {
				key = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
				userCourseEnv = null;
			} else if (course != null) {
				key = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
				course = null;
			} else if (courseRes != null) {
				key = CourseFactory.loadCourse(courseRes).getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
				courseRes = null;
			}
		}
		return key;
	}

}
