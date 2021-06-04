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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 22.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticsTest {
	
	@Test
	public void mean() {
		double[] data1 = new double[]{ 1.0, 3.0, 5.0, 8.0 };
		double mean1 = new Statistics(data1).getMean();
		Assert.assertEquals(4.25, mean1, 0.001);
		
		//test empty array
		double[] data2 = new double[0];
		double mean2 = new Statistics(data2).getMean();
		Assert.assertTrue(Double.isNaN(mean2));
		
		//test min array
		double[] data3 = new double[]{ 1.5 };
		double mean3 = new Statistics(data3).getMean();
		Assert.assertEquals(1.5, mean3, 0.001);
	}
	
	@Test
	public void variance() {
		//test empty array
		double[] data2 = new double[0];
		double variance2 = new Statistics(data2).getVariance();
		Assert.assertTrue(Double.isNaN(variance2));
	}
	
	@Test
	public void stdDev() {
		//test empty array
		double[] data2 = new double[0];
		double variance2 = new Statistics(data2).getStdDev();
		Assert.assertTrue(Double.isNaN(variance2));
	}
	
	@Test
	public void median() {
		double[] data1 = new double[]{ 1.0, 3.0, 5.0, 8.0 };
		double median1 = new Statistics(data1).median();
		Assert.assertEquals(4.0, median1, 0.001);
		
		//test empty array
		double[] data2 = new double[0];
		double median2 = new Statistics(data2).median();
		Assert.assertEquals(-1.0, median2, 0.001);
		
		//test min array
		double[] data3 = new double[]{ 1.5 };
		double median3 = new Statistics(data3).median();
		Assert.assertEquals(1.5, median3, 0.001);
	}
	
	@Test
	public void mode() {
		double[] data1 = new double[]{ 1.0, 3.0, 5.0, 8.0 };
		List<Double> mode1 = new Statistics(data1).mode();
		Assert.assertNotNull(mode1);
		
		//test empty array
		double[] data2 = new double[0];
		List<Double> mode2 = new Statistics(data2).mode();
		Assert.assertNotNull(mode2);
		
		//test min array
		double[] data3 = new double[]{ 1.5 };
		List<Double> mode3 = new Statistics(data3).mode();
		Assert.assertNotNull(mode3);
	}
	
	

}
