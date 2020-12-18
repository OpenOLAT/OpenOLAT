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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.spi.OpenIdConnectFullConfigurableProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthAdminController extends FormBasicController {
	
	private static final String[] keys = new String[]{ "on" };
	private static final String[] values = new String[] { "" };
	
	private FormLink addProviderLink;

	private MultipleSelectionElement userCreationEl;
	
	private MultipleSelectionElement linkedInEl;
	private TextElement linkedInApiKeyEl;
	private TextElement linkedInApiSecretEl;
	
	private MultipleSelectionElement twitterEl;
	private TextElement twitterApiKeyEl;
	private TextElement twitterApiSecretEl;
	
	private MultipleSelectionElement googleEl;
	private TextElement googleApiKeyEl;
	private TextElement googleApiSecretEl;
	
	private MultipleSelectionElement facebookEl;
	private TextElement facebookApiKeyEl;
	private TextElement facebookApiSecretEl;
	
	private MultipleSelectionElement adfsEl;
	private MultipleSelectionElement adfsDefaultEl;
	private TextElement adfsApiKeyEl;
	private TextElement adfsApiSecretEl;
	private TextElement adfsOAuth2EndpointEl;
	
	private MultipleSelectionElement azureAdfsEl;
	private MultipleSelectionElement azureAdfsDefaultEl;
	private TextElement azureAdfsApiKeyEl;
	private TextElement azureAdfsApiSecretEl;
	private TextElement azureAdfsTenantEl;
	
	private MultipleSelectionElement tequilaEl;
	private TextElement tequilaApiKeyEl;
	private TextElement tequilaApiSecretEl;
	private TextElement tequilaOAuth2EndpointEl;
	
	private MultipleSelectionElement keycloakEl;
	private TextElement keycloakClientIdEl;
	private TextElement keycloakClientSecretEl;
	private TextElement keycloakEndpointEl;
	private TextElement keycloakRealmEl;
	
	private MultipleSelectionElement openIdConnectIFEl;
	private MultipleSelectionElement openIdConnectIFDefaultEl;
	private TextElement openIdConnectIFApiKeyEl;
	private TextElement openIdConnectIFApiSecretEl;
	private TextElement openIdConnectIFIssuerEl;
	private TextElement openIdConnectIFAuthorizationEndPointEl;
	
	private FormLayoutContainer customProvidersCont;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private AddOpenIDConnectIFFullConfigurableController addConfigCtrl;
	
	private List<ConfigurableProviderWrapper> providerWrappers = new ArrayList<>();
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	public OAuthAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer oauthCont = FormLayoutContainer.createDefaultFormLayout("oauth", getTranslator());
		oauthCont.setFormTitle(translate("oauth.admin.title"));
		oauthCont.setRootForm(mainForm);
		formLayout.add(oauthCont);

		userCreationEl = uifactory.addCheckboxesHorizontal("user.creation.enabled", oauthCont, keys, values);
		userCreationEl.addActionListener(FormEvent.ONCHANGE);
		if(oauthModule.isAllowUserCreation()) {
			userCreationEl.select(keys[0], true);
		}
		String callbackUrl = "<span class='o_copy_code o_nowrap'><input type='text' value='" + oauthModule.getCallbackUrl() + "' onclick='this.select()'/></span>";
		uifactory.addStaticTextElement("oauth.redirect.uri", callbackUrl, oauthCont);


		initLinkedInForm(formLayout);
		initTwitterForm(formLayout);
		initGoogleForm(formLayout);
		initFacebookForm(formLayout);
		initAdfsForm(formLayout);
		initAzureAdfsForm(formLayout);
		initKeycloakForm(formLayout);
		initTequilaForm(formLayout);
		initOpenIDConnectForm(formLayout);
		
		customProvidersCont = FormLayoutContainer.createBareBoneFormLayout("custom.providers", getTranslator());
		customProvidersCont.setRootForm(mainForm);
		formLayout.add(customProvidersCont);
		
		//highly configurable providers
		initCustomProviders();
		
		//buttons
		FormLayoutContainer buttonBonesCont = FormLayoutContainer.createDefaultFormLayout("button_bones", getTranslator());
		buttonBonesCont.setRootForm(mainForm);
		formLayout.add(buttonBonesCont);
		uifactory.addSpacerElement("buttons-spacer", buttonBonesCont, true);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonBonesCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		addProviderLink = uifactory.addFormLink("add.openidconnectif.custom", buttonLayout, Link.BUTTON);
	}
	
	private void initLinkedInForm(FormItemContainer formLayout) {
		FormLayoutContainer linkedinCont = FormLayoutContainer.createDefaultFormLayout("linkedin", getTranslator());
		linkedinCont.setFormTitle(translate("linkedin.admin.title"));
		linkedinCont.setFormTitleIconCss("o_icon o_icon_provider_linkedin");
		linkedinCont.setRootForm(mainForm);
		formLayout.add(linkedinCont);
		
		linkedInEl = uifactory.addCheckboxesHorizontal("linkedin.enabled", linkedinCont, keys, values);
		linkedInEl.addActionListener(FormEvent.ONCHANGE);
		String apiKey = oauthModule.getLinkedInApiKey();
		linkedInApiKeyEl = uifactory.addTextElement("linkedin.id", "linkedin.api.id", 256, apiKey, linkedinCont);
		String apiSecret = oauthModule.getLinkedInApiSecret();
		linkedInApiSecretEl = uifactory.addTextElement("linkedin.secret", "linkedin.api.secret", 256, apiSecret, linkedinCont);
		if(oauthModule.isLinkedInEnabled()) {
			linkedInEl.select(keys[0], true);
		} else {
			linkedInApiKeyEl.setVisible(false);
			linkedInApiSecretEl.setVisible(false);
		}
	}
	
	private void initTwitterForm(FormItemContainer formLayout) {
		FormLayoutContainer twitterCont = FormLayoutContainer.createDefaultFormLayout("twitter", getTranslator());
		twitterCont.setFormTitle(translate("twitter.admin.title"));
		twitterCont.setFormTitleIconCss("o_icon o_icon_provider_twitter");
		twitterCont.setRootForm(mainForm);
		formLayout.add(twitterCont);
		
		twitterEl = uifactory.addCheckboxesHorizontal("twitter.enabled", twitterCont, keys, values);
		twitterEl.addActionListener(FormEvent.ONCHANGE);
		String twitterApiKey = oauthModule.getTwitterApiKey();
		twitterApiKeyEl = uifactory.addTextElement("twitter.id", "twitter.api.id", 256, twitterApiKey, twitterCont);
		String twitterApiSecret = oauthModule.getTwitterApiSecret();
		twitterApiSecretEl = uifactory.addTextElement("twitter.secret", "twitter.api.secret", 256, twitterApiSecret, twitterCont);
		if(oauthModule.isTwitterEnabled()) {
			twitterEl.select(keys[0], true);
		} else {
			twitterApiKeyEl.setVisible(false);
			twitterApiSecretEl.setVisible(false);
		}
	}
	
	private void initGoogleForm(FormItemContainer formLayout) {
		FormLayoutContainer googleCont = FormLayoutContainer.createDefaultFormLayout("google", getTranslator());
		googleCont.setFormTitle(translate("google.admin.title"));
		googleCont.setFormTitleIconCss("o_icon o_icon_provider_google");
		googleCont.setRootForm(mainForm);
		formLayout.add(googleCont);
		
		googleEl = uifactory.addCheckboxesHorizontal("google.enabled", googleCont, keys, values);
		googleEl.addActionListener(FormEvent.ONCHANGE);
		String googleApiKey = oauthModule.getGoogleApiKey();
		googleApiKeyEl = uifactory.addTextElement("google.id", "google.api.id", 256, googleApiKey, googleCont);
		String googleApiSecret = oauthModule.getGoogleApiSecret();
		googleApiSecretEl = uifactory.addTextElement("google.secret", "google.api.secret", 256, googleApiSecret, googleCont);
		if(oauthModule.isGoogleEnabled()) {
			googleEl.select(keys[0], true);
		} else {
			googleApiKeyEl.setVisible(false);
			googleApiSecretEl.setVisible(false);
		}
	}
	
	private void initFacebookForm(FormItemContainer formLayout) {
		FormLayoutContainer facebookCont = FormLayoutContainer.createDefaultFormLayout("facebook", getTranslator());
		facebookCont.setFormTitle(translate("facebook.admin.title"));
		facebookCont.setFormTitleIconCss("o_icon o_icon_provider_facebook");
		facebookCont.setRootForm(mainForm);
		formLayout.add(facebookCont);
		
		facebookEl = uifactory.addCheckboxesHorizontal("facebook.enabled", facebookCont, keys, values);
		facebookEl.addActionListener(FormEvent.ONCHANGE);
		String facebookApiKey = oauthModule.getFacebookApiKey();
		facebookApiKeyEl = uifactory.addTextElement("facebook.id", "facebook.api.id", 256, facebookApiKey, facebookCont);
		String facebookApiSecret = oauthModule.getFacebookApiSecret();
		facebookApiSecretEl = uifactory.addTextElement("facebook.secret", "facebook.api.secret", 256, facebookApiSecret, facebookCont);
		if(oauthModule.isFacebookEnabled()) {
			facebookEl.select(keys[0], true);
		} else {
			facebookApiKeyEl.setVisible(false);
			facebookApiSecretEl.setVisible(false);
		}
	}
	
	private void initAdfsForm(FormItemContainer formLayout) {
		FormLayoutContainer adfsCont = FormLayoutContainer.createDefaultFormLayout("adfs", getTranslator());
		adfsCont.setFormTitle(translate("adfs.admin.title"));
		adfsCont.setFormTitleIconCss("o_icon o_icon_provider_adfs");
		adfsCont.setRootForm(mainForm);
		formLayout.add(adfsCont);
		
		adfsEl = uifactory.addCheckboxesHorizontal("adfs.enabled", adfsCont, keys, values);
		adfsEl.addActionListener(FormEvent.ONCHANGE);
		
		adfsDefaultEl = uifactory.addCheckboxesHorizontal("adfs.default.enabled", adfsCont, keys, values);
		adfsDefaultEl.addActionListener(FormEvent.ONCHANGE);
		
		String adfsOAuth2Endpoint = oauthModule.getAdfsOAuth2Endpoint();
		adfsOAuth2EndpointEl = uifactory.addTextElement("adfs.oauth2.endpoint", "adfs.oauth2.endpoint", 256, adfsOAuth2Endpoint, adfsCont);
		adfsOAuth2EndpointEl.setExampleKey("adfs.oauth2.endpoint.example", null);
		
		String adfsApiKey = oauthModule.getAdfsApiKey();
		adfsApiKeyEl = uifactory.addTextElement("adfs.id", "adfs.api.id", 256, adfsApiKey, adfsCont);
		String adfsApiSecret = oauthModule.getAdfsApiSecret();
		adfsApiSecretEl = uifactory.addTextElement("adfs.secret", "adfs.api.secret", 256, adfsApiSecret, adfsCont);
		adfsApiSecretEl.setHelpText(translate("adfs.api.secret.hint"));
		
		if(oauthModule.isAdfsEnabled()) {
			adfsEl.select(keys[0], true);
		} else {
			adfsApiKeyEl.setVisible(false);
			adfsApiSecretEl.setVisible(false);
			adfsDefaultEl.setVisible(false);
			adfsOAuth2EndpointEl.setVisible(false);
		}
		
		if(oauthModule.isAdfsRootEnabled()) {
			adfsDefaultEl.select(keys[0], true);
		}
	}
	
	private void initAzureAdfsForm(FormItemContainer formLayout) {
		FormLayoutContainer adfsCont = FormLayoutContainer.createDefaultFormLayout("azuredfs", getTranslator());
		adfsCont.setFormTitle(translate("azure.adfs.admin.title"));
		adfsCont.setFormTitleIconCss("o_icon o_icon_provider_adfs");
		adfsCont.setRootForm(mainForm);
		formLayout.add(adfsCont);
		
		azureAdfsEl = uifactory.addCheckboxesHorizontal("azure.adfs.enabled", adfsCont, keys, values);
		azureAdfsEl.addActionListener(FormEvent.ONCHANGE);
		
		azureAdfsDefaultEl = uifactory.addCheckboxesHorizontal("azure.adfs.default.enabled", adfsCont, keys, values);
		azureAdfsDefaultEl.addActionListener(FormEvent.ONCHANGE);
		
		String azureAdfsTenant = oauthModule.getAzureAdfsTenant();
		azureAdfsTenantEl = uifactory.addTextElement("azure.adfs.tenant", "azure.adfs.tenant", 256, azureAdfsTenant, adfsCont);
		azureAdfsTenantEl.setHelpText(translate("azure.adfs.tenant.hint"));
		
		String azureAdfsApiKey = oauthModule.getAzureAdfsApiKey();
		azureAdfsApiKeyEl = uifactory.addTextElement("azure.adfs.id", "adfs.api.id", 256, azureAdfsApiKey, adfsCont);
		String azureAdfsApiSecret = oauthModule.getAzureAdfsApiSecret();
		azureAdfsApiSecretEl = uifactory.addTextElement("azure.adfs.secret", "adfs.api.secret", 256, azureAdfsApiSecret, adfsCont);
				
		
		if(oauthModule.isAzureAdfsEnabled()) {
			azureAdfsEl.select(keys[0], true);
		} else {
			azureAdfsApiKeyEl.setVisible(false);
			azureAdfsApiSecretEl.setVisible(false);
			azureAdfsDefaultEl.setVisible(false);
			azureAdfsTenantEl.setVisible(false);
		}
		
		if(oauthModule.isAzureAdfsRootEnabled()) {
			azureAdfsDefaultEl.select(keys[0], true);
		}
	}
	
	private void initKeycloakForm(FormItemContainer formLayout) {
		FormLayoutContainer keycloakCont = FormLayoutContainer.createDefaultFormLayout("keycloak", getTranslator());
		keycloakCont.setFormTitle(translate("keycloak.admin.title"));
		keycloakCont.setFormTitleIconCss("o_icon o_icon_provider_openid");
		keycloakCont.setRootForm(mainForm);
		formLayout.add(keycloakCont);
		
		keycloakEl = uifactory.addCheckboxesHorizontal("keycloak.enabled", keycloakCont, keys, values);
		keycloakEl.addActionListener(FormEvent.ONCHANGE);
		
		String keycloakEndpoint = oauthModule.getKeycloakEndpoint();
		keycloakEndpointEl = uifactory.addTextElement("keycloak.endpoint", "keycloak.endpoint", 256, keycloakEndpoint, keycloakCont);
		keycloakEndpointEl.setExampleKey("keycloak.endpoint.example", null);

		String keycloakRealm = oauthModule.getKeycloakRealm();
		keycloakRealmEl = uifactory.addTextElement("keycloak.realm", "keycloak.realm", 256, keycloakRealm, keycloakCont);
		
		String keycloakClientId = oauthModule.getKeycloakClientId();
		keycloakClientIdEl = uifactory.addTextElement("keycloak.id", "keycloak.api.id", 256, keycloakClientId, keycloakCont);
		String keycloakClientSecret = oauthModule.getKeycloakClientSecret();
		keycloakClientSecretEl = uifactory.addTextElement("keycloak.secret", "keycloak.api.secret", 256, keycloakClientSecret, keycloakCont);
		
		if(oauthModule.isKeycloakEnabled()) {
			keycloakEl.select(keys[0], true);
		} else {
			keycloakClientIdEl.setVisible(false);
			keycloakClientSecretEl.setVisible(false);
			keycloakEndpointEl.setVisible(false);
			keycloakRealmEl.setVisible(false);
		}
	}
	
	private void initTequilaForm(FormItemContainer formLayout) {
		FormLayoutContainer tequilaCont = FormLayoutContainer.createDefaultFormLayout("tequila", getTranslator());
		tequilaCont.setFormTitle(translate("tequila.admin.title"));
		tequilaCont.setFormTitleIconCss("o_icon o_icon_provider_tequila");
		tequilaCont.setRootForm(mainForm);
		formLayout.add(tequilaCont);
		
		tequilaEl = uifactory.addCheckboxesHorizontal("tequila.enabled", tequilaCont, keys, values);
		tequilaEl.addActionListener(FormEvent.ONCHANGE);
		
		String tequilaOAuth2Endpoint = oauthModule.getTequilaOAuth2Endpoint();
		tequilaOAuth2EndpointEl = uifactory.addTextElement("tequila.oauth2.endpoint", "tequila.oauth2.endpoint", 256, tequilaOAuth2Endpoint, tequilaCont);
		tequilaOAuth2EndpointEl.setExampleKey("tequila.oauth2.endpoint.example", null);
		
		String tequilaApiKey = oauthModule.getTequilaApiKey();
		tequilaApiKeyEl = uifactory.addTextElement("tequila.id", "tequila.api.id", 256, tequilaApiKey, tequilaCont);
		String tequilaApiSecret = oauthModule.getTequilaApiSecret();
		tequilaApiSecretEl = uifactory.addTextElement("tequila.secret", "tequila.api.secret", 256, tequilaApiSecret, tequilaCont);
		
		if(oauthModule.isTequilaEnabled()) {
			tequilaEl.select(keys[0], true);
		} else {
			tequilaApiKeyEl.setVisible(false);
			tequilaApiSecretEl.setVisible(false);
			tequilaOAuth2EndpointEl.setVisible(false);
		}
	}
	
	private void initOpenIDConnectForm(FormItemContainer formLayout) {
		FormLayoutContainer openIdConnectIFCont = FormLayoutContainer.createDefaultFormLayout("openidconnectif", getTranslator());
		openIdConnectIFCont.setFormTitle(translate("openidconnectif.admin.title"));
		openIdConnectIFCont.setFormTitleIconCss("o_icon o_icon_provider_openid");
		openIdConnectIFCont.setRootForm(mainForm);
		formLayout.add(openIdConnectIFCont);
		
		openIdConnectIFEl = uifactory.addCheckboxesHorizontal("openidconnectif.enabled", openIdConnectIFCont, keys, values);
		openIdConnectIFEl.addActionListener(FormEvent.ONCHANGE);
		
		openIdConnectIFDefaultEl = uifactory.addCheckboxesHorizontal("openidconnectif.default.enabled", openIdConnectIFCont, keys, values);
		openIdConnectIFDefaultEl.addActionListener(FormEvent.ONCHANGE);

		String openIdConnectIFApiKey = oauthModule.getOpenIdConnectIFApiKey();
		openIdConnectIFApiKeyEl = uifactory.addTextElement("openidconnectif.id", "openidconnectif.api.id", 256, openIdConnectIFApiKey, openIdConnectIFCont);
		String openIdConnectIFApiSecret = oauthModule.getOpenIdConnectIFApiSecret();
		openIdConnectIFApiSecretEl = uifactory.addTextElement("openidconnectif.secret", "openidconnectif.api.secret", 256, openIdConnectIFApiSecret, openIdConnectIFCont);
		
		String openIdConnectIFIssuer = oauthModule.getOpenIdConnectIFIssuer();
		openIdConnectIFIssuerEl = uifactory.addTextElement("openidconnectif.issuer", "openidconnectif.issuer",
				256, openIdConnectIFIssuer, openIdConnectIFCont);
		openIdConnectIFIssuerEl.setExampleKey("openidconnectif.issuer.example", null);

		String openIdConnectIFAuthorizationEndPoint = oauthModule.getOpenIdConnectIFAuthorizationEndPoint();
		openIdConnectIFAuthorizationEndPointEl = uifactory.addTextElement("openidconnectif.authorization.endpoint", "openidconnectif.authorization.endpoint",
				256, openIdConnectIFAuthorizationEndPoint, openIdConnectIFCont);
		openIdConnectIFAuthorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);

		if(oauthModule.isOpenIdConnectIFEnabled()) {
			openIdConnectIFEl.select(keys[0], true);
		} else {
			openIdConnectIFApiKeyEl.setVisible(false);
			openIdConnectIFDefaultEl.setVisible(false);
			openIdConnectIFApiSecretEl.setVisible(false);
			openIdConnectIFIssuerEl.setVisible(false);
			openIdConnectIFAuthorizationEndPointEl.setVisible(false);
		}
		
		if(oauthModule.isOpenIdConnectIFRootEnabled()) {
			openIdConnectIFDefaultEl.select(keys[0], true);
		}
	}
	
	private void initCustomProviders() {
		// remove old ones
		for(ConfigurableProviderWrapper providerWrapper:providerWrappers) {
			FormItemContainer layoutCont = providerWrapper.getLayoutCont();
			customProvidersCont.remove(layoutCont);	
		}

		providerWrappers.clear();
		List<OAuthSPI> configurableSpies = oauthModule.getAllConfigurableSPIs();
		for(OAuthSPI configurableSpi:configurableSpies) {
			if(configurableSpi instanceof OpenIdConnectFullConfigurableProvider) {
				ConfigurableProviderWrapper wrapper =
						initOpenIDConnectIFFullConfigurableProviders(customProvidersCont, (OpenIdConnectFullConfigurableProvider)configurableSpi);
				if(wrapper != null) {
					providerWrappers.add(wrapper);
				}
			}
		}
	}

	private ConfigurableProviderWrapper initOpenIDConnectIFFullConfigurableProviders(FormItemContainer formLayout, OpenIdConnectFullConfigurableProvider provider) {
		ConfigurableProviderWrapper wrapper = new ConfigurableProviderWrapper(provider);
		wrapper.initForm(formLayout);
		return wrapper;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		//linkedin
		allOk &= mandatory(linkedInEl, linkedInApiKeyEl, linkedInApiSecretEl);
		//twitter
		allOk &= mandatory(twitterEl, twitterApiKeyEl, twitterApiSecretEl);
		//google
		allOk &= mandatory(googleEl, googleApiKeyEl, googleApiSecretEl);
		//facebook
		allOk &= mandatory(facebookEl, facebookApiKeyEl, facebookApiSecretEl);
		//adfs
		allOk &= mandatory(adfsEl, adfsApiKeyEl, adfsOAuth2EndpointEl);
		//Azure ADFS
		allOk &= mandatory(azureAdfsEl, azureAdfsApiKeyEl, azureAdfsApiSecretEl);
		//tequila
		allOk &= mandatory(tequilaEl, tequilaApiKeyEl, tequilaApiSecretEl, tequilaOAuth2EndpointEl);
		//keycloak
		allOk &= mandatory(keycloakEl, keycloakClientIdEl, keycloakClientSecretEl, keycloakEndpointEl, keycloakRealmEl);
		//open id connect
		allOk &= mandatory(openIdConnectIFEl, openIdConnectIFAuthorizationEndPointEl, openIdConnectIFApiKeyEl, openIdConnectIFApiSecretEl);
		
		for(ConfigurableProviderWrapper wrapper:providerWrappers) {
			allOk &= wrapper.validateFormLogic();
		}

		return allOk;
	}
	
	private boolean mandatory(MultipleSelectionElement selectEl, TextElement... textEls) {
		boolean allOk = true;
		
		if(textEls != null) {
			for(int i=textEls.length; i-->0; ) {
				TextElement textEl = textEls[i];
				if(textEl != null) {
					textEl.clearError();
					if(selectEl.isAtLeastSelected(1)) {
						if(!StringHelper.containsNonWhitespace(textEl.getValue())) {
							textEl.setErrorKey("form.legende.mandatory", null);
							allOk = false;
						}
					}
				}
			}
		}
		
		return allOk;
	}
	
	private boolean mandatory(TextElement... textEls) {
		boolean allOk = true;
		
		if(textEls != null) {
			for(int i=textEls.length; i-->0; ) {
				TextElement textEl = textEls[i];
				if(textEl != null) {
					textEl.clearError();
					if(!StringHelper.containsNonWhitespace(textEl.getValue())) {
						textEl.setErrorKey("form.legende.mandatory", null);
						allOk &= false;
					}
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == linkedInEl) {
			linkedInApiKeyEl.setVisible(linkedInEl.isAtLeastSelected(1));
			linkedInApiSecretEl.setVisible(linkedInEl.isAtLeastSelected(1));	
		} else if(source == twitterEl) {
			twitterApiKeyEl.setVisible(twitterEl.isAtLeastSelected(1));
			twitterApiSecretEl.setVisible(twitterEl.isAtLeastSelected(1));
		} else if(source == googleEl) {
			googleApiKeyEl.setVisible(googleEl.isAtLeastSelected(1));
			googleApiSecretEl.setVisible(googleEl.isAtLeastSelected(1));
		} else if(source == facebookEl) {
			facebookApiKeyEl.setVisible(facebookEl.isAtLeastSelected(1));
			facebookApiSecretEl.setVisible(facebookEl.isAtLeastSelected(1));
		} else if(source == adfsEl) {
			adfsApiKeyEl.setVisible(adfsEl.isAtLeastSelected(1));
			adfsApiSecretEl.setVisible(adfsEl.isAtLeastSelected(1));
			adfsDefaultEl.setVisible(adfsEl.isAtLeastSelected(1));
			adfsOAuth2EndpointEl.setVisible(adfsEl.isAtLeastSelected(1));
		} else if(source == azureAdfsEl) {
			azureAdfsApiKeyEl.setVisible(azureAdfsEl.isAtLeastSelected(1));
			azureAdfsApiSecretEl.setVisible(azureAdfsEl.isAtLeastSelected(1));
			azureAdfsDefaultEl.setVisible(azureAdfsEl.isAtLeastSelected(1));
			azureAdfsTenantEl.setVisible(azureAdfsEl.isAtLeastSelected(1));
		} else if(source == keycloakEl) {
			keycloakClientIdEl.setVisible(keycloakEl.isAtLeastSelected(1));
			keycloakClientSecretEl.setVisible(keycloakEl.isAtLeastSelected(1));
			keycloakEndpointEl.setVisible(keycloakEl.isAtLeastSelected(1));
			keycloakRealmEl.setVisible(keycloakEl.isAtLeastSelected(1));
		} else if(source == tequilaEl) {
			tequilaApiKeyEl.setVisible(tequilaEl.isAtLeastSelected(1));
			tequilaApiSecretEl.setVisible(tequilaEl.isAtLeastSelected(1));
			tequilaOAuth2EndpointEl.setVisible(tequilaEl.isAtLeastSelected(1));
		}  else if(source == openIdConnectIFEl) {
			openIdConnectIFIssuerEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFApiKeyEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFDefaultEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFApiSecretEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFAuthorizationEndPointEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
		} else if(addProviderLink == source) {
			doAddOpenIDConnectIFCustom(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd())) {
				ConfigurableProviderWrapper providerWrapper = (ConfigurableProviderWrapper)link.getUserObject();
				doConfirmDelete(ureq, providerWrapper);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		oauthModule.setAllowUserCreation(userCreationEl.isAtLeastSelected(1));
		
		if(linkedInEl.isAtLeastSelected(1)) {
			oauthModule.setLinkedInEnabled(true);
			oauthModule.setLinkedInApiKey(linkedInApiKeyEl.getValue());
			oauthModule.setLinkedInApiSecret(linkedInApiSecretEl.getValue());
		} else {
			oauthModule.setLinkedInEnabled(false);
			oauthModule.setLinkedInApiKey("");
			oauthModule.setLinkedInApiSecret("");
		}
		
		if(twitterEl.isAtLeastSelected(1)) {
			oauthModule.setTwitterEnabled(true);
			oauthModule.setTwitterApiKey(twitterApiKeyEl.getValue());
			oauthModule.setTwitterApiSecret(twitterApiSecretEl.getValue());
		} else {
			oauthModule.setTwitterEnabled(false);
			oauthModule.setTwitterApiKey("");
			oauthModule.setTwitterApiSecret("");
		}
		
		if(googleEl.isAtLeastSelected(1)) {
			oauthModule.setGoogleEnabled(true);
			oauthModule.setGoogleApiKey(googleApiKeyEl.getValue());
			oauthModule.setGoogleApiSecret(googleApiSecretEl.getValue());
		} else {
			oauthModule.setGoogleEnabled(false);
			oauthModule.setGoogleApiKey("");
			oauthModule.setGoogleApiSecret("");
		}
		
		if(facebookEl.isAtLeastSelected(1)) {
			oauthModule.setFacebookEnabled(true);
			oauthModule.setFacebookApiKey(facebookApiKeyEl.getValue());
			oauthModule.setFacebookApiSecret(facebookApiSecretEl.getValue());
		} else {
			oauthModule.setFacebookEnabled(false);
			oauthModule.setFacebookApiKey("");
			oauthModule.setFacebookApiSecret("");
		}
		
		if(adfsEl.isAtLeastSelected(1)) {
			oauthModule.setAdfsEnabled(true);
			oauthModule.setAdfsApiKey(adfsApiKeyEl.getValue());
			oauthModule.setAdfsApiSecret(adfsApiSecretEl.getValue());
			oauthModule.setAdfsRootEnabled(adfsDefaultEl.isAtLeastSelected(1));
			oauthModule.setAdfsOAuth2Endpoint(adfsOAuth2EndpointEl.getValue());
		} else {
			oauthModule.setAdfsEnabled(false);
			oauthModule.setAdfsApiKey("");
			oauthModule.setAdfsApiSecret("");
			oauthModule.setAdfsRootEnabled(false);
			oauthModule.setAdfsOAuth2Endpoint("");
		}
		
		if(azureAdfsEl.isAtLeastSelected(1)) {
			oauthModule.setAzureAdfsEnabled(true);
			oauthModule.setAzureAdfsApiKey(azureAdfsApiKeyEl.getValue());
			oauthModule.setAzureAdfsApiSecret(azureAdfsApiSecretEl.getValue());
			oauthModule.setAzureAdfsRootEnabled(azureAdfsDefaultEl.isAtLeastSelected(1));
			oauthModule.setAzureAdfsTenant(azureAdfsTenantEl.getValue());
		} else {
			oauthModule.setAzureAdfsEnabled(false);
			oauthModule.setAzureAdfsApiKey("");
			oauthModule.setAzureAdfsApiSecret("");
			oauthModule.setAzureAdfsRootEnabled(false);
			oauthModule.setAzureAdfsTenant("");
		}
		
		if(keycloakEl.isAtLeastSelected(1)) {
			oauthModule.setKeycloakEnabled(true);
			oauthModule.setKeycloakClientId(keycloakClientIdEl.getValue());
			oauthModule.setKeycloakClientSecret(keycloakClientSecretEl.getValue());
			oauthModule.setKeycloakEndpoint(keycloakEndpointEl.getValue());
			oauthModule.setKeycloakRealm(keycloakRealmEl.getValue());
		} else {
			oauthModule.setKeycloakEnabled(false);
			oauthModule.setKeycloakClientId("");
			oauthModule.setKeycloakClientSecret("");
			oauthModule.setKeycloakEndpoint("");
			oauthModule.setKeycloakRealm("");
		}
		
		if(tequilaEl.isAtLeastSelected(1)) {
			oauthModule.setTequilaEnabled(true);
			oauthModule.setTequilaApiKey(tequilaApiKeyEl.getValue());
			oauthModule.setTequilaApiSecret(tequilaApiSecretEl.getValue());
			oauthModule.setTequilaOAuth2Endpoint(tequilaOAuth2EndpointEl.getValue());
		} else {
			oauthModule.setTequilaEnabled(false);
			oauthModule.setTequilaApiKey("");
			oauthModule.setTequilaApiSecret("");
			oauthModule.setTequilaOAuth2Endpoint("");
		}
		
		if(openIdConnectIFEl.isAtLeastSelected(1)) {
			oauthModule.setOpenIdConnectIFEnabled(true);
			oauthModule.setOpenIdConnectIFApiKey(openIdConnectIFApiKeyEl.getValue());
			oauthModule.setOpenIdConnectIFApiSecret(openIdConnectIFApiSecretEl.getValue());
			oauthModule.setOpenIdConnectIFRootEnabled(openIdConnectIFDefaultEl.isAtLeastSelected(1));
			oauthModule.setOpenIdConnectIFIssuer(openIdConnectIFIssuerEl.getValue());
			oauthModule.setOpenIdConnectIFAuthorizationEndPoint(openIdConnectIFAuthorizationEndPointEl.getValue());
		} else {
			oauthModule.setOpenIdConnectIFEnabled(false);
			oauthModule.setOpenIdConnectIFApiKey("");
			oauthModule.setOpenIdConnectIFApiSecret("");
			oauthModule.setOpenIdConnectIFRootEnabled(false);
			oauthModule.setOpenIdConnectIFIssuer("");
			oauthModule.setOpenIdConnectIFAuthorizationEndPoint("");
		}
		
		for(ConfigurableProviderWrapper wrapper:providerWrappers) {
			wrapper.commit();
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addConfigCtrl == source) {
			if(event == Event.DONE_EVENT) {
				initCustomProviders();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				ConfigurableProviderWrapper providerWrapper = (ConfigurableProviderWrapper)confirmDeleteCtrl.getUserObject();
				doDelete(providerWrapper);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addConfigCtrl);
		removeAsListenerAndDispose(cmc);
		addConfigCtrl = null;
		cmc = null;
	}

	private void doAddOpenIDConnectIFCustom(UserRequest ureq) {
		addConfigCtrl = new AddOpenIDConnectIFFullConfigurableController(ureq, getWindowControl());
		listenTo(addConfigCtrl);

		String title = translate("add.openidconnectif.custom");
		cmc = new CloseableModalController(getWindowControl(), null, addConfigCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, ConfigurableProviderWrapper providerWrapper) {
		OAuthSPI spi = providerWrapper.getSpi();
		String title = translate("confirm.delete.provider.title", new String[]{ spi.getProviderName() });
		String text = translate("confirm.delete.provider.text", new String[]{ spi.getProviderName() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(providerWrapper);
	}
	
	private void doDelete(ConfigurableProviderWrapper providerWrapper) {
		OAuthSPI spi = providerWrapper.getSpi();
		if(spi instanceof OpenIdConnectFullConfigurableProvider) {
			oauthModule.removeAdditionalOpenIDConnectIF(spi.getProviderName());
		}
		initCustomProviders();
	}
	
	public class ConfigurableProviderWrapper {
		
		private FormLayoutContainer openIdConnectIFCont;
		
		private MultipleSelectionElement openIdConnectIFConfEl;
		private TextElement openIdConnectIFConfName;
		private TextElement openIdConnectIFConfDisplayName;
		private TextElement openIdConnectIFConfApiKeyEl;
		private TextElement openIdConnectIFConfApiSecretEl;
		private TextElement openIdConnectIFConfIssuerEl;
		private TextElement openIdConnectIFConfAuthorizationEndPointEl;
		
		private final OpenIdConnectFullConfigurableProvider spi;
		
		public ConfigurableProviderWrapper(OpenIdConnectFullConfigurableProvider spi) {
			this.spi = spi;
		}
		
		public OpenIdConnectFullConfigurableProvider getSpi() {
			return spi;
		}
		
		public FormItemContainer getLayoutCont() {
			return openIdConnectIFCont;
		}
		
		public void initForm(FormItemContainer container) {
			String counter = Long.toString(CodeHelper.getRAMUniqueID());
			openIdConnectIFCont = FormLayoutContainer.createDefaultFormLayout("openidconnectif." + counter, getTranslator());
			openIdConnectIFCont.setFormTitle(translate("openidconnectif.admin.custom.title", new String[]{ spi.getProviderName() }));
			openIdConnectIFCont.setFormTitleIconCss("o_icon o_icon_provider_openid");
			openIdConnectIFCont.setRootForm(mainForm);
			container.add(openIdConnectIFCont);
			openIdConnectIFConfEl = uifactory.addCheckboxesHorizontal("openidconnectif." + counter + ".default.enabled", "openidconnectif.default.enabled", openIdConnectIFCont, keys, values);
			if(spi.isRootEnabled()) {
				openIdConnectIFConfEl.select(keys[0], true);
			}
			
			String providerName = spi.getProviderName();
			openIdConnectIFConfName = uifactory.addTextElement("openidconnectif." + counter + ".name", "openidconnectif.name", 256, providerName, openIdConnectIFCont);
			openIdConnectIFConfName.setEnabled(false);
			
			String displayName = spi.getDisplayName();
			openIdConnectIFConfDisplayName = uifactory.addTextElement("openidconnectif." + counter + ".displayname", "openidconnectif.displayname", 256, displayName, openIdConnectIFCont);
			String apiKey = spi.getAppKey();
			openIdConnectIFConfApiKeyEl = uifactory.addTextElement("openidconnectif." + counter + ".id", "openidconnectif.api.id", 256, apiKey, openIdConnectIFCont);
			String apiSecret = spi.getAppSecret();
			openIdConnectIFConfApiSecretEl = uifactory.addTextElement("openidconnectif." + counter + ".secret", "openidconnectif.api.secret", 256, apiSecret, openIdConnectIFCont);
			String issuer = spi.getIssuer();
			openIdConnectIFConfIssuerEl = uifactory.addTextElement("openidconnectif." + counter + ".issuer", "openidconnectif.issuer", 256, issuer, openIdConnectIFCont);
			openIdConnectIFConfIssuerEl.setExampleKey("openidconnectif.issuer.example", null);
			String endPoint = spi.getEndPoint();
			openIdConnectIFConfAuthorizationEndPointEl = uifactory.addTextElement("openidconnectif." + counter + ".authorization.endpoint", "openidconnectif.authorization.endpoint", 256, endPoint, openIdConnectIFCont);
			openIdConnectIFConfAuthorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);

			FormLink deleteButton  = uifactory.addFormLink("delete.".concat(counter), "delete", "delete", null, openIdConnectIFCont, Link.BUTTON);
			deleteButton.setUserObject(this);
		}

		protected boolean validateFormLogic() {
			boolean allOk = true;
			
			allOk &= mandatory(openIdConnectIFConfName, openIdConnectIFConfDisplayName, openIdConnectIFConfApiKeyEl, openIdConnectIFConfApiSecretEl,
					openIdConnectIFConfIssuerEl, openIdConnectIFConfAuthorizationEndPointEl);
			
			return allOk;
		}
		
		protected void commit() {
			String displayName = openIdConnectIFConfDisplayName.getValue();
			String issuer = openIdConnectIFConfIssuerEl.getValue();
			String endPoint = openIdConnectIFConfAuthorizationEndPointEl.getValue();
			String apiKey = openIdConnectIFConfApiKeyEl.getValue();
			String apiSecret = openIdConnectIFConfApiSecretEl.getValue();
			boolean rootEnabled = openIdConnectIFConfEl.isAtLeastSelected(1);
			oauthModule.setAdditionalOpenIDConnectIF(spi.getProviderName(), displayName, rootEnabled, issuer, endPoint, apiKey, apiSecret);
		}
	}
}
