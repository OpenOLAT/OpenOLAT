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

package org.olat.group.ui.wizard;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.ui.BGTranslatorFactory;
import org.olat.group.ui.BusinessGroupFormController;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <BR>
 * Two step wizard to copy a given business group. In the first step the user is
 * asked about what should be copied, in the second step the usr is asked for a
 * groupname and the groups details.
 * <P>
 * Initial Date: Sep 11, 2004
 * 
 * @author Florian Gnägi
 */
public class BGCopyWizardController extends WizardController {
	
	private static final String PACKAGE = Util.getPackageName(BGCopyWizardController.class);

	private BGCopyWizardCopyForm copyForm;
	private BusinessGroupFormController groupController;
	private Translator trans;
	private BusinessGroup originalGroup, copiedGroup;
	
	private final BusinessGroupService bgs;

	/**
	 * Constructor fot the business group copy wizard
	 * 
	 * @param ureq
	 * @param wControl
	 * @param originalGroup original business group: master that should be copied
	 * @param flags
	 */
	public BGCopyWizardController(UserRequest ureq, WindowControl wControl, BusinessGroup originalGroup) {
		super(ureq, wControl, 2);
		setBasePackage(BGCopyWizardController.class);
		bgs = CoreSpringFactory.getImpl(BusinessGroupService.class);
		this.trans = BGTranslatorFactory.createBGPackageTranslator(PACKAGE, originalGroup.getType(), ureq.getLocale());
		this.originalGroup = originalGroup;
		// init wizard step 1
		copyForm = new BGCopyWizardCopyForm(ureq, wControl);
		listenTo(copyForm);
		// init wizard title and set step 1
		setWizardTitle(trans.translate("bgcopywizard.title"));
		setNextWizardStep(trans.translate("bgcopywizard.copyform.title"), copyForm.getInitialComponent());
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == this.groupController) {
			if (event == Event.DONE_EVENT) {
				copiedGroup = doCopyGroup();
				// finished event
				fireEvent(ureq, Event.DONE_EVENT);
				// do logging
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.BG_GROUP_COPIED, getClass(), 
						LoggingResourceable.wrap(originalGroup), LoggingResourceable.wrap(copiedGroup));
			}
		}
		else if (source == copyForm) {
			if (event == Event.DONE_EVENT) {
				removeAsListenerAndDispose(groupController);
				groupController = new BusinessGroupFormController(ureq, getWindowControl(), originalGroup);
				listenTo(groupController);
				
				groupController.setGroupName(originalGroup.getName() + " " + trans.translate("bgcopywizard.copyform.name.copy"));
				
				setNextWizardStep(this.trans.translate("bgcopywizard.detailsform.title"), this.groupController.getInitialComponent());
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// default wizard will lissen to cancel wizard event
		super.event(ureq, source, event);
		// now wizard steps events
		
	}

	private BusinessGroup doCopyGroup() {
		originalGroup = bgs.loadBusinessGroup(originalGroup);
		String bgName = groupController.getGroupName();
		String bgDesc = groupController.getGroupDescription();
		Integer bgMax = groupController.getGroupMax();
		Integer bgMin = groupController.getGroupMin();

		BusinessGroup newGroup = bgs.copyBusinessGroup(originalGroup, bgName, bgDesc,
				bgMin, bgMax, null, null,
				copyForm.isCopyAreas(), copyForm.isCopyTools(), copyForm.isCopyRights(), copyForm.isCopyOwners(),
				copyForm.isCopyParticipants(), copyForm.isCopyMembersVisibility(), copyForm.isCopyWaitingList(),
				true /*copy resources */ );
		return newGroup;
	}

	/**
	 * @return The new business group created as a copy of the original business
	 *         group
	 */
	public BusinessGroup getNewGroup() {
		return copiedGroup;
	}
}