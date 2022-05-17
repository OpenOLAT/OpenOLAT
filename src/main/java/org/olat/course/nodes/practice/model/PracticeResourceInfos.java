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
package org.olat.course.nodes.practice.model;

import org.olat.course.nodes.practice.PracticeResource;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeResourceInfos {
	
	private final PracticeResource resource;
	private final int numOfItems;
	
	public PracticeResourceInfos(PracticeResource resource, int numOfItems) {
		this.resource = resource;
		this.numOfItems = numOfItems;
	}
	
	public String getName() {
		return resource.getName();
	}
	
	public boolean isTestEntry() {
		return resource.getTestEntry() != null;
	}
	
	public PracticeResource getResource() {
		return resource;
	}
	
	public int getNumOfItems() {
		return numOfItems;
	}
}
