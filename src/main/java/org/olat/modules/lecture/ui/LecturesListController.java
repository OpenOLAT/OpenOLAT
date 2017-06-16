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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.ui.LecturesListDataModel.StatsCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private LecturesListDataModel tableModel;
	
	private final String propsIdentifier;
	private final boolean authorizedAbsenceEnabled;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<LectureBlockIdentityStatistics> statistics;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	
	public LecturesListController(UserRequest ureq, WindowControl wControl,
			List<LectureBlockIdentityStatistics> statistics,
			List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier) {
		super(ureq, wControl, "lectures_coaching");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.statistics = statistics;
		this.propsIdentifier = propsIdentifier;
		this.userPropertyHandlers = userPropertyHandlers;
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, StatsCols.id));
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(propsIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, null,
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.entry));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.plannedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.attendedLectures));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.absentLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(StatsCols.authorizedAbsenceLectures));
		}
		
		tableModel = new LecturesListDataModel(columnsModel); 
		tableModel.setObjects(statistics);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
