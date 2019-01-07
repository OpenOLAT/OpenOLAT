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
package org.olat.modules.edusharing.manager;

import org.edu_sharing.webservices.authbyapp.AuthenticationException;
import org.edu_sharing.webservices.usage2.Usage2Exception_Exception;
import org.olat.core.id.Identity;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.DeleteUsageParameter;
import org.olat.modules.edusharing.EdusharingClient;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingProperties;
import org.olat.modules.edusharing.EdusharingResponse;
import org.olat.modules.edusharing.GetPreviewParameter;
import org.olat.modules.edusharing.GetRenderedParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingClientImpl implements EdusharingClient {
	
	@Autowired
	private EdusharingHttpClient httpClient;
	@Autowired
	private EdusharingSoapClient soapClient;

	@Override
	public EdusharingProperties getRepoConfig() throws EdusharingException {
		return httpClient.getMetadata();
	}

	@Override
	public String createTicket(Identity identity) throws EdusharingException {
		try {
			return soapClient.createTicket(identity);
		} catch (AuthenticationException e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public boolean validateTicket(String ticket) throws EdusharingException {
		try {
			return soapClient.valdateTicket(ticket);
		} catch (AuthenticationException e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public EdusharingResponse getPreview(GetPreviewParameter parameter) throws EdusharingException {
		try {
			return httpClient.getPreview(parameter);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public EdusharingResponse getRendered(GetRenderedParameter parameter) throws EdusharingException {
		try {
			return httpClient.getRendered(parameter);
		} catch (Exception e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public String getRenderUrl(GetRenderedParameter parameter) {
		return httpClient.getRenderUrl(parameter);
	}

	@Override
	public void createUsage(CreateUsageParameter parameter) throws EdusharingException {
		try {
			soapClient.createUsage(parameter);
		} catch (Usage2Exception_Exception e) {
			throw new EdusharingException(e);
		}
	}

	@Override
	public void deleteUsage(DeleteUsageParameter parameter) throws EdusharingException {
		try {
			soapClient.deleteUsage(parameter);
		} catch (Usage2Exception_Exception e) {
			throw new EdusharingException(e);
		}
	}

}
