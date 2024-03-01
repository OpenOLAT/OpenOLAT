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
package org.olat.modules.quality.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.YesNoCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter.DateRange;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.project.ProjNoteFilter;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityDataCollectionTopicType;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.modules.quality.manager.DataCollectionToDoTaskProvider;
import org.olat.modules.quality.manager.EvaluationFormSessionToDoTaskProvider;
import org.olat.modules.quality.ui.DataCollectionDataModel.DataCollectionCols;
import org.olat.modules.quality.ui.DataCollectionDataSource.DataCollectionDataSourceUIFactory;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoTaskSelectionEvent;
import org.olat.modules.todo.ui.ToDoTasksCompactController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionListController extends FormBasicController
		implements DataCollectionDataSourceUIFactory, TooledController, Activateable2 {

	private enum DataCollectionFilter { form, title, topicType, topic, key, generator, status, start, deadline, toDos }
	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_PREPERATION = "Preperation";
	private static final String TAB_ID_READY = "Ready";
	private static final String TAB_ID_RUNNING = "Running";
	private static final String TAB_ID_FINISHED = "Finshed";
	private static final String FILTER_KEY_TODO = "todo";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_TOPIC = "topic";
	private static final String CMD_TODOS = "todos";
	
	private final TooledStackedPanel stackPanel;
	private Link createDataCollectionLink;
	private FlexiFiltersTab tabAll;
	private FlexiFiltersTab tabPreperation;
	private FlexiFiltersTab tabReady;
	private FlexiFiltersTab tabRunning;
	private FlexiFiltersTab tabFinished;
	private DataCollectionDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private DataCollectionController dataCollectionCtrl;
	private ReferencableEntriesSearchController formSearchCtrl;
	private DataCollectionDeleteConfirmationController deleteConfirmationCtrl;
	private ToDoTasksCompactController toDosCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	
	private final MainSecurityCallback secCallback;
	private final QualityDataCollectionViewSearchParams defaultSearchParams;
	private final DataCollectionDataSource dataSource;
	
	@Autowired
	private QualityModule qualityModule;
	@Autowired
	private QualityService qualityService;
	@Autowired
	private BaseSecurityModule securityModule;

	public DataCollectionListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			MainSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.secCallback = secCallback;
		
		defaultSearchParams = new QualityDataCollectionViewSearchParams();
		defaultSearchParams.setOrgansationRefs(secCallback.getViewDataCollectionOrganisationRefs());
		defaultSearchParams.setReportAccessIdentity(getIdentity());
		defaultSearchParams.setLearnResourceManagerOrganisationRefs(secCallback.getLearnResourceManagerOrganisationRefs());
		defaultSearchParams.setIgnoreReportAccessRelationRole(!securityModule.isRelationRoleEnabled());
		defaultSearchParams.setCountToDoTasks(qualityModule.isToDoEnabled());
		dataSource = new DataCollectionDataSource(getTranslator(), defaultSearchParams, this);
		dataSource.setSearchParams(new QualityDataCollectionViewSearchParams());
		
		initForm(ureq);
		initFilters();
		initFilterTabs(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DataCollectionCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.status, new DataCollectionStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.title, CMD_EDIT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.deadline));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.topicType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.topic));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.formName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.qualitativeFeedback, new YesNoCellRenderer()));
		DefaultFlexiColumnModel numParticipantsColumn = new DefaultFlexiColumnModel(DataCollectionCols.numberParticipants);
		numParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		numParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(numParticipantsColumn);
		if (qualityModule.isToDoEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DataCollectionCols.toDos));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DataCollectionCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DataCollectionCols.generatorTitle));
		
		dataModel = new DataCollectionDataModel(dataSource, columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "dataCollections", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_qual_dc_list");
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-data-collection");
		tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", FlexiTableElement.TABLE_EMPTY_ICON);
		tableEl.setSearchEnabled(true);
	}
	
	protected void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		List<RepositoryEntry> formEntries = qualityService.getFormEntries(defaultSearchParams);
		SelectionValues formValues = new SelectionValues();
		formEntries.forEach(formEntry -> formValues.add(entry(formEntry.getKey().toString(), formEntry.getDisplayname())));
		formValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.form"), DataCollectionFilter.form.name(), formValues, true));
		
		List<QualityGenerator> generators = qualityService.getGenerators(defaultSearchParams);
		if (!generators.isEmpty()) {
			SelectionValues generatorsValues = new SelectionValues();
			generators.forEach(generator -> generatorsValues.add(entry(generator.getKey().toString(), generator.getTitle())));
			generatorsValues.sort(SelectionValues.VALUE_ASC);
			filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.generator.title"), DataCollectionFilter.generator.name(), generatorsValues, true));
		}
		
		filters.add(new FlexiTableTextFilter(translate("data.collection.title"), DataCollectionFilter.title.name(), true));
		
		SelectionValues topicTypeValues = new SelectionValues();
		Arrays.stream(QualityDataCollectionTopicType.values())
				.forEach(type -> topicTypeValues.add(entry(type.name(), translate(type.getI18nKey()))));
		topicTypeValues.sort(SelectionValues.VALUE_ASC);
		filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.topic.type"), DataCollectionFilter.topicType.name(), topicTypeValues, false));
		
		filters.add(new FlexiTableTextFilter(translate("data.collection.topic"), DataCollectionFilter.topic.name(), false));
		
		filters.add(new FlexiTableTextFilter(translate("data.collection.id"), DataCollectionFilter.key.name(), false));
		
		filters.add(new FlexiTableDateRangeFilter(translate("data.collection.start"),
				DataCollectionFilter.start.name(), false, false, translate("data.collection.start.after"),
				translate("data.collection.start.before"), getLocale()));
		
		filters.add(new FlexiTableDateRangeFilter(translate("data.collection.deadline"),
				DataCollectionFilter.deadline.name(), false, false, translate("data.collection.deadline.after"),
				translate("data.collection.deadline.before"), getLocale()));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(entry(QualityDataCollectionStatus.PREPARATION.name(), translate("data.collection.status.preparation")));
		statusValues.add(entry(QualityDataCollectionStatus.READY.name(), translate("data.collection.status.ready")));
		statusValues.add(entry(QualityDataCollectionStatus.RUNNING.name(), translate("data.collection.status.running")));
		statusValues.add(entry(QualityDataCollectionStatus.FINISHED.name(), translate("data.collection.status.finished")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("data.collection.status"), DataCollectionFilter.status.name(), statusValues, true));
		
		if (qualityModule.isToDoEnabled()) {
			SelectionValues myValues = new SelectionValues();
			myValues.add(SelectionValues.entry(FILTER_KEY_TODO, translate("filter.todo.has")));
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.todo"), DataCollectionFilter.toDos.name(), myValues, false));
		}
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	protected void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(5);
		
		tabAll = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL,
				translate("all"),
				TabSelectionBehavior.reloadData,
				List.of());
		tabs.add(tabAll);
		
		tabPreperation = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_PREPERATION,
				translate("data.collection.status.preparation"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, QualityDataCollectionStatus.PREPARATION.name())));
		tabs.add(tabPreperation);
		
		tabReady = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_READY,
				translate("data.collection.status.ready"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, QualityDataCollectionStatus.READY.name())));
		tabs.add(tabReady);
		
		tabRunning = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_RUNNING,
				translate("data.collection.status.running"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, QualityDataCollectionStatus.RUNNING.name())));
		tabs.add(tabRunning);
		
		tabFinished = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_FINISHED,
				translate("data.collection.status.finished"),
				TabSelectionBehavior.reloadData,
				List.of(FlexiTableFilterValue.valueOf(ProjNoteFilter.status, QualityDataCollectionStatus.FINISHED.name())));
		tabs.add(tabFinished);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabAll);
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		if (tab == null) return;
		
		tableEl.setSelectedFilterTab(ureq, tab);
		reload();
	}

	@Override
	public void forgeTopicItem(DataCollectionRow row) {
		if (row.getTopicRepositoryKey() != null ) {
			FormLink link = uifactory.addFormLink("topic_" + row.getKey(), CMD_TOPIC, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(StringHelper.escapeHtml(row.getTopic()));
			String businessPath = "[RepositoryEntry:" + row.getTopicRepositoryKey() + "]";
			row.setTopicBusinessPath(businessPath);
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
			link.setUrl(url);
			link.setUserObject(row);
			row.setTopicItem(link);
		} else if (row.getTopicCurriculumElementKey() != null ) {
			FormLink link = uifactory.addFormLink("topic_" + row.getKey(), CMD_TOPIC, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(StringHelper.escapeHtml(row.getTopic()));
			String businessPath = "[CurriculumAdmin:0][Curriculum:" + row.getTopicCurriculumElementCurriculumKey() + "][CurriculumElement:" + row.getTopicCurriculumElementKey() + "]";
			row.setTopicBusinessPath(businessPath);
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
			link.setUrl(url);
			link.setUserObject(row);
			row.setTopicItem(link);
		} else {
			StaticTextElement topicItem = uifactory.addStaticTextElement("topic_" + row.getKey(), null, StringHelper.escapeHtml(row.getTopic()), null);
			row.setTopicItem(topicItem);
		}
	}

	@Override
	public void forgeToDosLink(DataCollectionRow row) {
		if(row.getNumToDoTasksTotal() > 0) {
			String text = translate("data.collection.todos.num", String.valueOf(row.getNumToDoTasksDone()), String.valueOf(row.getNumToDoTasksTotal()));
			
			FormLink link = uifactory.addFormLink("todos_" + row.getKey(), CMD_TODOS, "", null, null, Link.NONTRANSLATED);
			link.setI18nKey(text);
			link.setUserObject(row);
			row.setToDosItem(link);
		}
	}

	@Override
	public void initTools() {
		if (secCallback.canCreateDataCollections()) {
			createDataCollectionLink = LinkFactory.createToolLink("data.collection.create", translate("data.collection.create"), this);
			createDataCollectionLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qual_dc_create");
			stackPanel.addTool(createDataCollectionLink, Align.left);
		}
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		FlexiFiltersTab tab = tableEl.getFilterTabById(type);
		if (tab != null) {
			selectFilterTab(ureq, tab);
		} else {
			selectFilterTab(ureq, tabAll);
			if (QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(type)) {
				Long key = entry.getOLATResourceable().getResourceableId();
				DataCollectionRow row = dataModel.getObjectByKey(key);
				if (row == null) {
					dataModel.clear();
					dataModel.load(null, null, 0, -1);
					row = dataModel.getObjectByKey(key);
					if (row != null) {
						int index = dataModel.getObjects().indexOf(row);
						if (index >= 1 && tableEl.getPageSize() > 1) {
							int page = index / tableEl.getPageSize();
							tableEl.setPage(page);
						}
						doOpenDataCollection(ureq, type, row.getDataCollection());
						List<ContextEntry> subEntries = entries.subList(1, entries.size());
						dataCollectionCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
					} else {
						tableEl.reset();
						showInfo("data.collection.forbidden");
					}
				} else {
					doOpenDataCollection(ureq, type, row.getDataCollection());
					List<ContextEntry> subEntries = entries.subList(1, entries.size());
					dataCollectionCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
				}
			}
		}
	}
	
	private void doOpenDataCollection(UserRequest ureq, String type, QualityDataCollectionView dataCollectionView) {
		if (QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(type)) {
			doEditDataCollection(ureq, dataCollectionView);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				DataCollectionRow row = dataModel.getObject(se.getIndex());
				if (CMD_EDIT.equals(cmd)) {
					doEditDataCollection(ureq, row.getDataCollection());
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				reload();
			} else if (event instanceof FlexiTableSearchEvent) {
				reload();
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_TOPIC.equals(link.getCmd()) && link.getUserObject() instanceof DataCollectionRow row) {
				doOpenTopic(ureq, row);
			} else if (CMD_TODOS.equals(link.getCmd()) && link.getUserObject() instanceof DataCollectionRow row) {
				doOpenToDoTasks(ureq, link, row.getKey());
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(createDataCollectionLink == source) {
			doSelectEvaluationForm(ureq);
		} else if (stackPanel == source && event instanceof PopEvent && stackPanel.getLastController() == this) {
			tableEl.reset(false, false, true);
		} 
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == dataCollectionCtrl && event instanceof DataCollectionEvent) {
			DataCollectionEvent dccEvent = (DataCollectionEvent) event;
			Action action = dccEvent.getAction();
			if (Action.DELETE.equals(action)) {
				QualityDataCollection dataCollectionToDelete = dccEvent.getDataCollection();
				doConfirmDeleteDataCollection(ureq, dataCollectionToDelete);
			}
		} else if (source == formSearchCtrl) {
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry formEntry = formSearchCtrl.getSelectedEntry();
				doCreateDataCollection(ureq, formEntry);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == deleteConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				QualityDataCollectionLight dataCollection = deleteConfirmationCtrl.getDataCollection();
				doDeleteDataCollection(dataCollection);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == toDosCtrl) {
			if (event instanceof ToDoTaskSelectionEvent selEvent) {
				calloutCtrl.deactivate();
				cleanUp();
				doOpenToDoTask(ureq, selEvent.getToDoTask());
			}
		} else if (source == cmc) {
			cleanUp();
		} else if (toDosCtrl == source) {
			if (event == Event.DONE_EVENT) {
				if (calloutCtrl != null) {
					calloutCtrl.deactivate();
					cleanUp();
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(toDosCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		calloutCtrl = null;
		formSearchCtrl = null;
		toDosCtrl = null;
		cmc = null;
	}

	private void doSelectEvaluationForm(UserRequest ureq) {
		formSearchCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq,
				EvaluationFormResource.TYPE_NAME, translate("data.collection.form.select"));
		this.listenTo(formSearchCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				formSearchCtrl.getInitialComponent(), true, translate("data.collection.form.select"));
		cmc.activate();
	}
	
	private void doCreateDataCollection(UserRequest ureq, RepositoryEntry formEntry) {
		List<Organisation> organisations = qualityService.getDefaultOrganisations(getIdentity());
		QualityDataCollection dataCollection = qualityService.createDataCollection(organisations, formEntry);
		doEditDataCollection(ureq, dataCollection);
	}
	
	private void doEditDataCollection(UserRequest ureq, QualityDataCollectionLight dataCollection) {
		doEditDataCollection(ureq, dataCollection, null);
	}
	
	private void doEditDataCollection(UserRequest ureq, QualityDataCollectionLight dataCollection, List<ContextEntry> ces) {
		WindowControl bwControl = addToHistory(ureq, dataCollection, null);
		dataCollectionCtrl = new DataCollectionController(ureq, bwControl, stackPanel, dataCollection);
		listenTo(dataCollectionCtrl);
		String title = dataCollection.getTitle();
		String formattedTitle = StringHelper.containsNonWhitespace(title)
				? Formatter.truncate(title, 50)
				: translate("data.collection.title.empty");
		stackPanel.pushController(formattedTitle, dataCollectionCtrl);
		dataCollectionCtrl.activate(ureq, ces, null);
	}

	private void doConfirmDeleteDataCollection(UserRequest ureq, QualityDataCollectionLight dataCollection) {
		deleteConfirmationCtrl = new DataCollectionDeleteConfirmationController(ureq, getWindowControl(), dataCollection);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("data.collection.delete.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doDeleteDataCollection(QualityDataCollectionLight dataCollection) {
		qualityService.deleteDataCollection(dataCollection);
		tableEl.reset(true, false, true);
		stackPanel.popUpToController(this);
	}

	private void reload() {
		QualityDataCollectionViewSearchParams params = new QualityDataCollectionViewSearchParams();
		doApplyFilter(params);
		
		dataSource.setSearchParams(params);
		tableEl.reset(true, true, true);
	}
	
	private void doApplyFilter(QualityDataCollectionViewSearchParams params) {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		if (filters == null || filters.isEmpty()) return;
		
		for (FlexiTableFilter filter : filters) {
			if (DataCollectionFilter.form.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<RepositoryEntryRefImpl> formEntryRefs = values.stream()
							.map(Long::valueOf)
							.map(RepositoryEntryRefImpl::new)
							.collect(Collectors.toList());
					params.setFormEntryRefs(formEntryRefs);
				} else {
					params.setFormEntryRefs(null);
				}
			}
			if (DataCollectionFilter.title.name() == filter.getFilter()) {
				if (StringHelper.containsNonWhitespace(filter.getValue())) {
					params.setTitle(filter.getValue());
				} else {
					params.setTitle(null);
				}
			}
			if (DataCollectionFilter.topicType.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<QualityDataCollectionTopicType> topicTypes = values.stream()
							.map(QualityDataCollectionTopicType::valueOf)
							.collect(Collectors.toList());
					params.setTopicTypes(topicTypes);
				} else {
					params.setTopicTypes(null);
				}
			}
			if (DataCollectionFilter.topic.name() == filter.getFilter()) {
				if (StringHelper.containsNonWhitespace(filter.getValue())) {
					params.setTopic(filter.getValue());
				} else {
					params.setTopic(null);
				}
			}
			if (DataCollectionFilter.key.name() == filter.getFilter()) {
				if (StringHelper.containsNonWhitespace(filter.getValue()) && StringHelper.isLong(filter.getValue())) {
					Long key = Long.valueOf(filter.getValue());
					params.setDataCollectionRef(() -> key);
				} else {
					params.setDataCollectionRef(null);
				}
			}
			if (DataCollectionFilter.generator.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<? extends QualityGeneratorRef> generatorRefs = values.stream()
							.map(QualityGeneratorRef::of)
							.collect(Collectors.toList());
					params.setGeneratorRefs(generatorRefs);
				} else {
					params.setGeneratorRefs(null);
				}
			}
			if (DataCollectionFilter.status.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty()) {
					List<QualityDataCollectionStatus> status = values.stream()
							.map(QualityDataCollectionStatus::valueOf)
							.collect(Collectors.toList());
					params.setStatus(status);
				} else {
					params.setStatus(null);
				}
			}
			if (DataCollectionFilter.start.name() == filter.getFilter()) {
				DateRange dateRange = ((FlexiTableDateRangeFilter)filter).getDateRange();
				Date from = dateRange != null? dateRange.getStart(): null;
				Date to = dateRange != null? dateRange.getEnd(): null;
				params.setStartAfter(from);
				params.setStartBefore(to);
			}
			if (DataCollectionFilter.deadline.name() == filter.getFilter()) {
				DateRange dateRange = ((FlexiTableDateRangeFilter)filter).getDateRange();
				Date from = dateRange != null? dateRange.getStart(): null;
				Date to = dateRange != null? dateRange.getEnd(): null;
				params.setDeadlineAfter(from);
				params.setDeadlineBefore(to);
			}
			if (DataCollectionFilter.toDos.name() == filter.getFilter()) {
				List<String> values = ((FlexiTableMultiSelectionFilter)filter).getValues();
				if (values != null && !values.isEmpty() && values.contains(FILTER_KEY_TODO)) {
					params.setToDoTasks(true);
				} else {
					params.setToDoTasks(false);
				}
			}
		}
	}
	
	private void doOpenToDoTask(UserRequest ureq, ToDoTask toDoTask) {
		QualityDataCollection dataCollection = qualityService.loadDataCollectionByKey(() -> toDoTask.getOriginId());
		if (dataCollection == null) {
			return;
		}
		
		List<ContextEntry> ces = List.of(
				BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableType(DataCollectionController.ORES_TODOS_TYPE)),
				BusinessControlFactory.getInstance().createContextEntry(OresHelper.createOLATResourceableInstance(ToDoTaskListController.TYPE_TODO, toDoTask.getKey())));
		doEditDataCollection(ureq, dataCollection, ces);
	}
	
	private void doOpenTopic(UserRequest ureq, DataCollectionRow row) {
		if (row.getTopicBusinessPath() != null) {
			NewControllerFactory.getInstance().launch(row.getTopicBusinessPath(), ureq, getWindowControl());
		}
	}
	
	private void doOpenToDoTasks(UserRequest ureq, FormLink link, Long dataCollectionKey) {
		removeAsListenerAndDispose(toDosCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		
		ToDoTaskSearchParams toDoTaskSeachParams = new ToDoTaskSearchParams();
		toDoTaskSeachParams.setStatus(ToDoStatus.OPEN_TO_DONE);
		toDoTaskSeachParams.setTypes(List.of(DataCollectionToDoTaskProvider.TYPE, EvaluationFormSessionToDoTaskProvider.TYPE));
		toDoTaskSeachParams.setOriginIds(List.of(dataCollectionKey));
		toDosCtrl = new ToDoTasksCompactController(ureq, getWindowControl(), toDoTaskSeachParams);
		listenTo(toDosCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toDosCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

}
