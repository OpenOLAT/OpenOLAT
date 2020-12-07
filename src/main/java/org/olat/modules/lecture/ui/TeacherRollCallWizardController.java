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
package org.olat.modules.lecture.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.RollCallSecurityCallback;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherRollCallWizardController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private NextPreviousController nextPreviousCtrl;
	private SingleParticipantCallController participantCtrl;
	private CloseRollCallConfirmationController closeRollCallCtrl;
	
	private LectureBlock lectureBlock;
	private List<Identity> participants;
	private Identity calledIdentity;
	private RollCallSecurityCallback secCallback;
	
	@Autowired
	private UserManager userManager;
	
	public TeacherRollCallWizardController(UserRequest ureq, WindowControl wControl,
			LectureBlock lectureBlock, List<Identity> participants, RollCallSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.lectureBlock = lectureBlock;
		this.participants = participants;
		if(!participants.isEmpty()) {
			calledIdentity = participants.get(0);
		}
		
		mainVC = createVelocityContainer("teacher_wizard");
		nextPreviousCtrl = new NextPreviousController(ureq, getWindowControl());
		listenTo(nextPreviousCtrl);
		mainVC.put("nextPrevious", nextPreviousCtrl.getInitialComponent());
		putInitialPanel(mainVC);
		
		if(calledIdentity == null) {
			doCloseRollCall(ureq);
		} else {
			doSelect(ureq, calledIdentity);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(participantCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doNext(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				doClose(ureq);
			} else if(event == Event.CLOSE_EVENT) {
				doCloseRollCall(ureq);
			}
		} else if(closeRollCallCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CANCELLED_EVENT) {
				doClose(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	private void doNext(UserRequest ureq) {
		int index = participants.indexOf(calledIdentity);
		if(index >= 0 && index + 1 < participants.size()) {
			calledIdentity = participants.get(index + 1);
			doSelect(ureq, calledIdentity);
		} else if(index + 1 >= participants.size() || index == -1) {
			doCloseRollCall(ureq);
		}
	}
	
	private void doCloseRollCall(UserRequest ureq) {
		removeAsListenerAndDispose(participantCtrl);
		removeAsListenerAndDispose(closeRollCallCtrl);
		
		closeRollCallCtrl = new CloseRollCallConfirmationController(ureq, getWindowControl(), lectureBlock, secCallback);
		listenTo(closeRollCallCtrl);
		calledIdentity = null;

		mainVC.put("call", closeRollCallCtrl.getInitialComponent());
		nextPreviousCtrl.updateNextPrevious(null);
	}
	
	private void doPrevious(UserRequest ureq) {
		int index;
		if(closeRollCallCtrl != null) {
			removeAsListenerAndDispose(closeRollCallCtrl);
			closeRollCallCtrl = null;
			index = participants.size();
		} else {
			index = participants.indexOf(calledIdentity);
		}
		
		if(index > 0 && index <= participants.size()) {
			calledIdentity = participants.get(index - 1);
			doSelect(ureq, calledIdentity);
		}
	}

	private void doSelect(UserRequest ureq, Long callIdentityKey) {
		for(Identity participant:participants) {
			if(participant.getKey().equals(callIdentityKey)) {
				calledIdentity = participant;
				doSelect(ureq, participant);
			}
		}
	}
	
	private void doSelect(UserRequest ureq, Identity callIdentity) {
		removeAsListenerAndDispose(participantCtrl);
		
		participantCtrl = new SingleParticipantCallController(ureq, getWindowControl(), lectureBlock, callIdentity);
		listenTo(participantCtrl);
		mainVC.put("call", participantCtrl.getInitialComponent());
		
		nextPreviousCtrl.updateNextPrevious(callIdentity);
	}
	
	private void doClose(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private class NextPreviousController extends FormBasicController {
		
		private SingleSelection participantsEl;
		private FormLink nextLink, previousLink, closeLink;
		
		private String[] participantKeys;
		
		public NextPreviousController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "teacher_wizard_buttons");
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			nextLink = uifactory.addFormLink("next.participant", formLayout, Link.BUTTON);
			nextLink.setIconRightCSS("o_icon o_icon-lg o_icon_next");
			nextLink.setElementCssClass("o_sel_next");
			
			previousLink = uifactory.addFormLink("previous.participant", formLayout, Link.BUTTON);
			previousLink.setIconLeftCSS("o_icon o_icon-lg o_icon_previous");
			previousLink.setElementCssClass("o_sel_previous");
			
			closeLink = uifactory.addFormLink("close", formLayout, Link.BUTTON);
			closeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_close");
			closeLink.setElementCssClass("o_sel_close");
			
			participantKeys = new String[participants.size()];
			String[] participantValues = new String[participants.size()];
			for(int i=participants.size(); i-->0; ) {
				Identity participant = participants.get(i);
				participantKeys[i] = participant.getKey().toString();
				participantValues[i] = userManager.getUserDisplayName(participant);
			}

			participantsEl = uifactory.addDropdownSingleselect("participants", null, formLayout,
					participantKeys, participantValues, null);
			participantsEl.setDomReplacementWrapperRequired(false);
			participantsEl.addActionListener(FormEvent.ONCHANGE);
		}

		protected void updateNextPrevious(Identity callIdentity) {
			if(callIdentity == null) {
				//last step
				nextLink.setVisible(false);
				participantsEl.setVisible(false);
				previousLink.setEnabled(!participants.isEmpty());
			} else {
				int index = participants.indexOf(callIdentity);
				nextLink.setVisible(true);
				nextLink.setEnabled(index >= 0 && index + 1 <= participants.size());
				previousLink.setEnabled(index > 0);
				participantsEl.setVisible(true);
				String calledIdentityKey = callIdentity.getKey().toString();
				for(String participantKey:participantKeys) {
					if(participantKey.equals(calledIdentityKey)) {
						participantsEl.select(participantKey, true);
						break;
					}	
				}
			}
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(nextLink == source) {
				doNext(ureq);
			} else if(previousLink == source) {
				doPrevious(ureq);
			} else if(closeLink == source) {
				doClose(ureq);
			} else if(participantsEl == source) {
				if(participantsEl.isOneSelected()) {
					Long identityKey = Long.valueOf(participantsEl.getSelectedKey());
					doSelect(ureq, identityKey);
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		@Override
		protected void doDispose() {
			//
		}
	}
}