package org.olat.core.util.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * Initial date: 8 oct. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AlphaNumericalComparatorTest {
	
	/**
	 * This sort was used for the list of QTI internal variables.
	 */
	@Test
	public void sortQtiResponses() {
		List<String> variables = new ArrayList<>();
		variables.add("RESPONSE_1");
		variables.add("RESPONSE_2");
		variables.add("RESPONSE_10");
		variables.add("RESPONSE_3");
		variables.add("RESPONSE_21");
		variables.add("RESPONSE_25");
		variables.add("RESPONSE_30");
		variables.add("completionStatus");
		
		Collections.sort(variables, new AlphaNumericalComparator());
		
		Assert.assertEquals("completionStatus", variables.get(0));
		Assert.assertEquals("RESPONSE_1", variables.get(1));
		Assert.assertEquals("RESPONSE_2", variables.get(2));
		Assert.assertEquals("RESPONSE_3", variables.get(3));
		Assert.assertEquals("RESPONSE_10", variables.get(4));
		Assert.assertEquals("RESPONSE_21", variables.get(5));
		Assert.assertEquals("RESPONSE_25", variables.get(6));
		Assert.assertEquals("RESPONSE_30", variables.get(7));
	}
	
	@Test
	public void sortDazzle() {
		List<String> values = Arrays.asList("dazzle2", "dazzle10", "dazzle1", "dazzle2.7", "dazzle2.10", "2", "10", "1", "EctoMorph6", "EctoMorph62", "EctoMorph7");
    	
		Collections.sort(values, new AlphaNumericalComparator());

		Assert.assertEquals("1", values.get(0));
		Assert.assertEquals("2", values.get(1));
		Assert.assertEquals("10", values.get(2));
		Assert.assertEquals("dazzle1", values.get(3));
		Assert.assertEquals("dazzle2", values.get(4));
		Assert.assertEquals("dazzle2.7", values.get(5));
		Assert.assertEquals("dazzle2.10", values.get(6));
		Assert.assertEquals("dazzle10", values.get(7));
		Assert.assertEquals("EctoMorph6", values.get(8));
		Assert.assertEquals("EctoMorph7", values.get(9));
		Assert.assertEquals("EctoMorph62", values.get(10));
	}
}
