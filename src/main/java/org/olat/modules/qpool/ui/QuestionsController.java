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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionsController extends BasicController implements StackedControllerAware {
	
	private Link deleteItem, selectItem;
	private QuestionListController listCtrl;
	private final QuestionItemPreviewController previewCtrl;
	private final QuestionItemSummaryController detailsCtrl;

	private StackedController stackPanel;
	private final VelocityContainer mainVC;
	private DialogBoxController confirmDeleteBox;
	
	private final QuestionPoolService qpoolService;
	
	public QuestionsController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl);
		
		qpoolService = CoreSpringFactory.getImpl(QuestionPoolService.class);
		
		listCtrl = new QuestionListController(ureq, wControl, source);
		listenTo(listCtrl);
		detailsCtrl = new QuestionItemSummaryController(ureq, wControl);
		listenTo(detailsCtrl);
		previewCtrl = new QuestionItemPreviewController(ureq, wControl);
		listenTo(previewCtrl);
		
		mainVC = createVelocityContainer("items");
		mainVC.put("items", listCtrl.getInitialComponent());
		mainVC.put("details", detailsCtrl.getInitialComponent());
		mainVC.put("preview", previewCtrl.getInitialComponent());
		
		deleteItem = LinkFactory.createButton("delete.item", mainVC, this);
		selectItem = LinkFactory.createButton("select.item", mainVC, this);

		String[] js = new String[]{"js/jquery/uilayout/jquery.layout-latest.min.js"};
		JSAndCSSComponent jsAndCssComp = new JSAndCSSComponent("layouting", js, null);
		mainVC.put("layout", jsAndCssComp);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void updateSource(QuestionItemsSource source) {
		listCtrl.updateSource(source);
	}
	
	public QuestionItem getQuestionAt(int index) {
		return listCtrl.getQuestionItemAt(index);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		listCtrl.setStackedController(stackPanel);
		this.stackPanel = stackPanel;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(selectItem == source) {
			doSelect(ureq, detailsCtrl.getItem());
		} else if(deleteItem == source) {
			doConfirmDelete(ureq, detailsCtrl.getItem());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == listCtrl) {
			if(event instanceof QuestionEvent) {
				QuestionEvent se = (QuestionEvent)event;
				QuestionItem item = se.getItem();
				doUpdateDetails(ureq, item);
			} else if(event instanceof QPoolEvent) {
				fireEvent(ureq, event);
			}
		} else if(source == confirmDeleteBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				QuestionItem item = (QuestionItem)confirmDeleteBox.getUserObject();
				doDelete(ureq, item);
			}
		} else if (source instanceof QuestionItemDetailsController) {
			if(event instanceof QPoolEvent) {
				if(QPoolEvent.ITEM_DELETED.equals(event.getCommand())) {
					postDelete(ureq);
					stackPanel.popUpToRootController(ureq);
				}
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	protected void doSelect(UserRequest ureq, QuestionItem item) {
		QuestionItemDetailsController detailsCtrl = new QuestionItemDetailsController(ureq, getWindowControl(), item);
		listenTo(detailsCtrl);
		LayoutMain3ColsController mainCtrl = new LayoutMain3ColsController(ureq, getWindowControl(), detailsCtrl);
		listenTo(mainCtrl);
		stackPanel.pushController(item.getTitle(), mainCtrl);
	}
	
	private void doConfirmDelete(UserRequest ureq, QuestionItem item) {
		confirmDeleteBox = activateYesNoDialog(ureq, null, translate("confirm.delete"), confirmDeleteBox);
		confirmDeleteBox.setUserObject(item);
	}
	
	private void doUpdateDetails(UserRequest ureq, QuestionItem item) {
		detailsCtrl.updateItem(item);
		previewCtrl.updateItem(ureq, item);
	}
	
	private void doDelete(UserRequest ureq, QuestionItem item) {
		qpoolService.deleteItems(Collections.singletonList(item));
		postDelete(ureq);
	}
	
	private void postDelete(UserRequest ureq) {
		listCtrl.reset();
		detailsCtrl.reset();
		previewCtrl.reset();
		
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_DELETED));
		showInfo("item.deleted");
	}
}
