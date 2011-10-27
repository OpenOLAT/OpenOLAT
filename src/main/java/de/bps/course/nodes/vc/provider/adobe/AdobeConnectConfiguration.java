//<OLATCE-103>
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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
