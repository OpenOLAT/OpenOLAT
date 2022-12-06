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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Flow;
import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 19.04.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InteractionResultComponentRenderer extends AssessmentObjectComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		InteractionResultComponent cmp = (InteractionResultComponent)source;
		Interaction interaction = cmp.getInteraction();
		ResolvedAssessmentItem resolvedAssessmentItem = cmp.getResolvedAssessmentItem();
		
		ItemSessionState itemSessionState = cmp.getItemSessionState();
		AssessmentRenderer assessmentRenderer = new AssessmentRenderer(renderer);
		
		if(cmp.isShowSolution()) {
			assessmentRenderer.setSolutionAllowed(true);
			assessmentRenderer.setSolutionMode(true);
		} else {
			assessmentRenderer.setReviewMode(true);
		}
		
		if(interaction instanceof Block) {
			renderBlock(assessmentRenderer, sb, cmp, resolvedAssessmentItem, itemSessionState, (Block)interaction, ubu, translator);
		} else if(interaction instanceof Flow) {
			renderFlow(assessmentRenderer, sb, cmp, resolvedAssessmentItem, itemSessionState, (Flow)interaction, ubu, translator);
		}
	}

	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {
		//
	}
}
