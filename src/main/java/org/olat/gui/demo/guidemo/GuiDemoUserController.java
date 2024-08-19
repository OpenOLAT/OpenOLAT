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
import org.olat.user.UserInfoProfile;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserInfoService;
import org.olat.user.UsersAvatarController;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitLayout;
import org.olat.user.UsersPortraitsComponent.PortraitSize;
import org.olat.user.UsersPortraitsFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoUserController extends BasicController {
	
	@Autowired
	private UserInfoService userInfoService;

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
		identities.add(identity1);
		identities.add(getIdentity());
		TransientIdentity identity2 = new TransientIdentity();
		identity2.setKey(-2l);
		identity2.setProperty(UserConstants.FIRSTNAME, "Đorđe");
		identity2.setProperty(UserConstants.LASTNAME, "Miomir");
		identity2.setProperty(UserConstants.GENDER, "male");
		identities.add(identity2);
		TransientIdentity identity3 = new TransientIdentity();
		identity3.setKey(-3l);
		identity3.setProperty(UserConstants.FIRSTNAME, "Pauline");
		identity3.setProperty(UserConstants.LASTNAME, "Lola");
		identity3.setProperty(UserConstants.EMAIL, "pauline.lola@openolat.com");
		identities.add(identity3);
		
		
		// User info
		UserInfoProfileConfig profileConfig = new UserInfoProfileConfig();
		profileConfig.setChatEnabled(true);
		
		UserInfoProfile profile = userInfoService.createProfile(getIdentity());
		UserInfoProfileController infoProfileCtrl1 = new UserInfoProfileController(ureq, wControl, profileConfig, profile);
		listenTo(infoProfileCtrl1);
		mainVC.put("info-1", infoProfileCtrl1.getInitialComponent());
		
		UserInfoProfile profile3 = userInfoService.createProfile(identity3);
		profile3 = userInfoService.create(profile3.getIdentityKey(), profile3.getUsername(),
				profile3.getDisplayName(), profile3.getInitials(), profile3.getInitialsCss(),
				profile3.isPortraitAvailable(), Presence.dnd);
		GuiDemoUserInfoController infoCtrl2 = new GuiDemoUserInfoController(ureq, getWindowControl(), profileConfig, profile3);
		listenTo(infoCtrl2);
		mainVC.put("info-2", infoCtrl2.getInitialComponent());

		
		// User infos
		DisplayPortraitController portrait1Ctrl = new DisplayPortraitController(ureq, getWindowControl(), getIdentity(), true, true, false, true);
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
		
		
		// User portraits
		UsersPortraitsComponent assigneesCmp1 = UsersPortraitsFactory.create(ureq, "user-portraits-1", mainVC);
		assigneesCmp1.setAriaLabel(translate("user.portraits"));
		assigneesCmp1.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp1.setSize(PortraitSize.xsmall);
		assigneesCmp1.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp2 = UsersPortraitsFactory.create(ureq, "user-portraits-2", mainVC);
		assigneesCmp2.setAriaLabel(translate("user.portraits"));
		assigneesCmp2.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp2.setSize(PortraitSize.small);
		assigneesCmp2.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
		
		UsersPortraitsComponent assigneesCmp3 = UsersPortraitsFactory.create(ureq, "user-portraits-3", mainVC);
		assigneesCmp3.setAriaLabel(translate("user.portraits"));
		assigneesCmp3.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp3.setSize(PortraitSize.medium);
		assigneesCmp3.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp4 = UsersPortraitsFactory.create(ureq, "user-portraits-4", mainVC);
		assigneesCmp4.setAriaLabel(translate("user.portraits"));
		assigneesCmp4.setLPortraitLayout(PortraitLayout.verticalPortraitsDisplayName);
		assigneesCmp4.setSize(PortraitSize.large);
		assigneesCmp4.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp5 = UsersPortraitsFactory.create(ureq, "user-portraits-5", mainVC);
		assigneesCmp5.setAriaLabel(translate("user.portraits"));
		assigneesCmp5.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp5.setSize(PortraitSize.xsmall);
		assigneesCmp5.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp6 = UsersPortraitsFactory.create(ureq, "user-portraits-6", mainVC);
		assigneesCmp6.setAriaLabel(translate("user.portraits"));
		assigneesCmp6.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp6.setSize(PortraitSize.small);
		assigneesCmp6.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp7 = UsersPortraitsFactory.create(ureq, "user-portraits-7", mainVC);
		assigneesCmp7.setAriaLabel(translate("user.portraits"));
		assigneesCmp7.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp7.setSize(PortraitSize.medium);
		assigneesCmp7.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp8 = UsersPortraitsFactory.create(ureq, "user-portraits-8", mainVC);
		assigneesCmp8.setAriaLabel(translate("user.portraits"));
		assigneesCmp8.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp8.setSize(PortraitSize.large);
		assigneesCmp8.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	
		UsersPortraitsComponent assigneesCmp9 = UsersPortraitsFactory.create(ureq, "user-portraits-9", mainVC);
		assigneesCmp9.setAriaLabel(translate("user.portraits"));
		assigneesCmp9.setLPortraitLayout(PortraitLayout.overlappingPortraits);
		assigneesCmp9.setSize(PortraitSize.medium);
		assigneesCmp9.setMaxUsersVisible(2);
		assigneesCmp9.setUsers(UsersPortraitsFactory.createPortraitUsers(identities));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
