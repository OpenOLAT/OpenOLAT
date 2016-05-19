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
import java.util.Collections;

import org.apache.velocity.context.Context;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.render.velocity.VelocityComponent;
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
	
	private String mapperUri;
	private URI assessmentObjectUri;
	private ResourceLocator resourceLocator;
	private CandidateSessionContext candidateSessionContext;
	
	public AssessmentObjectComponent(String name) {
		super(name);
	}
	
	public abstract AssessmentObjectFormItem getQtiItem();
	
	public String getMapperUri() {
		return mapperUri;
	}

	public void setMapperUri(String mapperUri) {
		this.mapperUri = mapperUri;
	}

	public URI getAssessmentObjectUri() {
		return assessmentObjectUri;
	}

	public void setAssessmentObjectUri(URI assessmentObjectUri) {
		this.assessmentObjectUri = assessmentObjectUri;
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
		return itemSessionState.getEndTime() != null || solutionMode;
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
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return Collections.emptyList();
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

		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/QtiWorksRendering.js");
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/AsciiMathInputController.js");
		jsa.addRequiredStaticJsFile("assessment/rendering/javascript/UpConversionAjaxController.js");
		
		jsa.addRequiredStaticJsFile("js/jquery/maphilight/jquery.maphilight.js");
		jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.qti.min.js");
		
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.associate.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicAssociate.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicGap.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.graphicOrder.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.selectPoint.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.positionObject.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.slider.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.order.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.match.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.gapMatch.js");
		jsa.addRequiredStaticJsFile("js/jquery/qti/jquery.hotspot.js");
	}
	
	@Override
	public abstract AssessmentObjectComponentRenderer getHTMLRendererSingleton();

}
