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
package org.olat.modules.todo.ui;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.indicators.IndicatorsItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigPrefs;
import org.olat.core.gui.control.generic.dashboard.TableWidgetConfigProvider;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.DateRange;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTasksWidgetDataModel.WidgetCols;
import org.olat.modules.todo.ui.ToDoUIFactory.Due;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 6 May 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class ToDoTasksWidgetController extends TableWidgetController implements TableWidgetConfigProvider {

	private static final String CMD_OPEN = "open";

	private final Formatter formatter;
	private IndicatorsItem indicatorsEl;
	private FormLink showAllLink;
	private ToDoTasksWidgetDataModel dataModel;
	private FlexiTableElement tableEl;
	private Map<String, FormLink> keyToIndicatorLink;

	private SelectionValues figureValues;
	private String keyFigureKey;

	@Autowired
	private ToDoService toDoService;

	protected ToDoTasksWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(ToDoTasksWidgetController.class, getLocale(), getTranslator()));
		formatter = Formatter.getInstance(getLocale());
	}
	
	protected abstract String getBaseBusinessPath();

	protected abstract ToDoTaskSearchParams createBaseParams();
	
	protected abstract void doOpenRow(UserRequest ureq, ToDoTaskRow row);

	@Override
	protected TableWidgetConfigProvider getConfigProvider() {
		return this;
	}

	@Override
	public SelectionValues getFigureValues() {
		return figureValues;
	}

	@Override
	public TableWidgetConfigPrefs getDefault() {
		TableWidgetConfigPrefs prefs = new TableWidgetConfigPrefs();
		prefs.setKeyFigureKey("myToDos");
		prefs.setFocusFigureKeys(Set.of("open", "overdue", "new"));
		prefs.setNumRows(5);
		return prefs;
	}

	@Override
	public void update(TableWidgetConfigPrefs prefs) {
		keyFigureKey = prefs.getKeyFigureKey();

		FormLink keyIndicator = keyToIndicatorLink.get(keyFigureKey);
		indicatorsEl.setKeyIndicator(keyIndicator);

		List<FormItem> focusIndicators = keyToIndicatorLink.entrySet().stream()
				.filter(e -> prefs.getFocusFigureKeys().contains(e.getKey()))
				.map(Entry::getValue)
				.map(FormItem.class::cast)
				.toList();
		indicatorsEl.setFocusIndicatorsItems(focusIndicators);

		tableEl.setPageSize(prefs.getNumRows());

		reload();
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_todo_task\"> </i> " + translate("widget.todo.title");
	}

	@Override
	protected String createIndicators(FormLayoutContainer widgetCont) {
		indicatorsEl = IndicatorsFactory.createItem("indicators", widgetCont);

		keyToIndicatorLink = new LinkedHashMap<>();
		figureValues = new SelectionValues();
		createIndicator(widgetCont, "myToDos", "widget.todo.my", "[ToDos:0][" + ToDoTaskListController.TAB_ID_MY + ":0]");
		createIndicator(widgetCont, "open", "widget.todo.open", "[ToDos:0][" + ToDoTaskListController.TAB_ID_OPEN + ":0]");
		createIndicator(widgetCont, "overdue", "widget.todo.overdue", "[ToDos:0][" + ToDoTaskListController.TAB_ID_OVERDUE + ":0]");
		createIndicator(widgetCont, "new", "widget.todo.new", "[ToDos:0][" + ToDoTaskListController.TAB_ID_NEW + ":0]");

		return indicatorsEl.getComponent().getComponentName();
	}

	private void createIndicator(FormLayoutContainer widgetCont, String name, String labelI18nKey, String businessPath) {
		FormLink link = IndicatorsFactory.createIndicatorFormLink(name, CMD_OPEN, "", "", widgetCont);
		setUrl(link, businessPath, getBaseBusinessPath() + businessPath);
		keyToIndicatorLink.put(name, link);
		figureValues.add(SelectionValues.entry(name, translate(labelI18nKey)));
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		FlexiCellRenderer renderer = new TextFlexiCellRenderer(EscapeMode.none);
		renderer = wrapCellLink(renderer);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(WidgetCols.title, renderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(WidgetCols.priority, new ToDoPriorityCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(WidgetCols.dueDate, new ToDoDueDateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(WidgetCols.due, new ToDoDueCellRenderer()));

		dataModel = new ToDoTasksWidgetDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 5, false, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setShowSmallPageSize(false);

		return tableEl.getComponent().getComponentName();
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = DashboardUIFactory.createShowAllLink(widgetCont);
		String businessPath = "[ToDos:0][" + ToDoTaskListController.TAB_ID_ALL + ":0]";
		setUrl(showAllLink, businessPath, getBaseBusinessPath() + businessPath);
		return showAllLink.getComponent().getComponentName();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				doOpenRow(ureq, dataModel.getObject(se.getIndex()));
			}
		} else if (source == showAllLink && showAllLink.getUserObject() instanceof String businessPath) {
			doOpen(ureq, businessPath);
		} else if (source instanceof FormLink link) {
			if (CMD_OPEN.equals(link.getCmd())) {
				if (link.getUserObject() instanceof String businessPath) {
					doOpen(ureq, businessPath);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	protected void doOpen(UserRequest ureq, String businessPath) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(businessPath);
		fireEvent(ureq, new ActivateEvent(entries));
	}

	protected void updateIndicator(String key, long count, String i18nKey) {
		FormLink link = keyToIndicatorLink.get(key);
		if (link != null) {
			link.setI18nKey(IndicatorsFactory.createLinkText(translate(i18nKey), String.valueOf(count)));
		}
	}
	
	private void reload() {
		Date newReference = DateUtils.addDays(new Date(), -1);
		
		long myCount = toDoService.getToDoTaskCount(createMyToDosParams());
		long openCount = toDoService.getToDoTaskCount(createOpenParams());
		long overdueCount = toDoService.getToDoTaskCount(createOverdueParams());
		long newCount = toDoService.getToDoTaskCount(createNewParams(newReference));

		updateIndicator("myToDos", myCount, "widget.todo.my");
		updateIndicator("open", openCount, "widget.todo.open");
		updateIndicator("overdue", overdueCount, "widget.todo.overdue");
		updateIndicator("new", newCount, "widget.todo.new");

		ToDoTaskSearchParams rowParams = switch (keyFigureKey) {
			case "open" -> createOpenParams();
			case "overdue" -> createOverdueParams();
			case "new" -> createNewParams(newReference);
			default -> createMyToDosParams();
		};
		List<ToDoTask> tasks = toDoService.getToDoTasks(rowParams);
		updateTableRows(tasks, newReference);
	}

	private void updateTableRows(List<ToDoTask> tasks, Date newReference) {
		LocalDate now = LocalDate.now();
		
		List<ToDoTaskRow> rows = tasks.stream()
				.sorted(Comparator.comparing(ToDoTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
								.thenComparing(ToDoTask::getTitle, Comparator.nullsLast(Comparator.naturalOrder())))
				.map(task -> toRow(task, now, newReference))
				.limit(tableEl.getPageSize())
				.toList();
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private ToDoTaskRow toRow(ToDoTask task, LocalDate now, Date newReference) {
		ToDoTaskRow row = new ToDoTaskRow(task);
		String displayName = ToDoUIFactory.getDisplayName(getTranslator(), task);
		displayName = StringHelper.escapeHtml(displayName);
		if (newReference != null && task.getCreationDate() != null && newReference.before(task.getCreationDate())) {
			displayName += "<span class=\"o_labeled_light o_todo_new\">" + translate("new.label") + "</span>";
		}
		displayName = "<span>" + displayName + "</span>";
		row.setDisplayName(displayName);

		if (task.getDueDate() != null) {
			row.setFormattedDueDate(formatter.formatDate(task.getDueDate()));
			Due due = ToDoUIFactory.getDue(getTranslator(), DateUtils.toLocalDate(task.getDueDate()), now, task.getStatus());
			row.setDue(due.name());
			row.setOverdue(due.overdue());
		}
		return row;
	}
	
	private ToDoTaskSearchParams createMyToDosParams() {
		ToDoTaskSearchParams params = createBaseParams();
		params.setStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
		return params;
	}

	private ToDoTaskSearchParams createOpenParams() {
		ToDoTaskSearchParams params = createBaseParams();
		params.setStatus(List.of(ToDoStatus.open));
		return params;
	}

	private ToDoTaskSearchParams createOverdueParams() {
		ToDoTaskSearchParams params = createBaseParams();
		params.setStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
		DateRange dateRange = ToDoDueFilter.overdue.getDateRange(new Date());
		params.setDueDateRanges(List.of(dateRange));
		return params;
	}

	private ToDoTaskSearchParams createNewParams(Date reference) {
		ToDoTaskSearchParams params = createBaseParams();
		params.setStatus(List.of(ToDoStatus.open, ToDoStatus.inProgress));
		params.setCreatedAfter(reference);
		return params;
	}
	
}
