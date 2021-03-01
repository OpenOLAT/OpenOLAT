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
package org.olat.course.noderight.ui;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightType;

/**
 * 
 * Initial date: 3 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightWrapper {
	
	private NodeRightType type;
	private NodeRight nodeRight;
	private FormLayoutContainer container;
	private MultipleSelectionElement rolesEl;
	private FormLink addRolesLink;
	private FlexiTableElement tableEl;
	private NodeRightGrantDataModel dataModel;
	
	public NodeRightType getType() {
		return type;
	}

	public void setType(NodeRightType type) {
		this.type = type;
	}

	public NodeRight getNodeRight() {
		return nodeRight;
	}

	public void setNodeRight(NodeRight nodeRight) {
		this.nodeRight = nodeRight;
	}

	public FormLayoutContainer getContainer() {
		return container;
	}
	
	public void setContainer(FormLayoutContainer container) {
		this.container = container;
	}
	
	public MultipleSelectionElement getRolesEl() {
		return rolesEl;
	}

	public void setRolesEl(MultipleSelectionElement rolesEl) {
		this.rolesEl = rolesEl;
	}

	public FormLink getAddRolesLink() {
		return addRolesLink;
	}

	public void setAddRolesLink(FormLink addRolesLink) {
		this.addRolesLink = addRolesLink;
	}

	public FlexiTableElement getTableEl() {
		return tableEl;
	}
	
	public void setTableEl(FlexiTableElement tableEl) {
		this.tableEl = tableEl;
	}
	
	public NodeRightGrantDataModel getDataModel() {
		return dataModel;
	}
	
	public void setDataModel(NodeRightGrantDataModel dataModel) {
		this.dataModel = dataModel;
	}
}
