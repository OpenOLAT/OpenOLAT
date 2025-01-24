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
package org.olat.resource.accesscontrol.model;

import org.olat.resource.accesscontrol.Price;

/**
 * 
 * Description:<br>
 * Helper class to bundle a method to access content and its price
 * 
 * <P>
 * Initial Date:  30 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PriceMethodBundle {
	
	private final Price price;
	private final boolean autoBooking;
	private final AccessMethod method;
	
	public PriceMethodBundle(Price price, AccessMethod method) {
		this(price, method, false);
	}
	
	public PriceMethodBundle(Price price, AccessMethod method, boolean autoBooking) {
		this.price = price;
		this.method = method;
		this.autoBooking = autoBooking;
	}

	public Price getPrice() {
		return price;
	}

	public boolean isAutoBooking() {
		return autoBooking;
	}

	public AccessMethod getMethod() {
		return method;
	}
	
}
