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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.EmailProperty;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesSearchFormController extends FormBasicController {

	public static final String PROPS_IDENTIFIER = LecturesSearchFormController.class.getName();
	
	private TextElement login;
	private TextElement bulkEl;
	private TextElement curriculumEl;
	private FormLink searchButton;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private FormLayoutContainer privateDatesCont;
	private SingleSelection dateTypesEl;
	private SingleSelection publicDatesEl;

	private final boolean admin;
	private final boolean adminProps;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final Map<String,FormItem> propFormItems = new HashMap<>();
	private final List<OrganisationRef> searcheableOrganisations;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	
	public LecturesSearchFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		admin = roles.isAdministrator() || roles.isLearnResourceManager() || roles.isLectureManager() || roles.isAuthor();
		adminProps = securityModule.isUserAllowedAdminProps(roles);
		
		if(organisationModule.isEnabled()) {
			searcheableOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.administrator,
				OrganisationRoles.principal, OrganisationRoles.learnresourcemanager, OrganisationRoles.lecturemanager);
		} else {
			searcheableOrganisations = null;
		}
		
		initForm(ureq);
		updateDatesVisibility();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		login = uifactory.addTextElement("login", "search.form.login", 128, "", formLayout);
		login.setVisible(adminProps);
		
		List<UserPropertyHandler> allPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, adminProps);
		if(adminProps) {
			userPropertyHandlers = allPropertyHandlers.stream()
					.filter(prop -> !UserConstants.NICKNAME.equals(prop.getName()))
					.collect(Collectors.toList());
		} else {
			userPropertyHandlers = allPropertyHandlers;
		}

		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem fi = userPropertyHandler.addFormItem(getLocale(), null, PROPS_IDENTIFIER, false, formLayout);
				fi.setMandatory(false);
				// DO NOT validate email field => see OLAT-3324, OO-155, OO-222
				if (userPropertyHandler instanceof EmailProperty && fi instanceof TextElement) {
					TextElement textElement = (TextElement)fi;
					textElement.setItemValidatorProvider(null);
				}
	
				propFormItems.put(userPropertyHandler.getName(), fi);
			}
		}
		
		bulkEl = uifactory.addTextAreaElement("bulk", 4, 72, "", formLayout);
		bulkEl.setHelpText(translate("bulk.hint"));
		bulkEl.setExampleKey("bulk.example", null);

		List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
		
		String[] dateKeys;
		String[] dateValues;
		if(cycles.isEmpty()) {
			dateKeys = new String[]{ "none", "private"};
			dateValues = new String[] { translate("dates.none"), translate("dates.private") };
		} else {
			dateKeys = new String[]{ "none", "private", "public"};
			dateValues = new String[] { translate("dates.none"), translate("dates.private"), translate("dates.public")	};
		}

		dateTypesEl = uifactory.addRadiosVertical("dates", formLayout, dateKeys, dateValues);
		dateTypesEl.select(dateKeys[0], true);
		dateTypesEl.addActionListener(FormEvent.ONCHANGE);

		List<RepositoryEntryLifecycle> filteredCycles = new ArrayList<>();
		for(RepositoryEntryLifecycle cycle:cycles) {
			if(cycle.getValidTo() == null) {
				filteredCycles.add(cycle);
			}
		}
		
		String[] publicKeys = new String[filteredCycles.size()];
		String[] publicValues = new String[filteredCycles.size()];
		int count = 0;		
		for(RepositoryEntryLifecycle cycle:filteredCycles) {
				publicKeys[count] = cycle.getKey().toString();
				
				StringBuilder sb = new StringBuilder(32);
				boolean labelAvailable = StringHelper.containsNonWhitespace(cycle.getLabel());
				if(labelAvailable) {
					sb.append(cycle.getLabel());
				}
				if(StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
					if(labelAvailable) sb.append(" - ");
					sb.append(cycle.getSoftKey());
				}
				publicValues[count++] = sb.toString();
		}
		publicDatesEl = uifactory.addDropdownSingleselect("public.dates", formLayout, publicKeys, publicValues, null);

		String privateDatePage = velocity_root + "/cycle_dates.html";
		privateDatesCont = FormLayoutContainer.createCustomFormLayout("private.date", getTranslator(), privateDatePage);
		privateDatesCont.setRootForm(mainForm);
		privateDatesCont.setLabel("private.dates", null);
		formLayout.add("private.date", privateDatesCont);
		
		startDateEl = uifactory.addDateChooser("date.start", "date.start", null, privateDatesCont);
		startDateEl.setElementCssClass("o_sel_repo_lifecycle_validfrom");
		endDateEl = uifactory.addDateChooser("date.end", "date.end", null, privateDatesCont);
		endDateEl.setElementCssClass("o_sel_repo_lifecycle_validto");
		
		curriculumEl = uifactory.addTextElement("curriculum", "curriculum", 255, null, formLayout);
		curriculumEl.setVisible(curriculumModule.isEnabled());
	
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("search", buttonCont);
	}
	
	private void updateDatesVisibility() {
		if(dateTypesEl.isOneSelected()) {
			String type = dateTypesEl.getSelectedKey();
			if("none".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(false);
			} else if("public".equals(type)) {
				publicDatesEl.setVisible(true);
				privateDatesCont.setVisible(false);
			} else if("private".equals(type)) {
				publicDatesEl.setVisible(false);
				privateDatesCont.setVisible(true);
			}
		}
	}
	
	public List<UserPropertyHandler> getUserPropertyHandlers() {
		return userPropertyHandlers;
	}
	
	public LectureStatisticsSearchParameters getSearchParameters() {
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();

		String type = dateTypesEl.getSelectedKey();
		if("none".equals(type)) {
			params.setStartDate(null);
			params.setEndDate(null);
			params.setLifecycle(null);
		} else if("public".equals(type)) {
			params.setStartDate(null);
			params.setEndDate(null);
			if(publicDatesEl.isOneSelected() && StringHelper.isLong(publicDatesEl.getSelectedKey())) {
				RepositoryEntryLifecycle lifecycle = lifecycleDao.loadById(Long.valueOf(publicDatesEl.getSelectedKey()));
				params.setLifecycle(lifecycle);
			} else {
				params.setLifecycle(null);
			}
		} else if("private".equals(type)) {
			params.setStartDate(startDateEl.getDate());
			params.setEndDate(endDateEl.getDate());
			params.setLifecycle(null);
		}
		
		params.setLogin(getLogin());
		params.setBulkIdentifiers(getBulkIdentifiers());
		params.setUserProperties(getSearchProperties());
		params.setOrganisations(searcheableOrganisations);
		
		params.setCurriculumSearchString(curriculumEl.getValue());
		return params;
	}
	
	private String getLogin() {
		return login.isVisible() && StringHelper.containsNonWhitespace(login.getValue())
				? login.getValue() : null;
	}
	
	private Map<String,String> getSearchProperties() {
		Map<String, String> userPropertiesSearch = new HashMap<>();				
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem ui = propFormItems.get(userPropertyHandler.getName());
				String uiValue = userPropertyHandler.getStringValue(ui);
				if(userPropertyHandler.getName().startsWith("genericCheckboxProperty")) {
					if(!"false".equals(uiValue)) {
						userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
					}
				} else if (StringHelper.containsNonWhitespace(uiValue) && !uiValue.equals("-")) {
					userPropertiesSearch.put(userPropertyHandler.getName(), uiValue);
				}
			}
		}
		return userPropertiesSearch;
	}
	
	private List<String> getBulkIdentifiers() {
		String val = bulkEl.getValue();
		
		List<String> identifiers = new ArrayList<>();
		String[] lines = val.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if(username.length() > 0) {
				identifiers.add(username);
			}
		}
		return identifiers;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateTypesEl) {
			updateDatesVisibility();
		} else if (source == searchButton) {
			if(validate()) {
				fireEvent (ureq, Event.DONE_EVENT);
			}		
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validate();
		return allOk;
	}
	
	private boolean validate() {
		boolean atLeastOne = false;
		if(login.isVisible()) {
			atLeastOne = isNotEmpty(login.getValue());
		}
				
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler != null) {
				FormItem ui = propFormItems.get(userPropertyHandler.getName());
				String uiValue = userPropertyHandler.getStringValue(ui);
				if (isNotEmpty(uiValue)) {
					atLeastOne |= true;
				}
			}
		}
		
		if(isNotEmpty(bulkEl.getValue())) {
			atLeastOne |= true;
		}
		
		if(curriculumEl.isVisible() && isNotEmpty(curriculumEl.getValue())) {
			atLeastOne |= true;
		}
		
		if("public".equals(dateTypesEl.getSelectedKey()) && publicDatesEl.isVisible() && publicDatesEl.isOneSelected()) {
			atLeastOne |= true;
		}
		if("private".equals(dateTypesEl.getSelectedKey()) && (startDateEl.getDate() != null && endDateEl.getDate() != null)) {
			atLeastOne |= true;
		}
		
		if(!atLeastOne) {
			showWarning("error.search.form.notempty");
		}
		return atLeastOne;
	}
	
	/**
	 * Coaches have a restriction on the user they can see, the
	 * test is therefore lighter for them.
	 * 
	 * @param val The value to check
	 * @return false if the value is considered too small to be acceptable
	 */
	private boolean isNotEmpty(String val) {
		if(admin) {
			int count = 0;
			if(StringHelper.containsNonWhitespace(val) && !val.equals("-") && !val.equals("*")) {
				for(char c:val.toCharArray()) {
					if(c == '-'
							|| (c >= 48 && c <= 57)
							|| (c >= 65 && c <= 90)
							|| (c >= 97 && c <= 122)) {
						count++;
					}
				}
			}
			return count >= 2;
		}
		return StringHelper.containsNonWhitespace(val) && !val.equals("-");
	}
}
