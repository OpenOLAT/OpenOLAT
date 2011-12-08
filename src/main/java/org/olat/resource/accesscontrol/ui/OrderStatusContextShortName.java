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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.ShortName;
import org.olat.resource.accesscontrol.model.OrderStatus;

/**
 * 
 * Description:<br>
 * Short name for filters by order status
 * 
 * <P>
 * Initial Date:  27 mai 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OrderStatusContextShortName implements ShortName {
	private String contextName;
	private OrderStatus[] status;
	
	public OrderStatusContextShortName(String contextName, OrderStatus status) {
		this.contextName = contextName;
		this.status = new OrderStatus[]{status};
	}
	
	public OrderStatusContextShortName(String contextName, OrderStatus... status) {
		this.contextName = contextName;
		this.status = status;
	}
	
	@Override
	public String getShortName() {
		return contextName;
	}

	public OrderStatus[] getStatus() {
		return status;
	}
	
	public String getContextName() {
		return contextName;
	}
}
