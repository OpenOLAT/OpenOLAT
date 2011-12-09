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
 * Each virtual classroom implementation must persist it's configuration
 * by using this interface. The course node is able to differentiate for
 * which implementation the stored configuration is intended (necessary to
 * support multiple virtual classroom implementations in one OLAT instance). 
 * 
 * <P>
 * Initial Date:  20.12.2010 <br>
 * @author skoeber
 */
public interface VCConfiguration extends Serializable {

	public String getProviderId();
	public String getTemplateKey();
	public boolean isUseMeetingDates();
	public List<MeetingDate> getMeetingDates();
	public boolean isCreateMeetingImmediately();
	public boolean isConfigValid();
}
//</OLATCE-103>