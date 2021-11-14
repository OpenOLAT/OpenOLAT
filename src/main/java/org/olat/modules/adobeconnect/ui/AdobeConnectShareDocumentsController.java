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
package org.olat.modules.adobeconnect.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
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
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.ui.AdobeConnectContentTableModel.ACContentsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectShareDocumentsController extends FormBasicController {
	
	private FlexiTableElement contentTableEl;
	private AdobeConnectContentTableModel contentModel;
	
	private AdobeConnectMeeting meeting;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectShareDocumentsController(UserRequest ureq, WindowControl wControl, AdobeConnectMeeting meeting) {
		super(ureq, wControl, "share_documents");
		this.meeting = meeting;
		
		initForm(ureq);
		loadModel();
	}
	
	public AdobeConnectMeeting getMeeting() {
		return meeting;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.icon, new AdobeConnectIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.dateBegin, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.resource));

		contentModel = new AdobeConnectContentTableModel(columnsModel, getLocale());
		contentTableEl = uifactory.addTableElement(getWindowControl(), "meetingContents", contentModel, 24, false, getTranslator(), formLayout);
		contentTableEl.setCustomizeColumns(false);
		contentTableEl.setMultiSelect(true);
		contentTableEl.setEmptyTableMessageKey("no.contents");
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private void loadModel() {
		AdobeConnectErrors error = new AdobeConnectErrors();
		List<AdobeConnectSco> scos = adobeConnectManager.getRecordings(meeting, error);
		List<AdobeConnectContentRow> rows = new ArrayList<>(scos.size());
		
		for(AdobeConnectSco sco:scos) {
			rows.add(new AdobeConnectContentRow(sco));
		}
		contentModel.setObjects(rows);
		contentTableEl.reset(true, true, true);
		contentTableEl.setSelectAllEnable(true);

		List<String> sharedDocumentIds = meeting.getSharedDocumentIds();
		if(!sharedDocumentIds.isEmpty()) {
			Set<Integer> selectedRows = new HashSet<>();
			for(String scoId:sharedDocumentIds ) {
				Integer index = contentModel.indexOf(scoId);
				if(index != null) {
					selectedRows.add(index);
				}
			}
			contentTableEl.setMultiSelectedIndex(selectedRows);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Set<Integer> selectedRows = contentTableEl.getMultiSelectedIndex();
		List<AdobeConnectSco> sharedScos = new ArrayList<>(selectedRows.size());
		for(Integer selectedRow:selectedRows) {
			AdobeConnectContentRow row = contentModel.getObject(selectedRow.intValue());
			sharedScos.add(row.getSco());
		}

		meeting = adobeConnectManager.shareDocuments(meeting, sharedScos);
		dbInstance.commit();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
