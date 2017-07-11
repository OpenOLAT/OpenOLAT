package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
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
	
	private static final OLog log = Tracing.createLoggerFor(FlowComponentRenderer.class);

	@Override
	public void render(Renderer renderer, StringOutput target, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		FlowComponent avc = (FlowComponent)source;
		AssessmentRenderer aRenderer = new AssessmentRenderer(renderer);
		if(avc.getFlowStatics() != null) {
			try {
				avc.getFlowStatics().forEach((flow)
					-> avc.getHTMLRendererSingleton().renderFlow(aRenderer, target, avc, (ResolvedAssessmentItem)null, (ItemSessionState)null, flow, ubu, translator));
			} catch (Exception e) {
				log.error("", e);
			}
		}
		if(avc.getInlineStatics() != null) {
			try {
				avc.getInlineStatics().forEach((inline)
						-> avc.getHTMLRendererSingleton().renderInline(aRenderer, target, avc, (ResolvedAssessmentItem)null, (ItemSessionState)null, inline, ubu, translator));
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
