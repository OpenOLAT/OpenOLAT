/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.quality.ui.ParticipationDataModel;
import org.olat.modules.quality.ui.ParticipationDataModel.ParticipationCols;
import org.olat.modules.quality.ui.ParticipationListController;
import org.olat.modules.quality.ui.ParticipationRow;
import org.olat.modules.quality.ui.QualityContextRoleRenderer;
import org.olat.modules.quality.ui.wizard.AddEmailContext.EmailIdentity;

/**
 * 
 * Initial date: Apr 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AddEmailOverviewController extends StepFormBasicController {
	
	private ParticipationDataModel dataModel;
	private FlexiTableElement tableEl;
	
	private final AddEmailContext context;
	
	public AddEmailOverviewController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));
		context = (AddEmailContext)getOrCreateFromRunContext("context", AddEmailContext::new);
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("participation.user.email.overview");
		setFormDescription("participation.user.email.overview.desc");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.firstname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.lastname));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.email));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipationCols.role, new QualityContextRoleRenderer()));
		
		dataModel = new ParticipationDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, true, getTranslator(), flc);
	}
	
	private void loadModel() {
		List<ParticipationRow> rows = new ArrayList<>();
		for (EmailIdentity emailIdentity : context.getEmailToIdentity().values()) {
			ParticipationRow row = new ParticipationRow(emailIdentity);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
