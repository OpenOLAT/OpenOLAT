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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.resource.accesscontrol.OrderStatus;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.olat.resource.accesscontrol.ui.OrderTableItem.Status;

/**
 * 
 * Initial date: 21 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrderTableRow {
	
	private final OrderTableItem item;
	
	private Status modifiedStatus;
	private OrderModificationSummary modificationsSummary;
	
	private FormLink toolsLink;

	private OrderDetailController detailsCtrl;
	
	public OrderTableRow(OrderTableItem item) {
		this.item = item;
	}
	
	public OrderTableItem getItem() {
		return item;
	}
	
	public Long getDeliveryKey() {
		return item.getDeliveryKey();
	}

	public Status getStatus() {
		return item.getStatus();
	}
	
	public OrderStatus getOrderStatus() {
		return item.getOrderStatus();
	}

	public Status getModifiedStatus() {
		return modifiedStatus;
	}
	
	public void setModifiedStatus(Status modifiedStatus) {
		this.modifiedStatus = modifiedStatus;
	}
	
	public Long getOrderKey() {
		return item.getOrderKey();
	}

	public String getOrderNr() {
		return item.getOrderNr();
	}
	
	public String getOfferLabel() {
		return item.getLabel();
	}
	
	public Date getCreationDate() {
		return item.getCreationDate();
	}

	public String getResourceDisplayname() {
		return item.getResourceDisplayname();
	}

	public List<AccessMethod> getMethods() {
		return item.getMethods();
	}

	public Price getTotal() {
		return item.getTotal();
	}
	
	public Price getCancellationFees() {
		return item.getCancellationFees();
	}

	public OrderModificationSummary getModificationsSummary() {
		return modificationsSummary;
	}

	public void setModificationsSummary(OrderModificationSummary modificationsSummary) {
		this.modificationsSummary = modificationsSummary;
	}

	public String[] getUserProperties() {
		return item.getUserProperties();
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public String getDetailsControllerName() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().getComponent().getComponentName();
		}
		return null;
	}
	
	public boolean isDetailsControllerAvailable() {
		if(detailsCtrl != null) {
			return detailsCtrl.getInitialFormItem().isVisible();
		}
		return false;
	}

	public OrderDetailController getDetailsController() {
		return detailsCtrl;
	}

	public void setDetailsController(OrderDetailController detailsCtrl) {
		this.detailsCtrl = detailsCtrl;
	}
}
