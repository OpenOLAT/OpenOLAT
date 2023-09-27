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
package org.olat.user.ui.identity;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.panel.IconPanelItem;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.ldap.ui.LDAPAuthenticationController;
import org.olat.login.LoginModule;
import org.olat.login.auth.AuthenticationProvider;
import org.olat.login.oauth.OAuthAuthenticationProvider;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.ui.OAuthAuthenticationController;
import org.olat.shibboleth.ShibbolethDispatcher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserExternalAuthenticationController extends FormBasicController {
	
	private int count = 0;
	private final Formatter format;
	private List<Authentication> authentications;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OAuthLoginModule oauthModule;
	@Autowired
	private BaseSecurity securityManager;
	
	public UserExternalAuthenticationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL, Util.createPackageTranslator(OAuthAuthenticationController.class, ureq.getLocale()));
		authentications = loadExternalAuthentications();
		format = Formatter.getInstance(getLocale());
		initForm(ureq);
	}
	
	public boolean hasAuthentications() {
		return !authentications.isEmpty();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("external.providers.title");
		setFormInfo("external.providers.desc", new String[] { WebappHelper.getMailConfig("mailSupport") });
		
		for(Authentication authentication:authentications) {
			AuthenticationProvider provider = loginModule.getAuthenticationProvider(authentication.getProvider());
			if(provider != null && isExternalProvider(authentication)) {
				initProvider(formLayout, authentication, provider);
			}
		}
	}
	
	private IconPanelItem initProvider(FormItemContainer formLayout, Authentication authentication, AuthenticationProvider provider) {
		String providerName = authentication.getProvider();
		IconPanelItem iconPanel = new IconPanelItem("provider-" + (++count));
		iconPanel.setElementCssClass("o_block_bottom o_sel_ac_offer");
		
		String iconCssClass = "";
		String name = "";
		if(provider instanceof OAuthAuthenticationProvider) {
			OAuthSPI oauthSpi = oauthModule.getProvider(providerName);
			iconCssClass = oauthSpi.getIconCSS();
			name = translate("login." + oauthSpi.getName());
		} else if(provider != null) {
			iconCssClass = provider.getIconCssClass();
			name = provider.getName();
		}
		iconPanel.setIconCssClass("o_icon o_icon-fw " + iconCssClass);
		iconPanel.setTitle(name);
		
		IconPanelLabelTextContent content = new IconPanelLabelTextContent("content_" + (++count));
		iconPanel.setContent(content);
		
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("creation.date"), format.formatDate(authentication.getCreationDate())));
		content.setLabelTexts(labelTexts);
		
		formLayout.add(iconPanel);
		return iconPanel;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<Authentication> loadExternalAuthentications() {
		return securityManager.getAuthentications(getIdentity()).stream()
				.filter(this::isExternalProvider)
				.toList();
	}
	
	private boolean isExternalProvider(Authentication authentication) {
		AuthenticationProvider provider = loginModule.getAuthenticationProvider(authentication.getProvider());
		if(provider == null) {
			return false;
		}
		
		String providerName = provider.getName();
		if(ShibbolethDispatcher.PROVIDER_SHIB.equals(providerName) || LDAPAuthenticationController.PROVIDER_LDAP.equals(providerName)) {
			return true;
		}
		return provider instanceof OAuthAuthenticationProvider;
	}
}
