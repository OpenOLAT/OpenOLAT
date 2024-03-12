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
package org.olat.core.commons.services.export.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.export.ui.ExportsListDataModel.ExportsCols;
import org.olat.core.commons.services.vfs.ui.management.VFSSizeCellRenderer;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExportsAdminListController extends FormBasicController {
	
	private static final String ALL_TAB_ID = "All";
	private static final String COMPLETE_ARCHIVES_TAB_ID = "Complete";
	private static final String PARTIAL_ARCHIVES_TAB_ID = "Partial";
	private static final String ONGOING_TAB_ID = "OnGoing";
	private static final String ONLY_ADMINISTRATORS_TAB_ID = "OnlyAdministrators";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab completeArchiveTab;
	private FlexiFiltersTab partialArchiveTab;
	private FlexiFiltersTab ongoingTab;
	private FlexiFiltersTab onlyAdministratorsTab;
	
	private FlexiTableElement tableEl;
	private ExportsListDataModel tableModel;
	
	private int counter = 0;
	private final String subIdent;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private ExportInfosController detailsCtrl;
	private ConfirmDeleteExportController confirmDeleteCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ExportManager exportManager;
	
	public ExportsAdminListController(UserRequest ureq, WindowControl wControl, String subIdent) {
		super(ureq, wControl, "export_admin_list", Util.createPackageTranslator(ExportsAdminListController.class, ureq.getLocale()));
		this.subIdent = subIdent;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.entry));
		
		StaticFlexiCellRenderer entryRenderer = new StaticFlexiCellRenderer("entry", new TextFlexiCellRenderer());
		entryRenderer.setIconLeftCSS("o_icon o_icon-fw o_CourseModule_icon");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.entry.i18nHeaderKey(), ExportsCols.entry.ordinal(), "entry",
				entryRenderer));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.entryExternalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.archiveType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ExportsCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.access,
				new ExportAccessRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.archiveSize,
				new VFSSizeCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.status,
				new ExportStatusRenderer(getTranslator())));
		
		StickyActionColumnModel toolsColumn = new StickyActionColumnModel(ExportsCols.tools);
		toolsColumn.setExportable(false);
		toolsColumn.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsColumn);
		
		tableModel = new ExportsListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_export_list");
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "exports-admin-v1");
	}
	
	protected void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		allTab.setElementCssClass("o_sel_export_all");
		tabs.add(allTab);
		
		completeArchiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters(COMPLETE_ARCHIVES_TAB_ID, translate("filter.complete.archive"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(completeArchiveTab);

		partialArchiveTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PARTIAL_ARCHIVES_TAB_ID, translate("filter.partial.archive"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(partialArchiveTab);
		
		ongoingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ONGOING_TAB_ID, translate("filter.ongoing.archive"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(ongoingTab);
		
		onlyAdministratorsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ONLY_ADMINISTRATORS_TAB_ID, translate("filter.only.administrators.archive"),
				TabSelectionBehavior.reloadData, List.of());
		tabs.add(onlyAdministratorsTab);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	public void loadModel() {
		SearchExportMetadataParameters params = getSearchParams();
		List<ExportMetadata> exports = exportManager.searchMetadata(params);
		List<ExportRow> rows = new ArrayList<>(exports.size());
		for(ExportMetadata export:exports) {
			rows.add(forge(export));
		}
		
		Collections.sort(rows, new ExportRowComparator());

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ExportRow forge(ExportMetadata export) {
		String creatorFullName = userManager.getUserDisplayName(export.getCreator());
		String type = getTranslatedType(export);
		ExportRow row = new ExportRow(export, type, creatorFullName);
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
		row.setToolsButton(toolsLink);
		toolsLink.setUserObject(row);
		
		return row;
	}
	
	private String getTranslatedType(ExportMetadata metadata) {
		if(metadata == null || metadata.getArchiveType() == null) {
			return null;
		}
		ArchiveType type = metadata.getArchiveType();
		return switch(type) {
			case COMPLETE -> translate("archive.complete");
			case PARTIAL -> translate("archive.partial");
			default -> "-";
		};
	}
	
	protected SearchExportMetadataParameters getSearchParams() {
		SearchExportMetadataParameters params = new SearchExportMetadataParameters(List.of(ArchiveType.COMPLETE, ArchiveType.PARTIAL));
		params.setResSubPath(subIdent);
		
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		if(selectedTab == completeArchiveTab) {
			params.setArchiveTypes(List.of(ArchiveType.COMPLETE));
		} else if(selectedTab == partialArchiveTab) {
			params.setArchiveTypes(List.of(ArchiveType.PARTIAL));
		} else if(selectedTab == ongoingTab) {
			params.setOngoingExport(true);
		} else if(selectedTab == onlyAdministratorsTab) {
			params.setOnlyAdministrators3(Boolean.TRUE);
		}

		return params;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				toolsCalloutCtrl.deactivate();
				cleanUp();
			}
		} else if(confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(detailsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(toolsCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		toolsCalloutCtrl = null;
		detailsCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof FlexiTableFilterTabEvent fte && FlexiTableFilterTabEvent.SELECT_FILTER_TAB.equals(fte.getCommand())) {
				loadModel();
			} else if(event instanceof SelectionEvent se && "entry".equals(se.getCommand())) {
				ExportRow row = tableModel.getObject(se.getIndex());
				if(row == null) {
					loadModel();
				} else {
					doOpenRepositoryEntry(ureq, row);
				}
			}
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd) && link.getUserObject() instanceof ExportRow exportRow) {
				doOpenTools(ureq, link, exportRow);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, ExportRow exportRow) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		ExportMetadata metadata = exportManager.getExportMetadataByKey(exportRow.getMetadataKey());
		if(metadata == null) {
			tableEl.reloadData();
			showWarning("warning.export.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), exportRow);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private void doConfirmDelete(UserRequest ureq, ExportRow row) {
		confirmDeleteCtrl = new ConfirmDeleteExportController(ureq, getWindowControl(), row);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("confirm.delete.title", StringHelper.escapeHtml(row.getTitle()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDownload(UserRequest ureq, ExportRow row) {
		VFSLeaf archive = row.getArchive();
		MediaResource resource;
		if(archive == null) {
			resource = new NotFoundMediaResource();
		} else {
			resource = new VFSMediaResource(archive);
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpenRepositoryEntry(UserRequest ureq, ExportRow row) {
		String businessPath = "[RepositoryEntry:" + row.getRepositoryEntryKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		
	}
	
	protected void doOpenMetadata(UserRequest ureq, ExportRow row) {
		detailsCtrl = new ExportInfosController(ureq, getWindowControl(), row);
		listenTo(detailsCtrl);
		
		String title = translate("details.title", StringHelper.escapeHtml(row.getTitle()));
		cmc = new CloseableModalController(getWindowControl(), translate("close"), detailsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private class ToolsController extends BasicController {
		
		private final Link metadataLink;
		private final Link downloadLink;
		private final Link deleteLink;
		
		private final ExportRow exportRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ExportRow exportRow) {
			super(ureq, wControl);
			this.exportRow = exportRow;

			VelocityContainer mainVC = createVelocityContainer("tools");
			
			metadataLink = LinkFactory.createLink("show.metadata", "show.metadata", "metadata", mainVC, this);
			metadataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_description");
			downloadLink = LinkFactory.createLink("download", "download", "download", mainVC, this);
			downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
			deleteLink = LinkFactory.createLink("delete", "delete", "delete", mainVC, this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(metadataLink == source) {
				doOpenMetadata(ureq, exportRow);
			} else if(downloadLink == source) {
				doDownload(ureq, exportRow);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, exportRow);
			}
		}
	}
}
