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

import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.PopEvent;
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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.qti.questionimport.ItemsPackage;
import org.olat.ims.qti.questionimport.QImport_1_InputStep;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.modules.qpool.model.QItemList;
import org.olat.modules.qpool.ui.events.QItemChangeEvent;
import org.olat.modules.qpool.ui.events.QItemCreationCmdEvent;
import org.olat.modules.qpool.ui.events.QItemEdited;
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
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.CreateRepositoryEntryController;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This controller wrap the table of qitems and decorate it with
 * features like copy, delete...<br/>
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionListController extends AbstractItemListController implements BreadcrumbPanelAware {

	private FormLink list, exportItem, shareItem, removeItem, newItem, copyItem, deleteItem, authorItem, importItem, bulkChange;

	private BreadcrumbPanel stackPanel;
	private RenameController renameCtrl;
	private CloseableModalController cmc;
	private CloseableModalController cmcNewItem;
	private CloseableModalController cmcShareItemToSource;
	private QPoolItemEditorController newItemCtrl;
	private DialogBoxController confirmCopyBox;
	private DialogBoxController confirmDeleteBox;
	private DialogBoxController confirmRemoveBox;
	private DialogBoxController confirmDeleteSourceBox;
	private ShareItemOptionController shareItemsCtrl;
	private ShareItemSourceOptionController shareItemsToSourceCtrl;
	private PoolsController selectPoolCtrl;
	private SelectBusinessGroupController selectGroupCtrl;
	private CreateCollectionController createCollectionCtrl;
	private CollectionListController chooseCollectionCtrl;
	private StepsMainRunController exportWizard;
	private StepsMainRunController importAuthorsWizard;
	private StepsMainRunController excelImportWizard;
	private ImportController importItemCtrl;
	private CollectionTargetController listTargetCtrl;
	private ShareTargetController shareTargetCtrl;
	private CreateRepositoryEntryController addController;
	private QuestionItemDetailsController currentDetailsCtrl;
	private LayoutMain3ColsController currentMainDetailsCtrl;
	private MetadataBulkChangeController bulkChangeCtrl;
	private ImportSourcesController importSourcesCtrl;
	private NewItemOptionsController newItemOptionsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ReferencableEntriesSearchController importTestCtrl;
	
	private QuestionItemCollection itemCollection;
	
	private boolean itemCollectionDirty = false;

	@Autowired
	private LifeFullIndexer lifeFullIndexer;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public QuestionListController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source, String key) {
		super(ureq, wControl, source, key);
	}

	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		list = uifactory.addFormLink("list", formLayout, Link.BUTTON);
		exportItem = uifactory.addFormLink("export.item", formLayout, Link.BUTTON);
		shareItem = uifactory.addFormLink("share.item", formLayout, Link.BUTTON);
		if(getSource().isRemoveEnabled()) {
			removeItem = uifactory.addFormLink("unshare.item", formLayout, Link.BUTTON);
		}

		newItem = uifactory.addFormLink("new.item", formLayout, Link.BUTTON);
		copyItem = uifactory.addFormLink("copy", formLayout, Link.BUTTON);
		importItem = uifactory.addFormLink("import.item", formLayout, Link.BUTTON);
		authorItem = uifactory.addFormLink("author.item", formLayout, Link.BUTTON);
		if(getSource().isDeleteEnabled()) {
			deleteItem = uifactory.addFormLink("delete.item", formLayout, Link.BUTTON);
		}
		bulkChange = uifactory.addFormLink("bulk.change", formLayout, Link.BUTTON);
	}

	public QuestionItemCollection getItemCollection() {
		return itemCollection;
	}

	public void setItemCollection(QuestionItemCollection itemCollection) {
		this.itemCollection = itemCollection;
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(stackPanel != null) {
			stackPanel.addListener(this);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link == list) {
				doList(ureq);
			} else if(link == exportItem) {
				List<QuestionItemShort> items = getSelectedShortItems(false);
				if(items.size() > 0) {
					doExport(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == shareItem) {
				doShare(ureq);
			} else if(link == removeItem) {
				List<QuestionItemShort> items = getSelectedShortItems(false);
				if(items.size() > 0) {
					doConfirmRemove(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == copyItem) {
				List<QuestionItemShort> items = getSelectedShortItems(false);
				if(items.size() > 0) {
					doConfirmCopy(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == deleteItem) {
				List<QuestionItemShort> items = getSelectedShortItems(true);
				if(items.size() > 0) {
					doConfirmDelete(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == authorItem) {
				List<QuestionItemShort> items = getSelectedShortItems(false);
				if(items.size() > 0) {
					doChooseAuthoren(ureq, items);
				} else {
					showWarning("error.select.one");
				}
			} else if(link == importItem) {
				doOpenImport(ureq);
			} else if(link == newItem) {
				doChooseNewItemType(ureq);
			} else if(link == bulkChange) {
				List<QuestionItemShort> items = getSelectedShortItems(true);
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
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(itemCollectionDirty && event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				Controller mainCtrl = pe.getController();
				if(mainCtrl != null && mainCtrl.isControllerListeningTo(this)) {
					reloadData();
					itemCollectionDirty = false;
				}
			}
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == shareTargetCtrl) {
			List<QuestionItemShort> items = getSelectedShortItems(false);
			calloutCtrl.deactivate();
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
			calloutCtrl.deactivate();
			if(ImportSourcesController.IMPORT_FILE.equals(event.getCommand())) {
				doOpenFileImport(ureq);
			} else if(ImportSourcesController.IMPORT_REPO.equals(event.getCommand())) {
				doOpenRepositoryImport(ureq);
			} else if(ImportSourcesController.IMPORT_EXCEL.equals(event.getCommand())) {
				doOpenExcelImport(ureq);
			}
		} else if(source == newItemOptionsCtrl) {
			cmc.deactivate();
			if(event instanceof QItemCreationCmdEvent) {
				QItemCreationCmdEvent qicce = (QItemCreationCmdEvent)event;
				doCreateNewItem(ureq, qicce.getTitle(), qicce.getFactory());
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
			List<QuestionItemShort> items = getSelectedShortItems(false);
			calloutCtrl.deactivate();
			if(CollectionTargetController.ADD_TO_LIST_POOL_CMD.equals(event.getCommand())) {
				if(items.isEmpty()) {
					showWarning("error.select.one");
				} else {
					doChooseCollection(ureq, items);
				}
			} else if(CollectionTargetController.NEW_LIST_CMD.equals(event.getCommand())) {
				doAskCollectionName(ureq, items);
			} else if(CollectionTargetController.RENAME_LIST_CMD.equals(event.getCommand())) {
				doOpenRenameCallout(ureq);
			} else if(CollectionTargetController.DELETE_LIST_CMD.equals(event.getCommand())) {
				doConfirmDeleteSource(ureq);
			}
		} else if(source == createCollectionCtrl) {
			if(Event.DONE_EVENT == event) {
				List<QuestionItemShort> items = createCollectionCtrl.getUserObject();
				String collectionName = createCollectionCtrl.getName();
				doCreateCollection(ureq, collectionName, items);
			}
			cmc.deactivate();
			cleanUp();
		} if(source == confirmDeleteSourceBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				doDeleteItemCollection(ureq);
			}
		} else if(source == renameCtrl) {
			cmc.deactivate();
			if(Event.CHANGED_EVENT == event) {
				String newName = renameCtrl.getName();
				doRenameItemCollection(ureq, newName);
			}
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
		} else if(source == excelImportWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(excelImportWizard);
				excelImportWizard = null;
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
			RepositoryEntry re = importTestCtrl.getSelectedEntry();
			cmc.deactivate();
			cleanUp();
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doImportResource(ureq, re);
			}
		} else if(source == shareItemsToSourceCtrl) {
			if(QPoolEvent.ITEM_SHARED.equals(event.getCommand())) {
				getItemsTable().reset();
			}
			cmcShareItemToSource.deactivate();
			cleanUp();
		} else if(source == bulkChangeCtrl) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateRows(bulkChangeCtrl.getUpdatedItems());
				fireEvent(ureq, new QPoolEvent(QPoolEvent.BULK_CHANGE));
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
				doDelete(items);
			}
		} else if(source == confirmRemoveBox) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmRemoveBox.getUserObject();
				doRemove(items);
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
			} else if(event instanceof QItemEdited) {
				itemCollectionDirty = true;
			} else if (event instanceof QPoolEvent) {
				QPoolEvent qce = (QPoolEvent)event;
				if(QPoolEvent.ITEM_DELETED.equals(qce.getCommand())) {
					fireEvent(ureq, qce);
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
		} else if(source == cmcNewItem) {
			showInfo("create.success");
			if(newItemCtrl.getItem() != null && newItemCtrl.getItem().getKey() != null) {
				List<QuestionItem> newItems = Collections.singletonList(newItemCtrl.getItem());
				getSource().postImport(newItems, false);
			}
			getItemsTable().reset();
			QPoolEvent qce = new QPoolEvent(QPoolEvent.ITEM_CREATED);
			fireEvent(ureq, qce);
			cleanUp();
		} else if(source == newItemCtrl) {
			if(event instanceof QItemChangeEvent) {
				QItemChangeEvent ce = (QItemChangeEvent)event;
				if(ce.getItem() != null) {
					lifeFullIndexer.indexDocument(QItemDocument.TYPE, ce.getItem().getKey());
				}
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(cmcNewItem);
		removeAsListenerAndDispose(newItemCtrl);
		removeAsListenerAndDispose(addController);
		removeAsListenerAndDispose(bulkChangeCtrl);
		removeAsListenerAndDispose(importItemCtrl);
		removeAsListenerAndDispose(importTestCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(createCollectionCtrl);
		cmc = null;
		cmcNewItem = null;
		newItemCtrl = null;
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
	
	private void doChooseNewItemType(UserRequest ureq) {
		removeAsListenerAndDispose(newItemOptionsCtrl);
		newItemOptionsCtrl = new NewItemOptionsController(ureq, getWindowControl());
		listenTo(newItemOptionsCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				newItemOptionsCtrl.getInitialComponent(), true, translate("new.item"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doCreateNewItem(UserRequest ureq, String title, QItemFactory factory) {
		removeAsListenerAndDispose(newItemCtrl);
		
		newItemCtrl = factory.getEditor(ureq, getWindowControl(), title);
		listenTo(newItemCtrl);

		removeAsListenerAndDispose(cmcNewItem);
		cmcNewItem = new CloseableModalController(getWindowControl(), translate("close"),
				newItemCtrl.getInitialComponent(), true, translate("import.repository"));
		cmcNewItem.activate();
		listenTo(cmcNewItem);
	}
	
	private void doOpenImport(UserRequest ureq) {
		String title = translate("import");
		removeAsListenerAndDispose(importSourcesCtrl);
		importSourcesCtrl = new ImportSourcesController(ureq, getWindowControl());
		listenTo(importSourcesCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), importSourcesCtrl.getInitialComponent(), importItem, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doOpenFileImport(UserRequest ureq) {
		removeAsListenerAndDispose(importItemCtrl);
		importItemCtrl = new ImportController(ureq, getWindowControl(), getSource());
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
		if(getSource().askEditable()) {
			removeAsListenerAndDispose(shareItemsToSourceCtrl);
			shareItemsToSourceCtrl = new ShareItemSourceOptionController(ureq, getWindowControl(), importItems, getSource());
			listenTo(shareItemsToSourceCtrl);

			removeAsListenerAndDispose(cmcShareItemToSource);
			cmcShareItemToSource = new CloseableModalController(getWindowControl(), translate("close"),
					shareItemsToSourceCtrl.getInitialComponent(), true, translate("import.item"));
			cmcShareItemToSource.activate();
			listenTo(cmcShareItemToSource);
		} else {
			int postImported = getSource().postImport(importItems, true);
			if(postImported > 0) {
				getItemsTable().reset();
			}
			
			if(importItems.isEmpty()) {
				showWarning("import.failed");
			} else {
				showInfo("import.success", Integer.toString(importItems.size()));
				getItemsTable().reset();
			}
		}
	}
	
	private void doOpenRepositoryImport(UserRequest ureq) {
		removeAsListenerAndDispose(importTestCtrl);
		String[] allowed = new String[]{ TestFileResource.TYPE_NAME, SurveyFileResource.TYPE_NAME };
		importTestCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, allowed,
				null, translate("import.repository"), false, false, false, true, Can.copyable);
		listenTo(importTestCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importTestCtrl.getInitialComponent(), true, translate("import.repository"));
		cmc.setContextHelp(ureq, "org.olat.modules.qpool.ui", "import-repo.html", "help.hover.importrepo");
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenExcelImport(UserRequest ureq) {
		removeAsListenerAndDispose(excelImportWizard);
		
		final ItemsPackage importPackage = new ItemsPackage();
		Step additionalStep = null;
		if(getSource().askEditable()) {
			additionalStep = new EditableStep(ureq);
		}
		Step start = new QImport_1_InputStep(ureq, importPackage, additionalStep);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				List<ItemAndMetadata> itemsToImport = importPackage.getItems();
				QTIQPoolServiceProvider spi
					= (QTIQPoolServiceProvider)CoreSpringFactory.getBean("qtiPoolServiceProvider");
				List<QuestionItem> importItems = spi.importBeecomItem(getIdentity(), itemsToImport, getLocale());
				
				boolean editable = true;
				if(getSource().askEditable()) {
					Object editableCtx = runContext.get("editable");
					editable = (editableCtx instanceof Boolean) ? ((Boolean)editableCtx).booleanValue() : false;
				}
				int postImported = getSource().postImport(importItems, editable);
				if(postImported > 0) {
					getItemsTable().reset();
				}
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		excelImportWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.excellike"), "o_sel_qpool_excel_import_wizard");
		listenTo(excelImportWizard);
		getWindowControl().pushAsModalDialog(excelImportWizard.getInitialComponent());
	}
	
	protected void doList(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(listTargetCtrl);
		listTargetCtrl = new CollectionTargetController(ureq, getWindowControl(), itemCollection != null);
		listenTo(listTargetCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), listTargetCtrl.getInitialComponent(), list, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
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
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
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
		
		String type = TestFileResource.TYPE_NAME;
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
		addController = new CreateRepositoryEntryController(ureq, getWindowControl(), handler);
		addController.setCreateObject(new QItemList(items));
		listenTo(addController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportToRepositoryEntry(UserRequest ureq, Long repoEntryKey) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoEntryKey, false);
		if(re != null) {
			WindowControl wControl = BusinessControlFactory.getInstance()
					.createBusinessWindowControl(getWindowControl(), re, OresHelper.createOLATResourceableType("Editor"));
			NewControllerFactory.getInstance().launch(ureq, wControl);
		}
	}
	
	private void doCreateCollection(UserRequest ureq, String name, List<QuestionItemShort> items) {
		QuestionItemCollection coll = qpoolService.createCollection(getIdentity(), name, items);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_CREATED, coll.getKey()));
	}
	
	private void doChooseAuthoren(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(importAuthorsWizard);

		Step start = new ImportAuthor_1_ChooseMemberStep(ureq, items);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				addAuthors(runContext);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		importAuthorsWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("author.item"), "o_sel_qpool_import_1_wizard");
		listenTo(importAuthorsWizard);
		getWindowControl().pushAsModalDialog(importAuthorsWizard.getInitialComponent());
	}
	
	private void addAuthors(StepsRunContext runContext) {
		@SuppressWarnings("unchecked")
		List<QuestionItemShort> items = (List<QuestionItemShort>)runContext.get("items");
		@SuppressWarnings("unchecked")
		List<Identity> authors = (List<Identity>)runContext.get("members");
		qpoolService.addAuthors(authors, items);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<QuestionItemShort> items) {
		confirmDeleteBox = activateYesNoDialog(ureq, null, translate("confirm.delete"), confirmDeleteBox);
		confirmDeleteBox.setUserObject(items);
		confirmDeleteBox.setContextHelp("org.olat.modules.qpool.ui", "delete-item.html", "help.hover.deleteitem");
	}
	
	private void doDelete(List<QuestionItemShort> items) {
		qpoolService.deleteItems(items);
		getItemsTable().reset();
		showInfo("item.deleted");
	}
	
	protected void doShare(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(shareTargetCtrl);
		shareTargetCtrl = new ShareTargetController(ureq, getWindowControl());
		listenTo(shareTargetCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), shareTargetCtrl.getInitialComponent(), shareItem, title, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();	
	}
	
	private void doConfirmRemove(UserRequest ureq, List<QuestionItemShort> items) {
		String text = translate("confirm.unshare", new String[]{ getSource().getName() });
		confirmRemoveBox = activateYesNoDialog(ureq, null, text, confirmRemoveBox);
		confirmRemoveBox.setUserObject(items);
		confirmRemoveBox.setContextHelp("org.olat.modules.qpool.ui", "remove-item.html", "help.hover.removeitem");
	}
	
	protected void doRemove(List<QuestionItemShort> items) {
		getSource().removeFromSource(items);
		getItemsTable().reset();
		showInfo("item.deleted");
	}
	
	private void doOpenRenameCallout(UserRequest ureq) {
		removeAsListenerAndDispose(renameCtrl);
		renameCtrl = new RenameController(ureq, getWindowControl());
		listenTo(renameCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				renameCtrl.getInitialComponent(), true, translate("rename.collection"));
		cmc.activate();	
		listenTo(cmc);
	}
	
	private void doRenameItemCollection(UserRequest ureq, String newName) {
		itemCollection = qpoolService.renameCollection(itemCollection, newName);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_CHANGED, itemCollection.getKey()));
	}

	private void doConfirmDeleteSource(UserRequest ureq) {
		confirmDeleteSourceBox = activateYesNoDialog(ureq, null, translate("confirm.delete.source"), confirmDeleteSourceBox);
	}
	
	private void doDeleteItemCollection(UserRequest ureq) {
		qpoolService.deleteCollection(itemCollection);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_DELETED));
	}
	
	protected void doSelectGroup(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl());
		selectGroupCtrl.setUserObject(items);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("select.group"));
		cmc.setContextHelp(ureq, "org.olat.modules.qpool.ui", "share-group.html", "help.hover.sharegroup");
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
		confirmCopyBox.setContextHelp("org.olat.modules.qpool.ui", "copy-item.html", "help.hover.copyitem");
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
	
	@Override
	protected void doSelect(UserRequest ureq, ItemRow row) {
		QuestionItem item = qpoolService.loadItemById(row.getKey());
		doSelect(ureq, item, row.isEditable());
	}
		
	protected void doSelect(UserRequest ureq, QuestionItem item, boolean editable) {
		removeAsListenerAndDispose(currentDetailsCtrl);
		removeAsListenerAndDispose(currentMainDetailsCtrl);
		
		currentDetailsCtrl = new QuestionItemDetailsController(ureq, getWindowControl(), item, editable, getSource().isDeleteEnabled());
		currentDetailsCtrl.setBreadcrumbPanel(stackPanel);
		listenTo(currentDetailsCtrl);
		currentMainDetailsCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), currentDetailsCtrl);
		listenTo(currentMainDetailsCtrl);
		stackPanel.pushController(item.getTitle(), currentMainDetailsCtrl);
	}
}
