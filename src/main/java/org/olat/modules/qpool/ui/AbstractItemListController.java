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
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.MarkManager;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.ui.QuestionItemDataModel.Cols;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractItemListController extends FormBasicController implements ItemRowsSource {

	private FlexiTableElement itemsTable;
	private QuestionItemDataModel model;
	
	private final MarkManager markManager;
	private final QPoolService qpoolService;
	
	private QuestionItemsSource source;
	
	public AbstractItemListController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl, "item_list");

		this.source = source;
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.key.i18nKey(), Cols.key.ordinal(), true, "key"));
		//columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.identifier.i18nKey(), Cols.identifier.ordinal(), true, "identifier"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal(), true, "title"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.taxnonomyLevel.i18nKey(), Cols.taxnonomyLevel.ordinal(), true, "taxonomyLevel.field"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.difficulty.i18nKey(), Cols.difficulty.ordinal(), true, "point"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18nKey(), Cols.type.ordinal(), true, "type"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.format.i18nKey(), Cols.format.ordinal(), true, "format"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.rating.i18nKey(), Cols.rating.ordinal(), true, "rating"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status.i18nKey(), Cols.status.ordinal(), true, "status"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("select", translate("select"), "select-item"));
		
		model = new QuestionItemDataModel(columnsModel, this, getTranslator());
		itemsTable = uifactory.addTableElement(ureq, "items", model, model, 20, true, getTranslator(), formLayout);
		itemsTable.setMultiSelect(true);
		itemsTable.setRendererType(FlexiTableRendererType.dataTables);
		
		initButtons(formLayout);
	}
	
	protected void initButtons(FormItemContainer formLayout) {
		//to override
	}
	
	protected FlexiTableElement getItemsTable() {
		return itemsTable;
	}
	
	protected QuestionItemDataModel getModel() {
		return model;
	}
	
	protected String getTableFormDispatchId() {
		return itemsTable == null ? null : itemsTable.getFormDispatchId();
	}

	public void reset() {
		itemsTable.reset();
	}
	
	public QuestionItemsSource getSource() {
		return source;
	}
	
	public void updateSource(QuestionItemsSource source) {
		this.source = source;
		itemsTable.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == itemsTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("rSelect".equals(se.getCommand())) {
					ItemRow row = model.getObject(se.getIndex());
					doClick(ureq, row);
				} else if("select-item".equals(se.getCommand())) {
					ItemRow row = getModel().getObject(se.getIndex());
					doSelect(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("select".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				doSelect(ureq, row);
			} else if("mark".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				if(doMark(ureq, row)) {
					link.setCustomEnabledLinkCSS("b_mark_set");
				} else {
					link.setCustomEnabledLinkCSS("b_mark_not_set");
				}
				link.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	public List<QuestionItemShort> getShortItems(Set<Integer> index) {
		List<QuestionItemShort> items = new ArrayList<QuestionItemShort>();
		for(Integer i:index) {
			ItemRow row = model.getObject(i.intValue());
			if(row != null) {
				items.add(row);
			}
		}
		return items;
	}
	
	public List<QuestionItemView> getItemViews(Set<Integer> index) {
		List<QuestionItemView> items = new ArrayList<QuestionItemView>();
		for(Integer i:index) {
			ItemRow row = model.getObject(i.intValue());
			if(row != null) {
				items.add(row);
			}
		}
		return items;
	}

	public QuestionItemShort getQuestionItemAt(int index) {
		ItemRow row = model.getObject(index);
		if(row != null) {
			return qpoolService.loadItemById(row.getKey());
		}
		return null;
	}
	
	protected void doClick(UserRequest ureq, ItemRow row) {
		fireEvent(ureq, new QItemEvent("rSelect", row));
	}
	
	protected abstract void doSelect(UserRequest ureq, ItemRow row);
	
	protected boolean doMark(UserRequest ureq, OLATResourceable item) {
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.deleteMark(item);
			return false;
		} else {
			String businessPath = "[QuestionItem:" + item.getResourceableId() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public int getRowCount() {
		return source.getNumOfItems();
	}

	@Override
	public ResultInfos<ItemRow> getRows(String query, List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
		Set<Long> marks = markManager.getMarkResourceIds(getIdentity(), "QuestionItem", Collections.<String>emptyList());

		ResultInfos<QuestionItemView> items = source.getItems(query, condQueries, firstResult, maxResults, orderBy);
		List<ItemRow> rows = new ArrayList<ItemRow>(items.getObjects().size());
		for(QuestionItemView item:items.getObjects()) {
			ItemRow row = forgeRow(item, marks);
			rows.add(row);
		}
		return new DefaultResultInfos<ItemRow>(items.getNextFirstResult(), items.getCorrectedRowCount(), rows);
	}
	
	protected ItemRow forgeRow(QuestionItemView item, Set<Long> markedQuestionKeys) {
		boolean marked = markedQuestionKeys.contains(item.getKey());
		ItemRow row = new ItemRow(item);
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "&nbsp;&nbsp;&nbsp;&nbsp;", null, null, Link.NONTRANSLATED);
		markLink.setCustomEnabledLinkCSS(marked ? "b_mark_set" : "b_mark_not_set");
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		return row;
	}
}
