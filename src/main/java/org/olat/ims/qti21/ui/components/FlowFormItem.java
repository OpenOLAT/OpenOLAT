package org.olat.ims.qti21.ui.components;

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;

import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;

/**
 * 
 * Initial date: 10 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlowFormItem extends AssessmentObjectFormItem {
	
	private final FlowComponent component;

	public FlowFormItem(String name, File assessmentItemFile) {
		super(name, null);
		component = new FlowComponent(name, assessmentItemFile, this);
	}
	
	public List<FlowStatic> getFlowStatics() {
		return component.getFlowStatics();
	}

	public void setFlowStatics(List<FlowStatic> flowStatics) {
		component.setFlowStatics(flowStatics);
	}
	
	public List<InlineStatic> getInlineStatics() {
		return component.getInlineStatics();
	}

	public void setInlineStatics(List<InlineStatic> inlineStatics) {
		component.setInlineStatics(inlineStatics);
	}

	@Override
	public FlowComponent getComponent() {
		return component;
	}

	@Override
	protected FlowComponent getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		// 
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		//
	}

	@Override
	public void reset() {
		//
	}
	
	
}
