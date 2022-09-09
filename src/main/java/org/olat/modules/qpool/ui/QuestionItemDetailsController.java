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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.prefs.Preferences;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolItemEditorController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.ExportQItemResource;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.events.QItemEvent;
import org.olat.modules.qpool.ui.events.QItemReviewEvent;
import org.olat.modules.qpool.ui.events.QItemsProcessedEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.modules.qpool.ui.events.QPoolSelectionEvent;
import org.olat.modules.qpool.ui.metadata.MetadatasController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemDetailsController extends BasicController implements TooledController {
	
	private static final String GUIPREF_KEY_SHOW_METADATAS = "show.metadatas";

	private Link statusDraftLink;
	private Link statusReviewLink;
	private Link statusFinalLink;
	private Link statusEndOfLifeLink;
	private Link statusRevisedLink;
	private Link deleteLink;
	private Link nextItemLink;
	private Link numberItemsLink;
	private Link previousItemLink;
	private Link exportLogLink;
	private Link showMetadataLink;
	private Link hideMetadataLink;
	private Link shareGroupItemLink;
	private Link sharePoolItemLink;
	private Link exportItemLink;
	private Link copyItemLink;
	private Link convertItemLink;
	private Link convertItemButton;

	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private Controller questionCtrl;
	private MetadatasController metadatasCtrl;
	private ReviewActionController reviewActionCtrl;
	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private CommentAndRatingSecurityCallback commentAndRatingSecurityCallback;
	private CloseableModalController cmc;
	private ReviewStartController reviewStartCtrl;	
	private ReviewController reviewCtrl;
	private DialogBoxController confirmEndOfLifeCtrl;
	private CopyConfirmationController copyConfirmationCtrl;
	private ConversionConfirmationController conversionConfirmationCtrl;
	private DeleteConfirmationController deleteConfirmationCtrl;
	private SelectBusinessGroupController selectGroupCtrl;
	private PoolsController selectPoolCtrl;
	private ShareItemOptionController shareItemsCtrl;

	private final QPoolSecurityCallback qPoolSecurityCallback;
	private final QuestionItemsSource itemSource;
	private final QuestionItemSecurityCallback qItemSecurityCallback;
	private final Integer itemIndex;
	private final int numberOfItems;
	private Boolean showMetadatas;
	private LockResult lock;
	private boolean valid = true;
	private boolean questionEdited = false;
	
	@Autowired
	private QuestionPoolModule poolModule;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private UserManager userManager;
	
	public QuestionItemDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QPoolSecurityCallback qPoolSecurityCallback, QuestionItem item,
			QuestionItemSecurityCallback qItemSecurityCallback, QuestionItemsSource itemSource, Integer itemIndex,
			int numberOfItems) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.qPoolSecurityCallback = qPoolSecurityCallback;
		this.qItemSecurityCallback = qItemSecurityCallback;
		this.itemIndex = itemIndex;
		this.numberOfItems = numberOfItems;
		this.itemSource = itemSource;
		lock = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(item, getIdentity(), null, getWindow());
		
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		showMetadatas = (Boolean) guiPrefs.get(QuestionItemDetailsController.class, GUIPREF_KEY_SHOW_METADATAS);
		
		mainVC = createVelocityContainer("item_details");
		setMetadatasController(ureq, item, qItemSecurityCallback);
		setReviewActionController(ureq);
		setQuestionController(ureq, item, qItemSecurityCallback);
		setCommentsController(ureq);
		putInitialPanel(mainVC);
	}

	private void setMetadatasController(UserRequest ureq, QuestionItem item, QuestionItemSecurityCallback securityCallback) {
		metadatasCtrl = new MetadatasController(ureq, getWindowControl(), qPoolSecurityCallback, item, securityCallback,
				itemSource.isAdminItemSource(), false);
		mainVC.put("metadatas", metadatasCtrl.getInitialComponent());
		listenTo(metadatasCtrl);
	}

	private void setReviewActionController(UserRequest ureq) {
		reviewActionCtrl = new ReviewActionController(ureq, getWindowControl(), qItemSecurityCallback, lock);
		mainVC.put("review", reviewActionCtrl.getInitialComponent());
		listenTo(reviewActionCtrl);
	}

	private void setQuestionController(UserRequest ureq, QuestionItem item,
			QuestionItemSecurityCallback securityCallback) {
		removeAsListenerAndDispose(questionCtrl);
		questionCtrl = null;
	
		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		boolean canEditContent = securityCallback.canEditQuestion() && (spi != null && spi.isTypeEditable());
		if (canEditContent && !lock.isSuccess()) {
			canEditContent = false;
			String displayName = "???";
			if (lock.getOwner() != null) {
				displayName = userManager.getUserDisplayName(lock.getOwner());
			}
			String i18nMsg = lock.isDifferentWindows() ? "locked.readonly.same.user" : "locked.readonly";
			showWarning(i18nMsg, new String[] {displayName});
		}
		
		
		if (spi != null) {
			if (canEditContent) {
				QPoolItemEditorController editQuestionCtrl = spi.getEditableController(ureq, getWindowControl(), item);
				valid = editQuestionCtrl.isValid();
				questionCtrl = editQuestionCtrl;
			} else {
				questionCtrl = spi.getReadOnlyController(ureq, getWindowControl(), item);
			}
		}
		if (questionCtrl == null && spi != null) {
			questionCtrl = spi.getPreviewController(ureq, getWindowControl(), item, false);
		}
		if (questionCtrl == null) {
			questionCtrl = new QuestionItemRawController(ureq, getWindowControl());
		}
		listenTo(questionCtrl);

		if(mainVC != null) {
			if(valid) {
				mainVC.put("type_specifics", questionCtrl.getInitialComponent());
			} else {
				mainVC.contextPut("corrupted", Boolean.TRUE);
			}
		}
	}

	private void setCommentsController(UserRequest ureq) {		
		Roles roles = ureq.getUserSession().getRoles();
		boolean moderator = roles.isAdministrator() || roles.isPoolManager();
		boolean anonymous = roles.isGuestOnly() || roles.isInvitee();
		commentAndRatingSecurityCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), moderator, anonymous);
		removeAsListenerAndDispose(commentsAndRatingCtr);
		commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), metadatasCtrl.getItem(),
				null, commentAndRatingSecurityCallback, null, true, qItemSecurityCallback.canRate(), true);
		listenTo(commentsAndRatingCtr);
		mainVC.put("comments", commentsAndRatingCtr.getInitialComponent());
	}
	
	@Override
	public void initTools() {
		stackPanel.removeAllTools();
		initCommandTools();
		initShareTools();
		initStatusTools(); 
		initPrevNextTools();
		initMetadataTools();
	}

	private void initCommandTools() {
		Dropdown commandDropdown = new Dropdown("commands", "commands", false, getTranslator());
		commandDropdown.setIconCSS("o_icon o_icon-fw o_icon_qitem_commands");
		commandDropdown.setOrientation(DropdownOrientation.normal);
		stackPanel.addTool(commandDropdown, Align.left);
		
		copyItemLink = LinkFactory.createToolLink("copy", translate("copy"), this);
		copyItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_copy");
		commandDropdown.addComponent(copyItemLink);
		
		if ("IMS QTI 1.2".equals(metadatasCtrl.getItem().getFormat()) && valid) {
			if(availableConversionFormats(metadatasCtrl.getItem()).isEmpty()) {
				mainVC.contextPut("deprecatedForm", Boolean.TRUE);
			} else {
				convertItemLink = LinkFactory.createToolLink("convert", translate("convert.item"), this);
				convertItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_convert");
				commandDropdown.addComponent(convertItemLink);
				
				convertItemButton = LinkFactory.createButton("convert.item.long", mainVC, this);
				convertItemButton.setIconLeftCSS("o_icon o_icon-fw o_FileResource-IMSQTI21_icon");
			}
		}
		
		if (qItemSecurityCallback.canDelete()) {
			deleteLink = LinkFactory.createToolLink("delete.item", translate("delete.item"), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_delete");
			commandDropdown.addComponent(deleteLink);
		}
	}

	private void initStatusTools() {
		Component statusCmp;
		if (canChangeStatus()) {
			statusCmp = buildStatusDrowdown();
		} else {
			statusCmp = buildStatusLink();
		}
		stackPanel.addTool(statusCmp, Align.left);
	}
	
	private boolean canChangeStatus() {
		QuestionStatus actualStatus = metadatasCtrl.getItem().getQuestionStatus();
		return (qItemSecurityCallback.canSetDraft() && !QuestionStatus.draft.equals(actualStatus))
				|| (qItemSecurityCallback.canSetRevised() && !QuestionStatus.revised.equals(actualStatus))
				|| (qItemSecurityCallback.canSetReview() && !QuestionStatus.review.equals(actualStatus))
				|| (qItemSecurityCallback.canSetFinal() && !QuestionStatus.finalVersion.equals(actualStatus))
				|| (qItemSecurityCallback.canSetEndOfLife() && !QuestionStatus.endOfLife.equals(actualStatus));
	}
	private Dropdown buildStatusDrowdown() {
		QuestionStatus actualStatus = metadatasCtrl.getItem().getQuestionStatus();

		Dropdown statusDropdown = new Dropdown("process.states", "lifecycle.status", false, getTranslator());
		statusDropdown.setLabeled(true, true);
		statusDropdown.setElementCssClass("o_qpool_tools_status");
		statusDropdown.setIconCSS("o_icon o_icon-fw o_icon_qitem_" + actualStatus.name());
		statusDropdown.setInnerText(translate("lifecycle.status." + actualStatus.name()));
		statusDropdown.setInnerCSS("o_labeled o_qpool_status_" + actualStatus.name());

		
		statusDropdown.setOrientation(DropdownOrientation.normal);
	
		statusDraftLink = LinkFactory.createToolLink("lifecycle.status.draft", translate("lifecycle.status.draft"), this);
		statusDraftLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_draft");
		statusDraftLink.setElementCssClass("o_labeled o_qpool_status_draft");
		statusDraftLink.setVisible(qItemSecurityCallback.canSetDraft() && !QuestionStatus.draft.equals(actualStatus));
		statusDropdown.addComponent(statusDraftLink);

		statusRevisedLink = LinkFactory.createToolLink("lifecycle.status.revised", translate("lifecycle.status.revised"), this);
		statusRevisedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_revised");
		statusRevisedLink.setElementCssClass("o_labeled o_qpool_status_revised");
		statusRevisedLink.setVisible(qItemSecurityCallback.canSetRevised() && !QuestionStatus.revised.equals(actualStatus));
		statusDropdown.addComponent(statusRevisedLink);
		
		statusReviewLink = LinkFactory.createToolLink("lifecycle.status.review", translate("lifecycle.status.review"), this);
		statusReviewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_review");
		statusReviewLink.setElementCssClass("o_labeled o_qpool_status_review");
		statusReviewLink.setVisible(qItemSecurityCallback.canSetReview() && !QuestionStatus.review.equals(actualStatus));
		statusDropdown.addComponent(statusReviewLink);
		
		statusFinalLink = LinkFactory.createToolLink("lifecycle.status.finalVersion", translate("lifecycle.status.finalVersion"), this);
		statusFinalLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_finalVersion");
		statusFinalLink.setElementCssClass("o_labeled o_qpool_status_finalVersion");
		statusFinalLink.setVisible(qItemSecurityCallback.canSetFinal() && !QuestionStatus.finalVersion.equals(actualStatus));
		statusDropdown.addComponent(statusFinalLink);
		
		statusEndOfLifeLink = LinkFactory.createToolLink("lifecycle.status.endOfLife", translate("lifecycle.status.endOfLife"), this);
		statusEndOfLifeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_endOfLife");
		statusEndOfLifeLink.setElementCssClass("o_labeled o_qpool_status_endOfLife");
		statusEndOfLifeLink.setVisible(qItemSecurityCallback.canSetEndOfLife() && !QuestionStatus.endOfLife.equals(actualStatus));
		statusDropdown.addComponent(statusEndOfLifeLink);

		return statusDropdown;
	}

	private Component buildStatusLink() {
		QuestionStatus actualStatus = metadatasCtrl.getItem().getQuestionStatus();
		Link statusLink = LinkFactory.createToolLink("status.link", translate("lifecycle.status." + actualStatus.name()), this);
		statusLink.setElementCssClass("o_qpool_tools_status o_qpool_status_" + actualStatus.name());
		statusLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_" + actualStatus.name());
		return statusLink;
	}
	
	private void initShareTools() {
		Dropdown sharesDropdown = new Dropdown("share.item", "share.item", false, getTranslator());
		sharesDropdown.setIconCSS("o_icon o_icon-fw o_icon_qitem_share");
		sharesDropdown.setOrientation(DropdownOrientation.normal);
		stackPanel.addTool(sharesDropdown, Align.left);
		
		exportItemLink = LinkFactory.createToolLink("export.item", translate("export.item"), this);
		exportItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_export");
		sharesDropdown.addComponent(exportItemLink);

		if (qPoolSecurityCallback.canUsePools()) {
			sharePoolItemLink = LinkFactory.createToolLink("share.pool", translate("share.pool"), this);
			sharePoolItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pool_pool");
			sharesDropdown.addComponent(sharePoolItemLink);
		}
		
		if (qPoolSecurityCallback.canUseGroups()) {
			shareGroupItemLink = LinkFactory.createToolLink("share.group", translate("share.group"), this);
			shareGroupItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_pool_share");
			sharesDropdown.addComponent(shareGroupItemLink);
		}
	}

	private void initPrevNextTools() {
		previousItemLink = LinkFactory.createToolLink("previous", translate("previous"), this);
		previousItemLink.setIconLeftCSS("o_icon o_icon-fw o_icon_previous");
		if ((itemIndex != null && itemIndex <= 0) || numberOfItems <= 1) {
			previousItemLink.setEnabled(false);
		}
		stackPanel.addTool(previousItemLink);
		
		String numbersOf = translate("item.numbers.of", new String[]{
				itemIndex != null? Integer.toString(itemIndex + 1) : "",
				Integer.toString(numberOfItems) });
		numberItemsLink = LinkFactory.createToolLink("item.numbers.of", numbersOf, this);
		stackPanel.addTool(numberItemsLink);
		
		nextItemLink = LinkFactory.createToolLink("next", translate("next"), this);
		nextItemLink.setIconLeftCSS("o_icon io_icon-fw o_icon_next");
		if (itemIndex != null && itemIndex + 1 >= numberOfItems) {
			nextItemLink.setEnabled(false);
		}
		stackPanel.addTool(nextItemLink);
	}

	private void initMetadataTools() {
		if (qItemSecurityCallback.canExportAuditLog()) {
			exportLogLink = LinkFactory.createToolLink("export.log", translate("export.log"), this);
			exportLogLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log"); 
			stackPanel.addTool(exportLogLink, Align.right);
		}
		showMetadataLink = LinkFactory.createToolLink("metadata.show", translate("metadata.show"), this);
		showMetadataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_show_metadata");
		hideMetadataLink = LinkFactory.createToolLink("metadata.hide", translate("metadata.hide"), this);
		hideMetadataLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qitem_hide_metadata");
		if (showMetadatas == null || showMetadatas) {
			doShowMetadata();
		} else {
			doHideMetadata();
		}
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		finishQuestionEdition();
		if (lock != null && lock.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lock);
		}
		lock = null;
        super.doDispose();
	}

	private void finishQuestionEdition() {
		if (questionEdited) {
			QuestionItem item = metadatasCtrl.getItem();
			qpoolService.backupQuestion(item, getIdentity());

			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(), Action.UPDATE_QUESTION)
					.withBefore(item)
					.withAfter(item);
			qpoolService.persist(builder.create());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == statusDraftLink) {
			doStatusDraft(ureq, metadatasCtrl.getItem());
		} else if (source == statusRevisedLink) {
			doStatusRevised(ureq, metadatasCtrl.getItem());
		} else if (source == statusReviewLink) {
			doStatusReview(ureq, metadatasCtrl.getItem());
		} else if (source == statusFinalLink) {
			doStatusFinal(ureq, metadatasCtrl.getItem());
		} else if (source == statusEndOfLifeLink) {
			doConfirmEndOfLife(ureq, metadatasCtrl.getItem());
		} else if(source == deleteLink) {
			doConfirmDelete(ureq, metadatasCtrl.getItem());
		} else if(source == shareGroupItemLink) {
			doSelectGroup(ureq, metadatasCtrl.getItem());
		} else if(source == sharePoolItemLink) {
			doSelectPool(ureq, metadatasCtrl.getItem());
		} else if(source == exportItemLink) {
			doExport(ureq, metadatasCtrl.getItem());
		} else if(source == copyItemLink) {
			doConfirmCopy(ureq, metadatasCtrl.getItem());
		} else if(source == convertItemLink || source == convertItemButton) {
			doConfirmConversion(ureq, metadatasCtrl.getItem());
		} else if(source == exportLogLink) {
			doExportLog(ureq, metadatasCtrl.getItem());
		} else if(source == nextItemLink) {
			fireEvent(ureq, new QItemEvent("next", metadatasCtrl.getItem()));
		} else if(source == previousItemLink) {
			fireEvent(ureq, new QItemEvent("previous", metadatasCtrl.getItem()));
		} else if(source == showMetadataLink) {
			doShowMetadata(ureq);
		} else if(source == hideMetadataLink) {
			doHideMetadata(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == copyConfirmationCtrl) {
			doPostCopy(ureq, event);
			cmc.deactivate();
			cleanUp();
		} else if(source == conversionConfirmationCtrl) {
			doPostConvert(ureq, event);
			cmc.deactivate();
			cleanUp();
		} else if(source == selectGroupCtrl) {
			cmc.deactivate();
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					QuestionItem item = (QuestionItem)((SelectBusinessGroupController)source).getUserObject();
					doShareItemsToGroups(ureq, Collections.singletonList(item), groups);
				}
			}
		} else if (source == selectPoolCtrl) {
			cmc.deactivate();
			if(event instanceof QPoolSelectionEvent) {
				QPoolSelectionEvent qpe = (QPoolSelectionEvent)event;
				List<Pool> pools = qpe.getPools();
				if(pools.size() > 0) {
					QuestionItemShort item = (QuestionItemShort)selectPoolCtrl.getUserObject();
					doShareItemsToPools(ureq, Collections.singletonList(item), pools);
				}
			}	
		}  else if(source == shareItemsCtrl) {
			if(event instanceof QPoolEvent) {
				metadatasCtrl.updateShares();
				fireEvent(ureq, event);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == reviewActionCtrl) {
			if (QItemReviewEvent.START.equals(event.getCommand())) {
				doConfirmStartReview(ureq);
			} else if (QItemReviewEvent.DO.equals(event.getCommand())) {
				openReview(ureq);
			}
		} else if (source == reviewStartCtrl) {
			if (event == Event.DONE_EVENT) {
				TaxonomyLevel taxonomyLevel = reviewStartCtrl.getSelectedTaxonomyLevel();
				doStartReview(ureq, taxonomyLevel);
			}
			cmc.deactivate();
			cleanUp();	
		} else if (source == reviewCtrl) {
			if (event == Event.DONE_EVENT) {
				float rating = reviewCtrl.getCurrentRatting();
				String comment = reviewCtrl.getComment();
				doRate(ureq, rating, comment);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmEndOfLifeCtrl) {
			boolean endOfLife = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(endOfLife) {
				QuestionItem item = (QuestionItem)confirmEndOfLifeCtrl.getUserObject();
				doEndOfLife(ureq, item);
			}
			cleanUp();
		} else if(source == deleteConfirmationCtrl) {
			if (event == Event.DONE_EVENT) {
				List<QuestionItemShort> items = deleteConfirmationCtrl.getItemsToDelete();
				doDelete(ureq, items);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		} else if(source == questionCtrl) {
			if(event instanceof QItemEdited) {
				questionEdited = true;
				fireEvent(ureq, event);
			}
		} else if(source == metadatasCtrl) {
			if(event instanceof QItemEdited) {
				reloadData(ureq);
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(copyConfirmationCtrl);
		removeAsListenerAndDispose(conversionConfirmationCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(selectPoolCtrl);
		removeAsListenerAndDispose(reviewCtrl);
		removeAsListenerAndDispose(reviewStartCtrl);
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(confirmEndOfLifeCtrl);
		removeAsListenerAndDispose(shareItemsCtrl);
		cmc = null;
		copyConfirmationCtrl = null;
		conversionConfirmationCtrl = null;
		selectGroupCtrl = null;
		selectPoolCtrl = null;
		reviewCtrl = null;
		reviewStartCtrl = null;
		deleteConfirmationCtrl = null;
		confirmEndOfLifeCtrl = null;
		shareItemsCtrl = null;
	}
	
	private void doConfirmStartReview(UserRequest ureq) {
		reviewStartCtrl = new ReviewStartController(ureq, getWindowControl(), metadatasCtrl.getItem(), itemSource.isAdminItemSource());
		listenTo(reviewStartCtrl);
		cmc = new CloseableModalController(getWindowControl(), null,
				reviewStartCtrl.getInitialComponent(), true,
				translate("process.start.review.title"), false);
		listenTo(cmc);
		cmc.activate();
	}

	private void doStartReview(UserRequest ureq, TaxonomyLevel taxonomyLevel) {
		QuestionItem item = metadatasCtrl.getItem();
		if (item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl) item;
			if (itemImpl.getTaxonomyLevel() == null || !itemImpl.getTaxonomyLevel().equals(taxonomyLevel)) {
				itemImpl.setTaxonomyLevel(taxonomyLevel);
			}
		}
		doChangeQuestionStatus(ureq, item, QuestionStatus.review);
	}
	
	private void openReview(UserRequest ureq) {
		reviewCtrl = new ReviewController(ureq, getWindowControl());
		listenTo(reviewCtrl);
		cmc = new CloseableModalController(getWindowControl(), null,
				reviewCtrl.getInitialComponent(), true,
				translate("process.rating.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doRate(UserRequest ureq, float rating, String comment) {
		QuestionItem item = metadatasCtrl.getItem();
		qpoolService.rateItemInReview(item, getIdentity(), rating, comment);
		reloadData(ureq);
		setCommentsController(ureq);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_STATUS_CHANGED, item.getKey()));
	}

	private void doStatusDraft(UserRequest ureq, QuestionItem item) {
		doChangeQuestionStatus(ureq, item, QuestionStatus.draft);
	}
	
	private void doStatusRevised(UserRequest ureq, QuestionItem item) {
		doChangeQuestionStatus(ureq, item, QuestionStatus.revised);
	}
	
	private void doStatusReview(UserRequest ureq, QuestionItem item) {
		doChangeQuestionStatus(ureq, item, QuestionStatus.review);
	}
	
	private void doStatusFinal(UserRequest ureq, QuestionItem item) {
		doChangeQuestionStatus(ureq, item, QuestionStatus.finalVersion);
	}
	
	private void doConfirmEndOfLife(UserRequest ureq, QuestionItem item) {
		String msg = translate("process.confirm.endOfLife", StringHelper.escapeHtml(item.getTitle()));
		confirmEndOfLifeCtrl = activateYesNoDialog(ureq, null, msg, confirmEndOfLifeCtrl);
		confirmEndOfLifeCtrl.setUserObject(item);
	}

	private void doEndOfLife(UserRequest ureq, QuestionItem item) {
		doChangeQuestionStatus(ureq, item, QuestionStatus.endOfLife);
	}
	
	private void doChangeQuestionStatus(UserRequest ureq, QuestionItem item, QuestionStatus newStatus) {
		if(!newStatus.equals(item.getQuestionStatus()) && item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.STATUS_CHANGED);
			builder.withBefore(itemImpl);
			builder.withMessage("New status: " + newStatus);
			itemImpl.setQuestionStatus(newStatus);
			qpoolService.updateItem(itemImpl);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
			fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_STATUS_CHANGED, item.getKey()));
		}
		reloadData(ureq);
	}

	private void reloadData(UserRequest ureq) {
		Long itemKey = metadatasCtrl.getItem().getKey();
		QuestionItemView itemView = itemSource.getItemWithoutRestrictions(itemKey);
		if (itemView != null) {
			qItemSecurityCallback.setQuestionItemView(itemView);
			initTools();
			setCommentsController(ureq);
			QuestionItem reloadedItem = qpoolService.loadItemById(itemView.getKey());
			metadatasCtrl.setItem(reloadedItem, qItemSecurityCallback);
			reviewActionCtrl.setSecurityCallback(qItemSecurityCallback);
			setQuestionController(ureq, reloadedItem, qItemSecurityCallback);
		}
	}
	
	private void doConfirmCopy(UserRequest ureq, QuestionItemShort item) {
		copyConfirmationCtrl = new CopyConfirmationController(ureq, getWindowControl(), Collections.singletonList(item),
				itemSource);
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
			fireEvent(ureq, new QItemEvent("copy-item", ipEvent.getSuccessfullItems().get(0)));
		}
	}

	private void doConfirmConversion(UserRequest ureq, QuestionItemShort item) {
		Map<String,List<QuestionItemShort>> formatToItems = availableConversionFormats(item);
		
		conversionConfirmationCtrl = new ConversionConfirmationController(ureq, getWindowControl(), formatToItems,
				itemSource);
		listenTo(conversionConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				conversionConfirmationCtrl.getInitialComponent(), true, translate("convert.item"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private Map<String,List<QuestionItemShort>> availableConversionFormats(QuestionItemShort item) {
		Map<String,List<QuestionItemShort>> formatToItems = new HashMap<>();
		List<QPoolSPI> spies = poolModule.getQuestionPoolProviders();
		for(QPoolSPI sp:spies) {
			if(sp != null && sp.isConversionPossible(item)) {
				List<QuestionItemShort> convertItems;
				if(formatToItems.containsKey(sp.getFormat())) {
					convertItems = formatToItems.get(sp.getFormat());
				} else {
					convertItems = new ArrayList<>(1);
					formatToItems.put(sp.getFormat(), Collections.singletonList(item));
				}
				convertItems.add(item);	
			}
		}
		return formatToItems;
	}
	
	private void doPostConvert(UserRequest ureq, Event event) {
		if (event instanceof QItemsProcessedEvent) {
			QItemsProcessedEvent ipEvent = (QItemsProcessedEvent) event;
			int numberOfCopies = ipEvent.getNumberOfItems();
			int numberOfFails = ipEvent.getNumberOfFails();
			if(numberOfFails == 0) {
				showInfo("convert.item.successful", new String[]{ Integer.toString(numberOfCopies)} );
				fireEvent(ureq, new QItemEvent("convert-item", ipEvent.getSuccessfullItems().get(0)));
			} else {
				showWarning("convert.item.warning", new String[]{ Integer.toString(numberOfFails), Integer.toString(numberOfCopies) } );
			}
		}
	}
	
	private void doSelectGroup(UserRequest ureq, QuestionItem item) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl(), null, item);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSelectPool(UserRequest ureq, QuestionItem item) {
		removeAsListenerAndDispose(selectPoolCtrl);
		selectPoolCtrl = new PoolsController(ureq, getWindowControl());
		selectPoolCtrl.setUserObject(item);
		listenTo(selectPoolCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectPoolCtrl.getInitialComponent(), true, translate("select.pool"));
		cmc.activate();
		listenTo(cmc);
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

	private void doConfirmDelete(UserRequest ureq, QuestionItem item) {
		deleteConfirmationCtrl = new DeleteConfirmationController(ureq, getWindowControl(),
				Collections.singletonList(item));
		listenTo(deleteConfirmationCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				deleteConfirmationCtrl.getInitialComponent(), true, translate("confirm.delete.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(UserRequest ureq, List<QuestionItemShort> items) {
		for (QuestionItemShort item: items) {
			QuestionItem qitem = qpoolService.loadItemById(item.getKey());
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.DELETE_QUESTION_ITEM);
			builder.withBefore(qitem);
			qpoolService.persist(builder.create());
		}
		qpoolService.deleteItems(items);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_DELETED));
		showInfo("item.deleted");
	}
	
	private void doExport(UserRequest ureq, QuestionItemShort item) {
		ExportQItemResource mr = new ExportQItemResource("UTF-8", getLocale(), item);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
	
	private void doExportLog(UserRequest ureq, QuestionItemShort item) {
		List<QuestionItemAuditLog> auditLog = qpoolService.getAuditLogByQuestionItem(item);
		QuestionItemAuditLogExport export = new QuestionItemAuditLogExport(item, auditLog, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
	
	private void doShowMetadata(UserRequest ureq) {
		doShowMetadata();
		doPutMetadatasSwitch(ureq, Boolean.TRUE);
	}

	private void doShowMetadata() {
		stackPanel.addTool(hideMetadataLink, Align.right);
		stackPanel.removeTool(showMetadataLink);
		mainVC.contextPut("metadataSwitch", Boolean.TRUE);
	}
	
	private void doHideMetadata(UserRequest ureq) {
		doHideMetadata();
		doPutMetadatasSwitch(ureq, Boolean.FALSE);
	}

	private void doHideMetadata() {
		stackPanel.addTool(showMetadataLink, Align.right);
		stackPanel.removeTool(hideMetadataLink);
		mainVC.contextPut("metadataSwitch", Boolean.FALSE);
	}
	
	private void doPutMetadatasSwitch(UserRequest ureq, Boolean show) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(QuestionItemDetailsController.class, GUIPREF_KEY_SHOW_METADATAS, show);
		}
	}

}
