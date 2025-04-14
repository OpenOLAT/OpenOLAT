/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.iq;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.iq.ReferencesHistoryTableModel.ReferencesCols;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ReferenceHistoryWithInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReferencesHistoryController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private ReferencesHistoryTableModel tableModel;
	
	private final String subIdent;
	private final RepositoryEntry courseEntry;
	private final RepositoryEntry currentTestEntry;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qti21Service;
	
	public ReferencesHistoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, String subIdent,
			RepositoryEntry currentTestEntry) {
		super(ureq, wControl, "references_history");
		this.subIdent = subIdent;
		this.courseEntry = courseEntry;
		this.currentTestEntry = currentTestEntry;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReferencesCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferencesCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferencesCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferencesCols.assignedOn,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferencesCols.assignedBy));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReferencesCols.runs));
		
		tableModel = new ReferencesHistoryTableModel(columnsModel, currentTestEntry, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);	
		
		uifactory.addFormSubmitButton("close", formLayout);
	}
	
	private void loadModel() {
		List<ReferenceHistoryWithInfos> infos = qti21Service.getReferenceHistoryWithInfos(courseEntry, subIdent);
		List<ReferenceHistoryRow> rows = infos.stream().map(info -> {
			String assignedBy = info.doer() == null ? null : userManager.getUserDisplayName(info.doer());
			return new ReferenceHistoryRow(info.testEntry(), info.date(), assignedBy, info.runs());
		}).toList();
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
