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

package org.olat.course.condition.operators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Manager class for the operators in extended easy mode.
 * 
 * <P>
 * Initial Date:  23.10.2006 <br>
 * @author Lars Eberle (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class OperatorManager {

	private static List<Operator> ops = new ArrayList<>();
	
	static {

		//FIXME:laeb:Springify this!
		
// 		these commented out operators won't work with Shibboleth attributes, but they are there in case 
//		that other attributes become available here, too
		
		ops.add(new EqualsOperator());
		ops.add(new GreaterThanEqualsOperator());
		ops.add(new GreaterThanOperator());
		ops.add(new LowerThanEqualsOperator());
		ops.add(new LowerThanOperator());
		//
		ops.add(new IsInAttributeOperator());
		ops.add(new IsNotInAttributeOperator());
		ops.add(new HasAttributeOperator());
		ops.add(new HasNotAttributeOperator());
		ops.add(new AttributeStartswithOperator());
		ops.add(new AttributeEndswithOperator());
	}
	
	/**
	 * @return The List of registered operators
	 */
	public static List<Operator> getAvailableOperators() {
		return ops;
	}
	
	/**
	 * @param l the locale for translating the operators labels
	 * @return the translated labels for all registered operators
	 */
	public static String[] getAllRegisteredAndAlreadyTranslatedOperatorLabels(Locale l) {
		Translator t = new PackageTranslator(OperatorManager.class.getPackage().getName(), l);
		String[] tmp = new String[ops.size()];
		int i = 0;
		for (Operator o : ops) {
			tmp[i++] = t.translate(o.getLabelKey());
		}
		return tmp;
	}
	
	/**
	 * @return an array of all registered operators (exactly: of their keys)
	 */
	public static String[] getAllRegisteredOperatorKeys() {
		String[] tmp = new String[ops.size()];
		int i = 0;
		for (Operator o : ops) {
			tmp[i++] = o.getOperatorKey();
		}
		return tmp;
	}

	public static String[] getRegisteredOperatorKeys(List<String> operatorKeys) {
		List<String> registeredOperatorKeys = Arrays.asList(getAllRegisteredOperatorKeys());
		String[] tmp = new String[operatorKeys.size()];
		int i = 0;
		for (String operatorKey : operatorKeys) {
			if(registeredOperatorKeys.contains(operatorKey)) {
				tmp[i++] = operatorKey;
			}
		}
		return tmp;
	}

	public static String[] getRegisteredAndAlreadyTranslatedOperatorLabels(Locale locale, String[] operatorKeys) {
		List<String> keys = Arrays.asList(operatorKeys);
		Translator t = new PackageTranslator(OperatorManager.class.getPackage().getName(), locale);
		String[] tmp = new String[operatorKeys.length];
		int i = 0;
		for (Operator o : ops) {
			if(keys.contains(o.getOperatorKey())) {
				tmp[i++] = t.translate(o.getLabelKey());
			}
		}
		return tmp;
	}
	
}
