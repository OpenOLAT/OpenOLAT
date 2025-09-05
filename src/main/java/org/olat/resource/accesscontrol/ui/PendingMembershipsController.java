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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateWithDayFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.ui.PendingMembershipsTableModel.PendingMembershipCol;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-09-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsController extends FormBasicController {

	private static final String ROW_SELECT_ACTION = "select.row";

	private final Identity identity;
	private FlexiTableElement tableEl;
	private PendingMembershipsTableModel tableModel;
	
	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	
	public PendingMembershipsController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.identity = identity;
		
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
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}
	
	private void loadModel() {
		List<ResourceReservation> reservations = acService.getReservations(identity);
		Set<OLATResource> resources = reservations.stream().map(ResourceReservation::getResource).collect(Collectors.toSet());
		List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(resources);
		Map<Long, CurriculumElement> resourceKeyToElement = new HashMap<>();
		for (CurriculumElement curriculumElement : curriculumElements) {
			resourceKeyToElement.put(curriculumElement.getResource().getKey(), curriculumElement);
		}
		List<PendingMembershipRow> rows = reservations.stream().map(r -> toRow(r, resourceKeyToElement)).toList();
		tableModel.setObjects(rows);
		tableEl.reset();
	}

	private PendingMembershipRow toRow(ResourceReservation reservation, Map<Long, CurriculumElement> resourceKeyToElement) {
		CurriculumElement curriculumElement = resourceKeyToElement.get(reservation.getResource().getKey());
		return new PendingMembershipRow(curriculumElement.getDisplayName(), curriculumElement.getIdentifier(),
				curriculumElement.getBeginDate(), curriculumElement.getEndDate(), 
				curriculumElement.getType() != null ? curriculumElement.getType().getDisplayName() : "",
				reservation.getExpirationDate());
	}
}
