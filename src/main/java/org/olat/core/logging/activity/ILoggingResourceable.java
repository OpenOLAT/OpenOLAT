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
package org.olat.core.logging.activity;

import org.olat.core.id.context.ContextEntry;

public interface ILoggingResourceable {

	/**
	 * Checks whether this LoggingResourceable represents the same resource as the
	 * given ContextEntry.
	 * <p>
	 * This is used during the businessPath check.
	 * @param ce
	 * @return
	 */
	public boolean correspondsTo(ContextEntry ce);

	/**
	 * Returns the type of this LoggingResourceable - this is the OlatResourceable's type
	 * (in case this LoggingResource represents a OlatResourceable) - or the StringResourceableType's enum name()
	 * otherwise
	 * @return the type of this LoggingResourceable
	 */
	public String getType();

	/**
	 * Returns the id of this LoggingResourceable - the id varies depending on the type of this
	 * LoggingResourceable - but usually it is the olatresourceable id or the olatresource id.
	 * @return the id of this LoggingResourceable
	 */
	public String getId();

	/**
	 * Returns the name of this LoggingResourceable - the name varies depending on the type
	 * of this LoggingResource - e.g. in the course case it is the name of the course, in
	 * the CP case it is the html filename incl path
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns the ILoggingResourceableType of this LoggingResourceable - used for businessPath checking
	 * @return the ILoggingResourceableType of this LoggingResourceable
	 */
	public ILoggingResourceableType getResourceableType();
	
	/**
	 * Some components of a list of context entries are used to recreate the
	 * controllers hierarchy by back button and haven't any meaning for logging.
	 * @return
	 */
	public boolean isIgnorable();
}
