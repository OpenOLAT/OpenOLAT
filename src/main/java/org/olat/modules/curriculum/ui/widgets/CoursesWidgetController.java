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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.panel.EmptyPanelItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumListManagerController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.modules.lecture.ui.addwizard.RepositoryEntriesDataModel;
import org.olat.modules.lecture.ui.addwizard.RepositoryEntriesDataModel.EntriesCols;
import org.olat.modules.lecture.ui.addwizard.RepositoryEntryRow;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoursesWidgetController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private FormLink coursesLink;
	private FlexiTableElement entriesTableEl;
	private RepositoryEntriesDataModel entriesTableModel;

	private final MapperKey mapperThumbnailKey;
	private final List<RepositoryEntry> repositoryEntries;

	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	
	public CoursesWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl, "courses_widget", Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		repositoryEntries = curriculumService.getRepositoryEntries(curriculumElement);
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper(900, 600));
		
		initForm(ureq);

		if(!repositoryEntries.isEmpty()) {
			loadModel();
		}
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return List.of();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		coursesLink = uifactory.addFormLink("curriculum.courses", formLayout);
		coursesLink.setIconRightCSS("o_icon o_icon-fw o_icon_course_next");
		
		if(repositoryEntries.isEmpty()) {
			EmptyPanelItem emptyTeachersList = uifactory.addEmptyPanel("course.empty", null, formLayout);
			emptyTeachersList.setTitle(translate("curriculum.no.course.assigned.title"));
			emptyTeachersList.setIconCssClass("o_icon o_icon-lg o_CourseModule_icon");
		} else {
			initFormTable(formLayout);
		}
	}
	
	private void initFormTable(FormItemContainer formLayout) {
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
	
	private void loadModel() {
		AccessRenderer renderer = new AccessRenderer(getLocale());
		List<RepositoryEntryRow> rows = new ArrayList<>();
		for(RepositoryEntry entry:repositoryEntries) {
			rows.add(forgeRow(entry, renderer));
		}
		entriesTableModel.setObjects(rows);
		entriesTableEl.reset(true, true, true);
	}
	
	private RepositoryEntryRow forgeRow(RepositoryEntry entry, AccessRenderer renderer) {
		RepositoryEntryRow row = new RepositoryEntryRow(entry);
		VFSLeaf image = repositoryManager.getImage(entry.getKey(), entry.getOlatResource());
		if(image != null) {
			row.setThumbnailUrl(RepositoryEntryImageMapper.getImageUrl(mapperThumbnailKey.getUrl(), image));
		}
		
		String status = renderer.renderEntryStatus(entry);
		row.setStatusHtml(status);
		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(coursesLink == source) {
			List<ContextEntry> entries = BusinessControlFactory.getInstance()
					.createCEListFromResourceType(CurriculumListManagerController.CONTEXT_RESOURCES);
			fireEvent(ureq, new ActivateEvent(entries));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private static class EntriesDelegate implements FlexiTableCssDelegate {

		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return null;
		}

		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_cards";
		}

		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return null;
		}
	}
}
