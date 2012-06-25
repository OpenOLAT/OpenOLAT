/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.commons.coordinate.cluster.jms;

import java.util.Date;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.chiefcontrollers.ChiefControllerMessageEvent;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.id.User;
import org.olat.core.util.ObjectCloner;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;

public class JmsTestSer {

	/**
	 * @param args
	 * 
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) throws Exception {
		
		Identity ident = new Identity() {

			@SuppressWarnings("unused")
			public Date getDeleteEmailDate() {
				return null;
			}
			public Date getLastLogin() {
				return null;
			}
			public String getName() {
				return null;
			}
			public Integer getStatus() {
				return Identity.STATUS_ACTIV;
			}
			public User getUser() {
				return null;
			}
			
			public void setDeleteEmailDate(Date newDeleteEmail) {
				//
			}

			public void setLastLogin(Date loginDate) {
				//
			}

			public void setStatus(Integer newStatus) {
				//
			}

			public Date getCreationDate() {
				return null;
			}

			public Date getLastModified() {
				return null;
			}

			public boolean equalsByPersistableKey(Persistable persistable) {
				return persistable.getKey().equals(getKey());
			}

			public Long getKey() {
				return new Long(12345);
			}
			public void setName(String name) {
				// TODO Auto-generated method stub
				
			}};
		/*Object o = new String("test");
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data.out"));
		//out.writeObject("Data storage");
		out.writeObject(o);
		out.close();
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("data.out"));
		Object o2 = in.readObject();
		System.out.println("received:"+o2);
*/
		System.out.println("start!");
		//Object o = new MultiUserEvent("test1234");
		/*Object o = new ObjectAccessEvent(1, new OLATResourceable(){
			public Long getResourceableId() {
				return new Long(456);
			}
			public String getResourceableTypeName() {
				return "firstargtype";
			}});
		Object o2 = ObjectCloner.deepCopy(o);
		System.out.println("done! "+o2);
		*/
		
		/*
		 * --------------------------------------------------------------------------------------------------------------
		 */
		AssessmentChangedEvent ace = new AssessmentChangedEvent(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED, ident);
		
		System.out.println("result:"+ObjectCloner.deepCopy(ace));
		
		ChiefControllerMessageEvent ccme = new ChiefControllerMessageEvent();
		ccme.setMsg("yes, it is a message");
		System.out.println("result:"+ObjectCloner.deepCopy(ccme));

		BusinessGroupModifiedEvent bgme = new BusinessGroupModifiedEvent("com", new BusinessGroup(){

			public Boolean getAutoCloseRanksEnabled() {
				return null;
			}

			public String getDescription() {
				return null;
			}

			public Date getLastUsage() {
				return null;
			}

			public Integer getMaxParticipants() {
				return null;
			}

			public Integer getMinParticipants() {
				return null;
			}

			public String getName() {
				return null;
			}

			public SecurityGroup getOwnerGroup() {
				return null;
			}

			public SecurityGroup getPartipiciantGroup() {
				return null;
			}

			public String getType() {
				return null;
			}

			public SecurityGroup getWaitingGroup() {
				return null;
			}

			public Boolean getWaitingListEnabled() {
				return null;
			}

			public void setAutoCloseRanksEnabled(Boolean autoCloseRanksEnabled) {
				//
			}

			public void setDescription(String description) {
				//
			}

			public void setLastUsage(Date lastUsage) {
				//
			}

			public void setMaxParticipants(Integer maxParticipants) {
				//
			}

			public void setMinParticipants(Integer minParticipants) {
				//
			}

			public void setName(String name) {
				//
			}

			public void setWaitingGroup(SecurityGroup waitingGroup) {
				//				
			}

			public void setWaitingListEnabled(Boolean waitingListEnabled) {
				//
			}

			public boolean equalsByPersistableKey(Persistable persistable) {
				return false;
			}

			public Long getKey() {
				return new Long(678);
			}

			public Date getCreationDate() {
				return null;
			}

			public Date getLastModified() {
				return null;
			}

			public Long getResourceableId() {
				return null;
			}

			public String getResourceableTypeName() {
				return null;
			}

			public Date getDeleteEmailDate() {
				return null;
			}

			public void setDeleteEmailDate(Date deleteEmailDate) {
				//
			}

			public void setLastModified(Date date) {
				//
				
			}}, ident);
		System.out.println("bgme result:"+ObjectCloner.deepCopy(bgme));
		
		
		
	}

}
