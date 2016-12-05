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

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.model.Membership;
import org.olat.course.nodes.gta.ui.GTARunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;

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
	private String displayName;
	private Calendar cal = Calendar.getInstance();
	
	private final Translator translator;
	
	private final GTAManager gtaManager;
	private final UserManager userManager;
	private final RepositoryService repositoryService;
	private final BusinessGroupService businessGroupService;
	
	public GTANotifications(Subscriber subscriber, Locale locale, Date compareDate,
			RepositoryService repositoryService, GTAManager gtaManager,
			BusinessGroupService businessGroupService, UserManager userManager) {
		this.gtaManager = gtaManager;
		this.userManager = userManager;
		this.repositoryService = repositoryService;
		this.businessGroupService = businessGroupService;
		
		this.subscriber = subscriber;
		this.compareDate = compareDate;
		translator = Util.createPackageTranslator(GTARunController.class, locale);
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public List<SubscriptionListItem> getItems() {
		Publisher p = subscriber.getPublisher();
		ICourse course = CourseFactory.loadCourse(p.getResId());
		CourseNode node = course.getRunStructure().getNode(p.getSubidentifier());
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		if(entry.getRepositoryEntryStatus().isClosed() || entry.getRepositoryEntryStatus().isUnpublished()) {
			return Collections.emptyList();
		}
		
		if(entry != null && node instanceof GTACourseNode) {
			displayName = entry.getDisplayname();
			
			GTACourseNode gtaNode = (GTACourseNode)node;
			taskList = gtaManager.getTaskList(entry, gtaNode);
			
			if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
				createBusinessGroupsSubscriptionInfo(subscriber.getIdentity(), course.getCourseEnvironment(), gtaNode);
			} else {
				createIndividualSubscriptionInfo(subscriber.getIdentity(), course.getCourseEnvironment(), gtaNode);
			}

			//copy solutions
			if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SAMPLE_SOLUTION)) {
				File solutionDirectory = gtaManager.getSolutionsDirectory(course.getCourseEnvironment(), gtaNode);
				VFSContainer solutionContainer = gtaManager.getSolutionsContainer(course.getCourseEnvironment(), gtaNode);
				File[] solutions = solutionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
				for(File solution:solutions) {
					String author = getAuthor(solution, solutionContainer);
					appendSubscriptionItem("notifications.solution", new String[] { solution.getName(), author }, solution);
				}
			}
		}
		
		return items;
	}
	
	private void createIndividualSubscriptionInfo(Identity subscriberIdentity, CourseEnvironment ce, GTACourseNode gtaNode) {
		RepositoryEntry entry = ce.getCourseGroupManager().getCourseEntry();
		List<String> roles = repositoryService.getRoles(subscriberIdentity, entry);
		
		boolean owner = roles.contains(GroupRoles.owner.name());
		boolean coach = roles.contains(GroupRoles.coach.name());
		if(owner|| coach) {
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
				if(!duplicateKiller.contains(participant)) {
					assessableIdentities.add(participant);
					duplicateKiller.add(participant);
				}
			}
			
			boolean repoTutor = owner || (coachedGroups.isEmpty() && repositoryService.hasRole(subscriberIdentity, entry, GroupRoles.coach.name()));
			if(repoTutor) {
				List<Identity> courseParticipants = repositoryService.getMembers(entry, GroupRoles.participant.name());
				for(Identity participant:courseParticipants) {
					if(!duplicateKiller.contains(participant)) {
						assessableIdentities.add(participant);
						duplicateKiller.add(participant);
					}
				}
			}
			
			for(Identity assessedIdentity: assessableIdentities) {
				createIndividualSubscriptionInfo(assessedIdentity, ce, gtaNode, true);
			}
		}
		
		if(roles.contains(GroupRoles.participant.name())) {
			createIndividualSubscriptionInfo(subscriberIdentity, ce, gtaNode, false);
		}
	}

	private void createIndividualSubscriptionInfo(Identity assessedIdentity, CourseEnvironment ce, GTACourseNode gtaNode, boolean coach) {
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		if(coach && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			File submitDirectory = gtaManager.getSubmitDirectory(ce, gtaNode, assessedIdentity);
			File[] solutions = submitDirectory.listFiles(SystemFileFilter.FILES_ONLY);
			for(File solution:solutions) {
				appendSubscriptionItem("notifications.submission.individual", new String[] { solution.getName(), fullName }, solution);
			}
		}
		
		if(!coach && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionDirectory = gtaManager.getCorrectionDirectory(ce, gtaNode, assessedIdentity);
			VFSContainer correctionContainer = gtaManager.getCorrectionContainer(ce, gtaNode, assessedIdentity);
			File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
			for(File correction:corrections) {
				String author = getAuthor(correction, correctionContainer);
				appendSubscriptionItem("notifications.correction", new String[] { correction.getName(), author }, correction);
			}
		}
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			Task task = gtaManager.getTask(assessedIdentity, taskList);
			if(task != null) {
				int currentIteration = task.getRevisionLoop();
				for(int i=1; i<=currentIteration; i++) {
					//revision
					
					if(coach) {
						//revision
						File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(ce, gtaNode, i, assessedIdentity);
						File[] revisions = revisionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
						for(File revision:revisions) {
							appendSubscriptionItem("notifications.revision.individual", new String[] { revision.getName(), fullName }, revision);
						}
					} else {
						//corrections
						File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(ce, gtaNode, i, assessedIdentity);
						VFSContainer correctionContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(ce, gtaNode, i, assessedIdentity);
						File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
						for(File correction:corrections) {
							String author = getAuthor(correction, correctionContainer);
							appendSubscriptionItem("notifications.correction", new String[] { correction.getName(), author }, correction);
						}
					}
				}
			}
		}
	}
	
	private void createBusinessGroupsSubscriptionInfo(Identity subscriberIdentity, CourseEnvironment ce, GTACourseNode gtaNode) {
		RepositoryEntry entry = ce.getCourseGroupManager().getCourseEntry();

		Membership membership = gtaManager.getMembership(subscriberIdentity, entry, gtaNode);
		boolean owner = repositoryService.hasRole(subscriberIdentity, entry, GroupRoles.owner.name());
		if(owner) {
			List<BusinessGroup> groups = gtaManager.getBusinessGroups(gtaNode);
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, ce, gtaNode, true);
			}
		} else if(membership.isCoach() ) {
			List<BusinessGroup> groups = gtaManager.getCoachedBusinessGroups(subscriberIdentity, gtaNode);
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, ce, gtaNode, true);
			}
		}
		
		if(membership.isParticipant()) {
			List<BusinessGroup> groups = gtaManager.getParticipatingBusinessGroups(subscriberIdentity, gtaNode);
			for(BusinessGroup group:groups) {
				createBusinessGroupsSubscriptionItems(group, ce, gtaNode, false);
			}
		}
	}
	
	private void createBusinessGroupsSubscriptionItems(BusinessGroup group, CourseEnvironment ce, GTACourseNode gtaNode, boolean coach) {
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_SUBMIT)) {
			File submitDirectory = gtaManager.getSubmitDirectory(ce, gtaNode, group);
			VFSContainer submitContainer = gtaManager.getSubmitContainer(ce, gtaNode, group);
			File[] documents = submitDirectory.listFiles(SystemFileFilter.FILES_ONLY);
			for(File document:documents) {
				String author = getAuthor(document, submitContainer);
				appendSubscriptionItem("notifications.submission.group", new String[] { document.getName(), group.getName(), author }, document);
			}
		}
		
		if(!coach && gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)) {
			File correctionDirectory = gtaManager.getCorrectionDirectory(ce, gtaNode, group);
			VFSContainer correctionContainer = gtaManager.getCorrectionContainer(ce, gtaNode, group);
			File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
			for(File correction:corrections) {
				String author = getAuthor(correction, correctionContainer);
				appendSubscriptionItem("notifications.correction", new String[] { correction.getName(), author }, correction);
			}
		}
		
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			Task task = gtaManager.getTask(group, taskList);
			if(task != null) {
				int currentIteration = task.getRevisionLoop();
				for(int i=1; i<=currentIteration; i++) {
					//revision
					File revisionDirectory = gtaManager.getRevisedDocumentsDirectory(ce, gtaNode, i, group);
					VFSContainer revisionContainer = gtaManager.getRevisedDocumentsContainer(ce, gtaNode, i, group);

					File[] revisions = revisionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
					for(File revision:revisions) {
						String author = getAuthor(revision, revisionContainer);
						appendSubscriptionItem("notifications.revision.group", new String[] { revision.getName(), group.getName(), author }, revision);
					}
					//corrections
					if(!coach) {
						File correctionDirectory = gtaManager.getRevisedDocumentsCorrectionsDirectory(ce, gtaNode, i, group);
						VFSContainer correctionContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(ce, gtaNode, i, group);
						File[] corrections = correctionDirectory.listFiles(SystemFileFilter.FILES_ONLY);
						for(File correction:corrections) {
							String author = getAuthor(correction, correctionContainer);
							appendSubscriptionItem("notifications.correction", new String[] { correction.getName(), author }, correction);
						}
					}
				}
			}
		}
	}
	
	private String getAuthor(File file, VFSContainer container) {
		String author = null;
		VFSItem item = container.resolve(file.getName());
		if(item instanceof MetaTagged) {
			MetaInfo info = ((MetaTagged)item).getMetaInfo();
			if(info != null) {
				String username = info.getAuthor();
				if(username != null) {
					author = userManager.getUserDisplayName(username);
				}		
			}
		}
		return author == null ? "" : author;
	}
	
	private void appendSubscriptionItem(String notificationKey, String[] params, File file) {
		cal.setTimeInMillis(file.lastModified());
		Date modDate = cal.getTime();
		if(modDate.compareTo(compareDate) >= 0) {
			String desc = translator.translate(notificationKey, params);
			String businessPath = subscriber.getPublisher().getBusinessPath();
			String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			String iconCssClass = GTANotificationsHandler.CSS_CLASS_ICON;
			SubscriptionListItem item = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass);
			items.add(item);
		}
	}
}
