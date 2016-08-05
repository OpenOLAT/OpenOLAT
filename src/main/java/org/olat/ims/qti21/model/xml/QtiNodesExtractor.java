package org.olat.ims.qti21.model.xml;

import static org.olat.ims.qti21.QTI21Constants.MAXSCORE_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.MINSCORE_IDENTIFIER;

import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.BaseTypeAndCardinality;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * This is an helper class which extract data from the java model of QtiWorks.
 * 
 * Initial date: 05.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QtiNodesExtractor {
	
	public static Double extractMaxScore(AssessmentItem assessmentItem) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentItem.getOutcomeDeclaration(MAXSCORE_IDENTIFIER));
	}
	
	public static Double extractMaxScore(AssessmentTest assessmentTest) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentTest.getOutcomeDeclaration(MAXSCORE_IDENTIFIER));
	}
	
	public static Double extractMinScore(AssessmentTest assessmentTest) {
		return getOutcomeDeclarationDefaultFloatValue(assessmentTest.getOutcomeDeclaration(MINSCORE_IDENTIFIER));
	}
	
	public static Double getOutcomeDeclarationDefaultFloatValue(OutcomeDeclaration outcomeDeclaration) {
		Double doubleValue = null;
		if(outcomeDeclaration != null) {
			DefaultValue defaultValue = outcomeDeclaration.getDefaultValue();
			if(defaultValue != null) {
				Value evaluatedValue = defaultValue.evaluate();
				if(evaluatedValue instanceof FloatValue) {
					doubleValue = new Double(((FloatValue)evaluatedValue).doubleValue());
				}
			}
		}
		return doubleValue;
	}
	
	public static void extractIdentifiersFromCorrectResponse(CorrectResponse correctResponse, List<Identifier> correctAnswers) {
		BaseTypeAndCardinality responseDeclaration = correctResponse.getParent();
		if(responseDeclaration.hasCardinality(Cardinality.MULTIPLE)) {
			Value value = FieldValue.computeValue(Cardinality.MULTIPLE, correctResponse.getFieldValues());
			if(value instanceof MultipleValue) {
				MultipleValue multiValue = (MultipleValue)value;
				for(SingleValue sValue:multiValue.getAll()) {
					if(sValue instanceof IdentifierValue) {
						IdentifierValue identifierValue = (IdentifierValue)sValue;
						Identifier correctAnswer = identifierValue.identifierValue();
						correctAnswers.add(correctAnswer);
					}
				}
			}
		} else if(responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			Value value = FieldValue.computeValue(Cardinality.SINGLE, correctResponse.getFieldValues());
			if(value instanceof IdentifierValue) {
				IdentifierValue identifierValue = (IdentifierValue)value;
				correctAnswers.add(identifierValue.identifierValue());
			}
		}
	}

}
