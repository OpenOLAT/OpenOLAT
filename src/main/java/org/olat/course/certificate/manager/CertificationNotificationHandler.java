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
package org.olat.course.certificate.manager;

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
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificateController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("certificationNotificationHandler")
public class CertificationNotificationHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(CertificationNotificationHandler.class);
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private NotificationsManager notificationsManager;

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
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
			Translator trans = Util.createPackageTranslator(CertificateController.class, locale);

			// do not try to create a subscription info if state is deleted - results in
			// exceptions, course
			// can't be loaded when already deleted
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				Long courseId = Long.valueOf(p.getData());
				final ICourse course = CourseFactory.loadCourse(courseId);
				if (courseStatus(course)) {
					// course admins or users with the course right to have full access to
					// the assessment tool will have full access to user tests
					
					RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
					List<Certificate> certificates = certificatesManager.getCertificatesForNotifications(identity, entry, latestNews);
					for(Certificate certificate:certificates) {
						Date modDate = certificate.getCreationDate();
						Identity assessedIdentity = certificate.getIdentity();
						String fullname = userManager.getUserDisplayName(assessedIdentity);
						String desc = trans.translate("notifications.desc", new String[]{ fullname });

						String urlToSend = null;
						String businessPath = null;
						if(p.getBusinessPath() != null) {
							businessPath = p.getBusinessPath() + "[assessmentTool:0][Identity:" + assessedIdentity.getKey() + "]";
							urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
						}
						
						SubscriptionListItem subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, "o_icon_certificate");
						if(si == null) {
							String title = trans.translate("notifications.header", new String[]{ course.getCourseTitle() });
							si = new SubscriptionInfo(subscriber.getKey(), p.getType(), new TitleItem(title, "o_icon_certificate"), null);
						}
						si.addSubscriptionListItem(subListItem);
					}
				}
			} 
			if(si == null) {
				si = notificationsManager.getNoSubscriptionInfo();
			}
			return si;
		} catch (Exception e) {
			log.error("Error while creating assessment notifications", e);
			return notificationsManager.getNoSubscriptionInfo();
		}
	}
	
	private boolean courseStatus(ICourse course) {
		return course != null
				&& course.getCourseEnvironment().getCourseGroupManager().isNotificationsAllowed();
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		try {
			Long resId = subscriber.getPublisher().getResId();
			String displayName = repositoryManager.lookupDisplayNameByOLATResourceableId(resId);
			Translator trans = Util.createPackageTranslator(CertificateController.class, locale);
			return trans.translate("notifications.title", new String[]{ displayName });
		} catch (Exception e) {
			log.error("Error while creating certificate notifications for subscriber: " + subscriber.getKey(), e);
			return "-";
		}
	}

	@Override
	public String getType() {
		return CertificatesManager.ORES_CERTIFICATE;
	}
}
