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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.QItemList;
import org.olat.modules.qpool.ui.events.QItemEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.modules.qpool.ui.events.QPoolSelectionEvent;
import org.olat.modules.qpool.ui.metadata.MetadataBulkChangeController;
import org.olat.modules.qpool.ui.wizard.Export_1_TypeStep;
import org.olat.modules.qpool.ui.wizard.ImportAuthor_1_ChooseMemberStep;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositoryAddController;
import org.olat.repository.controllers.RepositoryDetailsController;

/**
 * 
 * This controller wrap the table of qitems and decorate it with
 * features like copy, delete...<br/>
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionListController extends AbstractItemListController implements StackedControllerAware {

	private FormLink list, exportItem, shareItem, removeItem, copyItem, deleteItem, authorItem, importItem, bulkChange;
	
	private StackedController stackPanel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCopyBox;
	private DialogBoxController confirmDeleteBox;
	private DialogBoxController confirmRemoveBox;
	private ShareItemOptionController shareItemsCtrl;
	private PoolsController selectPoolCtrl;
	private SelectBusinessGroupController selectGroupCtrl;
	private CreateCollectionController createCollectionCtrl;
	private CollectionListController chooseCollectionCtrl;
	private StepsMainRunController exportWizard;
	private StepsMainRunController importAuthorsWizard;
	private ImportController importItemCtrl;
	private CollectionTargetController listTargetCtrl;
	private ShareTargetController shareTargetCtrl;
	private CloseableCalloutWindowController shareCalloutCtrl;
	private RepositoryAddController addController;
	private QuestionItemDetailsController currentDetailsCtrl;
	private LayoutMain3ColsController currentMainDetailsCtrl;
	private MetadataBulkChangeController bulkChangeCtrl;
	private ImportSourcesController importSourcesCtrl;
	private CloseableCalloutWindowController importCalloutCtrl;
	private ReferencableEntriesSearchController importTestCtrl;
	
	private final QPoolService qpoolService;
	private final RepositoryManager repositoryManager;
	
	public QuestionListController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source, String key) {
		super(ureq, wControl, source, key);

		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
	}

	@Override
	protected void initButtons(FormItemContainer formLayout) {
		list = uifactory.addFormLink("list", formLayout, Link.BUTTON);
		exportItem = uifactory.addFormLink("export.item", formLayout, Link.BUTTON);
		shareItem = uifactory.addFormLink("share.item", formLayout, Link.BUTTON);
		if(getSource().isRemoveEnabled()) {
			removeItem = uifactory.addFormLink("unshare.item", formLayout, Link.BUTTON);
		}
		
		copyItem = uifactory.addFormLink("copy", formLayout, Link.BUTTON);
		importItem = uifactory.addFormLink("import.item", formLayout, Link.BUTTON);
		authorItem = uifactory.addFormLink("author.item", formLayout, Link.BUTTON);
		deleteItem = uifactory.addFormLink("delete.item", formLayout, Link.BUTTON);
		bulkChange = uifactory.addFormLink("bulk.change", formLayout, Link.BUTTON);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link == list) {
				doList(ureq);
			} else if(link == exportItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doExport(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == shareItem) {
				doShare(ureq);
			} else if(link == removeItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doConfirmRemove(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == copyItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doConfirmCopy(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == deleteItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doConfirmDelete(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == authorItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doChooseAuthoren(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == importItem) {
				doOpenImport(ureq);
			} else if(link == bulkChange) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(items.size() > 0) {
					doBulkChange(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == shareTargetCtrl) {
			List<QuestionItemShort> items = getSelectedShortItems();
			shareCalloutCtrl.deactivate();
			if(items.isEmpty()) {
				showWarning("error.select.one");
			} else if(ShareTargetController.SHARE_GROUP_CMD.equals(event.getCommand())) {
				doSelectGroup(ureq, items);
			} else if(ShareTargetController.SHARE_POOL_CMD.equals(event.getCommand())) {
				doSelectPool(ureq, items);
			}
		} else if(source == selectPoolCtrl) {
			cmc.deactivate();
			if(event instanceof QPoolSelectionEvent) {
				QPoolSelectionEvent qpe = (QPoolSelectionEvent)event;
				List<Pool> pools = qpe.getPools();
				if(pools.size() > 0) {
					@SuppressWarnings("unchecked")
					List<QuestionItemShort> items = (List<QuestionItemShort>)selectPoolCtrl.getUserObject();
					doShareItemsToGroups(ureq, items, null, pools);
				}
			}	
		} else if(source == importSourcesCtrl) {
			importCalloutCtrl.deactivate();
			if(ImportSourcesController.IMPORT_FILE.equals(event.getCommand())) {
				doOpenFileImport(ureq);
			} else if(ImportSourcesController.IMPORT_REPO.equals(event.getCommand())) {
				doOpenRepositoryImport(ureq);
			}
		} else if(source == selectGroupCtrl) {
			cmc.deactivate();
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					@SuppressWarnings("unchecked")
					List<QuestionItemShort> items = (List<QuestionItemShort>)selectGroupCtrl.getUserObject();
					doShareItemsToGroups(ureq, items, groups, null);
				}
			}
		} else if(source == shareItemsCtrl) {
			if(event instanceof QPoolEvent) {
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == listTargetCtrl) {
			List<QuestionItemShort> items = getSelectedShortItems();
			shareCalloutCtrl.deactivate();
			if(CollectionTargetController.ADD_TO_LIST_POOL_CMD.equals(event.getCommand())) {
				if(items.isEmpty()) {
					showWarning("error.select.one");
				} else {
					doChooseCollection(ureq, items);
				}
			} else if(CollectionTargetController.NEW_LIST_CMD.equals(event.getCommand())) {
				doAskCollectionName(ureq, items);
			}
		} else if(source == createCollectionCtrl) {
			if(Event.DONE_EVENT == event) {
				List<QuestionItemShort> items = (List<QuestionItemShort>)createCollectionCtrl.getUserObject();
				String collectionName = createCollectionCtrl.getName();
				doCreateCollection(ureq, collectionName, items);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == chooseCollectionCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if(source == exportWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(exportWizard);
				StepsRunContext runContext = exportWizard.getRunContext();
				exportWizard = null;
				if(event == Event.CHANGED_EVENT) {
					doExecuteExport(ureq, runContext);
				}
			}
		} else if(source == importAuthorsWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(importAuthorsWizard);
				importAuthorsWizard = null;
			}
		} else if(source == importItemCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getItemsTable().reset();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == importTestCtrl) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doImportResource(ureq, importTestCtrl.getSelectedEntry());
				getItemsTable().reset();
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == bulkChangeCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateRows(bulkChangeCtrl.getUpdatedItems());
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmCopyBox) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmCopyBox.getUserObject();
				doCopy(ureq, items);
			}
		} else if(source == confirmDeleteBox) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmDeleteBox.getUserObject();
				doDelete(ureq, items);
			}
		} else if(source == confirmRemoveBox) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmRemoveBox.getUserObject();
				doRemove(ureq, items);
			}
		} else if(source == currentDetailsCtrl) {
			if(event instanceof QItemEvent) {
				QItemEvent qce = (QItemEvent)event;
				if("copy-item".equals(qce.getCommand())) {
					stackPanel.popUpToRootController(ureq);
					doSelect(ureq, qce.getItem(), true);
				} else if("previous".equals(qce.getCommand())) {
					doPrevious(ureq, qce.getItem());
				} else if("next".equals(qce.getCommand())) {
					doNext(ureq, qce.getItem());
				}
			}
		} else if(source == addController) {
			if(event instanceof EntryChangedEvent) {
				cmc.deactivate();
				cleanUp();
				
				EntryChangedEvent addEvent = (EntryChangedEvent)event;
				Long repoEntryKey = addEvent.getChangedEntryKey();
				doExportToRepositoryEntry(ureq, repoEntryKey);
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();
				cleanUp();
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(addController);
		removeAsListenerAndDispose(bulkChangeCtrl);
		removeAsListenerAndDispose(importItemCtrl);
		removeAsListenerAndDispose(importTestCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(createCollectionCtrl);
		cmc = null;
		addController = null;
		bulkChangeCtrl = null;
		importItemCtrl = null;
		importTestCtrl = null;
		selectGroupCtrl = null;
		createCollectionCtrl = null;
	}
	
	protected void updateRows(List<QuestionItem> items) {
		List<Integer> rowIndex = getIndex(items);
		getModel().reload(rowIndex);
		getItemsTable().getComponent().setDirty(true);
	}
	
	private void doNext(UserRequest ureq, QuestionItem item) {
		ItemRow row = getRowByItemKey(item.getKey());
		ItemRow nextRow = getModel().getNextObject(row);
		if(nextRow != null) {
			QuestionItem nextItem = qpoolService.loadItemById(nextRow.getKey());
			stackPanel.popUpToRootController(ureq);
			doSelect(ureq, nextItem, row.isEditable());
		}
	}
	
	private void doPrevious(UserRequest ureq, QuestionItem item) {
		ItemRow row = getRowByItemKey(item.getKey());
		ItemRow previousRow = getModel().getPreviousObject(row);
		if(previousRow != null) {
			QuestionItem previousItem = qpoolService.loadItemById(previousRow.getKey());
			stackPanel.popUpToRootController(ureq);
			doSelect(ureq, previousItem, row.isEditable());
		}
	}
	
	private void doBulkChange(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(bulkChangeCtrl);
		bulkChangeCtrl = new MetadataBulkChangeController(ureq, getWindowControl(), items);
		listenTo(bulkChangeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				bulkChangeCtrl.getInitialComponent(), true, translate("bulk.change"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenImport(UserRequest ureq) {
		String title = translate("import");
		removeAsListenerAndDispose(importSourcesCtrl);
		importSourcesCtrl = new ImportSourcesController(ureq, getWindowControl());
		listenTo(importSourcesCtrl);
		
		removeAsListenerAndDispose(importCalloutCtrl);
		importCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), importSourcesCtrl.getInitialComponent(), importItem, title, true, null);
		listenTo(importCalloutCtrl);
		importCalloutCtrl.activate();	
	}
	
	private void doOpenFileImport(UserRequest ureq) {
		removeAsListenerAndDispose(importItemCtrl);
		importItemCtrl = new ImportController(ureq, getWindowControl());
		listenTo(importItemCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importItemCtrl.getInitialComponent(), true, translate("import.item"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doImportResource(UserRequest ureq, RepositoryEntry repositoryEntry) {
		QTIQPoolServiceProvider spi
			= (QTIQPoolServiceProvider)CoreSpringFactory.getBean("qtiPoolServiceProvider");
		List<QuestionItem> importItems = spi.importRepositoryEntry(getIdentity(), repositoryEntry, getLocale());
		if(importItems.isEmpty()) {
			showWarning("import.failed");
		} else {
			showInfo("import.success", Integer.toString(importItems.size()));
			getItemsTable().reset();
		}
	}
	
	private void doOpenRepositoryImport(UserRequest ureq) {
		removeAsListenerAndDispose(importTestCtrl);
		importTestCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, new String[]{ TestFileResource.TYPE_NAME },
				translate("import.repository"), false, false, false, false, true);
		listenTo(importTestCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importTestCtrl.getInitialComponent(), true, translate("import.repository"));
		cmc.activate();
		listenTo(cmc);
	}
	
	protected void doList(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(shareTargetCtrl);
		listTargetCtrl = new CollectionTargetController(ureq, getWindowControl());
		listenTo(listTargetCtrl);
		
		removeAsListenerAndDispose(shareCalloutCtrl);
		shareCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), listTargetCtrl.getInitialComponent(), list, title, true, null);
		listenTo(shareCalloutCtrl);
		shareCalloutCtrl.activate();	
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
	
	private void doChooseCollection(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(chooseCollectionCtrl);
		chooseCollectionCtrl = new CollectionListController(ureq, getWindowControl());
		chooseCollectionCtrl.setUserObject(items);
		listenTo(chooseCollectionCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				chooseCollectionCtrl.getInitialComponent(), true, translate("add.to.list"));
		cmc.activate();
		listenTo(cmc);
	}
	
	/**
	 * Test only QTI 1.2
	 * @param ureq
	 * @param items
	 */
	private void doExport(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(exportWizard);
		Step start = new Export_1_TypeStep(ureq, items);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		exportWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("export.item"), "o_sel_qpool_export_1_wizard");
		listenTo(exportWizard);
		getWindowControl().pushAsModalDialog(exportWizard.getInitialComponent());
	}
	
	private void doExecuteExport(UserRequest ureq, StepsRunContext runContext) {
		ExportFormatOptions format = (ExportFormatOptions)runContext.get("format");
		@SuppressWarnings("unchecked")
		List<QuestionItemShort> items = (List<QuestionItemShort>)runContext.get("itemsToExport");
		switch(format.getOutcome()) {
			case download: {
				MediaResource mr = qpoolService.export(items, format);
				if(mr != null) {
					ureq.getDispatchResult().setResultingMediaResource(mr);
				}
				break;
			}
			case repository: {
				doExportToRepository(ureq, items);
				break;
			}
		}
	}
	
	private void doExportToRepository(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(addController);
		
		addController = new RepositoryAddController(ureq, getWindowControl(), "a.nte");
		addController.setUserObject(new QItemList(items));
		listenTo(addController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	private void doExportToRepositoryEntry(UserRequest ureq, Long repoEntryKey) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoEntryKey, false);
		if(re != null) {
			//open editor
			RepositoryDetailsController.doEdit(ureq, re);
		}
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
		getItemsTable().reset();
		showInfo("item.deleted");
	}
	
	protected void doShare(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(shareTargetCtrl);
		shareTargetCtrl = new ShareTargetController(ureq, getWindowControl());
		listenTo(shareTargetCtrl);
		
		removeAsListenerAndDispose(shareCalloutCtrl);
		shareCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), shareTargetCtrl.getInitialComponent(), shareItem, title, true, null);
		listenTo(shareCalloutCtrl);
		shareCalloutCtrl.activate();	
	}
	
	private void doConfirmRemove(UserRequest ureq, List<QuestionItemShort> items) {
		String text = translate("confirm.unshare", new String[]{ getSource().getName() });
		confirmRemoveBox = activateYesNoDialog(ureq, null, text, confirmRemoveBox);
		confirmRemoveBox.setUserObject(items);
	}
	
	protected void doRemove(UserRequest ureq, List<QuestionItemShort> items) {
		getSource().removeFromSource(items);
		getItemsTable().reset();
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
	
	protected void doSelectPool(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(selectPoolCtrl);
		selectPoolCtrl = new PoolsController(ureq, getWindowControl());
		selectPoolCtrl.setUserObject(items);
		listenTo(selectPoolCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectPoolCtrl.getInitialComponent(), true, translate("select.pool"));
		cmc.activate();
		listenTo(cmc);
	}

	protected void doConfirmCopy(UserRequest ureq, List<QuestionItemShort> items) {
		String title = translate("copy");
		String text = translate("copy.confirmation");
		confirmCopyBox = activateYesNoDialog(ureq, title, text, confirmCopyBox);
		confirmCopyBox.setUserObject(items);
	}
	
	protected void doCopy(UserRequest ureq, List<QuestionItemShort> items) {
		List<QuestionItem> copies = qpoolService.copyItems(getIdentity(), items);
		getItemsTable().reset();
		showInfo("item.copied", Integer.toString(copies.size()));
		fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
	}
	
	private void doShareItemsToGroups(UserRequest ureq, List<QuestionItemShort> items, List<BusinessGroup> groups, List<Pool> pools) {
		removeAsListenerAndDispose(shareItemsCtrl);
		shareItemsCtrl = new ShareItemOptionController(ureq, getWindowControl(), items, groups, pools);
		listenTo(shareItemsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				shareItemsCtrl.getInitialComponent(), true, translate("share.item"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	protected void doSelect(UserRequest ureq, ItemRow row) {
		QuestionItem item = qpoolService.loadItemById(row.getKey());
		doSelect(ureq, item, row.isEditable());
	}
		
	protected void doSelect(UserRequest ureq, QuestionItem item, boolean editable) {
		removeAsListenerAndDispose(currentDetailsCtrl);
		removeAsListenerAndDispose(currentMainDetailsCtrl);
		
		currentDetailsCtrl = new QuestionItemDetailsController(ureq, getWindowControl(), item, editable);
		currentDetailsCtrl.setStackedController(stackPanel);
		listenTo(currentDetailsCtrl);
		currentMainDetailsCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), currentDetailsCtrl);
		listenTo(currentMainDetailsCtrl);
		stackPanel.pushController(item.getTitle(), currentMainDetailsCtrl);
	}
}
