/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.run.scoring;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 23 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScoreScalingHelper {
	
	private static final Logger log = Tracing.createLoggerFor(ScoreScalingHelper.class);
	
	public static final int DIVISION_SCALE = 10;
	public static final RoundingMode DIVISION_ROUNDING_MODE = RoundingMode.HALF_UP;

	public static boolean isEnabled(UserCourseEnvironment userCourseEnv) {
		return isEnabled(userCourseEnv.getCourseEnvironment());
	}
	
	public static boolean isEnabled(CourseEnvironment courseEnv) {
		return isEnabled(courseEnv.getRunStructure().getRootNode());
	}
	
	public static boolean isEnabled(ICourse course) {
		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		return isEnabled(courseEditorTreeNode.getCourseNode());
	}
	
	public static boolean isEnabled(CourseNode courseNode) {
		if(courseNode.getParent() != null) {
			for(CourseNode node=courseNode; node != null; node = (CourseNode)node.getParent()) {
				if(node.getParent() == null) {
					courseNode = node;
				}
			}
		}
		ModuleConfiguration editorConfig = courseNode.getModuleConfiguration();
		return STCourseNode.CONFIG_SCORE_VALUE_SUM_WEIGHTED.equals(editorConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY));
	}
	
	public static final String getRawScoreScale(CourseNode courseNode) {
		return courseNode.getModuleConfiguration()
				.getStringValue(MSCourseNode.CONFIG_KEY_SCORE_SCALING, MSCourseNode.CONFIG_DEFAULT_SCORE_SCALING);
	}
	
	public static final BigDecimal getScoreScale(CourseNode courseNode) {
		String val = getRawScoreScale(courseNode);
		return getScoreScale(val);
	}

	public static final BigDecimal getScoreScale(String val) {
		BigDecimal scale = null;
		if(StringHelper.containsNonWhitespace(val)) {
			try {
				int indexSlash = val.indexOf('/');
				if(val.indexOf('/') >= 0 && indexSlash + 1 < val.length()) {
					String firstVal = val.substring(0, indexSlash);
					String secondSlash = val.substring(indexSlash + 1);
					if(StringHelper.isLong(firstVal) && StringHelper.isLong(secondSlash)) {
						scale = BigDecimal.valueOf(Long.parseLong(firstVal))
								.divide(BigDecimal.valueOf(Long.parseLong(secondSlash)), DIVISION_SCALE, DIVISION_ROUNDING_MODE);
					}
				} else {
					scale = new BigDecimal(val);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
		if(scale == null) {
			scale = BigDecimal.ONE;
		}
		return scale;
	}
	
	public static final BigDecimal getWeightedScore(BigDecimal score, BigDecimal scale) {
		return score == null ? null : score.multiply(scale);
	}
	
	public static final BigDecimal getWeightedScore(Float score, BigDecimal scale) {
		return score == null ? null : BigDecimal.valueOf(score.doubleValue()).multiply(scale);
	}
	
	public static final Float getWeightedFloatScore(Float score, BigDecimal scale) {
		BigDecimal wScore = getWeightedScore(score, scale);
		return wScore == null ? null : Float.valueOf(wScore.floatValue());
	}
	
	public static final Float getWeightedFloatScore(BigDecimal score, BigDecimal scale) {
		BigDecimal wScore = getWeightedScore(score, scale);
		return wScore == null ? null : Float.valueOf(wScore.floatValue());
	}
	
	public static final Float getWeightedFloatScore(Float score, CourseNode courseNode) {
		BigDecimal scale = getScoreScale(courseNode);
		BigDecimal wScore = getWeightedScore(score, scale);
		return wScore == null ? null : Float.valueOf(wScore.floatValue());
	}
	
	public static final Float getWeightedFloatScore(BigDecimal score, CourseNode courseNode) {
		BigDecimal scale = getScoreScale(courseNode);
		BigDecimal wScore = getWeightedScore(score, scale);
		return wScore == null ? null : Float.valueOf(wScore.floatValue());
	}
	
	public static boolean validateScoreScaling(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(el.isVisible()) {
			if(StringHelper.containsNonWhitespace(el.getValue())) {
				String val = el.getValue();
				int indexSlash = val.indexOf('/');
				if(indexSlash > 0 && indexSlash + 1 < val.length()) {
					String firstVal = val.substring(0, indexSlash);
					String secondSlash = val.substring(indexSlash + 1);
					if(!StringHelper.isLong(firstVal) || !StringHelper.isLong(secondSlash)) {
						el.setErrorKey("form.error.nofraction");
						allOk &= false;
					}
				} else if(!val.matches(MSEditFormController.scoreRex) || Float.parseFloat(val) <= 0.0f) {
					el.setErrorKey("form.error.nofloat");
					allOk &= false;
				}
			} else if(el.isMandatory()) {
				el.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}		
		return allOk;
	}

}
