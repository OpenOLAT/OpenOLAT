/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.DateUtils;
import org.olat.core.util.LocalDateRange;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramLog;
import org.olat.modules.certificationprogram.CertificationProgramLogAction;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.RecertificationMode;
import org.olat.modules.certificationprogram.manager.CertificationProgramXStream;
import org.olat.modules.certificationprogram.model.CertificationProgramLogSearchParameters;
import org.olat.modules.certificationprogram.ui.CertificationProgramLogTableModel.ActivityLogCols;
import org.olat.modules.certificationprogram.ui.component.CertificationProgramActivityLogContextRenderer;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationCellRenderer;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 janv. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramLogController extends FormBasicController {
	
	private static final String TAB_ID_LAST_7_DAYS = "Last7Days";
	private static final String TAB_ID_LAST_4_WEEKS = "Last4Weeks";
	private static final String TAB_ID_LAST_12_MONTH = "Last12Month";
	private static final String TAB_ID_ALL = "All";
	
	protected static final String FILTER_ACTIVITY = "activity";
	protected static final String FILTER_DATE = "date";
	protected static final String FILTER_CONTEXT = "context";
	protected static final String FILTER_MEMBER = "member";
	protected static final String FILTER_USER = "user";

	private FlexiFiltersTab tabLast7Days;
	private FlexiFiltersTab tabLast4Weeks;
	private FlexiFiltersTab tabLast12Month;
	private FlexiFiltersTab tabAll;
	
	private FlexiTableElement tableEl;
	private CertificationProgramLogTableModel tableModel;

	private final CertificationProgram certificationProgram;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramLogController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram) {
		super(ureq, wControl, null, "program_log", Util.createPackageTranslator(ActivityLogController.class, ureq.getLocale()));
		this.certificationProgram = certificationProgram;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.date,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.context,
				new CertificationProgramActivityLogContextRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.object));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.message));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.originalValue));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.newValue));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ActivityLogCols.user));
		
		tableModel = new CertificationProgramLogTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);

		initFilters();
		initFilterTabs(ureq);
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);
		
		LocalDateTime today = DateUtils.setTime(LocalDateTime.now(), 0, 0, 0);
		tabLast7Days = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_7_DAYS, translate("tab.last.7.days"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, new LocalDateRange(today.minusDays(7), today.plusDays(1)))));
		tabs.add(tabLast7Days);

		tabLast4Weeks = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_4_WEEKS, translate("tab.last.4.weeks"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, new LocalDateRange(today.minusDays(28), today.plusDays(1)))));
		tabs.add(tabLast4Weeks);

		tabLast12Month = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_12_MONTH, translate("tab.last.12.month"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf(FILTER_DATE, new LocalDateRange(today.minusYears(1), today.plusDays(1)))));
		tabs.add(tabLast12Month);

		tabAll = FlexiFiltersTabFactory.tab(TAB_ID_ALL, translate("tab.all"), TabSelectionBehavior.reloadData);
		tabs.add(tabAll);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabLast7Days);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		
		filters.add(new FlexiTableDateRangeFilter(translate("activity.log.filter.date"), FILTER_DATE, true, true,
				getLocale()));

		SelectionValues contextPK = new SelectionValues();
		for(CertificationProgramActivityLogContext ctxt: CertificationProgramActivityLogContext.values()) {
			contextPK.add(SelectionValues.entry(ctxt.name(), CertificationUIFactory.getTranslatedLogContext(getTranslator(), ctxt), null,
					"o_icon o_icon-fw " + CertificationUIFactory.getLogContextIconCss(ctxt), null, true));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.context"), FILTER_CONTEXT,
				contextPK, true));
		
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.activity"), FILTER_ACTIVITY,
				getActivityFilterValues(), true));
		
		List<Identity> members = certificationProgramService.searchLogIdentity(certificationProgram);
		SelectionValues membersPK = new SelectionValues();
		for(Identity member:members) {
			membersPK.add(SelectionValues.entry(member.getKey().toString(), userManager.getUserDisplayName(member)));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.member"), FILTER_MEMBER, membersPK, true));
		
		List<Identity> users = certificationProgramService.searchLogDoer(certificationProgram);
		SelectionValues usersPK = new SelectionValues();
		for(Identity user:users) {
			usersPK.add(SelectionValues.entry(user.getKey().toString(), userManager.getUserDisplayName(user)));
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.filter.user"), FILTER_USER, usersPK, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private SelectionValuesSupplier getActivityFilterValues() {
		SelectionValues filter = new SelectionValues();
		
		// Certificates
		appendActivityFilterValues(filter, CertificationProgramLogAction.revoke_certificate, CertificationProgramLogAction.issue_certificate,
				CertificationProgramLogAction.expire_certificate);
		
		// Notifications sent
		appendActivityFilterValues(filter, CertificationProgramLogAction.send_notification_certificate_issued, CertificationProgramLogAction.send_notification_certificate_expired,
				CertificationProgramLogAction.send_notification_certificate_revoked, CertificationProgramLogAction.send_notification_program_removed,
				CertificationProgramLogAction.send_reminder_upcoming, CertificationProgramLogAction.send_reminder_overdue);

		// Add memberships
		appendActivityFilterValues(filter, CertificationProgramLogAction.add_membership, CertificationProgramLogAction.add_membership_manually,
				CertificationProgramLogAction.change_membership, CertificationProgramLogAction.remove_membership);
		
		// Reminder
		appendActivityFilterValues(filter, CertificationProgramLogAction.reminder_change_status, CertificationProgramLogAction.reminder_create,
				CertificationProgramLogAction.reminder_delete);
		filter.add(SelectionValues.entry(CertificationProgramLogAction.reminder_edit.name(),
				translate("activity.message.reminder_edit.filter")));
		appendActivityFilterValues(filter, CertificationProgramLogAction.reminder_change_content, CertificationProgramLogAction.reminder_customize_content,
				CertificationProgramLogAction.reminder_reset_content, CertificationProgramLogAction.notification_change_status,
				CertificationProgramLogAction.notification_change_content, CertificationProgramLogAction.notification_customize_content,
				CertificationProgramLogAction.notification_reset_content);
		// Owner
		appendActivityFilterValues(filter, CertificationProgramLogAction.add_owner, CertificationProgramLogAction.remove_owner);
		// Implementation
		appendActivityFilterValues(filter, CertificationProgramLogAction.add_implementation, CertificationProgramLogAction.remove_implementation);
		// Certification program configuration
		filter.add(SelectionValues.entry(CertificationProgramLogAction.edit_certification_program.name(),
				translate("activity.message.edit_certification_program.filter")));
		filter.add(SelectionValues.entry(CertificationProgramLogAction.edit_certification_program_organisations.name(),
				translate("activity.message.edit_certification_program_organisations.filter")));
	
		return filter;
	}
	
	private void appendActivityFilterValues(SelectionValues filter, CertificationProgramLogAction... actions) {
		if(actions == null || actions.length == 0 || actions[0] == null) return;
		
		for(int i=0; i<actions.length; i++) {
			CertificationProgramLogAction action = actions[i];
			if(action != null) {
				filter.add(SelectionValues.entry(action.name(), translate("activity.message.".concat(action.name()))));
			}
		}
	}
	
	private void loadModel() {
		LocalDateRange dateRange = getFilterDateRange();
		CertificationProgramLogSearchParameters searchParams = new CertificationProgramLogSearchParameters();
		searchParams.setCertificationProgram(certificationProgram);
		searchParams.setDateRange(dateRange);
		
		List<CertificationProgramLog> logs = certificationProgramService.searchLogs(searchParams);
		List<CertificationProgramLogRow> rows = new ArrayList<>(logs.size());
		for(CertificationProgramLog l:logs) {
			List<CertificationProgramLogRow> rs = forgeRows(l);
			if(rs != null && !rs.isEmpty()) {
				rows.addAll(rs);
			}
		}
		
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private LocalDateRange getFilterDateRange() {
    	FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_DATE);
		if(filter instanceof FlexiTableDateRangeFilter rangeFilter) {
			LocalDateRange  range = rangeFilter.getLocalDateRange();
			if(range != null && !range.isEmpty()) {
				return range;
			}
		}
		return null;
	}
	
	private List<CertificationProgramLogRow> forgeRows(CertificationProgramLog auditLog) {
		return switch(auditLog.getAction()) {
			// Sent mails
			case send_notification_certificate_issued, send_notification_certificate_renewed,
				send_notification_certificate_expired, send_notification_certificate_revoked, send_notification_program_removed,
				send_reminder_upcoming, send_reminder_overdue -> List.of(createRow(auditLog, CertificationProgramActivityLogContext.message, getMemberObject(auditLog), getActivity(auditLog)));
			// Manage owners
			case add_owner, remove_owner -> List.of(createRow(auditLog, CertificationProgramActivityLogContext.owner, getIdentityObject(auditLog), getActivity(auditLog)));
			// Manage implementations
			case add_implementation -> List.of(createRow(auditLog, CertificationProgramActivityLogContext.implementation, getCurriculumElementObject(auditLog), getActivity(auditLog)));
			case remove_implementation -> List.of(createRow(auditLog, CertificationProgramActivityLogContext.implementation, getCurriculumElementObject(auditLog), getActivity(auditLog)));
			// Change certification program
			case edit_certification_program -> createCertificationProgramConfigurationsRows(auditLog);
			case edit_certification_program_organisations -> List.of(createCertificationProgramOrganisationsRow(auditLog));
			// Manage reminders and notifications
			case reminder_delete -> List.of(createMessageRow(auditLog));
			case reminder_create, reminder_edit -> createReminderConfigurationsRows(auditLog);
			case reminder_change_status, notification_change_status -> List.of(createMessageStatusRow(auditLog));
			case reminder_customize_content, notification_customize_content -> List.of(createMessageContentRow(auditLog, translate("default.template"), translate("content.customized")));
			case reminder_reset_content, notification_reset_content -> List.of(createMessageContentRow(auditLog, translate("content.customized"), translate("default.template")));
			case reminder_change_content, notification_change_content -> List.of(createMessageContentRow(auditLog, null, null));
			// Certificates
			case revoke_certificate, issue_certificate, expire_certificate, remove_membership -> List.of(createCertificateStatusRow(auditLog));
			case add_membership, add_membership_manually -> List.of(createCertificateStatusRow(auditLog));
			
			default -> List.of();
		};
	}
	
	private CertificationProgramLogRow createCertificationProgramOrganisationsRow(CertificationProgramLog auditLog) {
		try {
			List<Long> beforeOrganisationKeys = CertificationProgramXStream.fromXmlToListLong(auditLog.getBefore());
			List<Long> afterOrganisationKeys = CertificationProgramXStream.fromXmlToListLong(auditLog.getAfter());
			
			Set<Long> organisationsKeys = new HashSet<>(beforeOrganisationKeys);
			organisationsKeys.addAll(afterOrganisationKeys);
			List<OrganisationRef> organisationsRefs = organisationsKeys.stream()
					.map(key -> new OrganisationRefImpl(key))
					.map(OrganisationRef.class::cast).toList();
			Map<Long, Organisation> organisationsMap = organisationService.getOrganisation(organisationsRefs).stream()
					.collect(Collectors.toMap(Organisation::getKey, o -> o, (u, v) -> u));
			
			String before = toString(beforeOrganisationKeys, organisationsMap);
			String after = toString(afterOrganisationKeys, organisationsMap);
			return createCertificationProgramConfigurationRow(auditLog,
					translate("certification.program.metadata"), translate("certification.admin.access"), before, after);
		} catch (Exception e) {
			logError("", e);
		}
		return null;
	}
	
	private String toString(List<Long> organisationKeys, Map<Long, Organisation> organisationsMap) {
		List<String> names = organisationKeys.stream()
				.map(key -> organisationsMap.get(key))
				.filter(Objects::nonNull)
				.map(Organisation::getDisplayName)
				.toList();
		return String.join(", ", names);
	}
	
	private List<CertificationProgramLogRow> createCertificationProgramConfigurationsRows(CertificationProgramLog auditLog) {
		List<CertificationProgramLogRow> rows = new ArrayList<>();
		try {
			CertificationProgram beforeConfiguration = CertificationProgramXStream.fromXml(auditLog.getBefore(), CertificationProgram.class);
			CertificationProgram afterConfiguration = CertificationProgramXStream.fromXml(auditLog.getAfter(), CertificationProgram.class);
			if(beforeConfiguration == null || afterConfiguration == null) {
				return List.of();
			}

			String displayNameBefore = beforeConfiguration.getDisplayName();
			String displayNameAfter = afterConfiguration.getDisplayName();
			if(!Objects.equals(displayNameBefore, displayNameAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.metadata"), translate("certification.program.name"), displayNameBefore, displayNameAfter));
			}
			
			String identifierBefore = beforeConfiguration.getIdentifier();
			String identifierAfter = afterConfiguration.getIdentifier();
			if(!Objects.equals(identifierBefore, identifierAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.metadata"), translate("certification.program.identifier"), identifierBefore, identifierAfter));
			}
			
			boolean validityEnabledBefore = beforeConfiguration.isValidityEnabled();
			boolean validityEnabledAfter = afterConfiguration.isValidityEnabled();
			if(validityEnabledBefore != validityEnabledAfter) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("validity.enable"), toString(validityEnabledBefore), toString(validityEnabledAfter)));
			}
			
			Duration validityDurationBefore = beforeConfiguration.getValidityTimelapseDuration();
			Duration validityDurationAfter = afterConfiguration.getValidityTimelapseDuration();
			if(!Objects.equals(validityDurationBefore, validityDurationAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("validity.enable"), toString(validityDurationBefore), toString(validityDurationAfter)));
			}
			
			boolean recertificationEnabledBefore = beforeConfiguration.isRecertificationEnabled();
			boolean recertificationEnabledAfter = afterConfiguration.isRecertificationEnabled();
			if(recertificationEnabledBefore != recertificationEnabledAfter) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("recertification.enable"), toString(recertificationEnabledBefore), toString(recertificationEnabledAfter)));
			}
			
			Duration recertificationWindowBefore = beforeConfiguration.getRecertificationWindowDuration();
			Duration recertificationWindowAfter = afterConfiguration.getRecertificationWindowDuration();
			if(!Objects.equals(recertificationWindowBefore, recertificationWindowAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("recertification.window"), toString(recertificationWindowBefore), toString(recertificationWindowAfter)));
			}
			
			RecertificationMode recertificationModeBefore = beforeConfiguration.getRecertificationMode();
			RecertificationMode recertificationModeAfter = afterConfiguration.getRecertificationMode();
			if(!Objects.equals(recertificationModeBefore, recertificationModeAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("recertification.mode"), toString(recertificationModeBefore), toString(recertificationModeAfter)));
			}
			
			boolean creditPointsEnabledBefore = beforeConfiguration.hasCreditPoints();
			boolean creditPointsEnabledAfter = afterConfiguration.hasCreditPoints();
			if(creditPointsEnabledBefore != creditPointsEnabledAfter) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("credit.point.enable"), toString(creditPointsEnabledBefore), toString(creditPointsEnabledAfter)));
			}
			
			BigDecimal creditPointsBefore = creditPointsEnabledBefore ? beforeConfiguration.getCreditPoints() : null;
			BigDecimal creditPointsAfter = creditPointsEnabledAfter ? afterConfiguration.getCreditPoints() : null;
			CreditPointSystem systemBefore = creditPointsEnabledBefore ? beforeConfiguration.getCreditPointSystem() : null;
			CreditPointSystem systemAfter = creditPointsEnabledAfter ? afterConfiguration.getCreditPointSystem() : null;
			if(!Objects.equals(creditPointsBefore, creditPointsAfter) || !Objects.equals(systemBefore, systemAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.configuration"), translate("credit.point.need"),
						toString(creditPointsBefore, systemBefore), toString(creditPointsAfter, systemAfter)));	
			}
			
			String certificateCustom1Before = beforeConfiguration.getCertificateCustom1();
			String certificateCustom1After = afterConfiguration.getCertificateCustom1();
			if(!Objects.equals(certificateCustom1Before, certificateCustom1After)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.certificate"), translate("certificate.custom1"), certificateCustom1Before, certificateCustom1After));	
			}
			String certificateCustom2Before = beforeConfiguration.getCertificateCustom2();
			String certificateCustom2After = afterConfiguration.getCertificateCustom2();
			if(!Objects.equals(certificateCustom2Before, certificateCustom2After)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.certificate"), translate("certificate.custom2"), certificateCustom2Before, certificateCustom2After));	
			}
			String certificateCustom3Before = beforeConfiguration.getCertificateCustom3();
			String certificateCustom3After = afterConfiguration.getCertificateCustom3();
			if(!Objects.equals(certificateCustom3Before, certificateCustom3After)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.certificate"), translate("certificate.custom3"), certificateCustom3Before, certificateCustom3After));	
			}
			
			CertificateTemplate templateBefore = beforeConfiguration.getTemplate();
			CertificateTemplate templateAfter = afterConfiguration.getTemplate();
			if(!Objects.equals(templateBefore, templateAfter)) {
				rows.add(createCertificationProgramConfigurationRow(auditLog,
						translate("certification.program.certificate"), translate("certificate.pdf.template"), toString(templateBefore), toString(templateAfter)));	
			}
			
		} catch (Exception e) {
			logError("", e);
		}
		return rows;
	}
	
	private CertificationProgramLogRow createCertificationProgramConfigurationRow(CertificationProgramLog auditLog,
			String object, String attribute, String before, String after) {
		
		String actor = auditLog.getDoer() == null
				? null
				: userManager.getUserDisplayName(auditLog.getDoer());
		Long actorKey = auditLog.getDoer() == null
				? null
				: auditLog.getDoer().getKey();

		CertificationProgramLogRow row = new CertificationProgramLogRow(auditLog, CertificationProgramActivityLogContext.setting,
				object, null, getActivity(auditLog, attribute), actor, actorKey);
		row.setOriginalValue(before);
		row.setNewValue(after);
		return row;
	}
	
	private List<CertificationProgramLogRow> createReminderConfigurationsRows(CertificationProgramLog auditLog) {
		List<CertificationProgramLogRow> rows = new ArrayList<>();
		
		try {
			CertificationProgramMailConfiguration beforeConfiguration = CertificationProgramXStream.fromXml(auditLog.getBefore(), CertificationProgramMailConfiguration.class);
			CertificationProgramMailConfiguration afterConfiguration = CertificationProgramXStream.fromXml(auditLog.getAfter(), CertificationProgramMailConfiguration.class);
			if(beforeConfiguration == null || afterConfiguration == null) {
				return List.of(createMessageRow(auditLog));
			}
			
			
			String titleBefore = beforeConfiguration.getTitle();
			String titleAfter = afterConfiguration.getTitle();
			if(!Objects.equals(titleBefore, titleAfter)) {
				rows.add(createReminderConfigurationRow(auditLog, translate("reminder.title"), titleBefore, titleAfter));
			}
			
			Duration durationBefore = beforeConfiguration.getTimeDuration();
			Duration durationAfter = afterConfiguration.getTimeDuration();
			if(!Objects.equals(durationBefore, durationAfter)) {
				rows.add(createReminderConfigurationRow(auditLog, translate("reminder.time"), toString(durationBefore), toString(durationAfter)));
			}
		} catch (Exception e) {
			logError("", e);
		}
		return rows;
	}
	
	private String toString(CertificateTemplate template) {
		return template == null ? null : template.getName();
	}
	
	private String toString(BigDecimal points, CreditPointSystem system) {
		return points == null || system == null ? null : CreditPointFormat.format(points, system);
	}
	
	private String toString(Duration duration) {
		return duration == null ? null : DurationCellRenderer.toString(duration, getTranslator());
	}
	
	private String toString(RecertificationMode mode) {
		return mode == null ? null : translate("recertification.mode.".concat(mode.name()));
	}
	
	private String toString(boolean val) {
		return val ? translate("on") : translate("off");
	}
	
	private CertificationProgramLogRow createReminderConfigurationRow(CertificationProgramLog auditLog, String attribute, String before, String after) {
		String actor = auditLog.getDoer() == null
				? null
				: userManager.getUserDisplayName(auditLog.getDoer());
		Long actorKey = auditLog.getDoer() == null
				? null
				: auditLog.getDoer().getKey();
		
		CertificationProgramMailConfiguration configuration = auditLog.getMailConfiguration();
		String object = configuration.getTitle();
		if(!StringHelper.containsNonWhitespace(object)) {
			object = translate("notifications.".concat(configuration.getType().name()));
		}
		
		CertificationProgramLogRow row = new CertificationProgramLogRow(auditLog, CertificationProgramActivityLogContext.message,
				object, null, getActivity(auditLog, attribute), actor, actorKey);
		row.setOriginalValue(before);
		row.setNewValue(after);
		return row;
	}
	
	private CertificationProgramLogRow createRow(CertificationProgramLog auditLog, CertificationProgramActivityLogContext context, String object, String activity) {
		String actor = auditLog.getDoer() == null
				? null
				: userManager.getUserDisplayName(auditLog.getDoer());
		Long actorKey = auditLog.getDoer() == null
				? null
				: auditLog.getDoer().getKey();
		Long memberKey = auditLog.getIdentity() == null
				? null
				: auditLog.getIdentity().getKey();
		return new CertificationProgramLogRow(auditLog, context, object, memberKey, activity, actor, actorKey);
	}
	
	private CertificationProgramLogRow createMessageContentRow(CertificationProgramLog auditLog, String before, String after) {
		CertificationProgramLogRow row = createMessageRow(auditLog);
		row.setOriginalValue(before);
		row.setNewValue(after);
		return row;
	}
	
	private CertificationProgramLogRow createMessageStatusRow(CertificationProgramLog auditLog) {
		CertificationProgramLogRow row = createMessageRow(auditLog);
		row.setOriginalValue(translate("notification.status." + auditLog.getBeforeStatus()));
		row.setNewValue(translate("notification.status." + auditLog.getAfterStatus()));
		return row;
	}
	
	private CertificationProgramLogRow createMessageRow(CertificationProgramLog auditLog) {
		CertificationProgramMailConfiguration configuration = auditLog.getMailConfiguration();
		String object = configuration.getTitle();
		if(!StringHelper.containsNonWhitespace(object)) {
			object = translate("notifications.".concat(configuration.getType().name()));
		}
		return createRow(auditLog, CertificationProgramActivityLogContext.message, object, getActivity(auditLog));
	}
	
	private CertificationProgramLogRow createCertificateStatusRow(CertificationProgramLog auditLog) {
		CertificationProgramLogRow row = createCertificateRow(auditLog);
		row.setOriginalValue(getCertificationStatus(auditLog.getBeforeStatus()));
		row.setNewValue(getCertificationStatus(auditLog.getAfterStatus()));
		return row;
	}
	
	private String getCertificationStatus(String value) {
		if(CertificationStatus.isValid(value) || "certified".equals(value) || "removed".equals(value)) {
			return translate("certification.status.".concat(value.toLowerCase()));
		}
		return null;
	}
	
	private CertificationProgramLogRow createCertificateRow(CertificationProgramLog auditLog) {
		String object = auditLog.getIdentity() == null
				? null
				: userManager.getUserDisplayName(auditLog.getIdentity());
		return createRow(auditLog, CertificationProgramActivityLogContext.member, object, getActivity(auditLog));
	}
	
	private String getActivity(CertificationProgramLog auditLog, String... args) {
		return translate("activity.message." + auditLog.getAction(), args);
	}
	
	private String getMemberObject(CertificationProgramLog auditLog) {
		Identity member = auditLog.getCertificate().getIdentity();
		return userManager.getUserDisplayName(member);
	}
	
	private String getIdentityObject(CertificationProgramLog auditLog) {
		Identity identity = auditLog.getIdentity();
		return identity == null
				? null
				: userManager.getUserDisplayName(identity);
	}

	private String getCurriculumElementObject(CertificationProgramLog auditLog) {
		CurriculumElement element = auditLog.getCurriculumElement();
		return element == null ? null : element.getDisplayName();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof FlexiTableSearchEvent) {
				loadModel();
			} else if (event instanceof FlexiTableFilterTabEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
