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
package org.olat.modules.coach.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.util.StringHelper;
import org.olat.modules.coach.security.PendingCourseBookingsRightProvider;
import org.olat.modules.coach.ui.PendingMembershipsTableModel.PendingMembershipsCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.model.UserResourceReservation;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-10-27<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PendingMembershipsController extends FormBasicController {
	public static final String USER_PROPS_ID = UserListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;

	private static final String ROW_SELECT_ACTION = "select.row";

	private FlexiTableElement tableEl;
	private PendingMembershipsTableModel tableModel;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private ACOrderDAO acOrderDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;

	public PendingMembershipsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "pending_memberships");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID, userPropertyHandler);
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, 
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", true,
					userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.title, ROW_SELECT_ACTION));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.extRef, ROW_SELECT_ACTION));
		DateWithDayFlexiCellRenderer dateWithDayFlexiCellRenderer = new DateWithDayFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.begin, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.end, dateWithDayFlexiCellRenderer));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.type));
		DateFlexiCellRenderer dateFlexiCellRenderer = new DateFlexiCellRenderer(getLocale());
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PendingMembershipsCols.confirmationUntil, dateFlexiCellRenderer));
		
		tableModel = new PendingMembershipsTableModel(userManager, userPropertyHandlers, columnModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list", tableModel, 
				25, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<UserResourceReservation> userResourceReservations = acOrderDao.getReservationsWithOrders(getIdentity(), 
				PendingCourseBookingsRightProvider.RELATION_RIGHT, userPropertyHandlers).stream()
				.filter(urr -> {
					String type = urr.getResourceReservation().getType();
					return StringHelper.containsNonWhitespace(type) && type.startsWith(CurriculumService.RESERVATION_PREFIX);
				}).toList();
		Set<OLATResource> resources = userResourceReservations.stream()
				.map(UserResourceReservation::getResourceReservation)
				.map(ResourceReservation::getResource).collect(Collectors.toSet());
		List<CurriculumElement> curriculumElements = curriculumElementDao.loadElementsByResources(resources);
		Map<Long, CurriculumElement> resourceKeyToElement = new HashMap<>();
		for (CurriculumElement curriculumElement : curriculumElements) {
			resourceKeyToElement.put(curriculumElement.getResource().getKey(), curriculumElement);
		}

		List<PendingMembershipRow> rows = userResourceReservations.stream().map(urr -> toRow(urr, resourceKeyToElement))
						.filter(Objects::nonNull).toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private PendingMembershipRow toRow(UserResourceReservation urr, Map<Long, CurriculumElement> resourceKeyToElement) {
		CurriculumElement curriculumElement = resourceKeyToElement.get(urr.getResourceReservation().getResource().getKey());
		if (curriculumElement == null) {
			return null;
		}
		if (!curriculumElement.getType().isAllowedAsRootElement()) {
			return null;
		}
		String extRef = curriculumElement.getIdentifier();
		PendingMembershipRow row = new PendingMembershipRow(urr.getResourceReservation().getIdentity().getKey(),
				curriculumElement.getDisplayName(), extRef, curriculumElement.getBeginDate(),
				curriculumElement.getEndDate(), 
				curriculumElement.getType() != null ? curriculumElement.getType().getDisplayName() : "",
				urr.getResourceReservation().getExpirationDate(),
				urr.getResourceReservation(),
				userPropertyHandlers, urr.getIdentityProps().toArray(new String[0]), getLocale());
		return row;		
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
