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
package org.olat.resource.accesscontrol.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.curriculum.ui.member.MemberDetailsConfig;
import org.olat.modules.curriculum.ui.member.MemberDetailsController;
import org.olat.repository.ui.list.ImplementationEvent;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.ui.PendingMembershipsTableModel.PendingMembershipCol;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-09-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsController extends FormBasicController implements FlexiTableComponentDelegate {

	private static final String ROW_SELECT_ACTION = "select.row";

	private final Identity identity;
	private FlexiTableElement tableEl;
	private PendingMembershipsTableModel tableModel;
	private final VelocityContainer detailsVC;
	private FlexiFiltersTab allTab;

	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private ACReservationDAO reservationDao;

	public PendingMembershipsController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, "pending_memberships");
		this.identity = identity;

		detailsVC = createVelocityContainer("pending_membership_details");

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.title, ROW_SELECT_ACTION));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.extRef, ROW_SELECT_ACTION));
		DateWithDayFlexiCellRenderer dateWithDayFlexiCellRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.begin, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.end, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.type));
		DateFlexiCellRenderer dateCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipCol.confirmationUntil, dateCellRenderer));
		
		tableModel = new  PendingMembershipsTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		//tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setSearchEnabled(true);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
		
		initFiltersPreset(ureq);
	}

	private void initFiltersPreset(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();

		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("all", translate("filter.all"), 
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof DetailsToggleEvent detailsToggleEvent) {
				PendingMembershipRow row = tableModel.getObject(detailsToggleEvent.getRowIndex());
				if (detailsToggleEvent.isVisible()) {
					doOpenDetails(ureq, row);
				} else {
					doCloseDetails(row);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof MemberDetailsController detailsCtrl) {
			if (detailsCtrl.getUserObject() instanceof PendingMembershipRow row) {
				if (event instanceof ImplementationEvent) {
					doLearnMoreAboutImplementation(ureq, row);
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}
	
	private void loadModel() {
		List<ResourceReservation> reservations = reservationDao.loadReservations(identity);
		Set<OLATResource> resources = reservations.stream().map(ResourceReservation::getResource).collect(Collectors.toSet());
		List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(resources);
		Map<Long, CurriculumElement> resourceKeyToElement = new HashMap<>();
		for (CurriculumElement curriculumElement : curriculumElements) {
			resourceKeyToElement.put(curriculumElement.getResource().getKey(), curriculumElement);
		}
		String searchString = tableEl.getQuickSearchString() != null ? tableEl.getQuickSearchString().toLowerCase() : null;
		List<PendingMembershipRow> rows = reservations.stream().map(r -> toRow(r, resourceKeyToElement))
				.filter(r -> {
					if (!StringHelper.containsNonWhitespace(searchString)) {
						return true;
					}
					if (StringHelper.containsNonWhitespace(r.getTitle()) && r.getTitle().toLowerCase().contains(searchString)) {
						return true;
					}
					if (StringHelper.containsNonWhitespace(r.getExtRef()) && r.getExtRef().toLowerCase().contains(searchString)) {
						return true;
					}
					return false;
				})
				.toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private PendingMembershipRow toRow(ResourceReservation reservation, Map<Long, CurriculumElement> resourceKeyToElement) {
		CurriculumElement curriculumElement = resourceKeyToElement.get(reservation.getResource().getKey());
		return new PendingMembershipRow(curriculumElement.getDisplayName(), curriculumElement.getIdentifier(),
				curriculumElement.getBeginDate(), curriculumElement.getEndDate(), 
				curriculumElement.getType() != null ? curriculumElement.getType().getDisplayName() : "",
				reservation.getExpirationDate(), curriculumElement.getKey(), reservation.getKey());
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>();
		if (rowObject instanceof PendingMembershipRow pendingMembershipRow && 
				pendingMembershipRow.getDetailsController() != null) {
			components.add(pendingMembershipRow.getDetailsController().getInitialFormItem().getComponent());
		};
		return components;
	}
	
	private void doOpenDetails(UserRequest ureq, PendingMembershipRow row) {
		if (row == null) {
			return;
		}
		
		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}

		CurriculumElement curriculumElement = curriculumElementDao.loadByKey(row.getCurriculumElementKey());
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(curriculumElement);
		List<CurriculumElement> elements = new ArrayList<>(descendants);
		elements.add(curriculumElement);
		
		MemberDetailsConfig detailsConfig = new MemberDetailsConfig(null, null, false, true, false, true, 
				true, true, true, true, true);
		
		MemberDetailsController detailsCtrl = new MemberDetailsController(ureq, getWindowControl(), mainForm,
				curriculumElement.getCurriculum(), curriculumElement, elements, identity, detailsConfig);
		detailsCtrl.setUserObject(row);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	private void doCloseDetails(PendingMembershipRow row) {
		if (row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
			row.setDetailsController(null);
		}
	}
	
	private void doLearnMoreAboutImplementation(UserRequest ureq, PendingMembershipRow row) {
		//
	}
}
