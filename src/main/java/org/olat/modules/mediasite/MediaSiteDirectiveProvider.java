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
package org.olat.modules.mediasite;

import java.util.Arrays;
import java.util.Collection;

import org.olat.core.commons.services.csp.CSPDirectiveProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Initial date: 14.10.2021<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
@Component
public class MediaSiteDirectiveProvider implements CSPDirectiveProvider {

	@Autowired
	private MediaSiteModule mediaSiteModule;
	
	@Override
	public Collection<String> getScriptSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getImgSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getFontSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getConnectSrcUrls() {
		return null;
	}

	@Override
	public Collection<String> getFrameSrcUrls() {
		return getUrls();
	}

	@Override
	public Collection<String> getMediaSrcUrls() {
		return null;
	}
	
	public Collection<String> getUrls() {
		return mediaSiteModule.isEnabled() ? Arrays.asList(mediaSiteModule.getBaseURL(), mediaSiteModule.getAdministrationURL()) : null;
	}

}
