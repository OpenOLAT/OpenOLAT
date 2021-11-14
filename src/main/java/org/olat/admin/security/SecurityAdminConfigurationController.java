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

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.services.csp.CSPModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.servlets.HeadersFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.12.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SecurityAdminConfigurationController extends FormBasicController {

	private static final String[] keys = new String[]{ "on" };
	private static final String[] values = new String[]{ "" };
	
	private static final String EXAMPLE = "https://example.com";
	
	private MultipleSelectionElement topFrameEl;
	private MultipleSelectionElement forceDownloadEl;

	private MultipleSelectionElement csrfEl;
	private MultipleSelectionElement strictTransportSecurityEl;
	private MultipleSelectionElement xContentTypeOptionsEl;
	private MultipleSelectionElement xFrameOptionsSameoriginEl;
	private MultipleSelectionElement contentSecurityPolicyEl;
	private MultipleSelectionElement contentSecurityPolicyReportOnlyEl;
	
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
	private CSPModule cspModule;
	@Autowired
	private FolderModule folderModule;
	
	private final HeadersFilter headersProvider;
	
	public SecurityAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		
		headersProvider = new HeadersFilter();
		CoreSpringFactory.autowireObject(headersProvider);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer resourcesCont = FormLayoutContainer.createDefaultFormLayout("resources", getTranslator());
		formLayout.add(resourcesCont);
		resourcesCont.setFormTitle(translate("sec.title"));
		resourcesCont.setFormDescription(translate("sec.description"));
		resourcesCont.setFormContextHelp("Security");

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
		topFrameEl.select("on", cspModule.isForceTopFrame());
		topFrameEl.addActionListener(FormEvent.ONCHANGE);
		topFrameEl.setEnabled(false);
		topFrameEl.setExampleKey("sec.top.frame.explanation", null);

		// on: send HTTP header X-FRAME-OPTIONS -> SAMEDOMAIN to prevent click-jack attacks. JS-top frame hack not save enough
		xFrameOptionsSameoriginEl = uifactory.addCheckboxesHorizontal("sec.xframe.sameorigin", "sec.xframe.sameorigin", headersCont, keys, values);
		xFrameOptionsSameoriginEl.addActionListener(FormEvent.ONCHANGE);
		if(cspModule.isXFrameOptionsSameoriginEnabled()) {
			xFrameOptionsSameoriginEl.select("on", true);
		}
		
		strictTransportSecurityEl = uifactory.addCheckboxesHorizontal("sec.strict.transport.sec", "sec.strict.transport.sec", headersCont, keys, values);
		strictTransportSecurityEl.addActionListener(FormEvent.ONCHANGE);
		if(cspModule.isStrictTransportSecurityEnabled()) {
			strictTransportSecurityEl.select("on", true);
		}
		
		xContentTypeOptionsEl = uifactory.addCheckboxesHorizontal("sec.content.type.options", "sec.content.type.options", headersCont, keys, values);
		xContentTypeOptionsEl.addActionListener(FormEvent.ONCHANGE);
		if(cspModule.isXContentTypeOptionsEnabled()) {
			xContentTypeOptionsEl.select("on", true);
		}
		
		FormLayoutContainer csrfCont = FormLayoutContainer.createDefaultFormLayout("csrf", getTranslator());
		formLayout.add(csrfCont);
		csrfCont.setFormDescription(translate("sec.description.csrf"));
		
		csrfEl = uifactory.addCheckboxesHorizontal("sec.csrf", "sec.csrf", csrfCont, keys, values);
		if(cspModule.isCsrfEnabled()) {
			csrfEl.select("on", true);
		}
		
		FormLayoutContainer cspCont = FormLayoutContainer.createDefaultFormLayout("csp", getTranslator());
		formLayout.add(cspCont);
		cspCont.contextPut("off_warn", translate("sec.description.csp"));
		
		contentSecurityPolicyEl = uifactory.addCheckboxesHorizontal("sec.content.security.policy", "sec.content.security.policy", cspCont, keys, values);
		contentSecurityPolicyEl.addActionListener(FormEvent.ONCHANGE);
		if(cspModule.isContentSecurityPolicyEnabled()) {
			contentSecurityPolicyEl.select("on", true);
		}
		
		cspOptionsCont = FormLayoutContainer.createDefaultFormLayout("cspOptions", getTranslator());
		formLayout.add(cspOptionsCont);
		cspOptionsCont.setVisible(contentSecurityPolicyEl.isAtLeastSelected(1));
		
		contentSecurityPolicyReportOnlyEl = uifactory.addCheckboxesHorizontal("sec.content.security.policy.report.only", "sec.content.security.policy.report.only", cspOptionsCont, keys, values);
		if(cspModule.isContentSecurityPolicyReportOnlyEnabled()) {
			contentSecurityPolicyReportOnlyEl.select("on", true);
		}
		
		String defaultSrcPolicy = cspModule.getContentSecurityPolicyDefaultSrc();
		defaultSrcEl = uifactory.addTextElement("sec.csp.default.src", 512, defaultSrcPolicy, cspOptionsCont);
		defaultSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("default-src"), EXAMPLE });
		
		String scriptSrcPolicy = cspModule.getContentSecurityPolicyScriptSrc();
		scriptSrcEl = uifactory.addTextElement("sec.csp.script.src", 512, scriptSrcPolicy, cspOptionsCont);
		scriptSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("script-src"), EXAMPLE });
		
		String styleSrcPolicy = cspModule.getContentSecurityPolicyStyleSrc();
		styleSrcEl = uifactory.addTextElement("sec.csp.style.src", 512, styleSrcPolicy, cspOptionsCont);
		styleSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("style-src"), EXAMPLE });
		
		String imgSrcPolicy = cspModule.getContentSecurityPolicyImgSrc();
		imgSrcEl = uifactory.addTextElement("sec.csp.img.src", 512, imgSrcPolicy, cspOptionsCont);
		imgSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("img-src"), EXAMPLE });
		
		String fontSrcPolicy = cspModule.getContentSecurityPolicyFontSrc();
		fontSrcEl = uifactory.addTextElement("sec.csp.font.src", 512, fontSrcPolicy, cspOptionsCont);
		fontSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("font-src"), EXAMPLE });
		
		String connectSrcPolicy = cspModule.getContentSecurityPolicyConnectSrc();
		connectSrcEl = uifactory.addTextElement("sec.csp.connect.src", 512, connectSrcPolicy, cspOptionsCont);
		connectSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("connect-src"), EXAMPLE });
		
		String frameSrcPolicy = cspModule.getContentSecurityPolicyFrameSrc();
		frameSrcEl = uifactory.addTextElement("sec.csp.frame.src", 512, frameSrcPolicy, cspOptionsCont);
		frameSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("frame-src"), EXAMPLE });
		
		String mediaSrcPolicy = cspModule.getContentSecurityPolicyMediaSrc();
		mediaSrcEl = uifactory.addTextElement("sec.csp.media.src", 512, mediaSrcPolicy, cspOptionsCont);
		mediaSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("nedia-src"), EXAMPLE });
		
		String objectSrcPolicy = cspModule.getContentSecurityPolicyObjectSrc();
		objectSrcEl = uifactory.addTextElement("sec.csp.object.src", 512, objectSrcPolicy, cspOptionsCont);
		objectSrcEl.setExampleKey("sec.csp.default.value", new String[] { headersProvider.getDefaultDirective("object-src"), EXAMPLE });
		
		String pluginTypePolicy = cspModule.getContentSecurityPolicyPluginType();
		pluginTypeEl = uifactory.addTextElement("sec.csp.plugin.type", 512, pluginTypePolicy, cspOptionsCont);
		pluginTypeEl.setExampleKey("sec.csp.default.value", new String[] { "", "application/x-shockwave-flash" });

		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createDefaultFormLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
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
		cspModule.setForceTopFrame(topFrameEl.isAtLeastSelected(1));
		cspModule.setXFrameOptionsSameoriginEnabled(xFrameOptionsSameoriginEl.isAtLeastSelected(1));
		cspModule.setStrictTransportSecurity(strictTransportSecurityEl.isAtLeastSelected(1));
		cspModule.setxContentTypeOptions(xContentTypeOptionsEl.isAtLeastSelected(1));
		folderModule.setForceDownload(forceDownloadEl.isAtLeastSelected(1));
		cspModule.setContentSecurityPolicy(contentSecurityPolicyEl.isAtLeastSelected(1));
		cspModule.setCsrfEnabled(csrfEl.isAtLeastSelected(1));
		
		boolean cspEnabled = contentSecurityPolicyEl.isAtLeastSelected(1);
		cspModule.setContentSecurityPolicy(cspEnabled);
		if(cspEnabled) {
			cspModule.setContentSecurityPolicyReportOnly(contentSecurityPolicyReportOnlyEl.isAtLeastSelected(1));
			cspModule.setContentSecurityPolicyDefaultSrc(defaultSrcEl.getValue());
			cspModule.setContentSecurityPolicyScriptSrc(scriptSrcEl.getValue());
			cspModule.setContentSecurityPolicyStyleSrc(styleSrcEl.getValue());
			cspModule.setContentSecurityPolicyImgSrc(imgSrcEl.getValue());
			cspModule.setContentSecurityPolicyFontSrc(fontSrcEl.getValue());
			cspModule.setContentSecurityPolicyConnectSrc(connectSrcEl.getValue());
			cspModule.setContentSecurityPolicyFrameSrc(frameSrcEl.getValue());
			cspModule.setContentSecurityPolicyMediaSrc(mediaSrcEl.getValue());
			cspModule.setContentSecurityPolicyObjectSrc(objectSrcEl.getValue());
			cspModule.setContentSecurityPolicyPluginType(pluginTypeEl.getValue());
		} else {
			cspModule.setContentSecurityPolicyReportOnly(false);
			cspModule.setContentSecurityPolicyDefaultSrc(null);
			cspModule.setContentSecurityPolicyScriptSrc(null);
			cspModule.setContentSecurityPolicyStyleSrc(null);
			cspModule.setContentSecurityPolicyImgSrc(null);
			cspModule.setContentSecurityPolicyFontSrc(null);
			cspModule.setContentSecurityPolicyConnectSrc(null);
			cspModule.setContentSecurityPolicyFrameSrc(null);
			cspModule.setContentSecurityPolicyMediaSrc(null);
			cspModule.setContentSecurityPolicyObjectSrc(null);
			cspModule.setContentSecurityPolicyPluginType(null);
		}
		CollaborationToolsFactory.getInstance().initAvailableTools();
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}