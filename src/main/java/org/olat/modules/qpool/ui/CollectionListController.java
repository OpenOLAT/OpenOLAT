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
package org.olat.modules.qpool.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;

/**
 * 
 * Initial date: 24.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectionListController extends FormBasicController {
	
	private FormLink selectButton;
	private CollectionsDataModel model;
	private FlexiTableElement collectionsTable;
	
	private List<QuestionItemShort> items;
	private final QPoolService qpoolService;
	
	public CollectionListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "collection_choose");
		
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		initForm(ureq);
	}
	
	public List<QuestionItemShort> getUserObject() {
		return items;
	}
	
	public void setUserObject(List<QuestionItemShort> items) {
		this.items = items;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<QuestionItemCollection> colls = qpoolService.getCollections(getIdentity());
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("collection.name", 0));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("collection.creationDate", 1));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select-coll"));
		
		model = new CollectionsDataModel(colls, columnsModel);
		collectionsTable = uifactory.addTableElement(getWindowControl(), "collections", model, getTranslator(), formLayout);
		collectionsTable.setMultiSelect(true);
		collectionsTable.setRendererType(FlexiTableRendererType.classic);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		selectButton = uifactory.addFormLink("select", buttonsCont, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectButton == source) {
			Set<Integer> selectedIndex = collectionsTable.getMultiSelectedIndex();
			List<QuestionItemCollection> collections = getShortItems(selectedIndex);
			qpoolService.addItemToCollection(items, collections);
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(collectionsTable == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-coll".equals(se.getCommand())) {
					QuestionItemCollection row = model.getObject(se.getIndex());
					qpoolService.addItemToCollection(items, Collections.singletonList(row));
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public List<QuestionItemCollection> getShortItems(Set<Integer> index) {
		List<QuestionItemCollection> collections = new ArrayList<>();
		for(Integer i:index) {
			QuestionItemCollection row = model.getObject(i.intValue());
			if(row != null) {
				collections.add(row);
			}
		}
		return collections;
	}
	
	private static class CollectionsDataModel extends DefaultFlexiTableDataModel<QuestionItemCollection> {
		public CollectionsDataModel(List<QuestionItemCollection> colls, FlexiTableColumnModel columnsModel) {
			super(colls, columnsModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			QuestionItemCollection coll = getObject(row);
			switch(col) {
				case 0: return coll.getName();
				case 1: return coll.getCreationDate();
				default: return "";
			}
		}
	}
}
