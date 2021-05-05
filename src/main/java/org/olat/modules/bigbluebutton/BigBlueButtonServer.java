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
package org.olat.modules.bigbluebutton;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface BigBlueButtonServer extends ModifiedInfo, CreateInfo {
	
	public Long getKey();
	
	public String getName();

	public void setName(String name);

	public String getUrl();

	public void setUrl(String url);

	public String getSharedSecret();

	public void setSharedSecret(String secret);

	public String getRecordingUrl();

	public void setRecordingUrl(String recordingUrl);

	public boolean isEnabled();

	public void setEnabled(boolean enabled);
	
	public boolean isManualOnly();
	
	public void setManualOnly(boolean manualOnly);
	
	public Double getCapacityFactory();

	public void setCapacityFactory(Double capacityFactory);

}
