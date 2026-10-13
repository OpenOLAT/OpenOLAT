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
package org.olat.user.propertyhandlers.ui;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropContextCfgTableModel.ContextCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 *
 */
public class UsrPropContextAddController extends FormBasicController {

	private static final String CMD_ADD = "add";

	private final UserPropertyHandler handler;
	private FlexiTableElement tableEl;
	private UsrPropContextCfgTableModel tableModel;

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	public UsrPropContextAddController(UserRequest ureq, WindowControl wControl, UserPropertyHandler handler) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.handler = handler;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("upc.context.add.info", new String[] { handler.getName() });

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.count));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("upc.context.add.select", translate("upc.context.add.select"), CMD_ADD));

		tableModel = new UsrPropContextCfgTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "contextsWithout", tableModel, 100, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setSearchEnabled(true);
	}

	private void loadModel() {
		UsrPropCfgObject cfg = usrPropCfgMng.getUserPropertiesConfigObject();
		String search = tableEl.getQuickSearchString();
		String needle = StringHelper.containsNonWhitespace(search) ? search.toLowerCase() : null;
		List<UsrPropContextRow> rows = new ArrayList<>();
		for (String name : cfg.contextsWithout(handler)) {
			UserPropertyUsageContext context = cfg.getUsageContexts().get(name);
			String description = context.getDescription() == null ? "" : context.getDescription();
			if (needle == null || name.toLowerCase().contains(needle) || description.toLowerCase().contains(needle)) {
				rows.add(new UsrPropContextRow(name, context, cfg.activeHandlerCount(context)));
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se && CMD_ADD.equals(se.getCommand())) {
				doAdd(ureq, tableModel.getObject(se.getIndex()));
			} else if (event instanceof FlexiTableReduceEvent) {
				loadModel();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doAdd(UserRequest ureq, UsrPropContextRow row) {
		row.context().addPropertyHandler(handler);
		if (!UsrPropCfgManager.canBeOptionalInContext(handler, row.name())) {
			row.context().setAsMandatoryUserProperty(handler, true);
		}
		usrPropCfgMng.saveUserPropertiesConfig();
		fireEvent(ureq, new ContextAddedEvent(row.name()));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to handle
	}

	public static class ContextAddedEvent extends Event {

		private static final long serialVersionUID = 7106384924815603355L;

		private final String contextName;

		public ContextAddedEvent(String contextName) {
			super("context.added");
			this.contextName = contextName;
		}

		public String getContextName() {
			return contextName;
		}
	}
}
