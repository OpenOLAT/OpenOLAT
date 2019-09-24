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
package org.olat.portfolio.ui.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.EPFilterSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Shows multiple filters (tag, type, fulltext, date) to filter an artefact
 * list. After choosing some of them sends an FilterChangedEvent containing the filter settings.
 * 
 * <P>
 * Initial Date: 08.07.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class PortfolioFilterController extends FormBasicController {

	public static final String HANDLER_PREFIX = "handler.";
	public static final String HANDLER_TITLE_SUFFIX = ".title";
	private static final String TAG_CMP_IDENTIFIER = "atag";
	private static final int DEFAULT_TYPE_AMOUNT = 4;
	private FormToggle tagAllBtn;
	private FormToggle typeAllBtn;
	private TextElement searchFld;
	private DateChooser dateStart;
	private DateChooser dateEnd;
	private FormLayoutContainer tagFlc;
	private FormLayoutContainer typeFlc;
	private FormLayoutContainer dateFlc;
	@Autowired
	private EPFrontendManager ePFMgr;
	@Autowired
	private PortfolioModule portfolioModule;
	private List<String> selectedTagsList;
	private ArrayList<FormToggle> tagCmpList;
	private List<String> selectedTypeList;
	private ArrayList<FormToggle> typeCmpList;
	private EPArtefactTypeSelectionController moreTypesCtlr;
	private TextElement filterName;
	private EPFilterSettings filterSettings;
	private FormToggle tagNoneBtn;
	private CloseableCalloutWindowController moreTypesCalloutCtrl;
	private Controller moreTagsCtlr;
	private CloseableCalloutWindowController moreTagsCalloutCtrl;
	private FormLink filterSave;
	private FormLayoutContainer filterFlc;
	private FormLink filterDel;
	private FormToggle tagEditBtn;
	private FormToggle typeEditBtn;
	private SingleSelection filterSel;
	private List<EPFilterSettings> nonEmptyFilters;

	public PortfolioFilterController(UserRequest ureq, WindowControl wControl, EPFilterSettings filterSettings) {
		super(ureq, wControl);
		if (filterSettings == null){
			this.filterSettings = new EPFilterSettings();
		} else {
			this.filterSettings = filterSettings;
		}
		// allows to instantiate with filter presets
		selectedTagsList = new ArrayList<>(filterSettings.getTagFilter());
		selectedTypeList = new ArrayList<>(filterSettings.getTypeFilter());
		initForm(ureq);
	}
	
	/**
	 * instantiate without existing filter settings
	 * if you want to preset filter settings, provide 3rd argument
	 * @param ureq
	 * @param wControl
	 */
	public PortfolioFilterController(UserRequest ureq, WindowControl wControl){
		this(ureq, wControl, null);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initOrUpdateForm(formLayout);
	}

	protected void initOrUpdateForm(FormItemContainer formLayout) {
		// filter by tag
		if(formLayout.getFormComponent("tagLayout") == null) {
			tagFlc = FormLayoutContainer.createCustomFormLayout("tagLayout", getTranslator(), velocity_root + "/filter-tags.html");
			formLayout.add(tagFlc);
			initTagFlc();
		} else {
			updateTagFlc();
		}

		// filter by type
		if(formLayout.getFormComponent("typeLayout") == null) {
			typeFlc = FormLayoutContainer.createCustomFormLayout("typeLayout", getTranslator(), velocity_root + "/filter-types.html");
			formLayout.add(typeFlc);		
			initOrUpdateTypeFlc(DEFAULT_TYPE_AMOUNT);
		} else {
			updateTypeFlc();
		}

		// filter by date
		if(formLayout.getFormComponent("dateLayout") == null) {
			String page = this.velocity_root + "/filter_date.html";
			dateFlc = FormLayoutContainer.createCustomFormLayout("dateLayout", getTranslator(), page);
			formLayout.add(dateFlc);
			dateFlc.setLabel("filter.by.date", null);
			dateStart = uifactory.addDateChooser("filter.date.start", null, dateFlc);
			dateStart.addActionListener(FormEvent.ONCHANGE);
			dateEnd = uifactory.addDateChooser("filter.date.end", null, dateFlc);
			dateEnd.addActionListener(FormEvent.ONCHANGE);
			dateStart.clearError();
			dateEnd.clearError();
		}

		if (filterSettings.getDateFilter().size() == 2) {
			dateStart.setDate(filterSettings.getDateFilter().get(0));
			dateEnd.setDate(filterSettings.getDateFilter().get(1));
		} else {
			dateStart.setDate(null);
			dateEnd.setDate(new Date());
		}
		
		// filter by fulltext
		String searchText = filterSettings.getTextFilter();
		if(formLayout.getFormComponent("search") == null) {
			searchFld = uifactory.addTextElement("search", "filter.search", 20, searchText, formLayout);
			searchFld.addActionListener(FormEvent.ONCHANGE);
		} else {
			searchFld.setValue(searchText);
		}
		
		// show filter list and save as new
		if(formLayout.getFormComponent("spacer") == null) {
			uifactory.addSpacerElement("spacer", formLayout, false);
		}
		
		//filter list
		initFilterList(formLayout);
		//filter save
		if(formLayout.getFormComponent("filterSaveLayout") == null) {
			String page = this.velocity_root + "/filter_search.html";
			filterFlc = FormLayoutContainer.createCustomFormLayout("filterSaveLayout", getTranslator(), page);
			formLayout.add(filterFlc);
			filterFlc.setLabel("filter.save", null);
			filterName = uifactory.addTextElement("filter.save.name", null, 30, filterSettings.getFilterName(), filterFlc);
			filterSave = uifactory.addFormLink("filter.save.link", "filter.save.new", null, filterFlc, Link.BUTTON);
			filterDel = uifactory.addFormLink("filter.del.link", "filter.del.link", null, filterFlc, Link.BUTTON);
		} else {
			filterName.setValue(filterSettings.getFilterName());
		}

		boolean isExistingFilter = filterSettings.getFilterName() != null; 
		toggleSaveUpdateFilterButtons(isExistingFilter);
	}
	
	private void updateUI(EPFilterSettings newSettings) {
		filterSettings = newSettings;
		selectedTagsList = new ArrayList<>(filterSettings.getTagFilter());
		selectedTypeList = new ArrayList<>(filterSettings.getTypeFilter());
		
		initOrUpdateForm(flc);
		flc.setDirty(true);
	}
	
	private void initFilterList(FormItemContainer formLayout) {
		List<EPFilterSettings> existingFilters = ePFMgr.getSavedFilterSettings(getIdentity());
		for(Iterator<EPFilterSettings> existingFilterIt=existingFilters.iterator(); existingFilterIt.hasNext(); ) {
			if(existingFilterIt.next().isFilterEmpty()) {
				existingFilterIt.remove();
			}
		}

		int amount = existingFilters.size() + 1;
		nonEmptyFilters = new ArrayList<>(amount);
		String[] theKeys = new String[amount];
		String[] theValues = new String[amount];
		
		String presetFilterIndex = theKeys[0] = "0";
		theValues[0] = translate("filter.all");

		int i=1;
		String presetFilterID = filterSettings.getFilterId();
		for (EPFilterSettings epFilterSettings : existingFilters) {
			theKeys[i] = epFilterSettings.getFilterId();
			theValues[i] = epFilterSettings.getFilterName();
			if (presetFilterID != null && presetFilterID.equals(epFilterSettings.getFilterId())) {
				presetFilterIndex = epFilterSettings.getFilterId(); 
			}
			nonEmptyFilters.add(epFilterSettings);
			i++;
		}
		// don't show anything if no filter exists
		if (!nonEmptyFilters.isEmpty()) {
			if(formLayout.getFormComponent("filter.select") == null) {
				filterSel = uifactory.addDropdownSingleselect("filter.select", formLayout, theKeys, theValues, null);
				filterSel.addActionListener(FormEvent.ONCHANGE);
			} else {
				filterSel.setKeysAndValues(theKeys, theValues, null);
				filterSel.setVisible(true);
			}
			if(presetFilterIndex != null) {
				filterSel.select(presetFilterIndex, true);
			}
		} else if(formLayout.getFormComponent("filter.select") != null) {
			formLayout.getFormComponent("filter.select").setVisible(false);
		}
	}

	/**
	 * 
	 */
	private void initTagFlc() {
		tagFlc.setLabel("filter.tag", null);
		tagAllBtn = uifactory.addToggleButton("filter.all", null, tagFlc, null, null);
		tagAllBtn.toggleOff();
		tagNoneBtn = uifactory.addToggleButton("filter.notag", null, tagFlc, null, null);
		Map<String, String> userTags = ePFMgr.getUsersMostUsedTags(getIdentity(), 8);
		int i = 1;
		tagCmpList = new ArrayList<>();
		LinkedList<Entry<String, String>> sortEntrySet = new LinkedList<>(userTags.entrySet());
		Collections.sort(sortEntrySet, new Comparator<Entry<String, String>>() {
			public int compare(Entry<String, String> arg0, Entry<String, String> arg1) {
				return arg0.getValue().compareTo(arg1.getValue());
			}
		});
		List<String> allActiveTagToggles = new ArrayList<>();
		for (Entry<String, String> entry : sortEntrySet) {
			String tag = entry.getValue();
			String tagText = StringHelper.escapeHtml(tag);
			String tagCmpName = TAG_CMP_IDENTIFIER + i;
			if (tagFlc.getComponent(tagCmpName)!=null) tagFlc.remove(tagCmpName);
			FormToggle link = uifactory.addToggleButton(tagCmpName, tagText, tagFlc, null, null);
			link.setLabel("tag.one", new String[] { tagText });
			link.setUserObject(tag);
			if (!selectedTagsList.isEmpty() && selectedTagsList.contains(tag)) {
				link.toggleOn();
				allActiveTagToggles.add(tag);
			} 
			tagCmpList.add(link);
			i++;
		}
		tagFlc.contextPut("tagCmpList", tagCmpList);		
		tagEditBtn = uifactory.addToggleButton("filter.edit", null, tagFlc, null, null);
		tagEditBtn.toggleOff();
		if (!allActiveTagToggles.containsAll(selectedTagsList)) tagEditBtn.toggleOn(); 
		if (selectedTagsList.isEmpty()) tagAllBtn.toggleOn();
	}
	
	private void updateTagFlc() {
		List<String> allActiveTagToggles = new ArrayList<>();
		for(FormToggle link:tagCmpList) {
			String tag = (String)link.getUserObject();
			if (selectedTagsList.contains(tag)) {
				if(!link.isOn()) {
					link.toggleOn();
				}
				allActiveTagToggles.add(tag);
			} else if(link.isOn()) {
					link.toggleOff();
			}
		}
		
		if (allActiveTagToggles.containsAll(selectedTagsList)) {
			tagEditBtn.toggleOff();
		} else {
			tagEditBtn.toggleOn(); 
		}
		if (selectedTagsList.isEmpty()) {
			tagAllBtn.toggleOn();
		} else {
			tagAllBtn.toggleOff();
		}
	}

	/**
	 * 
	 */
	private void initOrUpdateTypeFlc(int limit) {
		typeFlc.setLabel("filter.view", null);
		typeAllBtn = uifactory.addToggleButton("filter.all", null, typeFlc, null, null);
		typeAllBtn.toggleOff();
		List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
		typeCmpList = new ArrayList<>();
		int i=0;
		List<String> allActiveTypeToggles = new ArrayList<>();
		for (EPArtefactHandler<?> handler : handlers) {
			String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + HANDLER_TITLE_SUFFIX;
			FormToggle link = (FormToggle) typeFlc.getFormComponent(handlerClass);
			if (link==null) {
				Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
				typeFlc.setTranslator(handlerTrans);
				link = uifactory.addToggleButton(handlerClass, null, typeFlc, null, null);
				link.setUserObject(handler.getType());
			}	
			if (selectedTypeList.contains(handler.getType())) {
				link.toggleOn();
				allActiveTypeToggles.add(handler.getType());
			} else {
				link.toggleOff();
			}
			typeCmpList.add(link);
			i++;
			if (i>limit) break;
		}
		typeFlc.contextPut("typeCmpList", typeCmpList);
		typeEditBtn = uifactory.addToggleButton("filter.edit", null, typeFlc, null, null);
		typeEditBtn.toggleOff();
		if (!allActiveTypeToggles.containsAll(selectedTypeList)) typeEditBtn.toggleOn();
		if (selectedTypeList.isEmpty()) typeAllBtn.toggleOn();
	}
	
	private void updateTypeFlc() {
		List<String> allActiveTypeToggles = new ArrayList<>();
		for(FormToggle link:typeCmpList) {
			String type = (String)link.getUserObject();
			if (selectedTypeList.contains(type)) {
				if(!link.isOn()) {
					link.toggleOn();
				}
				allActiveTypeToggles.add(type);
			} else if(link.isOn()) {
				link.toggleOff();
			}
		}
		
		if (allActiveTypeToggles.containsAll(selectedTypeList)) {
			typeEditBtn.toggleOff();
		} else {
			typeEditBtn.toggleOn();
		}
		if (selectedTypeList.isEmpty()) {
			typeAllBtn.toggleOn();
		} else {
			typeAllBtn.toggleOff();
		}
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		// validate date-fields for correct date and start < end date
		dateStart.clearError();
		dateEnd.clearError();
		boolean isInputValid = true;
		if (dateEnd.hasError() || dateEnd.getDate() == null) {
			dateEnd.setErrorKey("filter.date.invalid", null);
			isInputValid = false;
		}
		if (StringHelper.containsNonWhitespace(dateStart.getValue()) && (dateStart.hasError() || dateStart.getDate() == null)) {
			dateStart.setErrorKey("filter.date.invalid", null);
			isInputValid = false;
		} else if (isInputValid && StringHelper.containsNonWhitespace(dateStart.getValue()) && dateStart.getDate().after(dateEnd.getDate())) {
			dateStart.setErrorKey("filter.date.invalid.afterend", null);
			isInputValid = false;
		}
		return isInputValid;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		// will catch an ENTER in text search-field
		String searchText = searchFld.getValue();
		filterSettings.setTextFilter(searchText);
		fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#formInnerEvent(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.form.flexible.FormItem,
	 *      org.olat.core.gui.components.form.flexible.impl.FormEvent)
	 */
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			if (link.getName().startsWith(TAG_CMP_IDENTIFIER)) {
				// a tag-Link got clicked, find out which
				String selectedTag = (String) link.getUserObject();
				if (!selectedTagsList.contains(selectedTag)) {
					selectedTagsList.add(selectedTag);
					tagAllBtn.toggleOff();
					tagNoneBtn.toggleOff();
				} else {
					selectedTagsList.remove(selectedTag);
					if (selectedTagsList.isEmpty()) tagAllBtn.toggleOn();
				}
				filterSettings.setTagFilter(selectedTagsList);
				fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
			} else if (link.getName().startsWith(HANDLER_PREFIX)) {
				// a type-link got clicked
				String selectedType = (String) link.getUserObject();
				if (!selectedTypeList.contains(selectedType)) {
					selectedTypeList.add(selectedType);
					typeAllBtn.toggleOff();
				} else {
					selectedTypeList.remove(selectedType);
					if (selectedTypeList.isEmpty()) typeAllBtn.toggleOn();
				}
				filterSettings.setTypeFilter(selectedTypeList);
				fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
			}

		}
		if (source == tagAllBtn) {
			resetTagLinks();
			tagNoneBtn.toggleOff();
			filterSettings.setTagFilter(selectedTagsList);
			fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
		} else if (source == tagNoneBtn) {
			resetTagLinks();
			tagAllBtn.toggleOff();
			filterSettings.setNoTagFilter();
			fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
		} else if (source == typeAllBtn) {
			resetTypeLinks();
			filterSettings.setTypeFilter(selectedTypeList);
			fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
		} else if (source == dateStart || source == dateEnd) {
			if (validateFormLogic(ureq)) {
				List<Date> dateList = new ArrayList<>();
				Date selStartDate = dateStart.getDate();
				Date selEndDate = dateEnd.getDate();
				dateList.add(selStartDate);
				dateList.add(selEndDate);
				filterSettings.setDateFilter(dateList);
				fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
			}
		} else if (source == typeEditBtn){
			popupTypesCallout(ureq);
		} else if (source == tagEditBtn){
			popupTagsCallout(ureq);			
		} else if (source == searchFld){
			String searchText = searchFld.getValue();
			filterSettings.setTextFilter(searchText);
			fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
		} else if (source == filterSave){
			if (StringHelper.containsNonWhitespace(filterName.getValue())){
				String oldFilterName = filterSettings.getFilterName();
				filterSettings.setFilterName(filterName.getValue());
				filterSettings.setTextFilter(searchFld.getValue());
				if (filterSave.getUserObject() != null && filterName.getValue().equals(oldFilterName)){
					// fake update, delete before adding again
					String filterID = (String) filterSave.getUserObject();
					ePFMgr.deleteFilterFromUsersList(getIdentity(), filterID);
				}
				if (!filterName.getValue().equals(oldFilterName) && oldFilterName != null){
					// set another id, to distinguish from old
					filterSettings.setFilterIdToUniqueId();
				}
				List<EPFilterSettings> filterList = ePFMgr.getSavedFilterSettings(getIdentity());
				// create a new or insert existing again
				filterList.add(filterSettings);					
				ePFMgr.setSavedFilterSettings(getIdentity(), filterList);
				toggleSaveUpdateFilterButtons(true);
				showInfo("filter.saved", filterSettings.getFilterName());
				updateUI(filterSettings);
			}
		} else if (source == filterDel){
			if (filterDel.getUserObject() != null) {
				ePFMgr.deleteFilterFromUsersList(getIdentity(), (String)filterDel.getUserObject());
				// refresh ui
				filterName.setValue("");
				toggleSaveUpdateFilterButtons(false);
				showInfo("filter.deleted");
				updateUI(new EPFilterSettings());
			}
		} else if (source == filterName){
			String oldFilterName = filterSettings.getFilterName();
			if (filterSave.getUserObject() != null && !filterName.getValue().equals(oldFilterName)){
				toggleSaveUpdateFilterButtons(false);
			} else if (filterName.getValue().equals(oldFilterName)){
				toggleSaveUpdateFilterButtons(true);
			}
		} else if (source == filterSel) {
			if(filterSel.isOneSelected()) {
				int filterIndex = filterSel.getSelected();
				if(filterIndex == 0) {
					EPFilterSettings newSettings = new EPFilterSettings();
					updateUI(newSettings);
					fireEvent(ureq, new PortfolioFilterChangeEvent(newSettings));
				} else if(filterIndex > 0 && filterIndex - 1 < nonEmptyFilters.size()) {
					EPFilterSettings newSettings = nonEmptyFilters.get(filterIndex - 1);
					if(!filterSettings.getFilterId().equals(newSettings.getFilterId())) {
						updateUI(newSettings);
						fireEvent(ureq, new PortfolioFilterChangeEvent(newSettings));
					}
				}
			}
		}
	}
	
	private void toggleSaveUpdateFilterButtons(boolean enableUpdateDelete){
		filterDel.setVisible(enableUpdateDelete);
		String filterID = null;
		if (enableUpdateDelete){
			filterSave.setI18nKey("filter.save.refresh");
			filterID = filterSettings.getFilterId();
		} else {
			filterSave.setI18nKey("filter.save.new");
		}
		filterDel.setUserObject(filterID);
		filterSave.setUserObject(filterID);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == moreTypesCtlr) {
			if (event == Event.CHANGED_EVENT) {
				// changed the selection in the popup
				filterSettings.setTypeFilter(selectedTypeList);
				// repaint to have correct state of the buttons
				initOrUpdateTypeFlc(DEFAULT_TYPE_AMOUNT);
				fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
			} else if (event == Event.DONE_EVENT) {
				// clicked save and close
				closeTypesCallout();
			}
		} else if (source == moreTagsCtlr) {
			if (event == Event.CHANGED_EVENT) {
				// changed the selection in the popup
				filterSettings.setTagFilter(selectedTagsList);
				// repaint to have correct state of the buttons
				initTagFlc();
				fireEvent(ureq, new PortfolioFilterChangeEvent(filterSettings));
			} else if (event == Event.DONE_EVENT) {
				closeTagsCallout();
			}
		} else if (source == moreTagsCalloutCtrl && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(moreTagsCalloutCtrl);
			moreTagsCalloutCtrl = null;
		} else if (source == moreTypesCalloutCtrl && event == CloseableCalloutWindowController.CLOSE_WINDOW_EVENT) {
			removeAsListenerAndDispose(moreTypesCalloutCtrl);
			moreTypesCalloutCtrl = null;
		}
			
	}

	private void resetTagLinks() {
		for (FormToggle link : tagCmpList) {
			link.toggleOff();
			selectedTagsList.clear();
		}
		tagEditBtn.toggleOff();
	}

	private void resetTypeLinks() {
		for (FormToggle link : typeCmpList) {
			link.toggleOff();
			selectedTypeList.clear();
		}
		typeEditBtn.toggleOff();
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
		// nothing
	}
	
	private void popupTypesCallout(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(moreTypesCtlr);
		moreTypesCtlr = new EPArtefactTypeSelectionController(ureq, getWindowControl(), selectedTypeList);
		listenTo(moreTypesCtlr);
		
		removeAsListenerAndDispose(moreTypesCalloutCtrl);
		moreTypesCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), moreTypesCtlr.getInitialComponent(), (FormLink)typeEditBtn, title, true, null);
		listenTo(moreTypesCalloutCtrl);
		moreTypesCalloutCtrl.activate();		
	}

	private void popupTagsCallout(UserRequest ureq) {
		String title = translate("filter.tag");
		removeAsListenerAndDispose(moreTagsCtlr);
		moreTagsCtlr = new EPArtefactTagSelectionController(ureq, getWindowControl(), selectedTagsList);
		listenTo(moreTagsCtlr);
		
		removeAsListenerAndDispose(moreTagsCalloutCtrl);
		moreTagsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), moreTagsCtlr.getInitialComponent(), (FormLink)tagEditBtn, title, true, null);
		listenTo(moreTagsCalloutCtrl);
		moreTagsCalloutCtrl.activate();
	}
	
	
	private void closeTypesCallout() {
		if (moreTypesCalloutCtrl != null){
			moreTypesCalloutCtrl.deactivate();
			removeAsListenerAndDispose(moreTypesCalloutCtrl);
			moreTypesCalloutCtrl = null;
		}
	}
	
	private void closeTagsCallout() {
		if (moreTagsCalloutCtrl != null){
			moreTagsCalloutCtrl.deactivate();
			removeAsListenerAndDispose(moreTagsCalloutCtrl);
			moreTagsCalloutCtrl = null;
		}
	}

}
