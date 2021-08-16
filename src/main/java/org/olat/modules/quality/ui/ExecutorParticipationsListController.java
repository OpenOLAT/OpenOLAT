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

import static org.olat.modules.quality.QualityExecutorParticipationStatus.FUTURE;
import static org.olat.modules.quality.QualityExecutorParticipationStatus.OVER;
import static org.olat.modules.quality.QualityExecutorParticipationStatus.PARTICIPATED;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.quality.QualityDataCollectionStatus;
import org.olat.modules.quality.QualityExecutorParticipation;
import org.olat.modules.quality.QualityExecutorParticipationSearchParams;
import org.olat.modules.quality.ui.ExecutorParticipationDataModel.ExecutorParticipationCols;
import org.olat.modules.quality.ui.security.MainSecurityCallback;

/**
 * 
 * Initial date: 20.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutorParticipationsListController extends FormBasicController implements Activateable2 {

	private static final String ORES_EXECUTION_TYPE = "execution";
	private static final String CMD_EXECUTE = "execute";
	private static final Collection<QualityDataCollectionStatus> STATUS_FILTER = Arrays.asList(
			QualityDataCollectionStatus.READY,
			QualityDataCollectionStatus.RUNNING,
			QualityDataCollectionStatus.FINISHED);
	
	private ExecutorParticipationDataModel dataModel;
	private FlexiTableElement tableEl;

	private ExecutionController executionCtrl;
	
	private final MainSecurityCallback secCallback;

	public ExecutorParticipationsListController(UserRequest ureq, WindowControl wControl,
			MainSecurityCallback secCallback) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.executionStatus,
				new QualityExecutionParticipationStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.deadline));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.topicType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExecutorParticipationCols.topic));
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(ExecutorParticipationCols.execute.i18nHeaderKey(),
				ExecutorParticipationCols.execute.ordinal(), CMD_EXECUTE,
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer("", CMD_EXECUTE, "o_icon o_icon-lg o_icon_qual_part_execute", null),
						null));
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);

		QualityExecutorParticipationSearchParams searchParams = new QualityExecutorParticipationSearchParams();
		searchParams.setExecutorRef(getIdentity());
		searchParams.setDataCollectionStatus(STATUS_FILTER);
		ExecutorParticipationDataSource dataSource = new ExecutorParticipationDataSource(getTranslator(), searchParams);
		dataModel = new ExecutorParticipationDataModel(dataSource, columnsModel, secCallback, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "user-participations", dataModel, 25, true, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_qual_exec_list");
		tableEl.setAndLoadPersistedPreferences(ureq, "quality-executor-participation");
		tableEl.setEmptyTableMessageKey("executor.participation.empty.table");
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if (ORES_EXECUTION_TYPE.equals(type)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			ExecutorParticipationRow row = dataModel.getObjectByParticipationKey(key);
			if (row == null) {
				dataModel.clear();
				dataModel.load(null, null, 0, -1);
				row = dataModel.getObjectByParticipationKey(key);
				if (row != null) {
					doExecute(ureq, row.getParticipation());
					int index = dataModel.getObjects().indexOf(row);
					if (index >= 1 && tableEl.getPageSize() > 1) {
						int page = index / tableEl.getPageSize();
						tableEl.setPage(page);
					}
				} else {
					showInfo("executor.participation.forbidden");
				}
			} else {
				doExecute(ureq, row.getParticipation());
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ExecutorParticipationRow row = dataModel.getObject(se.getIndex());
				if (CMD_EXECUTE.equals(cmd)) {
					doExecute(ureq, row.getParticipation());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == executionCtrl) {
			if (event == Event.DONE_EVENT) {
				showInfo("executor.participation.future.done.message");
				doDeactivateExecution(ureq);
			} else if (event == Event.CLOSE_EVENT) {
				doDeactivateExecution(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doDeactivateExecution(UserRequest ureq) {
		WindowControl wControl = addToHistory(ureq, this);
		getWindowControl().pop();
		String businessPath = wControl.getBusinessControl().getAsString();
		getWindowControl().getWindowBackOffice().getChiefController()
			.getScreenMode().setMode(Mode.standard, businessPath);
		cleanUp();
		tableEl.reloadData();
	}

	private void cleanUp() {
		removeAsListenerAndDispose(executionCtrl);
		executionCtrl = null;
	}

	@SuppressWarnings("deprecation")
	private void doExecute(UserRequest ureq, QualityExecutorParticipation participation) {
		if (FUTURE.equals(participation.getExecutionStatus())) {
			showInfo("executor.participation.future", participation.getTitle());
			return;
		}
		if (PARTICIPATED.equals(participation.getExecutionStatus())) {
			showInfo("executor.participation.already.done", participation.getTitle());
			return;
		}
		if (OVER.equals(participation.getExecutionStatus())) {
			showInfo("executor.participation.over", participation.getTitle());
			return;
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(ORES_EXECUTION_TYPE,
				participation.getParticipationRef().getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		executionCtrl = new ExecutionController(ureq, bwControl, participation);
		listenTo(executionCtrl);
		
		WindowControl wControl = getWindowControl();
		ChiefController cc = wControl.getWindowBackOffice().getChiefController();
		String businessPath = executionCtrl.getWindowControlForDebug().getBusinessControl().getAsString();
		cc.getScreenMode().setMode(Mode.full, businessPath);
		wControl.pushToMainArea(executionCtrl.getInitialComponent());
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
