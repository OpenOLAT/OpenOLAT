/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionProfessorship;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionDetailsController extends FormBasicController implements Activateable2 {

	private SpacerElement docSpacer;
	private FormLayoutContainer docLayout;
	private FormLayoutContainer professorshipLayout;
	private FormLayoutContainer containerDoc1;
	private FormLayoutContainer containerDoc2;
	private FormLayoutContainer containerDoc3;
	private StaticTextElement posTitleStaticElement;
	private StaticTextElement descriptionStaticElement;
	
	private Position position;
	
	private static final String[] docKeys = new String[]{"available","mandatory"};
	private final String[] docValues = new String[docKeys.length];
	
	private String[] professorshipKeys = new String[]{
			PositionProfessorship.assistant.name(),
			PositionProfessorship.full.name()
	};
	private String[] professorshipValues = new String[professorshipKeys.length];

	private final String mapperBaseUrl;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionDetailsController(UserRequest ureq, WindowControl wControl, Position position,
			String mapperBaseUrl, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl);
		this.position = position;
		this.mapperBaseUrl = mapperBaseUrl;
		this.secCallback = secCallback;

		docValues[0] = translate("document.available");
		docValues[1] = translate("document.mandatory");
		professorshipValues[0] = translate("professorship.assistant");
		professorshipValues[1] = translate("professorship.full");

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String positionTitle = position.getMLTitle(getLocale());
		positionTitle = StringHelper.escapeHtml(positionTitle);;
		posTitleStaticElement = uifactory.addStaticTextElement("position_title", "edit.position.details.title", positionTitle, formLayout);
		if(secCallback.canSeePositionURL()) {
			String link = RecruitingHelper.getLinkToPosition(position);
			uifactory.addStaticTextElement("ext_link", "edit.extern_link", link, formLayout);
			String linkDetails = RecruitingHelper.getLinkToPositionDetails(position);
			uifactory.addStaticTextElement("ext_link_details", "edit.extern_link.details", linkDetails, formLayout);
		}
		docSpacer = uifactory.addSpacerElement("sep1", formLayout, false);
		
		String page = velocity_root + "/positionDocs.html";
		docLayout = FormLayoutContainer.createCustomFormLayout("docs", getTranslator(), page);
		docLayout.setRootForm(mainForm);
		formLayout.add(docLayout);
		docLayout.setLabel("edit.docs.committee", null);
		
		docSpacer = uifactory.addSpacerElement("sep2", formLayout, false);

		containerDoc1 = appendDownloadLink(position.getDocument1(), 1);
		containerDoc2 = appendDownloadLink(position.getDocument2(), 2);
		containerDoc3 = appendDownloadLink(position.getDocument3(), 3);
		updateDocContainer();
		
		String description = position.getMLDescription(getLocale());
		descriptionStaticElement = uifactory.addStaticTextElement("description", "edit.position.details.description", description, formLayout);

		SpacerElement profSpacer = uifactory.addSpacerElement("sep3", formLayout, false);
		profSpacer.setVisible(recruitingModule.isProfessorshipTypeEnabled());
		
		String professorshipPage = velocity_root + "/professorship.html";
		professorshipLayout = FormLayoutContainer.createCustomFormLayout("professorship", getTranslator(), professorshipPage);
		professorshipLayout.setRootForm(mainForm);
		professorshipLayout.setLabel("professorship.type", null);
		professorshipLayout.setVisible(recruitingModule.isProfessorshipTypeEnabled());
		formLayout.add(professorshipLayout);
		updateProfessorship();
	}
	
	public void setPositionTitle(String title) {
		posTitleStaticElement.setValue(title);
	}
	
	protected FormLayoutContainer appendDownloadLink(Attachment att, int pos) {
		if(att == null) return null;
		
		String page = velocity_root + "/download_link.html";
		FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout("doc-cont-" + pos, getTranslator(), page);
		String downloadLabel = att.getName();
		if(downloadLabel == null) {
			downloadLabel = "Document";
		}
		String mimeType = CSSHelper.createFiletypeIconCssClassFor(downloadLabel);
		container.contextPut("document", new ApplicationDocument(downloadLabel, mimeType, "/" + pos + "/" + att.getName()));
		container.contextPut("mapperBaseURL", mapperBaseUrl);
		container.contextPut("tooltip", translate("edit.docs.committee.explanation"));
		container.setRootForm(mainForm);
		
		docLayout.add(container);
		return container;
	}
	
	protected FormLayoutContainer updateDownloadLink(FormLayoutContainer container, Attachment att, int pos) {
		if(att == null) {
			if(container != null) {
				container.setVisible(false);
			}
			return null;
		}
		
		if(container == null) {
			return appendDownloadLink(att, pos);
		}
		
		String downloadLabel = att.getName();
		if(downloadLabel == null) {
			downloadLabel = "Document";
		}
		container.contextPut("document", new ApplicationDocument(downloadLabel,  "/" + pos + "/" + att.getName()));
		container.setDirty(true);
		container.setVisible(true);
		return container;
	}
	
	protected void updateDocContainer() {
		boolean visible = secCallback.canSeePositionDocuments()
				&& (position.getDocument1() != null || position.getDocument2() != null || position.getDocument3() != null);
		docSpacer.setVisible(visible);
		docLayout.setVisible(visible);
	}
	
	protected void updateProfessorship() {
		List<String> professorshipTypes = new ArrayList<>();
		if(PositionProfessorship.any.name().equals(position.getProfessorship())) {
			professorshipTypes.add(professorshipValues[0]);
			professorshipTypes.add(professorshipValues[1]);
		} else if(PositionProfessorship.assistant.name().equals(position.getProfessorship())) {
			professorshipTypes.add(professorshipValues[0]);
		} else if(PositionProfessorship.full.name().equals(position.getProfessorship())) {
			professorshipTypes.add(professorshipValues[1]);
		}
		professorshipLayout.contextPut("professorshipTypes", professorshipTypes);
	}
	
	public void updatePosition(Position position) {
		this.position = position;
		
		String title = position.getMLTitle(getLocale());
		posTitleStaticElement.setValue(title);
		
		String description = position.getMLDescription(getLocale());
		descriptionStaticElement.setValue(description);

		containerDoc1 = updateDownloadLink(containerDoc1, position.getDocument1(), 1);
		containerDoc2 = updateDownloadLink(containerDoc2, position.getDocument2(), 2);
		containerDoc3 = updateDownloadLink(containerDoc3, position.getDocument3(), 3);
		updateDocContainer();
		updateProfessorship();
		flc.setDirty(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}
