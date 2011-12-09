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
*/
package org.olat.test.util.setup;

/**
 * Originally the test author could choose from a list of possible
 * setups, but later there was only one setup type used: TWO_NODE_CLUSTER. <br/>
 * It was decided that it would be wiser to run all tests with the same setup.
 * 
 * @author lavinia
 *
 */
public enum SetupType {

	/** @deprecated not supported anymore */
	CLEAN_AND_RESTARTED_SINGLE_VM,
	/** @deprecated not supported anymore */
	CLEAN_AND_RESTARTED_TWO_NODE_CLUSTER,
	/** @deprecated not supported anymore */
	RESTARTED_SINGLE_VM,
	/** @deprecated not supported anymore */
	RESTARTED_TWO_NODE_CLUSTER,
	/** @deprecated not supported anymore */
	SINGLE_VM,
	TWO_NODE_CLUSTER;
	
	/** @deprecated not supported anymore */
	public boolean isSingleVm() {
		if (this==CLEAN_AND_RESTARTED_SINGLE_VM) return true;
		if (this==RESTARTED_SINGLE_VM) return true;
		if (this==SINGLE_VM) return true;
		return false;
	}
}
