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
package org.olat.resource.accesscontrol.provider.paypalcheckout.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.olat.resource.accesscontrol.model.AbstractAccessMethod;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutAccessHandler;


/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="accheckoutmethod")
@DiscriminatorValue(value="checkout.method")
public class PaypalCheckoutAccessMethod extends AbstractAccessMethod {

	private static final long serialVersionUID = 7682228653442368290L;

	@Override
	public String getType() {
		return PaypalCheckoutAccessHandler.METHOD_TYPE;
	}

	@Override
	public String getMethodCssClass() {
		return PaypalCheckoutAccessHandler.METHOD_CSS_CLASS;
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
}
