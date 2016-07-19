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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.login.oauth.OAuthLoginModule;
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
	private TextElement adfsOAuth2EndpointEl;
	
	private MultipleSelectionElement openIdConnectIFEl;
	private MultipleSelectionElement openIdConnectIFDefaultEl;
	private TextElement openIdConnectIFApiKeyEl;
	private TextElement openIdConnectIFApiSecretEl;
	private TextElement openIdConnectIFIssuerEl;
	private TextElement openIdConnectIFAuthorizationEndPointEl;
	
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
		
		//linkedin
		FormLayoutContainer linkedinCont = FormLayoutContainer.createDefaultFormLayout("linkedin", getTranslator());
		linkedinCont.setFormTitle(translate("linkedin.admin.title"));
		linkedinCont.setFormTitleIconCss("o_icon o_icon_provider_linkedin");
		linkedinCont.setRootForm(mainForm);
		formLayout.add(linkedinCont);
		
		linkedInEl = uifactory.addCheckboxesHorizontal("linkedin.enabled", linkedinCont, keys, values);
		linkedInEl.addActionListener(FormEvent.ONCHANGE);
		String ApiKey = oauthModule.getLinkedInApiKey();
		linkedInApiKeyEl = uifactory.addTextElement("linkedin.id", "linkedin.api.id", 256, ApiKey, linkedinCont);
		String apiSecret = oauthModule.getLinkedInApiSecret();
		linkedInApiSecretEl = uifactory.addTextElement("linkedin.secret", "linkedin.api.secret", 256, apiSecret, linkedinCont);
		if(oauthModule.isLinkedInEnabled()) {
			linkedInEl.select(keys[0], true);
		} else {
			linkedInApiKeyEl.setVisible(false);
			linkedInApiSecretEl.setVisible(false);
		}
		
		//twitter
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
		
		//google
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
		
		//facebook
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
		
		//adfs
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
		if(oauthModule.isAdfsEnabled()) {
			adfsEl.select(keys[0], true);
		} else {
			adfsApiKeyEl.setVisible(false);
			adfsDefaultEl.setVisible(false);
			adfsOAuth2EndpointEl.setVisible(false);
		}
		
		if(oauthModule.isAdfsRootEnabled()) {
			adfsDefaultEl.select(keys[0], true);
		}
		
		//openIdConnectIF
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
		
		//buttons
		FormLayoutContainer buttonBonesCont = FormLayoutContainer.createDefaultFormLayout("button_bones", getTranslator());
		buttonBonesCont.setRootForm(mainForm);
		formLayout.add(buttonBonesCont);
		uifactory.addSpacerElement("buttons-spacer", buttonBonesCont, true);
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		buttonBonesCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
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
		//open id connect
		allOk &= mandatory(openIdConnectIFEl, openIdConnectIFAuthorizationEndPointEl, openIdConnectIFApiKeyEl, openIdConnectIFApiSecretEl);

		return allOk & super.validateFormLogic(ureq);
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
			adfsDefaultEl.setVisible(adfsEl.isAtLeastSelected(1));
			adfsOAuth2EndpointEl.setVisible(adfsEl.isAtLeastSelected(1));
		} else if(source == openIdConnectIFEl) {
			openIdConnectIFIssuerEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFApiKeyEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFDefaultEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFApiSecretEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
			openIdConnectIFAuthorizationEndPointEl.setVisible(openIdConnectIFEl.isAtLeastSelected(1));
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
			oauthModule.setAdfsRootEnabled(adfsDefaultEl.isAtLeastSelected(1));
			oauthModule.setAdfsOAuth2Endpoint(adfsOAuth2EndpointEl.getValue());
		} else {
			oauthModule.setAdfsEnabled(false);
			oauthModule.setAdfsApiKey("");
			oauthModule.setAdfsRootEnabled(false);
			oauthModule.setAdfsOAuth2Endpoint("");
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
	}
}
