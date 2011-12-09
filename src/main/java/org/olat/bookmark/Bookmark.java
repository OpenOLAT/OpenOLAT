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

package org.olat.bookmark;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public interface Bookmark extends CreateInfo, Persistable {

	/**
	 * returns the description of the bookmark
	 * 
	 * @return description
	 */
	public String getDescription();

	/**
	 * returns the internal reference (url) of the bookmark
	 * 
	 * @return internal reference
	 */
	public String getDetaildata();

	/**
	 * returns the resource key of the bookmark
	 * 
	 * @return resource key
	 */
	public Long getOlatreskey();

	/**
	 * @return the resource type (course, forum etc.) of the bookmark
	 */
	public String getOlatrestype();

	/**
	 * @return the resource type to display to the user
	 */
	public String getDisplayrestype();

	/**
	 * @return the owner of the bookmark
	 */
	public Identity getOwner();

	/**
	 * @return title of the bookmark
	 */
	public String getTitle();

	/**
	 * @param description the description of the bookmark
	 */
	public void setDescription(String description);

	/**
	 * set the internal reference of the bookmark
	 * 
	 * @param intref
	 */
	public void setDetaildata(String intref);

	/**
	 * set the olat resource key of the bookmark
	 * 
	 * @param olatreskey resource key
	 */
	public void setOlatreskey(Long olatreskey);

	/**
	 * set the olat resource type of the bookmark
	 * 
	 * @param olatrestype resource type
	 */
	public void setOlatrestype(String olatrestype);

	/**
	 * set the owner of the bookmark
	 * 
	 * @param ident
	 */
	public void setOwner(Identity ident);

	/**
	 * set the title of the bookmark
	 * 
	 * @param string
	 */
	public void setTitle(String string);
}