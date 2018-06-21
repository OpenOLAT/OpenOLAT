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
package org.olat.modules.quality.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.ui.ExecutorParticipationDataModel.ExecutorParticipationCols;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutorParticipationsListController extends FormBasicController {

	private static final String CMD_EXECUTE = "execute";
	
	private ExecutorParticipationDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final QualitySecurityCallback secCallback;
	
	public ExecutorParticipationsListController(UserRequest ureq, WindowControl wControl,
			QualitySecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.participationStatus));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.deadline));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.title));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(ExecutorParticipationCols.execute.i18nHeaderKey(),
				ExecutorParticipationCols.execute.ordinal(), CMD_EXECUTE,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_EXECUTE, "o_icon o_icon-lg o_icon_qual_part_execute", null),
						null));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);

		ExcecutorParticipationDataSource dataSource = new ExcecutorParticipationDataSource(getIdentity());
		dataModel = new ExecutorParticipationDataModel(dataSource, columnsModel, secCallback, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "user-participations", dataModel, 25, true, getTranslator(), formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
