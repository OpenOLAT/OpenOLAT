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
package org.olat.course.noderight;

import java.util.Collection;

import org.olat.basesecurity.IdentityRef;
import org.olat.course.noderight.NodeRight.EditMode;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface NodeRightService {
	
	public NodeRight getRight(ModuleConfiguration moduleConfig, NodeRightType type);
	
	public void setRight(ModuleConfiguration moduleConfig, NodeRight nodeRight);
	
	public boolean isGranted(ModuleConfiguration moduleConfig, UserCourseEnvironment userCourseEnv, NodeRightType type);

	public NodeRight clone(NodeRight nodeRight);

	public void setEditMode(NodeRight nodeRight, EditMode mode);
	
	public NodeRightGrant createGrant(NodeRightRole role);
	
	public NodeRightGrant createGrant(IdentityRef identityRef);
	
	public NodeRightGrant createGrant(BusinessGroupRef businessGroupRef);
	
	public void  setRoleGrants(NodeRight nodeRight, Collection<NodeRightRole> roles);

	public void addGrants(NodeRight nodeRight, Collection<NodeRightGrant> grants);

	public void removeGrant(NodeRight nodeRight, NodeRightGrant grant);
	
	boolean isSame(NodeRightGrant grant1, NodeRightGrant grant2);

}
