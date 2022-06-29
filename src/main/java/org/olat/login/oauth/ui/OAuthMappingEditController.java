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
package org.olat.login.oauth.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthMapping;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.model.OAuthAttributeMapping;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 20 juin 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthMappingEditController extends FormBasicController {
	
	private int counter = 0;
	private final OAuthSPI spi;
	private final OAuthMapping mapping;
	private final SelectionValues userProperties;
	
	private FormLink addAttributeButton;
	private FlexiTableElement tableEl;
	private OAuthMappingTableModel tableModel;

	@Autowired
	private UserManager userManager;
	@Autowired
	private OAuthLoginModule oauthModule;
	
	public OAuthMappingEditController(UserRequest ureq, WindowControl wControl, OAuthSPI spi, OAuthMapping mapping) {
		super(ureq, wControl, "mapping_editor");
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		this.spi = spi;
		this.mapping = mapping;
		userProperties = new SelectionValues();

		userProperties.add(SelectionValues.entry("id", translate("mapping.id")));
		userProperties.add(SelectionValues.entry("lang", translate("mapping.lang")));
		
		List<UserPropertyHandler> handlers = userManager.getUserPropertyHandlersFor(OAuthRegistrationController.USERPROPERTIES_FORM_IDENTIFIER, true);
		for(UserPropertyHandler handler:handlers) {
			String propertyName = handler.getName();
			if(OAuthUser.availableAttributes.contains(propertyName)) {
				String label = translate(handler.i18nFormElementLabelKey());
				userProperties.add(SelectionValues.entry(propertyName, label));
			} else {
				getLogger().warn("Attribute not supported by OAuth implementation: {}", propertyName);
			}
		}

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MappingCols.external));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MappingCols.openolat));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", translate("remove"), "remove"));
		tableModel = new OAuthMappingTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		addAttributeButton = uifactory.addFormLink("add.attribute", formLayout, Link.BUTTON);
		uifactory.addFormSubmitButton("save", formLayout);
	}
	
	private void loadModel() {
		List<OAuthAttributeMapping> mappings = mapping.getMapping();
		List<OAuthMappingRow> rows = new ArrayList<>();
		if(mappings != null) {
			for(OAuthAttributeMapping map:mappings) {
				OAuthMappingRow row = forgeRow(map);
				rows.add(row);
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addAttributeButton == source) {
			doAddAttribute();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<OAuthAttributeMapping> mappings = new ArrayList<>();
		
		List<OAuthMappingRow> rows = tableModel.getObjects();
		for(OAuthMappingRow row:rows) {
			OAuthAttributeMapping mapping = row.getAttributeMapping();
			if(mapping != null) {
				mappings.add(mapping);
			}
		}

		oauthModule.setMapping(spi, mappings);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doAddAttribute() {
		OAuthAttributeMapping attribute = new OAuthAttributeMapping("", "");
		OAuthMappingRow row = forgeRow(attribute);
		
		List<OAuthMappingRow> rows = tableModel.getObjects();
		rows.add(row);
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private OAuthMappingRow forgeRow(OAuthAttributeMapping attribute) {
		TextElement externalEl = uifactory.addTextElement("external." + (counter++), null, 128, attribute.getExternalAttribute(), flc);

		SingleSelection openolatEl = uifactory.addDropdownSingleselect("openolat." + (counter++), null, flc, userProperties.keys(), userProperties.values());
		if(userProperties.containsKey(attribute.getOpenolatAttribute())) {
			openolatEl.select(attribute.getOpenolatAttribute(), true);
		}
		return new OAuthMappingRow(externalEl, openolatEl);
	}
	
	private static class OAuthMappingTableModel extends DefaultFlexiTableDataModel<OAuthMappingRow> {
		
		private static final MappingCols[] COLS = MappingCols.values();
		
		private OAuthMappingTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			OAuthMappingRow mappingRow = getObject(row);
			switch(COLS[col]) {
				case external: return  mappingRow.getExternalEl();
				case openolat: return mappingRow.getOpenolatEl();
				default: return "ERROR";
			}
		}
	}
	
	public enum MappingCols implements FlexiSortableColumnDef {
		external("table.header.external"),
		openolat("table.header.openolat");
		
		private final String i18nKey;
		
		private MappingCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	public static class OAuthMappingRow {
		
		private final TextElement externalEl;
		private final SingleSelection openolatEl;
		
		public OAuthMappingRow(TextElement externalEl, SingleSelection openolatEl) {
			this.externalEl = externalEl;
			this.openolatEl = openolatEl;
		}

		public OAuthAttributeMapping getAttributeMapping() {
			if(StringHelper.containsNonWhitespace(externalEl.getValue()) && openolatEl.isOneSelected()) {
				return new OAuthAttributeMapping(externalEl.getValue(), openolatEl.getSelectedKey());
			}
			return null;
		}

		public TextElement getExternalEl() {
			return externalEl;
		}

		public SingleSelection getOpenolatEl() {
			return openolatEl;
		}
	}
}
