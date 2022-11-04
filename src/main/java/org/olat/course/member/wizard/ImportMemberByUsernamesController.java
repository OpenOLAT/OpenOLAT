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
package org.olat.course.member.wizard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.model.FindNamedIdentityCollection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ImportMemberByUsernamesController extends StepFormBasicController {
	
	public static final String RUN_CONTEXT_KEY = "import.member.by.username";
	private static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();

	private TextAreaElement namesEl;
	private FlexiTableElement tableEl;
	private ImportMemberOverviewDataModel userTableModel;
	
	private FormLink backLink;
	private FormLayoutContainer inputContainer;
	private FormLayoutContainer tableContainer;

	private boolean isAdministrativeUser;

	private final String formTitle;
	private final MembersByNameContext context;
	private List<String> notFoundNames;
	private Set<Identity> identitiesList;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;

	public ImportMemberByUsernamesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, String runContextKey, String formTitle) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.formTitle = formTitle;
		context = (MembersByNameContext)getOrCreateFromRunContext(runContextKey, MembersByNameContext::new);
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		initForm (ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (StringHelper.containsNonWhitespace(formTitle)) {
			setFormTranslatedTitle(formTitle);
		}
		
		// input field
		inputContainer = FormLayoutContainer.createDefaultFormLayout("input", getTranslator());
		formLayout.add(inputContainer);
		
		namesEl = uifactory.addTextAreaElement("addusers", "form.addusers", -1, 15, 40, false, false, context.getRawNames(), inputContainer);
		namesEl.setElementCssClass("o_sel_user_import");
		namesEl.setExampleKey ("form.names.example", null);
		namesEl.setLineNumbersEnbaled(true);
		namesEl.setStripedBackgroundEnabled(true);
		namesEl.setFixedFontWidth(true);
		namesEl.setOriginalLineBreaks(true);
		namesEl.setFocus(true);
		
		// table for duplicates
		String page = velocity_root + "/warn_duplicates.html";
		tableContainer = FormLayoutContainer.createCustomFormLayout("table", getTranslator(), page);
		formLayout.add(tableContainer);
		tableContainer.setVisible(false);
		
		// user search form
		backLink = uifactory.addFormLink("back", tableContainer);
		backLink.setIconLeftCSS("o_icon o_icon_back");
		
		//add the table
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colIndex = 0;
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			if(visible) {
				resultingPropertyHandlers.add(userPropertyHandler);
				tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++));
			}
		}
		
		userTableModel = new ImportMemberOverviewDataModel(resultingPropertyHandlers, getLocale(), tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "users", userTableModel, 100, false, getTranslator(), tableContainer);
		tableEl.setCustomizeColumns(false);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "user-import-table-v1");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			doBackToInput();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		String logins = namesEl.getValue();
		if(tableContainer.isVisible()) {
			Set<Identity> all = new HashSet<>(identitiesList);
			all.addAll(selectDuplicates());
			context.setIdentities(all);
			context.setNotFoundNames(notFoundNames);
			context.setRawNames(namesEl.getValue());
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if(processInput(logins)) {
			tableContainer.setVisible(true);
			inputContainer.setVisible(false);
		} else {
			context.setIdentities(identitiesList);
			context.setNotFoundNames(notFoundNames);
			context.setRawNames(namesEl.getValue());
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	private void doBackToInput() {
		tableContainer.setVisible(false);
		inputContainer.setVisible(true);
		identitiesList = null;
	}
	
	private List<Identity> selectDuplicates() {
		List<Identity> selectedIdentities = new ArrayList<>();
		for(Integer index:tableEl.getMultiSelectedIndex()) {
			Identity identity = userTableModel.getObject(index.intValue());
			if(identity != null) {
				selectedIdentities.add(identity);
			}
		}
		return selectedIdentities;
	}

	/**
	 * 
	 * @param inp The text input
	 * @return true if duplicates found
	 */
	private boolean processInput(String inp) {
		List<String> identList = getLines(inp);
		FindNamedIdentityCollection identityCollection = securityManager.findAndCollectIdentitiesBy(identList);
		
		identitiesList = identityCollection.getUnique();
		notFoundNames = identityCollection.getNotFoundNames();
		if (!identityCollection.getAmbiguousNames().isEmpty()) {
			String duplicateNames = identityCollection.getAmbiguousNames().stream()
					.collect(Collectors.joining(", "));
			tableContainer.contextPut("duplicatesMsg", translate("warn.duplicates.names", duplicateNames));
			tableContainer.setVisible(true);
			inputContainer.setVisible(false);
			
			userTableModel.setObjects(new ArrayList<>(identityCollection.getAmbiguous()));
			tableEl.reset(true, true, true);
			
			// Select previously selected identities
			Set<Integer> selectedRows = new HashSet<>();
			for(int i=userTableModel.getRowCount(); i--> 0; ) {
				Identity identity = userTableModel.getObject(i);
				if(context.getIdentities().contains(identity)) {
					selectedRows.add(Integer.valueOf(i));
				}
			}
			tableEl.setMultiSelectedIndex(selectedRows);
			
			return true;
		}
		return false;
	}
	
	private List<String> getLines(String inp) {
		List<String> identList = new ArrayList<>();
		String[] lines = inp.split("\r?\n");
		for (int i = 0; i < lines.length; i++) {
			String username = lines[i].trim();
			if(username.length() > 0) {
				identList.add(username);
			}
		}
		return identList;
	}
	
}