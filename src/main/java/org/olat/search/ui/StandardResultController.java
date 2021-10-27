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

package org.olat.search.ui;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.search.model.ResultDocument;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * The standard output for a search result.
 * <P>
 * Initial Date:  3 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class StandardResultController extends FormBasicController implements ResultController {
	
	protected final ResultDocument document;
	protected FormLink docLink, docHighlightLink;
	private boolean highlight;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private LicenseService licenseService;
	
	public StandardResultController(UserRequest ureq, WindowControl wControl, Form mainForm, ResultDocument document) {
		super(ureq, wControl, LAYOUT_CUSTOM, "standardResult", mainForm);
		this.document = document;
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer formLayoutCont = (FormLayoutContainer)formLayout;
			formLayoutCont.contextPut("result", document);
			formLayoutCont.contextPut("id", hashCode());
			formLayoutCont.contextPut("formatter", Formatter.getInstance(getLocale()));
			
			String author = document.getAuthor();
			if(StringHelper.containsNonWhitespace(author)) {
				List<IdentityShort> identities = securityManager.findShortIdentitiesByName(Collections.singleton(author));
				if(!identities.isEmpty()) {
					author = userManager.getUserDisplayName(identities.get(0));
				}
			}
			formLayoutCont.contextPut("author", author);
			
			if (StringHelper.containsNonWhitespace(document.getLicenseTypeKey())) {
				LicenseType licenseType = licenseService.loadLicenseTypeByKey(document.getLicenseTypeKey());
				if (!licenseService.isNoLicense(licenseType)) {
					formLayoutCont.contextPut("licenseIcon", LicenseUIFactory.getCssOrDefault(licenseType));
					formLayoutCont.contextPut("license", LicenseUIFactory.translate(licenseType, getLocale()));
				}
			}
		}
		
		String icon = document.getCssIcon();
		if(!StringHelper.containsNonWhitespace(icon)) {
			icon = "o_sp_icon";
		}
		
		String label = document.getTitle();
		if(label != null) {
			label = label.trim();
			if(label.length() > 128) {
				label = FilterFactory.getHtmlTagsFilter().filter(label);
				label = Formatter.truncate(label, 128);
			}
		}
		
		label = StringHelper.escapeHtml(label);
		docLink = uifactory.addFormLink("open_doc", label, label, formLayout, Link.NONTRANSLATED);
		docLink.setIconLeftCSS("o_icon o_icon-fw " + icon);
		
		String highlightLabel = document.getHighlightTitle();
		if(!StringHelper.containsNonWhitespace(highlightLabel)) {
			highlightLabel = label;
		}
		docHighlightLink = uifactory.addFormLink("open_doc_highlight", highlightLabel, highlightLabel, formLayout, Link.NONTRANSLATED);
		docHighlightLink.setIconLeftCSS("o_icon o_icon-fw ".concat(icon));
		
		if(StringHelper.containsNonWhitespace(document.getResourceUrl())) {
			String url = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(document.getResourceUrl());
			docLink.setUrl(url);
			docHighlightLink.setUrl(url);
		}
	}

	@Override
	public boolean isHighlight() {
		return highlight;
	}

	@Override
	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
		flc.contextPut("highlight", highlight);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == docLink || source == docHighlightLink) {
			if (event != null) {
				fireEvent(ureq, new SearchEvent(document));
			}
		}
	}
}
