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
package org.olat.course.condition;

import org.olat.core.util.StringHelper;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.group.model.BGAreaReference;
import org.olat.group.model.BusinessGroupReference;

/**
 * 
 * Convert the expert rules:
 * <ul>
 * 	<li>Replace the name of group and area to their primary keys.</li>
 * 	<li>Replace the key of group and area to their new primary keys.</li>
 * 	<li>Replace the key of group and area to their new names.</li>
 * </ul>
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KeyAndNameConverter {
	
	private static final String[] groupMethods = { "inLearningGroup", "inRightGroup", "isLearningGroupFull" };
	private static final String areaMethod = "inLearningArea";
	
	private KeyAndNameConverter() {
		//
	}
	
	/**
	 * isLearningGroupFull, inLearningGroup, inRightGroup, inLearningArea, isLearningGroupFull
	 */
	
	public static String convertExpressionKeyToName(String expression, CourseEnvironmentMapper envMapper) {
		for(String groupMethod:groupMethods) {
			for(BusinessGroupReference group:envMapper.getGroups()) {
				String strToMatch = groupMethod + "(\"" + group.getKey() + "\")";
				String replacement = groupMethod + "(\"" + group.getName() + "\")";
				expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
			}
		}

		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = areaMethod + "(\"" + area.getKey() + "\")";
			String replacement = areaMethod + "(\"" + area.getName() + "\")";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		for(BusinessGroupReference group:envMapper.getGroups()) {
			String strToMatch = "\"" + group.getKey() + "\"";
			String replacement = "\"" + group.getName() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = "\"" + area.getKey() + "\"";
			String replacement = "\"" + area.getName() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		return expression;
	}
	
	public static String convertExpressionNameToKey(String expression, CourseEnvironmentMapper envMapper) {
		for(String groupMethod:groupMethods) {
			for(BusinessGroupReference group:envMapper.getGroups()) {
				String strToMatch = groupMethod + "(\"" + group.getOriginalName() + "\")";
				String replacement = groupMethod + "(\"" + group.getKey() + "\")";
				expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
			}
		}
		
		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = areaMethod + "(\"" + area.getOriginalName() + "\")";
			String replacement = areaMethod + "(\"" + area.getKey() + "\")";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		// fallback for special case where there is blank between the ( and "
		for(BusinessGroupReference group:envMapper.getGroups()) {
			String strToMatch = "\"" + group.getOriginalName() + "\"";
			String replacement = "\"" + group.getKey() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = "\"" + area.getOriginalName() + "\"";
			String replacement = "\"" + area.getKey() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		return expression;
	}
	
	public static String convertExpressionKeyToKey(String expression, CourseEnvironmentMapper envMapper) {
		for(String groupMethod:groupMethods) {
			for(BusinessGroupReference group:envMapper.getGroups()) {
				String strToMatch = groupMethod + "(\"" + group.getOriginalKey() + "\")";
				String replacement = groupMethod + "(\"" + group.getKey() + "\")";
				expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
			}
		}
		
		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = areaMethod + "(\"" + area.getOriginalKey() + "\")";
			String replacement = areaMethod + "(\"" + area.getKey() + "\")";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		for(BusinessGroupReference group:envMapper.getGroups()) {
			String strToMatch = "\"" + group.getOriginalKey() + "\"";
			String replacement = "\"" + group.getKey() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		for(BGAreaReference area:envMapper.getAreas()) {
			String strToMatch = "\"" + area.getOriginalKey() + "\"";
			String replacement = "\"" + area.getKey() + "\"";
			expression = StringHelper.replaceAllCaseInsensitive(expression, strToMatch, replacement);
		}
		
		return expression;
	}
	
	public static String replaceIdsInCondition(String condition, CourseEnvironmentMapper courseEnvMapper) {
		if (condition != null) {
			condition = KeyAndNameConverter.convertExpressionKeyToKey(condition, courseEnvMapper);

			for (String nodeSourceId : courseEnvMapper.getNodeSourceIds()) {
				condition = condition.replaceAll(
						"\"" + nodeSourceId + "\"",
						"\"" + courseEnvMapper.getNodeTargetIdent(nodeSourceId) + "\"");
			}
		}
		return condition;
	}

}
