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
package org.olat.core.gui.components.url;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.components.form.flexible.FormItem;

/**
 *
 * Initial date: 29 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UrlComponent extends AbstractComponent implements FormBaseComponent {
	
	private static final ComponentRenderer RENDERER = new UrlComponentRenderer();
	
	private String url;
	private String ariaLabel;
	private boolean qrCodeEnabled = true;
	
	private final FormItem formItem;
	
	public UrlComponent(String name, String url) {
		this(name, url, null);
	}
	
	public UrlComponent(String name, String url, FormItem formItem) {
		super(name);
		this.url = url;
		this.formItem = formItem;
	}
	
	@Override
	public String getFormDispatchId() {
		return DISPPREFIX.concat(super.getDispatchID());
	}
	
	@Override
	public FormItem getFormItem() {
		return formItem;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
		setDirty(true);
	}
	
	public String getAriaLabel() {
		return ariaLabel;
	}
	
	public void setAriaLabel(String ariaLabel) {
		this.ariaLabel = ariaLabel;
		setDirty(true);
	}
	
	public boolean isQrCodeEnabled() {
		return qrCodeEnabled;
	}
	
	public void setQrCodeEnabled(boolean qrCodeEnabled) {
		this.qrCodeEnabled = qrCodeEnabled;
		setDirty(true);
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
}
