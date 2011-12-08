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

package org.olat.core.commons.services.commentAndRating.model;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * <h3>Description:</h3>
 * Return wrapper for the average rating over severals resources
 * <p>
 * Initial Date:  17 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class OLATResourceableRating {
	
	private final OLATResourceable ores;
	private final String resSubPath;
	private final Double rating;
	
	public OLATResourceableRating(String resName, Long resId, String resSubPath, Double rating) {
		ores = OresHelper.createOLATResourceableInstance(resName, resId);
		this.resSubPath = resSubPath;
		this.rating = rating;
	}

	public OLATResourceable getOres() {
		return ores;
	}

	public String getResSubPath() {
		return resSubPath;
	}

	/**
	 * @return The average rating
	 */
	public Double getRating() {
		return rating;
	}

}
