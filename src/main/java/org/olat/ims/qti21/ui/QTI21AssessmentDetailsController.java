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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.net.URI;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.QTI21TestSessionTableModel.TSCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentDetailsController extends FormBasicController {

	private FlexiTableElement tableEl;
	private QTI21TestSessionTableModel tableModel;
	
	private Identity assessedIdentity;
	private RepositoryEntry entry;
	private final String subIdent;
	
	private CloseableModalController cmc;
	private AssessmentResultController resultCtrl;
	
	@Autowired
	protected QTI21Service qtiService;
	
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry assessableEntry, String subIdent, Identity assessedIdentity) {
		super(ureq, wControl, "assessment_details");
		
		entry = assessableEntry;
		this.subIdent = subIdent;
		this.assessedIdentity = assessedIdentity;

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.lastModified.i18nKey(), TSCols.lastModified.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.results.i18nKey(), TSCols.results.ordinal(), "result",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(TSCols.results.i18nKey()), "result"), null)));

		tableModel = new QTI21TestSessionTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", tableModel, 20, false, getTranslator(), formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	protected void updateModel() {
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(entry, subIdent, assessedIdentity);
		tableModel.setObjects(sessions);
		tableEl.reset();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(resultCtrl);
		removeAsListenerAndDispose(cmc);
		resultCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentTestSession row = tableModel.getObject(se.getIndex());
				if("result".equals(cmd)) {
					doOpenResult(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doOpenResult(UserRequest ureq, AssessmentTestSession session) {
		if(resultCtrl != null) return;

		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		String mapperUri = registerCacheableMapper(null, "QTI21Resources::" + session.getTestEntry().getKey(), new ResourcesMapper(assessmentObjectUri));
		
		resultCtrl = new AssessmentResultController(ureq, getWindowControl(), assessedIdentity, session,
				ShowResultsOnFinish.details, fUnzippedDirRoot, mapperUri);
		listenTo(resultCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", resultCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}
}