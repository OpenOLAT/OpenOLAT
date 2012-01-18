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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.PublisherData;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionContext;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.notifications.items.TitleItem;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.scorm.assessment.ScormAssessmentManager;
import org.olat.notifications.NotificationsUpgradeHelper;
import org.olat.properties.Property;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Calculates it the user has any assessment news for the notification system.
 * Currently this checks for new tests
 * <P>
 * Initial Date: 21-giu-2005 <br>
 * 
 * @author Roberto Bagnoli
 */
public class AssessmentNotificationsHandler implements NotificationsHandler {
	private static final OLog log = Tracing.createLoggerFor(AssessmentNotificationsHandler.class);
	
	private static final String CSS_CLASS_USER_ICON = "o_icon_user";
	//private static final String CSS_CLASSS_IQTEST_ICON = "o_iqtest_icon";
	private static AssessmentNotificationsHandler INSTANCE;
	private Map<Long,SubscriptionContext> subsContexts = new HashMap<Long,SubscriptionContext>();

	/**
	 * Don't use this! Always use the getInstance() method. This is only used once by Spring!
	 */
	public AssessmentNotificationsHandler() {
		INSTANCE = this;
	}

	/**
	 * @return the singleton instance
	 */
	public static AssessmentNotificationsHandler getInstance() {
		return INSTANCE;
	}

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
	protected SubscriptionContext getAssessmentSubscriptionContext(Identity ident, ICourse course) {
		SubscriptionContext sctx = null;

		if (ident == null || canSubscribeForAssessmentNotification(ident, course)) {
			// Creates a new SubscriptionContext only if not found into cache
			Long courseId = course.getResourceableId();
			synchronized (subsContexts) { //o_clusterOK by:ld - no problem to have independent subsContexts caches for each cluster node
				sctx = subsContexts.get(courseId);
				if (sctx == null) {
					// a subscription context showing to the root node (the course's root
					// node is started when clicking such a notification)
					CourseNode cn = course.getRunStructure().getRootNode();
					CourseEnvironment ce = course.getCourseEnvironment();
					//FIXME:fg:b little problem is that the assessment tool and the course are not "the same" anymore, that is you can open the same course twice in the
					// dynamic tabs by a) klicking e.g. via repo, and b via notifications link to the assementtool
					sctx = new SubscriptionContext(CourseModule.ORES_COURSE_ASSESSMENT, ce.getCourseResourceableId(), cn.getIdent());
					subsContexts.put(courseId, sctx);
				}
			}
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
	 * Signal the <code>NotificationsManagerImpl</code> about assessment news
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
	 * Signal the <code>NotificationsManagerImpl</code> about assessment news
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
			NotificationsManager.getInstance().markPublisherNews(subsContext, ident);
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
		
		boolean isInstitutionalResourceManager = BaseSecurityManager.getInstance().isIdentityInSecurityGroup(ident, BaseSecurityManager.getInstance().findSecurityGroupByName(Constants.GROUP_INST_ORES_MANAGER));
		return isInstitutionalResourceManager || grpMan.isIdentityCourseAdministrator(ident) || grpMan.isIdentityCourseCoach(ident) || grpMan.hasRight(ident, CourseRights.RIGHT_ASSESSMENT);
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
	private List<AssessableCourseNode> getCourseTestNodes(ICourse course) {
		List<AssessableCourseNode> assessableNodes = new ArrayList<AssessableCourseNode>();

		Structure courseStruct = course.getRunStructure();
		CourseNode rootNode = courseStruct.getRootNode();

		getCourseTestNodes(rootNode, assessableNodes);

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
	private void getCourseTestNodes(INode node, List<AssessableCourseNode> result) {
		if (node != null) {
			if (node instanceof AssessableCourseNode && !(node instanceof STCourseNode)) {
				result.add((AssessableCourseNode) node);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				getCourseTestNodes(node.getChildAt(i), result);
			}
		}
	}

	/**
	 * @see org.olat.notifications.NotificationsHandler#createSubscriptionInfo(org.olat.notifications.Subscriber,
	 *      java.util.Locale, java.util.Date)
	 */
	public SubscriptionInfo createSubscriptionInfo(final Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();
		if(!NotificationsUpgradeHelper.checkCourse(p)) {
			//course don't exist anymore
			NotificationsManager.getInstance().deactivate(p);
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
		
		try {
			Date latestNews = p.getLatestNewsDate();
			Identity identity = subscriber.getIdentity();

			// do not try to create a subscription info if state is deleted - results in
			// exceptions, course
			// can't be loaded when already deleted
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				Long courseId = new Long(p.getData());
				final ICourse course = loadCourseFromId(courseId);
				if (course != null) {
					// course admins or users with the course right to have full access to
					// the assessment tool will have full access to user tests
					CourseGroupManager cgm = course.getCourseEnvironment().getCourseGroupManager();
					final boolean hasFullAccess = (cgm.isIdentityCourseAdministrator(identity) ? true : cgm.hasRight(identity,
							CourseRights.RIGHT_ASSESSMENT));
					final List<Identity> coachedUsers = new ArrayList<Identity>();
					if (!hasFullAccess) {
						// initialize list of users, only when user has not full access
						List<BusinessGroup> coachedGroups = cgm.getOwnedLearningGroupsFromAllContexts(identity);
						BaseSecurity securityManager = BaseSecurityManager.getInstance();
						for (Iterator<BusinessGroup> iter = coachedGroups.iterator(); iter.hasNext();) {
							BusinessGroup group = iter.next();
							coachedUsers.addAll(securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup()));
						}
					}

					List<AssessableCourseNode> testNodes = getCourseTestNodes(course);
					Translator translator = Util.createPackageTranslator(AssessmentNotificationsHandler.class, locale);

					
					for (AssessableCourseNode test:testNodes) {
						final CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
						
						List<Property> scoreProperties = cpm.listCourseNodeProperties(test, null, null, AssessmentManager.SCORE);
						List<Property> attemptProperties = cpm.listCourseNodeProperties(test, null, null, AssessmentManager.ATTEMPTS);

						for(Property attemptProperty:attemptProperties) {
					  	Date modDate = attemptProperty.getLastModified();
							Identity assessedIdentity = attemptProperty.getIdentity();
					  	if (modDate.after(compareDate) && (hasFullAccess || PersistenceHelper.listContainsObjectByKey(coachedUsers, assessedIdentity))) {
								String score = null;
								for(Property scoreProperty:scoreProperties) {
									if(scoreProperty.getIdentity().equalsByPersistableKey(assessedIdentity) ) {
										score = scoreProperty.getFloatValue().toString();
										break;
									}
								}
								
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
									desc = translator.translate("notifications.entry", new String[] { test.getShortTitle(),
											NotificationHelper.getFormatedName(assessedIdentity), score, type });
								}

								String urlToSend = null;
								if(p.getBusinessPath() != null) {
									String businessPath = p.getBusinessPath() + "[assessmentTool:0][Identity:" + assessedIdentity.getKey() + "][CourseNode:" + test.getIdent() + "]";
									urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
								}
								
								SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, modDate, CSS_CLASS_USER_ICON);
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
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Error while creating assessment notifications", e);
			checkPublisher(p);
			return NotificationsManager.getInstance().getNoSubscriptionInfo();
		}
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if(!NotificationsUpgradeHelper.checkCourse(p)) {
				log.info("deactivating publisher with key; " + p.getKey(), null);
				NotificationsManager.getInstance().deactivate(p);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Long resId = subscriber.getPublisher().getResId();
			String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
			Translator trans = Util.createPackageTranslator(AssessmentNotificationsHandler.class, locale);
			String title = trans.translate("notifications.title", new String[]{ displayName });
			return title;
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
