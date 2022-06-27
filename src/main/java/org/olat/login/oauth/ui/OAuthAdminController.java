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
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.Dropdown.SpacerItem;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthDisplayName;
import org.olat.login.oauth.OAuthLoginModule;
import org.olat.login.oauth.OAuthMapping;
import org.olat.login.oauth.OAuthSPI;
import org.olat.login.oauth.spi.GenericOAuth2Provider;
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
	private FormLink addDiscoveryLink;
	private FormLink addOAuth20ProviderLink;

	private MultipleSelectionElement userCreationEl;
	private MultipleSelectionElement skipDisclaimerEl;
	private MultipleSelectionElement skipRegistrationEl;
	
	private TextElement linkedInApiKeyEl;
	private TextElement linkedInApiSecretEl;
	
	private TextElement twitterApiKeyEl;
	private TextElement twitterApiSecretEl;
	
	private TextElement googleApiKeyEl;
	private TextElement googleApiSecretEl;
	
	private TextElement facebookApiKeyEl;
	private TextElement facebookApiSecretEl;
	
	private MultipleSelectionElement adfsDefaultEl;
	private TextElement adfsApiKeyEl;
	private TextElement adfsApiSecretEl;
	private TextElement adfsOAuth2EndpointEl;
	
	private MultipleSelectionElement azureAdfsDefaultEl;
	private TextElement azureAdfsApiKeyEl;
	private TextElement azureAdfsApiSecretEl;
	private TextElement azureAdfsTenantEl;
	
	private TextElement tequilaApiKeyEl;
	private TextElement tequilaApiSecretEl;
	private TextElement tequilaOAuth2EndpointEl;

	private MultipleSelectionElement switchEduIDDefaultEl;
	private TextElement switchEduIDApiKeyEl;
	private TextElement switchEduIDApiSecretEl;
	
	private MultipleSelectionElement datenlotsenDefaultEl;
	private TextElement datenlotsenKeyEl;
	private TextElement datenlotsenSecretEl;
	private TextElement datenlotsenEndpointEl;
	
	private MultipleSelectionElement keycloakDefaultEl;
	private TextElement keycloakClientIdEl;
	private TextElement keycloakClientSecretEl;
	private TextElement keycloakEndpointEl;
	private TextElement keycloakRealmEl;
	
	private MultipleSelectionElement openIdConnectIFDefaultEl;
	private TextElement openIdConnectIFApiKeyEl;
	private TextElement openIdConnectIFApiSecretEl;
	private TextElement openIdConnectIFIssuerEl;
	private TextElement openIdConnectIFAuthorizationEndPointEl;
	
	private FormLayoutContainer additionalProvidersCont;

	private CloseableModalController cmc;
	private DialogBoxController confirmDeleteCtrl;
	private AddConfigurableController addConfigCtrl;
	private OAuthMappingEditController editMappingCtrl;
	private AddDiscoveryURLController addDiscoveryUrlCtrl;
	private AddOpenIDConnectIFFullConfigurableController addIfConfigCtrl;
	
	private int counter = 0;
	private final List<OAuthSPI> allConfigurableSpis;
	private List<ProviderWrapper> wrappers = new ArrayList<>();
	private List<ProviderWrapper> providerWrappers = new ArrayList<>();
	
	@Autowired
	private OAuthLoginModule oauthModule;
	
	public OAuthAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		allConfigurableSpis = oauthModule.getAllSPIs().stream()
				.filter(spi -> !"GetTo".equals(spi.getName()) && !"panther".equals(spi.getName()))
				.collect(Collectors.toUnmodifiableList());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initAddProviderDropdown(formLayout);
		initGeneralSettings(formLayout);
		for(OAuthSPI spi:allConfigurableSpis) {
			initProviderForm(formLayout, spi);
		}

		additionalProvidersCont = FormLayoutContainer.createBareBoneFormLayout("additional.providers", getTranslator());
		additionalProvidersCont.setRootForm(mainForm);
		formLayout.add(additionalProvidersCont);
		
		//highly configurable providers
		initCustomProviders(additionalProvidersCont);
		
		//buttons
		FormLayoutContainer buttonBonesCont = FormLayoutContainer.createDefaultFormLayout("button_bones", getTranslator());
		buttonBonesCont.setRootForm(mainForm);
		formLayout.add(buttonBonesCont);
		uifactory.addSpacerElement("buttons-spacer", buttonBonesCont, true);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonBonesCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private void initProviderForm(FormItemContainer formLayout, OAuthSPI spi) {
		String spiName = spi.getName();
		FormLayoutContainer container = null;
		switch(spiName) {
			case "linkedin": container = initLinkedInForm(formLayout); break;
			case "twitter": container = initTwitterForm(formLayout); break;
			case "google": container = initGoogleForm(formLayout); break;
			case "facebook": container = initFacebookForm(formLayout); break;
			case "adfs": container = initAdfsForm(formLayout); break;
			case "azureAdfs": container = initAzureAdfsForm(formLayout); break;
			case "keycloak": container = initKeycloakForm(formLayout); break;
			case "switcheduid": container = initSwitchEduIDForm(formLayout); break;
			case "datenlotsen": container = initDatenlotsenForm(formLayout); break;
			case "tequila": container = initTequilaForm(formLayout); break;
			case "OpenIDConnect": container = initOpenIDConnectForm(formLayout); break;
			default: {
				if(!(spi instanceof GenericOAuth2Provider) && !(spi instanceof OpenIdConnectFullConfigurableProvider)) {
					getLogger().error("Cannot find container for provider: {}", spiName);
				}
			}
		}
		if(container != null) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons." + (counter++), getTranslator());
			container.add(buttonsCont);

			FormLink disableButton = uifactory.addFormLink("disable." + counter, "disable", "disable", null, buttonsCont, Link.BUTTON);
			disableButton.setUserObject(spi);
			
			if(spi instanceof OAuthMapping) {
				FormLink mappingButton = uifactory.addFormLink("mapping." + counter, "mapping", "edit.mapping", null, buttonsCont, Link.BUTTON);
				mappingButton.setUserObject(spi);
			}
			wrappers.add(new ProviderWrapper(spi, container));
		}
	}
	
	private ProviderWrapper getProviderWrapper(OAuthSPI spi) {
		return wrappers.stream()
				.filter(p -> p.getProvider() == spi)
				.findFirst().orElse(null);
	}
	
	private void initAddProviderDropdown(FormItemContainer formLayout) {
		String addPage = velocity_root + "/add_provider.html";
		FormLayoutContainer addCont = FormLayoutContainer.createCustomFormLayout("add.provider.cont", getTranslator(), addPage);
		addCont.setRootForm(mainForm);
		formLayout.add(addCont);
		
		DropdownItem addDropdown = uifactory.addDropdownMenu("add.provider", "add.provider", addCont, getTranslator());
		addDropdown.setCarretIconCSS("o_icon o_icon-fw o_icon_commands");
		addDropdown.setOrientation(DropdownOrientation.right);

		for(OAuthSPI spi:allConfigurableSpis) {
			if(!spi.isEnabled()) {
				String spiName = spi.getName();
				String i18nKey = providerI18nKey(spi);
				FormLink addLink = uifactory.addFormLink("provider." + spiName, "add",  i18nKey, null, addCont, Link.LINK);
				addLink.setIconLeftCSS("o_icon o_icon-fw " + spi.getIconCSS());
				addLink.setUserObject(spi);
				addDropdown.addElement(addLink);
			}
		}
		
		addDropdown.addElement(new SpacerItem("spacer"));
		
		addOAuth20ProviderLink = uifactory.addFormLink("add.oauth.20", addCont, Link.LINK);
		addOAuth20ProviderLink.setIconLeftCSS("o_icon o_icon-fw o_icon o_icon_provider_oauth");
		addDropdown.addElement(addOAuth20ProviderLink);
		
		addDiscoveryLink = uifactory.addFormLink("add.discovery.url", addCont, Link.LINK);
		addDiscoveryLink.setIconLeftCSS("o_icon o_icon-fw o_icon o_icon_provider_oauth");
		addDropdown.addElement(addDiscoveryLink);
		
		addProviderLink = uifactory.addFormLink("add.openidconnect.custom", addCont, Link.LINK);
		addProviderLink.setIconLeftCSS("o_icon o_icon-fw o_icon o_icon_provider_openid");
		addDropdown.addElement(addProviderLink);
	}
	
	private String providerI18nKey(OAuthSPI spi) {
		String i18nKey = spi.getName().toLowerCase() + ".add.title";
		// For backwards compatibility as the key for OAuth provider are often customized
		return i18nKey
				.replace("azureadfs", "azure.adfs")
				.replace("openidconnect.", "openidconnectif.");
	}

	private void initGeneralSettings(FormItemContainer formLayout) {
		FormLayoutContainer oauthCont = FormLayoutContainer.createDefaultFormLayout("oauth", getTranslator());
		oauthCont.setFormTitle(translate("oauth.admin.title"));
		oauthCont.setRootForm(mainForm);
		formLayout.add(oauthCont);

		userCreationEl = uifactory.addCheckboxesHorizontal("user.creation.enabled", oauthCont, keys, values);
		userCreationEl.addActionListener(FormEvent.ONCHANGE);
		if(oauthModule.isAllowUserCreation()) {
			userCreationEl.select(keys[0], true);
		}
		skipDisclaimerEl = uifactory.addCheckboxesHorizontal("skip.disclaimer", oauthCont, keys, values);
		skipDisclaimerEl.select(keys[0], oauthModule.isSkipDisclaimerDialog());
		skipRegistrationEl = uifactory.addCheckboxesHorizontal("skip.user.registration", oauthCont, keys, values);
		skipRegistrationEl.select(keys[0], oauthModule.isSkipRegistrationDialog());

		String callbackUrl = "<span class='o_copy_code o_nowrap'><input type='text' value='" + oauthModule.getCallbackUrl() + "' onclick='this.select()'/></span>";
		uifactory.addStaticTextElement("oauth.redirect.uri", callbackUrl, oauthCont);
	}
	
	private FormLayoutContainer initLinkedInForm(FormItemContainer formLayout) {
		FormLayoutContainer linkedInCont = FormLayoutContainer.createDefaultFormLayout("linkedin", getTranslator());
		linkedInCont.setFormTitle(translate("linkedin.admin.title"));
		linkedInCont.setFormTitleIconCss("o_icon o_icon_provider_linkedin");
		linkedInCont.setVisible(oauthModule.isLinkedInEnabled());
		linkedInCont.setRootForm(mainForm);
		formLayout.add(linkedInCont);

		String apiKey = oauthModule.getLinkedInApiKey();
		linkedInApiKeyEl = uifactory.addTextElement("linkedin.id", "linkedin.api.id", 256, apiKey, linkedInCont);
		linkedInApiKeyEl.setMandatory(true);
		String apiSecret = oauthModule.getLinkedInApiSecret();
		linkedInApiSecretEl = uifactory.addTextElement("linkedin.secret", "linkedin.api.secret", 256, apiSecret, linkedInCont);
		linkedInApiSecretEl.setMandatory(true);
		return linkedInCont;
	}
	
	private FormLayoutContainer initTwitterForm(FormItemContainer formLayout) {
		FormLayoutContainer twitterCont = FormLayoutContainer.createDefaultFormLayout("twitter", getTranslator());
		twitterCont.setFormTitle(translate("twitter.admin.title"));
		twitterCont.setFormTitleIconCss("o_icon o_icon_provider_twitter");
		twitterCont.setVisible(oauthModule.isTwitterEnabled());
		twitterCont.setRootForm(mainForm);
		formLayout.add(twitterCont);

		String twitterApiKey = oauthModule.getTwitterApiKey();
		twitterApiKeyEl = uifactory.addTextElement("twitter.id", "twitter.api.id", 256, twitterApiKey, twitterCont);
		twitterApiKeyEl.setMandatory(true);
		String twitterApiSecret = oauthModule.getTwitterApiSecret();
		twitterApiSecretEl = uifactory.addTextElement("twitter.secret", "twitter.api.secret", 256, twitterApiSecret, twitterCont);
		twitterApiSecretEl.setMandatory(true);
		return twitterCont;
	}
	
	private FormLayoutContainer initGoogleForm(FormItemContainer formLayout) {
		FormLayoutContainer googleCont = FormLayoutContainer.createDefaultFormLayout("google", getTranslator());
		googleCont.setFormTitle(translate("google.admin.title"));
		googleCont.setFormTitleIconCss("o_icon o_icon_provider_google");
		googleCont.setVisible(oauthModule.isGoogleEnabled());
		googleCont.setRootForm(mainForm);
		formLayout.add(googleCont);
		
		String googleApiKey = oauthModule.getGoogleApiKey();
		googleApiKeyEl = uifactory.addTextElement("google.id", "google.api.id", 256, googleApiKey, googleCont);
		googleApiKeyEl.setMandatory(true);
		String googleApiSecret = oauthModule.getGoogleApiSecret();
		googleApiSecretEl = uifactory.addTextElement("google.secret", "google.api.secret", 256, googleApiSecret, googleCont);
		googleApiSecretEl.setMandatory(true);
		return googleCont;
	}
	
	private FormLayoutContainer initFacebookForm(FormItemContainer formLayout) {
		FormLayoutContainer facebookCont = FormLayoutContainer.createDefaultFormLayout("facebook", getTranslator());
		facebookCont.setFormTitle(translate("facebook.admin.title"));
		facebookCont.setFormTitleIconCss("o_icon o_icon_provider_facebook");
		facebookCont.setVisible(oauthModule.isFacebookEnabled());
		facebookCont.setRootForm(mainForm);
		formLayout.add(facebookCont);
		
		String facebookApiKey = oauthModule.getFacebookApiKey();
		facebookApiKeyEl = uifactory.addTextElement("facebook.id", "facebook.api.id", 256, facebookApiKey, facebookCont);
		facebookApiKeyEl.setMandatory(true);
		String facebookApiSecret = oauthModule.getFacebookApiSecret();
		facebookApiSecretEl = uifactory.addTextElement("facebook.secret", "facebook.api.secret", 256, facebookApiSecret, facebookCont);
		facebookApiSecretEl.setMandatory(true);
		return facebookCont;
	}
	
	private FormLayoutContainer initAdfsForm(FormItemContainer formLayout) {
		FormLayoutContainer adfsCont = FormLayoutContainer.createDefaultFormLayout("adfs", getTranslator());
		adfsCont.setFormTitle(translate("adfs.admin.title"));
		adfsCont.setFormTitleIconCss("o_icon o_icon_provider_adfs");
		adfsCont.setVisible(oauthModule.isAdfsEnabled());
		adfsCont.setRootForm(mainForm);
		formLayout.add(adfsCont);
		
		adfsDefaultEl = uifactory.addCheckboxesHorizontal("adfs.default.enabled", adfsCont, keys, values);
		if(oauthModule.isAdfsRootEnabled()) {
			adfsDefaultEl.select(keys[0], true);
		}
		
		String adfsOAuth2Endpoint = oauthModule.getAdfsOAuth2Endpoint();
		adfsOAuth2EndpointEl = uifactory.addTextElement("adfs.oauth2.endpoint", "adfs.oauth2.endpoint", 256, adfsOAuth2Endpoint, adfsCont);
		adfsOAuth2EndpointEl.setExampleKey("adfs.oauth2.endpoint.example", null);
		adfsOAuth2EndpointEl.setMandatory(true);
		
		String adfsApiKey = oauthModule.getAdfsApiKey();
		adfsApiKeyEl = uifactory.addTextElement("adfs.id", "adfs.api.id", 256, adfsApiKey, adfsCont);
		adfsApiKeyEl.setMandatory(true);
		String adfsApiSecret = oauthModule.getAdfsApiSecret();
		adfsApiSecretEl = uifactory.addTextElement("adfs.secret", "adfs.api.secret", 256, adfsApiSecret, adfsCont);
		adfsApiSecretEl.setHelpText(translate("adfs.api.secret.hint"));
		return adfsCont;
	}
	
	private FormLayoutContainer initAzureAdfsForm(FormItemContainer formLayout) {
		FormLayoutContainer azureAdfsCont = FormLayoutContainer.createDefaultFormLayout("azuredfs", getTranslator());
		azureAdfsCont.setFormTitle(translate("azure.adfs.admin.title"));
		azureAdfsCont.setFormTitleIconCss("o_icon o_icon_provider_adfs");
		azureAdfsCont.setVisible(oauthModule.isAzureAdfsEnabled());
		azureAdfsCont.setRootForm(mainForm);
		formLayout.add(azureAdfsCont);
		
		azureAdfsDefaultEl = uifactory.addCheckboxesHorizontal("azure.adfs.default.enabled", azureAdfsCont, keys, values);
		azureAdfsDefaultEl.addActionListener(FormEvent.ONCHANGE);
		if(oauthModule.isAzureAdfsRootEnabled()) {
			azureAdfsDefaultEl.select(keys[0], true);
		}
		
		String azureAdfsTenant = oauthModule.getAzureAdfsTenant();
		azureAdfsTenantEl = uifactory.addTextElement("azure.adfs.tenant", "azure.adfs.tenant", 256, azureAdfsTenant, azureAdfsCont);
		azureAdfsTenantEl.setHelpText(translate("azure.adfs.tenant.hint"));
		
		String azureAdfsApiKey = oauthModule.getAzureAdfsApiKey();
		azureAdfsApiKeyEl = uifactory.addTextElement("azure.adfs.id", "adfs.api.id", 256, azureAdfsApiKey, azureAdfsCont);
		azureAdfsApiKeyEl.setMandatory(true);
		String azureAdfsApiSecret = oauthModule.getAzureAdfsApiSecret();
		azureAdfsApiSecretEl = uifactory.addTextElement("azure.adfs.secret", "adfs.api.secret", 256, azureAdfsApiSecret, azureAdfsCont);
		azureAdfsApiSecretEl.setMandatory(true);
		return azureAdfsCont;
	}
	
	private FormLayoutContainer initKeycloakForm(FormItemContainer formLayout) {
		FormLayoutContainer keycloakCont = FormLayoutContainer.createDefaultFormLayout("keycloak", getTranslator());
		keycloakCont.setFormTitle(translate("keycloak.admin.title"));
		keycloakCont.setFormTitleIconCss("o_icon o_icon_provider_openid");
		keycloakCont.setVisible(oauthModule.isKeycloakEnabled());
		keycloakCont.setRootForm(mainForm);
		formLayout.add(keycloakCont);

		keycloakDefaultEl = uifactory.addCheckboxesHorizontal("keycloak.default.enabled", keycloakCont, keys, values);
		if(oauthModule.isKeycloakRootEnabled()) {
			keycloakDefaultEl.select(keys[0], true);
		}
		
		String keycloakEndpoint = oauthModule.getKeycloakEndpoint();
		keycloakEndpointEl = uifactory.addTextElement("keycloak.endpoint", "keycloak.endpoint", 256, keycloakEndpoint, keycloakCont);
		keycloakEndpointEl.setExampleKey("keycloak.endpoint.example", null);
		keycloakEndpointEl.setMandatory(true);

		String keycloakRealm = oauthModule.getKeycloakRealm();
		keycloakRealmEl = uifactory.addTextElement("keycloak.realm", "keycloak.realm", 256, keycloakRealm, keycloakCont);
		
		String keycloakClientId = oauthModule.getKeycloakClientId();
		keycloakClientIdEl = uifactory.addTextElement("keycloak.id", "keycloak.api.id", 256, keycloakClientId, keycloakCont);
		keycloakClientIdEl.setMandatory(true);
		String keycloakClientSecret = oauthModule.getKeycloakClientSecret();
		keycloakClientSecretEl = uifactory.addTextElement("keycloak.secret", "keycloak.api.secret", 256, keycloakClientSecret, keycloakCont);
		keycloakClientSecretEl.setMandatory(true);
		return keycloakCont;
	}
	
	private FormLayoutContainer initTequilaForm(FormItemContainer formLayout) {
		FormLayoutContainer tequilaCont = FormLayoutContainer.createDefaultFormLayout("tequila", getTranslator());
		tequilaCont.setFormTitle(translate("tequila.admin.title"));
		tequilaCont.setFormTitleIconCss("o_icon o_icon_provider_tequila");
		tequilaCont.setVisible(oauthModule.isTequilaEnabled());
		tequilaCont.setRootForm(mainForm);
		formLayout.add(tequilaCont);

		String tequilaOAuth2Endpoint = oauthModule.getTequilaOAuth2Endpoint();
		tequilaOAuth2EndpointEl = uifactory.addTextElement("tequila.oauth2.endpoint", "tequila.oauth2.endpoint", 256, tequilaOAuth2Endpoint, tequilaCont);
		tequilaOAuth2EndpointEl.setExampleKey("tequila.oauth2.endpoint.example", null);
		tequilaOAuth2EndpointEl.setMandatory(true);
		
		String tequilaApiKey = oauthModule.getTequilaApiKey();
		tequilaApiKeyEl = uifactory.addTextElement("tequila.id", "tequila.api.id", 256, tequilaApiKey, tequilaCont);
		tequilaApiKeyEl.setMandatory(true);
		String tequilaApiSecret = oauthModule.getTequilaApiSecret();
		tequilaApiSecretEl = uifactory.addTextElement("tequila.secret", "tequila.api.secret", 256, tequilaApiSecret, tequilaCont);
		tequilaApiSecretEl.setMandatory(true);
		return tequilaCont;
	}
	
	private FormLayoutContainer initSwitchEduIDForm(FormItemContainer formLayout) {
		FormLayoutContainer switchEduIdCont = FormLayoutContainer.createDefaultFormLayout("switchEduID", getTranslator());
		switchEduIdCont.setFormTitle(translate("switcheduid.admin.title"));
		switchEduIdCont.setFormTitleIconCss("o_icon o_icon_provider_switch_eduid");
		switchEduIdCont.setVisible(oauthModule.isSwitchEduIDEnabled());
		switchEduIdCont.setRootForm(mainForm);
		formLayout.add(switchEduIdCont);
		
		switchEduIDDefaultEl = uifactory.addCheckboxesHorizontal("switcheduid.default.enabled", switchEduIdCont, keys, values);
		switchEduIDDefaultEl.select(keys[0], oauthModule.isSwitchEduIDRootEnabled());
		
		String apiKey = oauthModule.getSwitchEduIDApiKey();
		switchEduIDApiKeyEl = uifactory.addTextElement("switcheduid.id", "switcheduid.api.id", 256, apiKey, switchEduIdCont);
		switchEduIDApiKeyEl.setMandatory(true);
		String apiSecret = oauthModule.getSwitchEduIDApiSecret();
		switchEduIDApiSecretEl = uifactory.addTextElement("switcheduid.secret", "switcheduid.api.secret", 256, apiSecret, switchEduIdCont);
		switchEduIDApiSecretEl.setMandatory(true);
		return switchEduIdCont;
	}
	
	private FormLayoutContainer initDatenlotsenForm(FormItemContainer formLayout) {
		FormLayoutContainer datenlotsenCont = FormLayoutContainer.createDefaultFormLayout("datenlotsen", getTranslator());
		datenlotsenCont.setFormTitle(translate("datenlotsen.admin.title"));
		datenlotsenCont.setFormTitleIconCss("o_icon o_icon_provider_datenlotsen");
		datenlotsenCont.setVisible(oauthModule.isDatenlotsenEnabled());
		datenlotsenCont.setRootForm(mainForm);
		formLayout.add(datenlotsenCont);

		datenlotsenDefaultEl = uifactory.addCheckboxesHorizontal("datenlotsen.default.enabled", datenlotsenCont, keys, values);
		datenlotsenDefaultEl.select(keys[0], oauthModule.isDatenlotsenRootEnabled());
		
		String apiKey = oauthModule.getDatenlotsenApiKey();
		datenlotsenKeyEl = uifactory.addTextElement("datenlotsen.id", "datenlotsen.api.id", 256, apiKey, datenlotsenCont);
		datenlotsenKeyEl.setMandatory(true);
		String apiSecret = oauthModule.getDatenlotsenApiSecret();
		datenlotsenSecretEl = uifactory.addTextElement("datenlotsen.secret", "datenlotsen.api.secret", 256, apiSecret, datenlotsenCont);
		datenlotsenSecretEl.setMandatory(true);
		String endpoint = oauthModule.getDatenlotsenEndpoint();
		datenlotsenEndpointEl = uifactory.addTextElement("datenlotsen.endpoint", "datenlotsen.api.endpoint", 256, endpoint, datenlotsenCont);
		datenlotsenEndpointEl.setExampleKey("datenlotsen.api.endpoint.hint", null);
		datenlotsenEndpointEl.setMandatory(true);
		return datenlotsenCont;
	}
	
	private FormLayoutContainer initOpenIDConnectForm(FormItemContainer formLayout) {
		FormLayoutContainer openIdConnectIFCont = FormLayoutContainer.createDefaultFormLayout("openidconnectif", getTranslator());
		openIdConnectIFCont.setFormTitle(translate("openidconnectif.admin.title"));
		openIdConnectIFCont.setFormTitleIconCss("o_icon o_icon_provider_openid");
		openIdConnectIFCont.setVisible(oauthModule.isOpenIdConnectIFEnabled());
		openIdConnectIFCont.setRootForm(mainForm);
		formLayout.add(openIdConnectIFCont);

		openIdConnectIFDefaultEl = uifactory.addCheckboxesHorizontal("openidconnectif.default.enabled", openIdConnectIFCont, keys, values);
		if(oauthModule.isOpenIdConnectIFRootEnabled()) {
			openIdConnectIFDefaultEl.select(keys[0], true);
		}

		String openIdConnectIFApiKey = oauthModule.getOpenIdConnectIFApiKey();
		openIdConnectIFApiKeyEl = uifactory.addTextElement("openidconnectif.id", "openidconnectif.api.id", 256, openIdConnectIFApiKey, openIdConnectIFCont);
		String openIdConnectIFApiSecret = oauthModule.getOpenIdConnectIFApiSecret();
		openIdConnectIFApiKeyEl.setMandatory(true);
		openIdConnectIFApiSecretEl = uifactory.addTextElement("openidconnectif.secret", "openidconnectif.api.secret", 256, openIdConnectIFApiSecret, openIdConnectIFCont);
		openIdConnectIFApiSecretEl.setMandatory(true);
		
		String openIdConnectIFIssuer = oauthModule.getOpenIdConnectIFIssuer();
		openIdConnectIFIssuerEl = uifactory.addTextElement("openidconnectif.issuer", "openidconnectif.issuer",
				256, openIdConnectIFIssuer, openIdConnectIFCont);
		openIdConnectIFIssuerEl.setExampleKey("openidconnectif.issuer.example", null);
		openIdConnectIFIssuerEl.setMandatory(true);
		
		String openIdConnectIFAuthorizationEndPoint = oauthModule.getOpenIdConnectIFAuthorizationEndPoint();
		openIdConnectIFAuthorizationEndPointEl = uifactory.addTextElement("openidconnectif.authorization.endpoint", "openidconnectif.authorization.endpoint",
				256, openIdConnectIFAuthorizationEndPoint, openIdConnectIFCont);
		openIdConnectIFAuthorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);
		openIdConnectIFAuthorizationEndPointEl.setMandatory(true);
		return openIdConnectIFCont;
	}
	
	private void initCustomProviders(FormItemContainer formLayout) {
		// remove old ones
		for(ProviderWrapper providerWrapper:providerWrappers) {
			FormItemContainer layoutCont = providerWrapper.getProviderContainer();
			formLayout.remove(layoutCont);	
		}

		providerWrappers.clear();
		List<OAuthSPI> configurableSpies = oauthModule.getAllConfigurableSPIs();
		for(OAuthSPI configurableSpi:configurableSpies) {
			if(configurableSpi instanceof OpenIdConnectFullConfigurableProvider) {
				ConfigurableProviderWrapper wrapper =
						initOpenIDConnectIFFullConfigurableProviders(formLayout, (OpenIdConnectFullConfigurableProvider)configurableSpi);
				providerWrappers.add(wrapper);
			} else if(configurableSpi instanceof GenericOAuth2Provider) {
				GenericOAuth2ProviderWrapper wrapper = initGenericOAuth2Providers(formLayout, (GenericOAuth2Provider)configurableSpi);
				providerWrappers.add(wrapper);
			}
		}
	}

	private ConfigurableProviderWrapper initOpenIDConnectIFFullConfigurableProviders(FormItemContainer formLayout, OpenIdConnectFullConfigurableProvider provider) {
		ConfigurableProviderWrapper wrapper = new ConfigurableProviderWrapper(provider);
		wrapper.initForm(formLayout);
		return wrapper;
	}
	
	private GenericOAuth2ProviderWrapper initGenericOAuth2Providers(FormItemContainer formLayout, GenericOAuth2Provider provider) {
		GenericOAuth2ProviderWrapper wrapper = new GenericOAuth2ProviderWrapper(provider);
		wrapper.initForm(formLayout);
		return wrapper;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(ProviderWrapper wrapper:wrappers) {
			allOk &= mandatory(wrapper.getProviderContainer());
		}
		
		for(ProviderWrapper wrapper:providerWrappers) {
			if(wrapper instanceof ConfigurableProviderWrapper) {
				allOk &= ((ConfigurableProviderWrapper)wrapper).validateFormLogic();
			} else if(wrapper instanceof GenericOAuth2ProviderWrapper) {
				allOk &= ((GenericOAuth2ProviderWrapper)wrapper).validateFormLogic();
			}
		}

		return allOk;
	}
	
	private boolean mandatory(FormLayoutContainer container) {
		boolean allOk = true;
		
		for(FormItem item:container.getFormItems()) {
			if(item instanceof TextElement) {
				TextElement textEl = (TextElement)item;
				textEl.clearError();
				if(container.isVisible() && !StringHelper.containsNonWhitespace(textEl.getValue())) {
					textEl.setErrorKey("form.legende.mandatory", null);
					allOk = false;
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
		if(userCreationEl == source) {
			skipDisclaimerEl.setVisible(userCreationEl.isAtLeastSelected(1));
			skipRegistrationEl.setVisible(userCreationEl.isAtLeastSelected(1));
		} else if(addProviderLink == source) {
			doAddOpenIDConnectIFCustom(ureq);
		} else if(addDiscoveryLink == source) {
			doAddDiscovery(ureq);
		} else if(addOAuth20ProviderLink == source) {
			doAddConfiguration(ureq, null);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("delete".equals(link.getCmd()) || "disable".equals(link.getCmd())) {
				OAuthSPI spi = (OAuthSPI)link.getUserObject();
				doConfirmDelete(ureq, spi);
			} else if("mapping".equals(link.getCmd())) {
				OAuthSPI spi = (OAuthSPI)link.getUserObject();
				doEditMapping(ureq, spi);
			} else if("add".equals(link.getCmd()) && link.getUserObject() instanceof OAuthSPI) {
				ProviderWrapper providerWrapper = getProviderWrapper((OAuthSPI)link.getUserObject());
				if(providerWrapper != null) {
					providerWrapper.getProviderContainer().setVisible(true);
				}
				markDirty();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(userCreationEl.isAtLeastSelected(1)) {
			oauthModule.setAllowUserCreation(true);
			oauthModule.setSkipDisclaimerDialog(skipDisclaimerEl.isAtLeastSelected(1));
			oauthModule.setSkipRegistrationDialog(skipRegistrationEl.isAtLeastSelected(1));
		} else {
			oauthModule.setAllowUserCreation(false);
			oauthModule.setSkipDisclaimerDialog(false);
			oauthModule.setSkipRegistrationDialog(false);
		}
		
		for(ProviderWrapper wrapper:wrappers) {
			commit(wrapper);
		}
		
		for(ProviderWrapper wrapper:providerWrappers) {
			if(wrapper instanceof ConfigurableProviderWrapper) {
				((ConfigurableProviderWrapper)wrapper).commit();
			} else if(wrapper instanceof GenericOAuth2ProviderWrapper) {
				((GenericOAuth2ProviderWrapper)wrapper).commit();
			}
		}
	}
	
	private void commit(ProviderWrapper providerWrapper) {
		String spiName = providerWrapper.getProvider().getName();
		boolean enabled = providerWrapper.getProviderContainer().isVisible();
		switch(spiName) {
			case "linkedin": commitLinkedIn(enabled); break;
			case "twitter": commitTwitter(enabled); break;
			case "google": commitGoogle(enabled); break;
			case "facebook": commitFacebook(enabled); break;
			case "adfs": commitAdfs(enabled); break;
			case "azureAdfs": commitAzureAdfs(enabled); break;
			case "keycloak": commitKeycloak(enabled); break;
			case "switcheduid": commitSwitchEduID(enabled); break;
			case "datenlotsen": commitDatenlotsen(enabled); break;
			case "tequila": commitTequila(enabled); break;
			case "OpenIDConnect": commitOpenIdConnectIF(enabled); break;
			default: {
				getLogger().error("Cannot save provider: {}", spiName);
			}
		}
	}
	
	private void commitLinkedIn(boolean enabled) {
		if(enabled) {
			oauthModule.setLinkedInEnabled(true);
			oauthModule.setLinkedInApiKey(linkedInApiKeyEl.getValue());
			oauthModule.setLinkedInApiSecret(linkedInApiSecretEl.getValue());
		} else {
			oauthModule.setLinkedInEnabled(false);
			oauthModule.setLinkedInApiKey("");
			oauthModule.setLinkedInApiSecret("");
		}
	}

	private void commitTwitter(boolean enabled) {
		if(enabled) {
			oauthModule.setTwitterEnabled(true);
			oauthModule.setTwitterApiKey(twitterApiKeyEl.getValue());
			oauthModule.setTwitterApiSecret(twitterApiSecretEl.getValue());
		} else {
			oauthModule.setTwitterEnabled(false);
			oauthModule.setTwitterApiKey("");
			oauthModule.setTwitterApiSecret("");
		}
	}
	
	private void commitGoogle(boolean enabled) {
		if(enabled) {
			oauthModule.setGoogleEnabled(true);
			oauthModule.setGoogleApiKey(googleApiKeyEl.getValue());
			oauthModule.setGoogleApiSecret(googleApiSecretEl.getValue());
		} else {
			oauthModule.setGoogleEnabled(false);
			oauthModule.setGoogleApiKey("");
			oauthModule.setGoogleApiSecret("");
		}
	}
	
	private void commitFacebook(boolean enabled) {
		if(enabled) {
			oauthModule.setFacebookEnabled(true);
			oauthModule.setFacebookApiKey(facebookApiKeyEl.getValue());
			oauthModule.setFacebookApiSecret(facebookApiSecretEl.getValue());
		} else {
			oauthModule.setFacebookEnabled(false);
			oauthModule.setFacebookApiKey("");
			oauthModule.setFacebookApiSecret("");
		}
	}
	
	private void commitAdfs(boolean enabled) {
		if(enabled) {
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
	}
	
	private void commitAzureAdfs(boolean enabled) {
		if(enabled) {
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
	}
	
	private void commitKeycloak(boolean enabled) {
		if(enabled) {
			oauthModule.setKeycloakEnabled(true);
			oauthModule.setKeycloakClientId(keycloakClientIdEl.getValue());
			oauthModule.setKeycloakClientSecret(keycloakClientSecretEl.getValue());
			oauthModule.setKeycloakEndpoint(keycloakEndpointEl.getValue());
			oauthModule.setKeycloakRootEnabled(keycloakDefaultEl.isAtLeastSelected(1));
			oauthModule.setKeycloakRealm(keycloakRealmEl.getValue());
		} else {
			oauthModule.setKeycloakEnabled(false);
			oauthModule.setKeycloakClientId("");
			oauthModule.setKeycloakClientSecret("");
			oauthModule.setKeycloakEndpoint("");
			oauthModule.setKeycloakRootEnabled(false);
			oauthModule.setKeycloakRealm("");
		}
	}
	
	private void commitSwitchEduID(boolean enabled) {
		if(enabled) {
			oauthModule.setSwitchEduIDEnabled(true);
			oauthModule.setSwitchEduIDRootEnabled(switchEduIDDefaultEl.isAtLeastSelected(1));
			oauthModule.setSwitchEduIDApiKey(switchEduIDApiKeyEl.getValue());
			oauthModule.setSwitchEduIDApiSecret(switchEduIDApiSecretEl.getValue());
		} else {
			oauthModule.setSwitchEduIDEnabled(false);
			oauthModule.setSwitchEduIDRootEnabled(false);
			oauthModule.setSwitchEduIDApiKey("");
			oauthModule.setSwitchEduIDApiSecret("");
		}
	}
	
	private void commitDatenlotsen(boolean enabled) {
		if(enabled) {
			oauthModule.setDatenlotsenEnabled(true);
			oauthModule.setDatenlotsenRootEnabled(datenlotsenDefaultEl.isAtLeastSelected(1));
			oauthModule.setDatenlotsenApiKey(datenlotsenKeyEl.getValue());
			oauthModule.setDatenlotsenApiSecret(datenlotsenSecretEl.getValue());
			oauthModule.setDatenlotsenEndpoint(datenlotsenEndpointEl.getValue());
		} else {
			oauthModule.setDatenlotsenEnabled(false);
			oauthModule.setDatenlotsenRootEnabled(false);
			oauthModule.setDatenlotsenApiKey("");
			oauthModule.setDatenlotsenApiSecret("");
			oauthModule.setDatenlotsenEndpoint("");
		}
	}
	
	private void commitTequila(boolean enabled) {
		if(enabled) {
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
	}
	
	private void commitOpenIdConnectIF(boolean enabled) {
		if(enabled) {
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
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addIfConfigCtrl == source || addConfigCtrl == source || editMappingCtrl == source) {
			if(event == Event.DONE_EVENT) {
				initCustomProviders(additionalProvidersCont);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addDiscoveryUrlCtrl == source) {
			cmc.deactivate();
			JSONObject configuration = addDiscoveryUrlCtrl.getConfiguration();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doAddConfiguration(ureq, configuration);
			}
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doDelete((OAuthSPI)confirmDeleteCtrl.getUserObject());
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addDiscoveryUrlCtrl);
		removeAsListenerAndDispose(addIfConfigCtrl);
		removeAsListenerAndDispose(editMappingCtrl);
		removeAsListenerAndDispose(addConfigCtrl);
		removeAsListenerAndDispose(cmc);
		addDiscoveryUrlCtrl = null;
		editMappingCtrl = null;
		addIfConfigCtrl = null;
		addConfigCtrl = null;
		cmc = null;
	}

	private void doAddOpenIDConnectIFCustom(UserRequest ureq) {
		addIfConfigCtrl = new AddOpenIDConnectIFFullConfigurableController(ureq, getWindowControl());
		listenTo(addIfConfigCtrl);

		String title = translate("add.openidconnect.custom");
		cmc = new CloseableModalController(getWindowControl(), null, addIfConfigCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddConfiguration(UserRequest ureq, JSONObject configuration) {
		addConfigCtrl = new AddConfigurableController(ureq, getWindowControl(), configuration);
		listenTo(addConfigCtrl);
		
		String title = translate("add.oauth.20");
		cmc = new CloseableModalController(getWindowControl(), null, addConfigCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddDiscovery(UserRequest ureq) {
		addDiscoveryUrlCtrl = new AddDiscoveryURLController(ureq, getWindowControl());
		listenTo(addDiscoveryUrlCtrl);

		String title = translate("add.openidconnect.custom");
		cmc = new CloseableModalController(getWindowControl(), null, addDiscoveryUrlCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDelete(UserRequest ureq, OAuthSPI spi) {
		String providerName;
		if(spi instanceof OAuthDisplayName) {
			providerName = ((OAuthDisplayName)spi).getDisplayName();
		} else {
			String i18nKey = providerI18nKey(spi);
			providerName = translate(i18nKey);
		}

		String title = translate("confirm.delete.provider.title", providerName);
		String text = translate("confirm.delete.provider.text", providerName);
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(spi);
	}
	
	private void doDelete(OAuthSPI spi) {
		if(spi instanceof OpenIdConnectFullConfigurableProvider) {
			oauthModule.removeAdditionalOpenIDConnectIF(spi.getProviderName());
			initCustomProviders(additionalProvidersCont);
		} else if(spi instanceof GenericOAuth2Provider) {
			oauthModule.removeGenericOAuth(spi.getProviderName());
			initCustomProviders(additionalProvidersCont);
		} else {
			ProviderWrapper providerWrapper = getProviderWrapper(spi);
			if(providerWrapper != null) {
				providerWrapper.getProviderContainer().setVisible(false);
			}
		}
	}
	
	private void doEditMapping(UserRequest ureq, OAuthSPI spi) {
		editMappingCtrl = new OAuthMappingEditController(ureq, getWindowControl(), spi, (OAuthMapping)spi);
		listenTo(editMappingCtrl);
		
		String title = translate("edit.mapping");
		cmc = new CloseableModalController(getWindowControl(), null, editMappingCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static class ProviderWrapper {

		private final OAuthSPI provider;
		private FormLayoutContainer providerContainer;
		
		public ProviderWrapper(OAuthSPI provider, FormLayoutContainer providerContainer) {
			this.provider = provider;
			this.providerContainer = providerContainer;
		}

		public OAuthSPI getProvider() {
			return provider;
		}

		public FormLayoutContainer getProviderContainer() {
			return providerContainer;
		}
		
		public void setProviderContainer(FormLayoutContainer providerContainer) {
			this.providerContainer = providerContainer;
		}
	}
	
	public class GenericOAuth2ProviderWrapper extends ProviderWrapper {
		
		private FormLayoutContainer layoutCont;
		
		private MultipleSelectionElement rootEnabledEl;
		private TextElement nameEl;
		private TextElement displayNameEl;
		private TextElement apiKeyEl;
		private TextElement apiSecretEl;
		
		private SingleSelection responseTypeEl;
		private TextElement scopesEl;
		
		private TextElement issuerEl;
		private TextElement authorizationEndPointEl;
		private TextElement tokenEndPointEl;
		private TextElement userInfoEndPointEl;
		
		private final SelectionValues responseTypes;
		
		public GenericOAuth2ProviderWrapper(GenericOAuth2Provider spi) {
			super(spi, null);
			
			responseTypes = new SelectionValues();
			responseTypes.add(SelectionValues.entry("code", "code"));
			responseTypes.add(SelectionValues.entry("id_token", "id_token"));
			responseTypes.add(SelectionValues.entry("id_token token", "id_token token"));
		}

		public void initForm(FormItemContainer container) {
			counter++;
			
			GenericOAuth2Provider spi = (GenericOAuth2Provider)getProvider();
			layoutCont = FormLayoutContainer.createDefaultFormLayout("gen.oauth." + counter, getTranslator());
			layoutCont.setFormTitle(translate("generic.admin.custom.title", spi.getProviderName()));
			layoutCont.setFormTitleIconCss("o_icon o_icon_provider_oauth");
			layoutCont.setRootForm(mainForm);
			container.add(layoutCont);
			
			rootEnabledEl = uifactory.addCheckboxesHorizontal("gen.oauth.def.enabled." + counter, "openidconnectif.default.enabled", layoutCont, keys, values);
			if(spi.isRootEnabled()) {
				rootEnabledEl.select(keys[0], true);
			}
			
			String providerName = spi.getProviderName();
			nameEl = uifactory.addTextElement("gen.oauth.name." + counter, "openidconnectif.name", 256, providerName, layoutCont);
			nameEl.setEnabled(false);
			
			String displayName = spi.getDisplayName();
			displayNameEl = uifactory.addTextElement("gen.oauth.displayname." + counter, "openidconnectif.displayname", 256, displayName, layoutCont);
			displayNameEl.setMandatory(true);
			
			String apiKey = spi.getAppKey();
			apiKeyEl = uifactory.addTextElement("gen.oauth.id." + counter, "openidconnectif.api.id", 256, apiKey, layoutCont);
			apiKeyEl.setMandatory(true);
			
			String apiSecret = spi.getAppSecret();
			apiSecretEl = uifactory.addTextElement("gen.oauth.secret." + counter, "openidconnectif.api.secret", 256, apiSecret, layoutCont);
			apiSecretEl.setMandatory(true);
			
			responseTypeEl = uifactory.addDropdownSingleselect("gen.oauth.response.type." + counter, "response.type", layoutCont,
					responseTypes.keys(), responseTypes.values());
			responseTypeEl.setMandatory(true);
			if(responseTypes.containsKey(spi.getResponseType())) {
				responseTypeEl.select(spi.getResponseType(), true);
			}
			
			String scopes = spi.getScopes();
			scopesEl = uifactory.addTextElement("gen.oauth.scopes." + counter, "scopes", 64, scopes, layoutCont);
			
			String issuer = spi.getIssuer();
			issuerEl = uifactory.addTextElement("gen.oauth.issuer." + counter, "openidconnectif.issuer", 256, issuer, layoutCont);
			issuerEl.setExampleKey("openidconnectif.issuer.example", null);
			issuerEl.setMandatory(true);
			
			String authorizationEndPoint = spi.getAuthorizationBaseUrl();
			authorizationEndPointEl = uifactory.addTextElement("gen.oauth.auth.url." + counter, "openidconnectif.authorization.endpoint",
					256, authorizationEndPoint, layoutCont);
			authorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);
			authorizationEndPointEl.setMandatory(true);

			String tokenEndPoint = spi.getTokenBaseUrl();
			tokenEndPointEl = uifactory.addTextElement("gen.oauth.token.url." + counter, "token.endpoint", 256, tokenEndPoint, layoutCont);
			tokenEndPointEl.setExampleKey("token.endpoint.example", null);
			tokenEndPointEl.setMandatory(true);
			
			String userInfosEndPoint = spi.getUserInfosUrl();
			userInfoEndPointEl = uifactory.addTextElement("gen.oauth.userinfo.url." + counter, "userinfo.endpoint", 256, userInfosEndPoint, layoutCont);
			userInfoEndPointEl.setExampleKey("userinfo.endpoint.example", null);
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons." + counter, getTranslator());
			layoutCont.add(buttonsCont);

			FormLink deleteButton  = uifactory.addFormLink("delete." + counter, "disable", "disable", null, buttonsCont, Link.BUTTON);
			deleteButton.setUserObject(getProvider());
			
			FormLink attributeButton  = uifactory.addFormLink("mapping." + counter, "mapping", "edit.mapping", null, buttonsCont, Link.BUTTON);
			attributeButton.setUserObject(getProvider());
			
			setProviderContainer(layoutCont);
		}

		protected boolean validateFormLogic() {
			boolean allOk = true;
			
			allOk &= mandatory(nameEl, displayNameEl, apiKeyEl, apiSecretEl,
					issuerEl, authorizationEndPointEl, tokenEndPointEl);
			
			responseTypeEl.clearError();
			if(!responseTypeEl.isOneSelected()) {
				responseTypeEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
			
			return allOk;
		}
		
		protected void commit() {
			boolean rootEnabled = rootEnabledEl.isAtLeastSelected(1);

			String displayName = displayNameEl.getValue();
			String apiKey = apiKeyEl.getValue();
			String apiSecret = apiSecretEl.getValue();
			String issuer = issuerEl.getValue();
			String responseType = responseTypeEl.getSelectedKey();
			String scopes = scopesEl.getValue();
			String authorizationEndPoint = authorizationEndPointEl.getValue();
			String tokenEndPoint = tokenEndPointEl.getValue();
			String userInfoEndPoint = userInfoEndPointEl.getValue();
			
			String name = getProvider().getName();
			oauthModule.setGenericOAuth(name, displayName, rootEnabled, issuer, authorizationEndPoint, tokenEndPoint, userInfoEndPoint,
					responseType, scopes, apiKey, apiSecret);
		}
	}
	
	public class ConfigurableProviderWrapper extends ProviderWrapper {
		
		private FormLayoutContainer openIdConnectIFCont;
		
		private MultipleSelectionElement openIdConnectIFConfEl;
		private TextElement openIdConnectIFConfName;
		private TextElement openIdConnectIFConfDisplayName;
		private TextElement openIdConnectIFConfApiKeyEl;
		private TextElement openIdConnectIFConfApiSecretEl;
		private TextElement openIdConnectIFConfIssuerEl;
		private TextElement openIdConnectIFConfAuthorizationEndPointEl;
		
		public ConfigurableProviderWrapper(OpenIdConnectFullConfigurableProvider spi) {
			super(spi, null);
		}

		public void initForm(FormItemContainer container) {
			counter++;
			
			OpenIdConnectFullConfigurableProvider spi = (OpenIdConnectFullConfigurableProvider)getProvider();
			openIdConnectIFCont = FormLayoutContainer.createDefaultFormLayout("openidconnectif." + counter, getTranslator());
			openIdConnectIFCont.setFormTitle(translate("openidconnectif.admin.custom.title", spi.getProviderName()));
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
			openIdConnectIFConfDisplayName.setMandatory(true);
			
			String apiKey = spi.getAppKey();
			openIdConnectIFConfApiKeyEl = uifactory.addTextElement("openidconnectif." + counter + ".id", "openidconnectif.api.id", 256, apiKey, openIdConnectIFCont);
			openIdConnectIFConfApiKeyEl.setMandatory(true);
			String apiSecret = spi.getAppSecret();
			openIdConnectIFConfApiSecretEl = uifactory.addTextElement("openidconnectif." + counter + ".secret", "openidconnectif.api.secret", 256, apiSecret, openIdConnectIFCont);
			openIdConnectIFConfApiSecretEl.setMandatory(true);
			String issuer = spi.getIssuer();
			openIdConnectIFConfIssuerEl = uifactory.addTextElement("openidconnectif." + counter + ".issuer", "openidconnectif.issuer", 256, issuer, openIdConnectIFCont);
			openIdConnectIFConfIssuerEl.setExampleKey("openidconnectif.issuer.example", null);
			openIdConnectIFConfIssuerEl.setMandatory(true);
			String endPoint = spi.getEndPoint();
			openIdConnectIFConfAuthorizationEndPointEl = uifactory.addTextElement("openidconnectif." + counter + ".authorization.endpoint", "openidconnectif.authorization.endpoint", 256, endPoint, openIdConnectIFCont);
			openIdConnectIFConfAuthorizationEndPointEl.setExampleKey("openidconnectif.authorization.endpoint.example", null);
			openIdConnectIFConfAuthorizationEndPointEl.setMandatory(true);

			FormLink deleteButton  = uifactory.addFormLink("delete." + counter, "disable", "disable", null, openIdConnectIFCont, Link.BUTTON);
			deleteButton.setUserObject(getProvider());
			setProviderContainer(openIdConnectIFCont);
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
			oauthModule.setAdditionalOpenIDConnectIF(getProvider().getProviderName(), displayName, rootEnabled, issuer, endPoint, apiKey, apiSecret);
		}
	}
}
