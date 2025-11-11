/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Nov 10, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryInfoCardController extends FormBasicController {

	private RepositoryEntryInfoHeaderController headerCtrl;
	
	private final RepositoryEntry entry;
	
	@Autowired
	private MapperService mapperService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private NodeAccessService nodeAccessService;

	public RepositoryEntryInfoCardController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, LAYOUT_BAREBONE, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.entry = entry;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String infoPage = Util.getPackageVelocityRoot(RepositoryEntryInfoCardController.class) + "/repo_info.html";
		FormLayoutContainer infoCont = FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), infoPage);
		infoCont.setRootForm(mainForm);
		formLayout.add(infoCont);
		
		initHeader(ureq, infoCont);
		initInfos(infoCont);
	}
	
	private void initHeader(UserRequest ureq, FormItemContainer formLayout) {
		headerCtrl = new RepositoryEntryInfoHeaderController(ureq, getWindowControl(), mainForm);
		listenTo(headerCtrl);
		formLayout.add("header", headerCtrl.getInitialFormItem());
		
		headerCtrl.setTeaserUrl(getTeaserUrl());
		headerCtrl.setTitle(entry.getDisplayname());
		headerCtrl.getExternalRef(entry.getExternalRef());
		headerCtrl.setType(getType());
	}
	
	private String getType() {
		String type;
		if ("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
			if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
				NodeAccessType nodeAccesType = NodeAccessType.of(entry.getTechnicalType());
				if (ConditionNodeAccessProvider.TYPE.equals(nodeAccesType.getType())) {
					type = translate("CourseModule");
				} else {
					type = nodeAccessService.getNodeAccessTypeName(nodeAccesType, getLocale());
				}
			}
			type = translate("CourseModule");
		} else {
			type = translate(entry.getOlatResource().getResourceableTypeName());
		}
		return type;
	}
	
	protected String getTeaserUrl() {
		String teaserUrl = null;
		VFSLeaf image = repositoryService.getIntroductionImage(entry);
		if (image != null) {
			MapperKey mapperKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
			teaserUrl = RepositoryEntryImageMapper.getImageUrl(mapperKey.getUrl() , image);
		}
		return teaserUrl;
	}

	private void initInfos(FormItemContainer formLayout) {
		String itemPage = Util.getPackageVelocityRoot(RepositoryEntryInfoCardController.class) + "/repo_info_items.html";
		FormLayoutContainer itemsCont = FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), itemPage);
		itemsCont.setRootForm(mainForm);
		formLayout.add("items", itemsCont);
		
		uifactory.addStaticTextElement("table.header.key", String.valueOf(entry.getKey()), itemsCont);
		
		uifactory.addStaticTextElement("cif.type", getType(), itemsCont);
		
		String executionPeriod = getExecutionPeriod();
		if (StringHelper.containsNonWhitespace(executionPeriod)) {
			uifactory.addStaticTextElement("cif.dates", StringHelper.escapeHtml(executionPeriod), itemsCont);
		}
		
		String location = entry.getLocation();
		if (StringHelper.containsNonWhitespace(location)) {
			uifactory.addStaticTextElement("cif.location", StringHelper.escapeHtml(location), itemsCont);
		}
	}
	
	private String getExecutionPeriod() {
		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		if (lifecycle == null) {
			return null;
		}
		
		String executionPeriod = null;
		if (StringHelper.containsNonWhitespace(lifecycle.getLabel())) {
			executionPeriod = StringHelper.escapeHtml(lifecycle.getLabel());
		} else if (StringHelper.containsNonWhitespace(lifecycle.getSoftKey())) {
			executionPeriod = lifecycle.getSoftKey();
		} else {
			if (lifecycle.getValidFrom() != null) {
				executionPeriod = Formatter.getInstance(getLocale()).formatDate(lifecycle.getValidFrom());
			}
			if (lifecycle.getValidTo() != null) {
				if (StringHelper.containsNonWhitespace(executionPeriod)) {
					executionPeriod += " - ";
				}
				executionPeriod += Formatter.getInstance(getLocale()).formatDate(lifecycle.getValidTo());
			}
		}
		return executionPeriod;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
