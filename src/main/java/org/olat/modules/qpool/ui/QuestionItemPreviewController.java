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
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemPreviewController extends BasicController {

	private QuestionItem item;
	private Controller previewCtrl;
	private final SimpleStackedPanel previewPanel;
	@Autowired
	private QuestionPoolModule poolModule;
	
	public QuestionItemPreviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		previewPanel = new SimpleStackedPanel("preview.container");
		putInitialPanel(previewPanel);
	}

	public QuestionItem getItem() {
		return item;
	}
	
	public void reset() {
		this.item = null;
		removeAsListenerAndDispose(previewCtrl);
		previewCtrl = null;
		previewPanel.setContent(null);
	}
	
	public void refresh(UserRequest ureq) {
		updateItem(ureq, item);
	}
	
	public void updateItem(UserRequest ureq, QuestionItem updatedItem) {
		this.item = updatedItem;
		removeAsListenerAndDispose(previewCtrl);
		if(updatedItem == null) {
			previewCtrl = null;
			previewPanel.setContent(null);
		} else {
			Component content;
			QPoolSPI spi = poolModule.getQuestionPoolProvider(updatedItem.getFormat());
			if(spi == null) {
				content = getRawContent();
			} else {
				previewCtrl = spi.getPreviewController(ureq, getWindowControl(), updatedItem, true);
				if(previewCtrl == null) {
					content = getRawContent();
				} else {
					listenTo(previewCtrl);
					content = previewCtrl.getInitialComponent();
				}
			}
			
			previewPanel.setContent(content);
		}
	}
	
	private Component getRawContent() {
		return createVelocityContainer("raw_content");
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
