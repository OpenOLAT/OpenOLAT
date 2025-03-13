/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerTableModel.ElementCols;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.author.ACRenderer;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.provider.free.FreeAccessHandler;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceAccessHandler;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutAccessHandler;
import org.olat.resource.accesscontrol.provider.token.TokenAccessHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Mar 12, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementSelectionController extends FormBasicController {
	
	private static final String ALL_TAB_ID = "All";
	
	private FlexiTableElement tableEl;
	private CurriculumComposerTableModel tableModel;
	
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private AccessControlModule acModule;

	public CurriculumElementSelectionController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initFormTable(formLayout, ureq);
		initFilters();
		initFiltersPresets(ureq);
	}
	
	private void initFormTable(FormItemContainer formLayout, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel( ElementCols.curriculum));
		DateWithDayFlexiCellRenderer dateRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.status,
				new CurriculumStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.offers, new ACRenderer()));

		tableModel = new CurriculumComposerTableModel(columnsModel, true, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_curriculum_el_listing");
		tableEl.setEmptyTableSettings("table.curriculum.element.empty", null, "o_icon_curriculum_element");
		tableEl.setSearchEnabled(true);
		tableEl.setMultiSelect(true);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("select", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		List<Curriculum> curriculums = curriculumService.getCurriculums(searchParams);
		if(!curriculums.isEmpty()) {
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum cur:curriculums) {
				String key = cur.getKey().toString();
				String value = StringHelper.escapeHtml(cur.getDisplayName());
				if(StringHelper.containsNonWhitespace(cur.getIdentifier())) {
					value += " <small class=\"mute\"> \u00B7 " + StringHelper.escapeHtml(cur.getIdentifier()) + "</small>";
				}
				curriculumValues.add(SelectionValues.entry(key, value));
			}
			
			FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					CurriculumComposerController.FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
		}
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.deleted.name(), translate("filter.deleted")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				CurriculumComposerController.FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		List<CurriculumElementType> types = curriculumService.getCurriculumElementTypes();
		SelectionValues typesValues = new SelectionValues();
		for(CurriculumElementType type:types) {
			typesValues.add(SelectionValues.entry(type.getKey().toString(), type.getDisplayName()));
		}
		FlexiTableMultiSelectionFilter typeFilter = new FlexiTableMultiSelectionFilter(translate("filter.types"),
				CurriculumComposerController.FILTER_TYPE, typesValues, true);
		filters.add(typeFilter);
		
		SelectionValues offerSV = new SelectionValues();
		offerSV.add(SelectionValues.entry("offer", translate("filter.with.offer")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.with.offer"),
				CurriculumComposerController.FILTER_OFFER, offerSV, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		Map<String,FlexiFiltersTab> map = new HashMap<>();
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setFiltersExpanded(true);
		tabs.add(allTab);
		map.put(ALL_TAB_ID.toLowerCase(), allTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	void loadModel() {
		CurriculumElementInfosSearchParams searchParams = getSearchParams();
		
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		
		// Should be move to getCurriculumElementsWithInfos().
		List<OLATResource> resources = elements.stream().map(info -> info.curriculumElement().getResource()).toList();
		Map<OLATResource, List<OLATResourceAccess>> resourceToResourceAccess = catalogService.getResourceToResourceAccess(resources, null);
		
		List<CurriculumElementRow> rows = new ArrayList<>(elements.size());
		Map<Long, CurriculumElementRow> keyToRows = new HashMap<>();
		for(CurriculumElementInfos element:elements) {
			CurriculumElementRow row = forgeRow(element);
			forgeResourceAccess(row, resourceToResourceAccess.get(element.curriculumElement().getResource()));
			rows.add(row);
			keyToRows.put(element.getKey(), row);
		}
		//parent line
		for(CurriculumElementRow row:rows) {
			if(row.getParentKey() != null) {
				row.setParent(keyToRows.get(row.getParentKey()));
			}
		}
		Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		// Don't reload the data, only reset paging and number of rows
		tableEl.reset(true, true, false);
	}
	
	private CurriculumElementInfosSearchParams getSearchParams() {
		CurriculumElementInfosSearchParams searchParams = new CurriculumElementInfosSearchParams(getIdentity());
		searchParams.setRootElementsOnly(true);
		return searchParams;
	}
	
	private CurriculumElementRow forgeRow(CurriculumElementInfos element) {
		long refs = element.numOfResources() + element.numOfLectureBlocks();
		CurriculumElementRow row = new CurriculumElementRow(element.curriculumElement(), refs,
				element.numOfParticipants(), element.numOfCoaches(), element.numOfOwners(),
				element.numOfCurriculumElementOwners(), element.numOfMasterChoaches(), element.numOfPending(),
				null, null, null);
		
		return row;
	}

	private void forgeResourceAccess(CurriculumElementRow row, List<OLATResourceAccess> resourceAccesses) {
		if (resourceAccesses == null || resourceAccesses.isEmpty()) {
			return;
		}
		
		// One icon per type
		Set<String> methodTypes = new HashSet<>(2);
		Set<AccessMethod> methods = new HashSet<>(2);
		for (OLATResourceAccess resourceAccess : resourceAccesses) {
			for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
				String type = bundle.getMethod().getType();
				if (!methodTypes.contains(type)) {
					methodTypes.add(type);
					methods.add(bundle.getMethod());
				}
			}
		}
		
		if (!methods.isEmpty()) {
			List<PriceMethod> priceMethods = methods.stream()
					.sorted(new AccessMethodComparator())
					.map(this::toPriceMethod)
					.toList();
			row.setAccessPriceMethods(priceMethods);
		}
	}
	
	private PriceMethod toPriceMethod(AccessMethod method) {
		String type = method.getMethodCssClass() + "_icon";
		AccessMethodHandler amh = acModule.getAccessMethodHandler(method.getType());
		String displayName = amh.getMethodName(getLocale());
		return new PriceMethod(null, type, displayName);
	}
	
	private static final class AccessMethodComparator implements Comparator<AccessMethod> {
		
		@Override
		public int compare(AccessMethod o1, AccessMethod o2) {
			return Integer.compare(getMethodSortValue(o1), getMethodSortValue(o2));
		}
		
		private int getMethodSortValue(AccessMethod method) {
			return switch (method.getType()) {
			case FreeAccessHandler.METHOD_TYPE -> 1;
			case TokenAccessHandler.METHOD_TYPE -> 2;
			case PaypalCheckoutAccessHandler.METHOD_TYPE -> 3;
			case InvoiceAccessHandler.METHOD_TYPE -> 4;
			default -> 10;
			};
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public List<CurriculumElement> getSelectedElements() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.filter(Objects::nonNull)
				.map(CurriculumElementRow::getCurriculumElement)
				.filter(Objects::nonNull)
				.toList();
	}

}
