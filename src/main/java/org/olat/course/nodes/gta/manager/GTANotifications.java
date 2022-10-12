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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.IdentityMark;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevisionDate;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class GTANotifications {
	
	private final Date compareDate;
	private final Subscriber subscriber;
	private final List<SubscriptionListItem> items = new ArrayList<>();
	
	private TaskList taskList;
	private String header;
	private String displayName;
	private boolean markedOnly;
	private GTACourseNode gtaNode;
	private CourseEnvironment courseEnv;
	private Calendar cal = Calendar.getInstance();
	
	private final Translator translator;
	private final Formatter formatter;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentEntryDAO courseNodeAssessmentDao;
	
	public GTANotifications(Subscriber subscriber, boolean markedOnly, Locale locale, Date compareDate) {
		CoreSpringFactory.autowireObject(this);
		this.markedOnly = markedOnly;
		this.subscriber = subscriber;
		this.compareDate = compareDate;
		formatter = Formatter.getInstance(locale);
		translator = Util.createPackageTranslator(GTARunController.class, locale);
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public String getNotifificationHeader() {
		if(StringHelper.containsNonWhitespace(header)) {
			return header;
		}
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			return translator.translate("notifications.group.header", new String[]{ displayName });
		}
		
		if(markedOnly) {
			return translator.translate("notifications.individual.favorite.header", new String[]{ displayName });
		}
		return translator.translate("notifications.individual.header", new String[]{ displayName });
	}

	public List<SubscriptionListItem> getItems() {
		Publisher p = subscriber.getPublisher();
		ICourse course = CourseFactory.loadCourse(p.getResId());
		String subIdentifier = p.getSubidentifier();
		if(subIdentifier.startsWith("Marked::")) {
			subIdentifier = subIdentifier.substring("Marked::".length(), subIdentifier.length());
		}
		if(!course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed()) {
			return Collections.emptyList();
		}
		
		CourseNode node = course.getRunStructure().getNode(subIdentifier);
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(node instanceof GTACourseNode) {
			gtaNode = (GTACourseNode)node;
			displayName = entry.getDisplayname();
			courseEnv = course.getCourseEnvironment();
			taskList = gtaManager.getTaskList(entry, gtaNode);
			
			if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
				createBusinessGroupsSubscriptionInfo(subscriber.getIdentity());
			} else {
				Identity subscriberIdentity = subscriber.getIdentity();
				Set<Long> marks = null;
				if(markedOnly) {
					List<IdentityMark> identityMarks = gtaManager.getMarks(entry, gtaNode, subscriberIdentity);
					marks = new HashSet<>(identityMarks.size());
					for(IdentityMark identityMark:identityMarks) {
						if(identityMark.getParticipant() != null) {
							marks.add(identityMark.getParticipant().getKey());
						}
					}
				}
				createIndividualSubscriptionInfo(subscriberIdentity, marks);
			}
		}
		
		return items;
	}
	
	private void createIndividualSubscriptionInfo(Identity subscriberIdentity, Set<Long> marks) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
		Roles roles = securityManager.getRoles(subscriberIdentity);
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(subscriberIdentity, roles, entry);
		
		boolean owner = reSecurity.isOwner() || reSecurity.isEntryAdmin();
		boolean coach = reSecurity.isCourseCoach() || reSecurity.isCurriculumCoach() || reSecurity.isGroupCoach();
		if(owner || coach) {
			Set<Identity> duplicateKiller = new HashSet<>();
			List<Identity> assessableIdentities = new ArrayList<>();

			List<Identity> participants;
			List<BusinessGroup> coachedGroups = null;
			if(owner) {
				participants = businessGroupService.getMembersOf(entry, false, true);
			} else {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(subscriberIdentity, true, false);
				coachedGroups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				participants = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
			}
			
			for(Identity participant:participants) {
				if(!duplicateKiller.contains(participant)
						&& (marks == null || marks.contains(participant.getKey()))) {
					assessableIdentities.add(participant);
					duplicateKiller.add(participant);
				}
			}
			
			boolean repoTutor = owner || (coachedGroups.isEmpty() && repositoryService.hasRole(subscriberIdentity, entry, GroupRoles.coach.name()));
			if(repoTutor) {
				List<Identity> courseParticipants = repositoryService.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
				for(Identity participant:courseParticipants) {
					if(!duplicateKiller.contains(participant)
							&& (marks == null || marks.contains(participant.getKey()))) {
						assessableIdentities.add(participant);
						duplicateKiller.add(participant);
					}
				}
			}
			
			for(Identity assessedIdentity: assessableIdentities) {
				createIndividualSubscriptionInfo(assessedIdentity, true);
			}

			createCoachSolutionItems();
		} else {
			Task task = gtaManager.getTask(subscriberIdentity, taskList);
			if(task != null) {
				header = translator.translate("notifications.individual.header.task", new String[]{ getTaskName(task), displayName });
			}
		}
		
		boolean participant = reSecurity.isCourseParticipant() || reSecurity.isCurriculumParticipant() || reSecurity.isGroupParticipant();
		if(participant) {
			createIndividualSubscriptionInfo(subscriberIdentity, false);
			Task task = gtaManager.getTask(subscriberIdentity, taskList);
			if(isSolutionVisible(subscriberIdentity, null, task)) {
				createParticipantSolutionItems(task, subscriberIdentity, null);
			}
			if(task != null && task.getTaskStatus() == TaskProcess.graded) {
				createAssessmentItem(task, subscriberIdentity, false);
			}
		}
	}

	private void createIndividualSubscriptionInfo(Identity assessedIdentity, boolean coach) {
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		
		if(coach && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			task = checkSubmitStep(assessedIdentity, null, task);
			//show after the step submit, if submission date after compare date
			if(task != null && notInStep(task, TaskProcess.assignment, TaskProcess.submit)
					&& task.getSubmissionDate() != null && task.getSubmissionDate().after(compareDate)) {
				
				Date submissionDate = task.getSubmissionDate();
				String fullName = userManager.getUserDisplayName(assessedIdentity);
				File submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
				File[] submissions = submitDirectory.listFiles(SystemFileFilter.FILES_ONLY);
				if(submissions.length == 0) {
					String[] params = new String[] {
							getTaskName(task),		// 0
							displayName,			// 1
							fullName				// 2
					};
					appendSubscriptionItem("notifications.submission.individual", params, assessedIdentity, submissionDate, coach);
				} else {
					for(File submission:submissions) {
						String[] params = new String[] {
								getTaskName(task),		// 0
								displayName,			// 1
								submission.getName(),	// 2
								fullName				// 3
						};
						appendSubscriptionItemForFile("notifications.submission.individual.doc", params, assessedIdentity,
								"[submit:0]", submission, submissionDate, coach);
					}
				}
			}
		}
		
		createReviewAndRevisionsItems(task, assessedIdentity, null, coach);
		createAcceptedItem(task, assessedIdentity, null, coach);
	}
	
	private void createBusinessGroupsSubscriptionInfo(Identity subscriberIdentity) {
		RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();

		Membership membership = gtaManager.getMembership(subscriberIdentity, entry, gtaNode);
		Roles roles = securityManager.getRoles(subscriberIdentity);
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(subscriberIdentity, roles, entry);
		
		boolean owner = reSecurity.isOwner() || reSecurity.isEntryAdmin();
		if(owner) {
			List<BusinessGroup> groups = gtaManager.getBusinessGroups(gtaNode);
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, true);
			}
			createCoachSolutionItems();
		} else if(membership.isCoach()) {
			List<BusinessGroup> groups = gtaManager.getCoachedBusinessGroups(subscriberIdentity, gtaNode);
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, true);
			}
			createCoachSolutionItems();
		}
		
		if(membership.isParticipant()) {
			List<BusinessGroup> groups = gtaManager.getParticipatingBusinessGroups(subscriberIdentity, gtaNode);
			if(groups.size() == 1 && !owner && !membership.isCoach()) {
				Task task = gtaManager.getTask(groups.get(0), taskList);
				if(task != null) {
					header = translator.translate("notifications.group.header.task", new String[]{ getTaskName(task), displayName });
				}
			}
			
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, false);
				Task task = gtaManager.getTask(group, taskList);
				if(createParticipantSolutionItems(task, null, group)) {
					break;
				}
			}
		}
	}
	
	private void createBusinessGroupsSubscriptionItems(BusinessGroup group, boolean coach) {
		Task task = gtaManager.getTask(group, taskList);
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			task = checkSubmitStep(null, group, task);
			if(task != null && notInStep(task, TaskProcess.assignment, TaskProcess.submit)
					&& (task.getSubmissionDate() == null || task.getSubmissionDate().after(compareDate))) {
				File submitDirectory = gtaManager.getSubmitDirectory(courseEnv, gtaNode, group);
				VFSContainer submitContainer = gtaManager.getSubmitContainer(courseEnv, gtaNode, group);
				
				Date submissionDate = task.getSubmissionDate();
				File[] submisssions = submitDirectory.listFiles(SystemFileFilter.FILES_ONLY);
				if(submisssions.length == 0) {
					String[] params = new String[] { 
							getTaskName(task),
							displayName,
							group.getName()
					};
					appendSubscriptionItem("notifications.submission.group", params, group, submissionDate, coach);
				} else {
				
					for(File submission:submisssions) {
						String author = getAuthor(submission, submitContainer);
						String[] params = new String[] {
								getTaskName(task),		// 0
								displayName,			// 1
								submission.getName(),	// 2
								author,					// 3
								group.getName()			// 4
						};
						appendSubscriptionItemForFile("notifications.submission.group.doc", params, group,
								"[submit:0]", submission, submissionDate, coach);
					}
				}
			}
		}

		createReviewAndRevisionsItems(task, null, group, coach);
		createAcceptedItem(task, null, group, coach);
	}
	
	private void createReviewAndRevisionsItems(Task task, Identity assessedIdentity, BusinessGroup group, boolean coach) {
		if(task == null) return;
		
		String name;
		if(group != null) {
			name = group.getName();
		} else {
			name = userManager.getUserDisplayName(assessedIdentity);
		}
		
		boolean sendNotificationDueDate = true;
		List<TaskRevisionDate> taskRevisions = gtaManager.getTaskRevisionsDate(task);
		if(!coach && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			//check task revision 1
			if(task != null && notInStep(task, TaskProcess.assignment, TaskProcess.submit, TaskProcess.review)
					&& checkRevisionLoop(TaskProcess.revision, 1, taskRevisions)) {
				File correctionDirectory;
				VFSContainer correctionContainer;
				if(group != null) {
					correctionDirectory = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, group);
					correctionContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, group);
				} else {
					correctionDirectory = gtaManager.getCorrectionDirectory(courseEnv, gtaNode, assessedIdentity);
					correctionContainer = gtaManager.getCorrectionContainer(courseEnv, gtaNode, assessedIdentity);
				}

				Date correctionDate = getRevisionLoopDate(TaskProcess.revision, 1, taskRevisions);
				if(sendNotificationDueDate) {
					if(task.getRevisionsDueDate() != null) {
						String[] params = new String[] {
								getTaskName(task),
								displayName,
								formatter.formatDateAndTime(task.getRevisionsDueDate())	
						};
						if(group != null) {
							appendSubscriptionItem("notifications.correction.duedate", params, group, correctionDate, coach);
						} else {
							appendSubscriptionItem("notifications.correction.duedate", params, assessedIdentity, correctionDate, coach);
						}
					} else {
						String[] params = new String[] {
								getTaskName(task),
								displayName
						};
						if(group != null) {
							appendSubscriptionItem("notifications.correction", params, group, correctionDate, coach);
						} else {
							appendSubscriptionItem("notifications.correction", params, assessedIdentity, correctionDate, coach);
						}
					}
					sendNotificationDueDate = false;
				}

				File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
				for(File correction:corrections) {
					String author = getAuthor(correction, correctionContainer);
					String[] params = new String[] {
							getTaskName(task),
							displayName,
							correction.getName(),
							author
					};
					if(group != null) {
						appendSubscriptionItemForFile("notifications.correction.doc", params, group,
								"[correction:0]", correction, correctionDate, coach);
					} else {
						appendSubscriptionItemForFile("notifications.correction.doc", params, assessedIdentity,
								"[correction:0]", correction, correctionDate, coach);
					}
				}
			}
		}
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			task = checkRevisionStep(task);
			if(task != null && notInStep(task, TaskProcess.assignment, TaskProcess.submit, TaskProcess.review)) {
				int currentIteration = task.getRevisionLoop();
				for(int i=1; i<=currentIteration; i++) {
					
					if(coach) {
						// revision of the students
						if(checkRevisionLoop(TaskProcess.correction, i, taskRevisions)) {
							File revisionDirectory; 
							VFSContainer revisionContainer;
							if(group != null) {
								revisionDirectory = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, i, group);
								revisionContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, i, group);
							} else {
								revisionDirectory = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, i, assessedIdentity);
								revisionContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, i, assessedIdentity);
							}

							Date revisionDate = getRevisionLoopDate(TaskProcess.correction, i, taskRevisions);
							File[] revisions = revisionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
							if(revisions.length == 0) {
								String[] params = new String[] {
										getTaskName(task),
										displayName,
										name
								};
								if(group != null) {
									appendSubscriptionItem("notifications.revision.group", params, group, revisionDate, coach);
								} else {
									appendSubscriptionItem("notifications.revision.individual", params, assessedIdentity, revisionDate, coach);
								}
							} else {
								for(File revision:revisions) {
									String author = getAuthor(revision, revisionContainer);
									String[] params = new String[] {
											getTaskName(task),
											displayName,
											revision.getName(),
											name,
											author
									};
									if(group != null) {
										appendSubscriptionItemForFile("notifications.revision.group.doc", params, group,
												"[revision:" + i + "]", revision, revisionDate, coach);
									} else {
										appendSubscriptionItemForFile("notifications.revision.individual.doc", params, assessedIdentity,
												"[revision:" + i + "]", revision, revisionDate, coach);
									}
								}
							}
						}
					} else if(checkRevisionLoop(TaskProcess.revision, i, taskRevisions)) {
						// corrections of the coach
						File correctionDirectory;
						VFSContainer correctionContainer;
						if(group != null) {
							correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, i, group);
							correctionContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, i, group);
						} else {
							correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, i, assessedIdentity);
							correctionContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, i, assessedIdentity);
						}

						Date correctionDate = getRevisionLoopDate(TaskProcess.revision, i, taskRevisions);
						if(sendNotificationDueDate) {
							if(task.getRevisionsDueDate() != null) {
								String[] params = new String[] {
										getTaskName(task),
										displayName,
										formatter.formatDateAndTime(task.getRevisionsDueDate())
								};
								if(group != null) {
									appendSubscriptionItem("notifications.correction.duedate", params, group, correctionDate, coach);
								} else {
									appendSubscriptionItem("notifications.correction.duedate", params, assessedIdentity, correctionDate, coach);
								}
							} else {
								String[] params = new String[] {
										getTaskName(task),
										displayName
								};
								if(group != null) {
									appendSubscriptionItem("notifications.correction", params, group, correctionDate, coach);
								} else {
									appendSubscriptionItem("notifications.correction", params, assessedIdentity, correctionDate, coach);
								}
							}
							sendNotificationDueDate = false;
						}
						
						File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
						for(File correction:corrections) {
							String author = getAuthor(correction, correctionContainer);
							String[] params = new String[] {
									getTaskName(task),
									displayName,
									correction.getName(),
									author
							};
							
							if(group != null) {
								appendSubscriptionItemForFile("notifications.correction.doc", params, group,
										"[correction:" + i + "]", correction, correctionDate, coach);
							} else {
								appendSubscriptionItemForFile("notifications.correction.doc", params, assessedIdentity,
										"[correction:" + i + "]", correction, correctionDate, coach);
							}
							
						}
					}
				}
			}
		}
	}
	
	private void createAcceptedItem(Task task, Identity assessedIdentity, BusinessGroup assessedGroup, boolean coach) {
		if(task == null || task.getAcceptationDate() == null || coach) return;

		if(task.getAcceptationDate().after(compareDate)) {
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			String[] params = new String[] { 
				getTaskName(task),
				courseEntry.getDisplayname()
			};
			if(assessedGroup != null) {
				appendSubscriptionItem("notifications.accepted", params, assessedGroup, task.getAcceptationDate(), coach);
			} else {
				appendSubscriptionItem("notifications.accepted", params, assessedIdentity, task.getAcceptationDate(), coach);
			}
		}
	}
	
	private void createAssessmentItem(Task task, Identity assessedIdentity, boolean coach) {
		if(task == null || task.getGraduationDate() == null) return;
		
		if(task.getGraduationDate().after(compareDate)) {
			RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
			AssessmentEntry assessment = courseNodeAssessmentDao.loadAssessmentEntry(assessedIdentity, courseEntry, gtaNode.getIdent());
			boolean resultsVisible = assessment != null
					&& assessment.getUserVisibility() != null && assessment.getUserVisibility().booleanValue();
			if(resultsVisible) {
				String score = null;
				String status = null;
				CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, gtaNode);
				if(Mode.none != assessmentConfig.getScoreMode() && assessment.getScore() != null) {
					score = AssessmentHelper.getRoundedScore(assessment.getScore());
				}
				if(Mode.none != assessmentConfig.getPassedMode() && assessment.getPassed() != null) {
					status = assessment.getPassed().booleanValue()
							? translator.translate("notifications.assessment.passed.true") : translator.translate("notifications.assessment.passed.false");
				}

				Date graduationDate = task.getGraduationDate();
				String[] params = new String[] {
						getTaskName(task),
						courseEntry.getDisplayname(),
						score,
						status
				};
				
				if(score != null && status != null) {
					if(assessment.getPassed().booleanValue()) {
						appendSubscriptionItem("notifications.assessment.score.passed", params, assessedIdentity, graduationDate, coach);
					} else {
						appendSubscriptionItem("notifications.assessment.score.notpassed", params, assessedIdentity, graduationDate, coach);
					}
				} else if(score != null) {
					appendSubscriptionItem("notifications.assessment.score", params, assessedIdentity, graduationDate, coach);
				} else if(status != null) {
					appendSubscriptionItem("notifications.assessment.passed", params, assessedIdentity, graduationDate, coach);
				}

				ICourse course = CourseFactory.loadCourse(courseEnv.getCourseGroupManager().getCourseEntry());
				UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
						.createAndInitUserCourseEnvironment(assessedIdentity, course);
				List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode,
						assessedUserCourseEnv);
				for(File doc:docs) {
					String[] docParams = new String[] {
							getTaskName(task),
							courseEntry.getDisplayname(),
							doc.getName()
					};
					appendSubscriptionItemForFile("notifications.assessment.doc", docParams, assessedIdentity,
							"[assessment:0]",  doc, graduationDate, coach);
				}
			}
		}
	}
	
	private void createCoachSolutionItems() {
		//copy solutions
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
			File solutionDirectory = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
			VFSContainer solutionContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
			File[] solutions = solutionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
			for(File solution:solutions) {
				String author = getAuthor(solution, solutionContainer);
				String[] params = new String[] {
					displayName,
					solution.getName(),
					author
				};
				appendSubscriptionItemForFile("notifications.solution", params, "",
						"[solution:0]" , solution, null, true);
			}
		}
	}
	
	private boolean createParticipantSolutionItems(Task task, Identity assessedIdentity, BusinessGroup group) {
		//copy solutions
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
			RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
			DueDate dueDate = gtaManager.getSolutionDueDate(task, assessedIdentity, group, gtaNode, re, true);

			Date solutionDate = null;
			if(dueDate == null) {
				if(inStep(task, TaskProcess.solution, TaskProcess.grading, TaskProcess.graded)) {
					solutionDate = task.getSolutionDate();
				} else {
					return false;
				}
			} else if(dueDate != null && dueDate.getDueDate() != null) {
				if(task == null) {
					if(inStep(task, TaskProcess.assignment, TaskProcess.submit, TaskProcess.review, TaskProcess.correction, TaskProcess.revision)
						&& !dueDate.isRelative() && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false)) {
						solutionDate = dueDate.getDueDate();
					} else {
						return false;
					}
				} else {
					solutionDate = dueDate.getDueDate();
				}
			} else if(task != null) {
				solutionDate = task.getSolutionDate();
			}

			if(solutionDate != null) {
				if(task.getSolutionDate() != null && task.getSolutionDate().after(solutionDate)) {
					solutionDate = task.getSolutionDate();
				}
				
				File solutionDirectory = gtaManager.getSolutionsDirectory(courseEnv, gtaNode);
				VFSContainer solutionContainer = gtaManager.getSolutionsContainer(courseEnv, gtaNode);
				File[] solutions = solutionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
				for(File solution:solutions) {
					Date date = solutionDate;
					
					//check if there are new solutions too
					cal.setTimeInMillis(solution.lastModified());
					Date modificationDate = cal.getTime();
					if(date.before(modificationDate)) {
						date = modificationDate;
					}
					
					if(date.after(compareDate)) {
						String author = getAuthor(solution, solutionContainer);
						if(task != null) {
							String[] params = new String[] {
									getTaskName(task),
									displayName,
									solution.getName(),
									author
								};
							if(group != null) {
								appendSubscriptionItemForFile("notifications.solution.task", params, group,
										"[solution:0]", solution, date, false);
							} else {
								appendSubscriptionItemForFile("notifications.solution.task", params, assessedIdentity,
										"[solution:0]" , solution, date, false);
							}
						} else {
							String[] params = new String[] {
									displayName,
									solution.getName(),
									author
								};
							if(group != null) {
								appendSubscriptionItemForFile("notifications.solution", params, group,
										"[solution:0]" , solution, date, false);
							} else {
								appendSubscriptionItemForFile("notifications.solution", params, assessedIdentity,
										"[solution:0]" , solution, date, false);
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}	
	
	private Task checkSubmitStep(Identity assessedIdentity, BusinessGroup assessedGroup, Task task) {
		if(task != null && task.getTaskStatus() == TaskProcess.submit) {
			RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
			DueDate dueDate = gtaManager.getSubmissionDueDate(task, assessedIdentity, assessedGroup, gtaNode, re, true);
			DueDate lateDueDate = gtaManager.getLateSubmissionDueDate(task, assessedIdentity, assessedGroup, gtaNode, re, true);
			Date deadline = gtaManager.getDeadlineOf(dueDate, lateDueDate);
			if(deadline != null &&  deadline.before(new Date())) {
				int numOfDocs = getNumberOfSubmittedDocuments(assessedIdentity, assessedGroup);
				task = gtaManager.submitTask(task, gtaNode, numOfDocs, null, Role.auto);
				gtaManager.log("Submit", (SubmitEvent)null, task, null, assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.auto);
			}
		}
		return task;
	}
	
	private int getNumberOfSubmittedDocuments(Identity assessedIdentity, BusinessGroup assessedGroup) {
		File[] submittedDocuments;
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));

		} else {
			File documentsDir = gtaManager.getSubmitDirectory(courseEnv, gtaNode, assessedIdentity);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}
		return submittedDocuments == null ? 0 : submittedDocuments.length;
	}
	
	private boolean checkRevisionLoop(TaskProcess status, int revisionLoop,  List<TaskRevisionDate> taskRevisions) {
		for(TaskRevisionDate taskRevision:taskRevisions) {
			if(taskRevision.getTaskStatus() == status && taskRevision.getRevisionLoop() == revisionLoop) {
				Date date = taskRevision.getDate();
				if(date.after(compareDate)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Date getRevisionLoopDate(TaskProcess status, int revisionLoop,  List<TaskRevisionDate> taskRevisions) {
		for(TaskRevisionDate taskRevision:taskRevisions) {
			if(taskRevision.getTaskStatus() == status && taskRevision.getRevisionLoop() == revisionLoop) {
				return taskRevision.getDate();
			}
		}
		return null;
	}
	
	/**
	 * This checks the revision due date.
	 * 
	 * @param assessedIdentity The identity which is assessed
	 * @param assessedGroup The group which is assessed
	 * @param task The task
	 * @return An updated task if the status as automatically changed
	 */
	private Task checkRevisionStep(Task task) {
		if(task != null) {
			if(task.getTaskStatus() == TaskProcess.revision
					&& task.getRevisionsDueDate() != null
					&& task.getRevisionsDueDate().compareTo(new Date()) < 0) {
				//push to the next step
				task = gtaManager.nextStep(task, gtaNode, true, null, Role.auto);
			}
		}
		return task;
	}
	
	private boolean isSolutionVisible(Identity assessedIdentity, BusinessGroup assessedGroup, Task task) {
		if(task != null) {
			RepositoryEntry re = courseEnv.getCourseGroupManager().getCourseEntry();
			DueDate availableDate = gtaManager.getSolutionDueDate(task, assessedIdentity, assessedGroup, gtaNode, re, true);
			boolean visible = availableDate == null || 
					(availableDate.getDueDate() != null && availableDate.getDueDate().compareTo(new Date()) <= 0);
			if(visible) {
				if(task.getTaskStatus() == TaskProcess.solution || task.getTaskStatus() == TaskProcess.grading || task.getTaskStatus() == TaskProcess.graded) {
					// step solution or beyond
					return true;
				} else if((task.getTaskStatus() == TaskProcess.assignment || task.getTaskStatus() == TaskProcess.submit
						|| task.getTaskStatus() == TaskProcess.review || task.getTaskStatus() == TaskProcess.correction
						|| task.getTaskStatus() == TaskProcess.revision)
						&& gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION_VISIBLE_ALL, false)) {
					// steps before solution only if configured to
					return true;
				}
			}
		}
		return false;
	}
	
	private String getAuthor(File file, VFSContainer container) {
		String author = null;
		VFSItem item = container.resolve(file.getName());
		if(item.canMeta() == VFSConstants.YES) {
			VFSMetadata info = item.getMetaInfo();
			Identity identityKey = info.getFileInitializedBy();
			if(identityKey != null) {
				author = userManager.getUserDisplayName(identityKey);
			}		

		}
		return author == null ? "" : author;
	}
	

	private void appendSubscriptionItemForFile(String notificationKey, String[] params, Identity identity,
			String fileCategory, File file, Date modificationDate, boolean coach) {
		String idPath = "[Identity:" + identity.getKey() + "]";
		appendSubscriptionItemForFile(notificationKey, params, idPath, fileCategory, file, modificationDate, coach);
	}
	
	private void appendSubscriptionItemForFile(String notificationKey, String[] params, BusinessGroup group,
			String fileCategory, File file, Date modificationDate, boolean coach) {
		String idPath = "[BusinessGroup:" + group.getKey() + "]";
		appendSubscriptionItemForFile(notificationKey, params, idPath, fileCategory, file, modificationDate, coach);
	}
	
	private void appendSubscriptionItemForFile(String notificationKey, String[] params, String idPath,
			String fileCategory, File file, Date modificationDate, boolean coach) {
		if(modificationDate == null) {
			cal.setTimeInMillis(file.lastModified());
			modificationDate = cal.getTime();
		}
		if(modificationDate.compareTo(compareDate) >= 0) {
			String desc = translator.translate(notificationKey, params);
			String businessPath = subscriber.getPublisher().getBusinessPath();
			if(coach) {
				businessPath += "[Coach:0]";
			}
			if(StringHelper.containsNonWhitespace(idPath) ) {
				businessPath += idPath;
			}
			if(StringHelper.containsNonWhitespace(fileCategory)) {
				businessPath += fileCategory;
			}
			if(file != null) {
				businessPath += "[path=" + file.getName() + ":0]";
			}
			String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			String iconCssClass = GTANotificationsHandler.CSS_CLASS_ICON;
			SubscriptionListItem item = new SubscriptionListItem(desc, urlToSend, businessPath, modificationDate, iconCssClass);
			items.add(item);
		}
	}
	
	private void appendSubscriptionItem(String notificationKey, String[] params, Identity identity, Date modificationDate, boolean coach) {
		String path =  "[Identity:" + identity.getKey() + "]";
		appendSubscriptionItem(notificationKey, params, path, modificationDate, coach);
	}
	
	private void appendSubscriptionItem(String notificationKey, String[] params, BusinessGroup group, Date modificationDate, boolean coach) {
		String path =  "[BusinessGroup:" + group.getKey() + "]";
		appendSubscriptionItem(notificationKey, params, path, modificationDate, coach);
	}
	
	private void appendSubscriptionItem(String notificationKey, String[] params, String path, Date modificationDate, boolean coach) {
		String desc = translator.translate(notificationKey, params);
		String businessPath = subscriber.getPublisher().getBusinessPath();
		if(coach) {
			businessPath += "[Coach:0]";
		}
		if(StringHelper.containsNonWhitespace(path)) {
			businessPath += path;
		}
		String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		String iconCssClass = GTANotificationsHandler.CSS_CLASS_ICON;
		SubscriptionListItem item = new SubscriptionListItem(desc, urlToSend, businessPath, modificationDate, iconCssClass);
		items.add(item);
	}
	
	private boolean notInStep(Task task, TaskProcess... status) {
		boolean ok = true;
		if(task == null) {
			ok &= false;
		} else {
			for(TaskProcess state:status) {
				ok &= task.getTaskStatus() != state;
			}
		}
		return ok;
	}
	
	private boolean inStep(Task task, TaskProcess... status) {
		boolean ok = false;
		if(task != null) {
			for(TaskProcess state:status) {
				ok |= task.getTaskStatus() == state;
			}
		}
		return ok;
	}
	
	private String getTaskName (Task task) {
		if (!StringHelper.containsNonWhitespace(task.getTaskName())) {
			return gtaNode.getShortTitle();	
		} else {
			return task.getTaskName();
		}
	}
}
