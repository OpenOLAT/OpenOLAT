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
package org.olat.course.nodes.iq;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.ui.ChatViewConfig;
import org.olat.instantMessaging.ui.RosterRow;
import org.olat.instantMessaging.ui.SupervisorChatController;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.event.CompletionEvent;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IQSupervisorChatController extends SupervisorChatController {
	
	private final RepositoryEntry testEntry;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	public IQSupervisorChatController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, String resSubPath,
			RepositoryEntry testEntry, ChatViewConfig basisViewConfig) {
		super(ureq, wControl, courseEntry, resSubPath, basisViewConfig);
		this.testEntry = testEntry;
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), courseEntry.getOlatResource());
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof CompletionEvent) {
			processCompletionEvent((CompletionEvent)event);
		}
		super.event(event);
	}
	
	private void processCompletionEvent(CompletionEvent ce) {
		if(!resSubPath.equals(ce.getSubIdent()) || ce.getIdentityKey() == null) return;
		
		String channel = ce.getIdentityKey().toString();
		RosterRow row = tableModel.getObjectByChannel(channel);
		boolean running = ce.getStatus() == AssessmentRunStatus.running;
		if(row.isCanOpenChat() != running) {
			row.setCanOpenChat(running);
			tableEl.reset(false, false, true);
		}
	}
	
	@Override
	protected RosterRow reloadModel(RosterRow row) {
		row = super.reloadModel(row);
		
		if(testEntry != null) {
			List<IdentityRef> identityKeys = row.getRoster().getNonVipEntries().stream()
					.map(RosterEntry::getIdentityKey)
					.map(IdentityRefImpl::new)
					.collect(Collectors.toList());
			
			boolean running = qtiService.isRunningAssessmentTestSession(entry, resSubPath, testEntry, identityKeys);
			row.setCanOpenChat(running);
		}

		return row;
	}
	
	@Override
	protected List<RosterRow> loadModel(boolean reset) {
		final List<RosterRow> loadedRows = super.loadModel(reset);
		final List<Identity> runningList = qtiService.getRunningAssessmentTestSessions(entry, resSubPath, null, false);
		final Set<Long> runnings = runningList.stream()
				.map(Identity::getKey)
				.collect(Collectors.toSet());
		
		for(RosterRow row:loadedRows) {
			boolean running = row.getRoster().getNonVipEntries().stream()
					.map(RosterEntry::getIdentityKey)
					.anyMatch(runnings::contains);
			row.setCanOpenChat(running);
		}
		return loadedRows;
	}

}
