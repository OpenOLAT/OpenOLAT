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
package org.olat.core.commons.services.commentAndRating.model;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

/**
 * Description:<br>
 * The user rating object represents a rating of a user about an OLAT
 * resource.
 * 
 * <P>
 * Initial Date: 23.11.2009 <br>
 * 
 * @author gnaegi
 */
public interface UserRating extends CreateInfo, ModifiedInfo, Persistable {

	/**
	 * @return The OLAT resource type name of the resource which is beeing
	 *         commented
	 */
	public String getResName();

	/**
	 * @return The OLAT resource id of the resource which is beeing commented
	 */
	public Long getResId();

	/**
	 * @return An optional String to precisely define the resource if the
	 *         resource name and resource type id is not enough. Returns NULL if
	 *         not defined.
	 */
	public String getResSubPath();

	/**
	 * @return The Author of this comment.
	 */
	public Identity getCreator();
	
	/**
	 * @param creator The author of the comment
	 */
	public void setCreator(Identity creator);
	
	/**
	 * @return The users rating
	 */
	public Integer getRating();
	
	/**
	 * @param ratingValue The rating value
	 */
	public void setRating(Integer ratingValue);
	
}
