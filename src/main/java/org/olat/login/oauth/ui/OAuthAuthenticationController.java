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

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.DispatchResult;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthDisplayName;
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
public class OAuthAuthenticationController extends FormBasicController implements Activateable2 {
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	public OAuthAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "login");
		initForm(ureq);

		String provider = ureq.getParameter("provider");
		if(StringHelper.containsNonWhitespace(provider)) {
			OAuthSPI spi = oauthModule.getProvider(provider);
			if(spi != null) {
				redirect(ureq, spi);
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		for(OAuthSPI spi:  oauthModule.getEnableSPIs()) {
			String spiName;
			int presentation;
			if(spi instanceof OAuthDisplayName) {
				spiName = ((OAuthDisplayName)spi).getDisplayName();
				presentation = Link.BUTTON | Link.NONTRANSLATED;
			} else {
				spiName	= "login.".concat(spi.getName());
				presentation = Link.BUTTON;
			}
		
			FormLink button = uifactory.addFormLink(spiName, "login", spiName, null, formLayout, presentation);
			button.setIconLeftCSS(spi.getIconCSS());
			button.setElementCssClass("o_sel_auth_" + spi.getName());
			button.setUserObject(spi);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
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
				redirect(ureq, provider);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void redirect(UserRequest ureq, OAuthSPI provider) {
		DispatchResult result = ureq.getDispatchResult();
		if(result.getResultingMediaResource() == null) {// prevent twice the redirect
			HttpSession session = ureq.getHttpReq().getSession();
			MediaResource redirectResource = new OAuthResource(provider, session);
			result.setResultingMediaResource(redirectResource);
		}
	}
}