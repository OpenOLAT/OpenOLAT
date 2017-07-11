package org.olat.ims.qti21.ui.components;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 10 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlowComponent extends AssessmentObjectComponent {
	
	private final static FlowComponentRenderer RENDERER = new FlowComponentRenderer();
	
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private final File assessmentItemFile;
	
	private final FlowFormItem qtiItem;
	private List<FlowStatic> flowStatics;
	private List<InlineStatic> inlineStatics;
	
	public FlowComponent(String name, File assessmentItemFile, FlowFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
		this.assessmentItemFile = assessmentItemFile;
		setDomReplacementWrapperRequired(false);
	}
	
	public List<FlowStatic> getFlowStatics() {
		return flowStatics;
	}

	public void setFlowStatics(List<FlowStatic> flowStatics) {
		this.flowStatics = flowStatics;
	}

	public List<InlineStatic> getInlineStatics() {
		return inlineStatics;
	}

	public void setInlineStatics(List<InlineStatic> inlineStatics) {
		this.inlineStatics = inlineStatics;
	}

	public ResolvedAssessmentTest getResolvedAssessmentTest() {
		return resolvedAssessmentTest;
	}

	public void setResolvedAssessmentTest(ResolvedAssessmentTest resolvedAssessmentTest) {
		this.resolvedAssessmentTest = resolvedAssessmentTest;
	}

	@Override
	public FlowFormItem getQtiItem() {
		return qtiItem;
	}

	@Override
	public String getResponseUniqueIdentifier(ItemSessionState itemSessionState, Interaction interaction) {
		return null;
	}

	@Override
	public Interaction getInteractionOfResponseUniqueIdentifier(String responseUniqueId) {
		return null;
	}

	@Override
	public String relativePathTo(ResolvedAssessmentItem rAssessmentItem) {

		String relativePathString = "";
		if(resolvedAssessmentTest != null) {
			URI testUri = resolvedAssessmentTest.getTestLookup().getSystemId();
			File testFile = new File(testUri);
			Path relativePath = testFile.toPath().getParent().relativize(assessmentItemFile.toPath().getParent());
			relativePathString = relativePath.toString();
		}
		
		if(relativePathString.isEmpty()) {
			return relativePathString;
		} else if(relativePathString.endsWith("/")) {
			return relativePathString;
		}
		return relativePathString.concat("/");
	}

	@Override
	public FlowComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
