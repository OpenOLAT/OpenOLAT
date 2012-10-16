/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.ui.edit;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.model.DisplayMembers;



/**
 * Description:<BR>
 * Form having a save button which applies to two checkboxes asking for
 * showing/hiding the owners, partipiciants respectively, to the partipiciants.
 * <P>
 * Initial Date: Sep 22, 2004
 * 
 * @author patrick
 */

public class DisplayMemberSwitchForm extends FormBasicController {

	private SelectionElement showOwners, showPartips, showWaitingList;
	private SelectionElement openOwners, openPartips, openWaitingList;
	private SelectionElement downloadList;
	private boolean hasOwners, hasPartips, hasWaitingList;

	/**
	 * @param name
	 * @param transl
	 * @param hasPartips
	 * @param hasOwners
	 */
	public DisplayMemberSwitchForm(UserRequest ureq, WindowControl wControl, boolean hasOwners, boolean hasPartips, boolean hasWaitingList) {
		super(ureq, wControl);
		this.hasOwners = hasOwners;
		this.hasPartips = hasPartips;
		this.hasWaitingList = hasWaitingList;
		
		initForm(ureq);
	}
	
	public DisplayMembers getDisplayMembers() {
		DisplayMembers displayMembers = new DisplayMembers();
		displayMembers.setShowOwners(showOwners.isSelected(0));
		displayMembers.setShowParticipants(showPartips.isSelected(0));
		displayMembers.setShowWaitingList(showWaitingList.isVisible() && showWaitingList.isEnabled() && showWaitingList.isSelected(0));
		displayMembers.setOwnersPublic(openOwners.isSelected(0));
		displayMembers.setParticipantsPublic(openPartips.isSelected(0));
		displayMembers.setWaitingListPublic(openWaitingList.isVisible() && openWaitingList.isEnabled() && openWaitingList.isSelected(0));
		displayMembers.setDownloadLists(downloadList.isSelected(0));
		return displayMembers;
	}
	
	public void setDisplayMembers(DisplayMembers displayMembers) {
		showOwners.select("xx", displayMembers.isShowOwners());
		showPartips.select("xx", displayMembers.isShowParticipants());
		showWaitingList.select("xx", displayMembers.isShowWaitingList());
		openOwners.select("xx", displayMembers.isOwnersPublic());
		openPartips.select("xx", displayMembers.isParticipantsPublic());
		openWaitingList.select("xx", displayMembers.isWaitingListPublic());
		downloadList.select("xx", displayMembers.isDownloadLists());
	}
	
	public void setWaitingListReadOnly(boolean b) {
		showWaitingList.setEnabled(b);
		openWaitingList.setEnabled(b);
	}
	
	public void setWaitingListVisible(boolean b) {
		showWaitingList.setVisible(b);
		openWaitingList.setVisible(b);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent (ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		showOwners = uifactory.addCheckboxesVertical("ShowOwners", "chkBox.show.owners", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		showOwners.setVisible(hasOwners);
		showPartips = uifactory.addCheckboxesVertical("ShowPartips", "chkBox.show.partips", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		showPartips.setVisible(hasPartips);
		showWaitingList = uifactory.addCheckboxesVertical("ShowWaitingList", "chkBox.show.waitingList", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		showWaitingList.setVisible(hasWaitingList);

		openOwners = uifactory.addCheckboxesVertical("OpenOwners", "chkBox.open.owners", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		openOwners.setVisible(hasOwners);
		openPartips = uifactory.addCheckboxesVertical("OpenPartips", "chkBox.open.partips", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		openPartips.setVisible(hasPartips);
		openWaitingList = uifactory.addCheckboxesVertical("OpenWaitingList", "chkBox.open.waitingList", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);
		openWaitingList.setVisible(hasWaitingList);

		downloadList = uifactory.addCheckboxesVertical("DownloadList", "chkBox.open.downloadList", formLayout, new String[]{"xx"}, new String[]{""}, null, 1);

		showOwners.addActionListener(this, FormEvent.ONCLICK);
		showPartips.addActionListener(this, FormEvent.ONCLICK);
		showWaitingList.addActionListener(this, FormEvent.ONCLICK);
		openOwners.addActionListener(this, FormEvent.ONCLICK);
		openPartips.addActionListener(this, FormEvent.ONCLICK);
		openWaitingList.addActionListener(this, FormEvent.ONCLICK);
		downloadList.addActionListener(this, FormEvent.ONCLICK);
	}

	@Override
	protected void doDispose() {
		//
	}

}
