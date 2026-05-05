/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.copy;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.CopyApplicationParameters;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;

/**
 * 
 * Initial date: 7 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectDataToCopyController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private SelectionDataModel dataModel;
	
	private final CopyApplicationContext copyContext;
	
	private int counter = 0;
	
	private boolean hasMemo;
	private boolean hasTags;
	private boolean hasExperts;
	private boolean hasReferees;
	private boolean hasDocuments;
	private boolean hasCommitteeComment;
	private boolean hasComparativeExperts;
	private boolean hasCustomApplicationFields;

	private boolean hasReviews;
	private boolean hasDecisionTool;
	private boolean hasPublicFeedback;

	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public SelectDataToCopyController(UserRequest ureq, WindowControl wControl,
			StepsRunContext runContext, Form form, CopyApplicationContext copyContext) {
		super(ureq, wControl, form, runContext, LAYOUT_CUSTOM, "data_list");
		this.copyContext = copyContext;
		
		Position position = copyContext.getSourcePosition();
		
		hasMemo = recruitingModule.isApplicationsMemoEnabled();
		hasTags = (recruitingModule.isSystemTagsEnabled() && recruitingModule.isSystemTagsEnabled(position))
				|| (recruitingModule.isPositionTagsEnabled() && recruitingModule.isPositionTagsEnabled(position));
		hasExperts = recruitingModule.isReferenceEnabled() && position.isExpertRecommendationEnabled();
		hasReferees = recruitingModule.isReferenceEnabled() && position.isRefereeRecommendationEnabled();
		hasDocuments = !recruitingModule.getDocumentOptions(position).isEmpty();
		hasCommitteeComment = recruitingModule.isApplicationsCommitteeCommentEnabled() && position.isCommitteeCommentEnabled();
		
		List<PositionAttributeDefinition> definitions = position.getAttributesDefinitions();
		hasCustomApplicationFields =  recruitingModule.getPositionMaxNumberOfAdditionalAttributes() > 0
				&& definitions != null && !definitions.isEmpty();
		hasReviews = recruitingModule.isReviewEnabled() && position.getReviewDefinition() != null;
		hasDecisionTool = recruitingModule.isDecisionToolEnabled() && position.isDecisionTool();
		hasPublicFeedback = recruitingModule.isMembersFeedbackEnabled()
				&& feedbackService.hasFeedbackConfigurationEnabled(position);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initFormDescription();
		initSelectionForm(formLayout);
	}
	
	private void initSelectionForm(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.attribute));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SelectionCols.copy));
		
		dataModel = new  SelectionDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "copy.selection", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setLabel("copy.selection", null);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void initFormDescription() {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("<p>").append(translate("copy.data.hint1")).append("</p>")
		  .append("<p>").append(translate("copy.data.hint2")).append("</p>")
		  .append("<ul>");
		if(hasCustomApplicationFields) {
			sb.append("<li>").append(translate("copy.data.hint.custom.fields")).append("</li>");
		}
		if(hasDocuments) {
			sb.append("<li>").append(translate("copy.data.hint.documents")).append("</li>");
		}
		if(hasReferees) {
			sb.append("<li>").append(translate("copy.data.hint.referees")).append("</li>");
		}
		if(hasExperts) {
			sb.append("<li>").append(translate("copy.data.hint.experts")).append("</li>");
		}
		if(hasComparativeExperts) {
			sb.append("<li>").append(translate("copy.data.hint.comparative.experts")).append("</li>");
		}
		if(hasCommitteeComment) {
			sb.append("<li>").append(translate("copy.data.hint.committee.comment")).append("</li>");
		}
		if(hasTags) {
			sb.append("<li>").append(translate("copy.data.hint.tags")).append("</li>");
		}
		sb.append("</ul>");
		
		// Don't copy
		sb.append("<p>").append(translate("copy.data.hint3")).append("</p>")
		  .append("<ul>");
		if(hasPublicFeedback) {
			sb.append("<li>").append(translate("copy.data.hint.feedback")).append("</li>");
		}
		sb.append("<li>").append(translate("copy.data.hint.ratings")).append("</li>");
		if(hasReviews) {
			sb.append("<li>").append(translate("copy.data.hint.reviews")).append("</li>");
		}
		if(hasDecisionTool) {
			sb.append("<li>").append(translate("copy.data.hint.decision.tool")).append("</li>");
		}
		sb.append("<li>").append(translate("copy.data.hint.mail.log")).append("</li>")
		  .append("<li>").append(translate("copy.data.hint.activity.log")).append("</li>")
		  .append("</ul>");
		
		setFormTranslatedDescription(sb.toString());
	}
	
	private void loadModel() {
		List<SelectionData> rows = new ArrayList<>();
		forgeRow(CopyApplicationParameters.Copy.profileInformations, true, rows);
		if(hasDocuments) {
			forgeRow(CopyApplicationParameters.Copy.applicationDocuments, false, rows);
		}
		if(hasReferees) {
			forgeRow(CopyApplicationParameters.Copy.refereesAndLetters, false, rows);
		}
		if(hasExperts) {
			forgeRow(CopyApplicationParameters.Copy.expertsAndAssessments, false, rows);
		}
		if(hasComparativeExperts) {
			forgeRow(CopyApplicationParameters.Copy.comparativeExperts, false, rows);
		}
		if(hasMemo) {
			forgeRow(CopyApplicationParameters.Copy.memo, false, rows);
		}
		if(hasCommitteeComment) {
			forgeRow(CopyApplicationParameters.Copy.committeeComment, false, rows);
		}
		forgeRow(CopyApplicationParameters.Copy.applicationStatus, false, rows);
		if(hasTags) {
			forgeRow(CopyApplicationParameters.Copy.tags, false, rows);
		}
		forgeRow(CopyApplicationParameters.Copy.decision, false, rows);
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private SelectionData forgeRow(CopyApplicationParameters.Copy dataToCopy, boolean mandatory, List<SelectionData> rows) {
		String name = translate("data.to." + dataToCopy);
		
		SelectionValues kp = new SelectionValues();
		kp.add(SelectionValues.entry("on", ""));
		MultipleSelectionElement selectionEl = uifactory.addCheckboxesHorizontal("test_" + (++counter), velocity_root, flc, kp.keys(), kp.values());
		SelectionData row = new SelectionData(dataToCopy, name, selectionEl);
		rows.add(row);
		if(mandatory) {
			selectionEl.select("on", true);
			selectionEl.setEnabled(false);
		}
		return row;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		boolean selected = false;
		for(SelectionData data:dataModel.getObjects()) {
			selected |= data.getSelectionEl().isAtLeastSelected(1);
		}
		
		tableEl.clearError();
		if(!selected) {
			tableEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		commitChanges();
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void commitChanges() {
		List<CopyApplicationParameters.Copy> typeOfData = new ArrayList<>();
		for(SelectionData data:dataModel.getObjects()) {
			if(data.getSelectionEl().isAtLeastSelected(1)) {
				typeOfData.add(data.getTypeOfData());
			}
		}
		
		copyContext.setTypeOfData(typeOfData);
	}
	
	public static class SelectionData {
		
		private final String name;
		private final MultipleSelectionElement selectionEl;
		private final CopyApplicationParameters.Copy typeOfData;
		
		public SelectionData(CopyApplicationParameters.Copy typeOfData, String name, MultipleSelectionElement selectionEl) {
			this.name = name;
			this.selectionEl = selectionEl;
			this.typeOfData = typeOfData;
		}
		
		public CopyApplicationParameters.Copy getTypeOfData() {
			return typeOfData;
		}

		public String getName() {
			return name;
		}

		public MultipleSelectionElement getSelectionEl() {
			return selectionEl;
		}
	}
	
	private static class SelectionDataModel extends DefaultFlexiTableDataModel<SelectionData> {
		
		private static final SelectionCols[] COLS = SelectionCols.values();
		
		public SelectionDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			SelectionData app = getObject(row);
			switch(COLS[col]) {
				case attribute: return app.getName();
				case copy: return app.getSelectionEl();
				default: return null;
			}
		}
	}
	
	public enum SelectionCols implements FlexiSortableColumnDef {
		attribute("table.header.attribute"),
		copy("table.header.copy");

		private final String i18nKey;
		
		private SelectionCols(String key) {
			this.i18nKey = key;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
