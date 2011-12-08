/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2008 at frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroupManagerImpl;
import org.olat.group.ui.BusinessGroupTableModel;

/**
 * Description:<br>
 * Searches for groups from the whole system.
 * 
 * <P>
 * Initial Date: 11.04.2011 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupSearchController extends StepFormBasicController {

	private TextElement search;
	private FormLink searchButton;
	private FormLayoutContainer resTable;
	private ArrayList<MultipleSelectionElement> parts;
	private ArrayList<MultipleSelectionElement> mails;
	private ArrayList<MultipleSelectionElement> owners;
	private FormItem errorComp;
	private String lastSearchValue;

	// constructor to be used like a normal FormBasicController
	public GroupSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		Translator pT = Util.createPackageTranslator(BusinessGroupTableModel.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pT);
		initForm(ureq);
	}	
	
	// constructor for use in steps-wizzard
	public GroupSearchController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext stepsRunContext, int layoutVertical, String pageName) {
		super(ureq, wControl, form, stepsRunContext, layoutVertical, pageName);
		Translator pT = Util.createPackageTranslator(BusinessGroupTableModel.class, ureq.getLocale(), getTranslator());
		this.flc.setTranslator(pT);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("group.search.description");
		
		search = uifactory.addTextElement("search.field", "search.field", 100, "", formLayout);
		searchButton = uifactory.addFormLink("search", formLayout, Link.BUTTON_SMALL);
		
		resTable = FormLayoutContainer.createCustomFormLayout("resultsTable", getTranslator(), this.velocity_root	+ "/resulttable.html");
		formLayout.add(resTable);
		resTable.contextPut("bGM", BusinessGroupManagerImpl.getInstance());
		
		if (!isUsedInStepWizzard()) uifactory.addSpacerElement("space", formLayout, false);
		errorComp = uifactory.createSimpleErrorText("error", "");
		formLayout.add(errorComp);
		if (!isUsedInStepWizzard()) uifactory.addFormSubmitButton("save", formLayout);
	}

	
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton || source == search) {
			String searchValue = search.getValue();
			doSearchGroups(searchValue, ureq);
			lastSearchValue = searchValue;
		}
	}

	/**
	 * Perform a search for the given search value in the search result providers
	 * and clear any GUI errors that might be on the page
	 * 
	 * @param searchValue
	 * @param ureq
	 */
	private void doSearchGroups(String searchValue, UserRequest ureq) {	
		if (StringHelper.containsNonWhitespace(searchValue)){
			GroupSearchResultProvider searchProvider = new GroupSearchResultProvider(ureq.getIdentity(), getLocale(), null);
			Map<String, String> resMap = new HashMap<String, String>();
			searchProvider.getAutoCompleteContent(searchValue, resMap);
			updateResultTable(resMap);
			errorComp.clearError();
		}
	} 
	
	private void updateResultTable(Map<String, String> resMap){		
		owners = new ArrayList<MultipleSelectionElement>();
		parts = new ArrayList<MultipleSelectionElement>();
		mails = new ArrayList<MultipleSelectionElement>();
		
		for (Entry<String, String> entry : resMap.entrySet()) {			
			// prepare checkboxes
			String dummyLabel = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
			MultipleSelectionElement owner = uifactory.addCheckboxesHorizontal("owner"+entry.getValue(), "", resTable, new String[]{entry.getValue()}, new String[]{dummyLabel}, new String[]{""});
			MultipleSelectionElement part = uifactory.addCheckboxesHorizontal("part"+entry.getValue(), "", resTable, new String[]{entry.getValue()}, new String[]{dummyLabel}, new String[]{""});
			MultipleSelectionElement mail = uifactory.addCheckboxesHorizontal("mail"+entry.getValue(), "", resTable, new String[]{entry.getValue()}, new String[]{dummyLabel}, new String[]{""});
			owners.add(owner);
			parts.add(part);
			mails.add(mail);			
		}		
		resTable.contextPut("resMap", resMap);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String searchValue = search.getValue();
		if ((lastSearchValue == null && searchValue != null) || (lastSearchValue != null && !lastSearchValue.equals(searchValue))) {
			// User pressed enter in input field to search for groups, no group
			// selected yet. Just search for groups that matches for this input
			doSearchGroups(searchValue, ureq);
			lastSearchValue = searchValue;
			return false;
		}
		if (isUsedInStepWizzard()) return true;
		errorComp.clearError();
		boolean result = false;
		List<String> ownerGroups = getCheckedKeys(owners);
		List<String> partGroups = getCheckedKeys(parts);
		result = (ownerGroups.size() !=0 || partGroups.size() != 0);
		if (!result) {
			errorComp.setErrorKey("error.choose.one", null);
		}
		return result;
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		List<String> ownerGroups = getCheckedKeys(owners);
		List<Long> ownLong = convertStringToLongList(ownerGroups);
		
		List<String> partGroups = getCheckedKeys(parts);
		List<Long> partLong = convertStringToLongList(partGroups);
		
		List<String> mailGroups = getCheckedKeys(mails);
		List<Long> mailLong = convertStringToLongList(mailGroups);
		
		if (isUsedInStepWizzard()){
			// might be used in wizzard during user import or user bulk change. allow next/finish according to previous steps.
			addToRunContext("ownerGroups", ownLong);
			addToRunContext("partGroups", partLong);
			addToRunContext("mailGroups", mailLong);
			boolean groupsChoosen = (ownerGroups.size() !=0 || partGroups.size() != 0);
			boolean validImport = getFromRunContext("validImport") != null && ((Boolean) getFromRunContext("validImport"));
			boolean validBulkChange = getFromRunContext("validChange") != null && ((Boolean) getFromRunContext("validChange"));
		
			boolean isValid = groupsChoosen || (validImport || validBulkChange) ;
			addToRunContext("validGroupAdd",isValid );
			//fxdiff: FXOLAT-245 notify userbulkchange-wizard about valid change
			addToRunContext("validChange",isValid );
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			fireEvent(ureq, new AddToGroupsEvent(ownLong, partLong, mailLong));
		}		
	}

	
	private List<String> getCheckedKeys(List<MultipleSelectionElement> items){
		List<String> selected = new ArrayList<String>();
		if (items == null) return selected;
		for (MultipleSelectionElement formItem : items) {
			if (formItem.isSelected(0)) {
				selected.add(formItem.getKey(0));
			}		
		}
		return selected;		
	}
	
	private List<Long> convertStringToLongList(List<String> groups) {
		List<Long> ownLong = new ArrayList<Long>();
		if (groups == null || groups.isEmpty()) return ownLong;
		for (String group : groups) {
			Long key = null;
			try {
				key = Long.parseLong(group);
			} catch (Exception e) {
				// do nothing special
			}
			if (key != null) ownLong.add(key);				
		}
		return ownLong;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}

}
