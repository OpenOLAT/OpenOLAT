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
import java.util.List;
import java.util.Set;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.ui.LectureListRepositoryController;
import org.olat.modules.lecture.ui.addwizard.RepositoryEntriesDataModel.EntriesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignNewRepositoryEntryController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FlexiTableElement entriesTableEl;
	private RepositoryEntriesDataModel entriesTableModel;
	
	private final LectureBlock lectureBlock;
	private final MapperKey mapperThumbnailKey;
	private final RepositoryEntry currentEntry;
	private final CurriculumElement curriculumElement;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	
	public AssignNewRepositoryEntryController(UserRequest ureq, WindowControl wControl,
			LectureBlock lectureBlock, CurriculumElement curriculumElement, RepositoryEntry currentEntry) {
		super(ureq, wControl, "assign_elements", Util
				.createPackageTranslator(LectureListRepositoryController.class, ureq.getLocale()));
		this.lectureBlock = lectureBlock;
		this.currentEntry = currentEntry;
		this.curriculumElement = curriculumElement;
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		initForm(ureq);
		loadEntriesModel();
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String text;
		if(currentEntry == null) {
			text = translate("info.no.entry");
		} else {
			text = StringHelper.escapeHtml(currentEntry.getDisplayname());
			if(StringHelper.containsNonWhitespace(currentEntry.getExternalRef())) {
				text += "<small class='mute'> \u00B7 " + StringHelper.escapeHtml(currentEntry.getExternalRef()) + "</small>";
			}
		}
		uifactory.addStaticTextElement("current.entry", "current.entry", text, formLayout);

		initRepositoryEntriesForm(formLayout);

		uifactory.addFormSubmitButton("apply", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
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
		entriesTableEl.setCssDelegate(new CSSEntriesDelegate());
		entriesTableEl.setEmptyTableSettings("empty.course.list", null, "o_CourseModule_icon");
		
		VelocityContainer row = new VelocityContainer(null, "vc_row1", velocity_root + "/entry_1.html",
				getTranslator(), this);
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		entriesTableEl.setRowRenderer(row, this);
	}
	
	private void loadEntriesModel() {
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		List<RepositoryEntryRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:entries) {
			if(currentEntry == null || !currentEntry.equals(entry)) {
				rows.add(forgeRow(entry));
			}
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
		entriesTableEl.setVisible(true);
	}
	
	private RepositoryEntryRow forgeRow(RepositoryEntry entry) {
		RepositoryEntryRow row = new RepositoryEntryRow(entry);
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		if(image != null) {
			row.setThumbnailUrl(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image));
		}
		return row;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(mainForm.getFormItemContainer() == source) {
			if("ONCLICK".equals(event.getCommand()) && StringHelper.isLong(ureq.getParameter("entrychkbox"))) {
				Set<Integer> selected = Set.of(Integer.valueOf(ureq.getParameter("entrychkbox")));
				entriesTableEl.setMultiSelectedIndex(selected);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedEntriesIndex = entriesTableEl.isVisible()
				? entriesTableEl.getMultiSelectedIndex() : Set.of();
		if(selectedEntriesIndex.size() == 1) {
			int index = selectedEntriesIndex.iterator().next().intValue();
			RepositoryEntryRow selectedRow = entriesTableModel.getObject(index);
			RepositoryEntry entry = selectedRow.getEntry();
			lectureService.moveLectureBlock(lectureBlock, entry);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public static class CSSEntriesDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards o_cards_4";
		}
	}
}
