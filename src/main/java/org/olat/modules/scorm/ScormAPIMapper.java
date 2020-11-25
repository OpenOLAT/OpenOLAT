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
package org.olat.modules.scorm;

import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_COMMIT;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_FINISH;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_GETDIAGNOSTIC;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_GETERRORSTRING;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_GETLASTERROR;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_GETVALUE;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_INITIALIZE;
import static org.olat.modules.scorm.ScormAPIandDisplayController.LMS_SETVALUE;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;

/**
 * The mapper for scorm, serializable
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ScormAPIMapper implements Mapper, ScormAPICallback, Serializable {

	private static final long serialVersionUID = -144400398761676983L;
	private static final Logger log = Tracing.createLoggerFor(ScormAPIMapper.class);
	
	private transient Identity identity;
	private transient OLATApiAdapter scormAdapter;
	
	private transient ScormCourseNode scormNode;
	private transient UserCourseEnvironment userCourseEnv;
	
	private Long identityKey;
	private String resourceId;
	private String courseIdNodeId;
	private String lesson_mode;
	private String credit_mode;
	private boolean isAssessable;
	private String assessableType;
	private boolean attemptsIncremented;
	private Float currentScore;
	private Boolean currentPassed;
	private File cpRoot;
	
	public ScormAPIMapper() {
		//for XStream
	}
	
	public ScormAPIMapper(Identity identity, String resourceId, String courseIdNodeId, String assessableType,
			File cpRoot, OLATApiAdapter scormAdapter, boolean attemptsIncremented) {
		this.scormAdapter = scormAdapter;
		this.identity = identity;
		this.identityKey = identity.getKey();
		this.resourceId = resourceId;
		this.courseIdNodeId = courseIdNodeId;
		this.isAssessable = StringHelper.containsNonWhitespace(assessableType);
		this.assessableType = assessableType;
		this.cpRoot = cpRoot;
		this.lesson_mode = scormAdapter.getLessonMode();
		this.credit_mode = scormAdapter.getCreditMode();
		this.scormAdapter.addAPIListener(this);
		this.attemptsIncremented = attemptsIncremented;
		
		//setup the current score
		currentScore();
	}
	
	private void currentScore() {
		if(isAssessable) {
			checkForLms();
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(scormNode);
			if(Mode.none != assessmentConfig.getScoreMode()) {
				currentScore = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv).getScore();
			}
			if(Mode.none != assessmentConfig.getPassedMode()) {
				currentPassed = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv).getPassed();
			}
		}
	}
	
	private final void check() {
		if(identity == null) {
			identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		}
		
		if(isAssessable && !StringHelper.containsNonWhitespace(assessableType)) {
			assessableType = ScormEditController.CONFIG_ASSESSABLE_TYPE_SCORE;
		}
		
		if(scormAdapter == null) {
			try {
				scormAdapter = new OLATApiAdapter();
				String fullname = UserManager.getInstance().getUserDisplayName(identity);
				scormAdapter.init(cpRoot, resourceId, courseIdNodeId, FolderConfig.getCanonicalRoot(), identity.getName(), fullname, lesson_mode, credit_mode, hashCode());
				scormAdapter.addAPIListener(this);
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
	
	private final void checkForLms() {
		check();

		if(scormNode == null) {
			int sep = courseIdNodeId.indexOf('-');
			String courseId  = courseIdNodeId.substring(0, sep);
			String nodeId = courseIdNodeId.substring(sep + 1);
			ICourse course = CourseFactory.loadCourse(Long.parseLong(courseId));
			scormNode = (ScormCourseNode)course.getRunStructure().getNode(nodeId);
				
			IdentityEnvironment identityEnvironment = new IdentityEnvironment();
			identityEnvironment.setIdentity(identity);
			userCourseEnv = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		}
	}

	@Override
	public void lmsCommit(String olatSahsId, Properties scoreProp, Properties lessonStatusProp) {
		if (isAssessable) {
			checkForLms();
			calculateResults(olatSahsId, scoreProp, lessonStatusProp, false);
		}
	}

	@Override
	public void lmsFinish(String olatSahsId, Properties scoreProp, Properties lessonStatusProp) {
		if (isAssessable) {
			checkForLms();
			calculateResults(olatSahsId, scoreProp, lessonStatusProp, true);
		}
	}
	
	private void calculateResults(String olatSahsId, Properties scoreProp, Properties lessonStatusProp, boolean finish) {
		if(ScormEditController.CONFIG_ASSESSABLE_TYPE_PASSED.equals(assessableType)) {
			calculatePassed(olatSahsId, lessonStatusProp, finish);
		} else {
			calculateScorePassed(olatSahsId, scoreProp, finish);
		}
	}

	private void calculatePassed(String olatSahsId, Properties lessonStatusProp, boolean finish) {
		int found = 0;
		boolean passedScos = true;
		for (Iterator<Object> it_status = lessonStatusProp.values().iterator(); it_status.hasNext();) {
			String status = (String)it_status.next();
			passedScos &= "passed".equals(status);
			found++;
		}

		boolean passed = (found == scormAdapter.getNumOfSCOs()) && passedScos;
		// if advanceScore option is set update the score only if it is higher
		// <OLATEE-27>
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		ModuleConfiguration config = scormNode.getModuleConfiguration();
		if (config.getBooleanSafe(ScormEditController.CONFIG_ADVANCESCORE, true)) {
			if (currentPassed == null || !currentPassed.booleanValue()) {
				// </OLATEE-27>
				boolean increment = !attemptsIncremented && finish;
				ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(0.0f), Boolean.valueOf(passed));
				courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, increment,
						Role.user);
				if(increment) {
					attemptsIncremented = true;
				}
			} else if (!config.getBooleanSafe(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false)) {
				boolean increment = !attemptsIncremented && finish;
				ScoreEvaluation sceval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
				courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, increment,
						Role.user);
				if(increment) {
					attemptsIncremented = true;
				}
			}
		} else {
			boolean increment = !attemptsIncremented && finish;
			ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(0.0f), Boolean.valueOf(passed));
			courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, false,
					Role.user);
			if(increment) {
				attemptsIncremented = true;
			}
		}
		
		if(finish) {
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			CoreSpringFactory.getImpl(AssessmentNotificationsHandler.class).markPublisherNews(identity, courseId);
		}

		if (log.isDebugEnabled()) {
			String msg = "for scorm node:" + scormNode.getIdent() + " (" + scormNode.getShortTitle() + ") a lmsCommit for scoId "
					+ olatSahsId + " occured, passed: " + passed
					+ ", all lesson status now = " + lessonStatusProp.toString();
			log.debug(msg);
		}
	}
	
	private void calculateScorePassed(String olatSahsId, Properties scoProperties, boolean finish) {
		// do a sum-of-scores over all sco scores
		// <OLATEE-27>
		float score = -1f;
		// </OLATEE-27>
		for (Iterator<Object> it_score = scoProperties.values().iterator(); it_score.hasNext();) {
			// <OLATEE-27>
			if (score < 0f) {
				score = 0f;
			}
			// </OLATEE-27>
			String aScore = (String) it_score.next();
			float ascore = Float.parseFloat(aScore);
			score += ascore;
		}
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(scormNode);
		float cutval = assessmentConfig.getCutValue().floatValue();
		boolean passed = (score >= cutval);
		// if advanceScore option is set update the score only if it is higher
		// <OLATEE-27>
		ModuleConfiguration config = scormNode.getModuleConfiguration();
		if (config.getBooleanSafe(ScormEditController.CONFIG_ADVANCESCORE, true)) {
			if (score > (currentScore != null ? currentScore : -1f)) {
				// </OLATEE-27>
				boolean increment = !attemptsIncremented && finish;
				ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), Boolean.valueOf(passed));
				courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, increment,
						Role.user);
				if(increment) {
					attemptsIncremented = true;
				}
			} else if (!config.getBooleanSafe(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false)) {
				boolean increment = !attemptsIncremented && finish;
				ScoreEvaluation sceval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
				courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, increment,
						Role.user);
				if(increment) {
					attemptsIncremented = true;
				}
			}
		} else {
			// <OLATEE-27>
			if (score < 0f) {
				score = 0f;
			}
			// </OLATEE-27>
			boolean increment = !attemptsIncremented && finish;
			ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), Boolean.valueOf(passed));
			courseAssessmentService.updateScoreEvaluation(scormNode, sceval, userCourseEnv, identity, false, Role.user);
			if(increment) {
				attemptsIncremented = true;
			}
		}
		
		if(finish) {
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			CoreSpringFactory.getImpl(AssessmentNotificationsHandler.class).markPublisherNews(identity, courseId);
		}

		if (log.isDebugEnabled()) {
			String msg = "for scorm node:" + scormNode.getIdent() + " (" + scormNode.getShortTitle() + ") a lmsCommit for scoId "
					+ olatSahsId + " occured, total sum = " + score + ", cutvalue =" + cutval + ", passed: " + passed
					+ ", all scores now = " + scoProperties.toString();
			log.debug(msg);
		}
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		check();
		
		String apiCall = request.getParameter("apiCall");
		String apiCallParamOne = request.getParameter("apiCallParamOne");
		String apiCallParamTwo = request.getParameter("apiCallParamTwo");
		
		if(log.isDebugEnabled()) {
			log.debug("scorm api request by user: {}: {} ('{}' , '{}')", identity.getName(), apiCall, apiCallParamOne, apiCallParamTwo);
		}

		if (apiCall != null && apiCall.equals("initcall")) {
			//used for Mozilla / firefox only to get more time for fireing the onunload stuff triggered by overwriting the content.
			log.info("Init call");
			return createInitResource(request);
		}

		if (apiCall != null) {
			String returnValue = apiCall(apiCall, apiCallParamOne, apiCallParamTwo);
			return createResource(returnValue, request);
		} else if(relPath.contains("batch")) {
			try {				
				String batch = IOUtils.toString(request.getReader());
				JSONArray batchArray = new JSONArray(batch);
				for(int i=0; i<batchArray.length(); i++) {
					JSONObject obj = batchArray.getJSONObject(i);
					apiCall = obj.getString("apiCall");
					apiCallParamOne = obj.getString("param1");
					apiCallParamTwo = obj.getString("param2");
					apiCall(apiCall, apiCallParamOne, apiCallParamTwo);
				}
			} catch (IOException e) {
				log.error("", e);
			}
		}
		return createResource("", request);
	}
	
	private String apiCall(String apiCall, String apiCallParamOne, String apiCallParamTwo) {
		String returnValue = "";
		if (apiCall.equals(LMS_INITIALIZE)) {
			returnValue = scormAdapter.LMSInitialize(apiCallParamOne);
		} else if (apiCall.equals(LMS_GETVALUE)) {
			returnValue = scormAdapter.LMSGetValue(apiCallParamOne);
		} else if (apiCall.equals(LMS_SETVALUE)) {
			returnValue = scormAdapter.LMSSetValue(apiCallParamOne, apiCallParamTwo);
		} else if (apiCall.equals(LMS_COMMIT)) {
			returnValue = scormAdapter.LMSCommit(apiCallParamOne);
		} else if (apiCall.equals(LMS_FINISH)) {
			returnValue = scormAdapter.LMSFinish(apiCallParamOne);
		} else if (apiCall.equals(LMS_GETLASTERROR)) {
			returnValue = scormAdapter.LMSGetLastError();
		} else if (apiCall.equals(LMS_GETDIAGNOSTIC)) {
			returnValue = scormAdapter.LMSGetDiagnostic(apiCallParamOne);
		} else if (apiCall.equals(LMS_GETERRORSTRING)) {
			returnValue = scormAdapter.LMSGetErrorString(apiCallParamOne);
		}
		return returnValue;
	}
	
	private MediaResource createInitResource(HttpServletRequest request) {
		MediaResource resource;
		boolean acceptJson = ServletUtil.acceptJson(request);
		if(acceptJson && request == null) {
			resource = createHTMLResource("");
		} else {
			resource = createHTMLResource("<html><body></body></html>");
		}
		return resource;
	}
	
	private MediaResource createResource(String returnValue, HttpServletRequest request) {
		MediaResource resource;
		boolean acceptJson = ServletUtil.acceptJson(request);
		if(acceptJson && request == null) {
			resource = createHTMLResource("");
			
		} else if(StringHelper.containsNonWhitespace(returnValue)) {
			String data = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><p>" + returnValue + "</p></body></html>";
			resource = createHTMLResource(data);
		} else {
			resource = createHTMLResource("");
		}
		return resource;
	}
	

	private StringMediaResource createHTMLResource(String data) {
		StringMediaResource smr = new StringMediaResource();
		smr.setContentType("text/html");
		smr.setEncoding("utf-8");
		smr.setData(data);
		return smr;
	}
}