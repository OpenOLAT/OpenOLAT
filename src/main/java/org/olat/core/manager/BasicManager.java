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
package org.olat.core.manager;

import org.olat.core.logging.LogDelegator;

/**
 * <h3>Description:</h3>
 * The BasicManager provides common methods used in a manager like logging. A
 * manager implements the business logic on the data layer. It provides methods
 * that are used by controllers or other managers to build workflows or to
 * implement complex business rules where multiple managers are involved.
 * <p>
 * A manager should be state-less and be implemented as a singleton. In rare
 * situations it can make sense to have state-full managers that feature a cache
 * or something. In those cases you should implement a destroy method and call
 * this method from spring. An alternative to state-full managers
 * is to externalize the statefull elements to dedicated objects and access them
 * via the component module.
 * <p>
 * If your manager needs initialization, it should be initialized by spring
 * <p>
 * Initial Date: 28.08.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public abstract class BasicManager extends LogDelegator {

	//TODO: add more common stuff like database access...
	

}
