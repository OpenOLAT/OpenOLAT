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
package org.olat.modules.curriculum.ui.copy;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.copy.CopyElementDetailsResourcesTableModel.CopyResourcesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.AccessRenderer;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 20 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementDetailsTemplatesController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CopyElementDetailsResourcesTableModel tableModel;

	private final CopyElementContext context;
	private final CurriculumElement curriculumElement;
	
	@Autowired
	private CurriculumService curriculumService;

	public CopyElementDetailsTemplatesController(UserRequest ureq, WindowControl wControl, Form rootForm,
			CurriculumElement curriculumElement, CopyElementContext context) {
		super(ureq, wControl, LAYOUT_CUSTOM, "element_details_templates", rootForm);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(),
				Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator())));
		this.context = context;
		this.curriculumElement = curriculumElement;
		
		initForm(ureq);
		loadModel();
	}
	
	public int getNumOfTemplates() {
		return tableModel.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyResourcesCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyResourcesCols.activity,
				new CopySettingCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyResourcesCols.displayname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyResourcesCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CopyResourcesCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyResourcesCols.access,
				new AccessRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CopyResourcesCols.lifecycle,
				new RepositoryEntryLifecycleCellRenderer(getLocale())));

		tableModel = new CopyElementDetailsResourcesTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "templatesTable", tableModel, 20, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(curriculumElement);
		List<CopyElementDetailsResourcesRow> rows = new ArrayList<>(templates.size());
		final CopyResources copySetting = context.getCoursesEventsCopySetting();
		final CopyResources copyTemplateSetting = copySetting == null || copySetting == CopyResources.dont
				? CopyResources.dont
				: CopyResources.relation;
		for(RepositoryEntry template:templates) {
			rows.add(new CopyElementDetailsResourcesRow(template, -1, copyTemplateSetting));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
