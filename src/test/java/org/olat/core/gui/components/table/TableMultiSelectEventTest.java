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
package org.olat.core.gui.components.table;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.Before;
import org.junit.Test;

/**
 * Initial Date:  Nov 23, 2010 <br>
 * @author patrick
 */
public class TableMultiSelectEventTest {

	private BitSet oneSelection;
	private BitSet twoSelection;
	private TableMultiSelectEvent one;
	private TableMultiSelectEvent sameRefAsOne;
	private TableMultiSelectEvent sameAsOne;
	private TableMultiSelectEvent two;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		oneSelection = new BitSet();
		twoSelection = new BitSet();
		one = new TableMultiSelectEvent("one","oneAction",oneSelection);
		sameRefAsOne = one;
		sameAsOne = new TableMultiSelectEvent("one","oneAction",oneSelection);
		two = new TableMultiSelectEvent("two","two",twoSelection);
	}
	
	@Test
	public void testEqualsCodeContract() {
		assertFalse("Wrong equals implementation, different types are recognized as equals ",one.equals(new Integer(1)));
		assertFalse("Wrong equals implementation, different objects are recognized as equals ",one.equals(two));
		assertFalse("Wrong equals implementation, null value is recognized as equals ",one.equals(null));
		assertTrue("Wrong equals implementation, same objects are NOT recognized as equals ",one.equals(sameRefAsOne));
		assertTrue("Wrong equals implementation, same objecst are NOT recognized as equals ",one.equals(sameAsOne));
		
	}
	
	@Test
	public void testHashCodeContract() {
		assertTrue("Wrong hashCode implementation, same objects have NOT same hash-code ",one.hashCode() == sameRefAsOne.hashCode());
		assertFalse("Wrong hashCode implementation, different objects have same hash-code",one.hashCode() == two.hashCode());
		assertTrue("Wrong hashCode implementation, same objects have NOT same hash-code ",one.hashCode() == sameAsOne.hashCode());
	}

}
