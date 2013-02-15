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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.qpool.QuestionItem;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionsController extends BasicController implements StackedControllerAware {
	
	private QuestionListController listCtrl;
	private final QuestionItemPreviewController previewCtrl;
	private final QuestionItemOverviewController detailsCtrl;
	
	private final VelocityContainer mainVC;
	
	public QuestionsController(UserRequest ureq, WindowControl wControl, QuestionItemsSource source) {
		super(ureq, wControl);
		
		listCtrl = new QuestionListController(ureq, wControl, source);
		listenTo(listCtrl);
		detailsCtrl = new QuestionItemOverviewController(ureq, wControl);
		listenTo(detailsCtrl);
		previewCtrl = new QuestionItemPreviewController(ureq, wControl);
		listenTo(previewCtrl);
		
		mainVC = createVelocityContainer("items");
		mainVC.put("items", listCtrl.getInitialComponent());
		mainVC.put("details", detailsCtrl.getInitialComponent());
		mainVC.put("preview", previewCtrl.getInitialComponent());
		
		String[] js = new String[]{"js/jquery/uilayout/jquery.layout-latest.min.js"};
		JSAndCSSComponent jsAndCssComp = new JSAndCSSComponent("layouting", js, null);
		mainVC.put("layout", jsAndCssComp);
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public QuestionItem getQuestionAt(int index) {
		return listCtrl.getQuestionItemAt(index);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		listCtrl.setStackedController(stackPanel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == listCtrl) {
			if(event instanceof QuestionEvent) {
				QuestionEvent se = (QuestionEvent)event;
				QuestionItem item = se.getItem();
				doUpdateDetails(item);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doUpdateDetails(QuestionItem item) {
		detailsCtrl.updateItem(item);
		previewCtrl.updateItem(item);
	}
}
