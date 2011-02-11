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
* <p>
*/ 

package org.olat.notifications;

import org.olat.ControllerFactory;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.core.dispatcher.jumpin.JumpInHandlerFactory;
import org.olat.core.dispatcher.jumpin.JumpInReceptionist;
import org.olat.core.dispatcher.jumpin.JumpInResult;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroupManagerImpl;

/**
 * Description: <br>
 * Initial Date: 23.02.2005 <br>
 * @author Felix Jost
 */
@Deprecated // not to be used after 6.3, all done with businessPaths. 
//FIXME:FG:6.4 Delete Class and remove from olatextconfig.xml
public class SubscriptionJumpInHandlerFactory implements JumpInHandlerFactory {
	private static final String CONST_SID = "sid";

	public SubscriptionJumpInHandlerFactory() {
		// nothing to do
	}

	/**
	 * @see org.olat.core.dispatcher.jumpin.JumpInHandlerFactory#createJumpInHandler(org.olat.core.gui.UserRequest)
	 */
	public JumpInReceptionist createJumpInHandler(UserRequest ureq) {
		// get the subscription id
		String constSID = ureq.getParameter(CONST_SID);
		if (constSID == null) return null;
		Long subscriptionKey = null;
		try {
			subscriptionKey = new Long(constSID.trim());
		} catch (NumberFormatException nfe) {
			return null;
		}

		Subscriber sub = NotificationsManagerImpl.getInstance().getSubscriber(subscriptionKey);
		if (sub == null) { // the subscription does not exist anymore
			return null;
		}
		Identity owner = sub.getIdentity();
		Identity requestor = ureq.getIdentity();
		if (!owner.equalsByPersistableKey(requestor)) throw new OLATSecurityException("tried to access subscription sub, key:" + sub.getKey()
				+ ", owner is :" + owner.getName() + ", but was requested by " + requestor.getName());

		Publisher pub = sub.getPublisher();
		
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(pub.getResName(), pub.getResId());
		final String subidentifier = pub.getSubidentifier();
		final String title = "";

		return new JumpInReceptionist() {
			public String getTitle() {
				return title;
			}

			public OLATResourceable getOLATResourceable() {
				return ores;
			}

			public JumpInResult createJumpInResult(UserRequest aureq, WindowControl wControl) {
				String resName = ores.getResourceableTypeName();
				Long resId = ores.getResourceableId();
				if (subidentifier.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
					resName = CourseModule.ORES_TYPE_COURSE;
				}
				if (subidentifier.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
					resName = BusinessGroupManagerImpl.getInstance().loadBusinessGroup(resId, true).getResourceableTypeName();
				}
				OLATResourceable _ores = OresHelper.createOLATResourceableInstance(resName, resId);
				Controller cont = ControllerFactory.createLaunchController(_ores, subidentifier, aureq, wControl, false);
				return new JumpInResult(cont, subidentifier);
			}

			public String extractActiveViewId(UserRequest ureq) {
				return subidentifier;
			}
		};

	}

}

//class GroupJumpInReceptionist implements JumpInReceptionist

