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

package org.olat.core.commons.services.notifications;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * Initial Date:  21.10.2004 <br>
 * @author Felix Jost
 */
public interface Publisher extends Persistable, CreateInfo {
	/**
	 * @return resId
	 */
	public Long getResId();

	/**
	 * @param resId
	 */
	public void setResId(Long resId);

	/**
	 * @return resName
	 */
	public String getResName();

	/**
	 * @param resName
	 */
	public void setResName(String resName);

	
	/**
	 * @return the subident (to divide a olatresource further into subsegments)
	 */
	public String getSubidentifier();	
	
	/**
	 * @param subidentifier (max len 255)
	 */
	public void setSubidentifier(String subidentifier);
	
	/**
	 * @return the type
	 */
	public String getType();
	
	/**
	 * @return the data (normally an xml unpacked by streams into an java object
	 */
	public String getData();
	
	/**
	 * @param data
	 */
	public void setData(String data);
	
	/**
	 * @return state
	 */
	public int getState();

	/**
	 * @param state
	 */
	public void setState(int state);
	
	/**
	 * @return the date of the latestNews this publisher has.
	 */
	public Date getLatestNewsDate();

	/**
	 * @param latestRead
	 */
	public void setLatestNewsDate(Date latestRead);

	
	public String getBusinessPath();

	public void setBusinessPath(String businessPath);
	
}