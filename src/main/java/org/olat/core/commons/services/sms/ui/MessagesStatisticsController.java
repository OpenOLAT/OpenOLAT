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
package org.olat.core.commons.services.sms.ui;

import java.util.List;

import org.olat.core.commons.services.sms.SimpleMessageService;
import org.olat.core.commons.services.sms.model.MessageStatistics;
import org.olat.core.commons.services.sms.ui.MessageStatisticsDataModel.MLogStatsCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessagesStatisticsController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private MessageStatisticsDataModel model;
	
	@Autowired
	private SimpleMessageService messageService;
	
	public MessagesStatisticsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "statistics");
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel yearColumn = new DefaultFlexiColumnModel(MLogStatsCols.year, new YearCellRenderer());
		yearColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(yearColumn);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MLogStatsCols.month, new MonthCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MLogStatsCols.numOfMessages));
		
		model = new MessageStatisticsDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "stats", model, 50, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setExportEnabled(true);
	}
	
	private void loadModel() {
		List<MessageStatistics> stats = messageService.getStatisticsPerMonth();
		model.setObjects(stats);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
