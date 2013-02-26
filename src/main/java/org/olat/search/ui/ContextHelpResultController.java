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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.search.model.ResultDocument;

/**
 * Description:<br>
 * Show context help documents. Choose if the link to open the document
 * go to a new window or stay in the same.
 * 
 * <P>
 * Initial Date:  11 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class ContextHelpResultController extends FormBasicController implements ResultController {

	private final ResultDocument document;
	private boolean highlight;
	
	public ContextHelpResultController(UserRequest ureq, WindowControl wControl, Form mainForm, ResultDocument document) {
		super(ureq, wControl, LAYOUT_CUSTOM, "contextHelpResult", mainForm);
		this.document = document;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer formLayoutCont = (FormLayoutContainer)formLayout;
			formLayoutCont.contextPut("result", document);
			formLayoutCont.contextPut("id", this.hashCode());
			formLayoutCont.contextPut("formatter", Formatter.getInstance(getLocale()));
		}
		String target = openLinkInNewWindow(ureq) ? "_blank" : "_self";
		flc.contextPut("target", target);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private boolean openLinkInNewWindow(UserRequest ureq) {
		String context = ureq.getHttpReq().getContextPath();
		String request = ureq.getHttpReq().getRequestURI();
		if(StringHelper.containsNonWhitespace(context) && StringHelper.containsNonWhitespace(request)) {
			boolean helpDispatcher = request.startsWith(context + "/help/");
			if(helpDispatcher) {
				return false;
			}
		}
		return true;
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
	
	public FormItem getInitialFormItem() {
		return flc;
	}
}
