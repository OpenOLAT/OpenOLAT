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
package org.olat.course.nodes.ta;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.SolutionList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.properties.Property;

/**
 * 
 * Initial date: 30.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConvertToGTACourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(ConvertToGTACourseNode.class);
	
	private final GTAManager gtaManager;
	private final BaseSecurity securityManager;
	
	public ConvertToGTACourseNode() {
		gtaManager = CoreSpringFactory.getImpl(GTAManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
	}
	
	public void convert(TACourseNode sourceNode, GTACourseNode gtaNode, ICourse course, Identity convertedBy) {
		ModuleConfiguration modConfig = sourceNode.getModuleConfiguration();
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		gtaNode.setShortTitle("New_ " + sourceNode.getShortTitle());
		gtaNode.setLongTitle("New_ " + sourceNode.getLongTitle());
		gtaNode.setDisplayOption(sourceNode.getDisplayOption());
		gtaNode.setLearningObjectives(sourceNode.getLearningObjectives());
		
		TaskList taskList = gtaManager.createIfNotExists(courseEnv.getCourseGroupManager().getCourseEntry(), gtaNode);
		DBFactory.getInstance().commit();
		
		convertConfiguration(sourceNode, gtaNode);
		
		if(modConfig.getBooleanSafe(TACourseNode.CONF_TASK_ENABLED)) {	
			convertTasks(taskList, sourceNode, gtaNode, course);
			DBFactory.getInstance().commit();
		}
		
		if(modConfig.getBooleanSafe(TACourseNode.CONF_DROPBOX_ENABLED)) {
			convertDropbox(taskList, sourceNode, gtaNode, courseEnv, convertedBy);
			DBFactory.getInstance().commit();
		}

		if(modConfig.getBooleanSafe(TACourseNode.CONF_RETURNBOX_ENABLED)) {
			convertReturnbox( taskList, sourceNode, gtaNode, courseEnv, convertedBy);
			DBFactory.getInstance().commit();
		}

		if(modConfig.getBooleanSafe(TACourseNode.CONF_SCORING_ENABLED)) {
			//copy the scores
			convertAssessmentDatas(taskList, sourceNode, gtaNode, course);
			DBFactory.getInstance().commit();
		}
		
		//solutions
		if(modConfig.getBooleanSafe(TACourseNode.CONF_SOLUTION_ENABLED)) {
			copySolutions(sourceNode, gtaNode, courseEnv, convertedBy);
		}
	}
	
	private void convertTasks(TaskList taskList, TACourseNode sourceNode, GTACourseNode gtaNode, ICourse course) {
		File taskFolder = new File(FolderConfig.getCanonicalRoot(), TACourseNode.getTaskFolderPathRelToFolderRoot(course, sourceNode));
		VFSContainer taskContainer = VFSManager.olatRootContainer(TACourseNode.getTaskFolderPathRelToFolderRoot(course, sourceNode), null);
		
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		File gtaskDirectory = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		VFSContainer gtaskContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		
		//make the task
		TaskDefinitionList taskDefs = new TaskDefinitionList();
		for(File task:taskFolder.listFiles(SystemFileFilter.FILES_ONLY)) {
			TaskDefinition taskDef = new TaskDefinition();
			taskDef.setDescription("");
			taskDef.setFilename(task.getName());
			taskDef.setTitle(task.getName());
			taskDefs.getTasks().add(taskDef);
			try {
				File target = new File(gtaskDirectory, task.getName());
				Files.copy(task.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
				convertMetada(taskContainer, gtaskContainer, task.getName(), taskDef, null);
			} catch(Exception ex) {
				log.error("", ex);
			}

			List<Property> samples = courseEnv.getCoursePropertyManager().listCourseNodeProperties(sourceNode, null, null, TaskController.PROP_ASSIGNED);
			for(Property sample:samples) {
				File taskFile = new File(gtaskDirectory, sample.getStringValue());
				Identity id = securityManager.loadIdentityByKey(sample.getIdentity().getKey());
				gtaManager.selectTask(id, taskList, gtaNode, taskFile);
			}
		}
		
		gtaNode.getModuleConfiguration().set(GTACourseNode.GTASK_TASKS, taskDefs);
	}
	
	private void convertDropbox(TaskList taskList, TACourseNode sourceNode, GTACourseNode gtaNode,
			CourseEnvironment courseEnv, Identity convertedBy) {
		String dropbox = DropboxController.getDropboxPathRelToFolderRoot(courseEnv, sourceNode);
		VFSContainer dropboxContainer = VFSManager.olatRootContainer(dropbox, null);
		for(VFSItem userDropbox:dropboxContainer.getItems()) {
			if(userDropbox instanceof VFSContainer) {
				VFSContainer userDropContainer = (VFSContainer)userDropbox;
				String username = userDropContainer.getName();
				Identity assessedIdentity = securityManager.findIdentityByName(username);
				if(assessedIdentity != null) {
					VFSContainer sumbitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, assessedIdentity);
					
					boolean dropped = false;
					for(VFSItem dropppedItem:userDropContainer.getItems(new VFSSystemItemFilter())) {
						if(dropppedItem instanceof VFSLeaf) {
							VFSLeaf submittedDocument = sumbitContainer.createChildLeaf(dropppedItem.getName());
							VFSManager.copyContent((VFSLeaf)dropppedItem, submittedDocument, true, convertedBy);
							convertMetada(userDropContainer, sumbitContainer, dropppedItem.getName(), null, null);
							dropped = true;
						}
					}
					
					if(dropped) {
						setTaskStatus(taskList, assessedIdentity, TaskProcess.submit, gtaNode);
					}
				}
			}
		}
	}
	
	private void convertReturnbox(TaskList taskList, TACourseNode sourceNode, GTACourseNode gtaNode,
			CourseEnvironment courseEnv, Identity convertedBy) {
		String returnbox = ReturnboxController.getReturnboxPathRelToFolderRoot(courseEnv, sourceNode);
		VFSContainer returnContainer = VFSManager.olatRootContainer(returnbox, null);
		for(VFSItem item:returnContainer.getItems()) {
			if(item instanceof VFSContainer) {
				VFSContainer userContainer = (VFSContainer)item;
				String username = userContainer.getName();
				Identity assessedIdentity = securityManager.findIdentityByName(username);
				if(assessedIdentity != null) {
					VFSContainer correctionContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
					
					boolean returned = false;
					for(VFSItem returnedItem:userContainer.getItems(new VFSSystemItemFilter())) {
						if(returnedItem instanceof VFSLeaf) {
							VFSLeaf correctionDocument = correctionContainer.createChildLeaf(returnedItem.getName());
							VFSManager.copyContent((VFSLeaf)returnedItem, correctionDocument, true, convertedBy);
							convertMetada(userContainer, correctionContainer, returnedItem.getName(), null, null);
							returned = true;
						}
					}
					
					if(returned) {
						setTaskStatus(taskList, assessedIdentity, TaskProcess.grading, gtaNode);
					}
				}
			}
		}
	}
	
	private void convertAssessmentDatas(TaskList taskList, TACourseNode sourceNode, GTACourseNode gtaNode, ICourse course) {
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		CoursePropertyManager propertyMgr = courseEnv.getCoursePropertyManager();
		
		Map<Long,AssessmentEntry> datas = new HashMap<>();
		List<AssessmentEntry> properties = courseEnv.getAssessmentManager().getAssessmentEntries(sourceNode);

		for(AssessmentEntry property:properties) {
			Identity identity = property.getIdentity();
			datas.put(identity.getKey(), property);
		}
		properties = null;
		DBFactory.getInstance().getCurrentEntityManager().clear();
		
		AssessmentManager assessmentMgr = courseEnv.getAssessmentManager();
		for(AssessmentEntry assessmentData:datas.values()) {
			Identity assessedIdentity = securityManager.loadIdentityByKey(assessmentData.getIdentity().getKey());
			
			if(assessmentData.getPassed() != null || assessmentData.getScore() != null) {
				UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
				Float score = assessmentData.getScore() == null ? null : assessmentData.getScore().floatValue();
				ScoreEvaluation scoreEval = new ScoreEvaluation(score, assessmentData.getPassed());
				assessmentMgr.saveScoreEvaluation(gtaNode, null, assessedIdentity, scoreEval, userCourseEnv, false, Role.auto);
				
				//set graded
				Task task = gtaManager.getTask(assessedIdentity, taskList);
				if(task == null) {
					gtaManager.createTask(null, taskList, TaskProcess.graded, null, assessedIdentity, gtaNode);
				} else {
					gtaManager.updateTask(task, TaskProcess.graded, gtaNode, false, null, Role.auto);
				}
			}
			
			if(assessmentData.getAttempts() != null) {
				assessmentMgr.saveNodeAttempts(gtaNode, null, assessedIdentity, assessmentData.getAttempts(), assessmentData.getLastAttempt(), Role.auto);
			}
			
			if(StringHelper.containsNonWhitespace(assessmentData.getCoachComment())) {
				assessmentMgr.saveNodeCoachComment(gtaNode, assessedIdentity, assessmentData.getCoachComment());
			}
			
			if(StringHelper.containsNonWhitespace(assessmentData.getComment())) {
				assessmentMgr.saveNodeComment(gtaNode, null, assessedIdentity, assessmentData.getComment());
			}
		}
		
		DBFactory.getInstance().getCurrentEntityManager().clear();
		
		//copy log entries
		List<Property> logEntries = propertyMgr
				.listCourseNodeProperties(sourceNode, null, null, UserNodeAuditManager.LOG_IDENTIFYER);
		for(Property logEntry:logEntries) {
			String logText = logEntry.getTextValue();
			Identity identity = securityManager.loadIdentityByKey(logEntry.getIdentity().getKey());
			Property targetProp = propertyMgr.findCourseNodeProperty(gtaNode, identity, null, UserNodeAuditManager.LOG_IDENTIFYER);
			if(targetProp == null) {
				targetProp = propertyMgr
					.createCourseNodePropertyInstance(gtaNode, identity, null, UserNodeAuditManager.LOG_IDENTIFYER, null, null, null, logText);
			} else {
				targetProp.setTextValue(logText);
			}
			propertyMgr.saveProperty(targetProp);
		}	
	}
	
	private void copySolutions(TACourseNode sourceNode, GTACourseNode gtaNode, CourseEnvironment courseEnv,
			Identity convertedBy) {
		ModuleConfiguration gtaConfig = gtaNode.getModuleConfiguration();
		String solutionPath = SolutionController.getSolutionPathRelToFolderRoot(courseEnv, sourceNode);
		VFSContainer solutionContainer = VFSManager.olatRootContainer(solutionPath, null);
		VFSContainer solutionDirectory = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
		SolutionList solutionList = new SolutionList();
		
		for(VFSItem solution:solutionContainer.getItems(new VFSSystemItemFilter())) {
			if(solution instanceof VFSLeaf) {
				VFSLeaf solutionDocument = solutionDirectory.createChildLeaf(solution.getName());
				VFSManager.copyContent((VFSLeaf)solution, solutionDocument, true, convertedBy);
				
				Solution solDef = new Solution();
				convertMetada(solutionContainer, solutionDirectory, solution.getName(), null, solDef);
				solDef.setFilename(solution.getName());
				solutionList.getSolutions().add(solDef);
			}
		}
		
		gtaConfig.set(GTACourseNode.GTASK_SOLUTIONS, solutionList);	
	}
	
	private void convertConfiguration(TACourseNode sourceNode, GTACourseNode gtaNode) {
		ModuleConfiguration gtaConfig = gtaNode.getModuleConfiguration();
		ModuleConfiguration modConfig = sourceNode.getModuleConfiguration();
		
		gtaConfig.setStringValue(GTACourseNode.GTASK_TYPE, GTAType.individual.name());
		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_ASSIGNMENT, modConfig.getBooleanSafe(TACourseNode.CONF_TASK_ENABLED));
		
		if(TaskController.TYPE_AUTO.equals(modConfig.get(TACourseNode.CONF_TASK_TYPE))) {
			gtaConfig.setStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO);
		} else if(TaskController.TYPE_MANUAL.equals(modConfig.get(TACourseNode.CONF_TASK_TYPE))) {
			gtaConfig.setStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL);
		} else {
			gtaConfig.setStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL);
		}

		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_PREVIEW, modConfig.getBooleanSafe(TACourseNode.CONF_TASK_PREVIEW));
		
		if(modConfig.getBooleanSafe(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT)) {
			gtaConfig.setStringValue(GTACourseNode.GTASK_SAMPLING, GTACourseNode.GTASK_SAMPLING_REUSE);
		} else {
			gtaConfig.setStringValue(GTACourseNode.GTASK_SAMPLING, GTACourseNode.GTASK_SAMPLING_UNIQUE);
		}
		
		if(modConfig.get(TACourseNode.CONF_TASK_TEXT) != null) {
			gtaConfig.setStringValue(GTACourseNode.GTASK_USERS_TEXT, modConfig.get(TACourseNode.CONF_TASK_TEXT).toString());
		}
		
		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_SUBMIT, modConfig.getBooleanSafe(TACourseNode.CONF_DROPBOX_ENABLED));
		//drop box options
		String confirmation = modConfig.getStringValue(TACourseNode.CONF_DROPBOX_CONFIRMATION);
		if(StringHelper.containsNonWhitespace(confirmation)) {
			gtaConfig.setStringValue(GTACourseNode.GTASK_SUBMISSION_TEXT, confirmation);
			gtaConfig.setBooleanEntry(GTACourseNode.GTASK_SUBMISSION_MAIL_CONFIRMATION, modConfig.getBooleanSafe(TACourseNode.CONF_DROPBOX_ENABLEMAIL));
		}
		
		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_REVIEW_AND_CORRECTION, modConfig.getBooleanSafe(TACourseNode.CONF_RETURNBOX_ENABLED));
		
		//passed
		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_GRADING, modConfig.getBooleanSafe(TACourseNode.CONF_SCORING_ENABLED));
		
		//grading options
		gtaConfig.set(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, modConfig.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
		gtaConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MIN, modConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MIN));
		gtaConfig.set(MSCourseNode.CONFIG_KEY_SCORE_MAX, modConfig.get(MSCourseNode.CONFIG_KEY_SCORE_MAX));
		gtaConfig.set(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, modConfig.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
		if(modConfig.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE) != null) {
			gtaConfig.set(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, modConfig.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE));
		}
		gtaConfig.set(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, modConfig.getBooleanEntry(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD));
		gtaConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER));
		gtaConfig.set(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH, modConfig.get(MSCourseNode.CONFIG_KEY_INFOTEXT_COACH));
		
		gtaConfig.setBooleanEntry(GTACourseNode.GTASK_SAMPLE_SOLUTION, modConfig.getBooleanSafe(TACourseNode.CONF_SOLUTION_ENABLED));
	}
	
	private void setTaskStatus(TaskList taskList, Identity assessedIdentity, TaskProcess current, GTACourseNode gtaNode) {
		TaskProcess process = gtaManager.nextStep(current, gtaNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task == null) {
			gtaManager.createTask(null, taskList, process, null, assessedIdentity, gtaNode);
		} else {
			gtaManager.updateTask(task, process, gtaNode, false, null, Role.auto);
		}
	}
	
	private void convertMetada(VFSContainer source, VFSContainer target, String name, TaskDefinition taskDef, Solution solDef) {
		VFSItem sourceItem = source.resolve(name);
		VFSItem targetItem = target.resolve(name);
		if(sourceItem.canMeta() == VFSConstants.YES && targetItem.canMeta() == VFSConstants.YES) {
			VFSMetadata metaSource = sourceItem.getMetaInfo();
			VFSMetadata metaTarget = targetItem.getMetaInfo();
			
			if(metaSource != null) {
				if(taskDef != null) {
					if(StringHelper.containsNonWhitespace(metaSource.getTitle())) {
						taskDef.setTitle(metaSource.getTitle());
					}
					taskDef.setDescription(metaSource.getComment());
				}
				
				if(solDef != null && StringHelper.containsNonWhitespace(metaSource.getTitle())) {
					solDef.setTitle(metaSource.getTitle());
				}
				
				if(metaTarget != null) {
					metaTarget.copyValues(metaSource, true);
					CoreSpringFactory.getImpl(VFSRepositoryService.class).updateMetadata(metaTarget);
				}	
			}	
		}
	}
}
