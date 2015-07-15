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
package org.olat.modules.vitero.restapi;

import java.util.Date;

/**
 * 
 * Initial date: 15.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Examples {
	
	public static final ViteroBookingVO SAMPLE_ViteroBookingVO = new ViteroBookingVO();
	public static final ViteroGroupMemberVO SAMPLE_ViteroGroupMemberVO = new ViteroGroupMemberVO();
	
	  static {
		  SAMPLE_ViteroBookingVO.setAutoSignIn(true);
		  SAMPLE_ViteroBookingVO.setBookingId(23);
		  SAMPLE_ViteroBookingVO.setEnd(new Date());
		  SAMPLE_ViteroBookingVO.setEndBuffer(15);
		  SAMPLE_ViteroBookingVO.setEventName("New event");
		  SAMPLE_ViteroBookingVO.setExternalId("AC-234");
		  SAMPLE_ViteroBookingVO.setGroupId(24);
		  SAMPLE_ViteroBookingVO.setGroupName("NEW-EVENT_OLAT_938745983");
		  SAMPLE_ViteroBookingVO.setRoomSize(22);
		  SAMPLE_ViteroBookingVO.setStart(new Date());
		  SAMPLE_ViteroBookingVO.setStartBuffer(15);
		  SAMPLE_ViteroBookingVO.setTimeZoneId("");
		  
		  SAMPLE_ViteroGroupMemberVO.setGroupRole("participant");
		  SAMPLE_ViteroGroupMemberVO.setIdentityKey(23497l);
	}

}
