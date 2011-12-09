//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.wimba;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;

/**
 * 
 * Description:<br>
 * Config controller to adjust settings of Wimba Classroom, e.g. for usage in course editor
 * 
 * <P>
 * Initial Date:  06.01.2011 <br>
 * @author skoeber
 */
public class WimbaConfigController extends BasicController {
	
	private static String COMMAND_OPEN_ROOMSETTINGS = WimbaClassroomProvider.TARGET_OPEN_ROOMSETTINGS;
  private static String COMMAND_OPEN_MEDIASETTINGS = WimbaClassroomProvider.TARGET_OPEN_MEDIASETTINGS;
	
	// GUI
	private VelocityContainer editVC;
	private WimbaEditForm editForm;
	private Link openRoomSettings, openMediaSettings;
	
	private WimbaClassroomProvider wimba;
	private WimbaClassroomConfiguration config;
	private String roomId;

	protected WimbaConfigController(UserRequest ureq, WindowControl wControl, String roomId, WimbaClassroomProvider wimba, WimbaClassroomConfiguration config) {
		super(ureq, wControl);
		this.wimba = wimba;
		this.config = config;
		this.roomId = roomId;
		
		editVC = createVelocityContainer("edit");
		editForm = new WimbaEditForm(ureq, wControl, config);
		listenTo(editForm);
		editVC.put("editForm", editForm.getInitialComponent());
		
		openRoomSettings = LinkFactory.createButton(COMMAND_OPEN_ROOMSETTINGS, editVC, this);
		openMediaSettings = LinkFactory.createButton(COMMAND_OPEN_MEDIASETTINGS, editVC, this);
		openRoomSettings.setTarget("_blank");
    openMediaSettings.setTarget("_blank");
    
		putInitialPanel(editVC);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editForm) {
			fireEvent(ureq, event);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == openRoomSettings | source == openMediaSettings) {
			boolean exists = wimba.existsClassroom(roomId, config);
			if(exists) {
				openWimbaUrl(ureq, ((Link)source).getCommand());
			} else {
				// normally this case should not occure, but be failsafe and show msg
				getWindowControl().setError("error.no.room");
			}
      return;
    }
	}
	
	private void openWimbaUrl(UserRequest ureq, String target) {
  	boolean success = wimba.createModerator(ureq.getIdentity(), roomId);
  	if(success) {
  		wimba.login(ureq.getIdentity(), null);
  		String url = wimba.createServiceUrl(target, roomId);
  		RedirectMediaResource rmr = new RedirectMediaResource(url);
  		ureq.getDispatchResult().setResultingMediaResource(rmr);
  	} else {
  		// could not create moderator or update the rights
      getWindowControl().setError(translate("error.update.rights"));
      return;
  	}
  }

	@Override
	protected void doDispose() {
		if(editForm != null) {
			removeAsListenerAndDispose(editForm);
			editForm = null;
		}
	}

}
//</OLATCE-103>