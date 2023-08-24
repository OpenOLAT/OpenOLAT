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
package org.olat.modules.cemedia.ui.component;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.ui.MediaShareRow;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRelationsCellRenderer extends IconCssCellRenderer {
	
	private final UserManager userManager;

	public MediaRelationsCellRenderer(UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	protected String getIconCssClass(Object val) {
		if(val instanceof MediaShareRow row) {
			return getIconCssClass(row.getType());
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		if(val instanceof MediaShareRow row) {
			return row.getDisplayName();
		}
		return null;
	}
	
	public String getIconCssClass(MediaShare share) {
		return getIconCssClass(share.getType());
	}
	
	public String getIconCssClass(MediaToGroupRelationType type) {
		switch(type) {
			case USER: return "o_icon o_icn_fw o_icon_user";
			case BUSINESS_GROUP: return "o_icon o_icn_fw o_icon_group";
			case ORGANISATION: return "o_icon o_icn_fw o_icon_group";
			case REPOSITORY_ENTRY: return "o_icon o_icn_fw o_CourseModule_icon";
			default: return null;
		}
	}
	
	public String getDisplayName(MediaShare share) {
		switch(share.getType()) {
			case USER: return userManager.getUserDisplayName(share.getUser());
			case BUSINESS_GROUP: return share.getBusinessGroup().getName();
			case ORGANISATION: return share.getOrganisation().getDisplayName();
			case REPOSITORY_ENTRY: return share.getRepositoryEntry().getDisplayname();
			default: return null;
		}
	}
}
