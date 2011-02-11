package org.olat.course.run.preview;

import java.util.ArrayList;
import java.util.Date;

import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;

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
		final CourseGroupManager cgm = new PreviewCourseGroupManager(new ArrayList(), new ArrayList(), isCoach, isCourseAdmin);
		final UserNodeAuditManager auditman = new PreviewAuditManager();
		final AssessmentManager am = new PreviewAssessmentManager();
		final CoursePropertyManager cpm = new PreviewCoursePropertyManager();
		final Structure runStructure = course.getEditorTreeModel().createStructureForPreview();
		final String title = course.getCourseTitle();

		CourseEnvironment previewCourseEnvironment = new PreviewCourseEnvironment(title, runStructure, new Date(), course.getCourseFolderContainer(), 
				course.getCourseBaseContainer(),course.getResourceableId(), cpm, cgm, auditman, am);			
		
		return previewCourseEnvironment;
	}
}
