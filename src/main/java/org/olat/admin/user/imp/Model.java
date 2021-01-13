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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.user.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.admin.user.imp.ImportStep01.ImportStepForm01;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial Date: 2005
 * 
 * @author Felix Jost, Roman Haag
 * 
 * Description: Table model for user mass import.
 */
public class Model extends DefaultFlexiTableDataModel<Identity> {
	
	private static final ModelCols[] COLS = ModelCols.values();

	private final Locale locale;
	private final List<UserPropertyHandler> userPropertyHandlers;

	public Model(List<Identity> objects, FlexiTableColumnModel columnModel,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
		setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity ident = getObject(row);
		if(col < COLS.length) {
			boolean newUser = (ident instanceof TransientIdentity);
			switch(COLS[col]) {
				case newUser: return Boolean.valueOf(newUser);
				case login: return ident.getName();
				case cred: return newUser ? previewCred((TransientIdentity)ident) : "-";
				case lang: return newUser ? ((TransientIdentity)ident).getLanguage() : ident.getUser().getPreferences().getLanguage(); 
				case expiration: return ident.getExpirationDate();
				default: return "ERROR";
			}
		}
		
		int propCol = col - ImportStepForm01.USER_PROPS_OFFSET;
		if (propCol >= 0 && propCol < userPropertyHandlers.size()) {
			// get user property for this column for an already existing user
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(propCol);
			String value = userPropertyHandler.getUserProperty(ident.getUser(), locale);
			return (value == null ? "n/a" : value);
		}
		return "ERROR";
	}
	
	private String previewCred(TransientIdentity ident) {
		return ident.getPassword() == null ? "-" : "***";
	}

	@Override
	public DefaultFlexiTableDataModel<Identity> createCopyWithEmptyList() {
		return new Model(new ArrayList<>(), getTableColumnModel(), userPropertyHandlers, locale);
	}
	
	
	public enum ModelCols implements FlexiSortableColumnDef {
		newUser("table.user.existing"),
		login("table.user.login"),
		cred("table.user.pwd"),
		lang("table.user.lang"),
		expiration("table.user.expiration");
		
		private final String i18nKey;
		
		private ModelCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
