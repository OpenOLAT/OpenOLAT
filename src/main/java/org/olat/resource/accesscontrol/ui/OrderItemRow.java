/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.resource.accesscontrol.AccessTransaction;
import org.olat.resource.accesscontrol.OrderLine;
import org.olat.resource.accesscontrol.OrderPart;

/**
 * 
 * Initial date: 27 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderItemRow {

	private final boolean first;
	private final OrderPart part;
	private final OrderLine item;
	private final AccessTransaction transaction;
	private final String displayName;
	
	private FormLink transactionDetailsLink;
	
	public OrderItemRow(OrderPart part, OrderLine item, AccessTransaction transaction, String displayName,  boolean first) {
		this.part = part;
		this.item = item;
		this.first = first;
		this.transaction = transaction;
		this.displayName = displayName;
	}

	public boolean isFirst() {
		return first;
	}

	public String getDisplayName() {
		return displayName;
	}

	public OrderPart getPart() {
		return part;
	}

	public OrderLine getItem() {
		return item;
	}
	
	public AccessTransaction getTransaction() {
		return transaction;
	}
	
	public OLATResourceable getOLATResourceable() {
		return new OLATResourceable() {
			@Override
			public String getResourceableTypeName() {
				return item.getOffer().getResourceTypeName();
			}
			@Override
			public Long getResourceableId() {
				return item.getOffer().getResourceId();
			}
		};
	}

	public FormLink getTransactionDetailsLink() {
		return transactionDetailsLink;
	}

	public void setTransactionDetailsLink(FormLink transactionDetailsLink) {
		this.transactionDetailsLink = transactionDetailsLink;
	}
}
