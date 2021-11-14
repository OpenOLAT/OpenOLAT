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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.ui.events.QPoolEvent;

/**
 * 
 * This controller build the GUI with the table of items,
 * the preview and the short summary of metadatas.<br/>
 * 
 * Initial date: 12.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionsController extends BasicController implements Activateable2 {
	
	private QuestionListController listCtrl;
	private final TooledStackedPanel stackPanel;

	private QuestionItemsSource dataSource;
	
	public QuestionsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QuestionItemsSource source, QPoolSecurityCallback securityCallback, String key,
			boolean searchAllTaxonomyLevels) {
		super(ureq, wControl);
		
		this.stackPanel = stackPanel;
		this.dataSource = source;

		listCtrl = new QuestionListController(ureq, wControl, stackPanel, source, securityCallback, key, searchAllTaxonomyLevels);
		listenTo(listCtrl);

		putInitialPanel(listCtrl.getInitialComponent());
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		listCtrl.activate(ureq, entries, state);
	}
	
	public void setQuestionItemCollection(QuestionItemCollection coll) {
		listCtrl.setItemCollection(coll);
	}

	public void updateSource(QuestionItemsSource source) {
		this.dataSource = source;
		updateSource();
	}
	
	public void updateSource() {
		listCtrl.updateSource(dataSource);
		listCtrl.updateStatusFilter();
	}
	
	public QuestionItemShort getQuestionAt(int index) {
		return listCtrl.getQuestionItemAt(index);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == listCtrl) {
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
	
	private void postDelete(UserRequest ureq) {
		listCtrl.reset();
		
		fireEvent(ureq, new QPoolEvent(QPoolEvent.ITEM_DELETED));
		showInfo("item.deleted");
	}
}
