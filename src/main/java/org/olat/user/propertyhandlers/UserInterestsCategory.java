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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.List;

/**
 * <h3>Description:</h3>
 * This is a data container for a user interest category.
 * 
 * Initial Date: Aug 12, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class UserInterestsCategory {
	
	private String id;
	private List<UserInterestsCategory> subcategories;
	
	/**
	 * Default constructor.
	 */
	public UserInterestsCategory() {
		setSubcategories(new ArrayList<UserInterestsCategory>());
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public List<UserInterestsCategory> getSubcategories() {
		return subcategories;
	}
	
	public void setSubcategories(List<UserInterestsCategory> subcategories) {
		this.subcategories = subcategories;
	}
}
