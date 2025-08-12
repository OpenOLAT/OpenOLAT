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
package org.olat.modules.topicbroker.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.commons.controllers.activity.ActivityLogTableModel.ActivityLogCols;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.CssCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.topicbroker.TBAuditLog;
import org.olat.modules.topicbroker.TBAuditLog.Action;
import org.olat.modules.topicbroker.TBAuditLogSearchParams;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBBrokerRef;
import org.olat.modules.topicbroker.TBCustomField;
import org.olat.modules.topicbroker.TBCustomFieldDefinition;
import org.olat.modules.topicbroker.TBCustomFieldDefinitionSearchParams;
import org.olat.modules.topicbroker.TBCustomFieldType;
import org.olat.modules.topicbroker.TBEnrollmentStrategyType;
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBParticipantCandidates;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;
import org.olat.modules.topicbroker.TBTopicSearchParams;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.manager.TopicBrokerXStream;
import org.olat.modules.topicbroker.ui.TBActivityLogTableModel.TBActivityLogCols;
import org.olat.modules.topicbroker.ui.components.TBActivityLogContextRenderer;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import io.reactivex.rxjava3.functions.Function;

/**
 * 
 * Initial date: Aug 5, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBActivityLogController extends FormBasicController {
	
	static final String FILTER_CONTEXT = "context";
	static final String FILTER_ACTIVITY = "activity";
	static final String FILTER_PARTICIPANT = "participant";
	static final String FILTER_TOPIC = "topic";
	static final String FILTER_IDENTITY = "user";
	static final String FILTER_DATE = "date";
	private static final String TAB_ID_LAST_7_DAYS = "Last7Days";
	private static final String TAB_ID_LAST_4_WEEKS = "Last4Weeks";
	private static final String TAB_ID_LAST_12_MONTH = "Last12Month";
	private static final String TAB_ID_ALL = "All";
	private static final Collection<Action> ACTIONS_ALL = Set.of(
			Action.brokerUpdateContent,
			Action.brokerEnrollmentDone,
			Action.brokerEnrollmentStrategy,
			Action.brokerEnrollmentStrategyValue,
			Action.brokerEnrollmentReset,
			Action.participantUpdateContent,
			Action.topicUpdateContent,
			Action.topicUpdateSortOrder,
			Action.topicUpdateFile,
			Action.topicDeleteFile,
			Action.customFieldUpdateContent,
			Action.customFieldDeletePermanently,
			Action.selectionCreate,
			Action.selectionUpdateSortOrder,
			Action.selectionEnrollManually,
			Action.selectionEnrollProcessMan,
			Action.selectionEnrollProcessAuto,
			Action.selectionWithdrawManually,
			Action.selectionWithdrawProcessMan,
			Action.selectionWithdrawProcessAuto,
			Action.selectionDelete
			);
	private static final Collection<Action> ACTIONS_TOPIC = Set.of(
			Action.topicUpdateContent,
			Action.topicUpdateSortOrder,
			Action.topicUpdateFile,
			Action.topicDeleteFile,
			Action.customFieldUpdateContent,
			Action.customFieldDeletePermanently,
			Action.selectionCreate,
			Action.selectionUpdateSortOrder,
			Action.selectionEnrollManually,
			Action.selectionEnrollProcessMan,
			Action.selectionEnrollProcessAuto,
			Action.selectionWithdrawManually,
			Action.selectionWithdrawProcessMan,
			Action.selectionWithdrawProcessAuto,
			Action.selectionDelete
			);
	private static final Collection<Action> ACTIONS_PARTICIPANT = Set.of(
			Action.participantUpdateContent,
			Action.selectionCreate,
			Action.selectionUpdateSortOrder,
			Action.selectionEnrollManually,
			Action.selectionEnrollProcessMan,
			Action.selectionEnrollProcessAuto,
			Action.selectionWithdrawManually,
			Action.selectionWithdrawProcessMan,
			Action.selectionWithdrawProcessAuto,
			Action.selectionDelete
			);

	private TBActivityLogTableModel dataModel;
	private FlexiTableElement tableEl;

	private final TBParticipantCandidates participantCandidates;
	private final TBTopic topic;
	private final TBParticipant participant;
	private final Collection<Action> actions;
	private final Formatter formatter;
	private final List<TBCustomFieldDefinition> customFieldDefinitions;
	private final Map<Long, String> topicKeyToTitle;
	private final Map<Long, String> groupKeyToName = new HashMap<>(1);
	private final TBAuditLogSearchParams auditLogSearchParams;
	
	@Autowired
	private TopicBrokerService topicBrokeService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public TBActivityLogController(UserRequest ureq, WindowControl wControl, TBBrokerRef broker,
			TBParticipantCandidates participantCandidates, TBTopic topic, TBParticipant participant) {
		super(ureq, wControl, "audit_log");
		setTranslator(Util.createPackageTranslator(ActivityLogController.class, getLocale(), getTranslator()));
		this.participantCandidates = participantCandidates;
		this.topic = topic;
		this.participant = participant;
		if (participant != null) {
			actions = ACTIONS_PARTICIPANT;
		} else if (topic != null) {
			actions = ACTIONS_TOPIC;
		} else {
			actions = ACTIONS_ALL;
		}
		formatter = Formatter.getInstance(getLocale());
		
		TBTopicSearchParams topicSearchParams = new TBTopicSearchParams();
		topicSearchParams.setBroker(broker);
		topicKeyToTitle = topicBrokeService.getTopics(topicSearchParams).stream()
			.collect(Collectors.toMap(TBTopic::getKey, TBTopic::getTitle));
		
		TBCustomFieldDefinitionSearchParams definitionSearchParams = new TBCustomFieldDefinitionSearchParams();
		definitionSearchParams.setBroker(broker);
		customFieldDefinitions = topicBrokeService.getCustomFieldDefinitions(definitionSearchParams);
		
		auditLogSearchParams = new TBAuditLogSearchParams();
		auditLogSearchParams.setBroker(broker);
		auditLogSearchParams.setTopic(topic);
		auditLogSearchParams.setParticipant(participant);
		auditLogSearchParams.setActions(actions);
		auditLogSearchParams.setFetchAll(true);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("activity.log.title");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setDefaultOrderBy(new SortKey(ActivityLogCols.date.name(), false));

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.date, new CssCellRenderer("o_nowrap")));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.context, new TBActivityLogContextRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.object));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.activity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.valueOriginal));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.valueNew));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TBActivityLogCols.user));
		
		dataModel = new TBActivityLogTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setExportEnabled(true);
		tableEl.setSortSettings(options);

		initFilters();
		initFilterTabs(ureq);
		loadModel(true);
	}
	
	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		Date now = DateUtils.getStartOfDay(new Date());
		Date end = DateUtils.getEndOfDay(new Date());
		
		DateRange last7Days = new DateRange();
		last7Days.setStart(DateUtils.addDays(now, -7));
		last7Days.setEnd(end);
		FlexiFiltersTab last7DaysTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_7_DAYS, translate("tab.last.7.days"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last7Days)));
		tabs.add(last7DaysTab);
		
		DateRange last4Weeks = new DateRange();
		last4Weeks.setStart(DateUtils.addWeeks(now, -4));
		last4Weeks.setEnd(end);
		FlexiFiltersTab last4WeeksTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_4_WEEKS, translate("tab.last.4.weeks"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last4Weeks)));
		tabs.add(last4WeeksTab);

		DateRange last12Months = new DateRange();
		last12Months.setStart(DateUtils.addMonth(now, -12));
		last12Months.setEnd(end);
		FlexiFiltersTab last12MonthsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(TAB_ID_LAST_12_MONTH, translate("tab.last.12.month"),
				TabSelectionBehavior.clear, List.of(
						FlexiTableFilterValue.valueOf(FILTER_DATE, last12Months)));
		tabs.add(last12MonthsTab);
		
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithFilters(TAB_ID_ALL, translate("tab.all"),
				TabSelectionBehavior.clear, List.of());
		tabs.add(allTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(3);
		
		SelectionValues contextSV = new SelectionValues();
		if (topic == null && participant == null) {
			addContext(contextSV, TBActivityLogContext.configuration);
		}
		addContext(contextSV, TBActivityLogContext.topic);
		addContext(contextSV, TBActivityLogContext.participant);
		if (topic == null && participant == null) {
			addContext(contextSV, TBActivityLogContext.enrollmentProcess);
		}
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.context"), FILTER_CONTEXT,
				contextSV, true));
		
		filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.activity"), FILTER_ACTIVITY,
				getActivityFilterValues(), true));
		
		if (participant == null) {
			SelectionValues identityValues = new SelectionValues();
			List<Identity> filterIdentities = participantCandidates.getVisibleIdentities();
			if (filterIdentities != null && !filterIdentities.isEmpty()) {
				filterIdentities.stream().forEach(identity -> identityValues.add(
						SelectionValues.entry(
								identity.getKey().toString(),
								StringHelper.escapeHtml(userManager.getUserDisplayName(identity.getKey())))));
				identityValues.sort(SelectionValues.VALUE_ASC);
				if (!identityValues.isEmpty()) {
					filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.participant"), FILTER_PARTICIPANT,
							identityValues, true));
				}
			}
		}
		
		if (topic == null && topicKeyToTitle != null && !topicKeyToTitle.isEmpty()) {
			SelectionValues topicValues = new SelectionValues();
			topicKeyToTitle.entrySet().forEach(keyToTitle -> topicValues.add(
						SelectionValues.entry(
								keyToTitle.getKey().toString(),
								StringHelper.escapeHtml(keyToTitle.getValue()))));
				topicValues.sort(SelectionValues.VALUE_ASC);
				if (!topicValues.isEmpty()) {
					filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.topic"), FILTER_TOPIC,
							topicValues, true));
				}
		}
		
		List<Identity> doers = topicBrokeService.getAuditLogDoers(auditLogSearchParams);
		if (doers != null && !doers.isEmpty()) {
			SelectionValues identityValues = new SelectionValues();
			doers.forEach(identity -> identityValues.add(
					SelectionValues.entry(
							identity.getKey().toString(),
							StringHelper.escapeHtml(userManager.getUserDisplayName(identity.getKey())))));
			identityValues.sort(SelectionValues.VALUE_ASC);
			if (!identityValues.isEmpty()) {
				filters.add(new FlexiTableMultiSelectionFilter(translate("activity.log.user"), FILTER_IDENTITY,
						identityValues, true));
			}
		}
		
		FlexiTableDateRangeFilter dateFilter = new FlexiTableDateRangeFilter(translate("activity.log.date"), FILTER_DATE,
				true, true, getLocale());
		filters.add(dateFilter);
		
		tableEl.setFilters(true, filters, false, false);
	}

	private void addContext(SelectionValues contextSV, TBActivityLogContext context) {
		contextSV.add(SelectionValues.entry(context.name(), TBUIFactory.getTranslatedLogContext(getTranslator(), context)));
	}
	
	public void reload() {
		loadModel(false);
		dataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(false, false, true);
	}
	
	private void loadModel(boolean reset) {
		List<TBAuditLog> auditLogs = topicBrokeService.getAuditLog(auditLogSearchParams, 0, -1);
		List<TBActivityLogRow> rows = new ArrayList<>();
		
		for (TBAuditLog auditLog : auditLogs) {
			try {
				if (auditLog.getParticipant() == null
						|| participantCandidates.isAllIdentitiesVisible()
						|| participantCandidates.getVisibleIdentities().contains(auditLog.getParticipant().getIdentity())) {
					List<TBActivityLogRow> logRows = toRows(auditLog);
					if (logRows != null && !logRows.isEmpty()) {
						rows.addAll(logRows);
					}
				}
			} catch (com.thoughtworks.xstream.security.ForbiddenClassException fce) {
				// Probably a org.hibernate.proxy.pojo.bytebuddy.SerializableProxy: ignore it.
			} catch (com.thoughtworks.xstream.converters.ConversionException ce) {
				// Probably a org.hibernate.proxy.pojo.bytebuddy.SerializableProxy: ignore it.
			} catch (com.thoughtworks.xstream.mapper.CannotResolveClassException crce) {
				// Probably a org.hibernate.proxy.pojo.bytebuddy.SerializableProxy: ignore it.
			}
		}
		Collections.sort(rows, (r1, r2) -> r2.getDate().compareTo(r1.getDate()));
		
		dataModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
	}

	private void filterModel() {
		dataModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
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
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private SelectionValues getActivityFilterValues() {
		SelectionValues filterSV = new SelectionValues();
		
		if (actions.contains(Action.brokerUpdateContent)) {
			addSelectionValue(filterSV, translate("activity.configuration.change.max.selection"));
			addSelectionValue(filterSV, translate("activity.configuration.change.selection.period.begin"));
			addSelectionValue(filterSV, translate("activity.configuration.change.selection.period.end"));
			addSelectionValue(filterSV, translate("activity.configuration.change.required.enrollments"));
			addSelectionValue(filterSV, translate("activity.configuration.change.participant.can.reduce.enrollments"));
			addSelectionValue(filterSV, translate("activity.configuration.change.participant.can.withdraw"));
			addSelectionValue(filterSV, translate("activity.configuration.change.withdraw.end"));
			addSelectionValue(filterSV, translate("activity.configuration.change.enrollment.auto"));
			addSelectionValue(filterSV, translate("activity.configuration.change.auto.enrollment.strategy"));
			addSelectionValue(filterSV, translate("activity.configuration.change.overlapping.not.allowed"));
		}
		if (actions.contains(Action.brokerEnrollmentDone)) {
			addSelectionValue(filterSV, translate("activity.enrollment.done"));
		}
		if (actions.contains(Action.brokerEnrollmentStrategy)) {
			addSelectionValue(filterSV, translate("activity.enrollment.strategy"));
		}
		if (actions.contains(Action.brokerEnrollmentStrategyValue)) {
			addSelectionValue(filterSV, translate("activity.enrollment.stats"));
		}
		if (actions.contains(Action.brokerEnrollmentReset)) {
			addSelectionValue(filterSV, translate("activity.enrollment.reset"));
		}
		if (actions.contains(Action.participantUpdateContent)) {
			addSelectionValue(filterSV, translate("activity.participant.change.max.boost"));
			addSelectionValue(filterSV, translate("activity.participant.change.required.enrollments"));
		}
		if (actions.contains(Action.topicUpdateContent)) {
			addSelectionValue(filterSV, translate("activity.topic.change.title"));
			addSelectionValue(filterSV, translate("activity.topic.change.description"));
			addSelectionValue(filterSV, translate("activity.topic.change.begin.date"));
			addSelectionValue(filterSV, translate("activity.topic.change.end.date"));
			addSelectionValue(filterSV, translate("activity.topic.change.min.participants"));
			addSelectionValue(filterSV, translate("activity.topic.change.max.participants"));
			addSelectionValue(filterSV, translate("activity.topic.change.group.restriction"));
		}
		if (actions.contains(Action.topicUpdateSortOrder)) {
			addSelectionValue(filterSV, translate("activity.topic.change.sort.order"));
		}
		if (actions.contains(Action.topicUpdateFile) || actions.contains(Action.topicDeleteFile)) {
			addSelectionValue(filterSV, translate("activity.topic.file.updated.teaserimage"));
			addSelectionValue(filterSV, translate("activity.topic.file.updated.teaservideo"));
		}
		if (actions.contains(Action.customFieldUpdateContent)) {
			for (TBCustomFieldDefinition customFieldDefinition : customFieldDefinitions) {
				addSelectionValue(filterSV, translate("activity.topic.change.custom", StringHelper.escapeHtml(customFieldDefinition.getName())));
			}
		}
		if (actions.contains(Action.selectionCreate)) {
			addSelectionValue(filterSV, translate("activity.selection.selected"));
		}
		if (actions.contains(Action.selectionUpdateSortOrder)) {
			addSelectionValue(filterSV, translate("activity.selection.change.order"));
		}
		if (actions.contains(Action.selectionEnrollManually)) {
			addSelectionValue(filterSV, translate("activity.selection.enrolled.manually"));
		}
		if (actions.contains(Action.selectionEnrollProcessMan) || actions.contains(Action.selectionEnrollProcessAuto)) {
			addSelectionValue(filterSV, translate("activity.selection.enrolled"));
		}
		if (actions.contains(Action.selectionWithdrawManually)) {
			addSelectionValue(filterSV, translate("activity.selection.withdraw.manually"));
		}
		if (actions.contains(Action.selectionWithdrawProcessMan) || actions.contains(Action.selectionWithdrawProcessAuto)) {
			addSelectionValue(filterSV, translate("activity.selection.withdraw"));
		}
		if (actions.contains(Action.selectionDelete)) {
			addSelectionValue(filterSV, translate("activity.selection.unselected"));
		}
		
		filterSV.sort(SelectionValues.VALUE_ASC);
		return filterSV;
	}

	private void addSelectionValue(SelectionValues filterSV, String string) {
		filterSV.add(SelectionValues.entry(string, string));
	}

	private List<TBActivityLogRow> toRows(TBAuditLog auditLog) {
		switch (auditLog.getAction()) {
		case brokerUpdateContent: {
			List<TBActivityLogRow> rows = new ArrayList<>(2);
			TBBroker before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBBroker.class);
			TBBroker after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBBroker.class);
			
			String valueOriginal = valueOrBlank(before, TBBroker::getMaxSelections);
			String valueNew = valueOrBlank(after, TBBroker::getMaxSelections);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.max.selection", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::getSelectionStartDate, date -> formatter.formatDateAndTime(date));
			valueNew = valueOrBlank(after, TBBroker::getSelectionStartDate, date -> formatter.formatDateAndTime(date));
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.selection.period.begin", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::getSelectionEndDate, date -> formatter.formatDateAndTime(date));
			valueNew = valueOrBlank(after, TBBroker::getSelectionEndDate, date -> formatter.formatDateAndTime(date));
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.selection.period.end", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::getRequiredEnrollments);
			valueNew = valueOrBlank(after, TBBroker::getRequiredEnrollments);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.required.enrollments", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::isParticipantCanEditRequiredEnrollments);
			valueNew = valueOrBlank(after, TBBroker::isParticipantCanEditRequiredEnrollments);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.participant.can.reduce.enrollments", translateYesNo(valueOriginal), translateYesNo(valueNew));
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::isParticipantCanWithdraw);
			valueNew = valueOrBlank(after, TBBroker::isParticipantCanWithdraw);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.participant.can.withdraw", translateYesNo(valueOriginal), translateYesNo(valueNew));
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::getWithdrawEndDate, date -> formatter.formatDateAndTime(date));
			valueNew = valueOrBlank(after, TBBroker::getWithdrawEndDate, date -> formatter.formatDateAndTime(date));
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.withdraw.end", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::isAutoEnrollment);
			valueNew = valueOrBlank(after, TBBroker::isAutoEnrollment);
			if (!Objects.equals(valueOriginal, valueNew)) {
				String translatedValueOriginal = null;
				if (StringHelper.containsNonWhitespace(valueOriginal)) {
					translatedValueOriginal = Boolean.parseBoolean(valueOriginal)? translate("activity.configuration.change.enrollment.auto.auto"): translate("activity.configuration.change.enrollment.auto.manually");
				}
				String translatedValueNew = null;
				if (StringHelper.containsNonWhitespace(valueNew)) {
					translatedValueNew = Boolean.parseBoolean(valueNew)? translate("activity.configuration.change.enrollment.auto.auto"): translate("activity.configuration.change.enrollment.auto.manually");
				}
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.enrollment.auto", translatedValueOriginal, translatedValueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::getAutoEnrollmentStrategyType);
			valueNew = valueOrBlank(after, TBBroker::getAutoEnrollmentStrategyType);
			if (!Objects.equals(valueOriginal, valueNew)) {
				String translatedValueOriginal = null;
				if (StringHelper.containsNonWhitespace(valueOriginal)) {
					translatedValueOriginal = TBUIFactory.getTranslatedType(getTranslator(), TBEnrollmentStrategyType.valueOf(valueOriginal));
				}
				String translatedValueNew = null;
				if (StringHelper.containsNonWhitespace(valueNew)) {
					translatedValueNew = TBUIFactory.getTranslatedType(getTranslator(), TBEnrollmentStrategyType.valueOf(valueNew));
				}
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.auto.enrollment.strategy", translatedValueOriginal, translatedValueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBBroker::isOverlappingPeriodAllowed);
			valueNew = valueOrBlank(after, TBBroker::isOverlappingPeriodAllowed);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.configuration, null, "activity.configuration.change.overlapping.not.allowed", translateYesNo(valueOriginal, true), translateYesNo(valueNew, true));
			}
			
			return rows;
		}
		case brokerEnrollmentDone: {
			return List.of(createRow(auditLog, TBActivityLogContext.enrollmentProcess, null, "activity.enrollment.done", null, null));
		}
		case brokerEnrollmentStrategy: {
			// Log the XML. May be good enough.
			return List.of(createRow(auditLog, TBActivityLogContext.enrollmentProcess, null, "activity.enrollment.strategy", auditLog.getBefore(), null));
		}
		case brokerEnrollmentStrategyValue: {
			// Log the XML. May be good enough.
			return List.of(createRow(auditLog, TBActivityLogContext.enrollmentProcess, null, "activity.enrollment.stats", auditLog.getBefore(), null));
		}
		case brokerEnrollmentReset: {
			return List.of(createRow(auditLog, TBActivityLogContext.enrollmentProcess, null, "activity.enrollment.reset", null, null));
		}
		case participantUpdateContent: {
			List<TBActivityLogRow> rows = new ArrayList<>(1);
			TBParticipant before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBParticipant.class);
			TBParticipant after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBParticipant.class);
			
			String valueOriginal = valueOrBlank(before, TBParticipant::getBoost);
			String valueNew = valueOrBlank(after, TBParticipant::getBoost);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.participant.change.max.boost", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBParticipant::getRequiredEnrollments);
			valueNew = valueOrBlank(after, TBParticipant::getRequiredEnrollments);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.participant.change.required.enrollments", valueOriginal, valueNew);
			}
			
			return rows;
		}
		case topicUpdateContent: {
			List<TBActivityLogRow> rows = new ArrayList<>(2);
			TBTopic before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBTopic.class);
			TBTopic after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBTopic.class);
			
			String valueOriginal = valueOrBlank(before, TBTopic::getTitle);
			String valueNew = valueOrBlank(after, TBTopic::getTitle);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.title", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBTopic::getDescription);
			valueNew = valueOrBlank(after, TBTopic::getDescription);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.description", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBTopic::getBeginDate, date -> formatter.formatDateAndTime(date));
			valueNew = valueOrBlank(after, TBTopic::getBeginDate, date -> formatter.formatDateAndTime(date));
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.begin.date", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBTopic::getEndDate, date -> formatter.formatDateAndTime(date));
			valueNew = valueOrBlank(after, TBTopic::getEndDate, date -> formatter.formatDateAndTime(date));
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.end.date", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBTopic::getMinParticipants);
			valueNew = valueOrBlank(after, TBTopic::getMinParticipants);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.min.participants", valueOriginal, valueNew);
			}
			
			valueOriginal = valueOrBlank(before, TBTopic::getMaxParticipants);
			valueNew = valueOrBlank(after, TBTopic::getMaxParticipants);
			if (!Objects.equals(valueOriginal, valueNew)) {
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.max.participants", valueOriginal, valueNew);
			}
			
			Set<Long> beforeGroupRestriction = before != null? before.getGroupRestrictionKeys(): Set.of();
			Set<Long> afterGroupRestriction = after != null? after.getGroupRestrictionKeys(): Set.of();
			if (!Objects.equals(beforeGroupRestriction, afterGroupRestriction)) {
				valueOriginal = getGroupNames(beforeGroupRestriction);
				valueNew = getGroupNames(afterGroupRestriction);
				addRow(rows, auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.group.restriction", valueOriginal, valueNew);
			}
			
			return rows;
		}
		case topicUpdateSortOrder: {
			TBTopic before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBTopic.class);
			TBTopic after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBTopic.class);
			
			String valueOriginal = valueOrBlank(before, TBTopic::getSortOrder);
			String valueNew = valueOrBlank(after, TBTopic::getSortOrder);
			if (!Objects.equals(valueOriginal, valueNew)) {
				return List.of(createRow(auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.change.sort.order", valueOriginal, valueNew));
			}
			return null;
		}
		case topicUpdateFile:
		case topicDeleteFile: {
			TBAuditLog.TBFileAuditLog before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBAuditLog.TBFileAuditLog.class);
			TBAuditLog.TBFileAuditLog after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBAuditLog.TBFileAuditLog.class);
			
			String valueOriginal = valueOrBlank(before, TBAuditLog.TBFileAuditLog::getFilename);
			String valueNew = valueOrBlank(after, TBAuditLog.TBFileAuditLog::getFilename);
			
			if (before != null || after != null) {
				String identifier = before != null? before.getIdentifier(): after != null? after.getIdentifier(): null;
				if (TopicBrokerService.TEASER_IMAGE_DIR.equals(identifier)) {
					return List.of(createRow(auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.file.updated.teaserimage", valueOriginal, valueNew));
				} else if (TopicBrokerService.TEASER_VIDEO_DIR.equals(identifier)) {
					return List.of(createRow(auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), "activity.topic.file.updated.teaservideo", valueOriginal, valueNew));
				}
				// else custom field file
			}
			return null;
		}
		case customFieldUpdateContent:
		case customFieldDeletePermanently: {
			TBCustomField before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBCustomField.class);
			TBCustomField after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBCustomField.class);
			
			if (TBCustomFieldType.text == auditLog.getDefinition().getType()) {
				String valueOriginal = valueOrBlank(before, TBCustomField::getText);
				String valueNew = valueOrBlank(after, TBCustomField::getText);
				if (!Objects.equals(valueOriginal, valueNew)) {
					TBActivityLogRow row = createRow(auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), null, valueOriginal, valueNew);
					row.setTranslatedActivity(translate( "activity.topic.change.custom", auditLog.getDefinition().getName()));
					return List.of(row);
				}
			} else if (TBCustomFieldType.file == auditLog.getDefinition().getType()) {
				String valueOriginal = valueOrBlank(before, TBCustomField::getFilename);
				String valueNew = valueOrBlank(after, TBCustomField::getFilename);
				if (!Objects.equals(valueOriginal, valueNew)) {
					TBActivityLogRow row = createRow(auditLog, TBActivityLogContext.topic, getTopicObject(auditLog), null, valueOriginal, valueNew);
					row.setTranslatedActivity(translate( "activity.topic.change.custom", auditLog.getDefinition().getName()));
					return List.of(row);
				}
			}
			return null;
		}
		case selectionCreate: {
			TBSelection after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBSelection.class);
			
			String valueNew = getTopicTitle(after) + ": " + translate("selection.priority.no", valueOrBlank(after, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.selected", null, valueNew));
		}
		case selectionUpdateSortOrder: {
			TBSelection before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBSelection.class);
			TBSelection after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBSelection.class);
		
			String valueOriginal = getTopicTitle(before) + ": " + translate("selection.priority.no", valueOrBlank(before, TBSelection::getSortOrder));
			String valueNew = getTopicTitle(after) + ": " + translate("selection.priority.no", valueOrBlank(after, TBSelection::getSortOrder));
		
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.change.order", valueOriginal, valueNew));
		}
		case selectionEnrollManually:{
			TBSelection after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBSelection.class);
			
			String valueNew = getTopicTitle(after) + ": " + translate("selection.priority.no", valueOrBlank(after, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.enrolled.manually", null, valueNew));
		}
		case selectionEnrollProcessMan:
		case selectionEnrollProcessAuto: {
			TBSelection after = TopicBrokerXStream.fromXml(auditLog.getAfter(), TBSelection.class);
			
			String valueNew = getTopicTitle(after) + ": " + translate("selection.priority.no", valueOrBlank(after, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.enrolled", null, valueNew));
		}
		case selectionWithdrawManually: {
			TBSelection before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBSelection.class);
			
			String valueOriginal = getTopicTitle(before) + ": " + translate("selection.priority.no", valueOrBlank(before, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.withdraw.manually", valueOriginal, null));
		}
		case selectionWithdrawProcessMan:
		case selectionWithdrawProcessAuto: {
			TBSelection before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBSelection.class);
			
			String valueOriginal = getTopicTitle(before) + ": " + translate("selection.priority.no", valueOrBlank(before, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.withdraw", valueOriginal, null));
		}
		case selectionDelete: {
			TBSelection before = TopicBrokerXStream.fromXml(auditLog.getBefore(), TBSelection.class);
			
			String valueOriginal = getTopicTitle(before) + ": " + translate("selection.priority.no", valueOrBlank(before, TBSelection::getSortOrder));
			
			return List.of(createRow(auditLog, TBActivityLogContext.participant, getParticipantObject(auditLog), "activity.selection.unselected", valueOriginal, null));
		}
		
		default:
			//
		}
		
		return null;
	}

	private void addRow(List<TBActivityLogRow> rows, TBAuditLog auditLog, TBActivityLogContext context, String object,
			String activityI18nKey, String valueBefore, String valueNew) {
		rows.add(createRow(auditLog, context, object, activityI18nKey, valueBefore, valueNew));
	}
	
	private TBActivityLogRow createRow(TBAuditLog auditLog, TBActivityLogContext context, String object,
			String activityI18nKey, String valueBefore, String valueNew) {
		TBActivityLogRow row = createRow(auditLog);
		row.setContext(context);
		row.setObject(object);
		if (activityI18nKey != null) {
			row.setTranslatedActivity(translate(activityI18nKey));
		}
		row.setValueOriginal(valueBefore);
		row.setValueNew(valueNew);
		return row;
	}
	
	private TBActivityLogRow createRow(TBAuditLog auditLog) {
		TBActivityLogRow row = new TBActivityLogRow();
		row.setDate(auditLog.getCreationDate());
		row.setDoerDisplayName(userManager.getUserDisplayName(auditLog.getDoer()));
		if (auditLog.getParticipant() != null && auditLog.getParticipant().getIdentity() != null) {
			row.setParticipantKey(auditLog.getParticipant().getIdentity().getKey());
		}
		if (auditLog.getTopic() != null) {
			row.setTopicKey(auditLog.getTopic().getKey());
		}
		if (auditLog.getDoer() != null && auditLog.getDoer() != null) {
			row.setIdentityKey(auditLog.getDoer().getKey());
		}
		return row;
	}

	private String getParticipantObject(TBAuditLog auditLog) {
		return auditLog.getParticipant() != null? userManager.getUserDisplayName(auditLog.getParticipant().getIdentity()): null;
	}
	
	private String getTopicObject(TBAuditLog auditLog) {
		return auditLog.getTopic() != null? auditLog.getTopic().getTitle(): null;
	}

	private String getTopicTitle(TBSelection selection) {
		return topicKeyToTitle.getOrDefault(selection.getTopic().getKey(), selection.getTopic().getTitle());
	}
	
	private String getGroupNames(Set<Long> groupRestrictionKeys) {
		if (groupRestrictionKeys == null || groupRestrictionKeys.isEmpty()) {
			return "";
		}
		
		List<String> groupNames = new ArrayList<>(groupRestrictionKeys.size());
		for (Long groupKey : groupRestrictionKeys) {
			String groupName = groupKeyToName.computeIfAbsent(groupKey, key -> {
				BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
				return group != null? group.getName(): key.toString();
			});
			groupNames.add(groupName);
		}
		
		return groupNames.stream().sorted().collect(Collectors.joining(", "));
	}
	
	private <T, R> String valueOrBlank(T object, Function<T, R> function) {
		return valueOrBlank(object, function, Object::toString);
	}
	
	private <T, R> String valueOrBlank(T object, Function<T, R> function, Function<R, String> stringFunction) {
		R value = valueIfNotNull(object, function);
		if (value != null) {
			try {
				return stringFunction.apply(value);
			} catch (Throwable e) {
				//
			}
		}
		return "";
	}
	
	private <T, R> R valueIfNotNull(T object, Function<T, R> function) {
		if (object != null) {
			try {
				return function.apply(object);
			} catch (Throwable e) {
				//
			}
		}
		return null;
	}
	
	private String translateYesNo(String value) {
		return translateYesNo(value, false);
	}
	
	private String translateYesNo(String value, boolean oposite) {
		if (StringHelper.containsNonWhitespace(value)) {
			boolean parseedBoolean = Boolean.parseBoolean(value);
			if (oposite) {
				parseedBoolean = !parseedBoolean;
			}
			return parseedBoolean? translate("yes"): translate("no");
		}
		return null;
	}

}
