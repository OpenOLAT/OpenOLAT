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

package org.olat.group.context;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

/**
 * Description:<BR>
 * A business group context is a container for groups that are used together as
 * a set. Eg. all groups of a course, or a class with some subgroups, or a
 * Lehrgang / Studiengang. Group contexts can be attached many courses. The
 * group context can contain only business groups of one specific business group
 * type. Default group contexts can only be used for one resource (one
 * association allowed). This is configured by the setDefaultContext() method.
 * <P>
 * Initial Date: Aug 18, 2004
 * 
 * @author gnaegi
 */
public interface BGContext extends Persistable, CreateInfo, OLATResourceable {
	/** regular expression to check for valid group context names */
	public final static String VALID_GROUPCONTEXTNAME_REGEXP = "^[^,\"]*$";

	/**
	 * @return The context name
	 */
	abstract String getName();

	/**
	 * @return The context description
	 */
	abstract String getDescription();

	/**
	 * @param string The context description
	 */
	abstract void setDescription(String string);

	/**
	 * @param string The context name
	 */
	abstract void setName(String string);

	/**
	 * @return The owner group of this context
	 */
	abstract SecurityGroup getOwnerGroup();

	/**
	 * @return The group type used in this context
	 */
	abstract String getGroupType();

	/**
	 * @return boolen true: this is a default context, only one relation to a
	 *         resource allowed
	 */
	abstract boolean isDefaultContext();

	/**
	 * Set the default context: true means that only one resource relation is
	 * allowed
	 * 
	 * @param defaultContext
	 */
	abstract void setDefaultContext(boolean defaultContext);

}