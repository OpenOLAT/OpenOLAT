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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.LongRunnable;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskAwareRunnable;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentLoggingAction;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.bulk.BulkAssessmentOverviewController;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentFeedback;
import org.olat.course.assessment.model.BulkAssessmentRow;
import org.olat.course.assessment.model.BulkAssessmentSettings;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.ta.ReturnboxController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;



/**
 * The task which execute the bulk assessment<br>
 * 
 * Initial date: 20.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentTask implements LongRunnable, TaskAwareRunnable {

	private static final long serialVersionUID = 4614724183354689151L;
	private static final Logger log = Tracing.createLoggerFor(BulkAssessmentTask.class);
	
	private OLATResourceable courseRes;
	private String courseNodeIdent;
	private BulkAssessmentDatas datas;
	private BulkAssessmentSettings settings;
	private Long coachedIdentity;
	
	private transient Task task;
	private transient File unzipped;

	public BulkAssessmentTask(OLATResourceable courseRes, CourseNode courseNode, BulkAssessmentDatas datas,
			Long coachedIdentity) {
		this.courseRes = OresHelper.clone(courseRes);
		this.courseNodeIdent = courseNode.getIdent();
		this.settings = new BulkAssessmentSettings(courseNode);
		this.datas = datas;
		this.coachedIdentity = coachedIdentity;
	}
	
	public String getCourseNodeIdent() {
		return courseNodeIdent;
	}
	
	public BulkAssessmentSettings getSettings() {
		return settings;
	}
	
	public BulkAssessmentDatas getDatas() {
		return datas;
	}
	
	@Override
	public Queue getExecutorsQueue() {
		return Queue.sequential;
	}

	@Override
	public void setTask(Task task) {
		this.task = task;
	}

	/**
	 * Used by to task executor, without any GUI
	 */
	@Override
	public void run() {
		final List<BulkAssessmentFeedback> feedbacks = new ArrayList<>();
		try {
			log.info(Tracing.M_AUDIT, "Start process bulk assessment");

			LoggingResourceable[] infos = new LoggingResourceable[2];
			if(task != null && task.getCreator() != null) {
				UserSession session = new UserSession();
				session.setIdentity(task.getCreator());
				session.setSessionInfo(new SessionInfo(task.getCreator().getKey()));
				ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(session);
				infos[0] = LoggingResourceable.wrap(courseRes, OlatResourceableType.course);
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(infos[0]);
				infos[1] = LoggingResourceable.wrap(getCourseNode());
				ThreadLocalUserActivityLogger.addLoggingResourceInfo(infos[1]);
			}	

			doProcess(feedbacks);
			log.info(Tracing.M_AUDIT, "End process bulk assessment");
			cleanup();

			ThreadLocalUserActivityLogger.log(AssessmentLoggingAction.ASSESSMENT_BULK, getClass(), infos);
		} catch (Exception e) {
			log.error("", e);
			feedbacks.add(new BulkAssessmentFeedback("", "bulk.assessment.error"));
			throw e;
		} finally {
			cleanupUnzip();
			sendFeedback(feedbacks);
		}
	}
	
	public List<BulkAssessmentFeedback> process() {
		List<BulkAssessmentFeedback> feedbacks = new ArrayList<>();
		try {
			LoggingResourceable infos = LoggingResourceable.wrap(getCourseNode());
			ThreadLocalUserActivityLogger.addLoggingResourceInfo(infos);
			
			doProcess(feedbacks);
			cleanup();
		} catch (Exception e) {
			log.error("", e);
			feedbacks.add(new BulkAssessmentFeedback("", "bulk.assessment.error"));
		} finally {
			cleanupUnzip();
		}
		return feedbacks;
	}
	
	private void cleanup() {
		if(StringHelper.containsNonWhitespace(datas.getDataBackupFile())) {
			File backupFile = VFSManager.olatRootFile(datas.getDataBackupFile());
			if(backupFile.exists()) {
				File dir = backupFile.getParentFile();
				if(dir != null && dir.exists()) {
					FileUtils.deleteDirsAndFiles(dir, true, true);
				}
			}
		}
		cleanupUnzip();
	}
	
	private void cleanupUnzip() {
		try {
			if(unzipped != null && unzipped.exists()) {
				FileUtils.deleteDirsAndFiles(unzipped, true, true);
			}
		} catch (Exception e) {
			log.error("Cannot cleanup unzipped datas after bulk assessment", e);
		}
	}
	
	private void sendFeedback(List<BulkAssessmentFeedback> feedbacks) {
		if(task == null) {
			log.error("Haven't a task to know creator and modifiers of the task");
			return;
		}
		
		Identity creator = task.getCreator();
		String language = creator.getUser().getPreferences().getLanguage();
		Locale locale = I18nManager.getInstance().getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(BulkAssessmentOverviewController.class, locale,
				Util.createPackageTranslator(AssessmentManager.class, locale));
		MailManager mailManager = CoreSpringFactory.getImpl(MailManager.class);
		TaskExecutorManager taskManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		
		String feedbackStr = renderFeedback(feedbacks, translator);
		
		MailBundle mail = new MailBundle();
		mail.setToId(creator);
		mail.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
		List<Identity> modifiers = taskManager.getModifiers(task);
		if(!modifiers.isEmpty()) {
			ContactList cc = new ContactList("CC");
			cc.addAllIdentites(modifiers);
			mail.setContactList(cc);
		}
		
		String businessPath = "";
		ICourse course = CourseFactory.loadCourse(courseRes);
		CourseNode node = course.getRunStructure().getNode(courseNodeIdent);
		String courseTitle = course.getCourseTitle();
		String nodeTitle = node.getShortTitle();
		String numOfAssessedIds = Integer.toString(datas == null ? 0 : datas.getRowsSize());
		String date = Formatter.getInstance(locale).formatDateAndTime(new Date());
		
		mail.setContext(new MailContextImpl(courseRes, courseNodeIdent, businessPath));
		String subject = translator.translate("confirmation.mail.subject", new String[]{ courseTitle, nodeTitle });
		String body = translator.translate("confirmation.mail.body", new String[]{ courseTitle, nodeTitle, feedbackStr, numOfAssessedIds, date });
		mail.setContent(subject, body);
		mailManager.sendMessage(mail);
	}
	
	public static String renderFeedback(List<BulkAssessmentFeedback> feedbacks, Translator translator) {
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		StringBuilder sb = new StringBuilder();
		for(BulkAssessmentFeedback feedback:feedbacks) {
			String errorKey = feedback.getErrorKey();
			String msg = translator.translate(errorKey);
			String assessedName;
			if(feedback.getAssessedIdentity() != null) {
				assessedName = userManager.getUserDisplayName(feedback.getAssessedIdentity());
			} else {
				assessedName = feedback.getAssessedId();
			}
			sb.append(assessedName).append(": ").append(msg).append("\n");
		}
		return sb.toString();
	}
	
	public static boolean isBulkAssessable(CourseNode courseNode) {
		boolean bulkAssessability = false;
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if (assessmentConfig.isBulkEditable()) {
			// Now a more fine granular check on bulk features. Only show wizard for nodes that have at least one
			BulkAssessmentSettings settings = new BulkAssessmentSettings(courseNode);
			if (settings.isHasPassed() || settings.isHasScore() || settings.isHasUserComment() || settings.isHasReturnFiles()) {
				bulkAssessability = true;
			}
		}
		
		return bulkAssessability;
	}

	private CourseNode getCourseNode() {
		ICourse course = CourseFactory.loadCourse(courseRes);
		return course.getRunStructure().getNode(courseNodeIdent);
	}
	
	private void doProcess(List<BulkAssessmentFeedback> feedbacks) {
		final DB dbInstance = DBFactory.getInstance();
		final BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		final CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		final Identity coachIdentity = securityManager.loadIdentityByKey(coachedIdentity);
		final ICourse course = CourseFactory.loadCourse(courseRes);
		final CourseNode courseNode = getCourseNode();
		final AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		
		final boolean hasUserComment = assessmentConfig.hasComment();
		final boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		final boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		final boolean hasReturnFiles = (StringHelper.containsNonWhitespace(datas.getReturnFiles())
				&& (courseNode instanceof TACourseNode || courseNode instanceof GTACourseNode));
		
		if(hasReturnFiles) {
			try {
				File returnFilesZipped = VFSManager.olatRootFile(datas.getReturnFiles());
				String tmp = FolderConfig.getCanonicalTmpDir();
				unzipped = new File(tmp, UUID.randomUUID().toString() + File.separatorChar);
				unzipped.mkdirs();
				ZipUtil.unzip(returnFilesZipped, unzipped);
			} catch (Exception e) {
				log.error("Cannot unzip the return files during bulk assessment", e);
			}
		}
		
		Float min = null;
		Float max = null;
		Float cut = null;
		if (hasScore) {
			min = assessmentConfig.getMinScore();
			max = assessmentConfig.getMaxScore();
		}
		if (hasPassed) {
			cut = assessmentConfig.getCutValue();
		}
		
		int count = 0;
		List<BulkAssessmentRow> rows = datas.getRows();
		for(BulkAssessmentRow row:rows) {
			Long identityKey = row.getIdentityKey();
			if(identityKey == null) {
				feedbacks.add(new BulkAssessmentFeedback("bulk.action.no.such.user", row.getAssessedId()));
				continue;//nothing to do
			}

			Identity identity = securityManager.loadIdentityByKey(identityKey);
			IdentityEnvironment ienv = new IdentityEnvironment(identity, Roles.userRoles());
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
			
			//update comment, empty string will reset comment
			String userComment = row.getComment();
			if(hasUserComment && userComment != null){
				courseAssessmentService.updatedUserComment(courseNode, userComment, uce, coachIdentity);
				//LD: why do we have to update the efficiency statement?
				//EfficiencyStatementManager esm =	EfficiencyStatementManager.getInstance();
				//esm.updateUserEfficiencyStatement(uce);
			}
			
			boolean statusVisibilitySet = false;
			
			//update score
			Float score = row.getScore();
			if(hasScore && score != null) {
				// score < minimum score
				if ((min != null && score.floatValue() < min.floatValue()) || (score.floatValue() < AssessmentHelper.MIN_SCORE_SUPPORTED)) {
					// "bulk.action.lessThanMin";
				}
				// score > maximum score
				else if ((max != null && score.floatValue() > max.floatValue())
						|| (score.floatValue() > AssessmentHelper.MAX_SCORE_SUPPORTED)) {
					// "bulk.action.greaterThanMax";
				} else {
					// score between minimum and maximum score
					ScoreEvaluation se;
					if (hasPassed && cut != null){
						Boolean passed = (score.floatValue() >= cut.floatValue()) ? Boolean.TRUE	: Boolean.FALSE;
						se = new ScoreEvaluation(score, passed, datas.getStatus(), datas.getVisibility(), null, null, null, null);
					} else {
						se = new ScoreEvaluation(score, null, datas.getStatus(), datas.getVisibility(), null, null, null, null);
					}
					
					// Update score,passed properties in db, and the user's efficiency statement
					courseAssessmentService.updateScoreEvaluation(courseNode, se, uce, coachIdentity, false, Role.auto);
					statusVisibilitySet = true;
				}
			}
			
			Boolean passed = row.getPassed();
			if (hasPassed && passed != null && cut == null) { // Configuration of manual assessment --> Display passed/not passed: yes, Type of display: Manual by tutor
				ScoreEvaluation seOld = courseAssessmentService.getAssessmentEvaluation(courseNode, uce);
				Float oldScore = seOld.getScore();
				ScoreEvaluation se = new ScoreEvaluation(oldScore, passed, datas.getStatus(), datas.getVisibility(), null, null, null, null);
				// Update score,passed properties in db, and the user's efficiency statement
				boolean incrementAttempts = false;
				courseAssessmentService.updateScoreEvaluation(courseNode, se, uce, coachIdentity, incrementAttempts, Role.auto);
				statusVisibilitySet = true;
			}
			
			if(hasReturnFiles && row.getReturnFiles() != null && !row.getReturnFiles().isEmpty()) {
				Optional<Path> assessedFolder = getAssessedFolder(row, identity);
				if(assessedFolder.isPresent()) {
					processReturnFile(courseNode, row, uce, assessedFolder.get().toFile(), coachIdentity);
				}
			}
			
			if(courseNode instanceof GTACourseNode) {
				boolean acceptSubmission = datas.getAcceptSubmission() != null && datas.getAcceptSubmission().booleanValue();

				//push the state further
				GTACourseNode gtaNode = (GTACourseNode)courseNode;
				if((hasScore && score != null) || (hasPassed && passed != null)) {
					//pushed to graded
					updateTasksState(gtaNode, uce, TaskProcess.grading, acceptSubmission);
				} else if(hasReturnFiles) {
					//push to revised
					updateTasksState(gtaNode, uce, TaskProcess.correction, acceptSubmission);
				}
			}
			
			if(!statusVisibilitySet && (datas.getStatus() != null || datas.getVisibility() != null)) {
				ScoreEvaluation seOld = courseAssessmentService.getAssessmentEvaluation(courseNode, uce);
				ScoreEvaluation se = new ScoreEvaluation(seOld.getScore(), seOld.getPassed(),
						datas.getStatus(), datas.getVisibility(),
						seOld.getCurrentRunStartDate(), seOld.getCurrentRunCompletion(),
						seOld.getCurrentRunStatus(), seOld.getAssessmentID());
				// Update score,passed properties in db, and the user's efficiency statement
				boolean incrementAttempts = false;
				courseAssessmentService.updateScoreEvaluation(courseNode, se, uce, coachIdentity, incrementAttempts, Role.auto);
			}
			
			if(count++ % 5 == 0) {
				dbInstance.commitAndCloseSession();
			} else {
				dbInstance.commit();
			}
		}
	}
	
	private Optional<Path> getAssessedFolder(BulkAssessmentRow row, Identity identity) {
		try {
			String assessedId = row.getAssessedId();
			Optional<Path> assessedFolder = Files.walk(unzipped.toPath())
					.filter(Files::isDirectory)
					.filter(path -> path.getFileName().toString().equals(assessedId))
					.findAny();
			if (assessedFolder.isEmpty()) {
				String username = identity.getUser().getProperty(UserConstants.NICKNAME, null);
				if (StringHelper.containsNonWhitespace(username)) {
					assessedFolder = Files.walk(unzipped.toPath())
							.filter(Files::isDirectory)
							.filter(path -> path.getFileName().toString().equals(username))
							.findAny();
				}
			}
			return assessedFolder;
		} catch (IOException e) {
			// not found
		}
		return Optional.empty();
	}
	
	private void updateTasksState(GTACourseNode courseNode, UserCourseEnvironment uce, TaskProcess status, boolean acceptSubmission) {
		final GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		Identity identity = uce.getIdentityEnvironment().getIdentity();
		RepositoryEntry entry = uce.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
		org.olat.course.nodes.gta.Task gtaTask;
		TaskList taskList = gtaManager.getTaskList(entry, courseNode);
		if(taskList == null) {
			taskList = gtaManager.createIfNotExists(entry, courseNode);
			gtaTask = gtaManager.createTask(null, taskList, status, null, identity, courseNode);
		} else {
			gtaTask = gtaManager.getTask(identity, taskList);
			if(gtaTask == null) {
				gtaTask = gtaManager.createTask(null, taskList, status, null, identity, courseNode);
			}
		}
		
		if(gtaTask == null) {
			log.error("GTA Task is null by bulk assessment for: " + identity + " in entry:" + entry + " " + courseNode.getIdent());
		} else if(status == TaskProcess.correction) {
			int iteration = gtaTask.getRevisionLoop() <= 0 ? 1 : gtaTask.getRevisionLoop() + 1;
			gtaManager.updateTask(gtaTask, status, iteration, courseNode, false, null, Role.auto);
		} else if(status == TaskProcess.grading && acceptSubmission) {
			if(gtaTask.getTaskStatus() == TaskProcess.review
					|| gtaTask.getTaskStatus() == TaskProcess.correction
					|| gtaTask.getTaskStatus() == TaskProcess.revision) {
				gtaTask = gtaManager.reviewedTask(gtaTask, courseNode, null, Role.auto);
			}
			TaskProcess nextStep = gtaManager.nextStep(status, courseNode);
			gtaManager.updateTask(gtaTask, nextStep, courseNode, false, null, Role.auto);
		}
	}
	
	private void processReturnFile(CourseNode courseNode, BulkAssessmentRow row, UserCourseEnvironment uce, File assessedFolder, Identity coachIdentity) {
		Identity identity = uce.getIdentityEnvironment().getIdentity();
		VFSContainer returnBox = getReturnBox(uce, courseNode, identity);
		if(returnBox != null) {
			for(String returnFilename:row.getReturnFiles()) {
				File returnFile = new File(assessedFolder, returnFilename);
				VFSItem currentReturnLeaf = returnBox.resolve(returnFilename);
				if(currentReturnLeaf != null) {
					//remove the current file (delete make a version if it is enabled)
					currentReturnLeaf.delete();
				}

				VFSLeaf returnLeaf = returnBox.createChildLeaf(returnFilename);
				if(returnFile.exists()) {
					try(InputStream inStream = new FileInputStream(returnFile)) {
						VFSManager.copyContent(inStream, returnLeaf, coachIdentity);
					} catch (IOException e) {
						log.error("Cannot copy return file {} from {}", returnFilename, row.getIdentityKey(), e);
					}
				}
			}
		}
	}
	
	/**
	 * Return the target folder of the assessed identity. This is a factory method which take care
	 * of the type of the course node.
	 * 
	 * @param uce
	 * @param courseNode
	 * @param identity
	 * @return
	 */
	private VFSContainer getReturnBox(UserCourseEnvironment uce, CourseNode courseNode, Identity identity) {
		VFSContainer returnContainer = null;
		if(courseNode instanceof GTACourseNode) {
			final GTAManager gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
			CourseEnvironment courseEnv = uce.getCourseEnvironment();
			returnContainer = gtaManager.getCorrectionContainer(courseEnv, (GTACourseNode)courseNode, identity);
		} else {
			String returnPath = ReturnboxController.getReturnboxPathRelToFolderRoot(uce.getCourseEnvironment(), courseNode);
			VFSContainer rootFolder = VFSManager.olatRootContainer(returnPath, null);
			VFSItem assessedItem = rootFolder.resolve(identity.getName());
			if(assessedItem == null) {
				returnContainer = rootFolder.createChildContainer(identity.getName());
			} else if(assessedItem instanceof VFSContainer) {
				returnContainer = (VFSContainer)assessedItem;
			}
		}
		return returnContainer;
	}
}
