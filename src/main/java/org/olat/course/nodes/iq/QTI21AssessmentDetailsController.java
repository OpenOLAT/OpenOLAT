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
package org.olat.course.nodes.iq;

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
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.QTI21TestSessionTableModel.TSCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.UserTestSession;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentDetailsController extends FormBasicController {

	private FlexiTableElement tableEl;
	private QTI21TestSessionTableModel tableModel;
	
	private Identity assessedIdentity;
	private RepositoryEntry courseEntry;
	private IQTESTCourseNode courseNode;
	
	private CloseableModalController cmc;
	private AssessmentResultController resultCtrl;
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnvironment, IQTESTCourseNode courseNode) {
		super(ureq, wControl, "assessment_details");
		
		this.courseNode = courseNode;
		assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		courseEntry = userCourseEnvironment.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		
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
	
	private void updateModel() {
		List<UserTestSession> sessions = qtiService.getUserTestSessions(courseEntry, courseNode.getIdent(), assessedIdentity);
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
				UserTestSession row = tableModel.getObject(se.getIndex());
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

	private void doOpenResult(UserRequest ureq, UserTestSession row) {
		if(resultCtrl != null) return;
		
		resultCtrl = new AssessmentResultController(ureq, getWindowControl());
		listenTo(resultCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", resultCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}
}