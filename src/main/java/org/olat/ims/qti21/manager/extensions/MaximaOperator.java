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
package org.olat.ims.qti21.manager.extensions;

import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.manager.OpenOLATExtensionPackage;

import uk.ac.ed.ph.jacomax.MaximaTimeoutException;
import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.attribute.AttributeList;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.template.processing.ProcessTemplateValue;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableType;
import uk.ac.ed.ph.jqtiplus.running.ProcessingContext;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.ValidationContext;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.qtiworks.mathassess.GlueValueBinder;
import uk.ac.ed.ph.qtiworks.mathassess.glue.MathAssessBadCasCodeException;
import uk.ac.ed.ph.qtiworks.mathassess.glue.MathsContentTooComplexException;
import uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaProcess;
import uk.ac.ed.ph.qtiworks.mathassess.glue.maxima.QtiMaximaTypeConversionException;
import uk.ac.ed.ph.qtiworks.mathassess.glue.types.ValueWrapper;
import uk.ac.ed.ph.qtiworks.mathassess.value.ReturnTypeType;

/**
 * 
 * Initial date: 5 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MaximaOperator extends CustomOperator<OpenOLATExtensionPackage>  {
	
	private final static OLog log = Tracing.createLoggerFor(MaximaOperator.class);

	private static final long serialVersionUID = -5085928825187250511L;

	public MaximaOperator(final ExpressionParent parent) {
		super(parent);
	}

	@Override
	protected Value evaluateSelf(OpenOLATExtensionPackage jqtiExtensionPackage, ProcessingContext context, Value[] childValues, int depth) {
        AttributeList attributes = getAttributes();
        Attribute<?> attrValue = attributes.get("value");
        String code = (String)attrValue.getValue();

        log.debug("Performing scriptRule: code={}, simplify={}" + code);

        /* Pass variables to Maxima */
        final QtiMaximaProcess qtiMaximaProcess = jqtiExtensionPackage.obtainMaximaSessionForThread();
        for(int i=0; i<childValues.length; i++) {
        		Value childValue = childValues[i];
        		String val = getValue(childValue);
        		code = code.replace("$(" + (i+1) + ")", val);
        }
        
        /* Run code */
        log.debug("Executing scriptRule code");
        try {
            qtiMaximaProcess.executeScriptRule(code, true);
        } catch (final MaximaTimeoutException e) {
            context.fireRuntimeError(this, "A timeout occurred executing the ScriptRule logic. Not setting QTI variables and returing FALSE");
            return BooleanValue.FALSE;
        } catch (final RuntimeException e) {
            context.fireRuntimeError(this, "An unexpected problem occurred while trying to run the scriptRule logic. Not setting QTI variables and returing FALSE");
            return BooleanValue.FALSE;
        }

        /* Read variables back */
        log.debug("Reading variables back from Maxima");

        /* Run Maxima code and extract result */
        log.info("Running code to determine result of MAXIMA operator");
        
        final Class<? extends ValueWrapper> resultClass = GlueValueBinder.getCasReturnClass(ReturnTypeType.FLOAT);
        ValueWrapper maximaResult;
        try {
            maximaResult = qtiMaximaProcess.executeCasProcess(code, true, resultClass);
        } catch (final MaximaTimeoutException e) {
            context.fireRuntimeError(this, "A timeout occurred executing the CasCondition logic. Returning NULL");
            return NullValue.INSTANCE;
        } catch (final MathsContentTooComplexException e) {
            context.fireRuntimeError(this, "An unexpected problem occurred querying the result of CasProcess, so returning NULL");
            return NullValue.INSTANCE;
        } catch (final MathAssessBadCasCodeException e) {
            context.fireRuntimeError(this, "Your CasProcess code did not work as expected. The CAS input was '"
                    + e.getMaximaInput()
                    + "' and the CAS output was '"
                    + e.getMaximaOutput()
                    + "'. The failure reason was: " + e.getReason());
            return NullValue.INSTANCE;
        } catch (final QtiMaximaTypeConversionException e) {
            context.fireRuntimeError(this, "Your CasProcess code did not produce a result that could be converted into the required QTI type. The CAS input was '"
                    + e.getMaximaInput()
                    + "' and the CAS output was '"
                    + e.getMaximaOutput()
                    + "'");
            return NullValue.INSTANCE;
        } catch (final RuntimeException e) {
            log.warn("Unexpected Maxima failure", e);
            context.fireRuntimeError(this, "An unexpected problem occurred while executing this CasProcess");
            return BooleanValue.FALSE;
        }
        /* Bind result */
        Value result = GlueValueBinder.casToJqti(maximaResult);
        if (result==null) {
            context.fireRuntimeError(this, "Failed to convert result from Maxima back to a QTI variable - returning NULL");
            return NullValue.INSTANCE;
        }
        
        result = adjustTemplateValue(result, context);
        return result;
	}
	
	private Value adjustTemplateValue(Value value, ProcessingContext context) {
		ExpressionParent parent = getParent();
		if(parent instanceof ProcessTemplateValue) {
			ProcessTemplateValue processTemplateValue = (ProcessTemplateValue)parent;
			Identifier valueIdentifier = processTemplateValue.getIdentifier();
			VariableDeclaration declaration = context.ensureVariableDeclaration(valueIdentifier, VariableType.TEMPLATE);
			if(declaration != null && declaration.getBaseType() != value.getBaseType()) {
				value = adjustValueBaseType(value, declaration.getBaseType());
			}
		}
		return value;
	}
	
	private Value adjustValueBaseType(Value value, BaseType desiredType) {
		if(value.getBaseType() == BaseType.INTEGER && desiredType == BaseType.FLOAT) {
			IntegerValue integerValue = (IntegerValue)value;
			return new FloatValue(integerValue.doubleValue());
		} else if(value.getBaseType() == BaseType.FLOAT && desiredType == BaseType.INTEGER) {
			FloatValue floatValue = (FloatValue)value;
			int intValue = (int)Math.round(floatValue.doubleValue());
			return new IntegerValue(intValue);
		} else {
			log.error("Cannot convert " + value.getBaseType() + " to " + desiredType);
		}
		return value;
	}
	
	private String getValue(Value value) {
		String val = null;
		if(value.getBaseType() == BaseType.FLOAT) {
			val = Double.toString(((FloatValue)value).doubleValue());
		} else if(value.getBaseType() == BaseType.INTEGER) {
			val = Integer.toString(((IntegerValue)value).intValue());
		}
		return val;
	}

	@Override
	public Cardinality[] getProducedCardinalities(ValidationContext context) {
		return new Cardinality[] { Cardinality.SINGLE };
	}

	@Override
	public BaseType[] getProducedBaseTypes(ValidationContext context) {
		ExpressionParent parent = getParent();
		if(parent instanceof ProcessTemplateValue) {
			ProcessTemplateValue processTemplateValue = (ProcessTemplateValue)parent;
			Identifier valueIdentifier = processTemplateValue.getIdentifier();
			VariableDeclaration declaration = context.isValidLocalVariableReference(valueIdentifier);
			if(declaration != null && (declaration.getBaseType() == BaseType.INTEGER || declaration.getBaseType() == BaseType.FLOAT)) {
				return new BaseType[] { declaration.getBaseType() };
			}	
		}
		return new BaseType[] { BaseType.FLOAT, BaseType.INTEGER };
	}
}
