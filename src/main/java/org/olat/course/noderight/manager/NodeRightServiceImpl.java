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
package org.olat.course.noderight.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.id.Identity;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRight.EditMode;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.model.NodeRightGrantImpl;
import org.olat.course.noderight.model.NodeRightImpl;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupRef;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.modules.ModuleConfiguration;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class NodeRightServiceImpl implements NodeRightService {
	
	private static final String KEY_PREFIX = "node.right.";

	@Override
	public NodeRight getRight(ModuleConfiguration moduleConfig, NodeRightType type) {
		String key = KEY_PREFIX + type.getIdentifier();
		return moduleConfig.has(key)
				? (NodeRight)moduleConfig.get(key)
				: clone(type.getDefaultRight());
	}

	@Override
	public void setRight(ModuleConfiguration moduleConfig, NodeRight nodeRight) {
		String key = KEY_PREFIX + nodeRight.getTypeIdentifier();
		moduleConfig.set(key, nodeRight);
	}

	@Override
	public boolean isGranted(ModuleConfiguration moduleConfig, UserCourseEnvironment userCourseEnv, NodeRightType type) {
		NodeRight right = getRight(moduleConfig, type);
		return isGranted(right, userCourseEnv);
	}

	boolean isGranted(NodeRight right, UserCourseEnvironment userCourseEnv) {
		for (NodeRightGrant grant : right.getGrants()) {
			if (isInPeriod(grant, new Date())) {
				if (NodeRightRole.owner == grant.getRole() && userCourseEnv.isAdmin()) {
					return true;
				}
				if (NodeRightRole.coach == grant.getRole() && userCourseEnv.isCoach()) {
					return true;
				}
				if (NodeRightRole.participant == grant.getRole() && userCourseEnv.isParticipant()) {
					return true;
				}
				if (NodeRightRole.guest == grant.getRole() && userCourseEnv.getIdentityEnvironment().getRoles().isGuestOnly()) {
					return true;
				}
				Identity identity = userCourseEnv.getIdentityEnvironment().getIdentity();
				if (grant.getIdentityRef() != null && grant.getIdentityRef().getKey().equals(identity.getKey())) {
					return true;
				}
				if (grant.getBusinessGroupRef() != null) {
					CourseGroupManager courseGroupManager = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
					Long groupKey = grant.getBusinessGroupRef().getKey();
					boolean groupLinktToCourse = courseGroupManager.getAllBusinessGroups().stream()
							.anyMatch(group -> group.getKey().equals(groupKey));
					if (groupLinktToCourse && courseGroupManager.isIdentityInGroup(identity, groupKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean isInPeriod(NodeRightGrant grant, Date dueDate) {
		if (grant.getStart() != null && grant.getStart().after(dueDate)) {
			return false;
		}
		if (grant.getEnd() != null && grant.getEnd().before(dueDate)) {
			return false;
		}
		
		return true;
	}

	@Override
	public NodeRight clone(NodeRight nodeRight) {
		NodeRightImpl clone = new NodeRightImpl();
		clone.setTypeIdentifier(nodeRight.getTypeIdentifier());
		clone.setEditMode(nodeRight.getEditMode());
		
		Collection<NodeRightGrant> grants = nodeRight.getGrants();
		List<NodeRightGrant> clonedGrants = new ArrayList<>(grants.size());
		for (NodeRightGrant grant : grants) {
			NodeRightGrantImpl clonedGrant = new NodeRightGrantImpl();
			clonedGrant.setRole(grant.getRole());
			if (grant.getIdentityRef() != null) {
				clonedGrant.setIdentityRef(new IdentityRefImpl(grant.getIdentityRef().getKey()));
			}
			if (grant.getBusinessGroupRef() != null) {
				clonedGrant.setBusinessGroupRef(new BusinessGroupRefImpl(grant.getBusinessGroupRef().getKey()));	
			}
			clonedGrant.setStart(grant.getStart());
			clonedGrant.setEnd(grant.getEnd());
			clonedGrants.add(clonedGrant);
		}
		clone.setGrants(clonedGrants);
		
		return clone;
	}

	@Override
	public void setEditMode(NodeRight nodeRight, EditMode mode) {
		nodeRight.setEditMode(mode);
		if (EditMode.regular == mode) {
			nodeRight.getGrants().removeIf(this::isNotRoleGrant);
			nodeRight.getGrants().stream().forEach(this::removeDates);
			removeDuplicatGrants(nodeRight);
		}
	}
	
	private boolean isNotRoleGrant(NodeRightGrant grant) {
		return grant.getRole() == null;
	}
	
	private void removeDates(NodeRightGrant grant) {
		grant.setStart(null);
		grant.setEnd(null);
	}
	
	@Override
	public NodeRightGrant createGrant(NodeRightRole role) {
		return createGrant(role, null, null);
	}
	
	@Override
	public NodeRightGrant createGrant(IdentityRef identityRef) {
		return createGrant(null, identityRef, null);
		
	}
	
	@Override
	public NodeRightGrant createGrant(BusinessGroupRef businessGroupRef) {
		return createGrant(null, null, businessGroupRef);
	}

	NodeRightGrant createGrant(NodeRightRole role, IdentityRef identityRef, BusinessGroupRef businessGroupRef) {
		NodeRightGrantImpl grant = new NodeRightGrantImpl();
		grant.setRole(role);
		grant.setIdentityRef(identityRef);
		grant.setBusinessGroupRef(businessGroupRef);
		return grant;
	}

	@Override
	public void setRoleGrants(NodeRight nodeRight, Collection<NodeRightRole> roles) {
		nodeRight.getGrants().clear();
		Collection<NodeRightGrant> roleGrants = roles.stream().map(this::createGrant).collect(Collectors.toList());
		addGrants(nodeRight, roleGrants);
	}
	
	@Override
	public void addGrants(NodeRight nodeRight, Collection<NodeRightGrant> grants) {
		nodeRight.getGrants().addAll(grants);
		removeDuplicatGrants(nodeRight);
	}
	
	private void removeDuplicatGrants(NodeRight nodeRight) {
		Collection<NodeRightGrant> grants = nodeRight.getGrants();
		Collection<NodeRightGrant> unique = new ArrayList<>(grants.size());
		
		for (NodeRightGrant grant : grants) {
			if (!contains(unique, grant)) {
				unique.add(grant);
			}
		}
		
		grants.clear();
		grants.addAll(unique);
	}

	private boolean contains(Collection<NodeRightGrant> grants, NodeRightGrant grant) {
		return grants.stream().filter(existingGrant -> isSame(existingGrant, grant)).findAny().isPresent();
	}

	boolean isSame(NodeRightGrant grant1, NodeRightGrant grant2) {
		if (!Objects.equals(grant1.getStart(), grant2.getStart())) return false;
		if (!Objects.equals(grant1.getEnd(), grant2.getEnd())) return false;
		if (!Objects.equals(grant1.getRole(), grant2.getRole())) return false;
		
		Long identityKey1 = grant1.getIdentityRef() != null? grant1.getIdentityRef().getKey(): null;
		Long identityKey2 = grant2.getIdentityRef() != null? grant2.getIdentityRef().getKey(): null;
		if (!Objects.equals(identityKey1, identityKey2)) return false;
		
		Long groupKey1 = grant1.getBusinessGroupRef() != null? grant1.getBusinessGroupRef().getKey(): null;
		Long groupKey2 = grant2.getBusinessGroupRef() != null? grant2.getBusinessGroupRef().getKey(): null;
		return Objects.equals(groupKey1, groupKey2);
	}

	@Override
	public void removeGrant(NodeRight nodeRight, NodeRightGrant grant) {
		nodeRight.getGrants().remove(grant);
	}

}
