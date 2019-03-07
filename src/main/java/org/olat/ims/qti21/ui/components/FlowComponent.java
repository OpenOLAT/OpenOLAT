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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
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
	
	private static final FlowComponentRenderer RENDERER = new FlowComponentRenderer();
	
	private ResolvedAssessmentTest resolvedAssessmentTest;
	private final File assessmentItemFile;
	
	private final FlowFormItem qtiItem;
	
	private List<Block> blocks;
	private List<FlowStatic> flowStatics;
	private List<InlineStatic> inlineStatics;
	
	public FlowComponent(String name, File assessmentItemFile) {
		super(name);
		qtiItem = null;
		this.assessmentItemFile = assessmentItemFile;
		setDomReplacementWrapperRequired(false);
	}
	
	public FlowComponent(String name, File assessmentItemFile, FlowFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
		this.assessmentItemFile = assessmentItemFile;
		setDomReplacementWrapperRequired(false);
	}
	
	public List<Block> getBlocks() {
		return blocks;
	}

	public void setBlocks(List<Block> blocks) {
		this.blocks = blocks;
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
		
		if(relativePathString.isEmpty() || relativePathString.endsWith("/")) {
			return relativePathString;
		}
		return relativePathString.concat("/");
	}

	@Override
	public FlowComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
