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
package org.olat.course.member;

import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberInfoController extends FormBasicController {
	
	private FormLink homeLink, contactLink, assessmentLink;
	private StaticTextElement membershipCreationEl;

	private final Identity identity;
	private final Long repoEntryKey;
	private final UserCourseInformations courseInfos;
	
	private final UserManager userManager;
	private final UserCourseInformationsManager efficiencyStatementManager;
	
	public MemberInfoController(UserRequest ureq, WindowControl wControl, Identity identity,
			RepositoryEntry repoEntry, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "info_member", rootForm);
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, ureq.getLocale(), getTranslator()));
		
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		efficiencyStatementManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
	
		this.identity = identity;
		repoEntryKey = repoEntry.getKey();
		
		courseInfos = efficiencyStatementManager.getUserCourseInformations(repoEntry.getOlatResource().getResourceableId(), identity);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
		
			Controller dpc = new DisplayPortraitController(ureq, getWindowControl(), identity, true, false);
			listenTo(dpc); // auto dispose
			layoutCont.put("image", dpc.getInitialComponent());
			layoutCont.contextPut("fullname", userManager.getUserDisplayName(identity.getUser()));
		}
		
		//user properties
		FormLayoutContainer userPropertiesContainer = FormLayoutContainer.createDefaultFormLayout("userProperties", getTranslator());
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
		FormLayoutContainer courseInfosContainer = FormLayoutContainer.createDefaultFormLayout("courseInfos", getTranslator());
		formLayout.add("courseInfos", courseInfosContainer);
		
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
		membershipCreationEl = uifactory.addStaticTextElement("firstTime", "course.membership.creation", "", courseInfosContainer);
		uifactory.addStaticTextElement("lastTime", "course.lastTime", lastVisit, courseInfosContainer);
		uifactory.addStaticTextElement("numOfVisits", "course.numOfVisits", numOfVisits, courseInfosContainer);
		
		//links
		homeLink = uifactory.addFormLink("home", formLayout, "b_link_left_icon b_link_to_home");
		formLayout.add("home", homeLink);
		contactLink = uifactory.addFormLink("contact",	formLayout, "b_link_left_icon b_link_mail");
		formLayout.add("contact", contactLink);
		assessmentLink = uifactory.addFormLink("assessment",	formLayout, "b_link_left_icon b_link_assessment");
		formLayout.add("assessment", assessmentLink);
	}
	
	public void setMembershipCreation(Date date) {
		if(date != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			membershipCreationEl.setValue(formatter.formatDate(date));
		}
	}
	
	@Override
	protected void doDispose() {
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
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}