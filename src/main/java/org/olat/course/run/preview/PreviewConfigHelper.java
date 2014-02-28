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

import java.util.ArrayList;
import java.util.Date;

import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.repository.RepositoryEntry;

/**
 * Provides a PreviewCourseEnvironment without using the PreviewConfigController and the PreviewSettingsForm.
 * 
 * 
 * <P>
 * Initial Date:  03.12.2009 <br>
 * @author Lavinia Dumitrescu
 */
public class PreviewConfigHelper {

	public static CourseEnvironment getPreviewCourseEnvironment(boolean isCoach, boolean isCourseAdmin, ICourse course) {
		//generateEnvironment();
		final RepositoryEntry courseResource = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		final CourseGroupManager cgm = new PreviewCourseGroupManager(courseResource, new ArrayList<BusinessGroup>(), new ArrayList<BGArea>(), isCoach, isCourseAdmin);
		final UserNodeAuditManager auditman = new PreviewAuditManager();
		final AssessmentManager am = new PreviewAssessmentManager();
		final CoursePropertyManager cpm = new PreviewCoursePropertyManager();
		final Structure runStructure = course.getEditorTreeModel().createStructureForPreview();
		final String title = course.getCourseTitle();
		final CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();

		CourseEnvironment previewCourseEnvironment = new PreviewCourseEnvironment(title, runStructure, new Date(), course.getCourseFolderContainer(), 
				course.getCourseBaseContainer(),course.getResourceableId(), cpm, cgm, auditman, am, courseConfig);			
		
		return previewCourseEnvironment;
	}
}
