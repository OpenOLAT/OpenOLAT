package org.olat.course.nodes;

import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Provides a course node with the possibility to retrive its last ScoreEvaluation.
 * 
 * <P>
 * Initial Date:  11.04.2008 <br>
 * @author Lavinia Dumitrescu
 */
public interface SelfAssessableCourseNode extends CourseNode {

	/**
	 * Provides the ScoreEvaluation for this course node.
	 * Returns null if no scoring stored yet (that is no selftest finished yet).
	 * @param userCourseEnv
	 * @return Returns the ScoreEvaluation.
	 */
	public ScoreEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv);
}
