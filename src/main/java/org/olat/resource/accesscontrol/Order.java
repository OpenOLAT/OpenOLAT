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

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;

/**
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface Order {
	
	public Long getKey();
	
	public String getOrderNr();
	
	public boolean isValid();
	
	public Date getCreationDate();
	
	public OrderStatus getOrderStatus();
	
	public Identity getDelivery();
	
	public String getCurrencyCode();
	
	public Price getTotal();

	public Price getTotalOrderLines();

	public Price getDiscount();
	
	public List<OrderPart> getParts();
	
	public void recalculate();
}
