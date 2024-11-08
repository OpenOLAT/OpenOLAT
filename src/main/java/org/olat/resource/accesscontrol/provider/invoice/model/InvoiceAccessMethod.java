/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.provider.invoice.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.provider.invoice.InvoiceAccessHandler;


/**
 *
 * Initial date: 4 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
@Entity(name="acinvoicemethod")
@DiscriminatorValue(value="invoice.method")
public class InvoiceAccessMethod extends AbstractAccessMethod {

	private static final long serialVersionUID = 5072119677285818279L;

	@Override
	public String getType() {
		return InvoiceAccessHandler.METHOD_TYPE;
	}

	@Override
	public String getMethodCssClass() {
		return InvoiceAccessHandler.METHOD_CSS_CLASS;
	}

	@Override
	public boolean isNeedUserInteraction() {
		return true;
	}

	@Override
	public boolean isPaymentMethod() {
		return true;
	}

	@Override
	public boolean isVisibleInGui() {
		return true;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -130581 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof InvoiceAccessMethod method) {
			return getKey() != null && getKey().equals(method.getKey());
		}
		return false;
	}
}
