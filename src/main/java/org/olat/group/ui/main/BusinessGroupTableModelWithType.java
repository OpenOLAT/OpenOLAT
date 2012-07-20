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

package org.olat.group.ui.main;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroup;

/**
 * @author gnaegi
 */
public class BusinessGroupTableModelWithType extends DefaultTableDataModel<BGTableItem> {
	private final int columnCount;
	private Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupTableModelWithType(List<BGTableItem> owned, Translator trans, int columnCount) {
		super(owned);
		this.trans = trans;
		//fxdiff VCRP-1,2: access control of resources
		this.columnCount = columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		BGTableItem wrapped = (BGTableItem)objects.get(row);
		BusinessGroup businessGroup = wrapped.getBusinessGroup();
		switch (Cols.values()[col]) {
			case name:
				String name = businessGroup.getName();
				name = StringEscapeUtils.escapeHtml(name).toString();
				return name;
			case description:
				String description = businessGroup.getDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			case groupType:
				return trans.translate(businessGroup.getType());
			case allowLeave:
				return wrapped.getAllowLeave();
			case allowDelete:
				return wrapped.getAllowDelete();
			case resources:
				return wrapped;
			//fxdiff VCRP-1,2: access control of resources
			case accessControl:
				return new Boolean(wrapped.isAccessControl());
			case accessControlLaunch:
				if(wrapped.isAccessControl()) {
					if(wrapped.isMember()) return trans.translate("select");
					return trans.translate("table.access");
				}
				return null;
			case accessTypes:
				return wrapped.getAccessTypes();
			case mark:
				return new Boolean(wrapped.isMarked());
			default:
				return "ERROR";
		}
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public Object createCopyWithEmptyList() {
		return new BusinessGroupTableModelWithType(Collections.<BGTableItem>emptyList(), trans, columnCount);
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<BGTableItem> owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the business group at the given row
	 */
	public BusinessGroup getBusinessGroupAt(int row) {
		BGTableItem wrapped = (BGTableItem)objects.get(row);
		return wrapped.getBusinessGroup();
	}
	
	public void removeBusinessGroup(BusinessGroup bg) {
		for(int i=objects.size(); i-->0; ) {
			BGTableItem wrapped = (BGTableItem)objects.get(i);
			if(bg.equals(wrapped.getBusinessGroup())) {
				objects.remove(i);
				return;
			}
		}
	}
	
	public enum Cols {
		name("table.header.bgname"),
		description("table.header.description"),
		groupType(""),
		allowLeave("table.header.leave"),
		allowDelete("table.header.delete"),
		resources("table.header.resources"),
		accessControl(""),
		accessControlLaunch("table.header.ac"),
		accessTypes("table.header.ac"),
		mark("table.header.mark");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
}