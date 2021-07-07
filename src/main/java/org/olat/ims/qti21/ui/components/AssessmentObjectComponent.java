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
package org.olat.ims.qti21.ui.components;

import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.valueContains;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.context.Context;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.render.velocity.VelocityComponent;
import org.olat.core.helpers.Settings;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackElement;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.VisibilityMode;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentObjectComponent extends AbstractComponent implements VelocityComponent {
	
	private Context context;
	
	private final boolean mathAssess;
	private boolean hideFeedbacks = false;
	private boolean correctionHelp = false;
	private boolean correctionSolution = false;
	private boolean maxScoreAssessmentItem = false;
	
	private String mapperUri;
	private String submissionMapperUri;
	private URI assessmentObjectUri;
	private ResourceLocator resourceLocator;
	private CandidateSessionContext candidateSessionContext;
	
	public AssessmentObjectComponent(String name) {
		super(name);
		mathAssess = CoreSpringFactory.getImpl(QTI21Module.class).isMathAssessExtensionEnabled();
	}
	
	public abstract AssessmentObjectFormItem getQtiItem();
	
	public String getMapperUri() {
		return mapperUri;
	}

	public void setMapperUri(String mapperUri) {
		this.mapperUri = mapperUri;
	}
	
	/**
	 * Allow to define a specific mapper uri for the uploaded files.
	 * 
	 * @return The specific submission mapper uri or the standard one if it was not defined
	 */
	public String getSubmissionMapperUri() {
		return submissionMapperUri == null ? mapperUri : submissionMapperUri;
	}
	
	public void setSubmissionMapperUri(String submissionMapperUri) {
		this.submissionMapperUri = submissionMapperUri;
	}

	public URI getAssessmentObjectUri() {
		return assessmentObjectUri;
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		this.assessmentObjectUri = assessmentObjectUri;
	}
	
	public boolean isHideFeedbacks() {
		return hideFeedbacks;
	}

	public void setHideFeedbacks(boolean hideFeedbacks) {
		this.hideFeedbacks = hideFeedbacks;
	}

	public boolean isMaxScoreAssessmentItem() {
		return maxScoreAssessmentItem;
	}

	public void setMaxScoreAssessmentItem(boolean maxScoreAssessmentItem) {
		this.maxScoreAssessmentItem = maxScoreAssessmentItem;
	}

	public boolean isCorrectionHelp() {
		return correctionHelp;
	}

	public void setCorrectionHelp(boolean correctionHelp) {
		this.correctionHelp = correctionHelp;
	}
	
	public boolean isCorrectionSolution() {
		return correctionSolution;
	}

	public void setCorrectionSolution(boolean correctionSolution) {
		this.correctionSolution = correctionSolution;
	}

	public abstract String getResponseUniqueIdentifier(ItemSessionState itemSessionState, Interaction interaction);

	public abstract Interaction getInteractionOfResponseUniqueIdentifier(String responseUniqueId);
	
	public abstract String relativePathTo(ResolvedAssessmentItem resolvedAssessmentItem);
	
	public ResourceLocator getResourceLocator() {
		return resourceLocator;
	}

	public void setResourceLocator(ResourceLocator resourceLocator) {
		this.resourceLocator = resourceLocator;
	}
	
	public CandidateSessionContext getCandidateSessionContext() {
		return candidateSessionContext;
	}

	public void setCandidateSessionContext(CandidateSessionContext candidateSessionContext) {
		this.candidateSessionContext = candidateSessionContext;
	}
	
	//$itemSessionState/@entryTime!='' and not($isItemSessionEnded)
	public boolean isItemSessionOpen(ItemSessionState itemSessionState, boolean solutionMode) {
		return itemSessionState.getEntryTime() != null && !isItemSessionEnded(itemSessionState, solutionMode);
	}
	
	public boolean isItemSessionEnded(ItemSessionState itemSessionState, boolean solutionMode) {
		return candidateSessionContext.isTerminated() || itemSessionState.getEndTime() != null || solutionMode;
	}
	
	//<xsl:variable name="identifierMatch" select="boolean(qw:value-contains(qw:get-outcome-value(@outcomeIdentifier), @identifier))" as="xs:boolean"/>
    //<xsl:if test="($identifierMatch and @showHide='show') or (not($identifierMatch) and @showHide='hide')">
    //   <xsl:apply-templates/>
    //</xsl:if>
	public boolean isFeedback(FeedbackElement feedbackElement, ItemSessionState itemSessionState) {
		Identifier outcomeIdentifier = feedbackElement.getOutcomeIdentifier();
		Identifier identifier = feedbackElement.getIdentifier();
		Value outcomeValue = itemSessionState.getOutcomeValues().get(outcomeIdentifier);
		boolean identifierMatch = valueContains(outcomeValue, identifier);
		return (identifierMatch && feedbackElement.getVisibilityMode() == VisibilityMode.SHOW_IF_MATCH)
				|| (!identifierMatch && feedbackElement.getVisibilityMode() == VisibilityMode.HIDE_IF_MATCH);
	}
	
	public boolean isFeedback(ModalFeedback feedbackElement, ItemSessionState itemSessionState) {
		Identifier outcomeIdentifier = feedbackElement.getOutcomeIdentifier();
		Identifier identifier = feedbackElement.getIdentifier();
		Value outcomeValue = itemSessionState.getOutcomeValues().get(outcomeIdentifier);
		boolean identifierMatch = valueContains(outcomeValue, identifier);
		return (identifierMatch && feedbackElement.getVisibilityMode() == VisibilityMode.SHOW_IF_MATCH)
				|| (!identifierMatch && feedbackElement.getVisibilityMode() == VisibilityMode.HIDE_IF_MATCH);
	}

	@Override
	public Component getComponent(String name) {
		AssessmentObjectFormItem assessmentItem = getQtiItem();
		if(assessmentItem != null) {
			for(FormItem item:assessmentItem.getFormItems()) {
				if(item.getComponent().getComponentName().equals(name)) {
					return item.getComponent();
				}
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> cmps = new ArrayList<>();
		AssessmentObjectFormItem assessmentItem = getQtiItem();
		if(assessmentItem != null) {
			for(FormItem item:assessmentItem.getFormItems()) {
				cmps.add(item.getComponent());
			}
		}
		return cmps;
	}

	@Override
	public Context getContext() {
		return context;
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}
	
	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		loadJavascripts(vr);
	}
	
	protected void loadJavascripts(ValidationResult vr) {
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/QtiWorksRendering.js");
		if(mathAssess) {
			jsa.addRequiredStaticJsFile("assessment/rendering/javascript/AsciiMathInputController.js");
			jsa.addRequiredStaticJsFile("assessment/rendering/javascript/UpConversionAjaxController.js");
		}
		
		// drawing needs slider, slider need it too
		// drag and drop used a lot...
		jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.qti.min.js");
		// hotspot
		jsa.addRequiredStaticJsFile("js/jquery/maphilight/jquery.maphilight.js");
		//tab
		jsa.addRequiredStaticJsFile("js/jquery/taboverride/taboverride-4.0.0.min.js");
		
		if(Settings.isDebuging()) {
			// order needs dragula
			jsa.addRequiredStaticJsFile("js/dragula/dragula.js");
			jsa.addRequiredStaticJsFile("js/jquery/taboverride/jquery.taboverride.js");
			// qtiAutosave and qtiTimer are loaded by the controller
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.associate.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.choice.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.gapMatch.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicAssociate.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicGap.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicOrder.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.hotspot.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.hotspot.responsive.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.match.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.match_dnd.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.order.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.positionObject.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.selectPoint.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.countWords.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.slider.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.qtiCopyPaste.js");
			jsa.addRequiredStaticJsFile("js/jquery/openolat/jquery.paint.v2.js");
			jsa.addRequiredStaticJsFile("js/fabricjs/paint.fabric.js");
		} else {
			jsa.addRequiredStaticJsFile("js/dragula/dragula.min.js");
			jsa.addRequiredStaticJsFile("js/jquery/taboverride/jquery.taboverride.min.js");
			jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.qti.min.js");
			jsa.addRequiredStaticJsFile("js/fabricjs/paint.fabric.min.js");
		}
	}
	
	@Override
	public abstract AssessmentObjectComponentRenderer getHTMLRendererSingleton();

}
