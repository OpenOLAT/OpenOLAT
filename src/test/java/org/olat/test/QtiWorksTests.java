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
package org.olat.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * A test suite which run the tests of qtiworks (jqtiplus, the math assess glue
 * a.k.a Maxima, the samples which contains some integration tests)
 * 
 * Initial date: 29.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	//qtiworks-jqtiplus
	uk.ac.ed.ph.jqtiplus.node.content.ContentTest.class,
	uk.ac.ed.ph.jqtiplus.node.item.MapResponsePointTest.class,
	uk.ac.ed.ph.jqtiplus.node.item.MapResponseTest.class,
	uk.ac.ed.ph.jqtiplus.node.item.TemplateTest.class,
	uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteractionTest.class,
	uk.ac.ed.ph.jqtiplus.reading.QtiXmlReaderTest.class,
	uk.ac.ed.ph.jqtiplus.reading.QtiObjectReaderTest.class,
	uk.ac.ed.ph.jqtiplus.running.ChoiceItemBadStateTest.class,
	uk.ac.ed.ph.jqtiplus.running.ChoiceItemRunningTest.class,
	uk.ac.ed.ph.jqtiplus.running.ItemProcessorControllerTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestBranchRuleNavigationExitTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestBranchRuleNavigationTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestComplexLinearNavigationTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestIncompleteExitTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestLinearSimultaneousTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestNonlinearIndividualTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestNonlinearNavigationTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestNonlinearSimultaneousTest.class,
	uk.ac.ed.ph.jqtiplus.running.TestProcessorControllerTest.class,
	uk.ac.ed.ph.jqtiplus.serialization.QtiSerializerTest.class,
	uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifierAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifierRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.types.IdentifierAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.types.IdentifierRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.BaseTypeAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.BaseTypeRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.BooleanValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.BooleanValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.BooleanValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.CardinalityAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.CardinalityRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.DirectedPairValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.DirectedPairValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.DirectedPairValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.DurationValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.DurationValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.DurationValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.FileValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.FloatValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.FloatValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.FloatValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.IdentifierValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.IntegerValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.IntegerValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.IntegerValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.MultipleValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.NullValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.OrderedValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.PairValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.PairValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.PairValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.PointValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.PointValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.PointValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.RecordValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.StringValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.StringValueTest.class,
	uk.ac.ed.ph.jqtiplus.value.UriValueAcceptTest.class,
	uk.ac.ed.ph.jqtiplus.value.UriValueRefuseTest.class,
	uk.ac.ed.ph.jqtiplus.value.UriValueTest.class,
	uk.ac.ed.ph.jqtiplus.xmlutils.CustomUriSchemeTest.class,
	
	//qtiworks mathassess glue
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.CircularMaximaUpConversionTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.MaximaDataBinderBadParsingTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.MaximaDataBinderFromMaximaAndBackTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.MaximaDataBinderTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.MaximaDataBinderToMaximaAndBackTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaSessionExecuteStringCircularTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaSessionPassVariableTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaSessionQueryVariableTests.class,
	uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaSessionTest.class,
	
	//qtiworks samples (some integration tests)
	uk.ac.ed.ph.qtiworks.test.integration.QtiXmlReaderSampleTests.class,
	uk.ac.ed.ph.qtiworks.test.integration.SerializationSampleTests.class,
	uk.ac.ed.ph.qtiworks.test.integration.TemplateProcessingSampleTests.class,
	uk.ac.ed.ph.qtiworks.test.integration.ValidationSampleTests.class
	
})
public class QtiWorksTests {
	//
}
