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
package org.olat.modules.openbadges.criteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2023-06-21<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeCriteria {
	private static final Logger log = Tracing.createLoggerFor(BadgeCriteria.class);

	private String description;
	private boolean awardAutomatically;
	private List<BadgeCondition> conditions;

	public BadgeCriteria() {
		conditions = new ArrayList<>();
	}

	public String getDescription() {
		return description;
	}

	public String getDescriptionWithScan() {
		return StringHelper.xssScan(getDescription());
	}

	public boolean isAwardAutomatically() {
		return awardAutomatically;
	}

	public void setAwardAutomatically(boolean awardAutomatically) {
		this.awardAutomatically = awardAutomatically;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDescriptionWithScan(String description) {
		setDescription(StringHelper.unescapeHtml(FilterFactory.getHtmlTagsFilter().filter(StringHelper.xssScan(description))));
	}

	public List<BadgeCondition> getConditions(Set<String> acceptedKeys, Set<String> courseElementIdents) {
		return conditions.stream()
				.filter(bc -> StringHelper.containsNonWhitespace(bc.getKey()))
				.filter(bc -> acceptedKeys.contains(bc.getKey()))
				.filter(bc -> checkCourseElementSubIdent(bc, courseElementIdents))
				.toList();
	}

	private boolean checkCourseElementSubIdent(BadgeCondition bc, Set<String> courseElementIdents) {
		if (bc instanceof CompletionCriterionMetCondition completionCriterionMetCondition) {
			if (!courseElementIdents.contains(completionCriterionMetCondition.getSubIdent())) {
				return false;
			}
		}
		
		if (bc instanceof CourseElementPassedCondition courseElementPassedCondition) {
			if (!courseElementIdents.contains(courseElementPassedCondition.getSubIdent())) {
				return false;
			}
		}

		if (bc instanceof CourseElementScoreCondition courseElementScoreCondition) {
			if (!courseElementIdents.contains(courseElementScoreCondition.getSubIdent())) {
				return false;
			}
		}

		return true;
	}

	public List<BadgeCondition> getConditions() {
		return conditions;
	}

	public void setConditions(List<BadgeCondition> conditions) {
		this.conditions = conditions;
	}

	/**
	 * When copying, cloning or importing a course, we copy course badge classes, and
	 * the UUID of the copied badge classes changes in the process.
	 *
	 * This method detects occurrences of old UUIDs and replaces them with the
	 * corresponding new UUID.

	 * @param badgeClassUuidMap Maps old UUIDs to new UUIDs.
	 *
	 *  @return true if one of the conditions of this BadgeCriteria object has changed during this call.
	 */
	public boolean remapBadgeClassUuids(Map<String, String> badgeClassUuidMap) {
		boolean atLeastOneUuidRemapped = false;
		for (BadgeCondition condition : conditions) {
			if (condition instanceof OtherBadgeEarnedCondition otherBadgeEarnedCondition) {
				String uuid = otherBadgeEarnedCondition.getBadgeClassUuid();
				if (badgeClassUuidMap.containsKey(uuid)) {
					otherBadgeEarnedCondition.setBadgeClassUuid(badgeClassUuidMap.get(uuid));
					atLeastOneUuidRemapped = true;
				}
			}
		}
		return atLeastOneUuidRemapped;
	}

	/**
	 * Checks that all conditions to issuing a badge have been met for a course and recipient.
	 * 
	 * @param courseEntry The course to which the conditions apply.
	 * @param uce The user course environment for the recipient. Can be null for global badges.
	 * @param recipient The person that would obtain the badge.
	 * @param learningPath Is this course a learning path?
	 * @param courseBadge Is this a course badge?
	 * @param assessmentEntries An optional list of assessment entries to be used in conditions that use assessment data. 
	 *                          Can be empty or null. Can be only root entries if it is not a course badge.
	 * @return True if the conditions for receiving the badge are all met.
	 */
	public boolean allConditionsMet(RepositoryEntry courseEntry, UserCourseEnvironment uce, Identity recipient, 
									boolean learningPath, boolean courseBadge, List<AssessmentEntry> assessmentEntries) {
		if (!isAwardAutomatically()) {
			if (log.isDebugEnabled()) {
				log.debug("allConditionsMet(): not awarding badge automatically");
			}
			return true;
		}

		if (assessmentEntries != null) {
			assessmentEntries = assessmentEntries.stream()
					.filter(ae -> ae.getIdentity().equals(recipient))
					.filter(ae -> courseEntry == null || Objects.equals(courseEntry.getKey(), ae.getRepositoryEntry().getKey()))
					.toList();
		} else {
			assessmentEntries = new ArrayList<>();
		}
		
		if (log.isDebugEnabled()) {
			log.debug("allConditionsMet(): {} filtered assessmentEntries", assessmentEntries.size());
		}

		if (courseBadge) {
			return allCourseConditionsMet(courseEntry, recipient, learningPath, assessmentEntries, uce);
		} else {
			return allGlobalBadgeConditionsMet(recipient, assessmentEntries);
		}
	}

	/**
	 * Checks if all the conditions specific to a course of this BadgeCriteria object are
	 * satisfied.
	 *
	 * @param courseEntry       The course entry to which the conditions apply.
	 * @param recipient         The recipient to check the conditions for.
	 * @param learningPath      If true, the assessed course is a learning path
	 * @param assessmentEntries The assessment entries for the course and the recipient.
	 * @param uce               The user course environment for the recipient.
	 * @return True if all conditions of this badge criteria object are satisfied.
	 */
	private boolean allCourseConditionsMet(RepositoryEntry courseEntry, Identity recipient, boolean learningPath, 
										   List<AssessmentEntry> assessmentEntries, UserCourseEnvironment uce) {
		final boolean[] courseBadgeConditionChecked = { false };

		RepositoryEntryDAO repositoryEntryDao = CoreSpringFactory.getImpl(RepositoryEntryDAO.class);
		RepositoryEntry reloadedCourseEntry = repositoryEntryDao.loadByKey(courseEntry.getKey());
		
		ICourse course = CourseFactory.loadCourse(reloadedCourseEntry);
		if (uce == null || !course.getResourceableId().equals(uce.getCourseEnvironment().getCourseResourceableId())) {
			IdentityEnvironment ienv = uce != null ? uce.getIdentityEnvironment() : new IdentityEnvironment(recipient, Roles.userRoles());
			uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
		}

		if (!allConditionsApplyingToCoursesOnlyMet(reloadedCourseEntry, course, uce, recipient, learningPath, assessmentEntries, 
				courseBadgeConditionChecked)) {
			return false;
		}
		if (!allOtherBadgeConditionsMet(recipient, courseBadgeConditionChecked)) {
			return false;
		}

		if (!courseBadgeConditionChecked[0]) {
			if (log.isDebugEnabled()) {
				log.debug("allCourseConditionsMet(): no course badge conditions have been checked: consider the criteria unmet.");
			}
			return false;
		}

		return true;
	}

	private boolean allConditionsApplyingToCoursesOnlyMet(RepositoryEntry courseEntry, ICourse course, 
														  UserCourseEnvironment uce,
														  Identity recipient, boolean learningPath,
														  List<AssessmentEntry> assessmentEntries,
														  boolean[] courseBadgeConditionChecked) {
		if (assessmentEntries.isEmpty() && courseEntry != null) {
			AssessmentEntryDAO assessmentEntryDAO = CoreSpringFactory.getImpl(AssessmentEntryDAO.class);
			assessmentEntries = assessmentEntryDAO.loadAssessmentEntriesByAssessedIdentity(recipient, courseEntry);
			if (log.isDebugEnabled()) {
				log.debug("allConditionsApplyingToCoursesOnlyMet(): loaded {} assessmentEntries", assessmentEntries.size());
			}
		}

		if (!allCourseConditionsMet(assessmentEntries, courseBadgeConditionChecked)) {
			return false;
		}
		if (!allCourseElementConditionsMet(course, uce, courseBadgeConditionChecked)) {
			return false;
		}
		if (!learningPathCourseElementConditionsMet(learningPath, assessmentEntries, courseBadgeConditionChecked)) {
			return false;
		}
		if (!learningPathConditionMet(learningPath, assessmentEntries, courseBadgeConditionChecked)) {
			return false;
		}

		return true;
	}

	private boolean allCourseConditionsMet(List<AssessmentEntry> assessmentEntries, boolean[] courseBadgeConditionChecked) {
		boolean passed = false;
		float score = 0;
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			if (assessmentEntry.getEntryRoot() != null && assessmentEntry.getEntryRoot()) {
				if (assessmentEntry.getPassed() != null) {
					passed = assessmentEntry.getPassed();
				}
				if (assessmentEntry.getScore() != null) {
					score = assessmentEntry.getScore().floatValue();
				}
			}
		}

		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CoursePassedCondition) {
				if (log.isDebugEnabled()) {
					log.debug("allCourseConditionsMet(): CoursePassedCondition");
				}
				if (!passed) {
					if (log.isDebugEnabled()) {
						log.debug("not passed.");
					}
					return false;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("passed.");
					}
				}
				courseBadgeConditionChecked[0] = true;
			} else if (badgeCondition instanceof CourseScoreCondition courseScoreCondition) {
				if (log.isDebugEnabled()) {
					log.debug("allCourseConditionsMet(): CourseScoreCondition");
				}
				if (!courseScoreCondition.satisfiesCondition(score)) {
					if (log.isDebugEnabled()) {
						log.debug("score condition not met.");
					}
					return false;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("score condition met.");
					}
				}
				courseBadgeConditionChecked[0] = true;
			}
		}
		return true;
	}

	private boolean allCourseElementConditionsMet(ICourse course, UserCourseEnvironment uce,
												  boolean[] courseBadgeConditionChecked) {
		CourseAssessmentService courseAssessmentService = null;
		
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CourseElementPassedCondition courseElementPassedCondition) {
				if (courseAssessmentService == null) {
					courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
				}
				CourseNode courseNode = course.getRunStructure().getNode(courseElementPassedCondition.getSubIdent());
				AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, uce);
				if (log.isDebugEnabled()) {
					log.debug("allCourseElementConditionsMet(): CourseElementPassedCondition");
				}
				if (assessmentEvaluation.getPassed() == null || !assessmentEvaluation.getPassed()) {
					if (log.isDebugEnabled()) {
						log.debug("assessment evaluation not passed for course node '{}' ({}).", 
								courseNode.getShortTitle(), courseNode.getIdent());
					}
					return false;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("assessment evaluation passed for course node '{}' ({}).",
								courseNode.getShortTitle(), courseNode.getIdent());
					}
				}
				courseBadgeConditionChecked[0] = true;
			}

			if (badgeCondition instanceof CourseElementScoreCondition courseElementScoreCondition) {
				if (log.isDebugEnabled()) {
					log.debug("allCourseElementConditionsMet(): check CourseElementScoreCondition");
				}
				if (courseAssessmentService == null) {
					courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
				}
				CourseNode courseNode = course.getRunStructure().getNode(courseElementScoreCondition.getSubIdent());
				AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, uce);
				if (assessmentEvaluation.getScore() == null || !courseElementScoreCondition.satisfiesCondition(assessmentEvaluation.getScore())) {
					if (log.isDebugEnabled()) {
						log.debug("assessment evaluation score {} doesn't satisfy condition for course node '{}' ({})",
								courseElementScoreCondition.getValue(), courseNode.getShortTitle(), courseNode.getIdent());
					}
					return false;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("assessment evaluation score {} satisfies condition for course node '{}' ({})",
								courseElementScoreCondition.getValue(), courseNode.getShortTitle(), courseNode.getIdent());
					}
				}
				courseBadgeConditionChecked[0] = true;
			}
		}
		return true;
	}
	
	private boolean learningPathCourseElementConditionsMet(boolean learningPath, List<AssessmentEntry> assessmentEntries, boolean[] courseBadgeConditionChecked) {
		if (!learningPath) {
			return true;
		}

		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CompletionCriterionMetCondition completionCriterionMetCondition) {
				if (log.isDebugEnabled()) {
					log.debug("learningPathCourseElementConditionsMet(): check CompletionCriterionMetCondition");
				}

				for (AssessmentEntry assessmentEntry : assessmentEntries) {
					if (completionCriterionMetCondition.getSubIdent().equals(assessmentEntry.getSubIdent())) {
						if (assessmentEntry.getFullyAssessed() == null || !assessmentEntry.getFullyAssessed()) {
							if (log.isDebugEnabled()) {
								log.debug("completion criterion not met for course element {}", completionCriterionMetCondition.getSubIdent());
							}
							return false;
						}
					}
				}
				if (log.isDebugEnabled()) {
					log.debug("completion criterion met for course element {}", completionCriterionMetCondition.getSubIdent());
				}
				courseBadgeConditionChecked[0] = true;
			}
		}
		
		return true;
	}

	private boolean learningPathConditionMet(boolean learningPath, List<AssessmentEntry> assessmentEntries, boolean[] courseBadgeConditionChecked) {
		if (!learningPath) {
			return true;
		}

		double learningPathProgress = 0;
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			if (assessmentEntry.getEntryRoot() != null && assessmentEntry.getEntryRoot()) {
				if (assessmentEntry.getCompletion() != null) {
					learningPathProgress = assessmentEntry.getCompletion().floatValue() * 100;
				}
			}
		}

		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof LearningPathProgressCondition learningPathProgressCondition) {
				if (log.isDebugEnabled()) {
					log.debug("learningPathConditionMet(): check LearningPathProgressCondition");
				}

				if  (!learningPathProgressCondition.satisfiesCondition(learningPathProgress)) {
					if (log.isDebugEnabled()) {
						log.debug("learning path progress condition not met.");
					}
					return false;
				}
				if (log.isDebugEnabled()) {
					log.debug("learning path progress condition met.");
				}
				courseBadgeConditionChecked[0] = true;
			}
		}

		return true;
	}

	private boolean allOtherBadgeConditionsMet(Identity recipient, boolean[] courseBadgeConditionChecked) {
		OpenBadgesManager openBadgesManager = null;
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof OtherBadgeEarnedCondition otherBadgeCondition) {
				if (log.isDebugEnabled()) {
					log.debug("allOtherBadgeConditionsMet(): check OtherBadgeEarnedCondition");
				}
				if (openBadgesManager == null) {
					openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);
				}
				if (!openBadgesManager.hasBadgeAssertion(recipient, otherBadgeCondition.getBadgeClassUuid())) {
					if (log.isDebugEnabled()) {
						log.debug("condition depending on badge {} not met.", otherBadgeCondition.getBadgeClassUuid());
					}
					return false;
				}
				if (log.isDebugEnabled()) {
					log.debug("condition depending on badge {} met.", otherBadgeCondition.getBadgeClassUuid());
				}
				courseBadgeConditionChecked[0] = true;
			}
		}
		return true;
	}

	/**
	 * Check if all global badge conditions for the BadgeCriteria a global badge are met. This is a check for one
	 * potential badge recipient.
	 *
	 * @param recipient         The potential badge recipient.
	 * @param assessmentEntries List of assessment entries (this call is currently only interested in root entries)
	 *                          for the potential recipient
	 *
	 * @return true if all global badge conditions are met for this recipient
	 * 		   (or if there are no global badge conditions to be met), false if at least one condition is not met.
	 */
	private boolean allGlobalBadgeConditionsMet(Identity recipient, List<AssessmentEntry> assessmentEntries) {
		boolean globalBadgeConditionChecked = false;
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof GlobalBadgesEarnedCondition globalBadgesEarnedCondition) {
				if (log.isDebugEnabled()) {
					log.debug("allGlobalBadgeConditionsMet(): check GlobalBadgesEarnedCondition");
				}
				if (!globalBadgesEarnedConditionMet(recipient, globalBadgesEarnedCondition)) {
					if (log.isDebugEnabled()) {
						log.debug("condition depending on badges {} not met.", globalBadgesEarnedCondition
								.getBadgeClassKeys().stream().map(l -> Long.toString(l))
								.collect(Collectors.joining(",")));
					}
					return false;
				}
				if (log.isDebugEnabled()) {
					log.debug("condition depending on badges {} met with flying colors.", globalBadgesEarnedCondition
							.getBadgeClassKeys().stream().map(l -> Long.toString(l))
							.collect(Collectors.joining(",")));
				}
				globalBadgeConditionChecked = true;
			}
			if (badgeCondition instanceof CoursesPassedCondition coursesPassedCondition) {
				if (!coursesPassedConditionMet(recipient, assessmentEntries, coursesPassedCondition)) {
					return false;
				}
				globalBadgeConditionChecked = true;
			}
		}
		if (!globalBadgeConditionChecked) {
			return false;
		}
		return true;
	}

	private boolean globalBadgesEarnedConditionMet(Identity recipient, GlobalBadgesEarnedCondition globalBadgesEarnedCondition) {
		OpenBadgesManager openBadgesManager = null;
		for (Long badgeClassKey : globalBadgesEarnedCondition.getBadgeClassKeys()) {
			if (openBadgesManager == null) {
				openBadgesManager = CoreSpringFactory.getImpl(OpenBadgesManager.class);
			}
			if (!openBadgesManager.hasBadgeAssertion(recipient, badgeClassKey)) {
				return false;
			}
		}
		return true;
	}

	private boolean coursesPassedConditionMet(Identity recipient, List<AssessmentEntry> assessmentEntries,
											  CoursesPassedCondition coursesPassedCondition) {
		List<AssessmentEntry> rootAssessmentEntriesForRecipient = assessmentEntries;

		if (rootAssessmentEntriesForRecipient == null || rootAssessmentEntriesForRecipient.isEmpty()) {
			AssessmentEntryDAO assessmentEntryDAO = CoreSpringFactory.getImpl(AssessmentEntryDAO.class);
			rootAssessmentEntriesForRecipient = assessmentEntryDAO.loadRootAssessmentEntriesForAssessedIdentity(recipient);
		}

		HashSet<Long> coursesToPass = new HashSet<>(coursesPassedCondition.getCourseRepositoryEntryKeys());
		for (AssessmentEntry assessmentEntry : rootAssessmentEntriesForRecipient) {
			if (assessmentEntry.getEntryRoot() == null || !assessmentEntry.getEntryRoot()) {
				continue;
			}
			if (assessmentEntry.getPassed() != null && assessmentEntry.getPassed()) {
				coursesToPass.remove(assessmentEntry.getRepositoryEntry().getKey());
			}
		}
		return coursesToPass.isEmpty();
	}

	public Set<Long> getGlobalCourseRepositoryEntryKeys() {
		Set<Long> keys = new HashSet<>();
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CoursesPassedCondition coursesPassedCondition) {
				keys.addAll(coursesPassedCondition.getCourseRepositoryEntryKeys());
			}
		}
		return keys;
	}

	public Set<Long> getGlobalBadgeClassKeys() {
		Set<Long> keys = new HashSet<>();
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof GlobalBadgesEarnedCondition globalBadgesEarnedCondition) {
				keys.addAll(globalBadgesEarnedCondition.getBadgeClassKeys());
			}
		}
		return keys;
	}

	public boolean conditionForCourseNodeExists(String courseNodeSubIdent) {
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CompletionCriterionMetCondition completionCriterionCondition) {
				if (completionCriterionCondition.getSubIdent().equals(courseNodeSubIdent)) {
					return true;
				}
			}
			if (badgeCondition instanceof CourseElementPassedCondition courseElementPassedCondition) {
				if (courseElementPassedCondition.getSubIdent().equals(courseNodeSubIdent)) {
					return true;
				}
			}
			if (badgeCondition instanceof CourseElementScoreCondition courseElementScoreCondition) {
				if (courseElementScoreCondition.getSubIdent().equals(courseNodeSubIdent)) {
					return true;
				}
			}
		}
		return false;
	}

	public Set<String> otherBadgeClassUuids() {
		Set<String> uuids = new HashSet<>();
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof OtherBadgeEarnedCondition otherBadgeEarnedCondition) {
				uuids.add(otherBadgeEarnedCondition.getBadgeClassUuid());
			}
		}
		return uuids;
	}

	public void prepareForEntryReset(RepositoryEntry courseEntry) {
		for (BadgeCondition badgeCondition : getConditions()) {
			if (badgeCondition instanceof CourseElementPassedCondition courseElementPassedCondition) {
				courseElementPassedCondition.prepareForEntryReset(courseEntry);
			}
			if (badgeCondition instanceof CourseElementScoreCondition courseElementScoreCondition) {
				courseElementScoreCondition.prepareForEntryReset(courseEntry);
			}
		}
	}
}
