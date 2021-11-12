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
package org.olat.core.util.resource;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 12 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class OresHelperTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { null, null, false },// important
                { OresHelper.createOLATResourceableInstance("CourseModule", 1234l), OresHelper.createOLATResourceableInstance("CourseModule", 1234l), true },
                { OresHelper.createOLATResourceableInstance("CourseModule", 1234l), OresHelper.createOLATResourceableInstance("CourseModule", 1235l), false },
                { OresHelper.createOLATResourceableInstance("Test", 1234l), OresHelper.createOLATResourceableInstance("CourseModule", 1234l), false },
                { new NullTypeNameTester(), OresHelper.createOLATResourceableInstance("CourseModule", 1234l), false },
        });
    }
    
    private OLATResourceable o1;
    private OLATResourceable o2;
    private boolean isEqual;
    
    public OresHelperTest(OLATResourceable o1, OLATResourceable o2, boolean isEqual) {
    	this.o1 = o1;
    	this.o2 = o2;
    	this.isEqual = isEqual;
    }
	
	@Test
	public void checkEquals() {
		Assert.assertEquals(isEqual, OresHelper.equals(o1, o2));
	}
	
	private static class NullTypeNameTester implements OLATResourceable {

		@Override
		public Long getResourceableId() {
			return Long.valueOf(0);
		}

		@Override
		public String getResourceableTypeName() {
			return null;
		}
	}
	

}
