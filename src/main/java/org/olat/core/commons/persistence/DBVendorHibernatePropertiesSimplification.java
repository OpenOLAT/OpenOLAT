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
package org.olat.core.commons.persistence;

import java.util.Map;
import java.util.Properties;

/**
 * Description:<br>
 * Helper class created to concatenate DB specific connection properties (<code>java.util.Properties</code>) with the DB common
 * hibernate properties within the Spring config.
 * 
 * <P>
 * Initial Date:  07.09.2010 <br>
 * @author patrickb
 */
public class DBVendorHibernatePropertiesSimplification extends Properties {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public DBVendorHibernatePropertiesSimplification() {
		super();
	}

	/**
	 * First added key valu pairs, which are eventually overwritten by values added during a {@link #setMoreProperties(Map)} call.
	 * @param firstKeyValues
	 */
	public DBVendorHibernatePropertiesSimplification(Properties firstKeyValues) {
		super();
		putAll(firstKeyValues);
	}
	
	/**
	 * add more key value pairs
	 * @param more
	 */
	public void setAddMoreProperties(Map<String,String> more){
		putAll(more);
	}

}
