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
package org.olat.core.gui.components.form.flexible.impl.elements.table.filter;

import java.util.Collection;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 12 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MultiSelectionController extends AbstractMultiSelectionController {

	public MultiSelectionController(UserRequest ureq, WindowControl wControl, SelectionValues availableValues,
			Collection<String> preselectedKeys) {
		super(ureq, wControl, availableValues, preselectedKeys);
	}

	@Override
	protected boolean isClearLink() {
		return false;
	}

	@Override
	protected Event createChangedEvent(Set<String> selectedKeys) {
		return new KeysSelectedEvent(selectedKeys);
	}
	
	public static final class KeysSelectedEvent extends Event {

		private static final long serialVersionUID = 2195120197173429621L;
		
		private final Set<String> selectedKeys;

		public KeysSelectedEvent(Set<String> selectedKeys) {
			super("keys-selected");
			this.selectedKeys = selectedKeys;
		}

		public Set<String> getSelectedKeys() {
			return selectedKeys;
		}
		
	}

}
