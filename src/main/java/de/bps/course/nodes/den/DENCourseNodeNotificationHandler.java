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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryManager;

import de.bps.course.nodes.DENCourseNode;

/**
 * 
 * Description:<br>
 * Notification handler for date enrollment
 * 
 * <P>
 * Initial Date: 25.08.2008 <br>
 * 
 * @author bja
 */
public class DENCourseNodeNotificationHandler implements NotificationsHandler {
	private static final Logger log = Tracing.createLoggerFor(DENCourseNodeNotificationHandler.class);

	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();

		Date latestNews = p.getLatestNewsDate();

		// do not try to create a subscription info if state is deleted - results in
		// exceptions, course
		// can't be loaded when already deleted
		try {
			if (NotificationsManager.getInstance().isPublisherValid(p) && compareDate.before(latestNews)) {
				Long courseId = new Long(p.getData());
				final ICourse course = loadCourseFromId(courseId);
				if (courseStatus(course)) {
					final List<DENCourseNode> denNodes = getCourseDENNodes(course);
					final Translator trans = Util.createPackageTranslator(DENCourseNodeNotificationHandler.class, locale);

					String cssClass = new DENCourseNodeConfiguration().getIconCSSClass();
					si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(trans.translate("notifications.header", new String[]{course.getCourseTitle()}), cssClass), null);
					SubscriptionListItem subListItem;

					for (DENCourseNode denNode : denNodes) {
						String changer = "";
						String desc = trans.translate("notifications.entry", new String[] { denNode.getLongTitle(), changer });

						Date modDate = new Date();
						subListItem = new SubscriptionListItem(desc, null, null, modDate, cssClass);
						si.addSubscriptionListItem(subListItem);
					}
				}
			} else {
				si = NotificationsManager.getInstance().getNoSubscriptionInfo();
			}
		} catch (Exception e) {
			log.error("Error creating enrollment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(p);
			si = NotificationsManager.getInstance().getNoSubscriptionInfo();
		}

		return si;
	}
	
	private boolean courseStatus(ICourse course) {
		return course != null
				&& !course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed();
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if(!NotificationsUpgradeHelper.checkCourse(p)) {
				log.info("deactivating publisher with key; " + p.getKey());
				NotificationsManager.getInstance().deactivate(p);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 
	 * @param courseId
	 * @return
	 */
	private ICourse loadCourseFromId(Long courseId) {
		return CourseFactory.loadCourse(courseId);
	}

	/**
	 * 
	 * @param course
	 * @return
	 */
	private List<DENCourseNode> getCourseDENNodes(ICourse course) {
		List<DENCourseNode> denNodes = new ArrayList<DENCourseNode>(10);

		Structure courseStruct = course.getRunStructure();
		CourseNode rootNode = courseStruct.getRootNode();

		getCourseDENNodes(rootNode, denNodes);
		return denNodes;
	}

	/**
	 * 
	 * @param node
	 * @param result
	 */
	private void getCourseDENNodes(INode node, List<DENCourseNode> result) {
		if (node != null) {
			if (node instanceof DENCourseNode) result.add((DENCourseNode) node);

			for (int i = 0; i < node.getChildCount(); i++) {
				getCourseDENNodes(node.getChildAt(i), result);
			}
		}
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Long resId = subscriber.getPublisher().getResId();
			String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(resId);
			Translator trans = Util.createPackageTranslator(DENCourseNodeNotificationHandler.class, locale);
			return trans.translate("notifications.header", new String[]{displayName});
		} catch (Exception e) {
			log.error("Error while creating assessment notifications for subscriber: " + subscriber.getKey(), e);
			checkPublisher(subscriber.getPublisher());
			return "-";
		}
	}

	public String getType() {
		return "DENCourseNode";
	}
}
