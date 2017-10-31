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
package org.olat.modules.taxonomy.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyLevelTypesTableModel.TypesCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelTypesEditController extends FormBasicController {
	
	private FormLink addRootTypeButton;
	private FlexiTableElement tableEl;
	private TaxonomyLevelTypesTableModel model;
	
	private CloseableModalController cmc;
	private EditTaxonomyLevelTypeController rootLevelTypeCtrl;
	private EditTaxonomyLevelTypeController editLevelTypeCtrl;
	
	private Taxonomy taxonomy;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public TaxonomyLevelTypesEditController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl, "admin_level_types");
		this.taxonomy = taxonomy;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addRootTypeButton = uifactory.addFormLink("add.root.type", formLayout, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TypesCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		
		model = new TaxonomyLevelTypesTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "types", model, 25, false, getTranslator(), formLayout);
	}
	
	private void loadModel() {
		List<TaxonomyLevelType> types = taxonomyService.getTaxonomyLevelTypes(taxonomy);
		List<TaxonomyLevelTypeRow> rows = types
				.stream().map(t -> forgeRow(t))
				.collect(Collectors.toList());
		model.setObjects(rows);
		tableEl.reset(false, true, true);
	}
	
	private TaxonomyLevelTypeRow forgeRow(TaxonomyLevelType type) {
		return new TaxonomyLevelTypeRow(type);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(rootLevelTypeCtrl == source || editLevelTypeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(rootLevelTypeCtrl);
		removeAsListenerAndDispose(cmc);
		rootLevelTypeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addRootTypeButton == source) {
			doAddRootType(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					TaxonomyLevelTypeRow row = model.getObject(se.getIndex());
					doEditLevelType(ureq, row.getType());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doAddRootType(UserRequest ureq) {
		rootLevelTypeCtrl = new EditTaxonomyLevelTypeController(ureq, this.getWindowControl(), null, taxonomy);
		listenTo(rootLevelTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", rootLevelTypeCtrl.getInitialComponent(), true, translate("add.root.type"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditLevelType(UserRequest ureq, TaxonomyLevelTypeRef type) {
		TaxonomyLevelType reloadedType = taxonomyService.getTaxonomyLevelType(type);
		editLevelTypeCtrl = new EditTaxonomyLevelTypeController(ureq, this.getWindowControl(), reloadedType, taxonomy);
		listenTo(editLevelTypeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editLevelTypeCtrl.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
}
