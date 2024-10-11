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
package org.olat.modules.lecture.ui.addwizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.lecture.ui.addwizard.CurriculumElementsDataModel.ElementCols;
import org.olat.modules.lecture.ui.addwizard.RepositoryEntriesDataModel.EntriesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectCurriculumElementsAndResourcesController extends StepFormBasicController implements FlexiTableComponentDelegate {
	
	private FlexiTableElement curriculumElementTableEl;
	private CurriculumElementsDataModel curriculumElementTableModel;

	private FlexiTableElement entriesTableEl;
	private RepositoryEntriesDataModel entriesTableModel;
	
	private final AddLectureContext addLecture;
	private final MapperKey mapperThumbnailKey;
	
	@Autowired
	private MapperService mapperService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	public SelectCurriculumElementsAndResourcesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			AddLectureContext addLecture, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "elements");
		setTranslator(Util.createPackageTranslator(SelectCurriculumElementsAndResourcesController.class, getLocale(),
				Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator())));
		this.addLecture = addLecture;

		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		initForm(ureq);
		loadCurriculumModel();
		loadEntriesModel();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initCurriculumElementsForm(formLayout);
		initRepositoryEntriesForm(formLayout);
	}
	
	private void initRepositoryEntriesForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, EntriesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EntriesCols.externalRef, "select"));
		
		entriesTableModel = new RepositoryEntriesDataModel(columnsModel);
		entriesTableEl = uifactory.addTableElement(getWindowControl(), "entriesTable", entriesTableModel, 25, false, getTranslator(), formLayout);
		entriesTableEl.setCustomizeColumns(false);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setSelection(true, false, false);
		entriesTableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		entriesTableEl.setRendererType(FlexiTableRendererType.custom);
		entriesTableEl.setNumOfRowsEnabled(false);
		entriesTableEl.setCssDelegate(new EntriesDelegate());
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/entry_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		entriesTableEl.setRowRenderer(row, this);
	}

	private void loadEntriesModel() {
		List<RepositoryEntryRow> rows = new ArrayList<>();
		if(addLecture.getPotentielEntries() != null && !addLecture.getPotentielEntries().isEmpty()) {
			List<RepositoryEntry> entries = addLecture.getPotentielEntries();
			for(RepositoryEntry entry:entries) {
				rows.add(forgeRow(entry));
			}
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		entriesTableEl.setVisible(!rows.isEmpty());

		if(addLecture.getEntry() != null) {
			Set<Integer> index = new HashSet<>();
			List<RepositoryEntryRow> objects = entriesTableModel.getObjects();
			for(int i=objects.size(); i-->0; ) {
				RepositoryEntryRow object = objects.get(i);
				if(object.getKey().equals(addLecture.getEntry().getKey())) {
					index.add(Integer.valueOf(i));
				}
			}
			entriesTableEl.setMultiSelectedIndex(index);
		}
	}
	
	private RepositoryEntryRow forgeRow(RepositoryEntry entry) {
		RepositoryEntryRow row = new RepositoryEntryRow(entry);
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		if(image != null) {
			row.setThumbnailUrl(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image));
		}
		return row;
	}
	
	private void initCurriculumElementsForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer(false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.identifier, "select"));
		
		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementCols.references));
		
		curriculumElementTableModel = new CurriculumElementsDataModel(columnsModel);
		curriculumElementTableEl = uifactory.addTableElement(getWindowControl(), "elementsTable", curriculumElementTableModel, 25, false, getTranslator(), formLayout);
		curriculumElementTableEl.setCustomizeColumns(false);
		curriculumElementTableEl.setNumOfRowsEnabled(false);
		curriculumElementTableEl.setSelection(true, false, false);
	}
	
	private void loadCurriculumModel() {
		List<CurriculumElementRow> rows = new ArrayList<>();
		if(addLecture.getRootElement() != null) {
			List<CurriculumElementInfos> elements = curriculumService
					.getCurriculumElementsDescendantsWithInfos(addLecture.getRootElement());
			Map<Long,CurriculumElementRow> keyToRows = new HashMap<>();
			for(CurriculumElementInfos element:elements) {
				CurriculumElementRow row = new CurriculumElementRow(element.getCurriculumElement(),
						element.getNumOfResources());
				rows.add(row);
				keyToRows.put(row.getKey(), row);
			}
			//parent line
			for(CurriculumElementRow row:rows) {
				if(row.getParentKey() != null) {
					row.setParent(keyToRows.get(row.getParentKey()));
				}
			}
			
			if(rows.size() > 1) {
				Collections.sort(rows, new CurriculumElementTreeRowComparator(getLocale()));
			}
		}
		curriculumElementTableModel.setObjects(rows);
		curriculumElementTableEl.reset(true, true, true);
		curriculumElementTableEl.setVisible(!rows.isEmpty());
		
		if(addLecture.getCurriculumElement() != null) {
			Set<Integer> index = new HashSet<>();
			List<CurriculumElementRow> objects = curriculumElementTableModel.getObjects();
			for(int i=objects.size(); i-->0; ) {
				CurriculumElementRow object = objects.get(i);
				if(object.getKey().equals(addLecture.getCurriculumElement().getKey())) {
					index.add(Integer.valueOf(i));
				}
			}
			curriculumElementTableEl.setMultiSelectedIndex(index);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedCurriculumElementIndex = curriculumElementTableEl.isVisible()
				? curriculumElementTableEl.getMultiSelectedIndex() : Set.of();
		if(selectedCurriculumElementIndex.size() == 1) {
			int index = selectedCurriculumElementIndex.iterator().next().intValue();
			CurriculumElementRow selectedRow = curriculumElementTableModel.getObject(index);
			addLecture.setCurriculumElement(selectedRow.getCurriculumElement());
		} else {
			addLecture.setCurriculumElement(null);
		}
		
		Set<Integer> selectedEntriesIndex = entriesTableEl.isVisible()
				? entriesTableEl.getMultiSelectedIndex() : Set.of();
		if(selectedEntriesIndex.size() == 1) {
			int index = selectedEntriesIndex.iterator().next().intValue();
			RepositoryEntryRow selectedRow = entriesTableModel.getObject(index);
			addLecture.setEntry(selectedRow.getEntry());
		} else {
			addLecture.setEntry(null);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private static class EntriesDelegate implements FlexiTableCssDelegate {

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards o_cards_4";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return null;
		}
	}
}
