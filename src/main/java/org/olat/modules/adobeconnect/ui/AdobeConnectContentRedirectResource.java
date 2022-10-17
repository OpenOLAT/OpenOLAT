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
package org.olat.modules.adobeconnect.ui;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;

/**
 * 
 * Initial date: 18 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectContentRedirectResource implements MediaResource  {

	private static final Logger log = Tracing.createLoggerFor(AdobeConnectContentRedirectResource.class);

	private final Identity identity;
	private final AdobeConnectSco sco;

	/**
	 * @param redirectURL
	 */
	public AdobeConnectContentRedirectResource(Identity identity, AdobeConnectSco sco) {
		this.sco = sco;
		this.identity = identity;
	}
	
	@Override
	public long getCacheControlDuration() {
		return 0;
	}

	@Override
	public boolean acceptRanges() {
		return false;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public Long getSize() {
		return null;
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public void prepare(HttpServletResponse hres) {
		try {
			AdobeConnectManager adobeConnectManager = CoreSpringFactory.getImpl(AdobeConnectManager.class);
			AdobeConnectErrors error = new AdobeConnectErrors();
			String redirectUrl = adobeConnectManager.linkTo(sco, identity, error);
			hres.sendRedirect(redirectUrl);
		} catch (IOException | IllegalStateException ise){
			log.error("redirect failedto adobe connect failed", ise);
		}
	}

	@Override
	public void release() {
		// nothing to do
	}
}
