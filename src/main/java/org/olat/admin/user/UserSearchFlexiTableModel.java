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
package org.olat.admin.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.id.Identity;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class UserSearchFlexiTableModel extends DefaultFlexiTableDataModel<Identity> implements SortableFlexiTableDataModel<Identity> {
	private Locale locale;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	public UserSearchFlexiTableModel(List<Identity> identities, List<UserPropertyHandler> userPropertyHandlers,
			Locale locale, FlexiTableColumnModel columnModel) {
		super(identities, columnModel);
		this.locale = locale;
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity identity = getObject(row);
		return getValueAt(identity, col);
	}

	@Override
	public Object getValueAt(Identity identity, int col) {
		if(col >= 0 && col < userPropertyHandlers.size()) {
			UserPropertyHandler handler = userPropertyHandlers.get(col);
			return handler.getUserProperty(identity.getUser(), locale);
		}
		return "";
	}
	
	public List<Identity> getObjects(final Set<Integer> objectMarkers) {
		List<Identity> results = new ArrayList<>();
		for(Integer objectMarker:objectMarkers) {
			results.add(getObject(objectMarker.intValue()));
		}
		return results;
	}

	@Override
	public void sort(SortKey orderBy) {
		if (orderBy != null) {
			List<Identity> users = new UserSearchFlexiTableModel.UserSearchTableDataModelSorterDelegate(orderBy, this, locale).sort();
			super.setObjects(users);
		}
	}

	public static class UserSearchTableDataModelSorterDelegate extends SortableFlexiTableModelDelegate<Identity> {

		public UserSearchTableDataModelSorterDelegate(SortKey orderBy, UserSearchFlexiTableModel model, Locale locale) {
			super(orderBy, model, locale);
		}
	}
}
