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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 14 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiFilterMultiSelectionController extends AbstractMultiSelectionController {
	
	private final FlexiTableMultiSelectionFilter filter;

	public FlexiFilterMultiSelectionController(UserRequest ureq, WindowControl wControl,
			FlexiTableMultiSelectionFilter filter, List<String> preselectedKeys) {
		super(ureq, wControl, filter.getSelectionValues(), preselectedKeys);
		this.filter = filter;
	}

	@Override
	protected boolean isClearLink() {
		return true;
	}
	
	@Override
	protected Event createChangedEvent(Set<String> selectedKeys) {
		return new ChangeValueEvent(filter, selectedKeys != null? new ArrayList<>(selectedKeys): null);
	}

}
