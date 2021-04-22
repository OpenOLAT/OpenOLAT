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
package org.olat.ims.qti21.model.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.Selection;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 14 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QtiMaxScoreEstimator {
	
	public static boolean sameMaxScore(AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest) {
		boolean same = true;
		Double sectionMaxScore = null;
		
		List<SectionPart> sectionParts = section.getSectionParts();
		for(SectionPart sectionPart:sectionParts) {
			if(sectionPart instanceof AssessmentSection) {
				MaxScoreVisitor visitor = new MaxScoreVisitor();
				estimateMaxScore(sectionPart, resolvedAssessmentTest, visitor);
				Double sectionScore = visitor.get();
				if(sectionMaxScore == null) {
					sectionMaxScore = sectionScore;
				} else if(sectionScore == null || !sectionMaxScore.equals(sectionScore)) {
					same = false;
				}
			} else if(sectionPart instanceof AssessmentItemRef) {
				AssessmentItemRef itemRef = (AssessmentItemRef)sectionPart;
				ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				if(resolvedAssessmentItem != null) {
					AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
					if(assessmentItem != null) {
						Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
						if(sectionMaxScore == null) {
							sectionMaxScore = maxScore;
						} else if(maxScore == null || !sectionMaxScore.equals(maxScore)) {
							same = false;
						}
					}
				}
			}
		}

		return same;
	}
	
	public static Double estimateMaxScore(ResolvedAssessmentTest resolvedAssessmentTest) {
		AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
		
		MaxScoreVisitor visitor = new MaxScoreVisitor();
		for (final TestPart testPart:assessmentTest.getTestParts()) {
			 for (final AssessmentSection section:testPart.getAssessmentSections()) {
				 doAssessmentSectionEstimateMaxScore(section, resolvedAssessmentTest, visitor);
			 } 
		}
		return visitor.get();
	}
	
	public static Double estimateMaxScore(TestPart testPart, ResolvedAssessmentTest resolvedAssessmentTest) {
		MaxScoreVisitor visitor = new MaxScoreVisitor();
		for(final AssessmentSection section:testPart.getAssessmentSections()) {
			doAssessmentSectionEstimateMaxScore(section, resolvedAssessmentTest, visitor);
		} 
		return visitor.get();
	}
	
	public static Double estimateMaxScore(AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest) {
		MaxScoreVisitor visitor = new MaxScoreVisitor();
		doAssessmentSectionEstimateMaxScore(section, resolvedAssessmentTest, visitor);
		return visitor.get();
	}

	private static void estimateMaxScore(SectionPart sectionPart, ResolvedAssessmentTest resolvedAssessmentTest, MaxScoreVisitor visitor) {
		if(sectionPart instanceof AssessmentSection) {
			doAssessmentSectionEstimateMaxScore((AssessmentSection)sectionPart, resolvedAssessmentTest, visitor);
		} else if(sectionPart instanceof AssessmentItemRef) {
			AssessmentItemRef itemRef = (AssessmentItemRef)sectionPart;
			ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
			if(resolvedAssessmentItem != null) {
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				if(assessmentItem != null) {
					Double maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
					visitor.add(maxScore);
				}
			}
		}
	}

	private static void doAssessmentSectionEstimateMaxScore(AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest, MaxScoreVisitor visitor) {
		Selection selection = section.getSelection();
		if(selection != null) {
			selectSectionParts(section, resolvedAssessmentTest, visitor);
		} else {
			List<SectionPart> sectionParts = section.getSectionParts();
			for(SectionPart sectionPart:sectionParts) {
				estimateMaxScore(sectionPart, resolvedAssessmentTest, visitor);
			}
		}
	}
	
	private static void selectSectionParts(AssessmentSection section, ResolvedAssessmentTest resolvedAssessmentTest, MaxScoreVisitor visitor) {
		final List<SectionPart> children = section.getSectionParts();
        final Selection selection = section.getSelection();
        final int childCount = children.size();
        int requestedSelections = selection.getSelect();
        
        if (requestedSelections < 0) {
            requestedSelections = 0;
        }
        if (requestedSelections==0) {
            return;
        }
        
        int requiredChildCount = 0; /* (Number of children marked as required) */

        final List<MaxScoreVisitor> childrenMaxScore = new ArrayList<>();
        for(SectionPart child:children) {
        	MaxScoreVisitor childVisitor = new MaxScoreVisitor();
        	estimateMaxScore(child, resolvedAssessmentTest, childVisitor);
        	childrenMaxScore.add(childVisitor);
        }
        final List<MaxScoreVisitor> immutableChildrenMaxScore = new ArrayList<>(childrenMaxScore);

        /* Note any required selections */
        for (int i=0; i<childCount; i++) {
            if (children.get(i).getRequired()) {
                requiredChildCount++;
                visitor.add(childrenMaxScore.get(i));
            }
        }

        if (requiredChildCount > requestedSelections) {
        	requestedSelections = requiredChildCount;
        }
        
        final int remainingSelections = requestedSelections - requiredChildCount;
        if (remainingSelections > 0) {
            if (selection.getWithReplacement()) {
            	final int maxIndex = maxIndex(immutableChildrenMaxScore);
                for (int i=0; i<remainingSelections; i++) {
                	MaxScoreVisitor max = immutableChildrenMaxScore.get(maxIndex);
                	visitor.add(max);
                }
            }
            else {
            	// don't use twice the required section
            	for (int i=0; i<childCount; i++) {
                    if (children.get(i).getRequired()) {
                        childrenMaxScore.set(i, null);
                    }
                }
            	
                /* Selection without replacement */
                for (int i=0; i<remainingSelections; i++) {
                    int index = maxIndex(childrenMaxScore);
                    if(index >= 0 && index < childrenMaxScore.size()) {
	                    MaxScoreVisitor max = childrenMaxScore.get(index);
	                    childrenMaxScore.set(index, null);
	                    visitor.add(max);
                    }
                }
            }
        }
	}
	
	private static int maxIndex(List<MaxScoreVisitor> visitorList) {
		int maxIndex = -1;
		double maxVal = -1.0d;
		for(int i=0;i<visitorList.size(); i++) {
			MaxScoreVisitor visitor = visitorList.get(i);
			if(visitor != null && visitor.maxScoreTotal != null && maxVal < visitor.maxScoreTotal.doubleValue()) {
				maxVal = visitor.maxScoreTotal.doubleValue();
				maxIndex  = i;
			}
		}
		return maxIndex;
	}
	
	public static class MaxScoreVisitor implements Comparable<MaxScoreVisitor> {
		
		private DoubleAdder maxScoreTotal;
		
		public Double get() {
			return maxScoreTotal == null ? null : maxScoreTotal.doubleValue();
		}
		
		public void add(Double val) {
			if(val == null) return;
			
			if(maxScoreTotal == null) {
				maxScoreTotal = new DoubleAdder();
			} 
			maxScoreTotal.add(val.doubleValue());
		}
		
		public void add(MaxScoreVisitor visitor) {
			if(visitor == null || visitor.maxScoreTotal == null) return;
			add(visitor.maxScoreTotal.doubleValue());
		}

		@Override
		public int compareTo(MaxScoreVisitor o) {
			if(maxScoreTotal == null && o.maxScoreTotal == null) {
				return 0;
			}
			if(maxScoreTotal == null) {
				return -1;
			}
			if(o.maxScoreTotal == null) {
				return 1;
			}
			return Double.compare(maxScoreTotal.doubleValue(), o.maxScoreTotal.doubleValue());
		}
	}

}
