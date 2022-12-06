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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 10 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlowComponentRenderer extends AssessmentObjectComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(FlowComponentRenderer.class);

	@Override
	public void renderComponent(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		FlowComponent avc = (FlowComponent)source;
		AssessmentRenderer aRenderer = new AssessmentRenderer(renderer);
		if(avc.getFlowStatics() != null) {
			try {
				avc.getFlowStatics().forEach(flow
					-> avc.getHTMLRendererSingleton().renderFlow(aRenderer, target, avc, (ResolvedAssessmentItem)null, (ItemSessionState)null, flow, ubu, translator));
			} catch (Exception e) {
				log.error("", e);
			}
		}
		if(avc.getInlineStatics() != null) {
			try {
				avc.getInlineStatics().forEach(inline
						-> avc.getHTMLRendererSingleton().renderInline(aRenderer, target, avc, (ResolvedAssessmentItem)null, (ItemSessionState)null, inline, ubu, translator));
			} catch (Exception e) {
				log.error("", e);
			}
		}
		if(avc.getBlocks() != null) {
			try {
				avc.getBlocks().forEach(block
						-> avc.getHTMLRendererSingleton().renderBlock(aRenderer, target, avc, (ResolvedAssessmentItem)null, (ItemSessionState)null, block, ubu, translator));
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {
		//do nothing
	}
	
}
