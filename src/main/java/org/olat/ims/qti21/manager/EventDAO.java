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
package org.olat.ims.qti21.manager;

import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.marshalling.ItemSessionStateXmlMarshaller;

/**
 * 
 * Initial date: 20.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EventDAO {
	
	public CandidateEvent create(CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey) {
		
		CandidateEvent event = new CandidateEvent();
		event.setTestEventType(textEventType);
		event.setItemEventType(itemEventType);
		if (itemKey != null) {
            event.setTestItemKey(itemKey.toString());
        }
		return event;
	}
	
	
	public CandidateEvent create(CandidateItemEventType itemEventType, ItemSessionState itemSessionState) {
		final CandidateEvent event = new CandidateEvent();
        //event.setCandidateSession(candidateSession);
        event.setItemEventType(itemEventType);
        //event.setTimestamp(requestTimestampContext.getCurrentRequestTimestamp());

        /* Store event */
        //candidateEventDao.persist(event);

        /* Save current ItemSessionState */
        storeItemSessionState(event, itemSessionState);


        return event;
	}
	
    public void storeItemSessionState(CandidateEvent candidateEvent, ItemSessionState itemSessionState) {
        Document stateDocument = ItemSessionStateXmlMarshaller.marshal(itemSessionState);
        //TODO storeStateDocument(candidateEvent, stateDocument);
    }

}
