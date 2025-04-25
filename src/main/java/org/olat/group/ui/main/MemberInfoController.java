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
package org.olat.group.ui.main;

import java.util.Date;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserPortraitService;
import org.olat.user.UserPropertiesInfoController;
import org.olat.user.UserPropertiesInfoController.Builder;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberInfoController extends FormBasicController {
	
	private FormLink homeLink;
	private FormLink contactLink;
	private FormLink assessmentLink;
	private StaticTextElement membershipCreationEl;

	private final Identity identity;
	private final Roles identityRoles;
	private Long repoEntryKey;
	private UserCourseInformations courseInfos;
	private BusinessGroupMembershipInfos businessGroupInfos;
	
	private final boolean withLinks;
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserCourseInformationsManager userCourseInfosMgr;
	
	public MemberInfoController(UserRequest ureq, WindowControl wControl, Identity identity,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean withLinks) {
		super(ureq, wControl, "info_member");
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), getTranslator()));

		this.identity = identity;
		this.withLinks = withLinks;
		identityRoles = securityManager.getRoles(identity);
		
		if(repoEntry != null) {
			repoEntryKey = repoEntry.getKey();
			courseInfos = userCourseInfosMgr.getUserCourseInformations(repoEntry.getOlatResource(), identity);
		} else if(businessGroup != null) {
			businessGroupInfos = businessGroupService.getMembershipInfos(businessGroup, identity);
		}
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Formatter formatter = Formatter.getInstance(getLocale());
		Builder lvBuilder = UserPropertiesInfoController.LabelValues.builder();
		
		String typei18n;
		if(identityRoles.isInvitee()) {
			typei18n = "user.type.invitee";
		} else if(identityRoles.isGuestOnly()) {
			typei18n = "user.type.guest";
		} else {
			typei18n = "user.type.user";
		}
		lvBuilder.add(translate("user.type"), translate(typei18n));
		
		//course informations
		if(courseInfos != null) {
			String firstTime = formatter.formatDate(courseInfos.getInitialLaunch());
			lvBuilder.add(translate("course.membership.creation"), firstTime);
			
		} else if(businessGroupInfos != null) {
			String creation = formatter.formatDate(businessGroupInfos.creationDate());
			lvBuilder.add(translate("group.membership.creation"), creation);
		}
		
		if(securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles())) {
			if(courseInfos != null) {
				String lastVisit = "";
				String numOfVisits = "0";
				if(courseInfos.getRecentLaunch() != null) {
					lastVisit = formatter.formatDate(courseInfos.getRecentLaunch());
				}
				if(courseInfos.getVisit() >= 0) {
					numOfVisits = Integer.toString(courseInfos.getVisit());
				}
				lvBuilder.add(translate("course.lastTime"), lastVisit);
				lvBuilder.add(translate("course.numOfVisits"), numOfVisits);
			} else if(businessGroupInfos != null) {
				String lastVisit = formatter.formatDate(businessGroupInfos.lastModified());
				lvBuilder.add(translate("course.lastTime"), lastVisit);
			}
		}
		
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		if (withLinks) {
			profileConfig.setUserManagementLinkEnabled(false);
		}
		UserPropertiesInfoController userInfoCtrl = new UserPropertiesInfoController(ureq, getWindowControl(), mainForm,
				identity, getClass().getCanonicalName(), lvBuilder.build(), profileConfig);
		listenTo(userInfoCtrl);
		formLayout.add("userInfo", userInfoCtrl.getInitialFormItem());
		
		//links
		if(withLinks) {
			homeLink = uifactory.addFormLink("home", formLayout, Link.BUTTON);
			homeLink.setIconLeftCSS("o_icon o_icon_home");
			formLayout.add("home", homeLink);
			contactLink = uifactory.addFormLink("contact", formLayout, Link.BUTTON);
			contactLink.setIconLeftCSS("o_icon o_icon_mail");
			formLayout.add("contact", contactLink);
			
			if(repoEntryKey != null) {
				assessmentLink = uifactory.addFormLink("assessment", formLayout, Link.BUTTON);
				assessmentLink.setIconLeftCSS("o_icon o_icon_certificate");
				formLayout.add("assessment", assessmentLink);
			}
		}
	}
	
	public void setMembershipCreation(Date date) {
		if(date != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			membershipCreationEl.setValue(formatter.formatDate(date));
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == homeLink) {
			String businessPath = "[Identity:" + identity.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if (source == contactLink) {
			String businessPath = "[Identity:" + identity.getKey() + "][Contact:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else if (source == assessmentLink) {
			String businessPath =  "[RepositoryEntry:" + repoEntryKey + "][assessmentTool:0][Identity:" + identity.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
		}
	}
}