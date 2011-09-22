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
* Copyright (c) 1999-2009 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.logging.activity;

/**
 * A tagging interface used to specify the type of a Resourceable which is
 * logged with an entry in the o_loggingtable.
 * <p>
 * Implementation-Note: This is a parent interface of OlatResourceableType
 * (for OlatResourceables) and StringResourceableType (for simple Strings).
 * Both are simple enums - and in order to be able to have a parent interface
 * for two enums this interface requires name() - which is the only method
 * used by users of this ILoggingResourceableType.
 * <P>
 * Initial Date:  20.10.2009 <br>
 * @author Stefan
 */
public interface ILoggingResourceableType {
	
	/**
	 * Returns the name of this ILoggingResourceableType -
	 * implemented by sub-interfaces - the two enums - natively
	 * @return the name of this ILoggingResourceableType
	 */
	public String name();

}
