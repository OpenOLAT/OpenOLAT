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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementShort;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.member.CurriculumMembershipCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class MemberLeaveConfirmationController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(MemberLeaveConfirmationController.class);

	private FormLink confirmLink;
	private MultipleSelectionElement mailEl;

	private static final String[] keys = {"mail"};
	private final List<Identity> identities;
	private final List<CourseMembership> memberships;
	private final boolean withinCourse;
	private final boolean isBulkAction;

	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final List<MemberRow> members;
	private final CurriculumElement curriculumElement;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private BusinessGroupService businessGroupService;

	public MemberLeaveConfirmationController(UserRequest ureq, WindowControl wControl, List<Identity> identities,
											 List<CourseMembership> memberships, RepositoryEntry entry, BusinessGroup businessGroup,
											 List<MemberRow> members, CurriculumElement curriculumElement) {
		super(ureq, wControl, "confirm_delete");
		this.identities = identities;
		this.memberships = memberships;
		this.withinCourse = entry != null;
		this.entry = entry;
		this.businessGroup = businessGroup;
		this.members = members;
		this.curriculumElement = curriculumElement;
		this.isBulkAction = identities.size() > 1;
		initForm(ureq);
	}

	public List<Identity> getIdentities() {
		return identities;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("withinCourse", withinCourse);
		}

		if (identities != null && formLayout instanceof FormLayoutContainer formLayoutCont) {
			String externalRef = "";
			if (withinCourse) {
				externalRef = entry.getExternalRef() != null ? entry.getExternalRef() : "";
			} else if (curriculumElement != null) {
				externalRef = curriculumElement.getIdentifier() != null ? curriculumElement.getIdentifier() : "";
			} else if (businessGroup != null) {
				externalRef = businessGroup.getExternalId() != null ? businessGroup.getExternalId() : "";
			}

			String msg = constructMessage(externalRef);
			formLayoutCont.contextPut("msg", msg);
			// only available if there are members from a group
			if (businessGroup == null) {
				constructGroupMsg(formLayoutCont);
			}
		}

		boolean mandatoryEmail = groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		FormLayoutContainer optionsCont = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
		formLayout.add(optionsCont);
		formLayout.add("options", optionsCont);
		String[] values = new String[] {
				identities != null && identities.size() > 1 ? translate("remove.send.mail.bulk") : translate("remove.send.mail")
		};

		// if it's a bulk action, then construct a list of members with their roles
		if (isBulkAction) {
			List<String> memberValues = new ArrayList<>();
			if (withinCourse || businessGroup != null) {
				for (MemberRow member : members) {
					memberValues.add(userManager.getUserDisplayName(member.getIdentityKey()) + " (" + StringHelper.unescapeHtml(getRolesRendered(member, null) + ")"));
				}
			} else if (identities != null) {
				for (int i = 0; i < identities.size(); i++) {
					memberValues.add(userManager.getUserDisplayName(identities.get(i)) + " (" + StringHelper.unescapeHtml(getRolesRendered(null, memberships.get(i)) + ")"));
				}
			}


			StaticListElement memberValuesEl = uifactory.addStaticListElement("member.values", null, memberValues, optionsCont);
			memberValuesEl.setLabel(translate("members"), null, false);
			memberValuesEl.setShowAllI18nKey("remove.members.bulk.show.all");
		}

		mailEl = uifactory.addCheckboxesHorizontal("typ", "remove.send.mail.label", optionsCont, keys, values);
		mailEl.select(keys[0], true);
		mailEl.setEnabled(!mandatoryEmail);

		confirmLink = uifactory.addFormLink("confirm.leave", formLayout, Link.BUTTON);
		confirmLink.setElementCssClass("btn-danger");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	private String constructMessage(String externalRef) {
		String[] args;
		String msg;
		String msgTranslationKey;
		String displayName = "";
		if (withinCourse) {
			displayName = StringHelper.escapeHtml(entry.getDisplayname());
		} else if (curriculumElement != null) {
			displayName = StringHelper.escapeHtml(curriculumElement.getDisplayName());
		} else if (businessGroup != null) {
			displayName = StringHelper.escapeHtml(businessGroup.getName());
		}
		if (isBulkAction) {
			args = new String[]{
					String.valueOf(identities.size()),
					displayName,
					Formatter.addReference(getTranslator(), externalRef)
			};
			if (withinCourse) {
				msgTranslationKey = "dialog.modal.bg.remove.course.text.bulk";
			} else if (curriculumElement != null) {
				msgTranslationKey = "dialog.modal.bg.remove.cur.text.bulk";
			} else {
				msgTranslationKey = "dialog.modal.bg.remove.text.bulk";
			}
		} else {
			args = new String[]{
					StringHelper.escapeHtml(userManager.getUserDisplayName(identities.get(0))),
					getRolesRendered(members != null ? members.get(0) : null, memberships.get(0)),
					displayName,
					Formatter.addReference(getTranslator(), externalRef)
			};
			if (withinCourse) {
				msgTranslationKey = "dialog.modal.bg.remove.course.text";
			} else if (curriculumElement != null) {
				msgTranslationKey = "dialog.modal.bg.remove.cur.text";
			} else {
				msgTranslationKey = "dialog.modal.bg.remove.text";
			}
		}
		msg = translate(msgTranslationKey, args);
		return msg;
	}

	private void constructGroupMsg(FormLayoutContainer formLayoutCont) {
		boolean hasBusinessGroupMember = memberships.stream().anyMatch(CourseMembership::isBusinessGroupMember);

		if (hasBusinessGroupMember) {
			String groupMsg;
			StringBuilder groupsRendered = new StringBuilder();
			if (isBulkAction) {
				int groupMemberCount = 0;
				for (MemberRow member : members) {
					if (member.getMembership().isBusinessGroupMember()) {
						groupsRendered.append(getGroupsRendered(member, null));
						// append groups with delimiter ,  except for the last one
						if (groupMemberCount < members.size() - 1) {
							groupsRendered.append(", ");
						}
						groupMemberCount += 1;
					}
				}
				groupMsg = translate("remove.group.member.bulk", String.valueOf(groupMemberCount));
			} else {
				// individual actions, the message depends on specific member details. Not relevant for curriculum
				// hence members won't be null
				groupMsg = translate("remove.group.member");
				groupsRendered = getGroupsRendered(members.get(0), null);
			}
			// convert groupsRendered into a string list and get distinct values
			List<String> groupValues = new ArrayList<>(Arrays.stream(groupsRendered.toString().split(", ")).map(r -> r.replace("\"", "")).distinct().toList());
			StaticListElement groupValuesEl = uifactory.addStaticListElement("group.values", null, groupValues, formLayoutCont);
			groupValuesEl.setShowAllI18nKey("remove.group.members.show.all");

			formLayoutCont.contextPut("groupMsg", groupMsg);
		}
	}

	/**
	 * Renders the roles of an identity
	 *
	 * @param member The member whose roles are to be rendered, may be null if not within course
	 * @param membership The course membership, may be null if within course (because not needed)
	 * @return String representation of the rendered roles.
	 */
	private String getRolesRendered(MemberRow member, CourseMembership membership) {
		StringBuilder resultBuilder = new StringBuilder();
		try (StringOutput roleOutput = new StringOutput()) {
			if (withinCourse || businessGroup != null) {
				renderCourseRoles(roleOutput, member);
			} else {
				renderCurriculumRoles(roleOutput, membership);
			}
			resultBuilder.append(roleOutput);
		} catch (Exception e) {
			log.error("Error rendering roles", e);
		}
		return resultBuilder.toString();
	}

	private void renderCourseRoles(StringOutput sb, MemberRow member) {
		boolean and = false;
		Translator translator = Util.createPackageTranslator(CourseRoleCellRenderer.class, getLocale());
		CourseMembership membership = member.getMembership();

		// default repository entry group
		if (membership.isRepositoryEntryOwner()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.repo.owner"));
		}
		if (membership.isRepositoryEntryCoach()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.repo.tutor"));
		}
		if (membership.isRepositoryEntryParticipant()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.repo.participant"));
		}

		// business groups
		if (membership.isBusinessGroupCoach()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.group.tutor"));
			if (isBulkAction && businessGroup == null) {
				sb.append(" ").append(String.valueOf(getGroupsRendered(member, true)));
			}
		}
		if (membership.isBusinessGroupParticipant()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.group.participant"));
			if (isBulkAction && businessGroup == null) {
				sb.append(" ").append(String.valueOf(getGroupsRendered(member, false)));
			}
		}

		// curriculum
		if (membership.isCurriculumElementParticipant()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.curriculum.participant"));
		}
		if (membership.isCurriculumElementCoach()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.curriculum.coach"));
		}
		if (membership.isCurriculumElementOwner()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.curriculum.owner"));
		}

		if (membership.isWaiting()) {
			and = and(sb, and, "; ");
			sb.append(translator.translate("role.group.waiting"));
		}
		if (membership.isPending()) {
			and = and(sb, and, "; ");
			// if reservation exists, then add additional information to pending role: If it is pending for a participant or coach
			if (membership.getResourceReservation() != null) {
				if (membership.getResourceReservation().getType().equals(BusinessGroupService.GROUP_PARTICIPANT)) {
					sb.append(translator.translate("role.group.participant"));
				} else if (membership.getResourceReservation().getType().equals(BusinessGroupService.GROUP_COACH)) {
					sb.append(translator.translate("role.group.tutor"));
				}
				sb.append(" ");
			}

			sb.append(translator.translate("role.pending"));
		}
		if (membership.isExternalUser()) {
			sb.append(" ").append(translator.translate("role.external.user"));
		}
	}

	private void renderCurriculumRoles(StringOutput sb, CourseMembership membership) {
		Translator curriculumTranslator = Util.createPackageTranslator(CurriculumComposerController.class, getLocale());
		CurriculumMembershipCellRenderer roleRenderer = new CurriculumMembershipCellRenderer(curriculumTranslator, "; ");
		roleRenderer.render(null, sb, membership, 0, null, null, null);
	}

	private boolean and(StringOutput sb, boolean and, String delimiter) {
		if (and) sb.append(delimiter);
		return true;
	}

	private StringBuilder getGroupsRendered(MemberRow member, Boolean isCoach) {
		StringBuilder resultBuilder = new StringBuilder();
		try (StringOutput groupOutput = new StringOutput()) {
			renderGroups(groupOutput, member, isCoach);
			resultBuilder.append(StringHelper.unescapeHtml(groupOutput.toString()));
		} catch (Exception e) {
			log.error("Error rendering groups", e);
		}
		return resultBuilder;
	}

	private void renderGroups(StringOutput sb, MemberRow member, Boolean isCoach) {
		boolean and = false;
		List<BusinessGroupShort> groups = member.getGroups();
		if (groups != null && !groups.isEmpty()) {
			groups = groups.stream()
					.collect(Collectors.toMap(BusinessGroupShort::getKey, Function.identity(), (existing, replacement) -> existing))
					.values()
					.stream()
					.toList();
			// Pre-process identities into a Map for quick access
			Map<Long, Identity> identityMap = identities.stream()
					.collect(Collectors.toMap(Identity::getKey, Function.identity()));
			for (BusinessGroupShort group : groups) {
				// Default to true if isCoach is null
				boolean renderCondition = isCoach == null;
				// get identity object of member
				Identity identity = identityMap.get(member.getIdentityKey());
				// to pass it for getting businessGroupMembership for that identity with a given group, only one possible
				// because first parameter only gets one groupKey
				BusinessGroupMembership bgMembership = businessGroupService.getBusinessGroupMembership(List.of(group.getKey()), identity).get(0);
				if (isCoach != null) {
					renderCondition = (isCoach && bgMembership.isOwner()) || (!isCoach && bgMembership.isParticipant() || bgMembership.isWaiting());
				}
				// will be true, if it is not a bulk action, so isCoach == null
				// or that member is coach in current looped group
				// or that member is participant in current looped group
				// or that member is waiting for current looped group
				if (renderCondition) {
					and = and(sb, and, ", ");
					sb.append("\"");
					if (group.getName() == null && group.getKey() != null) {
						sb.append(group.getKey());
					} else {
						sb.append(StringHelper.escapeHtml(group.getName()));
					}
					sb.append("\"");
					if (!isBulkAction && isCoach == null) {
						Translator translator = Util.createPackageTranslator(CourseRoleCellRenderer.class, getLocale());
						boolean hasBothRoles = bgMembership.isOwner() && bgMembership.isParticipant();
						sb.append(" (");
						if (bgMembership.isWaiting()) {
							sb.append(translator.translate("role.group.waiting"));
						}
						if (bgMembership.isOwner()) {
							sb.append(translator.translate("role.group.tutor"));
						}
						if (bgMembership.isParticipant()) {
							if (hasBothRoles) {
								and = and(sb, and, "; ");
							}
							sb.append(translator.translate("role.group.participant"));
						}
						sb.append(")");
					}
				}
			}
		}

		List<CurriculumElementShort> curriculumElements = member.getCurriculumElements();
		if (curriculumElements != null && !curriculumElements.isEmpty()) {
			for (CurriculumElementShort curEl : curriculumElements) {
				and = and(sb, and, ", ");
				if (curEl.getDisplayName() == null && curEl.getKey() != null) {
					sb.append(curEl.getKey());
				} else {
					sb.append(StringHelper.escapeHtml(curEl.getDisplayName()));
				}
			}
		}
	}

	public boolean isSendMail() {
		return mailEl.isSelected(0);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (confirmLink == source && (validateFormLogic(ureq))) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}