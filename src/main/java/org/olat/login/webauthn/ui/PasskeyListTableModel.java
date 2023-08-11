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
package org.olat.login.webauthn.ui;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.AuthenticationImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import com.webauthn4j.data.attestation.authenticator.AAGUID;

/**
 * 
 * Initial date: 11 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasskeyListTableModel extends DefaultFlexiTableDataModel<PasskeyRow> {
	
	private static final PasskeyCols[] COLS = PasskeyCols.values();
	
	public PasskeyListTableModel(FlexiTableColumnModel columnModel) {
		super(columnModel);
	}

	@Override
	public Object getValueAt(int row, int col) {
		PasskeyRow passkey = getObject(row);
		switch(COLS[col]) {
			case username: return passkey.getAuthentication().getAuthusername();
			case aaguid: return getAAGUID(passkey.getAuthentication());
			default: return "ERROR";
		}
	}
	
	
	private String getAAGUID(Authentication authentication) {
		if(authentication instanceof AuthenticationImpl auth && auth.getAaGuid() != null && auth.getAaGuid().length > 0) {
			AAGUID aaguid = new AAGUID(auth.getAaGuid());
			return aaguid.toString();
		}
		return null;
	}
	
	public enum PasskeyCols implements FlexiSortableColumnDef {
		username("table.header.username"),
		aaguid("table.header.aaguid");
		
		private final String i18nKey;
		
		private PasskeyCols(String i18nKey) {
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