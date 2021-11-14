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
package org.olat.commons.memberlist.ui;

import java.util.List;
import java.util.Map;

import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;

/**
 * Initial Date: 28.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class MembersDisplayRunController extends BasicController {
	// User property context ID's used in all the child controllers defined here 
	static final String USER_PROPS_LIST_ID = MembersTableController.class.getName();
	static final String USER_PROPS_PRINT_ID = MembersPrintController.class.getName();
	static final String USER_PROPS_AVATAR_ID = MembersAvatarDisplayRunController.class.getName();

	private static final String GUIPREF_KEY_GROUPMEMBER = "groupmemberdisplay";
	private static final String GUIPREF_KEY_COURSEMEMBER = "coursememberdisplay";
	
	private VelocityContainer mainVC;
	
	private MembersAvatarDisplayRunController membersAvatarController;
	private MembersListDisplayRunController membersListController;
	
	private Link tableCustomLink, tableLink;
	
	private BusinessGroup businessGroup;
	private UserCourseEnvironment userCourseEnv;
	private String courseOrGroupIdentifier;
	
	private List<Identity> owners;
	private List<Identity> coaches;
	private List<Identity> participants;
	private List<Identity> waiting;
	private Map<Long,CurriculumMemberInfos> curriculumInfos;

	private final boolean canEmail;
	private final boolean canDownload;
	private final boolean showOwners;
	private final boolean showCoaches;
	private final boolean showParticipants;
	private final boolean showWaiting;
	private final boolean editable;
	private final boolean deduplicateList;
	
	
	public MembersDisplayRunController(UserRequest ureq, WindowControl wControl, Translator translator, UserCourseEnvironment userCourseEnv, BusinessGroup businessGroup,
			List<Identity> owners, List<Identity> coaches, List<Identity> participants, List<Identity> waiting, Map<Long,CurriculumMemberInfos> curriculumInfos,
			boolean canEmail, boolean canDownload, boolean deduplicateList,
			boolean showOwners, boolean showCoaches, boolean showParticipants, boolean showWaiting, boolean editable) {
		super(ureq, wControl);
		setTranslator(translator);
		this.courseOrGroupIdentifier = userCourseEnv == null ? GUIPREF_KEY_GROUPMEMBER + businessGroup.getKey()
				: GUIPREF_KEY_COURSEMEMBER + userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
		this.businessGroup = businessGroup;
		this.userCourseEnv = userCourseEnv;
		this.curriculumInfos = curriculumInfos;
		// lists
		this.owners = owners;
		this.coaches = coaches;
		this.participants = participants;
		this.waiting = waiting;
		// flags
		this.canEmail = canEmail;
		this.canDownload = canDownload;
		this.showOwners = showOwners;
		this.showCoaches = showCoaches;
		this.showParticipants = showParticipants;
		this.showWaiting = showWaiting;
		this.deduplicateList = deduplicateList;
		this.editable = editable;
		
		mainVC = createVelocityContainer("membersToggle");
		
		mainVC.contextPut("headline", businessGroup != null);
		
		tableCustomLink = LinkFactory.createLink(null, "tableCustomLink", "select.custom", "blank", getTranslator(), mainVC, this, Link.BUTTON);
		tableCustomLink.setIconLeftCSS("o_icon o_icon_table_custom o_icon-lg");
		tableCustomLink.setElementCssClass("o_sel_cmembers_thumbnails_view");
		
		tableLink = LinkFactory.createLink(null, "tableLink", "select.table", "blank", getTranslator(), mainVC, this, Link.BUTTON);
		tableLink.setIconLeftCSS("o_icon o_icon_table o_icon-lg");
		tableLink.setElementCssClass("o_sel_cmembers_table_view");
			
		if (doLoadMemberListConfig(ureq)) {
			doOpenPortraitView(ureq);
		} else { 
			doOpenListView(ureq, true);
		}		
		putInitialPanel(mainVC);		
	}

	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == tableCustomLink) {
			doOpenPortraitView(ureq);
		} else if (source == tableLink) {
			doOpenListView(ureq, true);
		}
	}
	
	private void doOpenPortraitView(UserRequest ureq) {
		if (membersAvatarController == null) {
			membersAvatarController = new MembersAvatarDisplayRunController(ureq, getWindowControl(), getTranslator(), 
					userCourseEnv, businessGroup, owners, coaches, participants, waiting, curriculumInfos,
					canEmail, canDownload, deduplicateList, showOwners, showCoaches, showParticipants, showWaiting, editable);
			listenTo(membersAvatarController);
		}
		mainVC.contextPut("portrait", Boolean.TRUE);
		mainVC.put("portraitView", membersAvatarController.getInitialComponent());
		tableCustomLink.setActive(true);
		tableLink.setActive(false);
		doUpdateMemberListConfig(ureq, true);
	}
	
	private void doOpenListView(UserRequest ureq, boolean onClick) {
		if (membersListController == null) {
			membersListController = new MembersListDisplayRunController(ureq, getWindowControl(), getTranslator(), 
					userCourseEnv, businessGroup, owners, coaches, participants, waiting, curriculumInfos, canEmail, canDownload, deduplicateList,
					showOwners, showCoaches, showParticipants, showWaiting, editable);
			listenTo(membersListController);
		}
		mainVC.put("listView", membersListController.getInitialComponent());
		mainVC.contextPut("portrait", Boolean.FALSE);
		if (onClick) {
			tableCustomLink.setActive(false);
			tableLink.setActive(true);
		}
		doUpdateMemberListConfig(ureq, false);
	}
	
	private boolean doLoadMemberListConfig(UserRequest ureq) {
		Boolean showPortraitConfig = Boolean.TRUE;
		if (ureq != null) {
			Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
			showPortraitConfig  = (Boolean) guiPrefs.get(MembersDisplayRunController.class, courseOrGroupIdentifier);
			if (showPortraitConfig == null) {
				showPortraitConfig = Boolean.TRUE;
			}
		}
		return showPortraitConfig;
	}
	
	private void doUpdateMemberListConfig(UserRequest ureq, boolean newValue) {
		// save new config in GUI prefs
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(MembersDisplayRunController.class, courseOrGroupIdentifier, Boolean.valueOf(newValue));
		}
	}
}
