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
package org.olat.modules.coach.reports;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.model.UserOrder;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBookingOrdersReportConfiguration extends TimeBoundReportConfiguration {

	@Override
	protected String getI18nCategoryKey() {
		return "report.category.bookingOrders";
	}

	@Override
	public int generateCustomHeaderColumns(OpenXMLWorksheet.Row header, int pos, Translator translator) {
		header.addCell(pos++, translator.translate("export.header.creationDate"));
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers) {
		ACOrderDAO orderDao = CoreSpringFactory.getImpl(ACOrderDAO.class);
		List<UserOrder> bookings = orderDao.getUserBookingsForOrganizations(coach, OrganisationRoles.educationmanager, userPropertyHandlers);
		for (UserOrder booking : bookings) {
			generateDataRow(workbook, sheet, userPropertyHandlers, booking);
		}
	}

	private void generateDataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, UserOrder booking) {
		OpenXMLWorksheet.Row row = sheet.newRow();
		int pos = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, booking.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos, "" + booking.getOrder().getCreationDate(), workbook.getStyles().getDateTimeStyle());
	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class).getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);
	}
}
