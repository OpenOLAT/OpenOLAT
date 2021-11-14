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
import org.olat.group.BusinessGroup;



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
		super(ureq, wControl, LAYOUT_DEFAULT_6_6);
		this.hasOwners = hasOwners;
		this.hasPartips = hasPartips;
		this.hasWaitingList = hasWaitingList;
		
		initForm(ureq);
	}
	
	public boolean isDisplayOwnersIntern() {
		return showOwners.isSelected(0);
	}
	
	public boolean isDisplayParticipantsIntern() {
		return showPartips.isSelected(0);
	}
	
	public boolean isDisplayWaitingListIntern() {
		return showWaitingList.isVisible() && showWaitingList.isEnabled() && showWaitingList.isSelected(0);
	}
	
	public boolean isDisplayOwnersPublic() {
		return openOwners.isSelected(0);
	}
	
	public boolean isDisplayParticipantsPublic() {
		return openPartips.isSelected(0);
	}
	
	public boolean isDisplayWaitingListPublic() {
		return openWaitingList.isVisible() && openWaitingList.isEnabled() && openWaitingList.isSelected(0);
	}
	
	public boolean isDownloadList() {
		return downloadList.isSelected(0);
	}
	
	public void setDisplayMembers(BusinessGroup group) {
		showOwners.select("show_owners", group.isOwnersVisibleIntern());
		showOwners.setElementCssClass("o_sel_group_show_owners");
		showPartips.select("show_participants", group.isParticipantsVisibleIntern());
		showPartips.setElementCssClass("o_sel_group_show_participants");
		showWaitingList.select("show_waiting_list", group.isWaitingListVisibleIntern());
		showWaitingList.setElementCssClass("o_sel_group_show_waiting_list");
		openOwners.select("open_owners", group.isOwnersVisiblePublic());
		openPartips.select("open_participants", group.isParticipantsVisiblePublic());
		openWaitingList.select("open_waiting_list", group.isWaitingListVisiblePublic());
		downloadList.select("download_list", group.isDownloadMembersLists());
	}
	
	public void setWaitingListReadOnly(boolean b) {
		showWaitingList.setEnabled(b);
		openWaitingList.setEnabled(b);
	}
	
	public void setWaitingListVisible(boolean b) {
		showWaitingList.setVisible(b);
		openWaitingList.setVisible(b);
	}
	
	public void setEnabled(boolean enabled) {
		showOwners.setEnabled(enabled);
		showPartips.setEnabled(enabled);
		showWaitingList.setEnabled(enabled);
		openOwners.setEnabled(enabled);
		openPartips.setEnabled(enabled);
		openWaitingList.setEnabled(enabled);
		downloadList.setEnabled(enabled);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == openOwners && openOwners.isSelected(0)){
			showOwners.select("show_owners", true);
		}
		if(source == openPartips && openPartips.isSelected(0)){
			showPartips.select("show_participants", true);
		}
		if(source == openWaitingList && openWaitingList.isSelected(0)){
			showWaitingList.select("show_waiting_list", true);
		}
		fireEvent (ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		showOwners = uifactory.addCheckboxesHorizontal("ShowOwners", "chkBox.show.owners", formLayout, new String[]{"show_owners"}, new String[]{""});
		showOwners.setVisible(hasOwners);
		showPartips = uifactory.addCheckboxesHorizontal("ShowPartips", "chkBox.show.partips", formLayout, new String[]{"show_participants"}, new String[]{""});
		showPartips.setVisible(hasPartips);
		showWaitingList = uifactory.addCheckboxesHorizontal("ShowWaitingList", "chkBox.show.waitingList", formLayout, new String[]{"show_waiting_list"}, new String[]{""});
		showWaitingList.setVisible(hasWaitingList);

		openOwners = uifactory.addCheckboxesHorizontal("OpenOwners", "chkBox.open.owners", formLayout, new String[]{"open_owners"}, new String[]{""});
		openOwners.setVisible(hasOwners);
		openOwners.setHelpText(translate("chkBox.open.owners.help"));
		openPartips = uifactory.addCheckboxesHorizontal("OpenPartips", "chkBox.open.partips", formLayout, new String[]{"open_participants"}, new String[]{""});
		openPartips.setVisible(hasPartips);
		openPartips.setHelpText(translate("chkBox.open.partips.help"));
		openWaitingList = uifactory.addCheckboxesHorizontal("OpenWaitingList", "chkBox.open.waitingList", formLayout, new String[]{"open_waiting_list"}, new String[]{""});
		openWaitingList.setVisible(hasWaitingList);
		openWaitingList.setHelpText(translate("chkBox.open.waitingList.help"));

		downloadList = uifactory.addCheckboxesHorizontal("DownloadList", "chkBox.open.downloadList", formLayout, new String[]{"download_list"}, new String[]{""});

		showOwners.addActionListener(FormEvent.ONCLICK);
		showPartips.addActionListener(FormEvent.ONCLICK);
		showWaitingList.addActionListener(FormEvent.ONCLICK);
		openOwners.addActionListener(FormEvent.ONCLICK);
		openPartips.addActionListener(FormEvent.ONCLICK);
		openWaitingList.addActionListener(FormEvent.ONCLICK);
		downloadList.addActionListener(FormEvent.ONCLICK);
	}

}
