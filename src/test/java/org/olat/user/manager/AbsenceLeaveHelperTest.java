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
package org.olat.user.manager;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.AbsenceLeave;
import org.olat.user.model.AbsenceLeaveImpl;

/**
 * 
 * Initial date: 16 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceLeaveHelperTest {
	
	@Test
	public void accept_resource() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Test", 234l);
		AbsenceLeave absenceLeave = createAbsenceLeave(addDaysToNow(-5), addDaysToNow(5), ores, null); 
		boolean onLeave = AbsenceLeaveHelper.isOnLeave(new Date(), absenceLeave, ores, null);
		Assert.assertTrue(onLeave);
	}
	
	@Test
	public void accept_noResource() {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Test", 235l);
		AbsenceLeave absenceLeave = createAbsenceLeave(addDaysToNow(-5), addDaysToNow(5), ores, null); 
		boolean onLeave = AbsenceLeaveHelper.isOnLeave(new Date(), absenceLeave, null, null);
		Assert.assertFalse(onLeave);
	}
	
	private AbsenceLeave createAbsenceLeave(Date start, Date end, OLATResourceable ores, String subIdent) {
		AbsenceLeaveImpl absenceLeave = new AbsenceLeaveImpl();
		absenceLeave.setAbsentFrom(start);
		absenceLeave.setAbsentTo(end);
		if(ores != null) {
			absenceLeave.setResName(ores.getResourceableTypeName());
			absenceLeave.setResId(ores.getResourceableId());
		}
		absenceLeave.setSubIdent(subIdent);
		return absenceLeave;
	}
	
	private Date addDaysToNow(int days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

}
