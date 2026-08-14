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
package org.olat.course.nodes.gta.manager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.ShieldOutputStream;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingArchiveController;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.GTAUIFactory;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.MemberViewQueries;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAResultsExport {
	
	private static final Logger log = Tracing.createLoggerFor(GTAResultsExport.class);

	private final Locale locale;
	private final ICourse course;
	private final GTACourseNode gtaNode;
	private final RepositoryEntry courseEntry;
	
	private final boolean obligationOk;
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	protected MemberViewQueries memberQueries;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private UserCourseInformationsManager userCourseInfosMgr;
	
	public GTAResultsExport(ICourse course, GTACourseNode gtaNode, Locale locale) {
		CoreSpringFactory.autowireObject(this);
		
		this.course = course;
		this.gtaNode = gtaNode;
		this.locale = locale;
		courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		obligationOk = NodeAccessType.of(course).getType().equals(LearningPathNodeAccessProvider.TYPE);
		translator = userManager.getPropertyHandlerTranslator(Util
				.createPackageTranslator(ScoreAccountingArchiveController.class, locale,
						Util.createPackageTranslator(GTAUIFactory.class, locale)));
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(ScoreAccountingHelper.class.getCanonicalName(), true);
	}

	public void export(String filename, List<Identity> assessedIdentities, List<BusinessGroup> businessGroups, ZipOutputStream exportStream) {
		try(OutputStream out = new ShieldOutputStream(exportStream)) {
			exportStream.putNextEntry(new ZipEntry(filename));
			exportWorkbook(assessedIdentities, businessGroups, out);
			exportStream.closeEntry();
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public void exportWorkbook(List<Identity> assessedIdentities, List<BusinessGroup> businessGroups, OutputStream exportStream) {
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(exportStream, 1)) {
			//headers
			OpenXMLWorksheet exportSheet = workbook.nextWorksheet();
			exportSheet.setHeaderRows(1);
			writeHeaders(exportSheet);
			writeData(assessedIdentities, businessGroups, exportSheet, workbook);
		} catch(Exception e) {
			log.error("", e);
		}
	}
	
	private void writeHeaders(OpenXMLWorksheet exportSheet) {
		int headerColCnt = 0;
		Row headerRow = exportSheet.newRow();
		
		headerRow.addCell(headerColCnt++, translator.translate("column.header.seqnum"));
		headerRow.addCell(headerColCnt++, translator.translate("column.header.businesspath"));
		//Initial launch date
		headerRow.addCell(headerColCnt++, translator.translate("column.header.initialLaunchDate"));

		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			headerRow.addCell(headerColCnt++, translator.translate(propertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.group.name"));
		}

		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.group.taskTitle"));
		}
		
		headerRow.addCell(headerColCnt++, translator.translate("table.header.group.step"));
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.assignment.completed"));
		}
		
		headerRow.addCell(headerColCnt++, translator.translate("table.header.submitted"));
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.review.correction.acceptation.date.alt"));
		}
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.revision.acceptation.date.alt"));
		}
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.assessment.date"));
		}

		headerRow.addCell(headerColCnt++, translator.translate("table.header.remarks"));
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, gtaNode);
		if(Mode.none != assessmentConfig.getScoreMode()) {
			headerRow.addCell(headerColCnt++, translator.translate("column.header.score"));
		}
		if(Mode.none != assessmentConfig.getPassedMode()) {
			headerRow.addCell(headerColCnt++, translator.translate("column.header.passed"));
		}
		if(assessmentConfig.hasAttempts()) {
			headerRow.addCell(headerColCnt++, translator.translate("column.header.attempts"));
		}
		if(obligationOk) {
			headerRow.addCell(headerColCnt++, translator.translate("column.header.obligation"));
		}
		headerRow.addCell(headerColCnt++, translator.translate("column.header.scoreLastModified"));//last modified
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT)) {
			headerRow.addCell(headerColCnt++, translator.translate("table.header.assignment.coach"));
		}
		
		if (assessmentConfig.hasComment()) {
			headerRow.addCell(headerColCnt++, translator.translate("column.header.comment"));
		}
	
		headerRow.addCell(headerColCnt, translator.translate("column.header.coachcomment"));//coach comment
	}
	
	private void writeData(List<Identity> assessedIdentities, List<BusinessGroup> businessGroups, OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))
				&& businessGroups == null) {
			businessGroups = gtaManager.getBusinessGroups(gtaNode);
		}

		List<BusinessGroupMembership> groupsMemberships = getGroupMemberships(assessedIdentities, businessGroups);
		Map<Long,String> groupsMap = getGroups(groupsMemberships, businessGroups);
		
		Map<Long,Date> firstTimes = userCourseInfosMgr.getInitialLaunchDates(courseEntry, assessedIdentities);
		Map<Long,Task> tasksMap = getTasks(groupsMemberships);
		
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(course.getCourseEnvironment(), gtaNode);
		Map<String,TaskDefinition> fileNameToDefinitions = taskDefinitions.stream()
				.filter(def -> Objects.nonNull(def.getFilename()))
				.collect(Collectors.toMap(TaskDefinition::getFilename, Function.identity(), (u, v) -> u));
		
		List<AssessmentEntry> assessments = assessmentService.loadAssessmentEntriesBySubIdent(courseEntry, gtaNode.getIdent());
		Map<Long,AssessmentEntry> assessmentsEntriesMap = assessments.stream()
				.filter(entry -> entry.getIdentity() != null)
				.collect(Collectors.toMap(entry -> entry.getIdentity().getKey(), Function.identity(), (u, v) -> u));
		
		int rowNumber = 0;
		for(Identity assessedIdentity:assessedIdentities) {
			Date firstTime = firstTimes.get(assessedIdentity.getKey());
			Task task = tasksMap.get(assessedIdentity.getKey());
			TaskDefinition taskDefinition = (task != null && StringHelper.containsNonWhitespace(task.getTaskName()))
					? fileNameToDefinitions.get(task.getTaskName())
					: null;
			AssessmentEntry assessmentEntry = assessmentsEntriesMap.get(assessedIdentity.getKey());
			String groups = groupsMap.get(assessedIdentity.getKey());
			
			writeData(++rowNumber, assessedIdentity, task, taskDefinition, assessmentEntry, firstTime, groups, exportSheet, workbook);
			dbInstance.commitAndCloseSession();
		}
	}
	
	private List<BusinessGroupMembership> getGroupMemberships(List<Identity> assessedIdentities, List<BusinessGroup> businessGroups) {
		if(GTAType.individual.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))
				|| businessGroups == null || businessGroups.isEmpty()) {
			return List.of();
		}

		List<Long> businessGroupsKeys = businessGroups.stream()
				.map(BusinessGroup::getKey)
				.collect(Collectors.toList());
		Identity[] assessedIdentitiesArr = assessedIdentities.toArray(new Identity[assessedIdentities.size()]);
		return businessGroupService.getBusinessGroupMembership(businessGroupsKeys, assessedIdentitiesArr);
	}
	
	private Map<Long,String> getGroups(List<BusinessGroupMembership> groupsMemberships, List<BusinessGroup> businessGroups) {
		if(GTAType.individual.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))
				|| businessGroups == null || businessGroups.isEmpty()) {
			return Map.of();
		}
		
		Map<Long,String> businessGroupsNames = businessGroups.stream()
				.collect(Collectors.toMap(BusinessGroup::getKey, BusinessGroup::getName, (u, v) -> u));
	
		Map<Long,String> membershipsMap = new HashMap<>();
		for(BusinessGroupMembership membership:groupsMemberships) {
			String name = businessGroupsNames.get(membership.getGroupKey());
			if(StringHelper.containsNonWhitespace(name) && membership.isParticipant()) {
			
				String names = membershipsMap.get(membership.getIdentityKey());
				if(names == null) {
					membershipsMap.put(membership.getIdentityKey(), name);
				} else {
					names += ", " + name;
					membershipsMap.put(membership.getIdentityKey(), names);
				}
			}
		}
		return membershipsMap;
	}
	
	private Map<Long,Task> getTasks(List<BusinessGroupMembership> groupsMemberships) {
		TaskList taskList = gtaManager.getTaskList(courseEntry, gtaNode);
		if(taskList == null) {
			return new HashMap<>();
		}

		List<Task> tasks = gtaManager.getTasks(taskList, gtaNode);
		Map<Long,Task> tasksMap;

		if(GTAType.individual.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			tasksMap = tasks.stream()
				.filter(task -> task.getIdentity() != null)
				.collect(Collectors.toMap(task -> task.getIdentity().getKey(), task -> task, (u, v) -> u));
		} else {
			Map<Long,Task> groupTasksMap = tasks.stream()
					.filter(task -> task.getBusinessGroup() != null)
					.collect(Collectors.toMap(task -> task.getBusinessGroup().getKey(), task -> task, (u, v) -> u));
			
			tasksMap = new HashMap<>();
			for(BusinessGroupMembership membership:groupsMemberships) {
				if(!membership.isParticipant()) continue;
				
				Task task = groupTasksMap.get(membership.getGroupKey());
				if(task != null) {
					tasksMap.put(membership.getIdentityKey(), task);
				}
			}
		}
		return tasksMap;
	}
	
	private void writeData(int rowNumber, Identity assessedIdentity, Task task, TaskDefinition taskDefinition,
			AssessmentEntry assessmentEntry, Date firstTime, String groups,
			OpenXMLWorksheet exportSheet, OpenXMLWorkbook workbook) {
		Row dataRow = exportSheet.newRow();
		int dataColCnt = 0;

		dataRow.addCell(dataColCnt++, rowNumber, null);
		ContextEntry ce = BusinessControlFactory.getInstance().createContextEntry(assessedIdentity);
		String uname = BusinessControlFactory.getInstance().getAsURIString(Collections.singletonList(ce), false);
		dataRow.addCell(dataColCnt++, uname, null);
		if(firstTime != null) {
			dataRow.addCell(dataColCnt++, firstTime, workbook.getStyles().getDateStyle());
		} else {
			dataColCnt++;
		}

		// add dynamic user properties
		for (UserPropertyHandler propertyHandler : userPropertyHandlers) {
			String value = propertyHandler.getUserProperty(assessedIdentity.getUser(), locale);
			if(StringHelper.containsNonWhitespace(value)) {
				dataRow.addCell(dataColCnt++, value);
			} else {
				dataColCnt++;
			}
		}
		
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			dataRow.addCell(dataColCnt++, groups);
		}
		
		// create a identenv with no roles, no attributes, no locale
		IdentityEnvironment ienv = new IdentityEnvironment();
		ienv.setIdentity(assessedIdentity);
		UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		ScoreAccounting scoreAccount = uce.getScoreAccounting();
		scoreAccount.evaluateAll();

		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, gtaNode);
		AssessmentEvaluation se = scoreAccount.evalCourseNode(gtaNode);
		
		// Task title/name
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			String title = taskDefinition == null ? null : taskDefinition.getTitle();
			if(!StringHelper.containsNonWhitespace(title) && task != null) {
				title = task.getTaskName();
			}
			dataRow.addCell(dataColCnt++, title);
		}
		
		//Step
		String status = task == null
				? null
				: translator.translate("process." + task.getTaskStatus());
		dataRow.addCell(dataColCnt++, status);
		
		// Assignment date
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			Date assignmentDate = task == null ? null : task.getAssignmentDate();
			dataRow.addCell(dataColCnt++, assignmentDate, workbook.getStyles().getDateStyle());
		}

		Date submissionDate = null;
		if(task != null) {
			submissionDate = task.getCollectionDate() == null ? task.getSubmissionDate() : task.getCollectionDate();
		}
		dataRow.addCell(dataColCnt++, submissionDate, workbook.getStyles().getDateStyle());
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			Date acceptationDate = task == null || task.getRevisionLoop() > 0 ? null : task.getAcceptationDate();
			dataRow.addCell(dataColCnt++, acceptationDate, workbook.getStyles().getDateStyle());
		}
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			Date acceptationDate = task == null || task.getRevisionLoop() == 0 ? null : task.getAcceptationDate();
			dataRow.addCell(dataColCnt++, acceptationDate, workbook.getStyles().getDateStyle());
		}
		// Grading date
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_GRADING)) {
			Date gradingDate = assessmentEntry == null ? null : assessmentEntry.getAssessmentDone();
			dataRow.addCell(dataColCnt++, gradingDate, workbook.getStyles().getDateStyle());
		}

		if(submissionDate != null) {
			StringBuilder remark = new StringBuilder();
			if(gtaManager.isSubmissionExtended(task, assessedIdentity, null, gtaNode, courseEntry, true)) {
				remark.append(translator.translate("label.extended"));
			} else if(gtaManager.isSubmissionLate(task, assessedIdentity, null, gtaNode, courseEntry, true)) {
				remark.append(translator.translate("label.late"));
			}
			if(task != null && task.getCollectionDate() != null) {
				if(remark.length() > 0) remark.append(", ");
				remark.append(translator.translate("label.collected"));
			}
			dataRow.addCell(dataColCnt, remark.toString());
		}
		dataColCnt++;

		if (Mode.none != assessmentConfig.getScoreMode()) {
			Float score = se.getScore();
			if (score != null) {
				dataRow.addCell(dataColCnt++, AssessmentHelper.getRoundedScore(score), null);
			} else { // score == null
				dataColCnt++;
			}
		}

		if (Mode.none != assessmentConfig.getPassedMode()) {
			Boolean passed = se.getPassed();
			if (passed != null) {
				String yesno;
				if (passed.booleanValue()) {
					yesno = translator.translate("column.field.yes");
				} else {
					yesno = translator.translate("column.field.no");
				}
				dataRow.addCell(dataColCnt++, yesno);
			} else { // passed == null
				dataColCnt++;
			}
		}

		if (assessmentConfig.hasAttempts()) {
			int a = se.getAttempts() == null ? 0 : se.getAttempts().intValue();
			dataRow.addCell(dataColCnt++, a, null);
		}
		
		if (obligationOk) {
			if (se.getObligation() != null && se.getObligation().getCurrent() != null) {
				switch (se.getObligation().getCurrent()) {
				case mandatory:
					dataRow.addCell(dataColCnt++, translator.translate("column.field.mandatory")); break;
				case optional:
					dataRow.addCell(dataColCnt++, translator.translate("column.field.optional")); break;
				case excluded:
					dataRow.addCell(dataColCnt++, translator.translate("column.field.excluded")); break;
				default:
					break;
				}
			} else {
				dataColCnt++;
			}
		}

		if(se.getLastModified() != null) {
			dataRow.addCell(dataColCnt++, se.getLastModified(), workbook.getStyles().getDateStyle());
		} else {
			dataColCnt++;
		}
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_COACH_ASSIGNMENT)) {
			String coach = assessmentEntry != null && assessmentEntry.getCoach() != null
					? userManager.getUserDisplayName(assessmentEntry.getCoach())
					: null;
			dataRow.addCell(dataColCnt++, coach);
			dataColCnt++;
		}

		if (assessmentConfig.hasComment()) {
			// Comments for user
			if (se.getComment() != null) {
				dataRow.addCell(dataColCnt++, se.getComment());
			} else {
				dataColCnt++;
			}
		}

		// Always export comments for tutors
		if (se.getCoachComment() != null) {
			dataRow.addCell(dataColCnt, se.getCoachComment());
		}
	}
}
