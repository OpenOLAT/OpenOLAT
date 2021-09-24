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

package org.olat.group.ui.area;

import java.util.List;

import org.olat.core.gui.components.choice.ChoiceModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * Description:<BR>
 * Initial Date: Aug 30, 2004
 * 
 * @author gnaegi
 */
public class GroupsToAreaDataModel implements ChoiceModel<BusinessGroup> {
	
	private final Translator translator;
	private final List<BusinessGroup> allGroups;
	private final List<BusinessGroup> inAreaGroups;
	private final List<BusinessGroup> repoGroups;

	/**
	 * Constructor for the GroupsToAreaDataModel
	 * 
	 * @param allGroups All available groups
	 * @param inAreaGroups All groups that are associated to the group area. The
	 *          checked rows.
	 */
	public GroupsToAreaDataModel(List<BusinessGroup> allGroups, List<BusinessGroup> repoGroups,
			List<BusinessGroup> inAreaGroups, Translator translator) {
		this.allGroups = allGroups;
		this.inAreaGroups = inAreaGroups;
		this.repoGroups = repoGroups;
		this.translator = translator;
	}

	@Override
	public int getRowCount() {
		return allGroups == null ? 0 : allGroups.size();
	}

	@Override
	public Boolean isEnabled(int row) {
		return inAreaGroups.contains(getObject(row)) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public String getLabel(int row) {
		BusinessGroup group = getObject(row);
		String label = StringHelper.escapeHtml(group.getName());
		if(inAreaGroups.contains(group) && !repoGroups.contains(group)) {
			String explain = translator.translate("");
			label += " <span title='" + explain + "'><i class='o_icon o_icon_warn'> </i></span>";
		}
		return label;
	}

	@Override
	public boolean isDisabled(int row) {
		return false;
	}

	@Override
	public BusinessGroup getObject(int row) {
		return allGroups.get(row);
	}
}
