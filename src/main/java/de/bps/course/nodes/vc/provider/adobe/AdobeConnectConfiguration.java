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
package de.bps.course.nodes.vc.provider.adobe;

import java.io.Serializable;

import de.bps.course.nodes.vc.DefaultVCConfiguration;

/**
 *
 * Description:<br>
 * Configuration object for Adobe Connect
 *
 * <P>
 * Initial Date:  20.12.2010 <br>
 * @author skoeber
 */
public class AdobeConnectConfiguration extends DefaultVCConfiguration implements Serializable {

  private boolean guestAccessAllowed;
  private boolean guestStartMeetingAllowed;

  public boolean isGuestAccessAllowed() {
    return guestAccessAllowed;
  }
  public void setGuestAccessAllowed(boolean guestAccessAllowed) {
    this.guestAccessAllowed = guestAccessAllowed;
  }
  public boolean isGuestStartMeetingAllowed() {
    return guestStartMeetingAllowed;
  }
  public void setGuestStartMeetingAllowed(boolean guestStartMeetingAllowed) {
    this.guestStartMeetingAllowed = guestStartMeetingAllowed;
  }
  
  @Override
  public boolean isConfigValid() {
  	boolean valid = true;
  	if(isUseMeetingDates()) {
  		valid = getMeetingDates() != null && !getMeetingDates().isEmpty();
  	}
    return valid;
  }

}
//</OLATCE-103>
