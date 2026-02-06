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


import static org.olat.core.util.openxml.OpenXMLUtils.truncateForCell;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.coach.reports.AbstractReportConfiguration;
import org.olat.modules.coach.reports.ReportConfigurationAccessSecurityCallback;
import org.olat.modules.coach.reports.TimeBoundReportConfiguration;
import org.olat.modules.coach.security.CourseProgressAndStatusRightProvider;
import org.olat.modules.coach.security.LecturesAndAbsencesRightProvider;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.BookingKey;
import org.olat.modules.curriculum.manager.BookingOrder;
import org.olat.modules.curriculum.manager.CurriculumAccountingDAO;
import org.olat.modules.curriculum.model.CurriculumAccountingSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.OrderTableItem;
import org.olat.resource.accesscontrol.ui.OrdersDataModel;
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
		if (secCallback.isCurriculumOwner() || secCallback.isCurriculumManager()
				|| secCallback.isAdministrator() || secCallback.isPrincipal()) {
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
		header.addCell(pos++, translator.translate("report.header.membership.status"));
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
		header.addCell(pos++, translator.translate("report.header.location"));
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
		
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		List<String> selectedRights = curriculumModule.getUserOverviewRightList();
		if(selectedRights.contains(CourseProgressAndStatusRightProvider.RELATION_RIGHT)) {
			header.addCell(pos++, translator.translate("report.header.statement.score"));
			header.addCell(pos++, translator.translate("report.header.statement.status"));
			header.addCell(pos++, translator.translate("report.header.statement.passed"));
			header.addCell(pos++, translator.translate("report.header.statement.not.passed"));
			header.addCell(pos++, translator.translate("report.header.statement.undefined"));
			header.addCell(pos++, translator.translate("report.header.statement.progress"));
			header.addCell(pos++, translator.translate("report.header.statement.certificate"));
			header.addCell(pos++, translator.translate("report.header.statement.certificate.validity"));
			header.addCell(pos++, translator.translate("report.header.statement.first.visit"));
			header.addCell(pos++, translator.translate("report.header.statement.last.visit"));
		}
		
		if(selectedRights.contains(LecturesAndAbsencesRightProvider.RELATION_RIGHT)) {
			header.addCell(pos++, translator.translate("report.header.absence.units"));
			header.addCell(pos++, translator.translate("report.header.absence.attended"));
			header.addCell(pos++, translator.translate("report.header.absence.not.excused"));
			header.addCell(pos++, translator.translate("report.header.absence.authorized"));
			header.addCell(pos++, translator.translate("report.header.absence.dispensed"));
		}
		
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet,
								List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		Translator translator = getTranslator(locale);
		Translator statusTranslator = Util.createPackageTranslator(OrdersDataModel.class, locale);
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
		
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		List<String> selectedRights = curriculumModule.getUserOverviewRightList();
		boolean withProgressAndstatus = selectedRights.contains(CourseProgressAndStatusRightProvider.RELATION_RIGHT);
		boolean withAbsences = selectedRights.contains(LecturesAndAbsencesRightProvider.RELATION_RIGHT);
		
		List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, userPropertyHandlers);
		if(withProgressAndstatus) {
			curriculumAccountingDao.loadAssessmentsInfos(bookingOrders, searchParams);
		}
		if(withAbsences) {
			loadAbsences(bookingOrders);
		}
		
		Map<String, String> accessTypeToName = getAccessTypeToName(bookingOrders, locale);
		for (BookingOrder bookingOrder : bookingOrders) {
			generateDataRow(workbook, sheet, userPropertyHandlers, bookingOrder, accessTypeToName,
					educationalTypeIdToName, withProgressAndstatus, withAbsences,
					statusTranslator, translator);
		}
	}
	
	private void loadAbsences(List<BookingOrder> bookingOrders) {
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		
		Set<CurriculumElementRef> elements = new HashSet<>();
		Map<BookingKey,List<BookingOrder>> ordersMap = new HashMap<>();
		for(BookingOrder order:bookingOrders) {
			BookingKey key = new BookingKey(order.getImplementationKey(), order.getIdentityKey());
			ordersMap.computeIfAbsent(key, k -> new ArrayList<>(2))
				.add(order);
			if(order.getImplementationKey() != null) {
				elements.add(new CurriculumElementRefImpl(order.getImplementationKey()));
			}
		}
		
		for(CurriculumElementRef element:elements) {
			CurriculumElement curriculumElement = curriculumService.getCurriculumElement(element);
			List<RepositoryEntry> entries = curriculumService
						.getRepositoryEntriesWithLectures(curriculumElement, null, true);
			if(!entries.isEmpty()) {
				LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
				params.setEntries(entries);
				List<LectureBlockIdentityStatistics> rawStatistics = lectureService
						.getLecturesStatistics(params, List.of(), null);
				List<LectureBlockIdentityStatistics> aggregatedStatistics = lectureService.groupByIdentity(rawStatistics);
				Map<Long,LectureBlockIdentityStatistics> aggregatedStatisticsMap = aggregatedStatistics.stream()
						.collect(Collectors.toMap(LectureBlockIdentityStatistics::getIdentityKey, u -> u, (u, v) -> u));
				
				for(BookingOrder order:bookingOrders) {
					if(element.getKey().equals(order.getImplementationKey())) {
						LectureBlockIdentityStatistics statistics = aggregatedStatisticsMap.get(order.getIdentityKey());
						order.setLectureBlockStatistics(statistics);
					}
				}
			}
		}
	}

	private Map<String, String> getAccessTypeToName(List<BookingOrder> bookingOrders, Locale locale) {
		Set<String> accessTypes = bookingOrders.stream().map(BookingOrder::getAccessMethod).filter(Objects::nonNull)
				.map(AccessMethod::getType).collect(Collectors.toSet());
		AccessControlModule accessControlModule = CoreSpringFactory.getImpl(AccessControlModule.class);
		Map<String, String> accessTypeToName = new HashMap<>();
		for (String accessType : accessTypes) {
			AccessMethodHandler accessMethodHandler = accessControlModule.getAccessMethodHandler(accessType);
			if (accessMethodHandler != null) {
				accessTypeToName.put(accessType, accessMethodHandler.getMethodName(locale));
			}
		}
		return accessTypeToName;
	}

	private void generateDataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet sheet,
								 List<UserPropertyHandler> userPropertyHandlers, BookingOrder bookingOrder,
								 Map<String, String> accessTypeToName, Map<String, String> educationalTypeIdToName,
								 boolean withProgressAndStatus, boolean withAbsences,
								 Translator statusTranslator, Translator curriculumTranslator) {
		OpenXMLWorksheet.Row row = sheet.newRow();
		int pos = 0;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			row.addCell(pos, bookingOrder.getIdentityProp(pos));
			pos++;
		}
		row.addCell(pos++, getMembershipStatusString(bookingOrder, curriculumTranslator));
		row.addCell(pos++, bookingOrder.getCurriculumName());
		row.addCell(pos++, bookingOrder.getCurriculumIdentifier());
		row.addCell(pos++, bookingOrder.getCurriculumOrgId());
		row.addCell(pos++, bookingOrder.getCurriculumOrgName());
		row.addCell(pos++, bookingOrder.getImplementationName());
		row.addCell(pos++, bookingOrder.getImplementationIdentifier());
		row.addCell(pos++, bookingOrder.getImplementationType());
		row.addCell(pos++, getImplementationStatusString(bookingOrder, curriculumTranslator));
		row.addCell(pos++, educationalTypeIdToName.get(bookingOrder.getImplementationFormat()));
		row.addCell(pos++, bookingOrder.getBeginDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(pos++, bookingOrder.getEndDate(), workbook.getStyles().getDateTimeStyle());
		row.addCell(pos++, bookingOrder.getImplementationLocation());
		row.addCell(pos++, "" + bookingOrder.getOrder().getKey());
		row.addCell(pos++, getStatusString(bookingOrder, statusTranslator));
		row.addCell(pos++, bookingOrder.getOfferName());
		row.addCell(pos++, getOfferType(bookingOrder, accessTypeToName));
		row.addCell(pos++, bookingOrder.getOfferCostCenter());
		row.addCell(pos++, bookingOrder.getOfferAccount());
		row.addCell(pos++, bookingOrder.getOrder().getPurchaseOrderNumber());
		row.addCell(pos++, truncateForCell(bookingOrder.getOrder().getComment()));
		row.addCell(pos++, bookingOrder.getOrder().getCreationDate(), workbook.getStyles().getDateTimeStyle());
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
		
		if(withProgressAndStatus) {
			pos = generateStatementDataRow(workbook, row, pos, bookingOrder, curriculumTranslator);
		}
		if(withAbsences) {
			generateAbsencesDataRow(row, pos, bookingOrder);
		}
	}

	private int generateAbsencesDataRow(OpenXMLWorksheet.Row row, int pos, BookingOrder bookingOrder) {
		LectureBlockIdentityStatistics statistics = bookingOrder.getLectureBlockStatistics();
		if(statistics == null) {
			for(int i=5; i-->0; ) {
				row.addCell(pos++, 0l, null);
			}
		} else {
			row.addCell(pos++, statistics.getTotalPersonalPlannedLectures(), null);
			row.addCell(pos++, statistics.getTotalAttendedLectures(), null);
			row.addCell(pos++, statistics.getTotalAbsentLectures(), null);
			row.addCell(pos++, statistics.getTotalAuthorizedAbsentLectures(), null);
			row.addCell(pos++, statistics.getTotalDispensationLectures(), null);
		}
		return pos;
	}
	
	private int generateStatementDataRow(OpenXMLWorkbook workbook, OpenXMLWorksheet.Row row, int pos, BookingOrder bookingOrder,
			Translator translator) {
		List<AssessmentEntry> assessmentEntries = bookingOrder.getAssessmentEntries();
		if(assessmentEntries == null || assessmentEntries.isEmpty()) {
			pos += 6;
		} else if(assessmentEntries.size() == 1) {
			AssessmentEntry assessmentEntry = assessmentEntries.get(0);
			row.addCell(pos++, assessmentEntry.getScore(), workbook.getStyles().getDoubleStyle());

			if(assessmentEntry.getPassed() == null) {
				pos = successStatus(row, pos, translator.translate("report.undefined"), 0l, 0l, 1l);
			} else if(assessmentEntry.getPassed().booleanValue()) {
				pos = successStatus(row, pos, translator.translate("report.passed"), 1l, 0l, 0l);
			} else {
				pos = successStatus(row, pos, translator.translate("report.not.passed"), 0l, 1l, 0l);
			}
			
			if(assessmentEntry.getCompletion() == null) {
				pos++;
			} else {
				row.addCell(pos++, assessmentEntry.getCompletion(), workbook.getStyles().getPercent0DecimalsStyle());
			}
		} else {
			BigDecimal score = null;
			long passed = 0l;
			long notPassed = 0l;
			long undefined = 0l;
			
			int numOfCompletion = 0;
			Double totalCompletion = null;
			for(AssessmentEntry assessmentEntry:assessmentEntries) {
				if(assessmentEntry.getScore() != null) {
					if(score == null) {
						score = assessmentEntry.getScore();
					} else if(assessmentEntry.getScore() != null) {
						score = score.add(assessmentEntry.getScore());
					}
				}
				if(assessmentEntry.getPassed() != null && assessmentEntry.getPassed().booleanValue()) {
					passed++;
				} else if(assessmentEntry.getPassed() != null && !assessmentEntry.getPassed().booleanValue()) {
					notPassed++;
				} else {
					undefined++;
				}
				if(assessmentEntry.getCompletion() != null) {
					numOfCompletion++;
					if(totalCompletion == null) {
						totalCompletion = assessmentEntry.getCompletion();
					} else {
						totalCompletion = totalCompletion.doubleValue() + assessmentEntry.getCompletion().doubleValue();
					}
				}
			}
			
			if(score == null) {
				pos++;
			} else {
				row.addCell(pos++, score, null);
			}
			
			if(passed > 0l && notPassed == 0l && undefined == 0l) {
				pos = successStatus(row, pos, translator.translate("report.passed"), passed, notPassed, undefined);
			} else if(notPassed > 0l) {
				pos = successStatus(row, pos, translator.translate("report.not.passed"), passed, notPassed, undefined);
			} else if(passed > 0l && notPassed == 0l && undefined > 0l) {
				pos = successStatus(row, pos, translator.translate("report.passed.mixed"), passed, notPassed, undefined);
			} else if(passed == 0l && notPassed == 0l && undefined > 0l) {
				pos = successStatus(row, pos, translator.translate("report.undefined"), passed, notPassed, undefined);
			} else {
				pos = successStatus(row, pos, null, passed, notPassed, undefined);
			}
			
			if(totalCompletion == null || numOfCompletion == 0) {
				pos++;
			} else {
				double averageCompletion = totalCompletion.doubleValue() / numOfCompletion;
				row.addCell(pos++, Double.valueOf(averageCompletion), workbook.getStyles().getPercent0DecimalsStyle());
			}
		}
		
		if(bookingOrder.getCertificateKeys() == null || bookingOrder.getCertificateKeys().isEmpty()) {
			pos++;
		} else if(bookingOrder.getCertificateKeys().size() == 1) {
			row.addCell(pos++, "1");
		} else {
			row.addCell(pos++, translator.translate("report.several"));
		}
		
		row.addCell(pos++, bookingOrder.getNextCertificationDate(), workbook.getStyles().getDateStyle());
		
		row.addCell(pos++, bookingOrder.getFirstVisit(), workbook.getStyles().getDateTimeStyle());
		row.addCell(pos++, bookingOrder.getLastVisit(), workbook.getStyles().getDateTimeStyle());

		return pos;
	}
	
	private int successStatus(OpenXMLWorksheet.Row row, int pos, String status, long passed, long notPassed, long undefined) {
		row.addCell(pos++, status);
		row.addCell(pos++, passed, null);
		row.addCell(pos++, notPassed, null);
		row.addCell(pos++, undefined, null);
		return pos;
	}
	
	private String getStatusString(BookingOrder bookingOrder, Translator statusTranslator) {
		String orderStatusString = bookingOrder.getOrder().getOrderStatus().name();
		Price cancellationFees = bookingOrder.getOrder().getCancellationFees();
		String trxStatus = bookingOrder.getTransactionStatus();
		String pspTrxStatus = null;
		if (StringHelper.containsNonWhitespace(bookingOrder.getPaypalTransactionStatus())) {
			pspTrxStatus = bookingOrder.getPaypalTransactionStatus();
		} else if (StringHelper.containsNonWhitespace(bookingOrder.getCheckoutTransactionStatus())) {
			pspTrxStatus = bookingOrder.getCheckoutTransactionStatus();
		}

		List<AccessMethod> orderMethods = new ArrayList<>();
		if (bookingOrder.getAccessMethod() != null) {
			orderMethods.add(bookingOrder.getAccessMethod());
		}
		OrderTableItem.Status status = OrderTableItem.Status.getStatus(orderStatusString, cancellationFees, trxStatus, 
				pspTrxStatus, orderMethods);
		return statusTranslator.translate(OrderTableItem.Status.getI18nKey(status));
	}
	
	private String getMembershipStatusString(BookingOrder bookingOrder, Translator curriculumTranslator) {
		if (bookingOrder.getOrdererMembershipStatus() == null) {
			return "";			
		}
		return curriculumTranslator.translate("membership." + bookingOrder.getOrdererMembershipStatus().name());
	}
	
	private String getImplementationStatusString(BookingOrder bookingOrder, Translator curriculumTranslator) {
		if (bookingOrder.getImplementationStatus() == null) {
			return "";
		}
		if (!CurriculumElementStatus.isValueOf(bookingOrder.getImplementationStatus())) {
			return "";
		}
		return curriculumTranslator.translate("status." + bookingOrder.getImplementationStatus());
	}

	private String getOfferType(BookingOrder bookingOrder, Map<String, String> accessTypeToName) {
		AccessMethod accessMethod = bookingOrder.getAccessMethod();
		if (accessMethod == null) {
			return null;
		}
		return accessTypeToName.get(accessMethod.getType());
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
		Translator statusTranslator = Util.createPackageTranslator(OrdersDataModel.class, locale);
		Map<String, String> educationalTypeIdToName = getEducationalTypeIdToName(locale);

		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();
		List<String> worksheetNames = List.of(translator.translate("report.booking"));
		
		CurriculumModule curriculumModule = CoreSpringFactory.getImpl(CurriculumModule.class);
		List<String> selectedRights = curriculumModule.getUserOverviewRightList();
		boolean withProgressAndstatus = selectedRights.contains(CourseProgressAndStatusRightProvider.RELATION_RIGHT);
		boolean withAbsences = selectedRights.contains(LecturesAndAbsencesRightProvider.RELATION_RIGHT);
		
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1, worksheetNames)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeader(sheet, userPropertyHandlers, locale);
			
			List<BookingOrder> bookingOrders = curriculumAccountingDao.bookingOrders(searchParams, userPropertyHandlers);
			if(withProgressAndstatus) {
				curriculumAccountingDao.loadAssessmentsInfos(bookingOrders, searchParams);
			}
			if(withAbsences) {
				loadAbsences(bookingOrders);
			}
			
			Map<String, String> accessTypeToName = getAccessTypeToName(bookingOrders, locale);
			for (BookingOrder bookingOrder : bookingOrders) {
				generateDataRow(workbook, sheet, userPropertyHandlers, bookingOrder, accessTypeToName, 
						educationalTypeIdToName, withProgressAndstatus, withAbsences,
						statusTranslator, translator);
				
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
