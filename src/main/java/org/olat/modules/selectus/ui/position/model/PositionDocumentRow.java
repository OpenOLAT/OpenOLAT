/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position.model;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;

import org.olat.modules.selectus.DocumentEnum;

/**
 * 
 * Initial date: 30 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionDocumentRow {
	
	private final DocumentEnum document;
	
	private MultipleSelectionElement enabledEl;
	private MultipleSelectionElement mandatoryEl;
	private SingleSelection usageEl;
	private MultipleSelectionElement combinedEl;
	private FormLink editNameButton;
	private TextElement documentNameEl;
	private TextElement documentSizeEl;
	
	private Map<Locale, String> documentNames = new HashMap<>();
	private Map<Locale, String> documentExplain = new HashMap<>();
	
	private boolean experts;
	private boolean referees;
	private boolean comparativeExperts;
	private boolean[] feedbackMembers;
	
	private boolean typePdf;
	private boolean typeXlsx;
	private boolean typeDocx;
	private boolean typeJpg;
	
	public PositionDocumentRow(DocumentEnum document) {
		this.document = document;
	}
	
	public DocumentEnum getDocument() {
		return document;
	}

	public MultipleSelectionElement getEnableEl() {
		return enabledEl;
	}

	public void setEnableEl(MultipleSelectionElement enableEl) {
		this.enabledEl = enableEl;
		enableEl.setUserObject(this);
	}

	public MultipleSelectionElement getMandatoryEl() {
		return mandatoryEl;
	}

	public void setMandatoryEl(MultipleSelectionElement mandatoryEl) {
		this.mandatoryEl = mandatoryEl;
	}

	public SingleSelection getUsageEl() {
		return usageEl;
	}

	public void setUsageEl(SingleSelection usageEl) {
		this.usageEl = usageEl;
	}

	public MultipleSelectionElement getCombinedEl() {
		return combinedEl;
	}

	public void setCombinedEl(MultipleSelectionElement combinedEl) {
		this.combinedEl = combinedEl;
	}

	public TextElement getDocumentNameEl() {
		return documentNameEl;
	}

	public void setDocumentNameEl(TextElement documentNameEl) {
		this.documentNameEl = documentNameEl;
	}
	
	public String getDocumentName(Locale locale) {
		return documentNames.get(locale);
	}
	
	public void setDocumentNames(String name, Locale locale) {
		documentNames.put(locale, name);
	}
	
	public String getDocumentExplain(Locale locale) {
		return documentExplain.get(locale);
	}
	
	public void setDocumentExplain(String name, Locale locale) {
		documentExplain.put(locale, name);
	}

	public TextElement getDocumentSizeEl() {
		return documentSizeEl;
	}

	public void setDocumentSizeEl(TextElement documentSizeEl) {
		this.documentSizeEl = documentSizeEl;
	}

	public FormLink getEditNameButton() {
		return editNameButton;
	}

	public void setEditNameButton(FormLink editNameButton) {
		this.editNameButton = editNameButton;
	}

	public boolean isHasOnlyPdf() {
		return !typeXlsx && !typeDocx && !typeJpg;
	}

	public boolean isExperts() {
		return experts;
	}

	public void setExperts(boolean experts) {
		this.experts = experts;
	}

	public boolean isReferees() {
		return referees;
	}

	public void setReferees(boolean referees) {
		this.referees = referees;
	}

	public boolean isComparativeExperts() {
		return comparativeExperts;
	}

	public void setComparativeExperts(boolean comparativeExperts) {
		this.comparativeExperts = comparativeExperts;
	}

	public boolean[] getFeedbackMembers() {
		return feedbackMembers;
	}

	public void setFeedbackMembers(boolean[] feedbackMembers) {
		this.feedbackMembers = feedbackMembers;
	}

	public boolean isTypePdf() {
		return typePdf;
	}

	public void setTypePdf(boolean typePdf) {
		this.typePdf = typePdf;
	}

	public boolean isTypeXlsx() {
		return typeXlsx;
	}

	public void setTypeXlsx(boolean typeXlsx) {
		this.typeXlsx = typeXlsx;
	}

	public boolean isTypeDocx() {
		return typeDocx;
	}

	public void setTypeDocx(boolean typeDocx) {
		this.typeDocx = typeDocx;
	}

	public boolean isTypeJpg() {
		return typeJpg;
	}

	public void setTypeJpg(boolean typeJpg) {
		this.typeJpg = typeJpg;
	}
	
	
}
