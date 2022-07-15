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
package org.olat.modules.portfolio.ui.shared;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AccessRights;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.ui.BinderController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.portfolio.ui.shared.InviteeBindersDataModel.BinderCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InviteeBindersController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private InviteeBindersDataModel tableModel;
	private final TooledStackedPanel stackedPanel;
	
	private BinderController binderCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioService portfolioService;
	
	public InviteeBindersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackedPanel) {
		super(ureq, wControl, "invitee_binders");
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		this.stackedPanel = stackedPanel;
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BinderCols.binderKey, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderCols.binderName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderCols.courseName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderCols.binderOwner));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		tableModel = new InviteeBindersDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
	}
	
	private void loadModel(String searchString) {
		List<AssessedBinder> assessedBinders = portfolioService.searchSharedBindersWith(getIdentity(), searchString);
		List<InviteeBinderRow> rows = new ArrayList<>(assessedBinders.size());
		for(AssessedBinder assessedBinder:assessedBinders) {
			String ownerFullname = userManager.getUserDisplayName(assessedBinder.getAssessedIdentity());
			rows.add(new InviteeBinderRow(assessedBinder, ownerFullname));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				InviteeBinderRow row = tableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					Activateable2 activeateable = doSelectBinder(ureq, row);
					if(activeateable != null) {
						activeateable.activate(ureq, null, null);
					}
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(se.getSearch());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private BinderController doSelectBinder(UserRequest ureq, InviteeBinderRow row) {
		Binder binder = portfolioService.getBinderByKey(row.getBinderKey());
		if(binder == null) {
			showWarning("warning.portfolio.not.found");
			return null;
		} else {
			removeAsListenerAndDispose(binderCtrl);

			portfolioService.updateBinderUserInformations(binder, getIdentity());
			OLATResourceable binderOres = OresHelper.createOLATResourceableInstance("Binder", binder.getKey());
			WindowControl swControl = addToHistory(ureq, binderOres, null);
			List<AccessRights> rights = portfolioService.getAccessRights(binder, getIdentity());
			BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getCallbackForCoach(binder, rights);
			BinderConfiguration config = BinderConfiguration.createConfig(binder);
			binderCtrl = new BinderController(ureq, swControl, stackedPanel, secCallback, binder, config);
			String displayName = StringHelper.escapeHtml(binder.getTitle());
			stackedPanel.pushController(displayName, binderCtrl);
			return binderCtrl;
		}
	}
}
