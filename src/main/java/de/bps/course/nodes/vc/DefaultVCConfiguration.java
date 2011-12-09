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