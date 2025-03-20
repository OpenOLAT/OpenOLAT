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
package org.olat.modules.curriculum.reports;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.coach.reports.AbstractReportConfiguration;
import org.olat.modules.coach.reports.ReportConfigurationAccessSecurityCallback;
import org.olat.modules.coach.reports.TimeBoundReportConfiguration;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.BookingOrder;
import org.olat.modules.curriculum.manager.CurriculumAccountingDAO;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-02-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AccountingReportConfiguration extends TimeBoundReportConfiguration implements CurriculumReportConfiguration {

	private Boolean excludeDeletedCurriculumElements;

	public void setExcludeDeletedCurriculumElements(Boolean excludeDeletedCurriculumElements) {
		this.excludeDeletedCurriculumElements = excludeDeletedCurriculumElements;
	}

	public Boolean getExcludeDeletedCurriculumElements() {
		return excludeDeletedCurriculumElements;
	}

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		if (!secCallback.isCurriculumContext()) {
			return false;
		}
		if (secCallback.isCurriculumOwner() || secCallback.isCurriculumManager()) {
			return true;
		}
		return false;
	}

	@Override
	protected Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CurriculumManagerRootController.class, locale, super.getTranslator(locale));
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.booking";
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		header.addCell(pos++, translator.translate("report.header.curriculum"));
		header.addCell(pos++, translator.translate("report.header.ext.ref"));
		header.addCell(pos++, translator.translate("report.header.curriculum.org.id"));
		header.addCell(pos++, translator.translate("report.header.curriculum.org.name"));
		header.addCell(pos++, translator.translate("report.header.implementation"));
		header.addCell(pos++, translator.translate("report.header.ext.ref"));
		header.addCell(pos++, translator.translate("report.header.implementation.type"));
		header.addCell(pos++, translator.translate("report.header.implementation.status"));
		header.addCell(pos++, translator.translate("report.header.implementation.format"));
		header.addCell(pos++, translator.translate("report.header.execution.from"));
		header.addCell(pos++, translator.translate("report.header.execution.to"));
		header.addCell(pos++, translator.translate("report.header.booking.number"));
		header.addCell(pos++, translator.translate("report.header.booking.status"));
		header.addCell(pos++, translator.translate("report.header.offer"));
		header.addCell(pos++, translator.translate("report.header.offer.type"));
		header.addCell(pos++, translator.translate("report.header.cost.center"));
		header.addCell(pos++, translator.translate("report.header.account"));
		header.addCell(pos++, translator.translate("report.header.po.number"));
		header.addCell(pos++, translator.translate("report.header.order.comment"));
		header.addCell(pos++, translator.translate("report.header.order.date"));
		header.addCell(pos++, translator.translate("report.header.price"));
		header.addCell(pos++, translator.translate("report.header.cancellation.fee"));
		header.addCell(pos++, translator.translate("report.header.billing.address"));
		header.addCell(pos++, translator.translate("report.header.name.company"));
		header.addCell(pos++, translator.translate("report.header.addition"));
		header.addCell(pos++, translator.translate("report.header.address.line", "1"));
		header.addCell(pos++, translator.translate("report.header.address.line", "2"));
		header.addCell(pos++, translator.translate("report.header.address.line", "3"));
		header.addCell(pos++, translator.translate("report.header.address.line", "4"));
		header.addCell(pos++, translator.translate("report.header.po.box"));
		header.addCell(pos++, translator.translate("report.header.region"));
		header.addCell(pos++, translator.translate("report.header.zip"));
		header.addCell(pos++, translator.translate("report.header.city"));
		header.addCell(pos++, translator.translate("report.header.country"));
		header.addCell(pos++, translator.translate("report.header.billing.address.org.id"));
		header.addCell(pos++, translator.translate("report.header.billing.address.org.name"));
		
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet,
								List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Map<String, String> educationalTypeIdToName = getEducationalTypeIdToName(locale);
		CurriculumAccountingDAO curriculumAccountingDao = CoreSpringFactory.getImpl(CurriculumAccountingDAO.class);
		CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
		searchParams.setIdentity(coach);
		if (getDurationTimeUnit() != null) {
			int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
			searchParams.setFromDate(getDurationTimeUnit().fromDate(new Date(), duration));
			searchParams.setToDate(getDurationTimeUnit().toDate(new Date()));
		}
		if (getExcludeDeletedCurriculumElements() != null) {
			searchParams.setExcludeDeletedCurriculumElements(getExcludeDeletedCurriculumElements());
		}
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, userPropertyHandlers);
		for (BookingOrder bookingOrder : bookingOrders) {
			generateDataRow(workbook, sheet, userPropertyHandlers, bookingOrder, educationalTypeIdToName);
		}
	}

	private void generateDataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet,
								 List<UserPropertyHandler> userPropertyHandlers, BookingOrder bookingOrder,
								 Map<String, String> educationalTypeIdToName) {
		OpenXMLWorksheet.Row row = sheet.newRow();
		int pos = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, bookingOrder.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, bookingOrder.getCurriculumName());
		row.addCell(pos++, bookingOrder.getCurriculumIdentifier());
		row.addCell(pos++, bookingOrder.getCurriculumOrgId());
		row.addCell(pos++, bookingOrder.getCurriculumOrgName());
		row.addCell(pos++, bookingOrder.getImplementationName());
		row.addCell(pos++, bookingOrder.getImplementationIdentifier());
		row.addCell(pos++, bookingOrder.getImplementationType());
		row.addCell(pos++, bookingOrder.getImplementationStatus());
		row.addCell(pos++, educationalTypeIdToName.get(bookingOrder.getImplementationFormat()));
		row.addCell(pos++, "" + bookingOrder.getBeginDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(pos++, "" + bookingOrder.getEndDate(), workbook.getStyles().getDateTimeStyle());
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
		return CoreSpringFactory.getImpl(UserManager.class)
				.getUserPropertyHandlersFor(AbstractReportConfiguration.PROPS_IDENTIFIER, false);
	}

	@Override
	public ReportContent generateReport(Curriculum curriculum, CurriculumElement curriculumElement,
										Identity doer, Locale locale, VFSLeaf file) {
		final CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);

		Set<CurriculumRef> curriculums = new HashSet<>();
		Set<CurriculumElementRef> implementations = new HashSet<>();
		try (OutputStream out = file.getOutputStream(true)) {
			CurriculumAccountingSearchParams searchParams = new CurriculumAccountingSearchParams();
			searchParams.setCurriculum(curriculum);
			searchParams.setCurriculumElement(curriculumElement);
			if (curriculum == null && curriculumElement == null) {
				CurriculumSearchParameters params = new CurriculumSearchParameters();
				params.setCurriculumAdmin(doer);
				List<Curriculum> ownedCurriculums = curriculumService.getCurriculums(params);
				searchParams.setCurriculums(ownedCurriculums.stream().map(Curriculum::getKey)
						.map(CurriculumRefImpl::new).collect(Collectors.toList()));
			}
			if (getDurationTimeUnit() != null) {
				int duration = getDuration() != null ? Integer.parseInt(getDuration()) : 0;
				searchParams.setFromDate(getDurationTimeUnit().fromDate(new Date(), duration));
				searchParams.setToDate(getDurationTimeUnit().toDate(new Date()));
			}
			if (getExcludeDeletedCurriculumElements() != null) {
				searchParams.setExcludeDeletedCurriculumElements(getExcludeDeletedCurriculumElements());
			}
			generateReport(searchParams, curriculums, implementations, locale, out);
		} catch (IOException e) {
			log.error("Unable to generate export", e);
			return null;
		}
		
		List<Curriculum> curriculumList = curriculums.isEmpty()
				? List.of()
				: curriculumService.getCurriculums(curriculums);
		List<CurriculumElement> implementationList = implementations.isEmpty()
				? List.of()
				: curriculumService.getCurriculumElements(implementations);
		return new ReportContent(curriculumList, implementationList);
	}
	
	public void generateReport(CurriculumAccountingSearchParams searchParams,
							   Set<CurriculumRef> curriculumsInReport, Set<CurriculumElementRef> implementationsInReport,
							   Locale locale, OutputStream out) {
		final CurriculumAccountingDAO curriculumAccountingDao = CoreSpringFactory.getImpl(CurriculumAccountingDAO.class);
		
		Translator translator = getTranslator(locale);
		Map<String, String> educationalTypeIdToName = getEducationalTypeIdToName(locale);

		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();
		List<String> worksheetNames = List.of(translator.translate("report.booking"));
		
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1, worksheetNames)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeader(sheet, userPropertyHandlers, locale);
			List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, userPropertyHandlers);
			for (BookingOrder bookingOrder : bookingOrders) {
				generateDataRow(workbook, sheet, userPropertyHandlers, bookingOrder, educationalTypeIdToName);
				
				if(curriculumsInReport != null && bookingOrder.getCurriculumKey() != null) {
					curriculumsInReport.add(new CurriculumRefImpl(bookingOrder.getCurriculumKey()));
				}
				if(implementationsInReport != null && bookingOrder.getImplementationKey() != null) {
					implementationsInReport.add(new CurriculumElementRefImpl(bookingOrder.getImplementationKey()));
				}
			}
		} catch (IOException e) {
			log.error("Unable to generate export", e);
		}
	}

	private Map<String, String> getEducationalTypeIdToName(Locale locale) {
		Translator repositoyTranslator = Util.createPackageTranslator(RepositoryService.class, locale);
		List<RepositoryEntryEducationalType> educationalTypes = CoreSpringFactory.getImpl(RepositoryManager.class).getAllEducationalTypes();
		HashMap<String, String> educationalTypeIdToName = new HashMap<>();
		for (RepositoryEntryEducationalType educationalType : educationalTypes) {
			String i18nKey = RepositoyUIFactory.getI18nKey(educationalType);
			String name = repositoyTranslator.translate(i18nKey);
			educationalTypeIdToName.put(educationalType.getIdentifier(), name);
		}
		return educationalTypeIdToName;
	}
}
