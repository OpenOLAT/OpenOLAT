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
package org.olat.admin.security;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.analytics.AnalyticsModule;
import org.olat.core.commons.services.analytics.spi.GoogleAnalyticsSPI;
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
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminController extends FormBasicController {

	private static final String[] keys = new String[]{ "on" };
	private static final String[] values = new String[]{ "" };
	
	private MultipleSelectionElement wikiEl;
	private MultipleSelectionElement topFrameEl;
	private MultipleSelectionElement forceDownloadEl;
	
	private MultipleSelectionElement strictTransportSecurityEl;
	private MultipleSelectionElement xContentTypeOptionsEl;
	private MultipleSelectionElement xFrameOptionsSameoriginEl;
	private MultipleSelectionElement contentSecurityPolicyEl;
	
	private TextElement defaultSrcEl;
	private TextElement scriptSrcEl;
	private TextElement styleSrcEl;
	private TextElement imgSrcEl;
	private TextElement fontSrcEl;
	private TextElement connectSrcEl;
	private TextElement frameSrcEl;
	private TextElement mediaSrcEl;
	private TextElement objectSrcEl;
	private TextElement pluginTypeEl;
	
	private FormLayoutContainer cspOptionsCont;
	
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private AnalyticsModule analyticsModule;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public SecurityAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer resourcesCont = FormLayoutContainer.createDefaultFormLayout("resources", getTranslator());
		formLayout.add(resourcesCont);
		resourcesCont.setFormTitle(translate("sec.title"));
		resourcesCont.setFormDescription(translate("sec.description"));
		resourcesCont.setFormContextHelp("Security");
		
		// on: block wiki (more security); off: do not block wiki (less security)
		wikiEl = uifactory.addCheckboxesHorizontal("sec.wiki", "sec.wiki", resourcesCont, keys, values);
		wikiEl.addActionListener(FormEvent.ONCHANGE);
		if(!securityModule.isWikiEnabled()) {
			wikiEl.select("on", true);
		}

		// on: force file download in folder component (more security); off: allow execution of content (less security)
		forceDownloadEl = uifactory.addCheckboxesHorizontal("sec.download", "sec.force.download", resourcesCont, keys, values);
		forceDownloadEl.addActionListener(FormEvent.ONCHANGE);
		if(folderModule.isForceDownload()) {
			forceDownloadEl.select("on", true);
		}
		
		
		FormLayoutContainer headersCont = FormLayoutContainer.createDefaultFormLayout("headers", getTranslator());
		formLayout.add(headersCont);
		headersCont.setFormDescription(translate("sec.description.headers"));
		
		// on: force top top frame (more security); off: allow in frame (less security)
		topFrameEl = uifactory.addCheckboxesHorizontal("sec.topframe", "sec.topframe", headersCont, keys, values);
		topFrameEl.select("on", securityModule.isForceTopFrame());
		topFrameEl.addActionListener(FormEvent.ONCHANGE);
		topFrameEl.setEnabled(false);
		topFrameEl.setExampleKey("sec.top.frame.explanation", null);

		// on: send HTTP header X-FRAME-OPTIONS -> SAMEDOMAIN to prevent click-jack attacks. JS-top frame hack not save enough
		xFrameOptionsSameoriginEl = uifactory.addCheckboxesHorizontal("sec.xframe.sameorigin", "sec.xframe.sameorigin", headersCont, keys, values);
		xFrameOptionsSameoriginEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isXFrameOptionsSameoriginEnabled()) {
			xFrameOptionsSameoriginEl.select("on", true);
		}
		
		strictTransportSecurityEl = uifactory.addCheckboxesHorizontal("sec.strict.transport.sec", "sec.strict.transport.sec", headersCont, keys, values);
		strictTransportSecurityEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isStrictTransportSecurityEnabled()) {
			strictTransportSecurityEl.select("on", true);
		}
		
		xContentTypeOptionsEl = uifactory.addCheckboxesHorizontal("sec.content.type.options", "sec.content.type.options", headersCont, keys, values);
		xContentTypeOptionsEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isXContentTypeOptionsEnabled()) {
			xContentTypeOptionsEl.select("on", true);
		}
		
		FormLayoutContainer cspCont = FormLayoutContainer.createDefaultFormLayout("csp", getTranslator());
		formLayout.add(cspCont);
		cspCont.contextPut("off_warn", translate("sec.description.csp"));
		
		contentSecurityPolicyEl = uifactory.addCheckboxesHorizontal("sec.content.security.policy", "sec.content.security.policy", cspCont, keys, values);
		contentSecurityPolicyEl.addActionListener(FormEvent.ONCHANGE);
		if(securityModule.isContentSecurityPolicyEnabled()) {
			contentSecurityPolicyEl.select("on", true);
		}
		
		cspOptionsCont = FormLayoutContainer.createDefaultFormLayout("cspOptions", getTranslator());
		formLayout.add(cspOptionsCont);
		cspOptionsCont.setVisible(contentSecurityPolicyEl.isAtLeastSelected(1));
		
		String defaultSrcPolicy = securityModule.getContentSecurityPolicyDefaultSrc();
		defaultSrcEl = uifactory.addTextElement("sec.csp.default.src", 512, defaultSrcPolicy, cspOptionsCont);
		defaultSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_DEFAULT_SRC, "https://example.com" });
		
		String scriptSrcPolicy = securityModule.getContentSecurityPolicyScriptSrc();
		scriptSrcEl = uifactory.addTextElement("sec.csp.script.src", 512, scriptSrcPolicy, cspOptionsCont);
		scriptSrcEl.setExampleKey("sec.csp.default.value", new String[] { getMandatoryScriptDirective(), "https://example.com" });
		
		String styleSrcPolicy = securityModule.getContentSecurityPolicyStyleSrc();
		styleSrcEl = uifactory.addTextElement("sec.csp.style.src", 512, styleSrcPolicy, cspOptionsCont);
		styleSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_STYLE_SRC, "https://example.com" });
		
		String imgSrcPolicy = securityModule.getContentSecurityPolicyImgSrc();
		imgSrcEl = uifactory.addTextElement("sec.csp.img.src", 512, imgSrcPolicy, cspOptionsCont);
		imgSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_IMG_SRC, "https://example.com" });
		
		String fontSrcPolicy = securityModule.getContentSecurityPolicyFontSrc();
		fontSrcEl = uifactory.addTextElement("sec.csp.font.src", 512, fontSrcPolicy, cspOptionsCont);
		fontSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_FONT_SRC, "https://example.com" });
		
		String connectSrcPolicy = securityModule.getContentSecurityPolicyConnectSrc();
		connectSrcEl = uifactory.addTextElement("sec.csp.connect.src", 512, connectSrcPolicy, cspOptionsCont);
		connectSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_CONNECT_SRC, "https://example.com" });
		
		String frameSrcPolicy = securityModule.getContentSecurityPolicyFrameSrc();
		frameSrcEl = uifactory.addTextElement("sec.csp.frame.src", 512, frameSrcPolicy, cspOptionsCont);
		frameSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_FRAME_SRC, "https://example.com" });
		
		String mediaSrcPolicy = securityModule.getContentSecurityPolicyMediaSrc();
		mediaSrcEl = uifactory.addTextElement("sec.csp.media.src", 512, mediaSrcPolicy, cspOptionsCont);
		mediaSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_MEDIA_SRC, "https://example.com" });
		
		String objectSrcPolicy = securityModule.getContentSecurityPolicyObjectSrc();
		objectSrcEl = uifactory.addTextElement("sec.csp.object.src", 512, objectSrcPolicy, cspOptionsCont);
		objectSrcEl.setExampleKey("sec.csp.default.value", new String[] { BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_OBJECT_SRC, "https://example.com" });
		
		String pluginTypePolicy = securityModule.getContentSecurityPolicyPluginType();
		pluginTypeEl = uifactory.addTextElement("sec.csp.plugin.type", 512, pluginTypePolicy, cspOptionsCont);
		pluginTypeEl.setExampleKey("sec.csp.default.value", new String[] { "", "application/x-shockwave-flash" });

		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String getMandatoryScriptDirective() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(BaseSecurityModule.DEFAULT_CONTENT_SECURITY_POLICY_SCRIPT_SRC);
		if(StringHelper.containsNonWhitespace(WebappHelper.getMathJaxCdn())) {
			try {
				String mathJaxCdn = WebappHelper.getMathJaxCdn();
				if(mathJaxCdn.startsWith("//")) {
					mathJaxCdn = "https:" + mathJaxCdn;
				}
				URL url = URI.create(mathJaxCdn).toURL();
				sb.append(" ").append(url.getProtocol()).append("://").append(url.getHost());
			} catch (MalformedURLException e) {
				logError("", e);
			}
		}
		
		if(analyticsModule != null && analyticsModule.getAnalyticsProvider() instanceof GoogleAnalyticsSPI) {
			sb.append(" ").append("https://www.google-analytics.com");
		}
		return sb.toString();
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(contentSecurityPolicyEl == source) {
			cspOptionsCont.setVisible(contentSecurityPolicyEl.isAtLeastSelected(1));
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		securityModule.setForceTopFrame(topFrameEl.isAtLeastSelected(1));
		securityModule.setWikiEnabled(!wikiEl.isAtLeastSelected(1));
		securityModule.setXFrameOptionsSameoriginEnabled(xFrameOptionsSameoriginEl.isAtLeastSelected(1));
		securityModule.setStrictTransportSecurity(strictTransportSecurityEl.isAtLeastSelected(1));
		securityModule.setxContentTypeOptions(xContentTypeOptionsEl.isAtLeastSelected(1));
		folderModule.setForceDownload(forceDownloadEl.isAtLeastSelected(1));
		securityModule.setContentSecurityPolicy(contentSecurityPolicyEl.isAtLeastSelected(1));
		
		boolean cspEnabled = contentSecurityPolicyEl.isAtLeastSelected(1);
		securityModule.setContentSecurityPolicy(cspEnabled);
		if(cspEnabled) {
			securityModule.setContentSecurityPolicyDefaultSrc(defaultSrcEl.getValue());
			securityModule.setContentSecurityPolicyScriptSrc(scriptSrcEl.getValue());
			securityModule.setContentSecurityPolicyStyleSrc(styleSrcEl.getValue());
			securityModule.setContentSecurityPolicyImgSrc(imgSrcEl.getValue());
			securityModule.setContentSecurityPolicyFontSrc(fontSrcEl.getValue());
			securityModule.setContentSecurityPolicyConnectSrc(connectSrcEl.getValue());
			securityModule.setContentSecurityPolicyFrameSrc(frameSrcEl.getValue());
			securityModule.setContentSecurityPolicyMediaSrc(mediaSrcEl.getValue());
			securityModule.setContentSecurityPolicyObjectSrc(objectSrcEl.getValue());
			securityModule.setContentSecurityPolicyPluginType(pluginTypeEl.getValue());
		} else {
			securityModule.setContentSecurityPolicyDefaultSrc(null);
			securityModule.setContentSecurityPolicyScriptSrc(null);
			securityModule.setContentSecurityPolicyStyleSrc(null);
			securityModule.setContentSecurityPolicyImgSrc(null);
			securityModule.setContentSecurityPolicyFontSrc(null);
			securityModule.setContentSecurityPolicyConnectSrc(null);
			securityModule.setContentSecurityPolicyFrameSrc(null);
			securityModule.setContentSecurityPolicyMediaSrc(null);
			securityModule.setContentSecurityPolicyObjectSrc(null);
			securityModule.setContentSecurityPolicyPluginType(null);
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
	}
}