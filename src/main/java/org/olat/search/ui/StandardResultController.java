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

import org.olat.core.commons.services.search.ResultDocument;
import org.olat.core.commons.services.search.ui.ResultController;
import org.olat.core.commons.services.search.ui.SearchEvent;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;

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
			formLayoutCont.contextPut("id", this.hashCode());
			formLayoutCont.contextPut("formatter", Formatter.getInstance(getLocale()));
		}
		
		String highlightLabel = document.getHighlightTitle();
		docHighlightLink = uifactory.addFormLink("open_doc_highlight", highlightLabel, highlightLabel, formLayout, Link.NONTRANSLATED);
		String icon = document.getCssIcon();
		if(!StringHelper.containsNonWhitespace(icon)) {
			icon = "o_sp_icon";
		}
		String cssClass = "b_with_small_icon_left " + icon;
		((Link)docHighlightLink.getComponent()).setCustomEnabledLinkCSS(cssClass);
		((Link)docHighlightLink.getComponent()).setCustomDisabledLinkCSS(cssClass);
		
		String label = document.getTitle();
		docLink = uifactory.addFormLink("open_doc", label, label, formLayout, Link.NONTRANSLATED);
		((Link)docLink.getComponent()).setCustomEnabledLinkCSS(cssClass);
		((Link)docLink.getComponent()).setCustomDisabledLinkCSS(cssClass);
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
	
	public FormItem getInitialFormItem() {
		return flc;
	}
}
