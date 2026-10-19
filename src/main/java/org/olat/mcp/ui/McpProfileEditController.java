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
package org.olat.mcp.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.mcp.McpModule;
import org.olat.mcp.McpProfile;
import org.olat.mcp.McpProfileType;
import org.olat.mcp.McpService;
import org.olat.mcp.manager.McpToolRegistry;
import org.olat.mcp.model.McpProfileImpl;
import org.olat.user.ui.organisation.OrganisationSelectionSource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates or edits an MCP profile: the general settings, the organisation
 * roles of a system profile, the rate limit override and the tools grouped
 * by pack. The identifier and the type are set at creation and never change.
 * Tool names of the profile without a registered provider are kept.
 *
 * Initial date: 2 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpProfileEditController extends FormBasicController {

	private static final Pattern IDENTIFIER = Pattern.compile(McpProfile.IDENTIFIER_PATTERN);
	static final int DESCRIPTION_MAX_LENGTH = 4000;
	static final int RATE_LIMIT_MAX = 100000;

	private TextElement identifierEl;
	private TextElement nameEl;
	private TextAreaElement descriptionEl;
	private FormToggle enabledEl;
	private SingleSelection typeEl;
	private MultipleSelectionElement rolesEl;
	private TextElement rateLimitEl;
	private ObjectSelectionElement organisationsEl;
	private McpToolTreeController toolsCtrl;
	private final Set<String> unknownTools = new TreeSet<>();
	private final List<Organisation> profileOrganisations;
	private final Roles managerRoles;

	private McpProfile profile;

	@Autowired
	private McpModule mcpModule;
	@Autowired
	private McpService mcpService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private McpToolRegistry toolRegistry;

	/**
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param profile The profile to edit, null to create a profile
	 */
	public McpProfileEditController(UserRequest ureq, WindowControl wControl, McpProfile profile) {
		super(ureq, wControl, LAYOUT_DEFAULT, Util.createPackageTranslator(UserSearchFlexiController.class, ureq.getLocale()));
		this.profile = profile;
		managerRoles = ureq.getUserSession().getRoles();
		profileOrganisations = profile == null ? List.of() : mcpService.getOrganisations(profile);
		if(profile != null) {
			Set<String> registered = new HashSet<>();
			toolRegistry.getProviders().forEach(provider -> registered.add(provider.getToolName()));
			for(String tool:profile.getTools()) {
				if(!registered.contains(tool)) {
					unknownTools.add(tool);
				}
			}
		}
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_mcp_profile_form");
		initGeneralForm(formLayout);
		initToolsForm(formLayout, ureq);

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initGeneralForm(FormItemContainer formLayout) {
		FormSection generalCont = uifactory.addFormSection("general", translate("profile.section.general"), formLayout, FormSection.Level.SUB_TITLE);

		if(profile == null) {
			identifierEl = uifactory.addTextElement("profile.identifier", McpProfileImpl.IDENTIFIER_MAX_LENGTH, "", generalCont);
			identifierEl.setMandatory(true);
			identifierEl.setExampleKey("profile.identifier.hint", null);
			identifierEl.setElementCssClass("o_sel_mcp_profile_identifier");
		} else {
			uifactory.addStaticTextElement("profile.identifier", "profile.identifier",
					StringHelper.escapeHtml(profile.getIdentifier()), generalCont);
		}

		String name = profile == null ? "" : profile.getName();
		nameEl = uifactory.addTextElement("profile.name", McpProfileImpl.NAME_MAX_LENGTH, name, generalCont);
		nameEl.setMandatory(true);
		nameEl.setElementCssClass("o_sel_mcp_profile_name");

		String description = profile == null ? "" : profile.getDescription();
		descriptionEl = uifactory.addTextAreaElement("profile.description", "profile.description",
				DESCRIPTION_MAX_LENGTH, 3, 60, false, false, description, generalCont);

		enabledEl = uifactory.addToggleButton("profile.enabled", "profile.enabled", translate("on"), translate("off"), generalCont);
		enabledEl.toggle(profile != null && profile.isEnabled());
		enabledEl.setExampleKey("profile.enabled.hint", null);

		SelectionValues typeValues = new SelectionValues();
		typeValues.add(SelectionValues.entry(McpProfileType.system.name(), translate("profile.type.system"),
				translate("profile.type.system.desc"), null, null, true));
		typeValues.add(SelectionValues.entry(McpProfileType.user.name(), translate("profile.type.user"),
				translate("profile.type.user.desc"), null, null, true));
		if(profile == null) {
			typeEl = uifactory.addCardSingleSelectHorizontal("profile.type", "profile.type", generalCont, typeValues);
			typeEl.setMandatory(true);
			typeEl.select(McpProfileType.system.name(), true);
			typeEl.addActionListener(FormEvent.ONCHANGE);
		} else {
			String type = profile.getType() == null ? "" : translate("profile.type." + profile.getType().name());
			uifactory.addStaticTextElement("profile.type", "profile.type", type, generalCont);
		}

		initOrganisationsForm(generalCont);

		SelectionValues roleValues = new SelectionValues();
		for(OrganisationRoles role:OrganisationRoles.valuesWithoutGuestAndInvitee()) {
			roleValues.add(SelectionValues.entry(role.name(), translate("role." + role.name())));
		}
		rolesEl = uifactory.addCheckboxesVertical("profile.roles", "profile.roles", generalCont,
				roleValues.keys(), roleValues.values(), 2);
		rolesEl.setMandatory(true);
		rolesEl.setExampleKey("profile.roles.hint", null);
		if(profile != null) {
			for(OrganisationRoles role:profile.getOrganisationRoles()) {
				if(roleValues.containsKey(role.name())) {
					rolesEl.select(role.name(), true);
				}
			}
		}

		Integer rateLimit = profile == null ? null : profile.getRateLimitRequestsPerMinute();
		rateLimitEl = uifactory.addTextElement("profile.ratelimit", 6, rateLimit == null ? "" : rateLimit.toString(), generalCont);
		rateLimitEl.setDisplaySize(8);
		rateLimitEl.setExampleKey("profile.ratelimit.hint", new String[] { Integer.toString(mcpModule.getRateLimitRequestsPerMinute()) });
	}

	private void initOrganisationsForm(FormItemContainer formLayout) {
		Collection<? extends OrganisationRef> selected = profileOrganisations;
		if(profile == null) {
			selected = List.of(organisationService.getDefaultOrganisation());
		}
		OrganisationSelectionSource organisationSource = new OrganisationSelectionSource(selected,
				() -> managerRoles.isSystemAdmin()
					? organisationService.getOrganisations()
					: organisationService.getOrganisations(getIdentity(), managerRoles, OrganisationRoles.administrator));
		organisationsEl = uifactory.addObjectSelectionElement("organisations", "profile.organisations", formLayout,
				getWindowControl(), true, organisationSource);
		organisationsEl.setMandatory(true);
		organisationsEl.setExampleKey("profile.organisations.hint", null);
		organisationsEl.setElementCssClass("o_sel_mcp_profile_organisations");
		// without organisation module the default organisation is the only one
		organisationsEl.setVisible(organisationModule.isEnabled());
	}

	private void initToolsForm(FormItemContainer formLayout, UserRequest ureq) {
		FormSection toolsCont = uifactory.addFormSection("tools", translate("profile.section.tools"), formLayout, FormSection.Level.SUB_TITLE);
		uifactory.addStaticTextElement("tools_intro", null, translate("profile.tools.intro"), toolsCont);

		Set<String> selectedTools = profile == null ? Set.of() : profile.getTools();
		toolsCtrl = new McpToolTreeController(ureq, getWindowControl(), mainForm, selectedTools, true);
		listenTo(toolsCtrl);
		toolsCont.add("tools_tree", toolsCtrl.getInitialFormItem());

		if(!unknownTools.isEmpty()) {
			uifactory.addStaticTextElement("profile.tools.unknown", "profile.tools.unknown",
					StringHelper.escapeHtml(String.join(", ", unknownTools)), toolsCont);
		}
	}

	private McpProfileType getSelectedType() {
		if(profile != null) {
			return profile.getType();
		}
		return typeEl.isOneSelected() ? McpProfileType.secureValueOf(typeEl.getSelectedKey()) : null;
	}

	private void updateUI() {
		rolesEl.setVisible(getSelectedType() == McpProfileType.system);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == typeEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		if(identifierEl != null) {
			identifierEl.clearError();
			String identifier = identifierEl.getValue();
			if(!StringHelper.containsNonWhitespace(identifier)) {
				identifierEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!IDENTIFIER.matcher(identifier.trim()).matches()) {
				identifierEl.setErrorKey("profile.identifier.invalid");
				allOk &= false;
			} else if(mcpService.getProfileByIdentifier(identifier.trim()) != null) {
				identifierEl.setErrorKey("profile.identifier.taken");
				allOk &= false;
			}
		}

		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else if(nameEl.getValue().trim().length() > McpProfileImpl.NAME_MAX_LENGTH) {
			nameEl.setErrorKey("input.toolong", Integer.toString(McpProfileImpl.NAME_MAX_LENGTH));
			allOk &= false;
		}

		descriptionEl.clearError();
		if(descriptionEl.getValue() != null && descriptionEl.getValue().length() > DESCRIPTION_MAX_LENGTH) {
			descriptionEl.setErrorKey("input.toolong", Integer.toString(DESCRIPTION_MAX_LENGTH));
			allOk &= false;
		}

		if(typeEl != null) {
			typeEl.clearError();
			if(!typeEl.isOneSelected()) {
				typeEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		organisationsEl.clearError();
		if(getSelectedOrganisations().isEmpty()) {
			organisationsEl.setErrorKey("profile.organisations.mandatory");
			allOk &= false;
		}

		rolesEl.clearError();
		if(rolesEl.isVisible() && !rolesEl.isAtLeastSelected(1)) {
			rolesEl.setErrorKey("profile.roles.mandatory");
			allOk &= false;
		}

		allOk &= McpUIHelper.validateInteger(rateLimitEl, 1, RATE_LIMIT_MAX, false);

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		McpProfile editedProfile;
		if(profile == null) {
			try {
				editedProfile = mcpService.createProfile(identifierEl.getValue().trim(), nameEl.getValue().trim(), getSelectedType(),
						getSelectedOrganisations());
			} catch (IllegalArgumentException e) {
				// a parallel creation took the identifier
				identifierEl.setErrorKey("profile.identifier.taken");
				return;
			}
		} else {
			editedProfile = mcpService.getProfile(profile);
			if(editedProfile == null) {
				showWarning("profile.deleted");
				fireEvent(ureq, Event.CANCELLED_EVENT);
				return;
			}
		}

		editedProfile.setName(nameEl.getValue().trim());
		String description = descriptionEl.getValue();
		editedProfile.setDescription(StringHelper.containsNonWhitespace(description) ? description.trim() : null);
		editedProfile.setEnabled(enabledEl.isOn());
		if(editedProfile.getType() == McpProfileType.system) {
			editedProfile.setOrganisationRoles(getSelectedRoles());
		} else {
			editedProfile.setOrganisationRoles(List.of());
		}
		editedProfile.setRateLimitRequestsPerMinute(McpUIHelper.getInteger(rateLimitEl));
		editedProfile.setTools(getSelectedTools());

		// the service refuses an enabled profile without organisation, link them first
		mcpService.setOrganisations(editedProfile, getSelectedOrganisations());
		profile = mcpService.updateProfile(editedProfile);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private List<OrganisationRoles> getSelectedRoles() {
		Collection<String> selectedKeys = rolesEl.getSelectedKeys();
		List<OrganisationRoles> roles = new ArrayList<>(selectedKeys.size());
		for(OrganisationRoles role:OrganisationRoles.valuesWithoutGuestAndInvitee()) {
			if(selectedKeys.contains(role.name())) {
				roles.add(role);
			}
		}
		return roles;
	}

	private Set<String> getSelectedTools() {
		Set<String> tools = new HashSet<>(unknownTools);
		tools.addAll(toolsCtrl.getSelectedTools());
		return tools;
	}

	/**
	 * @return The selected organisations, the current organisations of the
	 * 		profile or the default organisation if the organisation module is
	 * 		switched off
	 */
	private List<Organisation> getSelectedOrganisations() {
		if(!organisationsEl.isVisible()) {
			return profileOrganisations.isEmpty()
					? List.of(organisationService.getDefaultOrganisation())
					: profileOrganisations;
		}
		return organisationService.getOrganisation(OrganisationSelectionSource.toRefs(organisationsEl.getSelectedKeys()));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
