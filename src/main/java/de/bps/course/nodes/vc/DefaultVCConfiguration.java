//<OLATCE-103>
/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2011 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
 */
package de.bps.course.nodes.vc;

import java.io.Serializable;
import java.util.List;


/**
 * 
 * Description:<br>
 * Standard configuration object, each provider implementation must override this class
 * and extend it with it's specific configuration values.
 * 
 * <P>
 * Initial Date:  18.01.2011 <br>
 * @author skoeber
 */
public abstract class DefaultVCConfiguration implements VCConfiguration, Serializable {
	
	public static String DEFAULT_TEMPLATE = "default";
	
	private String providerId;
	private String templateKey;
	private List<MeetingDate> meetingDatas;
	private boolean useMeetingDates;
	private boolean createMeetingImmediately;

	@Override
  public String getProviderId() {
    return providerId;
  }

  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
  
  @Override
  public String getTemplateKey() {
    return templateKey;
  }
  
  public void setTemplateKey(String templateKey) {
    this.templateKey = templateKey;
  }
  
  @Override
  public boolean isUseMeetingDates() {
  	return useMeetingDates;
  }
  
  public void setUseMeetingDates(boolean useMeetingDates) {
  	this.useMeetingDates = useMeetingDates;
  }
  
  @Override
  public List<MeetingDate> getMeetingDates() {
    return meetingDatas;
  }
  
  public void setMeetingDatas(List<MeetingDate> meetingDatas) {
    this.meetingDatas = meetingDatas;
  }

	public void setCreateMeetingImmediately(boolean createMeetingImmediately) {
		this.createMeetingImmediately = createMeetingImmediately;
	}

	@Override
	public boolean isCreateMeetingImmediately() {
		return createMeetingImmediately;
	}

	@Override
	public abstract boolean isConfigValid();

}
//</OLATCE-103>