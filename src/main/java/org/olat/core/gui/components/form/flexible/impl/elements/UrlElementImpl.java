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
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.UrlElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.url.UrlComponent;

/**
 *
 * Initial date: 29 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UrlElementImpl extends FormItemImpl implements UrlElement {
	
	private final UrlComponent component;
	
	public UrlElementImpl(String name, String url) {
		super(name);
		component = new UrlComponent(name, url, this);
	}
	
	@Override
	public String getUrl() {
		return component.getUrl();
	}
	
	@Override
	public void setUrl(String url) {
		component.setUrl(url);
	}
	
	@Override
	public void setAriaLabel(String ariaLabel) {
		component.setAriaLabel(ariaLabel);
	}
	
	@Override
	public boolean isQrCodeEnabled() {
		return component.isQrCodeEnabled();
	}
	
	@Override
	public void setQrCodeEnabled(boolean qrCodeEnabled) {
		component.setQrCodeEnabled(qrCodeEnabled);
	}
	
	@Override
	protected UrlComponent getFormItemComponent() {
		return component;
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void reset() {
		//
	}
	
	@Override
	protected void rootFormAvailable() {
		//
	}
	
}
