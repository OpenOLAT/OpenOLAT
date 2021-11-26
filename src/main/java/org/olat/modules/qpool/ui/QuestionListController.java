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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.types.ImsQTI21Resource;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.ims.qti.QTIModule;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.ims.qti.qpool.QTIQPoolServiceProvider;
import org.olat.ims.qti.questionimport.ItemAndMetadata;
import org.olat.ims.qti.questionimport.ItemsPackage;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.questionimport.AssessmentItemAndMetadata;
import org.olat.ims.qti21.questionimport.AssessmentItemsPackage;
import org.olat.ims.qti21.questionimport.ImportOptions;
import org.olat.ims.qti21.questionimport.QImport_1_InputStep;
import org.olat.modules.qpool.ExportFormatOptions;
import org.olat.modules.qpool.ExportFormatOptions.Outcome;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QItemList;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.datasource.TaxonomyLevelItemsSource;
import org.olat.modules.qpool.ui.events.ExportFormatSelectionEvent;
import org.olat.modules.qpool.ui.events.QItemCreationCmdEvent;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.events.QItemEvent;
import org.olat.modules.qpool.ui.events.QItemsProcessedEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.modules.qpool.ui.events.QPoolSelectionEvent;
import org.olat.modules.qpool.ui.metadata.MetadataBulkChangeController;
import org.olat.modules.qpool.ui.wizard.Export_1_TypeStep;
import org.olat.modules.qpool.ui.wizard.ImportAuthor_1_ChooseMemberStep;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.CreateEntryController;
import org.olat.repository.ui.author.CreateRepositoryEntryController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This controller wrap the table of qitems and decorate it with
 * features like copy, delete...<br/>
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class QuestionListController extends AbstractItemListController implements Activateable2 {

	private FormLink statusDraftLink;
	private FormLink statusReviewLink;
	private FormLink statusRevisedLink;
	private FormLink statusFinalLink;
	private FormLink statusEndOfLifeLink;
	private FormLink createTest;
	private FormLink list, exportItem, shareItem, removeItem, newItem, copyItem, convertItem, deleteItem, authorItem, importItem, bulkChange;

	private final TooledStackedPanel stackPanel;
	private RenameController renameCtrl;
	private CloseableModalController cmc;
	private CloseableModalController cmcShareItemToSource;
	private DeleteConfirmationController deleteConfirmationCtrl;
	private CopyConfirmationController copyConfirmationCtrl;
	private DialogBoxController confirmRemoveBox;
	private DialogBoxController confirmDeleteSourceBox;
	private CreateTestTargetController createTestTargetCtrl;
	private CreateTestOverviewController createTestOverviewCtrl;
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
	private CreateEntryController addController;
	private QuestionItemDetailsController currentDetailsCtrl;
	private MetadataBulkChangeController bulkChangeCtrl;
	private ImportSourcesController importSourcesCtrl;
	private NewItemOptionsController newItemOptionsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ReferencableEntriesSearchController importTestCtrl;
	private ConversionConfirmationController conversionConfirmationCtrl;
	
	private QuestionItemCollection itemCollection;
	
	private boolean itemCollectionDirty = false;

	@Autowired
	private DB dbInstance;
	@Autowired
	private QTIModule qtiModule;
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;
	
	public QuestionListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QuestionItemsSource source, QPoolSecurityCallback securityCallback, String key,
			boolean searchAllTaxonomyLevels) {
		super(ureq, wControl, securityCallback, source, key, searchAllTaxonomyLevels);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
	}

	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		if (getSource().isStatusFilterEnabled()) {
			QuestionStatus statusFilter = getSource().getStatusFilter();
			statusDraftLink = uifactory.addFormLink("source.status.draft", "source.status.draft", null, formLayout, Link.BUTTON);
			statusDraftLink.setUserObject(QuestionStatus.draft);
			statusDraftLink.setElementCssClass("btn-arrow-right o_qpool_qitem_draft");
			statusDraftLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qitem_draft");
			if (QuestionStatus.draft.equals(statusFilter)) setSelectionCssClass(statusDraftLink);
			statusReviewLink = uifactory.addFormLink("source.status.review", "source.status.review", null, formLayout, Link.BUTTON);
			statusReviewLink.setUserObject(QuestionStatus.review);
			statusReviewLink.setElementCssClass("btn-arrow-right o_qpool_qitem_review");
			statusReviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qitem_review");
			if (QuestionStatus.review.equals(statusFilter)) setSelectionCssClass(statusReviewLink);
			statusRevisedLink = uifactory.addFormLink("source.status.revised", "source.status.revised", null, formLayout, Link.BUTTON);
			statusRevisedLink.setUserObject(QuestionStatus.revised);
			statusRevisedLink.setElementCssClass("btn-arrow-right o_qpool_qitem_revised");
			statusRevisedLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qitem_revised");
			if (QuestionStatus.revised.equals(statusFilter)) setSelectionCssClass(statusRevisedLink);
			statusFinalLink = uifactory.addFormLink("source.status.finalVersion", "source.status.finalVersion", null, formLayout, Link.BUTTON);
			statusFinalLink.setUserObject(QuestionStatus.finalVersion);
			statusFinalLink.setElementCssClass("btn-arrow-right o_qpool_qitem_final");
			statusFinalLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qitem_finalVersion");
			if (QuestionStatus.finalVersion.equals(statusFilter)) setSelectionCssClass(statusFinalLink);
			statusEndOfLifeLink = uifactory.addFormLink("source.status.endOfLife", "source.status.endOfLife", null, formLayout, Link.BUTTON);
			statusEndOfLifeLink.setUserObject(QuestionStatus.endOfLife);
			statusEndOfLifeLink.setElementCssClass("btn-arrow-right o_qpool_qitem_end_of_life");
			statusEndOfLifeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_qitem_endOfLife");
			if (QuestionStatus.endOfLife.equals(statusFilter)) setSelectionCssClass(statusEndOfLifeLink);
			updateStatusFilter();
		}
		
		if(getSource().isCreateEnabled()) {
			newItem = uifactory.addFormLink("new.item", formLayout, Link.BUTTON);
			newItem.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_new");
		}
		importItem = uifactory.addFormLink("import.item", formLayout, Link.BUTTON);
		importItem.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_import");
		
		if (getSecurityCallback().canUseCollections()) {
			list = uifactory.addFormLink("list", formLayout, Link.BUTTON);
		}
		if (getSecurityCallback().canCreateTest()) {
			createTest = uifactory.addFormLink("create.test", formLayout, Link.BUTTON);
		}
		exportItem = uifactory.addFormLink("export.item", formLayout, Link.BUTTON);
		if (getSecurityCallback().canUsePools() || getSecurityCallback().canUseGroups()) {
			shareItem = uifactory.addFormLink("share.item", formLayout, Link.BUTTON);
		}
		if(getSource().isRemoveEnabled()) {
			removeItem = uifactory.addFormLink("unshare.item", formLayout, Link.BUTTON);
		}
		if(getSource().isCopyEnabled()) {
			copyItem = uifactory.addFormLink("copy", formLayout, Link.BUTTON);
		}
		if(getSource().isImportEnabled()) {
			convertItem = uifactory.addFormLink("convert.item", formLayout, Link.BUTTON);
		}
		if(getSource().isAuthorRightsEnable()) {
			authorItem = uifactory.addFormLink("author.item", formLayout, Link.BUTTON);
		}
		if(getSource().isDeleteEnabled()) {
			deleteItem = uifactory.addFormLink("delete.item", formLayout, Link.BUTTON);
		}
		if(getSource().isBulkChangeEnabled()) {
			bulkChange = uifactory.addFormLink("bulk.change", formLayout, Link.BUTTON);
		}
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}

	public QuestionItemCollection getItemCollection() {
		return itemCollection;
	}

	public void setItemCollection(QuestionItemCollection itemCollection) {
		this.itemCollection = itemCollection;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("QuestionItem".equals(type)) {
			Long itemKey = entry.getOLATResourceable().getResourceableId();
			ItemRow row = getModel().getObjectByKey(itemKey);
			if(row == null) {
				getModel().load(null, null, null, 0, -1);
				row = getModel().getObjectByKey(itemKey);
				if(row != null) {
					doOpenDetails(ureq, row);
					int index = getModel().getObjects().indexOf(row);
					if(index >= 1 && getItemsTable().getPageSize() > 1) {
						int page = index / getItemsTable().getPageSize();
						getItemsTable().setPage(page);
					}
				}
			} else {
				doOpenDetails(ureq, row);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if(link == list) {
				doList(ureq);
			} else if (link == createTest) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(!items.isEmpty()) {
					doCreateTest(ureq, items);
				} else {
					showWarning("error.select.one.create.test");
				}
			} else if(link == exportItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(!items.isEmpty()) {
					doExport(ureq, items);
				} else {
					showWarning("error.select.one.export");
				}
			} else if(link == shareItem) {
				doShare(ureq);
			} else if(link == removeItem) {
				List<QuestionItemShort> items = getRemovableItems();
				if(!items.isEmpty()) {
					doConfirmRemove(ureq, items);
				} else {
					showWarning("error.select.one.remove");
				}
			} else if(link == copyItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(!items.isEmpty()) {
					doConfirmCopy(ureq, items);
				} else {
					showWarning("error.select.one.copy");
				}
			} else if(link == convertItem) {
				List<QuestionItemShort> items = getSelectedShortItems();
				if(!items.isEmpty()) {
					doConfirmConversion(ureq, items);
				} else {
					showWarning("error.select.one.convert");
				}
			} else if(link == deleteItem) {
				List<QuestionItemShort> items = getDeletableItems();
				if(!items.isEmpty()) {
					doConfirmDelete(ureq, items);
				} else {
					noItemsToDeleteWarning();
				}
			} else if(link == authorItem) {
				List<QuestionItemShort> items = getAuthorsEditableItems();
				if(!items.isEmpty()) {
					doChooseAuthors(ureq, items);
				} else {
					showWarning("error.select.one.author");
				}
			} else if(link == importItem) {
				doOpenImport(ureq);
			} else if(link == newItem) {
				doChooseNewItemType(ureq);
			} else if(link == bulkChange) {
				List<ItemRow> items = getMetadataEditableItems();
				if(!items.isEmpty()) {
					doBulkChange(ureq, items);
				} else {
					showWarning("error.select.one.metadata");
				}
			} else if (link == statusDraftLink || link == statusReviewLink || link == statusRevisedLink || link == statusFinalLink || link == statusEndOfLifeLink) {
				doSetSourceStatus(link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<QuestionItemShort> getRemovableItems() {
		return getItemsTable().getMultiSelectedIndex().stream()
				.map(index -> getModel().getObject(index.intValue()))
				.filter(itemRow -> itemRow != null && itemRow.getSecurityCallback().canRemove())
				.collect(Collectors.toList());
	}

	private List<QuestionItemShort> getDeletableItems() {
		return getItemsTable().getMultiSelectedIndex().stream()
				.map(index -> getModel().getObject(index.intValue()))
				.filter(itemRow -> itemRow != null && itemRow.getSecurityCallback().canDelete())
				.collect(Collectors.toList());
	}
	
	private List<QuestionItemShort> getAuthorsEditableItems() {
		return getItemsTable().getMultiSelectedIndex().stream()
				.map(index -> getModel().getObject(index.intValue()))
				.filter(itemRow -> itemRow != null && itemRow.getSecurityCallback().canEditAuthors())
				.collect(Collectors.toList());
	}
	
	private List<ItemRow> getMetadataEditableItems() {
		return getItemsTable().getMultiSelectedIndex().stream()
				.map(index -> getModel().getObject(index.intValue()))
				.filter(itemRow -> itemRow != null && itemRow.getSecurityCallback().canEditMetadata())
				.collect(Collectors.toList());
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == stackPanel) {
			if(itemCollectionDirty && event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				Controller mainCtrl = pe.getController();
				if(mainCtrl != null && mainCtrl.isControllerListeningTo(this)) {
					reloadData();
					updateStatusFilter();
					fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEMS_RELOADED));
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
			List<QuestionItemShort> items = getSelectedShortItems();
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
					doShareItemsToPools(ureq, items, pools);
				}
			}	
		} else if(source == importSourcesCtrl) {
			calloutCtrl.deactivate();
			if(ImportSourcesController.IMPORT_FILE.equals(event.getCommand())) {
				doOpenFileImport(ureq);
			} else if(ImportSourcesController.IMPORT_REPO.equals(event.getCommand())) {
				doOpenRepositoryImport(ureq);
			} else if(ImportSourcesController.IMPORT_EXCEL_QTI_12.equals(event.getCommand())) {
				doOpenExcelImportQTI12(ureq);
			} else if(ImportSourcesController.IMPORT_EXCEL_QTI_21.equals(event.getCommand())) {
				doOpenExcelImportQTI21(ureq);
			}
		} else if(source == newItemOptionsCtrl) {
			cmc.deactivate();
			if(event instanceof QItemCreationCmdEvent) {
				QItemCreationCmdEvent qicce = (QItemCreationCmdEvent)event;
				doCreateNewItem(ureq, qicce.getTitle(), qicce.getTaxonomyLevel(), qicce.getFactory());
			}
		} else if(source == selectGroupCtrl) {
			cmc.deactivate();
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					@SuppressWarnings("unchecked")
					List<QuestionItemShort> items = (List<QuestionItemShort>)selectGroupCtrl.getUserObject();
					doShareItemsToGroups(ureq, items, groups);
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
		} else if(source == createTestTargetCtrl && event instanceof ExportFormatSelectionEvent) {
			calloutCtrl.deactivate();
			ExportFormatSelectionEvent efsEvent = (ExportFormatSelectionEvent) event;
			ExportFormatOptions format = efsEvent.getFormat();
			List<QuestionItemShort> items = getSelectedShortItems();
			doShowCreateTestOverview(ureq, items, format);
		} else if (source == createTestOverviewCtrl) {
			List<QuestionItemShort> items = createTestOverviewCtrl.getExportableQuestionItems();
			String typeFormat = createTestOverviewCtrl.getResourceTypeFormat();
			LicenseType licenseType = createTestOverviewCtrl.getLicenseType();
			boolean groupBy = createTestOverviewCtrl.isGroupByTaxonomyLevel();
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				doOpenCreateRepositoryTest(ureq, items, typeFormat, licenseType, groupBy);
			}
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
		} else if(source == copyConfirmationCtrl) {
			doPostCopy(ureq, event);
			cmc.deactivate();
			cleanUp();
		} else if(source == conversionConfirmationCtrl) {
			doPostConvert(ureq, event);
			cmc.deactivate();
			cleanUp();
		} else if(source == deleteConfirmationCtrl) {
			if (event == Event.DONE_EVENT) {
				List<QuestionItemShort> items = deleteConfirmationCtrl.getItemsToDelete();
				doDelete(items);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmRemoveBox) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<QuestionItemShort> items = (List<QuestionItemShort>)confirmRemoveBox.getUserObject();
				doRemove(items);
			}
		} else if(source == currentDetailsCtrl) {
			if(event instanceof QItemEvent) {
				QItemEvent qce = (QItemEvent)event;
				if("copy-item".equals(qce.getCommand()) || "convert-item".equals(qce.getCommand())) {
					stackPanel.popUpToRootController(ureq);
					doOpenDetails(ureq, qce.getItem());
				} else if("previous".equals(qce.getCommand())) {
					doPrevious(ureq, qce.getItem());
				} else if("next".equals(qce.getCommand())) {
					doNext(ureq, qce.getItem());
				}
			} else if(event instanceof QItemEdited) {
				String title = ((QItemEdited) event).getItem().getTitle();
				stackPanel.changeDisplayname(title, null, currentDetailsCtrl);
				itemCollectionDirty = true;
			} else if (event instanceof QPoolEvent) {
				QPoolEvent qce = (QPoolEvent)event;
				if(QPoolEvent.ITEM_DELETED.equals(qce.getCommand())) {
					fireEvent(ureq, qce);
				} else if (QPoolEvent.ITEM_STATUS_CHANGED.equals(qce.getCommand())) {
					itemCollectionDirty = true;
				}
			}
		} else if(source == addController) {
			if(event instanceof EntryChangedEvent) {
				cmc.deactivate();
				cleanUp();
				
				EntryChangedEvent addEvent = (EntryChangedEvent)event;
				Long repoEntryKey = addEvent.getRepositoryEntryKey();
				doCreateRepositoryTest(ureq, repoEntryKey);
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
		removeAsListenerAndDispose(createTestOverviewCtrl);
		removeAsListenerAndDispose(bulkChangeCtrl);
		removeAsListenerAndDispose(importItemCtrl);
		removeAsListenerAndDispose(importTestCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(createCollectionCtrl);
		removeAsListenerAndDispose(conversionConfirmationCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(copyConfirmationCtrl);
		cmc = null;
		addController = null;
		createTestOverviewCtrl = null;
		bulkChangeCtrl = null;
		importItemCtrl = null;
		importTestCtrl = null;
		selectGroupCtrl = null;
		createCollectionCtrl = null;
		conversionConfirmationCtrl = null;
		deleteConfirmationCtrl = null;
		copyConfirmationCtrl = null;
	}
	
	protected void updateRows(List<QuestionItem> items) {
		List<Integer> rowIndex = getIndex(items);
		getModel().reload(rowIndex);
		getItemsTable().getComponent().setDirty(true);
	}

	private void doNext(UserRequest ureq, QuestionItem item) {
		ItemRow row = getRowByItemKey(item.getKey());
		ItemRow nextRow = getModel().getNextObject(row, getItemsTable());
		doSelectOrReset(ureq, nextRow);
	}
	
	private void doPrevious(UserRequest ureq, QuestionItem item) {
		ItemRow row = getRowByItemKey(item.getKey());
		ItemRow previousRow = getModel().getPreviousObject(row, getItemsTable());
		doSelectOrReset(ureq, previousRow);
	}
	
	private void doSelectOrReset(UserRequest ureq, ItemRow row) {
		if(row != null) {
			stackPanel.popUpToRootController(ureq);
			doOpenDetails(ureq, row);
		} else {
			getItemsTable().reset(true, true, true);
		}
	}

	private void doSetSourceStatus(FormLink link) {
		if (getSource().isStatusFilterEnabled()) {
			QuestionStatus status = (QuestionStatus) link.getUserObject();
			getSource().setStatusFilter(status);
			getItemsTable().reset(true, true, true);
			removeSelectionCssClass(statusDraftLink);
			removeSelectionCssClass(statusReviewLink);
			removeSelectionCssClass(statusRevisedLink);
			removeSelectionCssClass(statusFinalLink);
			removeSelectionCssClass(statusEndOfLifeLink);
			setSelectionCssClass(link);
		}
	}

	private void removeSelectionCssClass(FormLink link) {
		String cssClass = link.getElementCssClass();
		if (cssClass.contains(" o_qpool_status_slected")) {
			cssClass = cssClass.replace(" o_qpool_status_slected", "");
			link.setElementCssClass(cssClass);
			link.getComponent().setDirty(true);
		}
	}

	public void updateStatusFilter() {
		if (getSource().isStatusFilterEnabled()) {
			QuestionStatus actualStatus = getSource().getStatusFilter();
			reloadStatusFilterTitle(statusDraftLink, "source.status.draft");
			reloadStatusFilterTitle(statusReviewLink, "source.status.review");
			reloadStatusFilterTitle(statusRevisedLink, "source.status.revised");
			reloadStatusFilterTitle(statusFinalLink, "source.status.finalVersion");
			reloadStatusFilterTitle(statusEndOfLifeLink, "source.status.endOfLife");
			getSource().setStatusFilter(actualStatus);
		}
	}

	private void reloadStatusFilterTitle(FormLink link, String i18n) {
		QuestionStatus linkStatus = (QuestionStatus) link.getUserObject();
		getSource().setStatusFilter(linkStatus);
		int numItems = getSource().getNumOfItems(false);
		link.setI18nKey(i18n, new String[] {Integer.toString(numItems)});
		link.getComponent().setDirty(true);
	}

	private void setSelectionCssClass(FormLink link) {
		String cssClass = link.getElementCssClass();
		link.setElementCssClass(cssClass + " o_qpool_status_slected");
		link.getComponent().setDirty(true);
	}

	private void doBulkChange(UserRequest ureq, List<ItemRow> items) {
		removeAsListenerAndDispose(bulkChangeCtrl);
		bulkChangeCtrl = new MetadataBulkChangeController(ureq, getWindowControl(), getSecurityCallback(), items,
				getSource().isAdminItemSource());
		listenTo(bulkChangeCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				bulkChangeCtrl.getInitialComponent(), true, translate("bulk.change"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doChooseNewItemType(UserRequest ureq) {
		removeAsListenerAndDispose(newItemOptionsCtrl);
		if (getSource() instanceof TaxonomyLevelItemsSource) {
			TaxonomyLevelItemsSource tliSource = (TaxonomyLevelItemsSource) getSource();
			newItemOptionsCtrl = new NewItemOptionsController(ureq, getWindowControl(), getSecurityCallback(),
					tliSource.getTaxonomyLevel(), getSource().isAdminItemSource());
		} else {
			newItemOptionsCtrl = new NewItemOptionsController(ureq, getWindowControl(), getSecurityCallback(),
					getSource().isAdminItemSource());
		}
		listenTo(newItemOptionsCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				newItemOptionsCtrl.getInitialComponent(), true, translate("new.item"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doCreateNewItem(UserRequest ureq, String title, TaxonomyLevel taxonomyLevel, QItemFactory factory) {
		QuestionItem item = factory.createItem(getIdentity(), title, getLocale());
		List<QuestionItem> newItems = Collections.singletonList(item);
		getSource().postImport(newItems, false);
		if (taxonomyLevel != null && item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl) item;
			itemImpl.setTaxonomyLevel(taxonomyLevel);
			qpoolService.updateItem(itemImpl);
		}
		if (licenseModule.isEnabled(licenseHandler)) {
			// The QItemFactory may create a no license as part of the import process.
			// But for new question items the default license should be created.
			// So delete the no license first, so that the default license can be created.
			licenseService.delete(item);
			licenseService.createDefaultLicense(item, licenseHandler, getIdentity());
		}
		getItemsTable().reset();
		
		dbInstance.commit();
		qpoolService.index(newItems);
		
		QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
				Action.CREATE_QUESTION_ITEM_NEW);
		builder.withAfter(item);
		qpoolService.persist(builder.create());
		
		QPoolEvent qce = new QPoolEvent(QPoolEvent.ITEM_CREATED);
		fireEvent(ureq, qce);
		doOpenDetails(ureq, item);
	}
	
	private void doOpenImport(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(importSourcesCtrl);
		
		importSourcesCtrl = new ImportSourcesController(ureq, getWindowControl());
		listenTo(importSourcesCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), importSourcesCtrl.getInitialComponent(), importItem, null, true, null);
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
		
		List<QuestionItem> importItems = null;
		if(ImsQTI21Resource.TYPE_NAME.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
			QTI21QPoolServiceProvider spi = CoreSpringFactory.getImpl(QTI21QPoolServiceProvider.class);
			importItems = spi.importRepositoryEntry(getIdentity(), repositoryEntry, getLocale());
		} else {
			QTIQPoolServiceProvider spi
				= (QTIQPoolServiceProvider)CoreSpringFactory.getBean("qtiPoolServiceProvider");
			importItems = spi.importRepositoryEntry(getIdentity(), repositoryEntry, getLocale());
		}
		for (QuestionItem item: importItems) {
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.CREATE_QUESTION_ITEM_BY_IMPORT);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
		}

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
			qpoolService.index(importItems);
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
		String[] allowed;
		if(qtiModule.isCreateResourcesEnabled()) {
			allowed = new String[]{ ImsQTI21Resource.TYPE_NAME, TestFileResource.TYPE_NAME, SurveyFileResource.TYPE_NAME };
		} else {
			allowed = new String[]{ ImsQTI21Resource.TYPE_NAME };
		}
		importTestCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, allowed,
				null, null, translate("import.repository"), false, false, false, false, true, false, Can.copyable);
		listenTo(importTestCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				importTestCtrl.getInitialComponent(), true, translate("import.repository"));
		cmc.setContextHelp(getTranslator(),"Data Management#qb_share");
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenExcelImportQTI12(UserRequest ureq) {
		removeAsListenerAndDispose(excelImportWizard);
		
		final ItemsPackage importPackage = new ItemsPackage();
		Step additionalStep = null;
		if(getSource().askEditable()) {
			additionalStep = new EditableStep(ureq);
		}
		
		final org.olat.ims.qti.questionimport.ImportOptions options = new org.olat.ims.qti.questionimport.ImportOptions();
		options.setShuffle(true);
		Step start = new org.olat.ims.qti.questionimport.QImport_1_InputStep(ureq, importPackage, options, additionalStep);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				List<ItemAndMetadata> itemsToImport = importPackage.getItems();
				QTIQPoolServiceProvider spi = CoreSpringFactory.getImpl (QTIQPoolServiceProvider.class);
				List<QuestionItem> importItems = spi.importBeecomItem(getIdentity(), itemsToImport, getLocale());
				for (QuestionItem item: importItems) {
					QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
							Action.CREATE_QUESTION_ITEM_BY_IMPORT);
					builder.withAfter(item);
					qpoolService.persist(builder.create());
				}
				
				boolean editable = true;
				if(getSource().askEditable()) {
					Object editableCtx = runContext.get("editable");
					editable = (editableCtx instanceof Boolean) ? ((Boolean)editableCtx).booleanValue() : false;
				}
				qpoolService.index(importItems);
				int postImported = getSource().postImport(importItems, editable);
				if(postImported > 0) {
					getItemsTable().reset();
				}
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		excelImportWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.excellike.12"), "o_sel_qpool_excel_import_wizard");
		listenTo(excelImportWizard);
		getWindowControl().pushAsModalDialog(excelImportWizard.getInitialComponent());
	}
	
	private void doOpenExcelImportQTI21(UserRequest ureq) {
		removeAsListenerAndDispose(excelImportWizard);
		
		Step additionalStep = null;
		if(getSource().askEditable()) {
			additionalStep = new EditableStep(ureq);
		}
		
		final AssessmentItemsPackage importPackage = new AssessmentItemsPackage();
		final ImportOptions options = new ImportOptions();
		options.setShuffle(true);

		Step start = new QImport_1_InputStep(ureq, importPackage, options, additionalStep);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				QTI21QPoolServiceProvider spi = CoreSpringFactory.getImpl(QTI21QPoolServiceProvider.class);
				List<AssessmentItemAndMetadata> items = importPackage.getItems();
				List<QuestionItem> importItems = new ArrayList<>();
				for(AssessmentItemAndMetadata item:items) {
					QuestionItem importedItem = spi.importExcelItem(getIdentity(), item, getLocale());
					if(importedItem != null) {
						importItems.add(importedItem);
					}
				}
				for (QuestionItem item: importItems) {
					QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
							Action.CREATE_QUESTION_ITEM_BY_IMPORT);
					builder.withAfter(item);
					qpoolService.persist(builder.create());
				}

				boolean editable = true;
				if(getSource().askEditable()) {
					Object editableCtx = runContext.get("editable");
					editable = editableCtx instanceof Boolean && ((Boolean)editableCtx).booleanValue();
				}
				int postImported = getSource().postImport(importItems, editable);
				if(postImported > 0) {
					getItemsTable().reset();
				}
				dbInstance.commit();
				qpoolService.index(importItems);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		excelImportWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.excellike.21"), "o_sel_qpool_excel_import_wizard");
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

	private void doCreateTest(UserRequest ureq, List<QuestionItemShort> items) {
		Set<ExportFormatOptions> exportFormatOptions = qpoolService.getExportFormatOptions(items, Outcome.repository);
		if (exportFormatOptions.size() > 1) {
			doChooseTestFormat(ureq, exportFormatOptions);
		} else if (exportFormatOptions.size() == 1) {
			doShowCreateTestOverview(ureq, items, exportFormatOptions.iterator().next());
		} else {
			showWarning("create.test.no.formats");
		}
	}
	
	private void doChooseTestFormat(UserRequest ureq, Set<ExportFormatOptions> exportFormatOptions) {
		removeAsListenerAndDispose(createTestTargetCtrl);
		createTestTargetCtrl = new CreateTestTargetController(ureq, getWindowControl(), exportFormatOptions);
		listenTo(createTestTargetCtrl);
		
		removeAsListenerAndDispose(calloutCtrl);
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), createTestTargetCtrl.getInitialComponent(), createTest, null, true, null);
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}

	private void doShowCreateTestOverview(UserRequest ureq, List<QuestionItemShort> items, ExportFormatOptions format) {
		removeAsListenerAndDispose(createTestOverviewCtrl);
		createTestOverviewCtrl = new CreateTestOverviewController(ureq, getWindowControl(), items, format, getSecurityCallback());
		listenTo(createTestOverviewCtrl);
		
		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				createTestOverviewCtrl.getInitialComponent(), true, translate("create.test"));
		cmc.activate();	
		listenTo(cmc);
	}
	
	private void doOpenCreateRepositoryTest(UserRequest ureq, List<QuestionItemShort> items, String type, LicenseType licenseType, boolean groupBy) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(addController);

		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(type);
		addController = new CreateRepositoryEntryController(ureq, getWindowControl(), handler, false);
		addController.setCreateObject(new QItemList(items, groupBy));
		addController.setLicenseType(licenseType);
		listenTo(addController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCreateRepositoryTest(UserRequest ureq, Long repoEntryKey) {
		RepositoryEntry re = repositoryManager.lookupRepositoryEntry(repoEntryKey, false);
		if(re != null) {
			WindowControl wControl = BusinessControlFactory.getInstance()
					.createBusinessWindowControl(getWindowControl(), re, OresHelper.createOLATResourceableType("Editor"));
			NewControllerFactory.getInstance().launch(ureq, wControl);
		}
	}

	private void doExport(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(exportWizard);
		Step start = new Export_1_TypeStep(ureq, items);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> StepsMainRunController.DONE_MODIFIED;
		
		exportWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("export.item"), "o_sel_qpool_export_1_wizard");
		listenTo(exportWizard);
		getWindowControl().pushAsModalDialog(exportWizard.getInitialComponent());
	}
	
	private void doExecuteExport(UserRequest ureq, StepsRunContext runContext) {
		ExportFormatOptions format = (ExportFormatOptions)runContext.get("format");
		@SuppressWarnings("unchecked")
		List<QuestionItemShort> items = (List<QuestionItemShort>)runContext.get("itemsToExport");
		MediaResource mr = qpoolService.export(items, format, getLocale());
		if(mr != null) {
			ureq.getDispatchResult().setResultingMediaResource(mr);
		}
	}
	
	private void doCreateCollection(UserRequest ureq, String name, List<QuestionItemShort> items) {
		QuestionItemCollection coll = qpoolService.createCollection(getIdentity(), name, items);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_CREATED, coll.getKey()));
	}
	
	private void doChooseAuthors(UserRequest ureq, List<QuestionItemShort> items) {
		removeAsListenerAndDispose(importAuthorsWizard);

		Step start = new ImportAuthor_1_ChooseMemberStep(ureq, items);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			addAuthors(runContext);
			return StepsMainRunController.DONE_MODIFIED;
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
		deleteConfirmationCtrl = new DeleteConfirmationController(ureq, getWindowControl(), items);
		listenTo(deleteConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("confirm.delete.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void noItemsToDeleteWarning() {
		if(getItemsTable().getMultiSelectedIndex().isEmpty()) {
			showWarning("error.select.one.delete");
		} else if(qpoolModule.isReviewProcessEnabled() && getItemsTable().getMultiSelectedIndex().stream()
				.map(index -> getModel().getObject(index.intValue()))
				.filter(itemRow -> itemRow != null && !itemRow.getSecurityCallback().isDeletableQuestionStatus())
				.count() > 0) {
			showWarning("error.select.one.delete.review");
		} else {
			showWarning("error.select.one.delete.owner");
		}
	}
	
	private void doDelete(List<QuestionItemShort> items) {
		for (QuestionItemShort item: items) {
			QuestionItem qitem = qpoolService.loadItemById(item.getKey());
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.DELETE_QUESTION_ITEM);
			builder.withBefore(qitem);
			qpoolService.persist(builder.create());
		}
		qpoolService.deleteItems(items);
		getItemsTable().reset(true, true, true);
		showInfo("item.deleted");
	}
	
	protected void doShare(UserRequest ureq) {
		String title = translate("filter.view");
		removeAsListenerAndDispose(shareTargetCtrl);
		shareTargetCtrl = new ShareTargetController(ureq, getWindowControl(), getSecurityCallback());
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
		confirmRemoveBox.setContextHelp("Data Management#qb_remove");
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
		cmc.setContextHelp(getTranslator(), "Data Management#qb_share");
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
		copyConfirmationCtrl = new CopyConfirmationController(ureq, getWindowControl(), items, getSource());
		listenTo(copyConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				copyConfirmationCtrl.getInitialComponent(), true, translate("confirm.copy.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doPostCopy(UserRequest ureq, Event event) {
		if (event instanceof QItemsProcessedEvent) {
			QItemsProcessedEvent ipEvent = (QItemsProcessedEvent) event;
			int numberOfCopies = ipEvent.getNumberOfItems();
			showInfo("item.copied", Integer.toString(numberOfCopies));
			getItemsTable().reset();
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		}
	}
	
	private void doConfirmConversion(UserRequest ureq, List<QuestionItemShort> items) {
		Map<String,List<QuestionItemShort>> formatToItems = new HashMap<>();
		List<QPoolSPI> spies = qpoolModule.getQuestionPoolProviders();
		for(QuestionItemShort item:items) {
			for(QPoolSPI sp:spies) {
				if(sp != null && sp.isConversionPossible(item)) {
					List<QuestionItemShort> convertItems;
					if(formatToItems.containsKey(sp.getFormat())) {
						convertItems = formatToItems.get(sp.getFormat());
					} else {
						convertItems = new ArrayList<>(items.size());
						formatToItems.put(sp.getFormat(), convertItems);
					}
					convertItems.add(item);	
				}
			}
		}
		
		if(formatToItems.isEmpty()) {
			showWarning("convert.item.not.possible");
		} else {
			conversionConfirmationCtrl = new ConversionConfirmationController(ureq, getWindowControl(), formatToItems,
					getSource());
			listenTo(conversionConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					conversionConfirmationCtrl.getInitialComponent(), true, translate("convert.item"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doPostConvert(UserRequest ureq, Event event) {
		if (event instanceof QItemsProcessedEvent) {
			QItemsProcessedEvent ipEvent = (QItemsProcessedEvent) event;
			int numberOfCopies = ipEvent.getNumberOfItems();
			int numberOfFails = ipEvent.getNumberOfFails();
			if(numberOfFails == 0) {
				showInfo("convert.item.successful", new String[]{ Integer.toString(numberOfCopies)} );
			} else {
				showWarning("convert.item.warning", new String[]{ Integer.toString(numberOfFails), Integer.toString(numberOfCopies) } );
			}
			
			getItemsTable().reset();
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		}
	}
	
	private void doShareItemsToGroups(UserRequest ureq, List<QuestionItemShort> items, List<BusinessGroup> groups) {
		removeAsListenerAndDispose(shareItemsCtrl);
		shareItemsCtrl = new ShareItemOptionController(ureq, getWindowControl(), items, groups, null);
		listenTo(shareItemsCtrl);
		
		String title;
		if (groups != null && groups.size() == 1) {
			title = translate("share.item.group", new String[] {groups.get(0).getName()});
		} else {
			title = translate("share.item.groups");
		}
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				shareItemsCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);	
	}
	
	private void doShareItemsToPools(UserRequest ureq, List<QuestionItemShort> items, List<Pool> pools) {
		removeAsListenerAndDispose(shareItemsCtrl);
		shareItemsCtrl = new ShareItemOptionController(ureq, getWindowControl(), items, null, pools);
		listenTo(shareItemsCtrl);
		
		String title;
		if (pools != null && pools.size() == 1) {
			title = translate("share.item.pool", new String[] {pools.get(0).getName()});
		} else {
			title = translate("share.item.pools");
		}
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				shareItemsCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);	
	}
	
	@Override
	protected void doSelect(UserRequest ureq, ItemRow row) {
		doOpenDetails(ureq, row);
	}

	protected void doOpenDetails(UserRequest ureq, ItemRow row) {
		Integer index = getIndex(row.getKey());
		int count = getModel().getRowCount();
		doOpenDetails(ureq, row, index, count);
	}

	private void doOpenDetails(UserRequest ureq, QuestionItem newQuestionItem) {
		ItemRow row = wrapNewItem(newQuestionItem);
		itemCollectionDirty = true;
		doOpenDetails(ureq, row, 0, 1);
	}

	private void doOpenDetails(UserRequest ureq, ItemRow row, int index, int count) {
		removeAsListenerAndDispose(currentDetailsCtrl);
		
		QuestionItem item = qpoolService.loadItemById(row.getKey());
		if(item == null) {
			showWarning("warning.item.deleted");
		} else {
			WindowControl bwControl = addToHistory(ureq, item, null);
			currentDetailsCtrl = new QuestionItemDetailsController(ureq, bwControl, stackPanel, getSecurityCallback(), item,
					row.getSecurityCallback(), getSource(), index, count);
			listenTo(currentDetailsCtrl);
			stackPanel.pushController(item.getTitle(), currentDetailsCtrl);
		}
	}
	
}
