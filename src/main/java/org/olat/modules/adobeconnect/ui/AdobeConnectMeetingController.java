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
package org.olat.modules.adobeconnect.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectMeetingPermission;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.ui.AdobeConnectContentTableModel.ACContentsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectMeetingController extends FormBasicController {
	
	private final boolean readOnly;
	private final boolean moderator;
	private final boolean administrator;
	private final AdobeConnectMeeting meeting;
	
	private boolean registered;

	private int counter;
	private Link joinButton;
	private FormLink registerButton;
	private FlexiTableElement contentTableEl;
	private AdobeConnectContentTableModel contentModel;
	
	@Autowired
	private AdobeConnectManager adobeConnectManager;
	
	public AdobeConnectMeetingController(UserRequest ureq, WindowControl wControl,
			AdobeConnectMeeting meeting, boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl, "meeting");
		this.meeting = meeting;
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = administrator;
		initForm(ureq);
		
		AdobeConnectErrors error = new AdobeConnectErrors();
		registered = adobeConnectManager.isRegistered(meeting, getIdentity(), getPermission(), error);
		updateButtons();
		
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", meeting.getName());
			if(StringHelper.containsNonWhitespace(meeting.getDescription())) {
				layoutCont.contextPut("description", meeting.getDescription());
			}
			if(meeting.getStartDate() != null) {
				String start = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getStartDate());
				layoutCont.contextPut("start", start);
			}
			if(meeting.getEndDate() != null) {
				String end = Formatter.getInstance(getLocale()).formatDateAndTime(meeting.getEndDate());
				layoutCont.contextPut("end", end);
				if(new Date().after(meeting.getEndDate())) {
					layoutCont.contextPut("ended", Boolean.TRUE);
				}
			}
		}

		registerButton = uifactory.addFormLink("meeting.register.button", flc, Link.BUTTON);

		joinButton = LinkFactory.createButtonLarge("meeting.join.button", flc.getFormItemComponent(), this);
		joinButton.setTarget("_blank");
		
		initContent(formLayout);
	}
	
	protected void initContent(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.icon, new AdobeConnectIconRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.dateBegin, new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ACContentsCols.resource));

		contentModel = new AdobeConnectContentTableModel(columnsModel, getLocale());
		contentTableEl = uifactory.addTableElement(getWindowControl(), "meetingContents", contentModel, 24, false, getTranslator(), formLayout);
		contentTableEl.setCustomizeColumns(false);
		contentTableEl.setNumOfRowsEnabled(false);
		contentTableEl.setEmtpyTableMessageKey("no.contents");
	}
	
	private void loadModel() {
		AdobeConnectErrors error = new AdobeConnectErrors();
		List<AdobeConnectSco> scos = adobeConnectManager.getRecordings(meeting, error);
		List<AdobeConnectContentRow> rows = new ArrayList<>(scos.size());
		for(AdobeConnectSco sco:scos) {
			AdobeConnectContentRow row = new AdobeConnectContentRow(sco);
			if(registered) {
				MediaResource resource = new AdobeConnectContentRedirectResource(getIdentity(), sco);
				DownloadLink openLink = uifactory.addDownloadLink("open-" + (++counter), translate("content.open"), null, resource, contentTableEl);
				row.setOpenLink(openLink);
			}
			rows.add(row);
		}
		contentModel.setObjects(rows);
		contentTableEl.reset(true, true, true);
	}
	
	private void updateButtons() {
		registerButton.setVisible(!registered && !readOnly);
		joinButton.setVisible(registered);
		joinButton.setEnabled(!readOnly);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(joinButton == source) {
			doJoin(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(registerButton == source) {
			doRegister();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doJoin(UserRequest ureq) {
		AdobeConnectErrors errors = new AdobeConnectErrors();
		String meetingUrl = adobeConnectManager.join(meeting, getIdentity(), errors);
		if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			MediaResource redirect = new RedirectMediaResource(meetingUrl);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		}
	}
	
	private void doRegister() {
		AdobeConnectErrors errors = new AdobeConnectErrors();
		registered = adobeConnectManager.registerFor(meeting, getIdentity(), getPermission(), errors);
		if(registered) {
			showInfo("meeting.successfully.registered");
			updateButtons();
		} else if(errors.hasErrors()) {
			getWindowControl().setError(AdobeConnectErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			showInfo("meeting.registration.failed");
		}
	}
	
	private AdobeConnectMeetingPermission getPermission() {
		AdobeConnectMeetingPermission permission;
		if(administrator || moderator) {
			permission = AdobeConnectMeetingPermission.host;
		} else {
			permission = AdobeConnectMeetingPermission.view;
		}
		return permission;
	}
}
