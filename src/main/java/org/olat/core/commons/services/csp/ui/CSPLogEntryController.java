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
package org.olat.core.commons.services.csp.ui;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.csp.CSPLog;
import org.olat.core.commons.services.csp.ui.event.NextEntryEvent;
import org.olat.core.commons.services.csp.ui.event.PreviousEntryEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSPLogEntryController extends FormBasicController implements TooledController {
	
	private final CSPLog logEntry;
	private String userFullname;
	
	private Link nextEntry;
	private Link previousEntry;
	private final TooledStackedPanel toolbarPanel;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;

	public CSPLogEntryController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CSPLog logEntry) {
		super(ureq, wControl);
		this.logEntry = logEntry;
		this.toolbarPanel = stackPanel;
		if(logEntry.getIdentityKey() != null) {
			Identity logIdentity = securityManager.loadIdentityByKey(logEntry.getIdentityKey());
			userFullname = userManager.getUserDisplayName(logIdentity);
		}

		initForm(ureq);
	}

	@Override
	public void initTools() {
		toolbarPanel.setToolbarEnabled(true);
		
		previousEntry = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousEntry.setTitle(translate("command.previous"));
		toolbarPanel.addTool(previousEntry, Align.rightEdge, false, "o_tool_previous");
		nextEntry = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextEntry.setTitle(translate("command.next"));
		toolbarPanel.addTool(nextEntry, Align.rightEdge, false, "o_tool_next");
	}

	@Override
	protected void doDispose() {
		toolbarPanel.setToolbarEnabled(false);
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String date = Formatter.getInstance(getLocale()).formatDateAndTime(logEntry.getCreationDate());
		uifactory.addStaticTextElement("date", date, formLayout);
		
		if(StringHelper.containsNonWhitespace(userFullname)) {
			uifactory.addStaticTextElement("user", userFullname, formLayout);
		}
		uifactory.addStaticTextElement("effective.directive", logEntry.getEffectiveDirective(), formLayout);
		uifactory.addStaticTextElement("blocked.uri", logEntry.getBlockedUri(), formLayout);
		uifactory.addStaticTextElement("document.uri", logEntry.getDocumentUri(), formLayout);
		uifactory.addStaticTextElement("referrer", logEntry.getReferrer(), formLayout);
		uifactory.addStaticTextElement("source.file", logEntry.getSourceFile(), formLayout);
		
		uifactory.addStaticTextElement("violated.directive", logEntry.getViolatedDirective(), formLayout);
		
		if(logEntry.getLineNumber() != null) {
			uifactory.addStaticTextElement("line.number", logEntry.getLineNumber().toString(), formLayout);
		}
		if(logEntry.getColumnNumber() != null) {
			uifactory.addStaticTextElement("column.number", logEntry.getColumnNumber().toString(), formLayout);
		}
		if(StringHelper.containsNonWhitespace(logEntry.getScriptSample())) {
			uifactory.addStaticTextElement("script.sample", logEntry.getScriptSample(), formLayout);
		}

		uifactory.addStaticTextElement("original.policy", logEntry.getOriginalPolicy(), formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttonsCont", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == nextEntry) {
			fireEvent(ureq, new NextEntryEvent(logEntry));
		} else if(source == previousEntry) {
			fireEvent(ureq, new PreviousEntryEvent(logEntry));
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
