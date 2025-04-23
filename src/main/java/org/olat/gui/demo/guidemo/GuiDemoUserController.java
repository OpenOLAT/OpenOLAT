/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.gui.demo.guidemo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.olat.admin.user.UserShortDescription;
import org.olat.admin.user.UserShortDescription.Rows;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.DisplayPortraitController;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.UsersAvatarController;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitLayout;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoUserController extends BasicController {
	
	@Autowired
	private UserPortraitService userPortraitService;

	public GuiDemoUserController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("guidemo-users");
		putInitialPanel(mainVC);
		
		List<Identity> identities = new ArrayList<>();
		TransientIdentity identity1 = new TransientIdentity();
		identity1.setKey(-1l);
		identity1.setProperty(UserConstants.FIRSTNAME, "Kristin");
		identity1.setProperty(UserConstants.LASTNAME, "Frøya");
		identity1.setProperty(UserConstants.GENDER, "female");
		identity1.setInitialsCssClass(UserManager.USER_INITIALS_CSS.get(1));
		identities.add(identity1);
		identities.add(getIdentity());
		TransientIdentity identity2 = new TransientIdentity();
		identity2.setKey(-2l);
		identity2.setProperty(UserConstants.FIRSTNAME, "Đorđe");
		identity2.setProperty(UserConstants.LASTNAME, "Miomir");
		identity2.setProperty(UserConstants.GENDER, "male");
		identity2.setInitialsCssClass(UserManager.USER_INITIALS_CSS.get(2));
		identities.add(identity2);
		TransientIdentity identity3 = new TransientIdentity();
		identity3.setKey(-3l);
		identity3.setProperty(UserConstants.FIRSTNAME, "Pauline");
		identity3.setProperty(UserConstants.LASTNAME, "Lola");
		identity3.setProperty(UserConstants.EMAIL, "pauline.lola@openolat.com");
		identity3.setInitialsCssClass(UserManager.USER_INITIALS_CSS.get(3));
		identities.add(identity3);
		
		
		// User info
		UserInfoProfileConfig profileConfig = new UserInfoProfileConfig();
		profileConfig.setChatEnabled(true);
		
		PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), getIdentity());
		UserInfoProfileController infoProfileCtrl1 = new UserInfoProfileController(ureq, wControl, profileConfig, portraitUser);
		listenTo(infoProfileCtrl1);
		mainVC.put("info-1", infoProfileCtrl1.getInitialComponent());
		
		UserInfoProfileController infoProfileCtrl2 = new UserInfoProfileController(ureq, wControl, profileConfig, portraitUser);
		listenTo(infoProfileCtrl2);
		mainVC.put("info-2", infoProfileCtrl2.getInitialComponent());
		
		PortraitUser portraitUser3 = userPortraitService.createPortraitUser(getLocale(), identity1);
		portraitUser3 = userPortraitService.createPortraitUser(portraitUser3.getIdentityKey(),
				portraitUser3.getUsername(), portraitUser3.isPortraitAvailable(), null, portraitUser3.getInitials(),
				portraitUser3.getInitialsCss(), portraitUser3.getDisplayName(), Presence.dnd);
		GuiDemoUserInfoController infoCtrl3 = new GuiDemoUserInfoController(ureq, getWindowControl(), profileConfig, portraitUser3, 1);
		listenTo(infoCtrl3);
		mainVC.put("info-3", infoCtrl3.getInitialComponent());
		
		PortraitUser portraitUser4 = userPortraitService.createPortraitUser(getLocale(), identity2);
		portraitUser4 = userPortraitService.createPortraitUser(portraitUser4.getIdentityKey(),
				portraitUser4.getUsername(), portraitUser4.isPortraitAvailable(), null, portraitUser4.getInitials(),
				portraitUser4.getInitialsCss(), portraitUser4.getDisplayName(), Presence.dnd);
		GuiDemoUserInfoController infoCtrl4 = new GuiDemoUserInfoController(ureq, getWindowControl(), profileConfig, portraitUser4, 4);
		listenTo(infoCtrl4);
		mainVC.put("info-4", infoCtrl4.getInitialComponent());
		
		PortraitUser portraitUser5 = userPortraitService.createPortraitUser(getLocale(), identity3);
		portraitUser5 = userPortraitService.createPortraitUser(portraitUser5.getIdentityKey(),
				portraitUser5.getUsername(), portraitUser5.isPortraitAvailable(), null, portraitUser5.getInitials(),
				portraitUser5.getInitialsCss(), portraitUser5.getDisplayName(), Presence.dnd);
		GuiDemoUserInfoController infoCtrl5 = new GuiDemoUserInfoController(ureq, getWindowControl(), profileConfig, portraitUser5, 100);
		listenTo(infoCtrl5);
		mainVC.put("info-5", infoCtrl5.getInitialComponent());

		
		// User infos
		DisplayPortraitController portrait1Ctrl = new DisplayPortraitController(ureq, getWindowControl(), getIdentity(), PortraitSize.large, true);
		listenTo(portrait1Ctrl);
		mainVC.put("infos-portrait-1", portrait1Ctrl.getInitialComponent());
		Rows additionalRows = Rows.builder()
				.addRow(translate("user.infos.additional.label"), translate("user.infos.additional.value"))
				.build();
		UserShortDescription userShortDescrCtrl = new UserShortDescription(ureq, getWindowControl(), getIdentity(), additionalRows);
		mainVC.put("infos-description-1", userShortDescrCtrl.getInitialComponent());
		listenTo(userShortDescrCtrl);
		
		// User avatars
		UsersAvatarController avatar1Ctrl = new UsersAvatarController(ureq, getWindowControl(), new HashSet<>(identities));
		listenTo(avatar1Ctrl);
		mainVC.put("avatars-1", avatar1Ctrl.getInitialComponent());
		
		
		// User portrait
		UserPortraitComponent userPortrait1 = UserPortraitFactory.createUserPortrait("user-portrait-1", mainVC, getLocale());
		userPortrait1.setSize(PortraitSize.xsmall);
		userPortrait1.setDisplayPresence(true);
		userPortrait1.setPortraitUser(userPortraitService.createPortraitUser(getLocale(), identities.get(0)));
		
		UserPortraitComponent userPortrait2 = UserPortraitFactory.createUserPortrait("user-portrait-2", mainVC, getLocale());
		userPortrait2.setSize(PortraitSize.small);
		userPortrait2.setDisplayPresence(true);

		userPortrait2.setPortraitUser(userPortraitService.createPortraitUser(getLocale(), identities.get(1)));
		
		UserPortraitComponent userPortrait3 = UserPortraitFactory.createUserPortrait("user-portrait-3", mainVC, getLocale());
		userPortrait3.setSize(PortraitSize.medium);
		userPortrait3.setDisplayPresence(true);

		userPortrait3.setPortraitUser(userPortraitService.createPortraitUser(getLocale(), identities.get(2)));
		
		UserPortraitComponent userPortrait4 = UserPortraitFactory.createUserPortrait("user-portrait-4", mainVC, getLocale());
		userPortrait4.setSize(PortraitSize.large);
		userPortrait4.setDisplayPresence(true);

		userPortrait4.setPortraitUser(userPortraitService.createPortraitUser(getLocale(), identities.get(3)));
		
		
		// User portraits
		UsersPortraitsComponent assigneesCmp1 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-1", mainVC);
		assigneesCmp1.setAriaLabel(translate("user.portraits"));
		assigneesCmp1.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp1.setSize(PortraitSize.xsmall);
		assigneesCmp1.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp2 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-2", mainVC);
		assigneesCmp2.setAriaLabel(translate("user.portraits"));
		assigneesCmp2.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp2.setSize(PortraitSize.small);
		assigneesCmp2.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
		
		UsersPortraitsComponent assigneesCmp3 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-3", mainVC);
		assigneesCmp3.setAriaLabel(translate("user.portraits"));
		assigneesCmp3.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp3.setSize(PortraitSize.medium);
		assigneesCmp3.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp4 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-4", mainVC);
		assigneesCmp4.setAriaLabel(translate("user.portraits"));
		assigneesCmp4.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp4.setSize(PortraitSize.large);
		assigneesCmp4.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp5 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-5", mainVC);
		assigneesCmp5.setAriaLabel(translate("user.portraits"));
		assigneesCmp5.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp5.setSize(PortraitSize.xsmall);
		assigneesCmp5.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp6 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-6", mainVC);
		assigneesCmp6.setAriaLabel(translate("user.portraits"));
		assigneesCmp6.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp6.setSize(PortraitSize.small);
		assigneesCmp6.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp7 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-7", mainVC);
		assigneesCmp7.setAriaLabel(translate("user.portraits"));
		assigneesCmp7.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp7.setSize(PortraitSize.medium);
		assigneesCmp7.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp8 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-8", mainVC);
		assigneesCmp8.setAriaLabel(translate("user.portraits"));
		assigneesCmp8.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp8.setSize(PortraitSize.large);
		assigneesCmp8.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
	
		UsersPortraitsComponent assigneesCmp9 = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-9", mainVC);
		assigneesCmp9.setAriaLabel(translate("user.portraits"));
		assigneesCmp9.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp9.setSize(PortraitSize.medium);
		assigneesCmp9.setMaxUsersVisible(2);
		assigneesCmp9.setUsers(userPortraitService.createPortraitUsers(getLocale(), identities));
		
		// Special users
		List<Identity> specialIdentities = new ArrayList<>();
		TransientIdentity deletedIdentity = new TransientIdentity();
		deletedIdentity.setStatus(Identity.STATUS_DELETED);
		specialIdentities.add(deletedIdentity);
		TransientIdentity unknownIdentity = null;
		specialIdentities.add(unknownIdentity);
		List<PortraitUser> specialPortraitUsers = userPortraitService.createPortraitUsers(getLocale(), specialIdentities);
		specialPortraitUsers = new ArrayList<>(specialPortraitUsers);
		specialPortraitUsers.add(userPortraitService.createGuestPortraitUser(getLocale()));
		
		UsersPortraitsComponent specialUserCmp = UserPortraitFactory.createUsersPortraits(ureq, "user-portraits-special", mainVC);
		specialUserCmp.setAriaLabel(translate("user.portraits"));
		specialUserCmp.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		specialUserCmp.setSize(PortraitSize.medium);
		specialUserCmp.setUsers(specialPortraitUsers);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
