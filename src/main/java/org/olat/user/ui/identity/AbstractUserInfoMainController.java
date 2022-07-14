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
package org.olat.user.ui.identity;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.commons.calendar.ui.WeeklyCalendarController;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.modules.co.ContactFormController;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageDisplayController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractUserInfoMainController extends BasicController {
	
	protected static final String CMD_HOMEPAGE = "homepage";
	protected static final String CMD_CALENDAR = "calendar";
	protected static final String CMD_FOLDER = "userfolder";
	protected static final String CMD_CONTACT = "contact";
	
	protected final Identity chosenIdentity;
	protected final boolean isInvitee;
	protected final boolean isDeleted;
	
	private FolderRunController folderRunController;
	private WeeklyCalendarController calendarController;
	private ContactFormController contactFormController;
	private HomePageDisplayController homePageDisplayController;

	@Autowired
	protected UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	protected CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private HomePageConfigManager homePageConfigManager;
	
	public AbstractUserInfoMainController(UserRequest ureq, WindowControl wControl, Identity chosenIdentity) {
		super(ureq, wControl);
		this.chosenIdentity = chosenIdentity;
		
		isInvitee = securityManager.getRoles(chosenIdentity).isInviteeOnly();
		isDeleted = chosenIdentity.getStatus().equals(Identity.STATUS_DELETED);
	}
	
	
	protected HomePageDisplayController doOpenHomepage(UserRequest ureq) {
		removeAsListenerAndDispose(homePageDisplayController);
		
		HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(chosenIdentity);
		removeAsListenerAndDispose(homePageDisplayController);
		homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), chosenIdentity, homePageConfig);
		listenTo(homePageDisplayController);
		return homePageDisplayController;
	}
	
	protected WeeklyCalendarController doOpenCalendar(UserRequest ureq) {
		removeAsListenerAndDispose(calendarController);
		
		KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(chosenIdentity);
		CalendarUserConfiguration config = calendarManager.findCalendarConfigForIdentity(calendarWrapper.getKalendar(), getIdentity());
		if (config != null) {
			calendarWrapper.setConfiguration(config);
		}
		
		calendarWrapper.setPrivateEventsVisible(chosenIdentity.equals(ureq.getIdentity()));
		if (chosenIdentity.equals(ureq.getIdentity())) {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
		} else {
			calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
		}
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		calendars.add(calendarWrapper);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType(CMD_CALENDAR);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		OLATResourceable callerOres = OresHelper.createOLATResourceableInstance(chosenIdentity.getName(), chosenIdentity.getKey());
		calendarController = new WeeklyCalendarController(ureq, bwControl, calendars,
				CalendarController.CALLER_PROFILE, callerOres, false);
		listenTo(calendarController);
		return calendarController;
	}
	
	protected FolderRunController doOpenFolder(UserRequest ureq) {
		removeAsListenerAndDispose(folderRunController);

		String chosenUserFolderRelPath = FolderConfig.getUserHome(chosenIdentity) + "/public";
		
		String fullName = userManager.getUserDisplayName(chosenIdentity);
		VFSContainer rootFolder = VFSManager.olatRootContainer(chosenUserFolderRelPath, null);
		VFSContainer namedFolder = new NamedContainerImpl(fullName, rootFolder);
		
		//decided in plenum to have read only view in the personal visiting card, even for admin
		VFSSecurityCallback secCallback = new ReadOnlyCallback();
		namedFolder.setLocalSecurityCallback(secCallback);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType("userfolder");
		WindowControl bwControl = addToHistory(ureq, ores, null);
		folderRunController = new FolderRunController(namedFolder, false, true, false, ureq, bwControl);
		folderRunController.setResourceURL("[Identity:" + chosenIdentity.getKey() + "][userfolder:0]");
		listenTo(folderRunController);
		return folderRunController;
	}
	
	protected ContactFormController doOpenContact(UserRequest ureq) {
		removeAsListenerAndDispose(contactFormController);
		
		ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
		String fullName = userManager.getUserDisplayName(chosenIdentity);
		ContactList emailList = new ContactList(fullName);
		emailList.add(chosenIdentity);
		cmsg.addEmailTo(emailList);
		
		OLATResourceable ores = OresHelper.createOLATResourceableType(CMD_CONTACT);
		WindowControl bwControl = addToHistory(ureq, ores, null);
		contactFormController = new ContactFormController(ureq, bwControl, true, false, false, cmsg);
		listenTo(contactFormController);
		return contactFormController;
	}

}
