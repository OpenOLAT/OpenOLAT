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
package org.olat.course.noderight.model;

import java.io.Serializable;
import java.util.Collection;

import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant;

/**
 * 
 * Initial date: 30 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightImpl implements NodeRight, Serializable {
	
	private static final long serialVersionUID = -2747529715429247817L;
	
	private String typeIdentifier;
	private EditMode editMode;
	private Collection<NodeRightGrant> grants;
	
	@Override
	public String getTypeIdentifier() {
		return typeIdentifier;
	}
	
	public void setTypeIdentifier(String typeIdentifier) {
		this.typeIdentifier = typeIdentifier;
	}

	@Override
	public EditMode getEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(EditMode editMode) {
		this.editMode = editMode;
	}

	@Override
	public Collection<NodeRightGrant> getGrants() {
		return grants;
	}

	public void setGrants(Collection<NodeRightGrant> grants) {
		this.grants = grants;
	}

}
