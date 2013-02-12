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
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionListController extends FormBasicController implements StackedControllerAware, ItemRowsSource {

	private FlexiTableElement itemsTable;
	private QuestionItemDataModel model;
	private StackedController stackPanel;
	
	private final MarkManager markManager;
	protected final QuestionPoolService qpoolService;
	
	private final QuestionItemsSource source;
	
	public QuestionListController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl, "item_list");
		
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		this.source = source;
		
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("item.key"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("item.subject"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("mark"));

		model = new QuestionItemDataModel(columnsModel, this);
		itemsTable = uifactory.addTableElement(ureq, "items", model, 20, getTranslator(), formLayout);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// 
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("select".equals(link.getCmd())) {
				QuestionItemRow row = (QuestionItemRow)link.getUserObject();
				doSelect(ureq, row.getItem());
			} else if("mark".equals(link.getCmd())) {
				QuestionItemRow row = (QuestionItemRow)link.getUserObject();
				if(doMark(ureq, row.getItem())) {
					link.setI18nKey("Mark_true");
				} else {
					link.setI18nKey("Mark_false");
				}
				link.getComponent().setDirty(true);
			}
		} else if(source == itemsTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				QuestionItemRow row = model.getObject(se.getIndex());
				QuestionItem item = row.getItem();
				fireEvent(ureq, new QuestionEvent(se.getCommand(), item));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected void doSelect(UserRequest ureq, QuestionItem item) {
		QuestionItemDetailsController detailsCtrl = new QuestionItemDetailsController(ureq, getWindowControl(), item);
		LayoutMain3ColsController mainCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), detailsCtrl);
		stackPanel.pushController(item.getSubject(), mainCtrl);
	}
	
	protected boolean doMark(UserRequest ureq, QuestionItem item) {
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.deleteMark(item);
			return false;
		} else {
			String businessPath = "[QuestionItem:" + item.getKey() + "]";
			markManager.setMark(item, getIdentity(), null, businessPath);
			return true;
		}
	}

	@Override
	public int getRowCount() {
		return source.getNumOfItems();
	}

	@Override
	public List<QuestionItemRow> getRows(int firstResult, int maxResults) {
		Set<Long> marks = markManager.getMarkResourceIds(getIdentity(), "QuestionItem", Collections.<String>emptyList());
		
		
		List<QuestionItem> items = source.getItems(firstResult, maxResults);
		List<QuestionItemRow> rows = new ArrayList<QuestionItemRow>(items.size());
		for(QuestionItem item:items) {
			QuestionItemRow row = forgeRow(item, marks);
			rows.add(row);
		}
		return rows;
	}
	
	protected QuestionItemRow forgeRow(QuestionItem item, Set<Long> markedQuestionKeys) {
		boolean marked = markedQuestionKeys.contains(item.getKey());
		
		QuestionItemRow row = new QuestionItemRow(item);
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "Mark_" + marked, null, null, Link.NONTRANSLATED);
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		FormLink selectLink = uifactory.addFormLink("select_" + row.getKey(), "select", "Select", null, null, Link.NONTRANSLATED);
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
		return row;
	}
}
