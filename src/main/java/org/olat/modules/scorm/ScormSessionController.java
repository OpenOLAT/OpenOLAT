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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
import org.olat.modules.scorm.manager.ScormManager;
import org.olat.modules.scorm.server.beans.LMSDataFormBean;
import org.olat.modules.scorm.server.beans.LMSDataHandler;
import org.olat.modules.scorm.server.beans.LMSResultsBean;
import org.olat.modules.scorm.server.sequence.ItemSequence;

/**
 * 
 * Initial date: 1 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScormSessionController {

	private static final Logger log = Tracing.createLoggerFor(ScormSessionController.class);
	
	private static final String SCORE_IDENT = "cmi.core.score.raw";
	private static final String LESSON_STATUS_IDENT = "cmi.core.lesson_status";
	private static final String CMI_STUDENT_ID = "cmi.core.student_id";
	private static final String CMI_STUDENT_NAME = "cmi.core.student_name";
	private static final String CMI_EXIT = "cmi.core.exit";

	private String studentId;
	private String studentName;
	//was used as reference id like out repo id
	
	//the sco id
	private final ScoState status = new ScoState();
	
	private LMSDataHandler odatahandler;
	private ScormManager scormManager;
	private SettingsHandler scormSettingsHandler;

	private Float currentScore;
	private Boolean currentPassed;
	private String assessableType;
	private boolean attemptsIncremented;
	private final Identity identity;
	private ScormCourseNode scormNode;
	private UserCourseEnvironment userCourseEnv;
	
	private Properties scoresProp; // keys: sahsId; values = raw score of an sco
	private Properties lessonStatusProp;
	
	private File scorePropsFile;
	private File lessonStatusPropsFile;
	
	/**
	 * creates a new API adapter
	 */
	public ScormSessionController (Identity identity, String assessableType) {
		this.identity = identity;
		this.assessableType = assessableType;
	}

	/**
	 * @param cpRoot
	 * @param repoId
	 * @param courseId
	 * @param userPath
	 * @param studentId - the olat username
	 * @param studentName - the students name
	 * @param isVerbose prints out what is going on inside the scorm RTE
	 */
	public	final void init (File cpRoot, String repoId, String courseId, String storagePath, String studentId, String studentName, String lesson_mode, String credit_mode, int controllerHashCode)
	throws IOException {
		this.studentId   = studentId;
		this.studentName = studentName;
		scormSettingsHandler = new SettingsHandler(cpRoot.getAbsolutePath(), repoId, courseId, storagePath, studentName, studentId, lesson_mode, credit_mode, controllerHashCode);
		
		// get a path for the scores per sco
		String savePath = scormSettingsHandler.getFilePath();
		scorePropsFile = new File(savePath, "_olat_score.properties");
		scoresProp = new Properties();
		if (scorePropsFile.exists()) {
			try(InputStream is = new BufferedInputStream(new FileInputStream(scorePropsFile))) {
				scoresProp.load(is);
			} catch (IOException e) {
				throw e;
			}
		}
		
		lessonStatusPropsFile = new File(savePath, "_olat_lesson_status.properties");
		lessonStatusProp = new Properties();
		if (lessonStatusPropsFile.exists()) {
			try(InputStream is = new BufferedInputStream(new FileInputStream(lessonStatusPropsFile))) {
				lessonStatusProp.load(is);
			} catch (IOException e) {
				throw e;
			}
		}
		
		scormManager = new ScormManager(cpRoot.getAbsolutePath(), true, true, true, scormSettingsHandler);
	}
	
	public void initCurrentScore(ICourse course, ScormCourseNode courseNode) {
		this.scormNode = courseNode;
		
		IdentityEnvironment identityEnvironment = new IdentityEnvironment();
		identityEnvironment.setIdentity(identity);
		userCourseEnv = new UserCourseEnvironmentImpl(identityEnvironment, course.getCourseEnvironment());
		
		if(isAssessable()) {
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
	
	public boolean isAssessable() {
		return StringHelper.containsNonWhitespace(assessableType);
	}
	
	public String getCreditMode() {
		return scormSettingsHandler.getCreditMode();
	}
	
	public String getLessonMode() {
		return scormSettingsHandler.getLessonMode();
	}
	
	public int getNumOfSCOs() {
		return scormManager.getNumOfSCOs();
	}
	
	public String getCurrenSCOId() {
		return status.getScoId();
	}
	
	public boolean isCurrentSCOFinished() {
		return status.getStatus() == ScoLifecycle.finished;
	}
	
	public String getCmiExit( ) {
		return status.getCmi(CMI_EXIT);
	}

	/**
	 * @param scoId
	 */
	public synchronized final void launchItem(String scoId, boolean force) {
		if(force) {
			status.setStatus(null);
			status.setScoId(scoId);
		}
		
		if (status.getStatus() == ScoLifecycle.launching) {
			log.debug("SCO {} is launching.", status.getScoId());
			return;
		}
		if (status.getStatus() == ScoLifecycle.initialized && status.isCurrent(scoId)) {
			log.debug("SCO {} is already running.", scoId);
			return;
		}
		status.cmiClear();

		log.debug("Launching sahs {}", scoId);

		if (status.getStatus() == ScoLifecycle.launching) {
			log.debug ("SCO {} will be unloaded.", status.getScoId());
		} else {
			status.setStatus(ScoLifecycle.launching);
			status.setScoId(scoId);

			//putting all cmi from the olat storage to the local storage
			LMSDataFormBean lmsDataBean = new LMSDataFormBean();
			lmsDataBean.setItemID(scoId);
			lmsDataBean.setLmsAction("get");
			odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
			LMSResultsBean lmsBean = odatahandler.getResultsBean();
			status.cmiClear();
			status.putCmi(CMI_STUDENT_ID, studentId);
			status.putCmi(CMI_STUDENT_NAME, studentName);
			
			String[][] strArr = lmsBean.getCmiStrings();
			if(strArr != null){
				for(int i=0;i<strArr.length;i++){
					String key = strArr[i][0];
					String value = strArr[i][1];
					status.putCmi(key, value);
					log.debug("passing cmi data to api adapter: "+key +": "+ value);
				}
			}
		}
	}

	public synchronized final void lmsInitialize() {
		status.setStatus(ScoLifecycle.initialized);
	}

	/**
	 * 
	 * @param isACommit true, if the call comes from a lmscommit, false if it comes from a lmsfinish
	 * @return
	 */
	public synchronized final String lmsCommit(String scoId, boolean isACommit, Map<String,String> scormAgainData) {
		if (scoId == null) {
			return "false";
		}

		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(scoId);
		lmsDataBean.setNextAction("5");
		lmsDataBean.setLmsAction("update");
		
		if(!scormAgainData.isEmpty()) {
			for(Map.Entry<String, String> entry:scormAgainData.entrySet()) {
				status.putCmi(entry.getKey(), entry.getValue());
			}
		}

		Map<String,String> cmiData = status.copyCmis();
		
		//work around for missing cmi's (needed by reload code, but not used in ilias code)
		if(cmiData.get("cmi.interactions._count") != null && cmiData.get("cmi.interactions._count") != "0"){
			int count = Integer.parseInt(cmiData.get("cmi.interactions._count"));
			for(int i=0;i<count;i++){
				//OLAT-4271: check first if cmi.interactions.n.objectives._count exist before putting a default one
				String objectivesCount = cmiData.get("cmi.interactions."+ i +".objectives._count");
				if(!StringHelper.containsNonWhitespace(objectivesCount)) {
					cmiData.put("cmi.interactions."+ i +".objectives._count","0");
				}
			}
		}
		
		String rawScore = cmiData.get(SCORE_IDENT);
		String lessonStatus = cmiData.get(LESSON_STATUS_IDENT);
		if (isACommit) {
			if (StringHelper.containsNonWhitespace(rawScore) || StringHelper.containsNonWhitespace(lessonStatus)) {
				// we have a score set in this sco.
				// persist
				saveScoreProperties(scoId, rawScore);
				saveLessonStatusProperties(scoId, lessonStatus);
				// notify
				if(isAssessable()) {
					calculateResults(lessonStatus, scoresProp, lessonStatusProp, false);
				}
			}
		} else {
			//if "isACommit" is false, this is a lmsFinish and the apiCallback shall save the points an passed information
			if (StringHelper.containsNonWhitespace(rawScore)) {
				scoresProp.put(scoId, rawScore);
			}
			if (StringHelper.containsNonWhitespace(lessonStatus)) {
				lessonStatusProp.put(scoId, lessonStatus);
			}
			
			if(isAssessable()) {
				calculateResults(lessonStatus, scoresProp, lessonStatusProp, false);
			}
		}
		
		log.debug("Commit cmis of {}: {}", scoId, cmiData);
		
		try {
			lmsDataBean.setDataAsMap(cmiData);
			odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
			odatahandler.updateCMIData(scoId);
			return "true";
		} catch (Exception e) {
			log.error("Error during commit", e);
			return "false";
		}
	}
	
	private synchronized void saveScoreProperties(String scoId, String rawScore) {
		if(StringHelper.containsNonWhitespace(rawScore)) {
			scoresProp.put(scoId, rawScore);
			try(OutputStream os = new BufferedOutputStream(new FileOutputStream(scorePropsFile))) {
				scoresProp.store(os, null);
			} catch (IOException e) {
				throw new OLATRuntimeException(this.getClass(), "could not save scorm-properties-file: "+scorePropsFile.getAbsolutePath(), e);
			}
		}
	}
	
	private synchronized void saveLessonStatusProperties(String scoId, String lessonStatus) {
		if(StringHelper.containsNonWhitespace(lessonStatus)) {
			lessonStatusProp.put(scoId, lessonStatus);
			try(OutputStream os = new BufferedOutputStream(new FileOutputStream(lessonStatusPropsFile))) {
				lessonStatusProp.store(os, null);
			} catch (IOException e) {
				throw new OLATRuntimeException(this.getClass(), "could not save scorm-properties-file: "+scorePropsFile.getAbsolutePath(), e);
			}
		}
	}
	
	public synchronized final void lmsFinish(boolean commit, Map<String, String> cmis) {
		if(status.getStatus() != ScoLifecycle.initialized) {
			return;
		}
		
		status.setStatus(ScoLifecycle.finished);
		String scoId = status.getScoId();
		if (commit) {
			// Stupid "implicit commit"
			lmsCommit(scoId, false, cmis); 
		}
		archiveScoData(scoId);
	}
	
	/**
	 * @return a String that points to the last accessed sco itemId
	 */
	public String getScormLastAccessedItemId(){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setLmsAction("boot");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getItemID();
	}
	
	/**
	 * Archive the current SCORM CMI Data, see ItemSequence.archiveScoData
	 * @return
	 */
	public boolean archiveScoData(String scoId) {
		boolean success = false;
		try {
			String itemId = scormManager.getSequence().findItemFromIndex(Integer.valueOf(scoId));
			ItemSequence item = scormManager.getSequence().getItem(itemId);
			if (item != null) {
				success = item.archiveScoData();
			}
		} catch (Exception e) {
			log.error("Error at OLATApiAdapter.archiveScoData(): ", e);
		}
		return success;
	}
	
	/**
	 * @param itemId
	 * @return true if the item is completed
	 */
	public boolean isItemCompleted(String itemId){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(itemId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getIsItemCompleted().equals("true");
	}
	
	/**
	 * @param itemId
	 * @return true if item has any not fullfilled preconditions
	 */
	public boolean hasItemPrerequisites(String itemId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(itemId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getHasPrerequisites().equals("true");
	}
	
	/**
	 * @return Map containing the recent sco items status
	 */
	public Map <String,String>getScoItemsStatus(){
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setLmsAction("boot");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] preReqTbl = lmsBean.getPreReqTable();
		Map <String,String>itemsStatus = new HashMap<>();
		//put table into map 
		for(int i=0; i < preReqTbl.length; i++){
			if(preReqTbl[i][1].equals("not attempted")) {
				preReqTbl[i][1] ="not_attempted";
			}
			itemsStatus.put(preReqTbl[i][0], preReqTbl[i][1]);
		}
		return itemsStatus;	
	}
	
	public String[][] getScoCmis(String scoId) {
		//putting all cmi from the olat storage to the local storage
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(scoId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		return lmsBean.getCmiStrings();
	}

	/**
	 * @param recentId
	 * @return the previos Sco itemId
	 */
	public Integer getPreviousSco(String recentId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(recentId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] pretable = lmsBean.getPreReqTable();
		String previousNavScoId = "-1";
		for(int i=0; i < pretable.length; i++){
			if(pretable[i][0].equals(recentId) &&  (i != 0 )){
				previousNavScoId =  pretable[--i][0];
				break;
			}
		}
		return Integer.valueOf(previousNavScoId);
	}

	/**
	 * @param recentId
	 * @return the next Sco itemId
	 */
	public Integer getNextSco(String recentId) {
		LMSDataFormBean lmsDataBean = new LMSDataFormBean();
		lmsDataBean.setItemID(recentId);
		lmsDataBean.setLmsAction("get");
		odatahandler = new LMSDataHandler(scormManager, lmsDataBean);
		LMSResultsBean lmsBean = odatahandler.getResultsBean();
		String[][] pretable = lmsBean.getPreReqTable();
		String nextNavScoId = "-1";
		for(int i=0; i < pretable.length; i++){
			if(pretable[i][0].equals(recentId) && (i != pretable.length-1)){
				nextNavScoId =  pretable[++i][0];
				break;
			}
		}
		return Integer.valueOf(nextNavScoId);
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
		
		boolean passed = (found == getNumOfSCOs()) && passedScos;
		// if advanceScore option is set update the score only if it is higher
		// <OLATEE-27>
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		ModuleConfiguration config = scormNode.getModuleConfiguration();
		if (config.getBooleanSafe(ScormEditController.CONFIG_ADVANCESCORE, true)) {
			if (currentPassed == null || !currentPassed.booleanValue()) {
				// </OLATEE-27>
				boolean increment = !attemptsIncremented && finish;
				ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
				ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(0.0f), currentEval.getGrade(),
						currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(), Boolean.valueOf(passed),
						currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
						currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
						currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
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
			ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
			ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(0.0f), currentEval.getGrade(),
					currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(), Boolean.valueOf(passed),
					currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
					currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
					currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
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
				ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
				ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), currentEval.getGrade(),
						currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(),
						Boolean.valueOf(passed), currentEval.getAssessmentStatus(),
						currentEval.getUserVisible(), currentEval.getCurrentRunStartDate(),
						currentEval.getCurrentRunCompletion(), currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
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
			ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(scormNode, userCourseEnv);
			ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), currentEval.getGrade(),
					currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(), Boolean.valueOf(passed),
					currentEval.getAssessmentStatus(), currentEval.getUserVisible(),
					currentEval.getCurrentRunStartDate(), currentEval.getCurrentRunCompletion(),
					currentEval.getCurrentRunStatus(), currentEval.getAssessmentID());
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
	
	public enum ScoLifecycle {
		launching,
		initialized,
		finished
	}
	
	public static class ScoState {
		
		private String scoId;
		private ScoLifecycle status;
		private final Map<String,String> cmis = new HashMap<>();
		
		public String getScoId() {
			return scoId;
		}
		
		public void setScoId(String scoId) {
			this.scoId = scoId;
		}
		
		public ScoLifecycle getStatus() {
			return status;
		}
		
		public void setStatus(ScoLifecycle status) {
			this.status = status;
		}
		
		public boolean isLaunched2() {
			return status == ScoLifecycle.initialized;
		}
		
		public boolean isLaunching2() {
			return status == ScoLifecycle.launching;
		}

		
		public boolean isCurrent(String id) {
			return scoId != null && scoId.equals(id);
		}
		
		public synchronized void cmiClear() {
			cmis.clear();
		}
		
		public synchronized String getCmi(String key) {
			return cmis.get(key);
		}
		
		public synchronized void putCmi(String key, String val) {
			cmis.remove(key);
			cmis.put(key, val);
		}
		
		public synchronized Map<String,String> copyCmis() {
			return new HashMap<>(cmis);
		}
	}
}

