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

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.manager.OpenOLATExtensionPackage;

import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.template.processing.ProcessTemplateValue;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableDeclaration;
import uk.ac.ed.ph.jqtiplus.running.ProcessingContext;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.ValidationContext;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 5 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MaximaOperator extends CustomOperator<OpenOLATExtensionPackage>  {
	
	private static final Logger log = Tracing.createLoggerFor(MaximaOperator.class);

	private static final long serialVersionUID = -5085928825187250511L;

	public MaximaOperator(final ExpressionParent parent) {
		super(parent);
	}

	@Override
	protected Value evaluateSelf(OpenOLATExtensionPackage jqtiExtensionPackage, ProcessingContext context, Value[] childValues, int depth) {
        log.warn("Maxima is not supported");
        return NullValue.INSTANCE;
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
