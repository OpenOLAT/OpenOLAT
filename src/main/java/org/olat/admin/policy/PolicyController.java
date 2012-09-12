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

package org.olat.admin.policy;

import java.util.List;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Policy;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;

/**
 * 
 * Description:<br>
 * TODO: Class Description for PolicyController
 * 
 */
public class PolicyController extends BasicController {
	
	private TableController tableCtr;
	
	
	/**
	 * caller of this constructor must make sure only olat admins come here
	 * 
	 * @param ureq
	 * @param wControl
	 * @param identity
	 */
	public PolicyController(UserRequest ureq, WindowControl wControl, Identity identity) { 
		super(ureq, wControl);
		
		List<Policy> entries = BaseSecurityManager.getInstance().getPoliciesOfIdentity(identity);
		TableDataModel<Policy> tdm = new PolicyTableDataModel(entries);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableCtr);
		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.secgroup", 0, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.policyimpl", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.resourceimplkey", 2, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.resourceimpltype", 3, null, ureq.getLocale()));
		tableCtr.setTableDataModel(tdm);
		
		putInitialPanel(tableCtr.getInitialComponent());
	}

	public void event(UserRequest ureq, Component source, Event event) {
		// no component events to listen to
	}
	
	protected void doDispose() {
		//
	}
	
	private static class PolicyTableDataModel extends DefaultTableDataModel<Policy> {
		
		public PolicyTableDataModel(List<Policy> entries) {
			super(entries);
		}
		
		public int getColumnCount() {
			// group key, permission, resource
			return 4;
		}

		public final Object getValueAt(int row, int col) {
			Policy o = getObject(row);
			switch(col) {
				case 0: return o.getSecurityGroup().getKey();
				case 1: return o.getPermission();
				case 2: return o.getOlatResource().getResourceableId();
				case 3: return o.getOlatResource().getResourceableTypeName();
			}
			return o;
		}
	}
}