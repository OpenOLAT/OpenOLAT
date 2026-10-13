/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableReduceEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableSingleSelectionFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.olat.user.propertyhandlers.ui.UsrPropContextCfgTableModel.ContextCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Description:<br>
 * this class displays a table with all UserPropertyUsageContexts and the number
 * of active PropertyHandlers in each Context. A filter restricts the list to
 * the contexts that use one property. In that state, the contexts without the
 * property can be added.
 *
 * <P>
 * Initial Date: 29.08.2011 <br>
 *
 * @author strentini
 */
public class UsrPropContextCfgTableController extends FormBasicController {

	private static final String FILTER_PROPERTY = "property";
	private static final String FILTER_ALL = "all";
	private static final String CMD_EDIT = "edit";

	private FlexiTableElement tableEl;
	private UsrPropContextCfgTableModel tableModel;
	private FormLink addContextButton;

	private CloseableModalController cmc;
	private UsrPropContextEditController contextEditCtr;
	private UsrPropContextAddController contextAddCtr;

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	public UsrPropContextCfgTableController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer topButtonCont = FormLayoutContainer.createButtonLayout("buttons.top", getTranslator());
		topButtonCont.setElementCssClass("o_button_group o_button_group_right o_button_group_top");
		formLayout.add(topButtonCont);

		addContextButton = uifactory.addFormLink("upc.context.add", topButtonCont, Link.BUTTON);
		addContextButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		addContextButton.setVisible(false);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.description));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ContextCols.count));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("upc.edit", translate("upc.edit"), CMD_EDIT));

		tableModel = new UsrPropContextCfgTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "contexts", tableModel, 100, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		initFilters(null);
	}

	private void initFilters(String selectedName) {
		UsrPropCfgObject cfg = usrPropCfgMng.getUserPropertiesConfigObject();
		SelectionValues values = new SelectionValues();
		values.add(SelectionValues.entry(FILTER_ALL, translate("upc.filter.all.active")));
		for (UserPropertyHandler handler : cfg.getPropertyHandlers()) {
			if (cfg.isActiveHandler(handler)) {
				values.add(SelectionValues.entry(handler.getName(), getPropertyLabel(handler)));
			}
		}
		FlexiTableSingleSelectionFilter propertyFilter = new FlexiTableSingleSelectionFilter(translate("upc.filter.property"),
				FILTER_PROPERTY, values, true);
		propertyFilter.setValue(values.containsKey(selectedName) ? selectedName : FILTER_ALL);
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		filters.add(propertyFilter);
		tableEl.setFilters(true, filters, false, true);
	}

	private String getPropertyLabel(UserPropertyHandler handler) {
		String label = I18nManager.getInstance().getLocalizedString(UserPropertyHandler.class.getPackage().getName(),
				handler.i18nFormElementLabelKey(), null, getLocale(), true, true);
		return label == null ? handler.getName() : handler.getName() + " · " + label;
	}

	private UserPropertyHandler getFilteredHandler() {
		FlexiTableFilter filter = FlexiTableFilter.getFilter(tableEl.getFilters(), FILTER_PROPERTY);
		String value = filter == null ? null : filter.getValue();
		return StringHelper.containsNonWhitespace(value) && !FILTER_ALL.equals(value)
				? usrPropCfgMng.getUserPropertiesConfigObject().getPropertyHandler(value) : null;
	}

	private void loadModel() {
		UsrPropCfgObject cfg = usrPropCfgMng.getUserPropertiesConfigObject();
		UserPropertyHandler filterHandler = getFilteredHandler();
		List<UsrPropContextRow> rows = new ArrayList<>();
		cfg.getUsageContexts().forEach((name, context) -> {
			if (filterHandler == null || context.contains(filterHandler)) {
				rows.add(new UsrPropContextRow(name, context, cfg.activeHandlerCount(context)));
			}
		});
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);

		addContextButton.setVisible(filterHandler != null && rows.size() < cfg.getUsageContexts().size());
		addContextButton.setUserObject(filterHandler);
	}

	public void reload() {
		UserPropertyHandler selected = getFilteredHandler();
		initFilters(selected == null ? null : selected.getName());
		loadModel();
	}

	public void filterByProperty(String handlerName) {
		initFilters(handlerName);
		loadModel();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se && CMD_EDIT.equals(se.getCommand())) {
				doEditContext(ureq, tableModel.getObject(se.getIndex()).name());
			} else if (event instanceof FlexiTableReduceEvent) {
				loadModel();
			}
		} else if (source == addContextButton) {
			doAddContext(ureq, (UserPropertyHandler) addContextButton.getUserObject());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == contextAddCtr) {
			cmc.deactivate();
			cleanUp();
			if (event instanceof UsrPropContextAddController.ContextAddedEvent added) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
				doEditContext(ureq, added.getContextName());
			}
		} else if (source == cmc) {
			cleanUp();
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(contextEditCtr);
		removeAsListenerAndDispose(contextAddCtr);
		removeAsListenerAndDispose(cmc);
		contextEditCtr = null;
		contextAddCtr = null;
		cmc = null;
	}

	private void doEditContext(UserRequest ureq, String contextName) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contextEditCtr);

		UserPropertyUsageContext context = usrPropCfgMng.getUserPropertiesConfigObject().getUsageContexts().get(contextName);
		contextEditCtr = new UsrPropContextEditController(ureq, getWindowControl(), context, contextName);
		listenTo(contextEditCtr);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contextEditCtr.getInitialComponent(),
				true, translate("upc.context.edit") + " :: " + contextName, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddContext(UserRequest ureq, UserPropertyHandler handler) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contextAddCtr);

		contextAddCtr = new UsrPropContextAddController(ureq, getWindowControl(), handler);
		listenTo(contextAddCtr);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contextAddCtr.getInitialComponent(),
				true, translate("upc.context.add") + " :: " + handler.getName(), true);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to handle
	}
}
