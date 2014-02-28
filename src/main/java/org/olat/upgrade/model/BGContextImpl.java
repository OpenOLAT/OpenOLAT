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

package org.olat.upgrade.model;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.logging.AssertException;

/**
 * Description:<BR>
 * Hibernate implementation of the business group context
 * <P>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public class BGContextImpl extends PersistentObject {
	private static final long serialVersionUID = -8242401346721569801L;

	private static final int GROUPTYPE_MAXLENGTH = 15;

	private String name;
	private String description;
	private String groupType;
	private SecurityGroup ownerGroup;
	private boolean defaultContext;

	/**
	 * Constructor used by hibernate
	 */
	protected BGContextImpl() {
	// nothing to be declared
	}

	/**
	 * @param name The name of the group context
	 * @param description The description of the group context
	 * @param ownerGroup Group that has administrative rights to edit this group
	 *          context
	 * @param groupType The type of groups allowed in this group context
	 * @param defaultContext true if this is a context of type default - only one
	 *          resouce is associated
	 */
	protected BGContextImpl(String name, String description, SecurityGroup ownerGroup, String groupType, boolean defaultContext) {
		setName(name);
		setDescription(description);
		setOwnerGroup(ownerGroup);
		setGroupType(groupType);
		setDefaultContext(defaultContext);
	}

	/**
	 * @see org.olat.group.context.BGContext#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @see org.olat.group.context.BGContext#getDescription()
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * @see org.olat.group.context.BGContext#setDescription(java.lang.String)
	 */
	public void setDescription(String string) {
		this.description = string;
	}

	/**
	 * @see org.olat.group.context.BGContext#setName(java.lang.String)
	 */
	public void setName(String string) {
		this.name = string;
	}

	/**
	 * @see org.olat.group.context.BGContext#getOwnerGroup()
	 */
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	protected void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	/**
	 * @see org.olat.group.context.BGContext#getGroupType()
	 */
	public String getGroupType() {
		return this.groupType;
	}

	protected void setGroupType(String groupType) {
		if (groupType.length() > GROUPTYPE_MAXLENGTH) throw new AssertException("grouptype of o_gp_bgcontext too long");
		this.groupType = groupType;
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return BGContextImpl.class.getName();
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return getKey();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "name=" + name + "::" + super.toString();
	}

	/**
	 * @see org.olat.group.context.BGContext#isDefaultContext()
	 */
	public boolean isDefaultContext() {
		return defaultContext;
	}

	/**
	 * @see org.olat.group.context.BGContext#setDefaultContext(boolean)
	 */
	public void setDefaultContext(boolean defaultContext) {
		this.defaultContext = defaultContext;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 836785 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGContextImpl) {
			BGContextImpl ctx = (BGContextImpl)obj;
			return equalsByPersistableKey(ctx);
		}
		return false;
	}
}
