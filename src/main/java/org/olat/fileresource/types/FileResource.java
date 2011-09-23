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
* <p>
*/ 

package org.olat.fileresource.types;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;

/**
 * Initial Date:  Apr 8, 2004
 *
 * @author Mike Stock
 */
public class FileResource implements OLATResourceable {

	/**
	 * Generic file resource type identifier.
	 */
	public static final String GENERIC_TYPE_NAME = "FileResource.FILE";
	private String typeName;
	private Long typeId;
	
	/**
	 * 
	 */
	public FileResource() {
		typeName = GENERIC_TYPE_NAME;
		typeId = new Long(CodeHelper.getForeverUniqueID());
	}

	 /**
	 * User by subtypes to set appropriate ResourceableTypeName
	 * @param newTypeName
	 */
	protected void setTypeName(String newTypeName) { typeName = newTypeName; }

	/**
	 * Only used internally when switching subtypes.
	 * @param newId
	 */
	public void overrideResourceableId(Long newId) { typeId = newId; }
	
	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableTypeName()
	 */
	public String getResourceableTypeName() {
		return typeName;
	}

	/**
	 * @see org.olat.core.id.OLATResourceablegetResourceableId()
	 */
	public Long getResourceableId() {
		return typeId;
	}

}
