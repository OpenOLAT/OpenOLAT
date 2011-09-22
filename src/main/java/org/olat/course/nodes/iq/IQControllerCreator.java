package org.olat.course.nodes.iq;

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;

public interface IQControllerCreator {

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQTestEditController(UserRequest ureq, WindowControl wControl, ICourse course,
			IQTESTCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce);

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQSelftestEditController(UserRequest ureq, WindowControl wControl, ICourse course,
			IQSELFCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce);

	/**
	 * The iq test edit screen in the course editor.
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param courseNode
	 * @param groupMgr
	 * @param euce
	 * @return
	 */
	public TabbableController createIQSurveyEditController(UserRequest ureq, WindowControl wControl, ICourse course,
			IQSURVCourseNode courseNode, CourseGroupManager groupMgr, UserCourseEnvironment euce);

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param ne
	 * @param courseNode
	 * @return
	 */
	public Controller createIQTestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, IQTESTCourseNode courseNode);

	public Controller createIQTestPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, IQTESTCourseNode courseNode);

	public Controller createIQSelftestRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, IQSELFCourseNode courseNode);

	public Controller createIQSurveyRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne, IQSURVCourseNode courseNode);

	public Controller createIQTestDetailsEditController(Long courseResourceableId, String ident, Identity identity,
			RepositoryEntry referencedRepositoryEntry, String qmdEntryTypeAssess, UserRequest ureq, WindowControl wControl);
	
	/**
	 * todo:remove to proper place
	 * @param locale
	 * @param course
	 * @param exportDirectory
	 * @param charset
	 * @return
	 */
	public boolean archiveIQTestCourseNode(Locale locale, String repositorySoftkey, Long courseResourceableId, String shortTitle,  String ident, File exportDirectory, String charset);
	
}