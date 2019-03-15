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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.modules.bc.FileInfo;
import org.olat.core.commons.modules.bc.FolderManager;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.pf.ui.PFRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
/**
*
* Initial date: 05.01.2017<br>
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFNotifications {
	private static final OLog log = Tracing.createLoggerFor(PFNotifications.class);
	
	private final Date compareDate;
	private final Subscriber subscriber;
	
	private final Translator translator;
	
	private final List<SubscriptionListItem> items = new ArrayList<>();
	
	private String displayname;

	private NotificationsManager notificationsManager;
	private PFManager pfManager;
	private UserManager userManager;

	public PFNotifications(Subscriber subscriber, Locale locale, Date compareDate, PFManager pfManager, 
			NotificationsManager notificationsManager,UserManager userManager) {
		this.subscriber = subscriber;
		this.compareDate = compareDate;
		this.notificationsManager = notificationsManager;
		this.pfManager = pfManager;
		this.userManager = userManager;
		translator = Util.createPackageTranslator(PFRunController.class, locale);
	}
	
	public List<SubscriptionListItem> getItems() {
		Publisher p = subscriber.getPublisher();
		Identity identity = subscriber.getIdentity();
		ICourse course = CourseFactory.loadCourse(p.getResId());
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		if(!courseEnv.getCourseGroupManager().isNotificationsAllowed()) {
			return Collections.emptyList();
		}
		
		CourseNode node = course.getRunStructure().getNode(p.getSubidentifier());
		Date latestNews = p.getLatestNewsDate();

		if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
			RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			this.displayname = entry.getDisplayname();

			CourseGroupManager groupManager = courseEnv.getCourseGroupManager();
			if (groupManager.isIdentityCourseCoach(identity) || groupManager.isIdentityCourseAdministrator(identity)) {
				List<Identity> participants = pfManager.getParticipants(identity, courseEnv, groupManager.isIdentityCourseAdministrator(identity));

				for (Identity participant : participants) {					
					gatherItems(participant, p, courseEnv, node);
				}
			} else {
				gatherItems(identity, p, courseEnv, node);
			}
		}
		return items;					
	}
	
	private void gatherItems (Identity participant, Publisher p,
			CourseEnvironment courseEnv, CourseNode node) {
		Path folderRoot = Paths.get(courseEnv.getCourseBaseContainer().getRelPath(),
				PFManager.FILENAME_PARTICIPANTFOLDER, node.getIdent(),
				pfManager.getIdFolderName(participant));

		final List<FileInfo> fInfos = FolderManager.getFileInfos(folderRoot.toString(), compareDate);

		SubscriptionListItem subListItem;
		for (Iterator<FileInfo> it_infos = fInfos.iterator(); it_infos.hasNext();) {
			FileInfo fi = it_infos.next();
			VFSMetadata metaInfo = fi.getMetaInfo();
			String filePath = fi.getRelPath();
			Date modDate = fi.getLastModified();
			String action = "upload";
			try {
				Path basepath = courseEnv.getCourseBaseContainer().getBasefile().toPath();
				Path completepath = Paths.get(basepath.toString(), PFManager.FILENAME_PARTICIPANTFOLDER, 
						node.getIdent(), pfManager.getIdFolderName(participant), filePath);
				BasicFileAttributes attrs = Files.readAttributes(completepath, BasicFileAttributes.class);
				if (attrs.creationTime().toMillis() < attrs.lastModifiedTime().toMillis()) {
					action = "modify";
				}
			} catch (IOException ioe) {
				log.error("IOException", ioe);
			}
			String forby = translator.translate("notifications.entry." + 
					(filePath.contains(PFManager.FILENAME_DROPBOX) ? "by" : "for"));

			String userDisplayName = userManager.getUserDisplayName(participant);
			String desc = translator.translate("notifications.entry." + action,
					new String[] { filePath, forby, userDisplayName});
			String businessPath = p.getBusinessPath();
			String urlToSend = BusinessControlFactory.getInstance()
					.getURLFromBusinessPathString(businessPath);

			String iconCssClass = null;
			if (metaInfo != null) {
				iconCssClass = metaInfo.getIconCssClass();
			}
			if (metaInfo != null && !metaInfo.getFilename().startsWith(".")) {
				subListItem = new SubscriptionListItem(desc, urlToSend, businessPath, modDate, iconCssClass);
				items.add(subListItem);
			}
		}
	}

	public String getDisplayname() {
		return displayname;
	}

}
