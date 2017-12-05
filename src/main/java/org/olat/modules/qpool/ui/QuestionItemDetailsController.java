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

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.manager.ExportQItemResource;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.events.QItemEvent;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.modules.qpool.ui.metadata.MetadatasController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemDetailsController extends BasicController implements TooledController, Activateable2 {
	
	private Link editItem;
	private Link startReviewLink;
	private Link reviewLink;
	private Link revisionLink;
	private Link finalLink;
	private Link endOfLifeLink;
	private Link deleteLink;
	private Link nextItemLink;
	private Link numberItemsLink;
	private Link previousItemLink;
	private Link showMetadataLink;
	private Link hideMetadataLink;
	private Link deleteItem, shareItem, exportItem, copyItem;

	private Controller editCtrl;
	private Controller previewCtrl;
	private CloseableModalController cmc;
	private final VelocityContainer mainVC;
	private DialogBoxController confirmStartReviewCtrl;
	private DialogBoxController confirmEndOfLifeCtrl;
	private DialogBoxController confirmDeleteBox;
	private LayoutMain3ColsController editMainCtrl;
	private SelectBusinessGroupController selectGroupCtrl;
	private final MetadatasController metadatasCtrl;
	private final UserCommentsAndRatingsController commentsAndRatingCtr;
	private final TooledStackedPanel stackPanel;

	private final QuestionItemSecurityCallback securityCallback;
	private final Integer itemIndex;
	private final int numberOfItems;
	
	@Autowired
	private QuestionPoolModule poolModule;
	@Autowired
	private QPoolService qpoolService;
	
	public QuestionItemDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QuestionItemSecurityCallback securityCallback, QuestionItem item, Integer itemIndex, int numberOfItems) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.securityCallback = securityCallback;
		this.itemIndex = itemIndex;
		this.numberOfItems = numberOfItems;
		
		metadatasCtrl = new MetadatasController(ureq, wControl, item, securityCallback);
		listenTo(metadatasCtrl);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean moderator = roles.isOLATAdmin();
		boolean anonymous = roles.isGuestOnly() || roles.isInvitee();
		CommentAndRatingSecurityCallback commentAndRatingSecurityCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), moderator, anonymous);
		commentsAndRatingCtr = new UserCommentsAndRatingsController(ureq, getWindowControl(), item, null, commentAndRatingSecurityCallback, true, true, true);
		listenTo(commentsAndRatingCtr);

		mainVC = createVelocityContainer("item_details");

		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		boolean canEditContent = securityCallback.canEditQuestion() && (spi != null && spi.isTypeEditable());
		if(canEditContent) {
			editItem = LinkFactory.createButton("edit", mainVC, this);
			editItem.setIconLeftCSS("o_icon o_icon_edit");
		}

		shareItem = LinkFactory.createButton("share.item", mainVC, this);
		copyItem = LinkFactory.createButton("copy", mainVC, this);
		if(securityCallback.canDelete()) {
			deleteItem = LinkFactory.createButton("delete.item", mainVC, this);
			deleteItem.setVisible(securityCallback.canEditQuestion());
		}
		exportItem = LinkFactory.createButton("export.item", mainVC, this);
		
		setPreviewController(ureq, item);
		mainVC.put("metadatas", metadatasCtrl.getInitialComponent());
		mainVC.put("comments", commentsAndRatingCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	public void initTools() {
		if (securityCallback.canStartReview()) {
			startReviewLink = LinkFactory.createToolLink("process.start.review", translate("process.start.review"), this);
			startReviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_start_review");
			stackPanel.addTool(startReviewLink, Align.left);
		}
		if (securityCallback.canReview()) {
			reviewLink = LinkFactory.createToolLink("process.review", translate("process.review"), this);
			reviewLink.setIconLeftCSS("o_icon o_icon-lg o_icon_review");
			stackPanel.addTool(reviewLink, Align.left);
		}
		if (securityCallback.canSetRevision()) {
			revisionLink = LinkFactory.createToolLink("process.revision", translate("process.revision"), this);
			revisionLink.setIconLeftCSS("o_icon o_icon-lg o_icon_revision");
			stackPanel.addTool(revisionLink, Align.left);
		}
		if (securityCallback.canSetFinal()) {
			finalLink = LinkFactory.createToolLink("process.final", translate("process.final"), this);
			finalLink.setIconLeftCSS("o_icon o_icon-lg o_icon_final");
			stackPanel.addTool(finalLink, Align.left);
		}
		if (securityCallback.canSetEndOfLife()) {
			endOfLifeLink = LinkFactory.createToolLink("process.endOfLife", translate("process.endOfLife"), this);
			endOfLifeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_end_of_life");
			stackPanel.addTool(endOfLifeLink, Align.left);
		}
		if (securityCallback.canDelete()) {
			deleteLink = LinkFactory.createToolLink("process.delete", translate("process.delete"), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
			stackPanel.addTool(deleteLink, Align.left);
		}
		
		previousItemLink = LinkFactory.createToolLink("previous", translate("previous"), this);
		previousItemLink.setIconLeftCSS("o_icon o_icon-lg o_icon_previous");
		stackPanel.addTool(previousItemLink);
		
		String numbersOf = translate("item.numbers.of", new String[]{
				itemIndex != null? Integer.toString(itemIndex + 1): "",
				Integer.toString(numberOfItems) });
		numberItemsLink = LinkFactory.createToolLink("item.numbers.of", numbersOf, this);
		stackPanel.addTool(numberItemsLink);
		
		nextItemLink = LinkFactory.createToolLink("next", translate("next"), this);
		nextItemLink.setIconLeftCSS("o_icon io_icon-lg o_icon_next");
		stackPanel.addTool(nextItemLink);
		
		showMetadataLink = LinkFactory.createToolLink("metadata.show", translate("metadata.show"), this);
		showMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit_metadata");
		hideMetadataLink = LinkFactory.createToolLink("metadata.hide", translate("metadata.hide"), this);
		hideMetadataLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit_metadata");
		doHideMetadata();
	}
	
	protected void setPreviewController(UserRequest ureq, QuestionItem item) {
		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		if(spi == null) {
			previewCtrl = new QuestionItemRawController(ureq, getWindowControl());
		} else {
			previewCtrl = spi.getPreviewController(ureq, getWindowControl(), item, false);
			if(previewCtrl == null) {
				previewCtrl = new QuestionItemRawController(ureq, getWindowControl());
			}
		}
		listenTo(previewCtrl);
		if(mainVC != null) {
			mainVC.put("type_specifics", previewCtrl.getInitialComponent());
		}
	}
	
	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resourceTypeName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("edit".equalsIgnoreCase(resourceTypeName)) {
			if(securityCallback.canEditQuestion() || metadatasCtrl.getItem() != null) {
				doEdit(ureq, metadatasCtrl.getItem());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startReviewLink) {
			doConfirmStartReview(ureq, metadatasCtrl.getItem());
		} else if (source == endOfLifeLink) {
			doConfirmEndOfLife(ureq, metadatasCtrl.getItem());
		} else if(source == deleteItem) {
			doConfirmDelete(ureq, metadatasCtrl.getItem());
		} else if(source == shareItem) {
			doSelectGroup(ureq, metadatasCtrl.getItem());
		} else if(source == exportItem) {
			doExport(ureq, metadatasCtrl.getItem());
		} else if(source == editItem) {
			doEdit(ureq, metadatasCtrl.getItem());
		} else if(source == copyItem) {
			doCopy(ureq, metadatasCtrl.getItem());
		} else if(source == nextItemLink) {
			fireEvent(ureq, new QItemEvent("next", metadatasCtrl.getItem()));
		} else if(source == previousItemLink) {
			fireEvent(ureq, new QItemEvent("previous", metadatasCtrl.getItem()));
		} else if(source == showMetadataLink) {
			doShowMetadata();
		} else if(source == hideMetadataLink) {
			doHideMetadata();
		} else if(source == stackPanel) {
			if(event instanceof PopEvent) {
				PopEvent pop = (PopEvent)event;
				if(pop.getController() == editMainCtrl) {
					doContentChanged(ureq);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectGroupCtrl) {
			if(event instanceof BusinessGroupSelectionEvent) {
				BusinessGroupSelectionEvent bge = (BusinessGroupSelectionEvent)event;
				List<BusinessGroup> groups = bge.getGroups();
				if(groups.size() > 0) {
					QuestionItem item = (QuestionItem)((SelectBusinessGroupController)source).getUserObject();
					doShareItems(ureq, item, groups);
					metadatasCtrl.updateShares();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmStartReviewCtrl) {
			boolean startReview = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(startReview) {
				QuestionItem item = (QuestionItem)confirmStartReviewCtrl.getUserObject();
				doStartReview(ureq, item);
			}
		} else if(source == confirmEndOfLifeCtrl) {
			boolean endOfLife = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(endOfLife) {
				QuestionItem item = (QuestionItem)confirmEndOfLifeCtrl.getUserObject();
				doEndOfLife(ureq, item);
			}
		} else if(source == confirmDeleteBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				QuestionItem item = (QuestionItem)confirmDeleteBox.getUserObject();
				doDelete(ureq, item);
			}
		} else if(source == cmc) {
			cleanUp();
		} else if(source == editCtrl) {
			if(event == Event.CHANGED_EVENT) {
				doContentChanged(ureq);
			} else if(event instanceof QItemEdited) {
				fireEvent(ureq, event);
			}
		} else if(source == metadatasCtrl) {
			if(event instanceof QItemEdited) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectGroupCtrl);
		cmc = null;
		selectGroupCtrl = null;
	}
	
	private void doConfirmStartReview(UserRequest ureq, QuestionItem item) {
		String msg = translate("process.confirm.start.review", StringHelper.escapeHtml(item.getTitle()));
		confirmStartReviewCtrl = activateYesNoDialog(ureq, null, msg, confirmStartReviewCtrl);
		confirmStartReviewCtrl.setUserObject(item);
	}

	private void doStartReview(UserRequest ureq, QuestionItemShort item) {
		qpoolService.startReview(Collections.singletonList(item));
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_REVIEW_STARTED, item.getKey()));
		showInfo("process.review.started");
	}
	
	private void doConfirmEndOfLife(UserRequest ureq, QuestionItem item) {
		String msg = translate("process.confirm.endOfLife", StringHelper.escapeHtml(item.getTitle()));
		confirmEndOfLifeCtrl = activateYesNoDialog(ureq, null, msg, confirmEndOfLifeCtrl);
		confirmEndOfLifeCtrl.setUserObject(item);
	}

	private void doEndOfLife(UserRequest ureq, QuestionItemShort item) {
		qpoolService.setEndOfLife(Collections.singletonList(item));
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_END_OF_LIFE, item.getKey()));
		showInfo("process.endOfLife.set");
	}
	
	private void doCopy(UserRequest ureq, QuestionItemShort item) {
		List<QuestionItem> copies = qpoolService.copyItems(getIdentity(), Collections.singletonList(item));
		if(copies.size() == 1) {
			showInfo("item.copied", Integer.toString(copies.size()));
			fireEvent(ureq, new QItemEvent("copy-item", copies.get(0)));
		}
	}
	
	private void doEdit(UserRequest ureq, QuestionItem item) {
		removeAsListenerAndDispose(editCtrl);
		
		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		editCtrl = spi.getEditableController(ureq, getWindowControl(), item);
		listenTo(editCtrl);
		
		editMainCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), editCtrl);
		stackPanel.pushController("Edition", editMainCtrl);
	}
	
	private void doContentChanged(UserRequest ureq) {
		QuestionItem item = metadatasCtrl.updateVersionNumber();
		//update preview
		setPreviewController(ureq, item);
	}
	
	private void doSelectGroup(UserRequest ureq, QuestionItem item) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl());
		selectGroupCtrl.setUserObject(item);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShareItems(UserRequest ureq, QuestionItemShort item, List<BusinessGroup> groups) {
		qpoolService.shareItemsWithGroups(Collections.singletonList(item), groups, false);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_SHARED));
	}

	private void doConfirmDelete(UserRequest ureq, QuestionItem item) {
		String msg = translate("confirm.delete", StringHelper.escapeHtml(item.getTitle()));
		confirmDeleteBox = activateYesNoDialog(ureq, null, msg, confirmDeleteBox);
		confirmDeleteBox.setUserObject(item);
	}
	
	private void doDelete(UserRequest ureq, QuestionItemShort item) {
		qpoolService.deleteItems(Collections.singletonList(item));
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_DELETED));
		showInfo("item.deleted");
	}
	
	private void doExport(UserRequest ureq, QuestionItemShort item) {
		ExportQItemResource mr = new ExportQItemResource("UTF-8", getLocale(), item);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
	
	private void doShowMetadata() {
		stackPanel.addTool(hideMetadataLink, Align.right);
		stackPanel.removeTool(showMetadataLink);
		mainVC.contextPut("metadataSwitch", Boolean.TRUE);
	}
	
	private void doHideMetadata() {
		stackPanel.addTool(showMetadataLink, Align.right);
		stackPanel.removeTool(hideMetadataLink);
		mainVC.contextPut("metadataSwitch", Boolean.FALSE);
	}

}
