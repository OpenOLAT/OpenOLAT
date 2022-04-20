/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.assessment.manager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.manager.AssessmentEntryDAO;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.scorm.assessment.ScormAssessmentManager;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Calculates it the user has any assessment news for the notification system.
 * Currently this checks for new tests
 * <P>
 * Initial Date: 21-giu-2005 <br>
 * 
 * @author Roberto Bagnoli
 */
@Service
public class AssessmentNotificationsHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(AssessmentNotificationsHandler.class);
	
	private static final String CSS_CLASS_USER_ICON = "o_icon_user";

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentEntryDAO courseNodeAssessmentDao;


	/**
	 * Returns the <code>SubscriptionContext</code> to use for assessment
	 * notification about specified <code>ICourse</code>.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * </ul>
	 * If <code>ident == null</code>, the subscription context is (created and)
	 * returned without authorization control
	 * 
	 * @param ident the identity, if null, no subscription check will be made
	 * @param course
	 * @return the subscription context to use or <code>null</code> if the
	 *         identity associated to the request is not allowed to be notified
	 * @see #canSubscribeForAssessmentNotification(Identity, ICourse)
	 */
	public SubscriptionContext getAssessmentSubscriptionContext(Identity ident, ICourse course) {
		SubscriptionContext sctx = null;
		if (ident == null || canSubscribeForAssessmentNotification(ident, course)) {
			// a subscription context showing to the root node (the course's root
			// node is started when clicking such a notification)
			CourseNode cn = course.getRunStructure().getRootNode();
			Long resourceableId = course.getResourceableId();
			sctx = new SubscriptionContext(CourseModule.ORES_COURSE_ASSESSMENT, resourceableId, cn.getIdent());
		}
		return sctx;
	}

	/**
	 * Shortcut for
	 * <code>getAssessmentSubscriptionContext((Identity) null, course)</code>
	 * 
	 * @param course
	 * @return the AssessmentSubscriptionContext
	 * @see #getAssessmentSubscriptionContext(Identity, ICourse)
	 */
	private SubscriptionContext getAssessmentSubscriptionContext(ICourse course) {
		return getAssessmentSubscriptionContext((Identity) null, course);
	}

	/**
	 * Return <code>PublisherData</code> instance to use for assessment
	 * notification.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * </ul>
	 * 
	 * @param course
	 * @param the business path
	 * @return the publisherdata
	 */
	public PublisherData getAssessmentPublisherData(ICourse course, String businessPath) {
		return new PublisherData(CourseModule.ORES_COURSE_ASSESSMENT, String.valueOf(course.getCourseEnvironment().getCourseResourceableId()), businessPath);
	}

	/**
	 * Signal the <code>NotificationsManager</code> about assessment news
	 * available for a course.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>courseId != null</code>
	 * </ul>
	 * 
	 * @param courseId the resourceable id of the course to signal news about
	 * @param ident the identity to ignore news for
	 */
	public void markPublisherNews(Identity ident, Long courseId) {
		ICourse course = loadCourseFromId(courseId);
		if (course == null) throw new AssertException("course with id " + courseId + " not found!");
		markPublisherNews(ident, course);
	}

	/**
	 * Signal the <code>NotificationsManager</code> about assessment news
	 * available on a course.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * </ul>
	 * 
	 * @param course the course to signal news about
	 * @param ident the identity to ignore news for
	 */
	private void markPublisherNews(Identity ident, ICourse course) {
		SubscriptionContext subsContext = getAssessmentSubscriptionContext(course);
		if (subsContext != null) {
			notificationsManager.markPublisherNews(subsContext, ident, true);
		}
	}

	/**
	 * Assessment notification rights check.<br>
	 * Tests if an <code>Identity</code> can subscribe for assessment
	 * notification for the specified <code>ICourse</code>.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * </ul>
	 * 
	 * @param ident the identity to check rights for. Can be <code>null</code>
	 * @param course the course to check rights against
	 * @return if <code>ident == null</code> this method always returns false;
	 *         otherwise subscriptions rights are met only by course
	 *         administrators and course coaches
	 */
	private boolean canSubscribeForAssessmentNotification(Identity ident, ICourse course) {
		if (ident == null) return false;

		CourseGroupManager grpMan = course.getCourseEnvironment().getCourseGroupManager();
		boolean isLearnResourceManager = organisationService.hasRole(ident, OrganisationRoles.learnresourcemanager);
		return isLearnResourceManager || grpMan.isIdentityCourseAdministrator(ident) || grpMan.isIdentityCourseCoach(ident) || grpMan.hasRight(ident, CourseRights.RIGHT_ASSESSMENT, null);
	}

	/**
	 * Utility method.<br>
	 * Load an instance of <code>ICourse</code> given its numeric resourceable
	 * id
	 */
	private ICourse loadCourseFromId(Long courseId) {
		return CourseFactory.loadCourse(courseId);
	}

	/**
	 * Utility method.<br>
	 * Build (recursively) the list of all test nodes belonging to the specified
	 * <code>ICourse</code>.<br>
	 * The returned <code>List</code> is empty if course has no
	 * AssessableCourseNode. Structure course node are excluded from the list.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * </ul>
	 * <br>
	 * <b>POST CONDITIONS</b>
	 * <ul>
	 * <li> The returned list, if not empty, contains ONLY instances of type
	 * <code>AssessableCourseNode</code>
	 * </ul>
	 */
	private List<CourseNode> getCourseTestNodes(ICourse course) {
		List<CourseNode> assessableNodes = new ArrayList<>();

		Structure courseStruct = course.getRunStructure();
		CourseNode rootNode = courseStruct.getRootNode();

		getCourseTestNodes(new CourseEntryRef(course), rootNode, assessableNodes);

		return assessableNodes;
	}

	/**
	 * Recursive step used by <code>getCourseAssessableNodes(ICourse)</code>.<br>
	 * <br>
	 * <b>PRE CONDITIONS</b>
	 * <ul>
	 * <li> <code>course != null</code>
	 * <li> <code>result != null</code>
	 * </ul>
	 * 
	 * @see #getCourseTestNodes(ICourse)
	 */
	private void getCourseTestNodes(RepositoryEntryRef courseEntry, INode node, List<CourseNode> result) {
		if (node != null) {
			CourseNode courseNode = (CourseNode)node;
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
			if (assessmentConfig.isAssessable() && (Mode.setByNode == assessmentConfig.getScoreMode() || Mode.setByNode == assessmentConfig.getPassedMode())) {
				result.add(courseNode);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				getCourseTestNodes(courseEntry, node.getChildAt(i), result);
			}
		}
	}
	
	private boolean courseStatus(ICourse course) {
		return course != null
				&& course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed();
	}

	@Override
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		if(!NotificationsUpgradeHelper.checkCourse(p)) {
			//course don't exist anymore
			notificationsManager.deactivate(p);
			return notificationsManager.getNoSubscriptionInfo();
		}
		
		try {
			Date latestNews = p.getLatestNewsDate();
			Identity identity = subscriber.getIdentity();

			// do not try to create a subscription info if state is deleted - results in
			// exceptions, course
			// can't be loaded when already deleted
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				Long courseId = Long.valueOf(p.getData());
				final ICourse course = loadCourseFromId(courseId);
				if (courseStatus(course)) {
					// course admins or users with the course right to have full access to
					// the assessment tool will have full access to user tests
					CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
					final boolean hasFullAccess = cgm.isIdentityCourseAdministrator(identity) || cgm.hasRight(identity, CourseRights.RIGHT_ASSESSMENT, null);
					final Set<Identity> coachedUsers = new HashSet<>();
					if (!hasFullAccess) {
						// initialize list of users, only when user has not full access
						List<BusinessGroup> coachedGroups = cgm.getOwnedBusinessGroups(identity);
						List<Identity> coachedIdentities = businessGroupService.getMembers(coachedGroups, GroupRoles.participant.name());
						coachedUsers.addAll(coachedIdentities);
						
						List<CurriculumElement> coachedCurriculumElements = cgm.getCoachedCurriculumElements(identity);
						List<Long> coachedCurriculumElementKeys = coachedCurriculumElements.stream()
								.map(CurriculumElement::getKey).collect(Collectors.toList());
						List<Identity> coachedCurriculumElementIdentities = cgm.getCoachesFromCurriculumElements(coachedCurriculumElementKeys);
						coachedUsers.addAll(coachedCurriculumElementIdentities);
	
						// course coaches
						boolean repoTutor = repositoryService.hasRole(identity, cgm.getCourseEntry(), GroupRoles.coach.name());
						if(repoTutor) {
							List<Identity> courseParticipants = repositoryService.getMembers(cgm.getCourseEntry(), RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
							coachedUsers.addAll(courseParticipants);
						}	
					}

					List<CourseNode> testNodes = getCourseTestNodes(course);
					Translator translator = Util.createPackageTranslator(AssessmentManager.class, locale);
					for (CourseNode test:testNodes) {
						List<AssessmentEntry> assessments = courseNodeAssessmentDao.loadAssessmentEntryBySubIdent(cgm.getCourseEntry(), test.getIdent());
						for(AssessmentEntry assessment:assessments) {
							Date modDate = DateUtils.getLater(assessment.getLastUserModified(), assessment.getLastCoachModified());
							Identity assessedIdentity = assessment.getIdentity();
							if (modDate != null && modDate.after(compareDate) && (hasFullAccess || coachedUsers.contains(assessedIdentity))) {
								BigDecimal score = assessment.getScore();
								if(test instanceof ScormCourseNode) {
									ScormCourseNode scormTest = (ScormCourseNode)test;
									//check if completed or passed
									String status = ScormAssessmentManager.getInstance().getLastLessonStatus(assessedIdentity.getName(), course.getCourseEnvironment(), scormTest);
									if(!"passed".equals(status) && !"completed".equals(status)) {
										continue;
									}	
								}
								
								String desc;
								String type = translator.translate("notifications.entry." + test.getType());
								if(score == null) {
									desc = translator.translate("notifications.entry.attempt", new String[] { test.getShortTitle(),
											NotificationHelper.getFormatedName(assessedIdentity), type });
								} else {
									String scoreStr =AssessmentHelper.getRoundedScore(score);
									desc = translator.translate("notifications.entry", new String[] { test.getShortTitle(),
											NotificationHelper.getFormatedName(assessedIdentity), scoreStr, type });
								}

								String urlToSend = null;
								String businessPath = null;
								if(p.getBusinessPath() != null) {
									businessPath = p.getBusinessPath() + "[Users:0][Node:" + test.getIdent() + "][Identity:" + assessedIdentity.getKey() + "]";
									urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
								}
								
								SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, CSS_CLASS_USER_ICON);
								if(si == null) {
									String title = translator.translate("notifications.header", new String[]{course.getCourseTitle()});
									String css = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(test.getType()).getIconCSSClass();
									si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(title, css), null);
								}
								si.addSubscriptionListItem(subListItem);
							}
						}
					}
				}
			} 
			if(si == null) {
				si = notificationsManager.getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Error while creating assessment notifications", e);
			checkPublisher(p);
			return notificationsManager.getNoSubscriptionInfo();
		}
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if(!NotificationsUpgradeHelper.checkCourse(p)) {
				log.info("deactivating publisher with key; " + p.getKey());
				notificationsManager.deactivate(p);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Long resId = subscriber.getPublisher().getResId();
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(resId);
			Translator trans = Util.createPackageTranslator(AssessmentManager.class, locale);
			return trans.translate("notifications.title", new String[]{ displayName });
		} catch (Exception e) {
			log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	@Override
	public String getType() {
		return "AssessmentManager";
	}
}
