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

package org.olat.group;


/**
 * 
 * @author ChristianGuretzki
 */

public class DeletableReference {

	private String name;
	private boolean isReferenced;
	
	private DeletableReference() {
		this.isReferenced = false;
	}

	private DeletableReference(String name) {
		this.name = name;
		this.isReferenced = true;
	}

	public static DeletableReference createNoDeletableReference() {
		return new DeletableReference();
	}

	public static DeletableReference createDeletableReference(String name) {
		return new DeletableReference(name);
	}

	public String getName() {
		return name;
	}

	public boolean isReferenced() {
		return isReferenced;
	}
	
}


