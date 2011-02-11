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

package org.olat.core.commons.contextHelp;

import org.olat.core.util.event.MultiUserEvent;


/**
 * <h3>Description:</h3> This event is fired when someone changed a rating for a
 * context help page.
 * <p>
 * Initial Date: 10.11.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class ContextHelpRatingEvent extends MultiUserEvent {
	private static final String COMMAND = "ContextHelpRatingEvent";
	private final String key;
	private final  Object[] ratingValues;

	/**
	 * Constructor to create a context help rating event
	 * @param key The key of the page that got rated
	 * @param ratingValues The new rating values
	 */
	public ContextHelpRatingEvent(String key, Object[] ratingValues) {
		super(COMMAND);
		this.key = key;
		this.ratingValues = ratingValues;
	}

	public String getKey() {
		return key;
	}

	public Object[] getRatingValues() {
		return ratingValues;
	}

}
