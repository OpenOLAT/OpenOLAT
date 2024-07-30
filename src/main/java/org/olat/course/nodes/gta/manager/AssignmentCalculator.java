/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.gta.AssignmentType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskReviewAssignment;

/**
 * 
 * Initial date: 25 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentCalculator {
	
	private static final Logger logger = Tracing.createLoggerFor(AssignmentCalculator.class);

    private static final Random random = new Random();
	
	private final List<Task> allTasks;
	private final Map<Identity,Task> allTasksMap;
	private final Set<Long> allCourseParticipantsKeys;
	private final List<TaskReviewAssignment> allAssignments;
	
	public AssignmentCalculator(List<Identity> allParticipants, List<Task> allTasks, List<TaskReviewAssignment> allAssignments) {
		allCourseParticipantsKeys = allParticipants.stream()
				.map(Identity::getKey)
				.collect(Collectors.toSet());
		this.allAssignments = allAssignments;
		this.allTasks = allTasks;
		allTasksMap = allTasks.stream()
				.collect(Collectors.toMap(Task::getIdentity, task -> task, (u,v) -> u));
	}
	
	public List<Participant> assign(AssignmentType assignmentType, int numberOfReviews, boolean mutual) {
		List<Participant> participants = loadCurrentState();
		
		for(Participant participant:participants) {
			List<Participant> toReviewList = new ArrayList<>(participants);
			Collections.shuffle(toReviewList, random);
			awardReviews(participant, toReviewList, assignmentType, numberOfReviews, mutual);
		}
		return participants;
	}
	
	private void awardReviews(Participant participant, List<Participant> toReviewList,
			AssignmentType assignmentType, int numberOfReviews, boolean mutual) {
		logger.debug("Assignment for: {}", participant.participant().getUser().getLastName());
		
		for(Participant toReview:toReviewList) {
			if(participant.numberOfAwardedReviews() >= numberOfReviews) {
				break;
			}
			if(exclude(participant, toReview, assignmentType, numberOfReviews)) {
				continue;
			}
			
			if(mutual) {
				// Check if the identity to review can review the participant
				if(!exclude(toReview, participant, assignmentType, numberOfReviews)
						&& toReview.numberOfAwardedReviews() < numberOfReviews) {
					// Awarded: list of the reviews the participant needs to do
					participant.plannedAwardedReviews().add(toReview.task());
					toReview.plannedAwardedReviews().add(participant.task());
					
					// Received: list of the reviews made by others
					toReview.plannedReceivedReviews().add(participant.participant());
					participant.plannedReceivedReviews().add(toReview.participant());
				}
			} else {
				// Awarded: list of the reviews the participant needs to do
				participant.plannedAwardedReviews().add(toReview.task());
				// Received: list of the reviews made by others
				toReview.plannedReceivedReviews().add(participant.participant());
			}
		}
	}
	
	private boolean exclude(Participant participant, Participant toReview,
			AssignmentType assignmentType, int numberOfReviews) {
		return (participant.participant().equals(toReview.participant())
				|| excludeMatchingByTask(participant, toReview, assignmentType)
				|| participant.isTaskAlreadyAwarded(toReview.task())
				|| toReview.numberOfReceivedReviews() >= numberOfReviews);
	}

	private boolean excludeMatchingByTask(Participant participant, Participant reviewer, AssignmentType assignmentType) {
		return switch(assignmentType) {
			case SAME_TASK -> excludeMatchingBySameTask(participant, reviewer);
			case OTHER_TASK -> excludeMatchingByOtherTask(participant, reviewer);
			case RANDOM -> false;
			default -> false;
		};
	}
	
	private boolean excludeMatchingBySameTask(Participant participant, Participant reviewer) {
		String participantTaskName = participant.getTaskName();
		String reviewerTaskName = reviewer.getTaskName();
		return participantTaskName != null && reviewerTaskName != null && !participantTaskName.equals(reviewerTaskName);
	}
	
	private boolean excludeMatchingByOtherTask(Participant participantTask, Participant reviewerTask) {
		String participantTaskName = participantTask.getTaskName();
		String reviewerTaskName = reviewerTask.getTaskName();
		
		Set<String> alreadyAwardedTaskName = participantTask.plannedAwardedReviews().stream()
				.map(Task::getTaskName)
				.collect(Collectors.toSet());
		for(TaskReviewAssignment assignment:participantTask.awardedReviews()) {
			Identity assignee = assignment.getAssignee();
			Task taskOfAssignee = allTasksMap.get(assignee);
			if(taskOfAssignee != null && StringHelper.containsNonWhitespace(taskOfAssignee.getTaskName())) {
				alreadyAwardedTaskName.add(taskOfAssignee.getTaskName());
			}
		}
		
		if(alreadyAwardedTaskName.isEmpty()) {
			return participantTaskName != null && reviewerTaskName != null
					&& participantTaskName.equals(reviewerTaskName);
		}
		return participantTaskName != null && reviewerTaskName != null
				&& !alreadyAwardedTaskName.contains(reviewerTaskName);
	}
	
	private List<Participant> loadCurrentState() {
		Map<Identity,List<TaskReviewAssignment>> taskToAssignments = new HashMap<>();
		Map<Identity,List<TaskReviewAssignment>> assigneeToAssignments = new HashMap<>();
		for(TaskReviewAssignment assignment:allAssignments) {
			if(assignment.isAssigned()) {
				assigneeToAssignments
					.computeIfAbsent(assignment.getAssignee(), t -> new ArrayList<>())
					.add(assignment);
			
				taskToAssignments
					.computeIfAbsent(assignment.getTask().getIdentity(), t -> new ArrayList<>())
					.add(assignment);
			}
		}
		
		return allTasks.stream()
				.filter(task -> task != null && task.getIdentity() != null)
				.filter(task -> allCourseParticipantsKeys.contains(task.getIdentity().getKey()))
				.filter(task -> StringHelper.containsNonWhitespace(task.getTaskName()))
				.filter(task -> task.getTaskStatus() != null
						&& task.getTaskStatus() != TaskProcess.assignment
						&& task.getTaskStatus() != TaskProcess.submit)
				.map(task -> {
					Identity identity = task.getIdentity();
					// Reviews already awarded, the identity needs to do them
					List<TaskReviewAssignment> awardedReviews = assigneeToAssignments.getOrDefault(identity, new ArrayList<>());
					// Reviews already received, the task will be reviewed
					List<TaskReviewAssignment> receivedReviews = taskToAssignments.getOrDefault(identity, new ArrayList<>());
					return new Participant(identity, task,
							awardedReviews, receivedReviews,
							new ArrayList<>(), new ArrayList<>());
				})
				.toList();	
	}

	public record Participant(Identity participant, Task task,
			List<TaskReviewAssignment> awardedReviews, List<TaskReviewAssignment> receivedReviews,
			List<Task> plannedAwardedReviews, List<Identity> plannedReceivedReviews) {
		
		public String getTaskName() {
			return task == null ? null : task.getTaskName();
		}

		public boolean isTaskAlreadyAwarded(Task task) {
			for(TaskReviewAssignment awardedReview:awardedReviews) {
				if(task.equals(awardedReview.getTask())) {
					return true;
				}
			}
			return plannedAwardedReviews.contains(task);
		}
		
		public int numberOfReceivedReviews() {
			return receivedReviews.size() + plannedReceivedReviews.size();
		}
		
		public int numberOfAwardedReviews() {
			return awardedReviews.size() + plannedAwardedReviews.size();
		}
	}
}
