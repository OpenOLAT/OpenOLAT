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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.indicators.IndicatorsItem;
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
import org.olat.modules.curriculum.ui.CurriculumElementDetailsController;
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

	private IndicatorsItem participantsIndicatorsEl;
	private FormLink participantsKeyLink;
	private FormLink activeParticipantsLink;
	private FormLink pendingParticipantsLink;
	private FormLink ownersLink;
	private FormLink coachesLink;
	private FormLink masterCoachesLink;

	private int counter = 0;
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
				.createPackageTranslator(CurriculumElementDetailsController.class, ureq.getLocale()));
		curriculumElementInfos = loadInformations(curriculumElement);
		initForm(ureq);
	}

	private CurriculumElementInfos loadInformations(CurriculumElement curriculumElement) {
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElements(null, List.of(curriculumElement));
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		if(elements == null || elements.isEmpty()) {
			return new CurriculumElementInfos(curriculumElement, null, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l);
		}
		return elements.get(0);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		participantsIndicatorsEl = IndicatorsFactory.createItem("participantsIndicators", formLayout);

		participantsKeyLink = IndicatorsFactory.createIndicatorFormLink("participantsKey", "participants", "", "", formLayout);
		participantsKeyLink.setUrl(getUrl("[Members:0]"));
		activeParticipantsLink = IndicatorsFactory.createIndicatorFormLink("activeParticipants", "activeParticipants", "", "", formLayout);
		activeParticipantsLink.setUrl(getUrl("[Members:0][Active:0][Participant:0]"));
		pendingParticipantsLink = IndicatorsFactory.createIndicatorFormLink("pendingParticipants", "pendingParticipants", "", "", formLayout);
		pendingParticipantsLink.setUrl(getUrl("[Members:0][Pending:0][All:0]"));

		participantsIndicatorsEl.setKeyIndicator(participantsKeyLink);
		participantsIndicatorsEl.setFocusIndicatorsItems(List.of(activeParticipantsLink, pendingParticipantsLink));

		initOtherRoles(ureq);
		updateParticipantsIndicator();
	}

	private void updateParticipantsIndicator() {
		Long max = curriculumElementInfos.curriculumElement().getMaxParticipants();
		Long min = curriculumElementInfos.curriculumElement().getMinParticipants();
		long numActive = curriculumElementInfos.numOfParticipants();
		long numPending = curriculumElementInfos.numOfPending();
		long total = numActive + numPending;

		String figure;
		String label = translate("curriculum.element.participants");
		if (max != null && max > 0) {
			ParticipantsAvailabilityNum availabilityNum = acService.getParticipantsAvailability(max, total, true);
			figure = getSpeedometerHtml(total, max.doubleValue(), getSpeedometerCssClass(min, total, availabilityNum));
			label = label + "<br>" + getParticipantsSubInfo(min, total, availabilityNum);
		} else {
			figure = Long.toString(total);
			if (min != null && min > 0 && total < min) {
				label = label + "<br><span class='text-muted'>"
						+ translate("participants.to.minimum", Long.toString(min - total)) + "</span>";
			}
		}

		participantsKeyLink.setI18nKey(IndicatorsFactory.createLinkText(label, figure));

		activeParticipantsLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_members_widget_icon o_membership_status_active\"></i> " + translate("membership.active"),
				Long.toString(numActive)));
		pendingParticipantsLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_members_widget_icon o_membership_status_pending\"></i> " + translate("membership.pending"),
				Long.toString(numPending)));
	}

	private String getSpeedometerCssClass(Long min, long total, ParticipantsAvailabilityNum availabilityNum) {
		if (availabilityNum.availability() == ParticipantsAvailability.overbooked) {
			return "o_speedometer_danger";
		} else if (min != null && total < min) {
			return "o_speedometer_warning";
		}
		return "o_speedometer_success";
	}

	private String getParticipantsSubInfo(Long min, long total, ParticipantsAvailabilityNum availabilityNum) {
		ParticipantsAvailability availability = availabilityNum.availability();
		if (availability == ParticipantsAvailability.fullyBooked || availability == ParticipantsAvailability.overbooked) {
			return getAvailabilityText(availabilityNum);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<span class='text-muted'>");
		sb.append(translate("filter.occupancy.status.free.num.seats", Long.toString(availabilityNum.numAvailable())));
		if (min != null && min > 0 && total < min) {
			sb.append(" / ");
			sb.append(translate("participants.to.minimum", Long.toString(min - total)));
		}
		sb.append("</span>");
		return sb.toString();
	}

	private String getSpeedometerHtml(long value, double maxValue, String cssClass) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='o_speedometer ").append(cssClass).append("'>");
		sb.append("<span class='o_speedometer_value'>").append(value).append("</span>");
		if (maxValue > 0.0d && value > 0) {
			double rotation = (value / maxValue) * 180;
			double transposed = 180 - rotation;
			sb.append("<span class='o_speedometer_indicator' style='--var-speed:-").append(Math.round(transposed)).append("deg;'></span>");
		}
		sb.append("</div>");
		return sb.toString();
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

	private void initOtherRoles(UserRequest ureq) {
		if(otherRolesInitialized) return;// Only once

		SearchMemberParameters params = new SearchMemberParameters(curriculumElementInfos.curriculumElement());
		params.setRoles(List.of(CurriculumRoles.coach, CurriculumRoles.mastercoach, CurriculumRoles.owner));
		List<CurriculumMember> members = curriculumService.getCurriculumElementsMembers(params);

		List<Identity> coaches = getCurriculumMembers(members, CurriculumRoles.coach);
		if(!coaches.isEmpty()) {
			coachesLink = uifactory.addFormLink("curriculum.element.coaches", "curriculum.element.coaches", null, flc, Link.LINK);
			coachesLink.setUrl(getUrl("[Members:0][Active:0][Coach:0]"));
			UsersPortraitsComponent coachesCmp = createUsersPortraits(ureq, coaches, "curriculum.element.coaches");
			flc.put("coaches", coachesCmp);
		} else {
			flc.remove("coaches");
		}

		List<Identity> masterCoaches = getCurriculumMembers(members, CurriculumRoles.mastercoach);
		if(!masterCoaches.isEmpty()) {
			masterCoachesLink = uifactory.addFormLink("curriculum.element.mastercoaches", "curriculum.element.mastercoaches", null, flc, Link.LINK);
			masterCoachesLink.setUrl(getUrl("[Members:0][Active:0][MasterCoach:0]"));
			UsersPortraitsComponent masterCoachesCmp = createUsersPortraits(ureq, masterCoaches, "curriculum.element.mastercoaches");
			flc.put("masterCoaches", masterCoachesCmp);
		} else {
			flc.remove("masterCoaches");
		}

		List<Identity> owners = getCurriculumMembers(members, CurriculumRoles.owner);
		if(!owners.isEmpty()) {
			ownersLink = uifactory.addFormLink("course.owners", "role.owner", null, flc, Link.LINK);
			ownersLink.setUrl(getUrl("[Members:0][Active:0][Owner:0]"));
			UsersPortraitsComponent ownersCmp = createUsersPortraits(ureq, owners, "role.owner");
			flc.put("owners", ownersCmp);
		} else {
			flc.remove("owners");
		}
		otherRolesInitialized = true;
	}

	private String getUrl(String path) {
		return BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(
				"[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElementInfos.curriculumElement().getKey() + "]" + path);
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

	public void loadModel(UserRequest ureq) {
		curriculumElementInfos = loadInformations(curriculumElementInfos.curriculumElement());
		this.otherRolesInitialized = false;
		initOtherRoles(ureq);
		updateParticipantsIndicator();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(coachesLink == source) {
			fireActivateActiveEvent(ureq, "Coach");
		} else if(masterCoachesLink == source) {
			fireActivateActiveEvent(ureq, "MasterCoach");
		} else if(ownersLink == source) {
			fireActivateActiveEvent(ureq, "Owner");
		} else if(participantsKeyLink == source) {
			fireActivateMembersEvent(ureq);
		} else if(activeParticipantsLink == source) {
			fireActivateActiveEvent(ureq, "Participant");
		} else if(pendingParticipantsLink == source) {
			fireActivatePendingEvent(ureq, "All");
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void fireActivateMembersEvent(UserRequest ureq) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS));
		fireEvent(ureq, new ActivateEvent(entries));
	}

	private void fireActivatePendingEvent(UserRequest ureq, String filter) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance()
				.createCEListFromString(OresHelper.createOLATResourceableType(CurriculumListManagerController.CONTEXT_MEMBERS),
						OresHelper.createOLATResourceableType("Pending"), OresHelper.createOLATResourceableType(filter));
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

}
