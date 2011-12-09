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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;

/**
 * 
 * Description:<br>
 * Implementation of the interface OrderPart
 * 
 * <P>
 * Initial Date:  19 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderPartImpl extends PersistentObject implements OrderPart {

	private Price total;
	private Price totalOrderLines;
	
	private List<OrderLine> lines;
	
	@Override
	public Price getTotal() {
		return total;
	}

	public void setTotal(Price total) {
		this.total = total;
	}

	@Override
	public Price getTotalOrderLines() {
		return totalOrderLines;
	}

	public void setTotalOrderLines(Price totalOrderLines) {
		this.totalOrderLines = totalOrderLines;
	}

	public List<OrderLine> getOrderLines() {
		if(lines == null) {
			lines = new ArrayList<OrderLine>();
		}
		return lines;
	}

	public void setOrderLines(List<OrderLine> lines) {
		this.lines = lines;
	}
	
	public void recalculate(String currencyCode) {
		totalOrderLines = new PriceImpl(BigDecimal.ZERO, currencyCode);
		for(OrderLine orderLine : getOrderLines()) {
			totalOrderLines = totalOrderLines.add(orderLine.getTotal());
		}
		
		total = totalOrderLines.clone();
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 27591 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrderPartImpl) {
			OrderPartImpl order = (OrderPartImpl)obj;
			return equalsByPersistableKey(order);
		}
		return false;
	}
}
