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
package org.olat.core.commons.services.tag;

/**
 * 
 * Initial date: 6 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface TagInfo extends Tag {

	/**
	 * get the number, how often the tag is being used in this context
	 * @return Long value about the usage
	 */
	public Long getCount();

	/**
	 * information if the tag is selected or not
	 * @return true/false
	 */
	public boolean isSelected();

	/**
	 * set the information if the tag is selected or not
	 * @param selected true/false
	 */
	public void setSelected(boolean selected);
	
}
