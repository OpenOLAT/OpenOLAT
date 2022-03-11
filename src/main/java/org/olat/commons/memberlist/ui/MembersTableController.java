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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.group.ui.main.MemberListTableModel;
import org.olat.group.ui.main.MemberListTableModel.Cols;
import org.olat.group.ui.main.MemberRow;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 30.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class MembersTableController extends FormBasicController {

	protected FlexiTableElement membersTable;
	protected MemberListTableModel membersModel;
	
	
	private ContactFormController emailController;
	private CloseableModalController cmc;

	private final AtomicInteger counter = new AtomicInteger();
	
	private final boolean chatEnabled, canEmail, deduplicateList, editable, userLastTimeVisible;
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<MemberRow> membersList;
	private final RepositoryEntry repoEntry; 
	private Set<MemberRow> duplicateCatcher;
	private final Map<Long,CurriculumMemberInfos> curriculumInfos;

	@Autowired
	private UserManager userManager;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private BaseSecurity securityManager;
	
	private BusinessGroup businessGroup;
	private CourseEnvironment courseEnv;
	
	private int pageSize = 20;
	private boolean curriculum;
	
	public MembersTableController(UserRequest ureq, WindowControl wControl, List<Identity> members, Set<MemberRow> duplicateCatcher,
			Map<Long,Date> recentLaunches, Map<Long,Date> initialLaunches, Map<Long,CurriculumMemberInfos> curriculumInfos,
			List<UserPropertyHandler> userPropertyHandlers, Map<Long,BusinessGroupMembership> groupmemberships, RepositoryEntry repoEntry, BusinessGroup businessGroup, 
			CourseEnvironment courseEnv, boolean deduplicateList, Translator translator, boolean editable, boolean canEmail, boolean userLastTimeVisible) {
		super(ureq, wControl, "table");
		setTranslator(translator);
		
		chatEnabled = imModule.isEnabled() && imModule.isPrivateEnabled();
		
		this.userPropertyHandlers = userPropertyHandlers;
		this.duplicateCatcher = duplicateCatcher;
		this.repoEntry = repoEntry;
		this.deduplicateList = deduplicateList;
		this.editable = editable;
		this.canEmail = canEmail;
		this.userLastTimeVisible = userLastTimeVisible;
		
		this.businessGroup = businessGroup;
		this.courseEnv = courseEnv;
		this.curriculumInfos = curriculumInfos;
		
		membersList = getMembersFromIdentity(members, groupmemberships, recentLaunches, initialLaunches);
		curriculum = curriculumInfos != null && !curriculumInfos.isEmpty();
	
		initForm(ureq);
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		SortKey defaultSortKey = initColumns(columnsModel);		
		membersModel = new MemberListTableModel(columnsModel, imModule.isOnlineStatusEnabled());
		membersModel.setObjects(membersList);
		membersModel.setCurriculumInfos(curriculumInfos);
		membersTable = uifactory.addTableElement(getWindowControl(), "table", membersModel, pageSize, false, getTranslator(), formLayout);
		membersTable.setEmptyTableSettings("nomembers", null, "o_icon_user");
		membersTable.setAndLoadPersistedPreferences(ureq, this.getClass().getSimpleName());
		membersTable.setExportEnabled(false);
		membersTable.setElementCssClass("o_sel_member_list");
		
		if(defaultSortKey != null) {
			FlexiTableSortOptions options = new FlexiTableSortOptions();
			options.setDefaultOrderBy(defaultSortKey);
			membersTable.setSortSettings(options);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == membersTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if ("vcard".equals(cmd)) {
					MemberRow row = membersModel.getObject(se.getIndex());
					doOpenHomePage(row, ureq);
				} else if ("email".equals(cmd)) {
					MemberRow row = membersModel.getObject(se.getIndex());
					doSendEmailToMember(row, ureq);
				}
			}	
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			MemberRow row = (MemberRow)link.getUserObject();
			if ("im".equals(cmd)) {
				doOpenChat(row, ureq);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == cmc) {
			cleanUp();
		} else if (source == emailController) {
			cmc.deactivate();
			cleanUp();
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(emailController);
		removeAsListenerAndDispose(cmc);
		emailController = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<MemberRow> getMembersFromIdentity(List<Identity> identities, Map<Long,BusinessGroupMembership> groupmemberships,
			Map<Long,Date> recentLaunches, Map<Long,Date> initialLaunches) {
		if (!deduplicateList) {
			duplicateCatcher = new HashSet<>();
		}
		List<MemberRow> memberList = new ArrayList<>();		
		for (Identity identity : identities) {
			MemberRow member = new MemberRow(new MemberView(identity, userPropertyHandlers, getLocale()));
			if (userLastTimeVisible) {
				if (repoEntry == null) {
					BusinessGroupMembership groupmembership = groupmemberships.get(identity.getKey());
					if(groupmembership != null) {
						member.setFirstTime(groupmembership.getCreationDate());
						member.setLastTime(groupmembership.getLastModified());
					}
				} else {
					member.setFirstTime(initialLaunches.get(identity.getKey()));
					member.setLastTime(recentLaunches.get(identity.getKey()));
				}
			}
			if (!duplicateCatcher.contains(member)) {
				memberList.add(member);
				if (!identity.equals(getIdentity())){
					forgeChatLink(member);
				}
			}
			duplicateCatcher.add(member);
		}
		return memberList;
	}
	
	protected void forgeChatLink(MemberRow row) {
		FormLink chatLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "im", "", null, null, Link.NONTRANSLATED);
		chatLink.setIconLeftCSS("o_icon o_icon_status_unavailable");
		chatLink.setUserObject(row);
		row.setChatLink(chatLink);
	}
	
	private SortKey initColumns(FlexiTableColumnModel columnsModel) {
		int colPos = AbstractMemberListController.USER_PROPS_OFFSET;
		SortKey defaultSortKey = null;
		String rowAction = "vcard";
				
		if (chatEnabled && editable) {
			DefaultFlexiColumnModel chatCol = new DefaultFlexiColumnModel(Cols.online.i18n(), Cols.online.ordinal());
			chatCol.setExportable(false);
			columnsModel.addFlexiColumnModel(chatCol);
		}
			
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(MembersDisplayRunController.USER_PROPS_LIST_ID , userPropertyHandler);
			String emailRowAction = rowAction;
			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName) || UserConstants.EMAIL.equals(propName)) {
				// when email is enabled, the action will trigger email workflow
				if (UserConstants.EMAIL.equals(propName) && canEmail) {
					emailRowAction = "email";
				}
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, emailRowAction, true, propName,
						new StaticFlexiCellRenderer(emailRowAction, new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
			
			if(defaultSortKey == null) {
				defaultSortKey = new SortKey(propName, true);
			}
		}
		
		if(curriculum) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.curriculumDisplayName));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Cols.rootCurriculumElementIdentifier));
		}
		
		if (userLastTimeVisible) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.firstTime.i18n(), Cols.firstTime.ordinal(), true, Cols.firstTime.name()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastTime.i18n(), Cols.lastTime.ordinal(), true, Cols.lastTime.name()));
		}
		return defaultSortKey;
	}
	
	private void doSendEmailToMember(MemberRow member, UserRequest ureq) {
		if (!editable) return;
		ContactList memberList;
		Identity identity = securityManager.loadIdentityByKey(member.getIdentityKey());
		String fullName = userManager.getUserDisplayName(identity);
		if (courseEnv == null) {
			memberList = new ContactList(translate("members.to", new String[]{ fullName, businessGroup.getName() }));
		} else {
			memberList = new ContactList(translate("members.to", new String[]{ fullName, courseEnv.getCourseTitle() }));
		}
		memberList.add(identity);
		doSendEmailToMember(memberList, ureq);
	}
	
	private void doSendEmailToMember(ContactList contactList, UserRequest ureq) {
		if (!contactList.getEmailsAsStrings().isEmpty()) {
			removeAsListenerAndDispose(cmc);
			removeAsListenerAndDispose(emailController);
			
			ContactMessage cmsg = new ContactMessage(ureq.getIdentity());
			cmsg.addEmailTo(contactList);
			// preset body template from i18n
			cmsg.setBodyText(createBodyTemplate());
			emailController = new ContactFormController(ureq, getWindowControl(), true, false, false, cmsg);
			listenTo(emailController);
			
			String title = translate("members.email.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), emailController.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private String createBodyTemplate() {
		if (courseEnv == null) {
			String courseName = businessGroup.getName();
			// Build REST URL to business group, use hack via group manager to access
			StringBuilder courseLink = new StringBuilder();
			courseLink.append(Settings.getServerContextPathURI())
				.append("/auth/BusinessGroup/").append(businessGroup.getKey());
			return translate("email.body.template", courseName, courseLink.toString());	
		} else {
			String courseName = courseEnv.getCourseTitle();
			// Build REST URL to course element, use hack via group manager to access repo entry
			StringBuilder courseLink = new StringBuilder();
			RepositoryEntry entry = courseEnv.getCourseGroupManager().getCourseEntry();
			courseLink.append(Settings.getServerContextPathURI())
				.append("/url/RepositoryEntry/").append(entry.getKey());
			return translate("email.body.template", courseName, courseLink.toString());		
		}
	}
	
	private void doOpenHomePage(MemberRow member, UserRequest ureq) {
		String url = "[HomePage:" + member.getIdentityKey() + "]";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(url);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
	
	private void doOpenChat(MemberRow member, UserRequest ureq) {
		Buddy buddy = imService.getBuddyById(member.getIdentityKey());
		OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
	}

}
