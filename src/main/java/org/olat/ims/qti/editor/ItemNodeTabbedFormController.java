/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableDefaultController;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;

/**
 * Initial Date: Nov 21, 2004 <br>
 * 
 * @author patrick
 */
public class ItemNodeTabbedFormController extends TabbableDefaultController {
	
	private Item item;
	private QTIEditorPackage qtiPackage;
	
	private Controller itemCtrl;
	private ItemMetadataFormController metadataCtr;
	private FeedbackFormController feedbackCtr;
	private Panel feedbackPanel = new Panel("feedbackPanel");

	private final boolean restrictedEdit;
	private final boolean blockedEdit;
	
	/**
	 * @param item
	 * @param qtiPackage
	 * @param wControl
	 * @param trnsltr
	 */
	public ItemNodeTabbedFormController(Item item, QTIEditorPackage qtiPackage, UserRequest ureq, WindowControl wControl,
			boolean restrictedEdit, boolean blockedEdit) {
		super(ureq, wControl);
		this.blockedEdit = blockedEdit;
		this.restrictedEdit = restrictedEdit;
		metadataCtr = new ItemMetadataFormController(ureq, getWindowControl(), item, qtiPackage, restrictedEdit, blockedEdit);
		listenTo(metadataCtr);
		this.item = item;
		this.qtiPackage = qtiPackage;
		
		int questionType = item.getQuestion().getType();
		switch (questionType) {
			case Question.TYPE_SC:
				itemCtrl = new ChoiceItemController(ureq, getWindowControl(), item, qtiPackage, getTranslator(), restrictedEdit, blockedEdit);
				break;
			case Question.TYPE_MC:
				itemCtrl = new ChoiceItemController(ureq, getWindowControl(), item, qtiPackage, getTranslator(), restrictedEdit, blockedEdit);
				break;
			case Question.TYPE_KPRIM:
				itemCtrl = new ChoiceItemController(ureq, getWindowControl(), item, qtiPackage, getTranslator(), restrictedEdit, blockedEdit);
				break;
			case Question.TYPE_FIB:
				itemCtrl = new FIBItemController(ureq, getWindowControl(), item, qtiPackage, getTranslator(), restrictedEdit, blockedEdit);
				break;
			case Question.TYPE_ESSAY:
				itemCtrl = new EssayItemController(ureq, getWindowControl(), item, qtiPackage, getTranslator(), restrictedEdit, blockedEdit);
				break;
		}
		if(itemCtrl != null) {
			listenTo(itemCtrl);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == metadataCtr && event.equals(Event.DONE_EVENT)) {
			qtiPackage.serializeQTIDocument();
		}	else {
			// Pass events over to the parent controller
			fireEvent(ureq, event);
		}
		
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof TabbedPaneChangedEvent) {
			TabbedPaneChangedEvent tabbedPaneEvent = (TabbedPaneChangedEvent) event;
			if (feedbackPanel.equals(tabbedPaneEvent.getNewComponent())) {
				if (feedbackCtr != null) removeAsListenerAndDispose(feedbackCtr);
				feedbackCtr = new FeedbackFormController(ureq, getWindowControl(), qtiPackage, item, restrictedEdit, blockedEdit);
				// feedback controller sends out NodeBeforeChangeEvents which must be propagated				
				listenTo(feedbackCtr);
				feedbackPanel.setContent(feedbackCtr.getInitialComponent());
			}
		}
	}

	@Override
	protected void doDispose() {
	// metadataCtr and feedbackCtr are registered as child controllers
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		// add as listener to get tab activation events
		tabbedPane.addListener(this);
		
		if (item.isAlient()) {
			// this is an unknown type.
			tabbedPane.addTab(translate("tab.metadata"),this.createVelocityContainer("tab_itemAlien"));			
			return;
		}

		tabbedPane.addTab(translate("tab.metadata"), metadataCtr.getInitialComponent());
		
		if (itemCtrl != null) { // if item was identified
			boolean isSurvey = qtiPackage.getQTIDocument().isSurvey();
			int questionType = item.getQuestion().getType();
			
			tabbedPane.addTab(translate("tab.question"), itemCtrl.getInitialComponent());			
			
			if (!isSurvey && questionType != Question.TYPE_ESSAY) {
				tabbedPane.addTab(translate("tab.feedback"), feedbackPanel);
			}
			Controller itemPreviewController = new ItemPreviewController(getWindowControl(), item, qtiPackage, getTranslator());
			listenTo(itemPreviewController);
			tabbedPane.addTab(translate("tab.preview"), itemPreviewController.getInitialComponent());
			tabbedPane.addListener(itemPreviewController);
		}
	}
}