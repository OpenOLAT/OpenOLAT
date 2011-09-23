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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */

package org.olat.modules.wiki;

/**
 * Description:<br>
 * Element of @see org.olat.modules.wiki.BreadCrumbController
 * <P>
 * Initial Date: Jul 4, 2006 <br>
 * 
 * @author guido
 */
public class Crumb {
	private String name;
	private String description;
	private int maxLength = 15;

	/**
	 * @param name
	 * @param description
	 */
	public Crumb(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		if (description.length() > maxLength) return description.substring(0, maxLength) + "...";
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}

}
