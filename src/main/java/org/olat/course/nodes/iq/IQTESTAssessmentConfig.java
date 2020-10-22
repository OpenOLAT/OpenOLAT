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
package org.olat.course.nodes.iq;

import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21DeliveryOptions.PassedType;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 19 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTAssessmentConfig implements AssessmentConfig {

	private final IQTESTCourseNode courseNode;

	public IQTESTAssessmentConfig(IQTESTCourseNode courseNode) {
		this.courseNode = courseNode;
	}

	@Override
	public boolean isAssessable() {
		return true;
	}

	@Override
	public boolean ignoreInCourseAssessment() {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		return config.getBooleanSafe(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT);
	}

	@Override
	public void setIgnoreInCourseAssessment(boolean ignoreInCourseAssessment) {
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		config.setBooleanEntry(IQEditController.CONFIG_KEY_IGNORE_IN_COURSE_ASSESSMENT, ignoreInCourseAssessment);
	}

	@Override
	public Mode getScoreMode() {
		return Mode.setByNode;
	}
	
	@Override
	public Float getMaxScore() {
		Float maxScore = null;

		ModuleConfiguration config = courseNode.getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			maxScore = (Float) config.get(IQEditController.CONFIG_KEY_MAXSCORE);
		} else {
			RepositoryEntry testEntry = courseNode.getCachedReferencedRepositoryEntry();
			if (testEntry != null) {
				if(QTIResourceTypeModule.isQtiWorks(testEntry.getOlatResource())) {
					ResolvedAssessmentTest resolvedAssessmentTest = courseNode.loadResolvedAssessmentTest(testEntry);
					if(resolvedAssessmentTest != null) {
						Double max = QtiMaxScoreEstimator.estimateMaxScore(resolvedAssessmentTest);
						if(max == null) {
							AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractAssumingSuccessful();
							if(assessmentTest != null) {
								max = QtiNodesExtractor.extractMaxScore(assessmentTest);
							}
						}
						if(max != null) {
							maxScore = Float.valueOf(max.floatValue());
						}		
					}
				} else {
					maxScore = (Float) config.get(IQEditController.CONFIG_KEY_MAXSCORE);
				}
			}
		}
		
		return maxScore;
	}


	@Override
	public Float getMinScore() {
		Float minScore = null;
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			minScore = (Float) config.get(IQEditController.CONFIG_KEY_MINSCORE);
		} else {
			RepositoryEntry testEntry = courseNode.getCachedReferencedRepositoryEntry();
			if (testEntry != null) {
				if(QTIResourceTypeModule.isQtiWorks(testEntry.getOlatResource())) {
					AssessmentTest assessmentTest = courseNode.loadAssessmentTest(testEntry);
					if(assessmentTest != null) {
						Double min = QtiNodesExtractor.extractMinScore(assessmentTest);
						if(min != null) {
							minScore = Float.valueOf(min.floatValue());
						}
					}
				} else {
					minScore = (Float) config.get(IQEditController.CONFIG_KEY_MINSCORE);
				}
			}
		}
		return minScore;
	}
	
	@Override
	public Mode getPassedMode() {
		Mode mode = Mode.none;
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			mode = Mode.setByNode;
		} else {
			RepositoryEntry testEntry = courseNode.getCachedReferencedRepositoryEntry();
			if (testEntry != null) {
				if(QTIResourceTypeModule.isQtiWorks(testEntry.getOlatResource())) {
					AssessmentTest assessmentTest = courseNode.loadAssessmentTest(testEntry);
					if(assessmentTest != null) {
						QTI21Service qti21Service = CoreSpringFactory.getImpl(QTI21Service.class);
						QTI21DeliveryOptions deliveryOptions = qti21Service.getDeliveryOptions(testEntry);
						if (deliveryOptions != null) {
							Double cutValue = QtiNodesExtractor.extractCutValue(assessmentTest);
							PassedType passedType = deliveryOptions.getPassedType(cutValue);
							if (passedType == PassedType.cutValue || passedType == PassedType.manually) {
								mode = Mode.setByNode;
							}
						}
					}
				} else {
					mode = Mode.setByNode;
				}
			}
		}
		return mode;
	}

	@Override
	public Float getCutValue() {
		Float cutValue = null;
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		// for onyx and QTI 1.2
		if (IQEditController.CONFIG_VALUE_QTI2.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))
				|| IQEditController.CONFIG_VALUE_QTI1.equals(config.get(IQEditController.CONFIG_KEY_TYPE_QTI))) {
			cutValue = (Float) config.get(IQEditController.CONFIG_KEY_CUTVALUE);
		} else {
			RepositoryEntry testEntry = courseNode.getCachedReferencedRepositoryEntry();
			if (testEntry != null) {
				if(QTIResourceTypeModule.isQtiWorks(testEntry.getOlatResource())) {
					AssessmentTest assessmentTest = courseNode.loadAssessmentTest(testEntry);
					if(assessmentTest != null) {
						Double cut = QtiNodesExtractor.extractCutValue(assessmentTest);
						if(cut != null) {
							cutValue = Float.valueOf(cut.floatValue());
						}
					}
				} else {
					cutValue = (Float) config.get(IQEditController.CONFIG_KEY_CUTVALUE);
				}
			}
		}
		return cutValue;
	}

	@Override
	public boolean isPassedOverridable() {
		return false;
	}
	
	@Override
	public Mode getCompletionMode() {
		return IQEditController.CONFIG_VALUE_QTI21.equals(courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_TYPE_QTI))
				? Mode.setByNode
				: Mode.none;
	}

	@Override
	public boolean hasAttempts() {
		return true;
	}
	
	@Override
	public boolean hasComment() {
		// coach should be able to add comments here, visible to users
		return true;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return true;// like user comment
	}

	@Override
	public boolean hasStatus() {
		return false;
	}

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public boolean isEditable() {
		// test scoring fields can be edited manually
		return true;
	}

	@Override
	public boolean isBulkEditable() {
		return false;
	}

	@Override
	public boolean hasEditableDetails() {
		return true;
	}

	@Override
	public boolean isExternalGrading() {
		return IQEditController.CORRECTION_GRADING.equals(courseNode.getModuleConfiguration()
				.getStringValue(IQEditController.CONFIG_CORRECTION_MODE, IQEditController.CORRECTION_AUTO))
				&& StringHelper.containsNonWhitespace((String)courseNode.getModuleConfiguration().get(IQEditController.CONFIG_KEY_REPOSITORY_SOFTKEY));
	}

	@Override
	public boolean isObligationOverridable() {
		return true;
	}

}
