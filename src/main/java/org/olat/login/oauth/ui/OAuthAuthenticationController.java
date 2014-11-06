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
package org.olat.login.oauth.ui;

import javax.servlet.http.HttpSession;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthResource;
import org.olat.login.oauth.OAuthSPI;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 03.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthAuthenticationController extends FormBasicController {
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	public OAuthAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "login");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		for(OAuthSPI spi:  oauthModule.getEnableSPIs()) {
			String spiName = spi.getName();
			FormLink button = uifactory.addFormLink(spiName, "login", "login." + spiName, null, formLayout, Link.BUTTON);
			button.setIconLeftCSS(spi.getIconCSS());
			button.setElementCssClass("o_sel_auth_" + spiName);
			button.setUserObject(spi);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink button = (FormLink)source;
			if("login".equals(button.getCmd())) {
				OAuthSPI provider = (OAuthSPI)source.getUserObject();
				HttpSession session = ureq.getHttpReq().getSession();
				MediaResource redirectResource = new OAuthResource(provider, session);
				ureq.getDispatchResult().setResultingMediaResource(redirectResource);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}