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
package org.olat.course.assessment.manager;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentLoggingAction;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.model.AssessmentNodesLastModified;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Objects;

/**
 * 
 * Initial date: 20.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseAssessmentManagerImpl implements AssessmentManager {
	
	private static final Logger log = Tracing.createLoggerFor(CourseAssessmentManagerImpl.class);
	
	public static final String ASSESSMENT_DOCS_DIR = "assessmentdocs";
	
	private static final Float FLOAT_ZERO = Float.valueOf(0.0f);
	private static final Double DOUBLE_ZERO = Double.valueOf(0.0d);
	private static final Integer INTEGER_ZERO = Integer.valueOf(0);
	
	private final CourseGroupManager cgm;
	
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public CourseAssessmentManagerImpl(CourseGroupManager cgm) {
		this.cgm = cgm;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public AssessmentEntry getOrCreateAssessmentEntry(CourseNode courseNode, Identity assessedIdentity, Boolean entryRoot) {
		return assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, cgm.getCourseEntry(), courseNode.getIdent(), entryRoot, courseNode.getReferencedRepositoryEntry());
	}
	
	private AssessmentEntry getOrCreate(Identity assessedIdentity, String subIdent, Boolean entryRoot, RepositoryEntry referenceEntry) {
		return assessmentService.getOrCreateAssessmentEntry(assessedIdentity, null, cgm.getCourseEntry(), subIdent, entryRoot, referenceEntry);
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(CourseNode courseNode) {
		return assessmentService.loadAssessmentEntriesBySubIdent(cgm.getCourseEntry(), courseNode.getIdent());
	}
	
	@Override
	public List<AssessmentEntry> getAssessmentEntriesWithStatus(CourseNode courseNode, AssessmentEntryStatus status, boolean excludeZeroScore) {
		return assessmentService.loadAssessmentEntriesBySubIdentWithStatus(cgm.getCourseEntry(), courseNode.getIdent(), status, excludeZeroScore, true);
	}

	@Override
	public AssessmentEntry getAssessmentEntry(CourseNode courseNode, Identity assessedIdentity) {
		return assessmentService.loadAssessmentEntry(assessedIdentity, cgm.getCourseEntry(), courseNode.getIdent());
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(Identity assessedIdentity) {
		log.debug("Load assessment entries of {}", assessedIdentity);
		return assessmentService.loadAssessmentEntriesByAssessedIdentity(assessedIdentity, cgm.getCourseEntry());
	}

	@Override
	public List<AssessmentEntry> getAssessmentEntries(BusinessGroup assessedGoup, CourseNode courseNode) {
		return assessmentService.loadAssessmentEntries(assessedGoup, cgm.getCourseEntry(), courseNode.getIdent());
	}

	@Override
	public AssessmentEntry updateAssessmentEntry(AssessmentEntry assessmentEntry) {
		AssessmentEntry updateAssessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
		DBFactory.getInstance().commit();
		return updateAssessmentEntry;
	}

	@Override
	public void saveNodeAttempts(CourseNode courseNode, Identity identity, Identity assessedIdentity, Integer attempts, Date lastAttempt, Role by) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		initUserVisibility(nodeAssessment, course.getCourseEnvironment(), courseNode, identity, by);
		if(by == Role.coach) {
			nodeAssessment.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			nodeAssessment.setLastUserModified(new Date());
		}
		nodeAssessment.setAttempts(attempts);
		nodeAssessment.setLastAttempt(lastAttempt);
		assessmentService.updateAssessmentEntry(nodeAssessment);

		//node log
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "ATTEMPTS set to: " + attempts, by);

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), courseNode, entryRoot);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));	
	}

	@Override
	public void saveNodeComment(CourseNode courseNode, Identity identity, Identity assessedIdentity, String comment) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		initUserVisibility(nodeAssessment, course.getCourseEnvironment(), courseNode, identity, Role.coach);
		nodeAssessment.setComment(comment);
		assessmentService.updateAssessmentEntry(nodeAssessment);
		
		// node log
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "COMMENT set to: " + comment, null);

		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_USER_COMMENT_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), courseNode, entryRoot);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_USERCOMMENT_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiUserComment, "", StringHelper.stripLineBreaks(comment)));	
	}

	@Override
	public void addIndividualAssessmentDocument(CourseNode courseNode, Identity identity, Identity assessedIdentity, File document, String filename) {
		if(document == null) return;
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = document.getName();
		}
		
		try {
			File directory = getAssessmentDocumentsDirectory(courseNode, assessedIdentity);
			File targetFile = new File(directory, filename);
			if(targetFile.exists()) {
				String newName = FileUtils.rename(targetFile);
				targetFile = new File(directory, newName);
			}
			Files.copy(document.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			
			//update counter
			ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
			Boolean entryRoot = isEntryRoot(course, courseNode);
			AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
			initUserVisibility(nodeAssessment, course.getCourseEnvironment(), courseNode, identity, Role.coach);
			File[] docs = directory.listFiles(SystemFileFilter.FILES_ONLY);
			int numOfDocs = docs == null ? 0 : docs.length;
			nodeAssessment.setNumberOfAssessmentDocuments(numOfDocs);
			assessmentService.updateAssessmentEntry(nodeAssessment);
			
			// node log
			UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "assessment document added: " + filename, null);
			
			// user activity logging
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_DOCUMENT_ADDED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.assessmentDocument, "", StringHelper.stripLineBreaks(filename)));
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void removeIndividualAssessmentDocument(CourseNode courseNode, Identity identity, Identity assessedIdentity, File document) {
		if(document != null && document.exists()) {
			FileUtils.deleteFile(document);
			
			//update counter
			ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
			Boolean entryRoot = isEntryRoot(course, courseNode);
			AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
			File directory = getAssessmentDocumentsDirectory(courseNode, assessedIdentity);
			File[] docs = directory.listFiles(SystemFileFilter.FILES_ONLY);
			int numOfDocs = docs == null ? 0 : docs.length;
			nodeAssessment.setNumberOfAssessmentDocuments(numOfDocs);
			assessmentService.updateAssessmentEntry(nodeAssessment);

			// node log
			UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "assessment document removed: " + document.getName(), null);
			
			// user activity logging
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_DOCUMENT_REMOVED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.assessmentDocument, "", StringHelper.stripLineBreaks(document.getName())));
		}
	}
	
	@Override
	public void deleteIndividualAssessmentDocuments(CourseNode courseNode) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		String courseRelPath = course.getCourseEnvironment().getCourseBaseContainer().getRelPath();
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseRelPath, ASSESSMENT_DOCS_DIR, courseNode.getIdent());
		File file = path.toFile();
		if(file.exists()) {
			FileUtils.deleteDirsAndFiles(file, true, true);
		}
	}

	private File getAssessmentDocumentsDirectory(CourseNode cNode, Identity assessedIdentity) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		String courseRelPath = course.getCourseEnvironment().getCourseBaseContainer().getRelPath();
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), courseRelPath, ASSESSMENT_DOCS_DIR, cNode.getIdent(), "person_" + assessedIdentity.getKey());
		File file = path.toFile();
		if(!file.exists()) {
			file.mkdirs();
		}
		return file;
	}

	@Override
	public void saveNodeCoachComment(CourseNode courseNode, Identity assessedIdentity, String comment) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		nodeAssessment.setCoachComment(comment);
		assessmentService.updateAssessmentEntry(nodeAssessment);
		
		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_COACH_COMMENT_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), courseNode, entryRoot);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);

		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_COACHCOMMENT_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiCoachComment, "", StringHelper.stripLineBreaks(comment)));	
	}

	@Override
	public void incrementNodeAttempts(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnv, Role by) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		int attempts = nodeAssessment.getAttempts() == null ? 1 :nodeAssessment.getAttempts().intValue() + 1;
		nodeAssessment.setAttempts(attempts);
		nodeAssessment.setLastAttempt(new Date());
		if(by == Role.coach) {
			nodeAssessment.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			nodeAssessment.setLastUserModified(new Date());
		}
		assessmentService.updateAssessmentEntry(nodeAssessment);
		DBFactory.getInstance().commit();
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		DBFactory.getInstance().commit();
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(cgm.getCourseEntry(), courseNode);
		if(assessmentConfig.isAssessable()) {
			efficiencyStatementManager.updateUserEfficiencyStatement(userCourseEnv);
		}
		
		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_ATTEMPTS_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), courseNode, entryRoot);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
		
		// user activity logging
		ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
				getClass(), 
				LoggingResourceable.wrap(assessedIdentity), 
				LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));
	}
	
	@Override
	public void updateLastModifications(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnv, Role by) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		if(by == Role.coach) {
			nodeAssessment.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			nodeAssessment.setLastUserModified(new Date());
		}
		assessmentService.updateAssessmentEntry(nodeAssessment);
		DBFactory.getInstance().commit();
		
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(cgm.getCourseEntry(), courseNode);
		if(assessmentConfig.isAssessable()) {
			efficiencyStatementManager.updateUserEfficiencyStatement(userCourseEnv);
		}
	}

	@Override
	public void updateLastVisited(CourseNode courseNode, Identity assessedIdentity, Date lastVisit) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		assessmentService.setLastVisit(nodeAssessment, lastVisit);
		DBFactory.getInstance().commit();
	}

	@Override
	public void updateCurrentCompletion(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnvironment,
			Date start, Double currentCompletion, AssessmentRunStatus runStatus, Role by) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		nodeAssessment.setCurrentRunCompletion(currentCompletion);
		nodeAssessment.setCurrentRunStartDate(start);
		nodeAssessment.setCurrentRunStatus(runStatus);
		if(by == Role.coach) {
			nodeAssessment.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			nodeAssessment.setLastUserModified(new Date());
		}
		assessmentService.updateAssessmentEntry(nodeAssessment);
		DBFactory.getInstance().commit();
	}

	@Override
	public void updateCompletion(CourseNode courseNode, Identity assessedIdentity, UserCourseEnvironment userCourseEnvironment,
			Double completion, AssessmentEntryStatus status, Role by) {
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		nodeAssessment.setCompletion(completion);
		nodeAssessment.setAssessmentStatus(status);
		if(by == Role.coach) {
			nodeAssessment.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			nodeAssessment.setLastUserModified(new Date());
		}
		assessmentService.updateAssessmentEntry(nodeAssessment);
		DBFactory.getInstance().commit();
		
		nodeAccessService.onStatusUpdated(courseNode, userCourseEnvironment, status);
		DBFactory.getInstance().commit();
		
		ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		DBFactory.getInstance().commit();
	}

	@Override
	public void updateFullyAssessed(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Boolean fullyAssessed,
			AssessmentEntryStatus status) {
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry nodeAssessment = getOrCreateAssessmentEntry(courseNode, assessedIdentity, entryRoot);
		if (Objects.equal(fullyAssessed, nodeAssessment.getFullyAssessed())) {
			// Fully assess can only set once
			return;
		}
		
		nodeAssessment.setAssessmentStatus(status);
		nodeAssessment.setFullyAssessed(fullyAssessed);
		
		assessmentService.updateAssessmentEntry(nodeAssessment);
		DBFactory.getInstance().commit();
		
		ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		DBFactory.getInstance().commit();
		
		updateUserEfficiencyStatement(userCourseEnvironment);
		generateCertificate(userCourseEnvironment, course);
	}

	@Override
	public void saveScoreEvaluation(CourseNode courseNode, Identity identity, Identity assessedIdentity,
			ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnv,
			boolean incrementUserAttempts, Role by) {
		final ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		final CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		
		Float score = scoreEvaluation.getScore();
		Boolean passed = scoreEvaluation.getPassed();
		Long assessmentId = scoreEvaluation.getAssessmentID();
		
		String subIdent = courseNode.getIdent();
		RepositoryEntry referenceEntry = courseNode.getReferencedRepositoryEntry();
		Boolean entryRoot = isEntryRoot(course, courseNode);
		AssessmentEntry assessmentEntry = getOrCreate(assessedIdentity, subIdent, entryRoot, referenceEntry);
		if(referenceEntry != null && !referenceEntry.equals(assessmentEntry.getReferenceEntry())) {
			assessmentEntry.setReferenceEntry(referenceEntry);
		}
		if(by == Role.coach) {
			assessmentEntry.setLastCoachModified(new Date());
		} else if(by == Role.user) {
			assessmentEntry.setLastUserModified(new Date());
		}
		if(score == null) {
			assessmentEntry.setScore(null);
		} else {
			assessmentEntry.setScore(new BigDecimal(Float.toString(score)));
		}
		assessmentEntry.setGrade(scoreEvaluation.getGrade());
		assessmentEntry.setGradeSystemIdent(scoreEvaluation.getGradeSystemIdent());
		assessmentEntry.setPerformanceClassIdent(scoreEvaluation.getPerformanceClassIdent());
		assessmentEntry.setPassed(passed);
		if(assessmentId != null) {
			assessmentEntry.setAssessmentId(assessmentId);
		}
		if(scoreEvaluation.getAssessmentStatus() != null) {
			AssessmentEntryStatus previousStatus = assessmentEntry.getAssessmentStatus();
			assessmentEntry.setAssessmentStatus(scoreEvaluation.getAssessmentStatus());
			if (AssessmentEntryStatus.done == scoreEvaluation.getAssessmentStatus() && AssessmentEntryStatus.done != previousStatus) {
				assessmentEntry.setAssessmentDoneBy(identity);
			} else if (AssessmentEntryStatus.done != scoreEvaluation.getAssessmentStatus()) {
				assessmentEntry.setAssessmentDoneBy(null);
			}
		}
		assessmentEntry.setUserVisibility(scoreEvaluation.getUserVisible());
		if(assessmentEntry.getScore() != null || assessmentEntry.getPassed() != null) {
			initUserVisibility(assessmentEntry, courseEnv, courseNode, identity, by);
		}
		if(scoreEvaluation.getCurrentRunCompletion() != null) {
			assessmentEntry.setCurrentRunCompletion(scoreEvaluation.getCurrentRunCompletion());
		}
		if(scoreEvaluation.getCurrentRunStatus() != null) {
			assessmentEntry.setCurrentRunStatus(scoreEvaluation.getCurrentRunStatus());
		}
		if(scoreEvaluation.getCurrentRunStartDate() != null) {
			assessmentEntry.setCurrentRunCompletion(scoreEvaluation.getCurrentRunCompletion());
		}
		
		Integer attempts = null;
		if(incrementUserAttempts) {
			attempts = assessmentEntry.getAttempts() == null ? 1 :assessmentEntry.getAttempts().intValue() + 1;
			assessmentEntry.setAttempts(attempts);
			assessmentEntry.setLastAttempt(new Date());
		}
		assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
		DBFactory.getInstance().commit();
		
		nodeAccessService.onScoreUpdated(courseNode, userCourseEnv, score, assessmentEntry.getUserVisibility());
		nodeAccessService.onPassedUpdated(courseNode, userCourseEnv, passed, assessmentEntry.getUserVisibility());
		nodeAccessService.onStatusUpdated(courseNode, userCourseEnv, assessmentEntry.getAssessmentStatus());
		DBFactory.getInstance().commit();
		
		//reevalute the tree
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		DBFactory.getInstance().commit();
		
		// node log
		UserNodeAuditManager am = courseEnv.getAuditManager();
		am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "score set to: " + String.valueOf(scoreEvaluation.getScore()), by);
		if (StringHelper.containsNonWhitespace(scoreEvaluation.getGrade())) {
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "grade set to: " + String.valueOf(scoreEvaluation.getGrade()), by);
		}
		logAuditPassed(courseNode, identity, by, userCourseEnv, scoreEvaluation.getPassed());
		if(scoreEvaluation.getAssessmentID()!=null) {
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "assessmentId set to: " + scoreEvaluation.getAssessmentID().toString(), by);
		}
		
		// notify about changes
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), courseNode, entryRoot);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
		
		// user activity logging
		logActivityPassed(assessedIdentity, scoreEvaluation.getPassed());
		if (scoreEvaluation.getScore()!=null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_SCORE_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiScore, "", String.valueOf(scoreEvaluation.getScore())));
		}
		if (scoreEvaluation.getGrade()!=null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_GRADE_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiGrade, "", String.valueOf(scoreEvaluation.getGrade())));
		}
		if (incrementUserAttempts && attempts!=null) {
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "ATTEMPTS set to: " + attempts, by);
			if(identity != null) {
				ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
						getClass(), 
						LoggingResourceable.wrap(identity), 
						LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));	
			} else {
				ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_ATTEMPTS_UPDATED, 
						getClass(), 
						LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiAttempts, "", String.valueOf(attempts)));	
			}
		}
		
		updateUserEfficiencyStatement(userCourseEnv);
		generateCertificate(userCourseEnv, course);
	}
	
	@Override
	public Overridable<Boolean> getRootPassed(UserCourseEnvironment userCourseEnvironment) {
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		CourseEnvironment courseEnv = userCourseEnvironment.getCourseEnvironment();
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		String subIdent = rootNode.getIdent();
		RepositoryEntry referenceEntry = rootNode.getReferencedRepositoryEntry();
		AssessmentEntry assessmentEntry = getOrCreate(assessedIdentity, subIdent, Boolean.TRUE, referenceEntry);
		return assessmentEntry.getPassedOverridable();
	}

	@Override
	public Overridable<Boolean> overrideRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment, Boolean passed) {
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		CourseEnvironment courseEnv = userCourseEnvironment.getCourseEnvironment();
		
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		String subIdent = rootNode.getIdent();
		RepositoryEntry referenceEntry = rootNode.getReferencedRepositoryEntry();
		AssessmentEntry assessmentEntry = getOrCreate(assessedIdentity, subIdent, Boolean.TRUE, referenceEntry);
		
		Date now = new Date();
		assessmentEntry.setLastCoachModified(now);
		assessmentEntry.getPassedOverridable().override(passed, coach, now);
		assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
		DBFactory.getInstance().commit();
		
		nodeAccessService.onPassedUpdated(rootNode, userCourseEnvironment, passed, Boolean.TRUE);
		DBFactory.getInstance().commit();
		
		ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		DBFactory.getInstance().commit();
		
		logAuditPassed(rootNode, coach, Role.coach, userCourseEnvironment, passed);
		logActivityPassed(assessedIdentity, passed);
		
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), rootNode, Boolean.TRUE);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
		
		updateUserEfficiencyStatement(userCourseEnvironment);
		generateCertificate(userCourseEnvironment, course);
		
		return assessmentEntry.getPassedOverridable();
	}

	@Override
	public Overridable<Boolean> resetRootPassed(Identity coach, UserCourseEnvironment userCourseEnvironment) {
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		
		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		CourseEnvironment courseEnv = userCourseEnvironment.getCourseEnvironment();
		
		CourseNode rootNode = courseEnv.getRunStructure().getRootNode();
		String subIdent = rootNode.getIdent();
		RepositoryEntry referenceEntry = rootNode.getReferencedRepositoryEntry();
		AssessmentEntry assessmentEntry = getOrCreate(assessedIdentity, subIdent, Boolean.TRUE, referenceEntry);
		
		Date now = new Date();
		assessmentEntry.setLastCoachModified(now);
		assessmentEntry.getPassedOverridable().reset();
		assessmentEntry = assessmentService.updateAssessmentEntry(assessmentEntry);
		DBFactory.getInstance().commit();
		
		Boolean passed = assessmentEntry.getPassed();
		nodeAccessService.onPassedUpdated(rootNode, userCourseEnvironment, passed, Boolean.TRUE);
		DBFactory.getInstance().commit();
		
		ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
		scoreAccounting.evaluateAll(true);
		DBFactory.getInstance().commit();
		
		logAuditPassed(rootNode, coach, Role.coach, userCourseEnvironment, passed);
		logActivityPassed(assessedIdentity, passed);
		
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED,
				assessedIdentity, cgm.getCourseEntry(), rootNode, Boolean.TRUE);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(ace, course);
		
		updateUserEfficiencyStatement(userCourseEnvironment);
		generateCertificate(userCourseEnvironment, course);
		
		return assessmentEntry.getPassedOverridable();
	}

	private void logAuditPassed(CourseNode courseNode, Identity identity, Role by,
			UserCourseEnvironment userCourseEnvironment, Boolean passed) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		if(passed != null) {
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "passed set to: " + passed.toString(), by);
		} else {
			am.appendToUserNodeLog(courseNode, identity, assessedIdentity, "passed set to \"undefined\"", by);
		}
	}

	private void logActivityPassed(Identity assessedIdentity, Boolean passed) {
		if (passed != null) {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", String.valueOf(passed)));
		} else {
			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_PASSED_UPDATED, 
					getClass(), 
					LoggingResourceable.wrap(assessedIdentity), 
					LoggingResourceable.wrapNonOlatResource(StringResourceableType.qtiPassed, "", "undefined"));
		}
	}

	private void updateUserEfficiencyStatement(UserCourseEnvironment userCourseEnvironment) {
		CourseEnvironment courseEnv = userCourseEnvironment.getCourseEnvironment();
		// write only when enabled for this course
		if (courseEnv.getCourseConfig().isEfficencyStatementEnabled()) {
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
			CourseNode rootNode = userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode();
			
			List<AssessmentNodeData> data = new ArrayList<>(50);
			AssessmentNodesLastModified lastModifications = new AssessmentNodesLastModified();
			
			AssessmentHelper.getAssessmentNodeDataList(0, rootNode, scoreAccounting, userCourseEnvironment, true, true,
					true, data, lastModifications);
			efficiencyStatementManager.updateUserEfficiencyStatement(assessedIdentity, courseEnv, data, lastModifications, cgm.getCourseEntry());
		}
	}

	private void generateCertificate(UserCourseEnvironment userCourseEnvironment, ICourse course) {
		if (course.getCourseConfig().isAutomaticCertificationEnabled()) {
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			ScoreAccounting scoreAccounting = userCourseEnvironment.getScoreAccounting();
			CourseNode rootNode = userCourseEnvironment.getCourseEnvironment().getRunStructure().getRootNode();
			AssessmentEvaluation rootEval = scoreAccounting.evalCourseNode(rootNode);
			if (rootEval != null && rootEval.getPassed() != null && rootEval.getPassed().booleanValue()
					&& certificatesManager.isCertificationAllowed(assessedIdentity, cgm.getCourseEntry())) {
				CertificateTemplate template = null;
				Long templateId = course.getCourseConfig().getCertificateTemplate();
				if (templateId != null) {
					template = certificatesManager.getTemplateById(templateId);
				}
				CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, rootEval.getScore(),
						rootEval.getMaxScore(), rootEval.getPassed(), rootEval.getCompletion());
				CertificateConfig config = CertificateConfig.builder()
						.withCustom1(course.getCourseConfig().getCertificateCustom1())
						.withCustom2(course.getCourseConfig().getCertificateCustom2())
						.withCustom3(course.getCourseConfig().getCertificateCustom3())
						.withSendEmailBcc(true)
						.withSendEmailLinemanager(true)
						.withSendEmailIdentityRelations(true)
						.build();
				certificatesManager.generateCertificate(certificateInfos, cgm.getCourseEntry(), template, config);
			}
		}
	}

	@Override
	public Float getNodeScore(CourseNode courseNode, Identity identity) {
		if (courseNode == null) {
			return FLOAT_ZERO; // return default value
		}
		
		AssessmentEntry entry = assessmentService.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		if(entry != null && entry.getScore() != null) {
			return entry.getScore().floatValue();
		}
		return FLOAT_ZERO;
	}

	@Override
	public String getNodeComment(CourseNode courseNode, Identity identity) {
		AssessmentEntry entry = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return entry == null ? null : entry.getComment();
	}

	@Override
	public List<File> getIndividualAssessmentDocuments(CourseNode courseNode, Identity identity) {
		File directory = getAssessmentDocumentsDirectory(courseNode, identity);
		File[] documents = directory.listFiles(SystemFileFilter.FILES_ONLY);
		List<File> documentList = new ArrayList<>();
		if(documents != null && documents.length > 0) {
			for(File document:documents) {
				documentList.add(document);
			}
		}
		return documentList;
	}

	@Override
	public String getNodeCoachComment(CourseNode courseNode, Identity identity) {
		AssessmentEntry entry = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return entry == null ? null : entry.getCoachComment();
	}

	@Override
	public Boolean getNodePassed(CourseNode courseNode, Identity identity) {
		if (courseNode == null) {
			return Boolean.FALSE; // return default value
		}
		
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null ? null : nodeAssessment.getPassed();
	}

	@Override
	public Integer getNodeAttempts(CourseNode courseNode, Identity identity) {
		if(courseNode == null) return INTEGER_ZERO;
		
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null || nodeAssessment.getAttempts() == null  ? INTEGER_ZERO : nodeAssessment.getAttempts();
	}

	@Override
	public Double getNodeCompletion(CourseNode courseNode, Identity identity) {
		if(courseNode == null) return DOUBLE_ZERO;
		
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null || nodeAssessment.getCompletion() == null  ? DOUBLE_ZERO : nodeAssessment.getCompletion();
	}
	
	@Override
	public Double getNodeCurrentRunCompletion(CourseNode courseNode, Identity identity) {
		if(courseNode == null) return DOUBLE_ZERO;
		
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null || nodeAssessment.getCurrentRunCompletion() == null  ? DOUBLE_ZERO : nodeAssessment.getCurrentRunCompletion();
	}

	@Override
	public Long getAssessmentID(CourseNode courseNode, Identity identity) {
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null ? null : nodeAssessment.getAssessmentId();
	}

	@Override
	public Date getScoreLastModifiedDate(CourseNode courseNode, Identity identity) {
		if(courseNode == null) return null;
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());
		return nodeAssessment == null ? null : nodeAssessment.getLastModified();
	}

	@Override
	public Boolean getNodeFullyAssessed(CourseNode courseNode, Identity identity) {
		AssessmentEntry nodeAssessment = assessmentService
				.loadAssessmentEntry(identity, cgm.getCourseEntry(), courseNode.getIdent());	
		return nodeAssessment == null ? null : nodeAssessment.getFullyAssessed();
	}
	
	@Override
	public OLATResourceable createOLATResourceableForLocking(Identity assessedIdentity) {
		return OresHelper.createOLATResourceableInstance("AssessmentManager::Identity", assessedIdentity.getKey());
	}
	
	@Override
	public void registerForAssessmentChangeEvents(GenericEventListener gel, Identity identity) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(gel, identity, cgm.getCourseEntry().getOlatResource());
	}

	@Override
	public void deregisterFromAssessmentChangeEvents(GenericEventListener gel) {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(gel, cgm.getCourseEntry().getOlatResource());
	}

	private Boolean isEntryRoot(ICourse course, CourseNode courseNode) {
		return course.getCourseEnvironment().getRunStructure().getRootNode().getIdent().equals(courseNode.getIdent());
	}
	
	/**
	 * The userVisibility is set when assessment data are set the first time.
	 * Assessment data is: score (and grade), passed, user comment, documents.
	 */
	public void initUserVisibility(AssessmentEntry assessmentEntry, CourseEnvironment courseEnv, CourseNode courseNode, Identity coach, Role by) {
		if (assessmentEntry.getUserVisibility() != null) return;
		
		boolean done = assessmentEntry.getAssessmentStatus() != null &&  AssessmentEntryStatus.done == assessmentEntry.getAssessmentStatus();
		boolean coachCanNotEdit = Role.coach == by
				&& !courseEnv.getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY)
				&& !cgm.isIdentityAnyCourseAdministrator(coach);
		
		Boolean initialUserVisibility = courseAssessmentService.getAssessmentConfig(cgm.getCourseEntry(), courseNode)
				.getInitialUserVisibility(done, coachCanNotEdit);
		assessmentEntry.setUserVisibility(initialUserVisibility);
	}
	
}
