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
package org.olat.ims.qti21.model.statistics;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NumericalInputInteractionStatistics extends AbstractTextEntryInteractionStatistics {
	
	private static final Logger log = Tracing.createLoggerFor(NumericalInputInteractionStatistics.class);
	
	private Double correctFloatResponse;
	private ToleranceMode toleranceMode;
	private Double lowerTolerance;
	private Double upperTolerance;
	
	public NumericalInputInteractionStatistics(Identifier responseIdentifier,
			String correctResponse, Double correctFloatResponse, ToleranceMode toleranceMode, Double lowerTolerance,
			Double upperTolerance, Double points) {
		super(responseIdentifier, correctResponse, points);
		this.correctFloatResponse = correctFloatResponse;
		this.toleranceMode = toleranceMode;
		this.lowerTolerance = lowerTolerance;
		this.upperTolerance = upperTolerance;
	}

	public ToleranceMode getToleranceMode() {
		return toleranceMode;
	}

	public Double getLowerTolerance() {
		return lowerTolerance;
	}

	public Double getUpperTolerance() {
		return upperTolerance;
	}

	@Override
	public boolean matchResponse(String value) {
		if(correctFloatResponse == null) return false;
		
		try {
			double answer = Double.parseDouble(value);
			return match(answer);
		} catch (NumberFormatException  e) {
			if(value.indexOf(',') >= 0) {//allow , instead of .
                try {
					double answer = Double.parseDouble(value.replace(',', '.'));
					return match(answer);
				} catch (final NumberFormatException e1) {
					//format can happen
				} catch (Exception e2) {
					log.error("", e2);
				}
        	}
			return false;
		} catch (Exception  e) {
			log.error("", e);
			return false;
		}
	}
	
	private boolean match(double answer) {
		double lTolerance = lowerTolerance == null ? 0.0d : lowerTolerance.doubleValue();
		double uTolerance = upperTolerance == null ? 0.0d : upperTolerance.doubleValue();
		return toleranceMode.isEqual(correctFloatResponse.doubleValue(), answer, lTolerance, uTolerance, true, true);
	}
}
