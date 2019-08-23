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
package org.olat.course.nodes.pf.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.manager.NotificationsUpgradeHelper;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.pf.ui.PFRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
*
* Initial date: 05.01.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
@Service
public class PFNotificationsHandler implements NotificationsHandler {

	private static final Logger log = Tracing.createLoggerFor(PFNotificationsHandler.class);
	protected static final String CSS_CLASS_ICON = "o_pf_icon";

	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired 
	private PFManager pfManager;
	@Autowired
	private UserManager userManager;

	public PFNotificationsHandler() {

	}
	
	protected static SubscriptionContext getSubscriptionContext(CourseEnvironment courseEnv, CourseNode node) {
		  return CourseModule.createSubscriptionContext(courseEnv, node, node.getIdent());
		}

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		SubscriptionInfo si = null;
		Publisher p = subscriber.getPublisher();

		try {
		 	final Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
			
		 	PFNotifications notifications = new PFNotifications(subscriber, locale, compareDate, 
		 			pfManager, notificationsManager, userManager);
		 	List<SubscriptionListItem> items = notifications.getItems();
			
			if (items.isEmpty()) {
				si = notificationsManager.getNoSubscriptionInfo();
			} else {
				String displayName = notifications.getDisplayname();
				String title = translator.translate("notifications.header", new String[]{ displayName });
				si = new SubscriptionInfo(subscriber.getKey(), p.getType(),	new TitleItem(title, CSS_CLASS_ICON), items);				
			}

		} catch (Exception e) {
			log.error("Unknown Exception", e);
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	private void checkPublisher(Publisher p) {
		try {
			if("BusinessGroup".equals(p.getResName())) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(p.getResId());
				if(bg == null) {
					log.info("deactivating publisher with key; " + p.getKey());
					NotificationsManager.getInstance().deactivate(p);
				}
			} else if ("CourseModule".equals(p.getResName())) {
				if(!NotificationsUpgradeHelper.checkCourse(p)) {
					log.info("deactivating publisher with key; " + p.getKey());
					NotificationsManager.getInstance().deactivate(p);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private TitleItem getTitleItem(Publisher p, Translator translator) {
		String title;
		try {
			String resName = p.getResName();
			if("BusinessGroup".equals(resName)) {
				BusinessGroup bg = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(p.getResId());
				title = translator.translate("notifications.header.group", new String[]{bg.getName()});
			} else if("CourseModule".equals(resName)) {
				String displayName = RepositoryManager.getInstance().lookupDisplayNameByOLATResourceableId(p.getResId());
				CourseNode node = CourseFactory.loadCourse(p.getResId()).getRunStructure().getNode(p.getSubidentifier());
				String shortName = (node != null ? node.getShortName() : "");
				title = translator.translate("notifications.header.course", new String[]{displayName, shortName});
			} else {
				title = translator.translate("notifications.header");
			}
		} catch (Exception e) {
			log.error("", e);
			checkPublisher(p);
			title = translator.translate("notifications.header");
		}
		return new TitleItem(title, CSSHelper.CSS_CLASS_FILETYPE_FOLDER);
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		Translator translator = Util.createPackageTranslator(PFRunController.class, locale);
		TitleItem title = getTitleItem(subscriber.getPublisher(), translator);
		return title.getInfoContent("text/plain");
	}

	@Override
	public String getType() {
		return "PFCourseNode";
	}

}
