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

package org.olat.resource.accesscontrol;

import java.util.Collections;
import java.util.List;

/**
 * 
 * Description:<br>
 * Result of an attempt to access a resource
 * 
 * <P>
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AccessResult {
	
	private final boolean accessible;
	private List<OfferAccess> availableMethods;
	
	public AccessResult(boolean accessible) {
		this(accessible, Collections.emptyList());
	}
	
	public AccessResult(boolean accessible, List<OfferAccess> availableMethods) {
		this.accessible = accessible;
		this.availableMethods = availableMethods;
	}

	public boolean isAccessible() {
		return accessible;
	}

	public List<OfferAccess> getAvailableMethods() {
		return availableMethods;
	}
}
