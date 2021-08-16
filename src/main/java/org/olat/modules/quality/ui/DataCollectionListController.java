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

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityDataCollectionLight;
import org.olat.modules.quality.QualityDataCollectionView;
import org.olat.modules.quality.QualityDataCollectionViewSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.ui.DataCollectionDataModel.DataCollectionCols;
import org.olat.modules.quality.ui.event.DataCollectionEvent;
import org.olat.modules.quality.ui.event.DataCollectionEvent.Action;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionListController extends FormBasicController implements TooledController, Activateable2 {

	private static final String CMD_EDIT = "edit";
	
	private final TooledStackedPanel stackPanel;
	private Link createDataCollectionLink;
	private DataCollectionDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private CloseableModalController cmc;
	private DataCollectionController dataCollectionCtrl;
	private DataCollectionSearchController searchCtrl;
	private ReferencableEntriesSearchController formSearchCtrl;
	private DataCollectionDeleteConfirmationController deleteConfirmationCtrl;
	
	private final MainSecurityCallback secCallback;
	private final DataCollectionDataSource dataSource;
	
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
		
		QualityDataCollectionViewSearchParams defaultSearchParams = new QualityDataCollectionViewSearchParams();
		defaultSearchParams.setOrgansationRefs(secCallback.getViewDataCollectionOrganisationRefs());
		defaultSearchParams.setReportAccessIdentity(getIdentity());
		defaultSearchParams.setLearnResourceManagerOrganisationRefs(secCallback.getLearnResourceManagerOrganisationRefs());
		defaultSearchParams.setIgnoreReportAccessRelationRole(!securityModule.isRelationRoleEnabled());
		dataSource = new DataCollectionDataSource(getTranslator(), defaultSearchParams);
		dataSource.setSearchParams(new QualityDataCollectionViewSearchParams());
		
		searchCtrl = new DataCollectionSearchController(ureq, getWindowControl(), mainForm, defaultSearchParams);
		searchCtrl.setEnabled(false);
		listenTo(searchCtrl);
		
		initForm(ureq);
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
		DefaultFlexiColumnModel numParticipantsColumn = new DefaultFlexiColumnModel(DataCollectionCols.numberParticipants);
		numParticipantsColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		numParticipantsColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(numParticipantsColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DataCollectionCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DataCollectionCols.generatorTitle));
		
		dataModel = new DataCollectionDataModel(dataSource, columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "dataCollections", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_qual_dc_list");
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-data-collection");
		tableEl.setEmptyTableSettings("table.search.empty", "table.search.empty.hint", FlexiTableElement.TABLE_EMPTY_ICON);
		tableEl.setSearchEnabled(true);
		tableEl.setExtendedSearch(searchCtrl);
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
		if (QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(type)) {
			doResetExtendedSearch(ureq);
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
	
	private void doOpenDataCollection(UserRequest ureq, String type, QualityDataCollectionView dataCollectionView) {
		if (QualityDataCollectionLight.RESOURCEABLE_TYPE_NAME.equals(type)) {
			doEditDataCollection(ureq, dataCollectionView);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl && event instanceof SelectionEvent) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			DataCollectionRow row = dataModel.getObject(se.getIndex());
			if (CMD_EDIT.equals(cmd)) {
				doEditDataCollection(ureq, row.getDataCollection());
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
		} else if (searchCtrl == source) {
			if (event instanceof SearchEvent) {
				SearchEvent se = (SearchEvent)event;
				doExtendedSearch(se);
			} else if (event == Event.CANCELLED_EVENT) {
				doResetExtendedSearch(ureq);
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
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(formSearchCtrl);
		removeAsListenerAndDispose(cmc);
		deleteConfirmationCtrl = null;
		formSearchCtrl = null;
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
		WindowControl bwControl = addToHistory(ureq, dataCollection, null);
		dataCollectionCtrl = new DataCollectionController(ureq, bwControl, stackPanel, dataCollection);
		listenTo(dataCollectionCtrl);
		String title = dataCollection.getTitle();
		String formattedTitle = StringHelper.containsNonWhitespace(title)
				? Formatter.truncate(title, 50)
				: translate("data.collection.title.empty");
		stackPanel.pushController(formattedTitle, dataCollectionCtrl);
		dataCollectionCtrl.activate(ureq, null, null);
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

	private void doExtendedSearch(SearchEvent se) {
		QualityDataCollectionViewSearchParams params = new QualityDataCollectionViewSearchParams();
		params.setTitle(se.getTitle());
		params.setTopic(se.getTopic());
		params.setStartAfter(se.getStartAfter());
		params.setStartBefore(se.getStartBefore());
		params.setDeadlineAfter(se.getDeadlineAfter());
		params.setDeadlineBefore(se.getDeadlineBefore());
		params.setDataCollectionRef(se.getDataCollectionRef());
		params.setGeneratorRefs(se.getGeneratorRefs());
		params.setFormEntryRefs(se.getFormEntryRefs());
		params.setTopicTypes(se.getTopicTypes());
		params.setStatus(se.getStatus());
		
		dataSource.setSearchParams(params);
		tableEl.reset(true, true, true);
	}
	
	private void doResetExtendedSearch(UserRequest ureq) {
		dataSource.setSearchParams(new QualityDataCollectionViewSearchParams());
		tableEl.resetSearch(ureq);
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
	}

}
