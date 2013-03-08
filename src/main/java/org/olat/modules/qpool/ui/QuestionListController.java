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
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.ui.QuestionItemDataModel.Cols;
import org.olat.modules.qpool.ui.wizard.ImportAuthor_1_ChooseMemberStep;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionListController extends FormBasicController implements StackedControllerAware, ItemRowsSource {

	private FormLink createList, shareItem, deleteItem, authorItem, importItem;
	
	private FlexiTableElement itemsTable;
	private QuestionItemDataModel model;
	private StackedController stackPanel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteBox;
	private SelectBusinessGroupController selectGroupCtrl;
	private CreateCollectionController createCollectionCtrl;
	private StepsMainRunController importAuthorsWizard;
	private ImportController importItemCtrl;
	
	private final MarkManager markManager;
	private final QuestionPoolService qpoolService;
	
	private QuestionItemsSource source;
	
	public QuestionListController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl, "item_list");

		this.source = source;
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		
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
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.key.i18nKey(), Cols.key.ordinal(), true, "key"));
		//columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.identifier.i18nKey(), Cols.identifier.ordinal(), true, "identifier"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.title.i18nKey(), Cols.title.ordinal(), true, "title"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.taxnonomyLevel.i18nKey(), Cols.taxnonomyLevel.ordinal(), true, "taxonomyLevel.field"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.difficulty.i18nKey(), Cols.difficulty.ordinal(), true, "point"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.type.i18nKey(), Cols.type.ordinal(), true, "type"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.status.i18nKey(), Cols.status.ordinal(), true, "status"));
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel("select", translate("select"), "select-item"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.mark.i18nKey(), Cols.mark.ordinal()));

		model = new QuestionItemDataModel(columnsModel, this, getTranslator());
		itemsTable = uifactory.addTableElement(ureq, "items", model, model, 20, true, getTranslator(), formLayout);
		itemsTable.setMultiSelect(true);
		itemsTable.setRendererType(FlexiTableRendererType.dataTables);
		
		createList = uifactory.addFormLink("create.list", formLayout, Link.BUTTON);
		shareItem = uifactory.addFormLink("share.item", formLayout, Link.BUTTON);
		importItem = uifactory.addFormLink("import.item", formLayout, Link.BUTTON);
		authorItem = uifactory.addFormLink("author.item", formLayout, Link.BUTTON);
		deleteItem = uifactory.addFormLink("delete.item", formLayout, Link.BUTTON);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}
	
	public void reset() {
		itemsTable.reset();
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
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link == createList) {
				Set<Integer> selections = itemsTable.getMultiSelectedIndex();
				List<QuestionItemShort> items = getShortItems(selections);
				doAskCollectionName(ureq, items);
			} else if(link == shareItem) {
				Set<Integer> selections = itemsTable.getMultiSelectedIndex();
				if(selections.size() > 0) {
					List<QuestionItemShort> items = getShortItems(selections);
					doSelectGroup(ureq, items);
				}
			} else if(link == deleteItem) {
				Set<Integer> selections = itemsTable.getMultiSelectedIndex();
				if(selections.size() > 0) {
					List<QuestionItemShort> items = getShortItems(selections);
					doConfirmDelete(ureq, items);
				}
			} else if(link == authorItem) {
				Set<Integer> selections = itemsTable.getMultiSelectedIndex();
				if(selections.size() > 0) {
					List<QuestionItemShort> items = getShortItems(selections);
					doChooseAuthoren(ureq, items);
				}
			} else if(link == importItem) {
				doOpenImport(ureq);
			} else if("select".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				doSelect(ureq, row);
			} else if("mark".equals(link.getCmd())) {
				ItemRow row = (ItemRow)link.getUserObject();
				if(doMark(ureq, row)) {
					link.setI18nKey("Mark_true");
				} else {
					link.setI18nKey("Mark_false");
				}
				link.getComponent().setDirty(true);
			}
		} else if(source == itemsTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("rSelect".equals(se.getCommand())) {
					ItemRow row = model.getObject(se.getIndex());
					fireEvent(ureq, new QItemEvent(se.getCommand(), row));
				} else if("select-item".equals(se.getCommand())) {
					ItemRow row = model.getObject(se.getIndex());
					doSelect(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectGroupCtrl) {
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					@SuppressWarnings("unchecked")
					List<QuestionItemShort> items = (List<QuestionItemShort>)selectGroupCtrl.getUserObject();
					doShareItems(ureq, items, groups);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == createCollectionCtrl) {
			if(Event.DONE_EVENT == event) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)createCollectionCtrl.getUserObject();
				String collectionName = createCollectionCtrl.getName();
				doCreateCollection(ureq, collectionName, items);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == importAuthorsWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importAuthorsWizard);
				importAuthorsWizard = null;
			}
		} else if(source == importItemCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				//
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmDeleteBox.getUserObject();
				doDelete(ureq, items);
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(importItemCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(createCollectionCtrl);
		cmc = null;
		importItemCtrl = null;
		selectGroupCtrl = null;
		createCollectionCtrl = null;
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

	public QuestionItemShort getQuestionItemAt(int index) {
		ItemRow row = model.getObject(index);
		if(row != null) {
			return qpoolService.loadItemById(row.getKey());
		}
		return null;
	}
	
	private void doOpenImport(UserRequest ureq) {
		removeAsListenerAndDispose(importItemCtrl);
		importItemCtrl = new ImportController(ureq, getWindowControl());
		listenTo(importItemCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importItemCtrl.getInitialComponent(), true, translate("import.item"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAskCollectionName(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(createCollectionCtrl);
		createCollectionCtrl = new CreateCollectionController(ureq, getWindowControl());
		createCollectionCtrl.setUserObject(items);
		listenTo(createCollectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createCollectionCtrl.getInitialComponent(), true, translate("create.list"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doCreateCollection(UserRequest ureq, String name, List<QuestionItemShort> items) {
		qpoolService.createCollection(getIdentity(), name, items);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_CREATED));
	}
	
	private void doChooseAuthoren(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(importAuthorsWizard);

		Step start = new ImportAuthor_1_ChooseMemberStep(ureq, items);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				addAuthors(ureq, runContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		importAuthorsWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("author.item"), "o_sel_qpool_import_1_wizard");
		listenTo(importAuthorsWizard);
		getWindowControl().pushAsModalDialog(importAuthorsWizard.getInitialComponent());
	}
	
	private void addAuthors(UserRequest ureq, StepsRunContext runContext) {
		@SuppressWarnings("unchecked")
		List<QuestionItemShort> items = (List<QuestionItemShort>)runContext.get("items");
		@SuppressWarnings("unchecked")
		List<Identity> authors = (List<Identity>)runContext.get("members");
		qpoolService.addAuthors(authors, items);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<QuestionItemShort> items) {
		confirmDeleteBox = activateYesNoDialog(ureq, null, translate("confirm.delete"), confirmDeleteBox);
		confirmDeleteBox.setUserObject(items);
	}
	
	private void doDelete(UserRequest ureq, List<QuestionItemShort> items) {
		qpoolService.deleteItems(items);
		itemsTable.reset();
		showInfo("item.deleted");
	}
	
	protected void doSelectGroup(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl());
		selectGroupCtrl.setUserObject(items);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShareItems(UserRequest ureq, List<QuestionItemShort> items, List<BusinessGroup> groups) {
		qpoolService.shareItems(items, groups);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_SHARED));
	}
	
	protected void doSelect(UserRequest ureq, ItemRow row) {
		QuestionItem item = qpoolService.loadItemById(row.getKey());
		QuestionItemDetailsController detailsCtrl = new QuestionItemDetailsController(ureq, getWindowControl(), item);
		LayoutMain3ColsController mainCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), detailsCtrl);
		stackPanel.pushController(item.getTitle(), mainCtrl);
	}
	
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

		ResultInfos<QuestionItemShort> items = source.getItems(query, condQueries, firstResult, maxResults, orderBy);
		List<ItemRow> rows = new ArrayList<ItemRow>(items.getObjects().size());
		for(QuestionItemShort item:items.getObjects()) {
			ItemRow row = forgeRow(item, marks);
			rows.add(row);
		}
		return new DefaultResultInfos<ItemRow>(items.getNextFirstResult(), items.getCorrectedRowCount(), rows);
	}
	
	protected ItemRow forgeRow(QuestionItemShort item, Set<Long> markedQuestionKeys) {
		boolean marked = markedQuestionKeys.contains(item.getKey());
		
		ItemRow row = new ItemRow(item);
		FormLink markLink = uifactory.addFormLink("mark_" + row.getKey(), "mark", "Mark_" + marked, null, null, Link.NONTRANSLATED);
		markLink.setUserObject(row);
		row.setMarkLink(markLink);
		return row;
	}
}
