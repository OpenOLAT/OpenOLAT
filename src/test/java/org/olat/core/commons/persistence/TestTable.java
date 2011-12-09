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
* <p>
*/ 

package org.olat.core.commons.persistence;

import java.util.Date;

/**
 * A <b>TestTable</b> is used to test the persistence package.
 * 
 * @author Andreas Ch. Kapp
 *
 */
public class TestTable {
	
	Long key;
	int version;
	String field1;
	long field2;
	String field3;
	Date creationDate;

	/**
	 * @return field1
	 */
	public String getField1() {
		return field1;
	}

	/**
	 * @return field2
	 */
	public long getField2() {
		return field2;
	}

	/**
	 * @return field3
	 */
	public String getField3() {
		return field3;
	}

	/**
	 * @return key
	 */
	public Long getKey() {
		return key;
	}

	/**
	 * @param string
	 */
	public void setField1(String string) {
		field1 = string;
	}

	/**
	 * @param l
	 */
	public void setField2(long l) {
		field2 = l;
	}

	/**
	 * @param string
	 */
	public void setField3(String string) {
		field3 = string;
	}

	/**
	 * @param long1
	 */
	public void setKey(Long long1) {
		key = long1;
	}

	/**
	 * @return creationDate
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * @return lastModified
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param date
	 */
	public void setCreationDate(Date date) {
		creationDate = date;
	}

	/**
	 * @param date
	 */
	public void setVersion(int version) {
		this.version = version;
	}

}
