/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.roommanagement;

import java.util.Collection;
import java.util.List;

import org.olat.core.commons.services.csp.CSPDirectiveProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 19 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Component
public class RoomDirectiveProvider implements CSPDirectiveProvider {
	
	@Autowired
	private RoomManagementModule roomManagementModule;

	@Override
	public Collection<String> getScriptSrcUrls() {
		return List.of();
	}

	@Override
	public Collection<String> getImgSrcUrls() {
		return roomManagementModule.isEnabled() && roomManagementModule.isMapEnabled()
				? List.of("https://*.tile.openstreetmap.org")
				: List.of();
	}

	@Override
	public Collection<String> getFontSrcUrls() {
		return List.of();
	}

	@Override
	public Collection<String> getConnectSrcUrls() {
		return List.of();
	}

	@Override
	public Collection<String> getFrameSrcUrls() {
		return List.of();
	}

	@Override
	public Collection<String> getMediaSrcUrls() {
		return List.of();
	}

}
