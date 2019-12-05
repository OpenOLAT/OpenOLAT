package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.admin.sysinfo.gui.LargeFilesLockedCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesNameCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesRevisionCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesSizeCellRenderer;
import org.olat.admin.sysinfo.gui.LargeFilesTrashedCellRenderer;
import org.olat.admin.sysinfo.model.FileStatsTableContentRow;
import org.olat.admin.sysinfo.model.FileStatsTableModel;
import org.olat.admin.sysinfo.model.FileStatsTableModel.FileStatsTableColumns;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams.Filter;
import org.springframework.beans.factory.annotation.Autowired;

public class LargeFilesController extends FormBasicController implements FlexiTableCssDelegate {

	private FlexiTableElement largeFilesTableElement;
	private FileStatsTableModel largeFilesTabelModel;
	
	private MultipleSelectionElement types;
	private SingleSelection trashed;
	private SingleSelection revision; 
	private DateChooser newerThan;
	private DateChooser olderThan;
	
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	
	public LargeFilesController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "large_files");
		initForm(ureq);
		updateModel();
	}
	
	public void updateModel() {
		List<VFSMetadata> files = vfsRepositoryService.getLargestFiles(100);
		List<VFSRevision> revisions = vfsRepositoryService.getLargestRevisions(100);
		
		List<FileStatsTableContentRow> rows = new ArrayList<>();
		
		for(VFSMetadata file:files) {
			rows.add(new FileStatsTableContentRow(file));
		}
	
		for(VFSRevision revision:revisions) {
			rows.add(new FileStatsTableContentRow(revision));
		}
		
		largeFilesTabelModel.setObjects(rows);
		largeFilesTableElement.reset(true, true, true);		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel column;
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.uuid));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, FileStatsTableColumns.name, new LargeFilesNameCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, FileStatsTableColumns.size, new LargeFilesSizeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, FileStatsTableColumns.path));
		
		column = new DefaultFlexiColumnModel(false, FileStatsTableColumns.trashed, new LargeFilesTrashedCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_TRASHED));
		columnsModel.addFlexiColumnModel(column);
		
		column = new DefaultFlexiColumnModel(false, FileStatsTableColumns.revision, new LargeFilesRevisionCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_REVISION));
		columnsModel.addFlexiColumnModel(column);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.revisionNr));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.revisionComment));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.fileCategory));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.fileType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.downloadCount));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.author, "selectAuthor"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.license));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.language));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.source));
		
		column = new DefaultFlexiColumnModel(false, FileStatsTableColumns.locked, new LargeFilesLockedCellRenderer());
		column.setIconHeader(CSSHelper.getIconCssClassFor(CSSHelper.CSS_CLASS_LOCKED));
		columnsModel.addFlexiColumnModel(column);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.lockedAt));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.lockedBy, "selectLockedBy"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.publisher));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.pubDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.createdAt));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, FileStatsTableColumns.lastModifiedAt));
		
		largeFilesTabelModel = new FileStatsTableModel(columnsModel, getLocale());
		largeFilesTableElement = uifactory.addTableElement(getWindowControl(), "large_files", largeFilesTabelModel, getTranslator(), formLayout);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(FileStatsTableColumns.size.name(), false));
		sortOptions.setFromColumnModel(true);
		largeFilesTableElement.setSortSettings(sortOptions);
		largeFilesTableElement.setAndLoadPersistedPreferences(ureq, "admin-large-files-list");	
		largeFilesTableElement.setSearchEnabled(false);
		largeFilesTableElement.setCssDelegate(this);
		
//		initFilters(largeFilesTableElement);		
	}
	
	private void initFilters(FlexiTableElement tableElement) {
		List<FlexiTableFilter> filters = new ArrayList<>(16);
		filters.add(new FlexiTableFilter(translate("filter.show.all"), Filter.showAll.name(), true));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.only.courses"), Filter.onlyCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.current.courses"), Filter.currentCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.upcoming.courses"), Filter.upcomingCourses.name()));
		filters.add(new FlexiTableFilter(translate("filter.old.courses"), Filter.oldCourses.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.booked.participant"), Filter.asParticipant.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.coach"), Filter.asCoach.name()));
		filters.add(new FlexiTableFilter(translate("filter.booked.author"), Filter.asAuthor.name()));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("filter.passed"), Filter.passed.name()));
		filters.add(new FlexiTableFilter(translate("filter.not.passed"), Filter.notPassed.name()));
		filters.add(new FlexiTableFilter(translate("filter.without.passed.infos"), Filter.withoutPassedInfos.name()));
		tableElement.setFilters(null, filters, false);
	}
	
	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}
	
	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}
	
	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		return null;
//		long size = largeFilesTabelModel.getObject(pos).getSize();
//		
//		if (size < 5000) {
//			return "o_table_row_success";
//		} else if (size < 100000) {
//			return "o_table_row_warning";
//		}
//		return "o_table_row_error";
	}

	@Override
	protected void doDispose() {

	}

	@Override
	protected void formOK(UserRequest ureq) {

	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(largeFilesTableElement == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				FileStatsTableContentRow contentRow = largeFilesTabelModel.getObject(te.getIndex());
				if("select".equals(cmd)) {
					if (contentRow.getAuthor() != null) {
						openUser(ureq, contentRow.getAuthor().getKey());
					}
				} else if("vcard".equals(cmd)) {
//					doSelectVcard(ureq, userRow);
				}
			}
		}
//		} else if(mailButton == source) {
//			doMail(ureq);
//		} else if(bulkChangesButton == source) {
//			doBulkEdit(ureq);
//		} else if(bulkMovebutton == source) {
//			doBulkMove(ureq);
//		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void openUser(UserRequest ureq, Long userKey) {
		NewControllerFactory.getInstance().launch("[UserAdminSite:0][usearch:0][table:0][Identity:" + userKey.toString() + "]", ureq, getWindowControl());
	}
}
