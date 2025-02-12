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
package org.olat.resource.accesscontrol.ui;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.resource.accesscontrol.BillingAddress;

/**
 * 
 * Initial date: Feb 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class BillingAddressComponent extends FormBaseComponentImpl {

	private static final BillingAddressRenderer RENDERER = new BillingAddressRenderer();
	
	private final Translator compTranslator;
	private final FormItem formItem;
	private BillingAddress billingAddress;
	private boolean temporaryAddressWarning = true;

	public BillingAddressComponent(String name, Locale locale) {
		this(name, locale, null);
	}
	
	BillingAddressComponent(String name, Locale locale, FormItem formItem) {
		super(name);
		this.formItem = formItem;
		this.compTranslator = Util.createPackageTranslator(BillingAddressRenderer.class, locale);
	}

	@Override
	public FormItem getFormItem() {
		return formItem;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	Translator getCompTranslator() {
		return compTranslator;
	}

	public BillingAddress getBillingAddress() {
		return billingAddress;
	}

	public void setBillingAddress(BillingAddress billingAddress) {
		this.billingAddress = billingAddress;
		setDirty(true);
	}

	public boolean isTemporaryAddressWarning() {
		return temporaryAddressWarning;
	}

	public void setTemporaryAddressWarning(boolean temporaryAddressWarning) {
		this.temporaryAddressWarning = temporaryAddressWarning;
	}

}
