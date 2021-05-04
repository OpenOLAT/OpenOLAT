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
package org.olat.modules.grading.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.grading.GradingService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryACColumnDescriptor;
import org.olat.repository.ui.RepositoryFlexiTableModel;
import org.olat.repository.ui.RepositoryFlexiTableModel.RepoCols;
import org.olat.repository.ui.author.AccessRenderer;
import org.olat.repository.ui.author.TypeRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 13 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchResourceListController extends StepFormBasicController {
	
	private ImportGradersContext graders;
	
	private FlexiTableElement tableEl;
	private RepositoryFlexiTableModel tableModel;
	
	@Autowired
	private GradingService gradingService;
	
	public SearchResourceListController(UserRequest ureq, WindowControl wControl, ImportGradersContext graders, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "resource_list");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.graders = graders;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.ac, new RepositoryEntryACColumnDescriptor()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.repoEntry, new TypeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.lifecycleLabel));// visible if lifecycle
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.displayname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RepoCols.access, new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		tableModel = new RepositoryFlexiTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "resources", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "graders-resources");
		tableEl.setEmptyTableMessageKey("resources.noresources");
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel() {
		List<RepositoryEntry> entries = gradingService.getReferenceRepositoryEntriesWithGrading(getIdentity());
		tableModel.setObjects(entries);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					select(ureq, tableModel.getObject(se.getIndex()));
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				tableModel.filter(se.getSearch(), null);
				tableEl.reset(true, true, true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void select(UserRequest ureq, RepositoryEntry entry) {
		graders.setEntry(entry);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
