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

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.resource.accesscontrol.manager.ACOrderDAO;
import org.olat.resource.accesscontrol.manager.BookingOrdersSearchParams;
import org.olat.resource.accesscontrol.model.UserOrder;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-01-29<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBookingOrdersReportConfiguration extends TimeBoundReportConfiguration {

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		return secCallback.isCoachingContext() && secCallback.isShowInvoicesReports();
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.category.bookingOrders";
	}

	@Override
	public int generateCustomHeaderColumns(OpenXMLWorksheet.Row header, int pos, Translator translator) {
		header.addCell(pos++, translator.translate("export.header.booking.number"));
		header.addCell(pos++, translator.translate("export.header.booking.status"));
		header.addCell(pos++, translator.translate("export.header.offer"));
		header.addCell(pos++, translator.translate("export.header.offer.type"));
		header.addCell(pos++, translator.translate("export.header.cost.center"));
		header.addCell(pos++, translator.translate("export.header.account"));
		header.addCell(pos++, translator.translate("export.header.po.number"));
		header.addCell(pos++, translator.translate("export.header.order.comment"));
		header.addCell(pos++, translator.translate("export.header.order.date"));
		header.addCell(pos++, translator.translate("export.header.price"));
		header.addCell(pos++, translator.translate("export.header.cancellation.fee"));
		header.addCell(pos++, translator.translate("export.header.billing.address"));
		header.addCell(pos++, translator.translate("export.header.name.company"));
		header.addCell(pos++, translator.translate("export.header.addition"));
		header.addCell(pos++, translator.translate("export.header.address.line", "1"));
		header.addCell(pos++, translator.translate("export.header.address.line", "2"));
		header.addCell(pos++, translator.translate("export.header.address.line", "3"));
		header.addCell(pos++, translator.translate("export.header.address.line", "4"));
		header.addCell(pos++, translator.translate("export.header.po.box"));
		header.addCell(pos++, translator.translate("export.header.region"));
		header.addCell(pos++, translator.translate("export.header.zip"));
		header.addCell(pos++, translator.translate("export.header.city"));
		header.addCell(pos++, translator.translate("export.header.country"));
		header.addCell(pos++, translator.translate("export.header.billing.address.org.id"));
		header.addCell(pos++, translator.translate("export.header.billing.address.org.name"));
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		List<UserOrder> bookings = loadBookings(coach, userPropertyHandlers);
		for (UserOrder booking : bookings) {
			generateDataRow(workbook, sheet, userPropertyHandlers, booking);
		}
	}
	
	private List<UserOrder> loadBookings(Identity coach, List<UserPropertyHandler> userPropertyHandlers) {
		ACOrderDAO orderDao = CoreSpringFactory.getImpl(ACOrderDAO.class);
		
		BookingOrdersSearchParams orgParams = new BookingOrdersSearchParams();
		orgParams.setIdentity(coach);
		setDateRange(orgParams);
		orgParams.setOrganisationRoles(List.of(OrganisationRoles.educationmanager, OrganisationRoles.linemanager));
		List<UserOrder> orgOrders = orderDao.getUserBookings(orgParams, userPropertyHandlers);
		
		BookingOrdersSearchParams groupParams = new BookingOrdersSearchParams();
		groupParams.setIdentity(coach);
		setDateRange(groupParams);
		groupParams.setGroupRoles(List.of(GroupRoles.coach));
		List<UserOrder> groupOrders = orderDao.getUserBookings(groupParams, userPropertyHandlers);
		
		Set<UserOrder> userOrders = Stream.concat(orgOrders.stream(), groupOrders.stream()).collect(Collectors.toSet());
		return userOrders.stream()
				.sorted(Comparator.comparing(o -> o.getOrder().getCreationDate())).collect(Collectors.toList());
	}

	private void setDateRange(BookingOrdersSearchParams params) {
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			params.setFromDate(getDurationTimeUnit().fromDate(new Date(), duration));
			params.setToDate(getDurationTimeUnit().toDate(new Date()));
		}
	}

	private void generateDataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet, List<UserPropertyHandler> userPropertyHandlers, UserOrder bookingOrder) {
		OpenXMLWorksheet.Row row = sheet.newRow();
		int pos = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, bookingOrder.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, "" + bookingOrder.getOrder().getKey());
		row.addCell(pos++, bookingOrder.getOrder().getOrderStatus().name());
		row.addCell(pos++, bookingOrder.getOfferName());
		row.addCell(pos++, bookingOrder.getOfferType());
		row.addCell(pos++, bookingOrder.getOfferCostCenter());
		row.addCell(pos++, bookingOrder.getOfferAccount());
		row.addCell(pos++, bookingOrder.getOrder().getPurchaseOrderNumber());
		row.addCell(pos++, bookingOrder.getOrder().getComment());
		row.addCell(pos++, "" + bookingOrder.getOrder().getCreationDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(pos++, PriceFormat.fullFormat(bookingOrder.getOrder().getTotal()));
		row.addCell(pos++, PriceFormat.fullFormat(bookingOrder.getOrder().getCancellationFees()));
		row.addCell(pos++, bookingOrder.getBillingAddress().getIdentifier());
		row.addCell(pos++, bookingOrder.getBillingAddress().getNameLine1());
		row.addCell(pos++, bookingOrder.getBillingAddress().getNameLine2());
		row.addCell(pos++, bookingOrder.getBillingAddress().getAddressLine1());
		row.addCell(pos++, bookingOrder.getBillingAddress().getAddressLine2());
		row.addCell(pos++, bookingOrder.getBillingAddress().getAddressLine3());
		row.addCell(pos++, bookingOrder.getBillingAddress().getAddressLine4());
		row.addCell(pos++, bookingOrder.getBillingAddress().getPoBox());
		row.addCell(pos++, bookingOrder.getBillingAddress().getRegion());
		row.addCell(pos++, bookingOrder.getBillingAddress().getZip());
		row.addCell(pos++, bookingOrder.getBillingAddress().getCity());
		row.addCell(pos++, bookingOrder.getBillingAddress().getCountry());
		row.addCell(pos++, bookingOrder.getBillingAddressOrgId());
		row.addCell(pos++, bookingOrder.getBillingAddressOrgName());
	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class).getUserPropertyHandlersFor(PROPS_IDENTIFIER, false);
	}
}
