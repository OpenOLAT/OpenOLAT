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
* <p>
*/
package org.olat.core.commons.chiefcontrollers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  12.06.2009 <br>
 * @author patrickb
 */
@RunWith(JUnit4.class)
public class ChiefControllerMessageEventTest {
	
	
	/**
	 * test the overriden equals method.
	 */
	@Test public void testEquals(){
		String msg_A = "A Test Message";
		//clusterwide with msg_A
		ChiefControllerMessageEvent ccme_A_clusterwide = new ChiefControllerMessageEvent();
		ccme_A_clusterwide.setMsg(msg_A);
		ccme_A_clusterwide.setClusterWideMessage(true);
		
		//not clusterwiede with same msg_A
		ChiefControllerMessageEvent ccme_A = new ChiefControllerMessageEvent();
		ccme_A.setMsg(msg_A);
		ccme_A.setClusterWideMessage(false);
		
		//they are not equal!!
		assertFalse("same message, but one is clusterwide other not.",ccme_A.equals(ccme_A_clusterwide));
		//check null value
		assertFalse("never equal with <null>", ccme_A.equals(null));
		//same objects are equal
		assertTrue("same objectRefs, clusterwide", ccme_A_clusterwide.equals(ccme_A_clusterwide));
		assertTrue("same objectRefs", ccme_A.equals(ccme_A));
		

		/*
		 * compare A with B, both with same message
		 */
		String msg_B = "A Test Message";
		//clusterwide with msg_B
		ChiefControllerMessageEvent ccme_B_clusterwide = new ChiefControllerMessageEvent();
		ccme_B_clusterwide.setMsg(msg_B);
		ccme_B_clusterwide.setClusterWideMessage(true);
		
		//not clusterwiede with same msg_B
		ChiefControllerMessageEvent ccme_B = new ChiefControllerMessageEvent();
		ccme_B.setMsg(msg_B);
		ccme_B.setClusterWideMessage(false);
		
		//A and B's are equal
		assertTrue("same message, and both clusterwide",ccme_A_clusterwide.equals(ccme_B_clusterwide));
		assertTrue("same message",ccme_A.equals(ccme_B));		
		
		/*
		 * compate A with C, with different Messages 
		 */
		String msg_C = "another message";
	//clusterwide with msg_C
		ChiefControllerMessageEvent ccme_C_clusterwide = new ChiefControllerMessageEvent();
		ccme_C_clusterwide.setMsg(msg_C);
		ccme_C_clusterwide.setClusterWideMessage(true);
		
		//not clusterwiede with same msg_C
		ChiefControllerMessageEvent ccme_C = new ChiefControllerMessageEvent();
		ccme_C.setMsg(msg_C);
		ccme_C.setClusterWideMessage(false);
		
		assertFalse("not same message, but both clusterwide",ccme_C.equals(ccme_A));
		assertFalse("not same message",ccme_C.equals(ccme_A));
	}
	
	
	@Test public void testHashCode(){

		String msg_A = "A Test Message";
		//clusterwide with msg_A
		ChiefControllerMessageEvent ccme_A_clusterwide = new ChiefControllerMessageEvent();
		ccme_A_clusterwide.setMsg(msg_A);
		ccme_A_clusterwide.setClusterWideMessage(true);
		
		//not clusterwiede with same msg_A
		ChiefControllerMessageEvent ccme_A = new ChiefControllerMessageEvent();
		ccme_A.setMsg(msg_A);
		ccme_A.setClusterWideMessage(false);
		
		//same objects are equal according to the equals method (see other equal testcase)
		assertTrue("MUST BE SAME HASHCODE: same objectRefs, clusterwide", ccme_A_clusterwide.hashCode() == ccme_A_clusterwide.hashCode());
		//subsequent call must still remain the same hashcode
		assertTrue("MUST BE SAME HASHCODE: same objectRefs, clusterwide", ccme_A_clusterwide.hashCode() == ccme_A_clusterwide.hashCode());
		
		assertTrue("MUST BE SAME HASHCODE: same objectRefs", ccme_A.hashCode() == ccme_A.hashCode());
		assertTrue("MUST BE SAME HASHCODE: same objectRefs", ccme_A.hashCode() == ccme_A.hashCode());
		

		/*
		 * compare A with B, both with same message
		 */
		String msg_B = "A Test Message";
		//clusterwide with msg_B
		ChiefControllerMessageEvent ccme_B_clusterwide = new ChiefControllerMessageEvent();
		ccme_B_clusterwide.setMsg(msg_B);
		ccme_B_clusterwide.setClusterWideMessage(true);
		
		//not clusterwiede with same msg_B
		ChiefControllerMessageEvent ccme_B = new ChiefControllerMessageEvent();
		ccme_B.setMsg(msg_B);
		ccme_B.setClusterWideMessage(false);
		
		//A and B's are equal
		assertTrue("MUST BE SAME HASHCODE: same message, and both clusterwide",ccme_A_clusterwide.hashCode() == ccme_B_clusterwide.hashCode());
		assertTrue("MUST BE SAME HASHCODE: same message, and both clusterwide",ccme_A_clusterwide.hashCode() == ccme_B_clusterwide.hashCode());
		
		assertTrue("MUST BE SAME HASHCODE: same message",ccme_A.hashCode() == ccme_B.hashCode());
		assertTrue("MUST BE SAME HASHCODE: same message",ccme_A.hashCode() == ccme_B.hashCode());
		
	}
	
	
}
