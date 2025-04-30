/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.widgets;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.SpeedometerElement;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumDashboardController;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.UsersPortraitsComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersWidgetController extends FormBasicController {
	
	private FormLink membersLink;
	private FormLink minimizeButton;
	private FormLink ownersLink;
	private FormLink coachesLink;
	private FormLink masterCoachesLink;
	private FormLink activeParticipantsLink;
	private FormLink pendingParticipantsLink;
	private SpeedometerElement speedometerEl;

	private int counter = 0;
	private String preferencesId;
	private AtomicBoolean minimized;
	private boolean otherRolesInitialized = false;
	private CurriculumElementInfos curriculumElementInfos;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;
	
	public MembersWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "members_widget", Util
				.createPackageTranslator(CurriculumDashboardController.class, ureq.getLocale()));
		preferencesId = "widget-members-cur-el-" + curriculumElement.getKey();
		curriculumElementInfos = loadInformations(curriculumElement);
		initForm(ureq);
	}
	
	private CurriculumElementInfos loadInformations(CurriculumElement curriculumElement) {
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElements(null, List.of(curriculumElement));
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		if(elements == null) {
			return new CurriculumElementInfos(curriculumElement, null, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l);
		}
		return elements.get(0);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		membersLink = uifactory.addFormLink("curriculum.members", formLayout);
		membersLink.setIconRightCSS("o_icon o_icon_course_next");
		membersLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar_day");
		
		Boolean minimizedObj = (Boolean)ureq.getUserSession()
				.getGuiPreferences()
				.get(LectureBlocksWidgetController.class, preferencesId, Boolean.FALSE);
		minimized = new AtomicBoolean(minimizedObj != null && minimizedObj.booleanValue());
		if(!minimized.get()) {
			initOtherRoles(ureq);
		}
		
		minimizeButton = uifactory.addFormLink("curriculum.minimize", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		minimizeButton.setTitle(translate("curriculum.minimize"));
		minimizeButton.setElementCssClass("o_button_details");
		updateMinimizeButton();
		
		String activeText = getText(translate("num.of.active.participants"), Long.toString(curriculumElementInfos.numOfParticipants()));
		activeParticipantsLink = uifactory.addFormLink("num.of.active.participants", activeText, null, formLayout, Link.LINK | Link.NONTRANSLATED);
		String pendingText = getText(translate("num.of.pending.participants"), Long.toString(curriculumElementInfos.numOfPending()));
		pendingParticipantsLink = uifactory.addFormLink("num.of.pending.participants", pendingText, null, formLayout, Link.LINK | Link.NONTRANSLATED);
		
		speedometerEl = new SpeedometerElement("participantsMeter");
		speedometerEl.setValueCssClass("o_speedometer_infos");
		formLayout.add("participantsMeter", speedometerEl);
		
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			initAvailability(layoutCont);
			layoutCont.contextPut("minimized", minimized);
		}
	}
	
	private void initAvailability(FormLayoutContainer layoutCont) {
		Long max = curriculumElementInfos.curriculumElement().getMaxParticipants();
		Long min = curriculumElementInfos.curriculumElement().getMinParticipants();
				
		String range = CurriculumHelper.getParticipantRange(getTranslator(), curriculumElementInfos.curriculumElement(), false);
			layoutCont.contextPut("minMax", range);

		long num = curriculumElementInfos.numOfParticipants() + curriculumElementInfos.numOfPending();
		speedometerEl.setValue(num);
		if(max != null && max.longValue() > 0l) {
			speedometerEl.setMaxValue(max.doubleValue());
			
			ParticipantsAvailabilityNum participantsAvailabilityNum = acService
					.getParticipantsAvailability(curriculumElementInfos.curriculumElement().getMaxParticipants(), num, true);
			String availability = getAvailabilityText(participantsAvailabilityNum);
			layoutCont.contextPut("availabilityMsg", availability);
			
			String speedometerClass = "o_speedometer_success";
			if(min != null && num < min.longValue()) {
				speedometerClass = "o_speedometer_warning";
			} else if(participantsAvailabilityNum.availability() == ParticipantsAvailability.overbooked) {
				speedometerClass = "o_speedometer_danger";
			}
			speedometerEl.setValueCssClass(speedometerClass);
		} else {
			speedometerEl.setMaxValue(num);
			speedometerEl.setValueCssClass("o_speedometer_success");
			
			String availability = translate(num <= 1 ? "filter.occupancy.status.seat" :"filter.occupancy.status.seats", Long.toString(num));
			layoutCont.contextPut("availabilityMsg", availability);
		}
	}
	
	private String getAvailabilityText(ParticipantsAvailabilityNum availability) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span class='o_curriculum_widget_members_status_").append(availability.availability().name().toLowerCase()).append("'>");
		
		String icon = switch(availability.availability()) {
			case manyLeft -> null;
			case fewLeft, fullyBooked -> "o_icon_warning";
			case overbooked -> "o_icon_circle_exclamation";
		};
		if(icon != null) {
			sb.append("<i class='o_icon o_icon-fw ").append(icon).append("'> </i> ");
		}
		
		String text = switch(availability.availability()) {
			case manyLeft -> translate("filter.occupancy.status.free.num.seats", Long.toString(availability.numAvailable()));
			case fewLeft -> translate("filter.occupancy.status.free.few.num.seats", Long.toString(availability.numAvailable()));
			case fullyBooked -> translate("filter.occupancy.status.fully.booked");
			case overbooked -> translate("filter.occupancy.status.overbooked");
		};
		sb.append(text);
		sb.append("</span>");
		return sb.toString();
	}
	
	private String getText(String label, String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span class=\"o_curriculum_widget_number\">")
		  .append("<label class=\"o_curriculum_widget_label\">").append(label).append("</label>")
		  .append("<span class=\"o_curriculum_widget_value\">").append(value).append("</span>")
		  .append("</span>");
		return sb.toString();
	}
	
	private void initOtherRoles(UserRequest ureq) {
		if(otherRolesInitialized) return;// Only once
		
		SearchMemberParameters params = new SearchMemberParameters(curriculumElementInfos.curriculumElement());
		params.setRoles(List.of(CurriculumRoles.coach, CurriculumRoles.mastercoach, CurriculumRoles.owner));
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);

		List<Identity> coaches = getCurriculumMembers(members, CurriculumRoles.coach);
		if(!coaches.isEmpty()) {
			coachesLink = uifactory.addFormLink("curriculum.element.coaches", "curriculum.element.coaches", null, flc, Link.LINK);
			UsersPortraitsComponent coachesCmp = createUsersPortraits(ureq, coaches, "curriculum.element.coaches");
			flc.put("coaches", coachesCmp);
		} else {
			flc.remove("coaches");
		}

		List<Identity> masterCoaches = getCurriculumMembers(members, CurriculumRoles.mastercoach);
		if(!masterCoaches.isEmpty()) {
			masterCoachesLink = uifactory.addFormLink("curriculum.element.mastercoaches", "curriculum.element.mastercoaches", null, flc, Link.LINK);
			UsersPortraitsComponent masterCoachesCmp = createUsersPortraits(ureq, masterCoaches, "curriculum.element.mastercoaches");
			flc.put("masterCoaches", masterCoachesCmp);
		} else {
			flc.remove("masterCoaches");
		}

		List<Identity> owners = getCurriculumMembers(members, CurriculumRoles.owner);
		if(!owners.isEmpty()) {
			ownersLink = uifactory.addFormLink("curriculum.element.owners", "curriculum.element.owners", null, flc, Link.LINK);
			UsersPortraitsComponent ownersCmp = createUsersPortraits(ureq, owners, "curriculum.element.owners");
			flc.put("owners", ownersCmp);
		} else {
			flc.remove("owners");
		}
		otherRolesInitialized = true;
	}
	
	private List<Identity> getCurriculumMembers(List<CurriculumMember> members, CurriculumRoles role) {
		return members.stream()
				.filter(m -> role.name().equals(m.getRole()))
				.map(CurriculumMember::getIdentity)
				.toList();
	}
	
	private UsersPortraitsComponent createUsersPortraits(UserRequest ureq, List<Identity> members, String role) {
		List<PortraitUser> portraitUsers = userPortraitService.createPortraitUsers(getLocale(), members);
		UsersPortraitsComponent usersPortraitCmp = UserPortraitFactory.createUsersPortraits(ureq, "users_" + counter++, flc.getFormItemComponent());
		usersPortraitCmp.setAriaLabel(translate(role));
		usersPortraitCmp.setSize(PortraitSize.medium);
		usersPortraitCmp.setMaxUsersVisible(5);
		usersPortraitCmp.setUsers(portraitUsers);
		return usersPortraitCmp;
	}
	
	private void updateMinimizeButton() {
		if(minimized.get()) {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_expand");
		} else {
			minimizeButton.setIconLeftCSS("o_icon o_icon_details_collaps");
		}
	}
	
	public void loadModel(UserRequest ureq) {
		curriculumElementInfos = loadInformations(curriculumElementInfos.curriculumElement());
		this.otherRolesInitialized = false;
		initOtherRoles(ureq);
		initAvailability(flc);
		
		String activeText = getText(translate("num.of.active.participants"), Long.toString(curriculumElementInfos.numOfParticipants()));
		activeParticipantsLink.setI18nKey(activeText);
		String pendingText = getText(translate("num.of.pending.participants"), Long.toString(curriculumElementInfos.numOfPending()));
		pendingParticipantsLink.setI18nKey(pendingText);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(membersLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_MEMBERS);
			fireEvent(ureq, new ActivateEvent(entries));
		} else if(coachesLink == source) {
			fireActivateActiveEvent(ureq, "Coach");	
		} else if(masterCoachesLink == source) {
			fireActivateActiveEvent(ureq, "MasterCoach");	
		} else if(ownersLink == source) {
			fireActivateActiveEvent(ureq, "Owner");	
		} else if(activeParticipantsLink == source) {
			fireActivateActiveEvent(ureq, "Participant");
		} else if(pendingParticipantsLink == source) {
			fireActivateEvent(ureq, "Pending");
		} else if(minimizeButton == source) {
			toogle(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void fireActivateEvent(UserRequest ureq, String filter) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS),
						OresHelper.createOLATResourceableType(filter));
		fireEvent(ureq, new ActivateEvent(entries));
	}
	
	private void fireActivateActiveEvent(UserRequest ureq, String filter) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS),
						OresHelper.createOLATResourceableType("Active"), OresHelper.createOLATResourceableType(filter));
		fireEvent(ureq, new ActivateEvent(entries));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void toogle(UserRequest ureq) {
		minimized.set(!minimized.get());
		updateMinimizeButton();
		ureq.getUserSession().getGuiPreferences()
			.putAndSave(LectureBlocksWidgetController.class, preferencesId, Boolean.valueOf(minimized.get()));
		if(!minimized.get()) {
			initOtherRoles(ureq);
		}
	}
}
