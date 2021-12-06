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
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BusinessGroupFormController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Searches for groups from the whole system. The list of
 * groups doesn't contain any managed groups with the flags
 * "membermanagement" and higher.
 * 
 * <P>
 * Initial Date: 11.04.2011 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupSearchController extends StepFormBasicController {

	private TextElement search;
	private FormSubmit searchButton;
	private FormLink saveLink;
	private FormLink searchLink;
	private FormItem errorComp;
	private FlexiTableElement table;
	private FormLayoutContainer tableCont;
	private GroupTableDataModel tableDataModel;
	
	private GroupChanges groupChanges;
	
	private String lastSearchValue;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	private boolean finishByFinish;

	// constructor to be used like a normal FormBasicController
	public GroupSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		Translator pT = Util.createPackageTranslator(BusinessGroupFormController.class, ureq.getLocale(), getTranslator());
		flc.setTranslator(pT);
		initForm(ureq);
	}	
	
	// constructor for use in steps-wizzard
	public GroupSearchController(UserRequest ureq, WindowControl wControl, Form form, StepsRunContext stepsRunContext, GroupChanges groupChanges, boolean finishByFinish) {
		super(ureq, wControl, form, stepsRunContext, LAYOUT_VERTICAL, "resulttable");
		Translator pT = Util.createPackageTranslator(BusinessGroupFormController.class, ureq.getLocale(), getTranslator());
		this.finishByFinish = finishByFinish;
		this.groupChanges = groupChanges;
		flc.setTranslator(pT);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("group.search.description");
		formLayout.setElementCssClass("o_sel_groups_search");

		search = uifactory.addTextElement("search.field", "search.field", 100, "", formLayout);
		
		if (isUsedInStepWizzard()) {
			searchLink = uifactory.addFormLink("search", formLayout, Link.BUTTON);
		} else {
			searchButton = uifactory.addFormSubmitButton("search", formLayout);
			uifactory.addSpacerElement("space", formLayout, false);
		}
		
		errorComp = uifactory.createSimpleErrorText("error", "");
		formLayout.add(errorComp);

		tableCont = FormLayoutContainer.createCustomFormLayout("", getTranslator(), velocity_root + "/resulttable.html");
		tableCont.setRootForm(mainForm);
		formLayout.add(tableCont);

		//group rights
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.key));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.groupName));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.description, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.courses));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.tutor));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.participant));
		
		tableDataModel = new GroupTableDataModel(Collections.<GroupWrapper>emptyList(), tableColumnModel);
		table = uifactory.addTableElement(getWindowControl(), "groupList", tableDataModel, getTranslator(), tableCont);
		table.setCustomizeColumns(false);
		tableCont.add("groupList", table);
		
		if (!isUsedInStepWizzard()) {
			saveLink = uifactory.addFormLink("save", formLayout, Link.BUTTON);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSearchGroups();
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		doSave(ureq);
	}

	@Override
	protected void formFinish(UserRequest ureq) {
		if(finishByFinish) {
			doSave(ureq);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == searchButton || source == searchLink || source == search) {
			doSearchGroups();
		} else if(source == saveLink) {
			if(validateFormLogic(ureq)) {
				doSave(ureq);
			}
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doSearchGroups() {
		String searchValue = search.getValue();
		doSearchGroups(searchValue);
		lastSearchValue = searchValue;
	}

	/**
	 * Perform a search for the given search value in the search result providers
	 * and clear any GUI errors that might be on the page
	 * 
	 * @param searchValue
	 * @param ureq
	 */
	private void doSearchGroups(String searchValue) {	
		if (StringHelper.containsNonWhitespace(searchValue)) {
			SearchBusinessGroupParams param1s = new SearchBusinessGroupParams();
			param1s.setNameOrDesc(searchValue);
			Set<BusinessGroup> dedupGroups = new HashSet<>();
			List<BusinessGroup> group1s = businessGroupService.findBusinessGroups(param1s, null, 0, -1);
			filterGroups(group1s, dedupGroups);
			
			SearchBusinessGroupParams param2s = new SearchBusinessGroupParams();
			param2s.setCourseTitle(searchValue);
			List<BusinessGroup> group2s = businessGroupService.findBusinessGroups(param2s, null, 0, -1);
			filterGroups(group2s, dedupGroups);
			
			List<BusinessGroup> groups = new ArrayList<>(group1s.size() + group2s.size());
			groups.addAll(group1s);
			groups.addAll(group2s);
			
			List<Long> groupKeysWithRelations = PersistenceHelper.toKeys(groups);
			List<BGRepositoryEntryRelation> resources = businessGroupService.findRelationToRepositoryEntries(groupKeysWithRelations, 0, -1);

			List<GroupWrapper> groupWrappers = new ArrayList<>();
			for(BusinessGroup group:groups) {
				StringBuilder sb = new StringBuilder();
				for(BGRepositoryEntryRelation resource:resources) {
					if(resource.getGroupKey().equals(group.getKey())) {
						if(sb.length() > 0) sb.append(", ");
						sb.append(resource.getRepositoryEntryDisplayName());
					}
				}

				GroupWrapper wrapper = new GroupWrapper(group, sb.toString());
				wrapper.setTutor(createSelection("tutor_" + group.getKey()));
				wrapper.setParticipant(createSelection("participant_" + group.getKey()));
				groupWrappers.add(wrapper);
			}

			table.reset();
			tableDataModel.setObjects(groupWrappers);
			errorComp.clearError();
		}
	}
	
	private void filterGroups(List<BusinessGroup> groups, Set<BusinessGroup> dedupGroups) {
		for(Iterator<BusinessGroup> groupIt=groups.iterator(); groupIt.hasNext(); ) {
			BusinessGroup group = groupIt.next();
			if(BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.membersmanagement)) {
				groupIt.remove();
			} else if(dedupGroups.contains(group)) {
				groupIt.remove();
			} else {
				dedupGroups.add(group);
			}
		}
	}
	
	private MultipleSelectionElement createSelection(String name) {
		MultipleSelectionElement selection = new MultipleSelectionElementImpl(name, Layout.horizontal);
		selection.setKeysAndValues(new String[]{"on"}, new String[]{""});
		tableCont.add(name, selection);
		return selection;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String searchValue = search.getValue();

		if (isUsedInStepWizzard()) {
			return true;
		}
		if ((lastSearchValue == null && StringHelper.containsNonWhitespace(searchValue))
				|| (lastSearchValue != null && !lastSearchValue.equals(searchValue))) {
			// User pressed enter in input field to search for groups, no group
			// selected yet. Just search for groups that matches for this input
			doSearchGroups(searchValue);
			lastSearchValue = searchValue;
			return false;
		}
		errorComp.clearError();
	
		List<Long> ownerGroups = getCheckedTutorKeys();
		List<Long> partGroups = getCheckedParticipantKeys();
		boolean result = !ownerGroups.isEmpty() || !partGroups.isEmpty();
		if (!result) {
			errorComp.setErrorKey("error.choose.one", null);
		}
		return result;
	}

	private void doSave(UserRequest ureq) {
		List<Long> ownerGroups = getCheckedTutorKeys();
		List<Long> partGroups = getCheckedParticipantKeys();
		
		if (isUsedInStepWizzard()) {
			boolean groupsChoosen = !ownerGroups.isEmpty() || !partGroups.isEmpty();
			// might be used in wizzard during user import or user bulk change. allow next/finish according to previous steps.
			if(groupChanges != null) {
				groupChanges.setOwnerGroups(ownerGroups);
				groupChanges.setParticipantGroups(partGroups);
				
				boolean validImport = getFromRunContext("validImport") != null && ((Boolean) getFromRunContext("validImport"));
				boolean isValid = groupsChoosen || (validImport || groupChanges.isValidChange()) ;
				addToRunContext("validGroupAdd",isValid );
				groupChanges.setValidChange(isValid);	
			} else {
				addToRunContext("ownerGroups", ownerGroups);
				addToRunContext("partGroups", partGroups);
				
				boolean validImport = getFromRunContext("validImport") != null && ((Boolean) getFromRunContext("validImport"));
				boolean validBulkChange = getFromRunContext("validChange") != null && ((Boolean) getFromRunContext("validChange"));
			
				boolean isValid = groupsChoosen || (validImport || validBulkChange) ;
				addToRunContext("validGroupAdd",isValid );
				addToRunContext("validChange",isValid );
			}
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			fireEvent(ureq, new AddToGroupsEvent(ownerGroups, partGroups));
		}	
	}

	private List<Long> getCheckedTutorKeys() {
		List<Long> selected = new ArrayList<>();
		for(GroupWrapper wrapper:tableDataModel.getObjects()) {
			if(wrapper.getTutor().isSelected(0)) {
				selected.add(wrapper.getGroupKey());
			}
		}
		return selected;		
	}
	
	private List<Long> getCheckedParticipantKeys() {
		List<Long> selected = new ArrayList<>();
		for(GroupWrapper wrapper:tableDataModel.getObjects()) {
			if(wrapper.getParticipant().isSelected(0)) {
				selected.add(wrapper.getGroupKey());
			}
		}
		return selected;		
	}
	
	private static class GroupWrapper {
		private final Long groupKey;
		private final String groupName;
		private final String description;
		private final String courses;
		
		private MultipleSelectionElement tutor;
		private MultipleSelectionElement participant;
		
		public GroupWrapper(BusinessGroup group, String courses) {
			groupKey = group.getKey();
			groupName = group.getName();
			description = group.getDescription();
			this.courses = courses;
		}

		public Long getGroupKey() {
			return groupKey;
		}
		
		public String getGroupName() {
			return groupName;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getCourses() {
			return courses;
		}

		public MultipleSelectionElement getTutor() {
			return tutor;
		}
		
		public void setTutor(MultipleSelectionElement tutor) {
			this.tutor = tutor;
		}
		
		public MultipleSelectionElement getParticipant() {
			return participant;
		}
		
		public void setParticipant(MultipleSelectionElement participant) {
			this.participant = participant;
		}
	}
	
	private static class GroupTableDataModel extends DefaultFlexiTableDataModel<GroupWrapper> {

		public GroupTableDataModel(List<GroupWrapper> options, FlexiTableColumnModel columnModel) {
			super(options, columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			GroupWrapper option = getObject(row);
			switch(Cols.values()[col]) {
				case key: return option.getGroupKey();
				case groupName: return option.getGroupName();
				case description: 
					String description = option.getDescription();
					description = FilterFactory.getHtmlTagsFilter().filter(description);
					description = Formatter.truncate(description, 256);
					return description;
				case courses: return option.getCourses();
				case tutor: return option.getTutor();
				case participant: return option.getParticipant();
				default: return option;
			}
		}
	}
	
	public static enum Cols implements FlexiColumnDef {
		key("table.group.key"),
		groupName("table.group.name"),
		description("description"),
		courses("table.header.resources"),
		tutor("table.group.add.tutor"),
		participant("table.group.add.participant");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18n;
		}
	}
}
