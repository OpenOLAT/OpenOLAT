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

package org.olat.resource;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;

/**
 * A <b>OLATResourceImpl</b> is 
 * 
 * @author Andreas
 *
 */
public class OLATResourceImpl extends PersistentObject implements OLATResource {

	private static final long serialVersionUID = 4797534778467150679L;

	/** for mysql, need always to provide a type and a key to allow a composite index, so 0 is
	 * a reserved key meaning "no key"
	 */
	public static final Long NULLVALUE = Long.valueOf(0l);

	private String resName;
	private Long resId;

	/**
	* Constructor needed for Hibernate.
	*/
	protected OLATResourceImpl() {
		// singleton
	}

	OLATResourceImpl(Long id, String typeName) {
		if (id == null) id = NULLVALUE;
		resId = id;
		resName = typeName;
	}

	OLATResourceImpl(OLATResourceable resourceable) {
		Long id = resourceable.getResourceableId();
		if (id == null) id = NULLVALUE;
		resId = id;
		resName = resourceable.getResourceableTypeName();
	}


	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return getResName();
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		Long val = getResId();
		if (val == null) throw new AssertException("hibernate should never set id to null, but to zero instead");
		return val.equals(NULLVALUE) ? null : val;
	}

	/**
	 * for hibernate only
	 * @param id
	 */
	private void setResId(Long id) {
		resId = id;
	}

	/**
	 * for hibernate only
	 * @return Long
	 */
	private Long getResId() {
		return resId;
	}

	/**
	 * for hibernate only
	 * @return String
	 */
	private String getResName() {
		return resName;
	}

	/**
	 * for hibernate only
	 * @param typeName
	 */
	private void setResName(String typeName) {
		resName = typeName;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder desc =
			new StringBuilder()
				.append(" OLATResource(")
				.append(this.getKey())
				.append(")[")
				.append(this.getResourceableTypeName())
				.append("(")
				.append(this.getResourceableId())
				.append(")")
				.append("], ");
		return desc.toString() + super.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 9734598 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OLATResource) {
			OLATResource resource = (OLATResource)obj;
			return getKey() != null && getKey().equals(resource.getKey());	
		}
		return false;
	}
}