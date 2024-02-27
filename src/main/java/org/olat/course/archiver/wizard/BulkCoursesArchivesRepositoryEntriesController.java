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
package org.olat.course.archiver.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportManager;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.export.model.ExportInfos;
import org.olat.core.commons.services.export.model.SearchExportMetadataParameters;
import org.olat.core.commons.services.taskexecutor.TaskStatus;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.course.archiver.wizard.BulkCoursesArchivesRepositoryEntriesTableModel.ArchivesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkCoursesArchivesRepositoryEntriesController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private BulkCoursesArchivesRepositoryEntriesTableModel tableModel;
	
	private int count = 0;
	private final SelectionValues archiveTypePK;
	private final BulkCoursesArchivesContext archiveContext;
	
	@Autowired
	private ExportManager exportManager;
	
	public BulkCoursesArchivesRepositoryEntriesController(UserRequest ureq, WindowControl wControl,
			BulkCoursesArchivesContext archiveContext, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "archives_entries");
		this.archiveContext = archiveContext;
		
		archiveTypePK = new SelectionValues();
		archiveTypePK.add(SelectionValues.entry(ArchiveType.COMPLETE.name(), translate("bulk.archive.types.complete")));
		archiveTypePK.add(SelectionValues.entry(ArchiveType.NONE.name(), translate("bulk.archive.types.none")));
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.status,
				new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.numOfArchives));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.statusArchives,
				new ArchiveRepositoryEntryStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ArchivesCols.typeArchive));
		
		tableModel = new BulkCoursesArchivesRepositoryEntriesTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "list", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_export_list");
	}
	
	private void loadModel() {
		List<RepositoryEntry> entries = archiveContext.getEntries();
		List<ArchiveRepositoryEntryRow> rows = new ArrayList<>(entries.size());
		for(RepositoryEntry entry:entries) {
			rows.add(forgeRow(entry));
		}
		
		addStatisticsAndInfos(entries, rows);
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ArchiveRepositoryEntryRow forgeRow(RepositoryEntry entry) {
		SingleSelection typeEl = uifactory.addDropdownSingleselect("archive.type." + (++count), null, flc,
				archiveTypePK.keys(), archiveTypePK.values());
		typeEl.select(ArchiveType.COMPLETE.name(), true);
		return new ArchiveRepositoryEntryRow(entry, typeEl);
	}
	
	private void addStatisticsAndInfos(List<RepositoryEntry> entries, List<ArchiveRepositoryEntryRow> rows) {
		Map<Long,ArchiveRepositoryEntryRow> repoEntryKeyToRow = rows.stream()
				.collect(Collectors.toMap(ArchiveRepositoryEntryRow::getKey, r -> r, (u, v) -> u));
		
		SearchExportMetadataParameters params = new SearchExportMetadataParameters();
		params.setRepositoryEntries(new ArrayList<>(entries));
		List<ExportInfos> infosList = exportManager.getResultsExport(params);
		
		for(ExportInfos infos:infosList ) {
			ExportMetadata metadata = infos.getExportMetadata();
			if(metadata == null || metadata.getEntry() == null) {
				continue;
			}
			
			RepositoryEntry entry = metadata.getEntry();
			ArchiveRepositoryEntryRow row = repoEntryKeyToRow.get(entry.getKey());
			if(row != null) {
				row.incrementNumOfArchives();

				if(metadata.getTask() != null
						&& (metadata.getTask().getStatus() == TaskStatus.newTask
							|| metadata.getTask().getStatus() == TaskStatus.inWork)) {
					row.setRunningArchive(true);
					row.getArchiveTypeEl().select(ArchiveType.NONE.name(), true);
					row.getArchiveTypeEl().setEnabled(false);
				}
				
				if(metadata.getArchiveType() == ArchiveType.COMPLETE) {
					row.setCompleteArchive(true);
				}
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean oneSelected = false;
		List<ArchiveRepositoryEntryRow> rows = tableModel.getObjects();
		for(ArchiveRepositoryEntryRow row:rows) {
			SingleSelection typeEl = row.getArchiveTypeEl();
			
			typeEl.clearError();
			if(typeEl.isEnabled()) {
				if(!typeEl.isOneSelected()) {
					typeEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} else if(ArchiveType.COMPLETE.name().equals(typeEl.getSelectedKey())) {
					oneSelected |= true;
				}
			}
		}
		if(allOk && !oneSelected) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<ArchiveRepositoryEntryRow> rows = tableModel.getObjects();
		for(ArchiveRepositoryEntryRow row:rows) {
			SingleSelection typeEl = row.getArchiveTypeEl();
			ArchiveType type = ArchiveType.NONE;
			if(typeEl.isEnabled() && ArchiveType.COMPLETE.name().equals(typeEl.getSelectedKey())) {
				type = ArchiveType.COMPLETE;
			}
			archiveContext.addArchiveTypeFor(type, row.getRepositoryEntry());
		}

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
