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
import org.olat.core.util.Formatter;
import org.olat.core.util.WebappHelper;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 27 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemBodyResultComponentRenderer extends AssessmentObjectComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		ItemBodyResultComponent cmp = (ItemBodyResultComponent)source;
		ResolvedAssessmentItem resolvedAssessmentItem = cmp.getResolvedAssessmentItem();
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		
		ItemSessionState itemSessionState = cmp.getItemSessionState();
		AssessmentRenderer assessmentRenderer = new AssessmentRenderer(renderer);
		assessmentRenderer.setOffline(cmp.isOffline());
		
		if(cmp.isShowSolution()) {
			assessmentRenderer.setSolutionAllowed(true);
			assessmentRenderer.setSolutionMode(true);
			assessmentRenderer.setReport(cmp.isReport());
		} else {
			assessmentRenderer.setReviewMode(true);
		}
		
		assessmentItem.getItemBody().getBlocks().forEach(block
				-> renderBlock(assessmentRenderer, sb, cmp, resolvedAssessmentItem, itemSessionState, block, ubu, translator));
		
		if(assessmentRenderer.isMathJax()
        		|| (WebappHelper.isMathJaxMarkers() && (sb.contains("\\(") || sb.contains("\\[") || sb.contains("$$")))) {
			sb.append(Formatter.elementLatexFormattingScript());
		}
	}

	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {
		//
	}
}
