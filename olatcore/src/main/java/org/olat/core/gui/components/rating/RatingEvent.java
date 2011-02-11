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
package org.olat.core.gui.components.rating;

import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * The rating event is fired by the RatingComponent when a new rating is made
 * <P>
 * Initial Date:  31.10.2008 <br>
 * @author gnaegi
 */
public class RatingEvent extends Event {
	private static final String RATING_EVENT = "ratingEvent";
	private float rating;

	/**
	 * Constructor for a rating event.
	 * @param rating The current rating value
	 */
	public RatingEvent(float rating) {
		super(RATING_EVENT);
		this.rating = rating;
	}

	/**
	 * @return The rating value made
	 */
	public float getRating() {
		return rating;
	}

}
