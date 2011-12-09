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
package org.olat.course.statistic.weekly;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TestWeeklyStatisticManager_fillGaps extends TestCase {

	private WeeklyStatisticManager manager_;

	@Override
	protected void setUp() throws Exception {
		manager_ = new WeeklyStatisticManager();
	}

	private void runTest(List<String> input, List<String> expectedOutput) throws Exception {
		List<String> resultingTestset = manager_.fillGapsInColumnHeaders(input);
		assertListEquals(expectedOutput, resultingTestset);
	}
	
	public void testNull() throws Exception {
		assertNull(manager_.fillGapsInColumnHeaders(null));
	}
	
	public void testOne() throws Exception {
		for(int i=0; i<50; i++) {
			String s;
			if (i<10) {
				s = "2010-0"+i;
			} else {
				s = "2010-"+i;
			}
			List<String> resultingTestset = manager_.fillGapsInColumnHeaders(createList(s));
			assertNull(resultingTestset);
		}
	}

	public void testSimple() throws Exception {
		runTest(
				createList("2010-01", "2010-02", "2010-03"), 
				createList("2010-01", "2010-02", "2010-03"));
	}

	public void testYearChange() throws Exception {
		runTest(
				createList("2009-50", "2010-01", "2010-02", "2010-03"), 
				createList("2009-50", "2009-51", "2009-52", "2009-53", "2010-01", "2010-02", "2010-03"));
	}
	
	public void testAllYearChanges() throws Exception {
		for(int i=2000; i<2200; i++) {
			List<String> input = createList(i+"-50", (i+1)+"-03");
			List<String> output = manager_.fillGapsInColumnHeaders(input);
			
			List<String> outputVariant1 = createList(i+"-50", i+"-51", i+"-52", (i+1)+"-01", (i+1)+"-02", (i+1)+"-03");
			List<String> outputVariant2 = createList(i+"-50", i+"-51", i+"-52", i+"-53", (i+1)+"-01", (i+1)+"-02", (i+1)+"-03");
			List<String> outputVariant3 = createList(i+"-50", i+"-51", i+"-52", i+"-53", /*WeeklyStatisticManager left out week 01... */ (i+1)+"-02", (i+1)+"-03");
			
			boolean matchesVariant1 = matches(input, outputVariant1);
			boolean matchesVariant2 = matches(input, outputVariant2);
			boolean matchesVariant3 = matches(input, outputVariant3);
			
			if (matchesVariant1 && !matchesVariant2 && !matchesVariant3) {
				// perfecto
			} else if (!matchesVariant1 && matchesVariant2 && !matchesVariant3) {
				// perfecto
			} else if (!matchesVariant1 && !matchesVariant2 && matchesVariant3) {
				// perfecto
			} else {
				fail("failed with input "+input);
			}
		}
	}
	
	public void testWronglyFormatted() throws Exception {
		runTest(
				createList("2010-1", "2010-2", "2010-4"), 
				createList("2010-1", "2010-02", "2010-2", "2010-03", "2010-04", "2010-4"));
	}

	public void testGapsA() throws Exception {
		runTest(
				createList("2010-01", "2010-02", "2010-04"), 
				createList("2010-01", "2010-02", "2010-03", "2010-04"));
	}

	public void testGapsB() throws Exception {
		runTest(
				createList("2010-01", "2010-02", "2010-04", "2010-07"), 
				createList("2010-01", "2010-02", "2010-03", "2010-04", "2010-05", "2010-06", "2010-07"));
	}

	public void testBigGap() throws Exception {
		runTest(
				createList("2009-50", "2010-12"), 
				createList("2009-50", "2009-51", "2009-52", "2009-53", "2010-01", "2010-02", "2010-03", "2010-04", "2010-05", "2010-06", "2010-07", "2010-08", "2010-09", "2010-10", "2010-11", "2010-12"));
	}

	public void testWrongInputParams() throws Exception {
		List<String> resultingTestset = manager_.fillGapsInColumnHeaders(createList("2010-50", "2010-12"));
		assertNull(resultingTestset);
	}

	private void assertListEquals(List<String> testset, List<String> resultingTestset) {
		if (testset==null || resultingTestset==null) {
			throw new IllegalArgumentException("testset and resultingtestset must not be empty");
		}
		assertEquals("size mismatch", testset.size(), resultingTestset.size());
		for(int i=0; i<testset.size(); i++) {
			String expectedStr = testset.get(i);
			String actualStr = resultingTestset.get(i);
			assertEquals("string at position "+i+" mismatch", expectedStr, actualStr);
		}
	}

	private boolean matches(List<String> input, List<String> output) {
		if (input.size()!=output.size()) {
			return false;
		}
		for(int i=0; i<input.size(); i++) {
			String expectedStr = input.get(i);
			String actualStr = output.get(i);
			if (!expectedStr.equals(actualStr)) {
				return false;
			}
		}
		return true;
	}
	
	private List<String> createList(String... strings) {
		if (strings==null || strings.length==0) {
			throw new IllegalArgumentException("strings must not be empty");
		}
		
		List<String> result = new ArrayList<String>(strings.length);
		for (int i = 0; i < strings.length; i++) {
			String aStr = strings[i];
			result.add(aStr);
		}
		return result;
	}
}
