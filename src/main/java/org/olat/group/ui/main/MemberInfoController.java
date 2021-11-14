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
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberInfoController extends FormBasicController {
	
	private FormLink homeLink, contactLink, assessmentLink;
	private StaticTextElement membershipCreationEl;

	private final Identity identity;
	private Long repoEntryKey;
	private UserCourseInformations courseInfos;
	
	private final boolean withLinks;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserCourseInformationsManager userCourseInfosMgr;
	
	public MemberInfoController(UserRequest ureq, WindowControl wControl, Identity identity,
			RepositoryEntry repoEntry, boolean withLinks) {
		super(ureq, wControl, "info_member");
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), getTranslator()));

		this.identity = identity;
		this.withLinks = withLinks;
		
		if(repoEntry != null){
			repoEntryKey = repoEntry.getKey();
			courseInfos = userCourseInfosMgr.getUserCourseInformations(repoEntry.getOlatResource(), identity);
		}
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
			Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), identity, true, false);
			listenTo(dpc); // auto dispose
			layoutCont.put("image", dpc.getInitialComponent());
			layoutCont.contextPut("fullname", StringHelper.escapeHtml(userManager.getUserDisplayName(identity)));
		}
		
		//user properties
		FormLayoutContainer userPropertiesContainer = FormLayoutContainer.createDefaultFormLayout_6_6("userProperties", getTranslator());
		formLayout.add("userProperties", userPropertiesContainer);
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(getClass().getCanonicalName(), false);
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			String value = userPropertyHandler.getUserProperty(identity.getUser(), getLocale());
			String key = userPropertyHandler.i18nFormElementLabelKey();
			if(value == null) {
				value = "";
			}
			uifactory.addStaticTextElement("up_" + propName, key, value, userPropertiesContainer);
		}

		//course informations
		FormLayoutContainer courseInfosContainer = FormLayoutContainer.createDefaultFormLayout_9_3("courseInfos", getTranslator());
		formLayout.add("courseInfos", courseInfosContainer);
		membershipCreationEl = uifactory.addStaticTextElement("firstTime", "course.membership.creation", "", courseInfosContainer);
		
		if(securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles())) {
			Formatter formatter = Formatter.getInstance(getLocale());
			
			String lastVisit = "";
			String numOfVisits = "0";
			if(courseInfos != null) {
				if(courseInfos.getRecentLaunch() != null) {
					lastVisit = formatter.formatDate(courseInfos.getRecentLaunch());
				}
				if(courseInfos.getVisit() >= 0) {
					numOfVisits = Integer.toString(courseInfos.getVisit());
				}	
			}
			uifactory.addStaticTextElement("lastTime", "course.lastTime", lastVisit, courseInfosContainer);
			uifactory.addStaticTextElement("numOfVisits", "course.numOfVisits", numOfVisits, courseInfosContainer);
		}
		
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