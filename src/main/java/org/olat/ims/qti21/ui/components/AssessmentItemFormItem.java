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

import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.close;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.exit;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.resethard;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.resetsoft;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.response;
import static org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event.solution;

import java.util.Collections;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;

import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;

/**
 * 
 * Initial date: 11.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemFormItem extends AbstractAssessmentFormItem {
	
	private final AssessmentItemComponent component;
	
	private String mapperUri;
	
	public AssessmentItemFormItem(String name) {
		super(name);
		component = new AssessmentItemComponent(name + "_cmp", this);
	}

	@Override
	public AssessmentItemComponent getComponent() {
		return component;
	}

	public String getMapperUri() {
		return mapperUri;
	}

	public void setMapperUri(String mapperUri) {
		this.mapperUri = mapperUri;
	}

	public ItemSessionController getItemSessionController() {
		return component.getItemSessionController();
	}

	public void setItemSessionController(ItemSessionController itemSessionController) {
		component.setItemSessionController(itemSessionController);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		//
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		String uri = ureq.getModuleURI();
		if(uri.startsWith(solution.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(solution, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(response.getPath())) {
			Map<Identifier, StringResponseData> stringResponseMap = extractStringResponseData();
			Map<Identifier, MultipartFileInfos> fileResponseMap;
			if(getRootForm().isMultipartEnabled()) {
				fileResponseMap = extractFileResponseData();
			} else {
				fileResponseMap = Collections.emptyMap();
			}
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(response, stringResponseMap, fileResponseMap, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(resethard.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(resethard, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(resetsoft.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(resetsoft, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(close.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(close, this);
			getRootForm().fireFormEvent(ureq, event);
		} else if(uri.startsWith(exit.getPath())) {
			QTIWorksAssessmentItemEvent event = new QTIWorksAssessmentItemEvent(exit, this);
			getRootForm().fireFormEvent(ureq, event);
		}
	}

	@Override
	public void reset() {
		//
	}
}