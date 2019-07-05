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
package org.olat.modules.adobeconnect.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.Authentication;
import org.olat.core.id.Identity;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.model.AdobeConnectError;
import org.olat.modules.adobeconnect.model.AdobeConnectErrorCodes;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.olat.modules.adobeconnect.model.AdobeConnectPrincipal;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;
import org.olat.modules.adobeconnect.model.BreezeSession;

/**
 * This is a dummy implementation in case.
 * 
 * Initial date: 23 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NoAdapterProvider implements AdobeConnectSPI {

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public AdobeConnectSco createScoMeeting(String name, String description, String folderScoId, String templateId,
			Date startDate, Date endDate, Locale locale, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public boolean updateScoMeeting(String scoId, String name, String description, String templateId, Date startDate,
			Date endDate, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return false;
	}

	@Override
	public AdobeConnectSco getScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public boolean deleteScoMeeting(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return false;
	}

	@Override
	public AdobeConnectSco createFolder(String name, AdobeConnectErrors errors) {
		errors.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public List<AdobeConnectSco> getFolderByName(String name, AdobeConnectErrors errors) {
		errors.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return new ArrayList<>();
	}

	@Override
	public List<AdobeConnectSco> getMeetingByName(String name, AdobeConnectErrors errors) {
		errors.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return new ArrayList<>();
	}

	@Override
	public List<AdobeConnectSco> getTemplates() {
		return new ArrayList<>();
	}

	@Override
	public List<AdobeConnectSco> getRecordings(AdobeConnectMeeting meeting, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return new ArrayList<>();
	}

	@Override
	public boolean setPermissions(String scoId, boolean allAccess, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return false;
	}

	@Override
	public boolean isManagedPassword() {
		return false;
	}

	@Override
	public boolean isMember(String scoId, String principalId, String permission, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return false;
	}

	@Override
	public boolean setMember(String scoId, String principalId, String permission, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return false;
	}

	@Override
	public AdobeConnectPrincipal getPrincipalByLogin(String login, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public AdobeConnectPrincipal createPrincipal(Identity identity, String login, String password, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public AdobeConnectPrincipal adminCommonInfo(AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}

	@Override
	public BreezeSession commonInfo(Authentication authentication, AdobeConnectErrors error) {
		error.append(new AdobeConnectError(AdobeConnectErrorCodes.serverNotAvailable));
		return null;
	}
}
