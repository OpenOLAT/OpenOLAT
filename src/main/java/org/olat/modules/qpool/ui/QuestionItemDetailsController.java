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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.impl.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Roles;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QPoolService;

/**
 * 
 * Initial date: 24.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemDetailsController extends BasicController implements StackedControllerAware {
	
	private Link deleteItem, shareItem, exportItem, editItem;

	private Controller previewCtrl;
	private CloseableModalController cmc;
	private final VelocityContainer mainVC;
	private DialogBoxController confirmDeleteBox;
	private SelectBusinessGroupController selectGroupCtrl;
	private final MetadatasController metadatasCtrl;
	private final UserCommentsAndRatingsController commentsAndRatingCtr;
	private StackedController stackPanel;

	private final boolean canEditContent;
	
	private final QuestionPoolModule poolModule;
	private final QPoolService qpoolService;
	
	public QuestionItemDetailsController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean editable) {
		super(ureq, wControl);

		poolModule = CoreSpringFactory.getImpl(QuestionPoolModule.class);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		
		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		if(spi == null) {
			previewCtrl = new QuestionItemRawController(ureq, wControl);
		} else {
			previewCtrl = spi.getPreviewController(ureq, getWindowControl(), item);
			if(previewCtrl == null) {
				previewCtrl = new QuestionItemRawController(ureq, wControl);
			}
		}
		listenTo(previewCtrl);
		
		boolean canEdit = editable || qpoolService.isAuthor(item, getIdentity());
		canEditContent = canEdit && (spi != null && spi.isTypeEditable());
		metadatasCtrl = new MetadatasController(ureq, wControl, item, canEdit);
		listenTo(metadatasCtrl);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean moderator = roles.isOLATAdmin();
		boolean anonymous = roles.isGuestOnly() || roles.isInvitee();
		CommentAndRatingService commentAndRatingService = CoreSpringFactory.getImpl(CommentAndRatingService.class);
		commentAndRatingService.init(getIdentity(), item, null, moderator, anonymous);
		commentsAndRatingCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
		listenTo(commentsAndRatingCtr);

		mainVC = createVelocityContainer("item_details");
		if(canEditContent) {
			editItem = LinkFactory.createButton("edit", mainVC, this);
			editItem.setCustomEnabledLinkCSS("b_link_left_icon b_link_edit");
		}
		shareItem = LinkFactory.createButton("share.item", mainVC, this);
		deleteItem = LinkFactory.createButton("delete.item", mainVC, this);
		deleteItem.setVisible(canEdit);
		exportItem = LinkFactory.createButton("export.item", mainVC, this);
		
		mainVC.put("type_specifics", previewCtrl.getInitialComponent());
		mainVC.put("metadatas", metadatasCtrl.getInitialComponent());
		mainVC.put("comments", commentsAndRatingCtr.getInitialComponent());
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == deleteItem) {
			doConfirmDelete(ureq, metadatasCtrl.getItem());
		} else if(source == shareItem) {
			doSelectGroup(ureq, metadatasCtrl.getItem());
		} else if(source == exportItem) {
			doExport(ureq, metadatasCtrl.getItem());
		} else if(source == editItem) {
			if(canEditContent) {
				doEdit(ureq, metadatasCtrl.getItem());
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
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				QuestionItem item = (QuestionItem)confirmDeleteBox.getUserObject();
				doDelete(ureq, item);
			}
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(selectGroupCtrl);
		cmc = null;
		selectGroupCtrl = null;
	}
	
	private void doEdit(UserRequest ureq, QuestionItem item) {
		QPoolSPI spi = poolModule.getQuestionPoolProvider(item.getFormat());
		Controller editCtrl = spi.getEditableController(ureq, getWindowControl(), item);
		
		LayoutMain3ColsController mainCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), editCtrl);
		stackPanel.pushController("Edition", mainCtrl);
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
		confirmDeleteBox = activateYesNoDialog(ureq, null, translate("confirm.delete"), confirmDeleteBox);
		confirmDeleteBox.setUserObject(item);
	}
	
	private void doDelete(UserRequest ureq, QuestionItemShort item) {
		qpoolService.deleteItems(Collections.singletonList(item));
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_DELETED));
		showInfo("item.deleted");
	}
	
	private void doExport(UserRequest ureq, QuestionItemShort item) {
		QPoolExportResource mr = new QPoolExportResource("UTF-8", item);
		ureq.getDispatchResult().setResultingMediaResource(mr);
	}
}
