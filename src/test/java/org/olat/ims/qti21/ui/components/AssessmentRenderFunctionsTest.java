package org.olat.ims.qti21.ui.components;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.OrderedValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 5 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentRenderFunctionsTest {

	@Test
	public void valueContains_nullValue() {
		NullValue nullVal = NullValue.INSTANCE;
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(null, Identifier.parseString("noid")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(nullVal, Identifier.parseString("noid")));
	}
	
	@Test
	public void valueContains_identifierValue() {
		IdentifierValue identifierVal = new IdentifierValue(Identifier.parseString("reference-id"));
		Assert.assertTrue(AssessmentRenderFunctions.valueContains(identifierVal, Identifier.parseString("reference-id")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierVal, Identifier.parseString("noid")));
	}
	
	@Test
	public void valueContains_multipleValues() {
		Value identifierValues = MultipleValue.createMultipleValue(
				new IdentifierValue(Identifier.parseString("reference-id")),
				new IdentifierValue(Identifier.parseString("reference-id")));
		
		Assert.assertTrue(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("reference-id")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("noid")));
	}
	
	@Test
	public void valueContains_multipleAlienValues() {
		Value identifierValues = MultipleValue.createMultipleValue(
				new IntegerValue(6),
				new IntegerValue(7));
		
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("6")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("noid")));
	}
	
	@Test
	public void valueContains_orderedValues() {
		Value identifierValues = OrderedValue.createOrderedValue(
				new IdentifierValue(Identifier.parseString("reference-id")),
				new IdentifierValue(Identifier.parseString("reference-id")));
		
		Assert.assertTrue(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("reference-id")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("noid")));
	}
	
	@Test
	public void valueContains_orderedAlienValues() {
		Value identifierValues = OrderedValue.createOrderedValue(
				new IntegerValue(6),
				new IntegerValue(7));
		
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("7")));
		Assert.assertFalse(AssessmentRenderFunctions.valueContains(identifierValues, Identifier.parseString("noid")));
	}
}
