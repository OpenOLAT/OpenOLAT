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
import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.scorm.ScormEditController;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.ModuleConfiguration;
import org.olat.user.UserManager;

/**
 * The mapper for scorm, serializable
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ScormAPIMapper implements Mapper, ScormAPICallback, Serializable {

	private static final long serialVersionUID = -144400398761676983L;
	private static final OLog log = Tracing.createLoggerFor(ScormAPIMapper.class);
	
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
	private boolean attemptsIncremeted;
	private File cpRoot;
	
	public ScormAPIMapper() {
		//for XStream
	}
	
	public ScormAPIMapper(Identity identity, String resourceId, String courseIdNodeId, boolean isAssessable,
			File cpRoot, OLATApiAdapter scormAdapter, boolean attemptsIncremeted) {
		this.scormAdapter = scormAdapter;
		this.identity = identity;
		this.identityKey = identity.getKey();
		this.resourceId = resourceId;
		this.courseIdNodeId = courseIdNodeId;
		this.isAssessable = isAssessable;
		this.cpRoot = cpRoot;
		this.lesson_mode = scormAdapter.getLessonMode();
		this.credit_mode = scormAdapter.getCreditMode();
		this.scormAdapter.addAPIListener(this);
		this.attemptsIncremeted = attemptsIncremeted;
	}
	
	private final void check() {
		if(identity == null) {
			identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(identityKey);
		}
		
		if(scormAdapter == null) {
			scormAdapter = new OLATApiAdapter();
			String fullname = UserManager.getInstance().getUserDisplayName(identity.getUser());
			scormAdapter.init(cpRoot, resourceId, courseIdNodeId, FolderConfig.getCanonicalRoot(), identity.getName(), fullname, lesson_mode, credit_mode, hashCode());
			scormAdapter.addAPIListener(this);
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
	public void lmsCommit(String olatSahsId, Properties scoProperties) {
		if (isAssessable) {
			checkForLms();
			calculateScorePassed(olatSahsId, scoProperties);
		}
	}

	@Override
	public void lmsFinish(String olatSahsId, Properties scoProperties) {
		if (isAssessable) {
			checkForLms();
			calculateScorePassed(olatSahsId, scoProperties);
		}
	}
	
	private void calculateScorePassed(String olatSahsId, Properties scoProperties) {
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
		
		float cutval = scormNode.getCutValueConfiguration().floatValue();
		boolean passed = (score >= cutval);
		// if advanceScore option is set update the score only if it is higher
		// <OLATEE-27>
		ModuleConfiguration config = scormNode.getModuleConfiguration();
		if (config.getBooleanSafe(ScormEditController.CONFIG_ADVANCESCORE, true)) {
			Float currentScore = scormNode.getUserScoreEvaluation(userCourseEnv).getScore();
			if (score > (currentScore != null ? currentScore : -1f)) {
				// </OLATEE-27>
				if(!attemptsIncremeted && config.getBooleanSafe(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false)) {
					attemptsIncremeted = true;
				}
				ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), Boolean.valueOf(passed));
				scormNode.updateUserScoreEvaluation(sceval, userCourseEnv, identity, attemptsIncremeted);
				userCourseEnv.getScoreAccounting().scoreInfoChanged(scormNode, sceval);
			} else if (!config.getBooleanSafe(ScormEditController.CONFIG_ATTEMPTSDEPENDONSCORE, false)) {
				ScoreEvaluation sceval = scormNode.getUserScoreEvaluation(userCourseEnv);
				scormNode.updateUserScoreEvaluation(sceval, userCourseEnv, identity, false);
				userCourseEnv.getScoreAccounting().scoreInfoChanged(scormNode, sceval);
			}
		} else {
			// <OLATEE-27>
			if (score < 0f) {
				score = 0f;
			}
			// </OLATEE-27>
			ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), Boolean.valueOf(passed));
			scormNode.updateUserScoreEvaluation(sceval, userCourseEnv, identity, false);
			userCourseEnv.getScoreAccounting().scoreInfoChanged(scormNode, sceval);
		}

		if (log.isDebug()) {
			String msg = "for scorm node:" + scormNode.getIdent() + " (" + scormNode.getShortTitle() + ") a lmsCommit for scoId "
					+ olatSahsId + " occured, total sum = " + score + ", cutvalue =" + cutval + ", passed: " + passed
					+ ", all scores now = " + scoProperties.toString();
			log.debug(msg, null);
		}
	}

	public MediaResource handle(String relPath, HttpServletRequest request) {
		check();
		
		String apiCall = request.getParameter("apiCall");
		String apiCallParamOne = request.getParameter("apiCallParamOne");
		String apiCallParamTwo = request.getParameter("apiCallParamTwo");
		
		if(log.isDebug()) {
			log.debug("scorm api request by user:"+ identity.getName() +": " + apiCall + "('" + apiCallParamOne + "' , '" + apiCallParamTwo + "')");
		}

		StringMediaResource smr = new StringMediaResource();
		smr.setContentType("text/html");
		smr.setEncoding("utf-8");
		
		if (apiCall != null && apiCall.equals("initcall")) {
			//used for Mozilla / firefox only to get more time for fireing the onunload stuff triggered by overwriting the content.
			smr.setData("<html><body></body></html>");
			return smr;
		}

		String returnValue = "";
		if (apiCall != null) {
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
			smr.setData("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body><p>"
					+ returnValue + "</p></body></html>");
			return smr;
		}
		smr.setData("");
		return smr;
	}
}