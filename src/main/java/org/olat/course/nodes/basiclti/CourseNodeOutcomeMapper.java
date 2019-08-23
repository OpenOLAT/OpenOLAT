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
package org.olat.course.nodes.basiclti;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.imsglobal.basiclti.XMLMap;
import org.imsglobal.pox.IMSPOXRequest;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.activity.IUserActivityLogger;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.logging.activity.UserActivityLoggerImpl;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.lti.ui.OutcomeMapper;
import org.olat.modules.assessment.Role;
import org.olat.resource.OLATResource;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Initial date: 14.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeOutcomeMapper extends OutcomeMapper {

	private static final long serialVersionUID = -4596920091938826925L;
	
	private Long courseOresId;
	private String courseNodeId;
	
	public CourseNodeOutcomeMapper() {
		//
	}
	
	public CourseNodeOutcomeMapper(Identity assessedId, OLATResource resource, String courseNodeId,
			String oauth_consumer_key, String oauth_secret, String sourcedId) {
		super(assessedId, resource, courseNodeId, oauth_consumer_key, oauth_secret, sourcedId);
		this.courseOresId = resource.getResourceableId();
		this.courseNodeId = courseNodeId;
	}
	
	@Override
	protected void reconnectUserSession(HttpServletRequest request) {
		super.reconnectUserSession(request);
		
		ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(request);
		ICourse course = CourseFactory.loadCourse(courseOresId);
		CourseNode cn = course.getRunStructure().getNode(courseNodeId);
		
		IUserActivityLogger logger = UserActivityLoggerImpl.setupLoggerForController(null);
		logger.addLoggingResourceInfo(LoggingResourceable.wrap(course));
		logger.addLoggingResourceInfo(LoggingResourceable.wrap(cn));
	}

	@Override
	protected boolean doUpdateResult(Float score) {
		ICourse course = CourseFactory.loadCourse(courseOresId);
		CourseNode node = course.getRunStructure().getNode(courseNodeId);
		if(node instanceof BasicLTICourseNode) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(node);
			
			Identity assessedId = getIdentity();
			Float cutValue = getCutValue(assessmentConfig);
			
			Float scaledScore = null;
			Boolean passed = null;
			if(score != null) {
				float scale = getScalingFactor(node);
				scaledScore = score * scale;
				if(cutValue != null) {
					passed = scaledScore >= cutValue;
				}
			}
			
			ScoreEvaluation eval = new ScoreEvaluation(scaledScore, passed);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(course);
			courseAssessmentService.updateScoreEvaluation(node, eval, userCourseEnv, assessedId, false, Role.user);
		}
		
		return super.doUpdateResult(score);
	}

	@Override
	protected boolean doDeleteResult() {
		ICourse course = CourseFactory.loadCourse(courseOresId);
		CourseNode node = course.getRunStructure().getNode(courseNodeId);
		if(node instanceof BasicLTICourseNode) {
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			Identity assessedId = getIdentity();
			ScoreEvaluation eval = new ScoreEvaluation(0.0f, false);
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(course);
			courseAssessmentService.updateScoreEvaluation(node, eval, userCourseEnv, assessedId, false, Role.user);
		}

		return super.doDeleteResult();
	}

	@Override
	protected String doReadResult(IMSPOXRequest pox) {
		ICourse course = CourseFactory.loadCourse(courseOresId);
		CourseNode node = course.getRunStructure().getNode(courseNodeId);
		if(node instanceof BasicLTICourseNode) {
			BasicLTICourseNode ltiNode = (BasicLTICourseNode)node;
			UserCourseEnvironment userCourseEnv = getUserCourseEnvironment(course);
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			ScoreEvaluation eval = courseAssessmentService.getAssessmentEvaluation(ltiNode, userCourseEnv);
			String score = "";
			if(eval != null && eval.getScore() != null) {
				float scaledScore = eval.getScore();
				if(scaledScore > 0.0f) {
					float scale = getScalingFactor(ltiNode);
					scaledScore= scaledScore / scale;
				}
				score = Float.toString(scaledScore);
			}
			Map<String,Object> theMap = new TreeMap<>();
			theMap.put("/readResultResponse/result/sourcedId", getSourcedId());
			theMap.put("/readResultResponse/result/resultScore/textString", score);
			theMap.put("/readResultResponse/result/resultScore/language", "en");
			String theXml = XMLMap.getXMLFragment(theMap, true);
			return pox.getResponseSuccess("Result read",theXml);
		}
		return super.doReadResult(pox);
	}
	
	private UserCourseEnvironment getUserCourseEnvironment(ICourse course) {
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(getIdentity());
		UserCourseEnvironmentImpl userCourseEnv = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		return userCourseEnv;
	}
	
	private float getScalingFactor(CourseNode node) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(node);
		if(assessmentConfig.hasScore()) {
			Float scale = node.getModuleConfiguration().getFloatEntry(BasicLTICourseNode.CONFIG_KEY_SCALEVALUE);
			if(scale == null) {
				return 1.0f;
			}
			return scale.floatValue();
		}
		return 1.0f;
	}
	
	private Float getCutValue(AssessmentConfig assessmentConfig) {
		if(assessmentConfig.hasPassed()) {
			Float cutValue = assessmentConfig.getCutValue();
			if(cutValue == null) {
				return null;
			}
			return cutValue;
		}
		return null;
	}
}
