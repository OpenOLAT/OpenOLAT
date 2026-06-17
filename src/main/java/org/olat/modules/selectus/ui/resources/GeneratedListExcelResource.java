/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.CommentService;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.comment.ApplicationReviewComment;
import org.olat.modules.selectus.model.comment.ApplicationReviewCommentKey;
import org.olat.modules.selectus.model.comment.ApplicationReviewComments;
import org.olat.modules.selectus.model.comment.PositionComments;
import org.olat.modules.selectus.model.review.ApplicationStatisticElement;
import org.olat.modules.selectus.model.review.ApplicationStatistics;
import org.olat.modules.selectus.model.review.ApplicationTextCollectionElement;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.PositionStatistics;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.Reviewer;
import org.olat.modules.selectus.model.review.ReviewerNameVisibilityEnum;
import org.olat.modules.selectus.ui.PositionApplicationsDataModel;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 21 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneratedListExcelResource extends ExcelFlexiTableResource {

	private static final Logger log = Tracing.createLoggerFor(GeneratedListExcelResource.class);
	
	private Position position;
	private final Identity identity;
	private List<Reviewer> reviewers;
	private final ListSettings settings;
	private PositionComments positionComments;
	private PositionStatistics positionStatistics;
	private ReviewerNameVisibilityEnum reviewerNameVisibility;
	private final RecruitingPositionSecurityCallback secCallback;
	private List<ReviewElementDefinition> reviewElementsDefinitions;
	private final PositionApplicationsDataModel applicationsDataModel;
	private final Map<Reviewer,Integer> numOfColumns = new HashMap<>();
	
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private CommentService commentService;
	
	public GeneratedListExcelResource(String filename, Position position, PositionApplicationsDataModel dataModel,
			Identity identity, RecruitingPositionSecurityCallback secCallback,
			ListSettings settings, Translator translator) {
		super(filename, dataModel, translator);
		this.position = position;
		this.settings = settings;
		this.identity = identity;
		this.secCallback = secCallback;
		this.applicationsDataModel = dataModel;
	}

	@Override
	protected void generate(OutputStream out) {
		if(settings.withReviews()) {
			PositionReviewDefinition reviewDefinition = position.getReviewDefinition();
			reviewDefinition = reviewService.getReviewDefinition(reviewDefinition);
			reviewerNameVisibility = reviewDefinition.getReviewNameVisibility();
			reviewElementsDefinitions = reviewDefinition.getElements().stream()
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			positionStatistics = reviewService.getReviewStatistics(position, identity, secCallback);
			reviewers = positionStatistics.getReviewers();
			Collections.sort(reviewers);
		}
		if(settings.isWithReviewDiscussions()) {
			positionComments = commentService.getComments(position);
		}
		
		List<String> sheetNames = new ArrayList<>();
		sheetNames.add("Applications");
		if(settings.isWithReviewTexts()) {
			for(ReviewElementDefinition definition:reviewElementsDefinitions) {
				if(definition.getType() == ReviewElementType.text) {
					sheetNames.add(definition.getLabel());
				}	
			}
		}
		if(settings.isWithReviewDiscussions()) {
			sheetNames.add(translator.translate("review.discussions"));
		}
		
		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, sheetNames.size(), sheetNames)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			
			// applications details
			createHeader(sheet, workbook);
			createData(sheet, workbook);
			
			// review texts
			if(settings.isWithReviewTexts()) {
				for(ReviewElementDefinition definition:reviewElementsDefinitions) {
					if(definition.getType() == ReviewElementType.text) {
						OpenXMLWorksheet textSheet = workbook.nextWorksheet();
						textSheet.setHeaderRows(1);
						createReviewTextHeader(textSheet, definition, workbook);
						createReviewTextData(textSheet, definition);
					}
				}
			}
			
			// discussions
			if(settings.isWithReviewDiscussions()) {
				OpenXMLWorksheet discussionSheet = workbook.nextWorksheet();
				buildDiscussionsNumOfColumns();
				createDiscussionsTextHeader(discussionSheet, workbook);
				createDiscussionsTextData(discussionSheet);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	private void buildDiscussionsNumOfColumns() {
		int rcnt = applicationsDataModel.getRowCount();
		for(Reviewer reviewer:reviewers) {
			int maxColumns = 1;
			for(int i=0; i<rcnt; i++) {
				ApplicationRow application = applicationsDataModel.getObject(i);
				ApplicationReviewCommentKey appReviewKey = new ApplicationReviewCommentKey(application.getKey(), reviewer.getKey());
				ApplicationReviewComments el = positionComments.getCommentsMap().get(appReviewKey);
				if(el != null) {
					List<String> comments = toString(el.getComments());
					if(maxColumns < comments.size()) {
						maxColumns = comments.size();
					}
				}
			}
			numOfColumns.put(reviewer, Integer.valueOf(maxColumns));
		}
	}
	
	private void createDiscussionsTextData(OpenXMLWorksheet sheet, ApplicationRow application) {
		Row row = sheet.newRow();

		int pos = 0;
		row.addCell(pos++, application.getApplication().getId(), null);
		row.addCell(pos++, application.getApplication().getPerson().getFirstName());
		row.addCell(pos++, application.getApplication().getPerson().getLastName());

		for(Reviewer reviewer:reviewers) {
			Integer cols = numOfColumns.get(reviewer);
			int columns = 1;
			if(cols != null && cols.intValue() > 0) {
				columns = cols.intValue();
			}
				
			ApplicationReviewCommentKey appReviewKey = new ApplicationReviewCommentKey(application.getKey(), reviewer.getKey());
			ApplicationReviewComments el = positionComments.getCommentsMap().get(appReviewKey);
			if(el == null) {
				pos += columns;
			} else {
				List<String> comments = toString(el.getComments());
				for(int i=0; i<columns; i++) {
					if(i < comments.size()) {
						row.addCell(pos++, comments.get(i));
					} else {
						pos++;
					}
				}
			}
		}
	}
	
	private List<String> toString(List<ApplicationReviewComment> comments) {
		StringBuilder sb = new StringBuilder(2048);
		for(ApplicationReviewComment comment:comments) {
			if(sb.length() > 0) {
				sb.append("\r");
			}
			
			String fullName = comment.getAuthorName();
			if(comment.getParentComment() == null) {
				sb.append(translator.translate("review.discussions.comment.by", new String[] { fullName }));
			} else {
				sb.append(translator.translate("review.discussions.reply.to", new String[] { fullName }));	
			}
			sb.append(": ")
			  .append(comment.getText());	
		}
		return split(sb.toString(), 32000);
	}
	
	public static List<String> split(String text, int length) {
		List<String> chunks = new ArrayList<>();
		for( ; text.length() > 0; ) {
			int textLength = text.length();
			int nextChunk = 0;
			if(textLength < length) {
				nextChunk = textLength;
			} else {
				int nextSpace = text.lastIndexOf(" ", length);
				if(nextSpace > (length - (length / 5f))) {
					nextChunk = nextSpace;
				} else {
					nextChunk = length;
				}	
			}
			chunks.add(text.substring(0, nextChunk));
			text = text.substring(nextChunk);
		}
		return chunks;
	}
	
	private void createDiscussionsTextData(OpenXMLWorksheet sheet) {
		int rcnt = applicationsDataModel.getRowCount();
		for(int i=0; i<rcnt; i++) {
			ApplicationRow appRow = applicationsDataModel.getObject(i);
			createDiscussionsTextData(sheet, appRow);
		}
	}
	
	private void createDiscussionsTextHeader(OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
		Row headerRow = sheet.newRow();
		sheet.setHeaderRows(1);
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("edit.application.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.firstName"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.lastName"), workbook.getStyles().getHeaderStyle());
		
		int reviewerNumber = 1;
		for(Reviewer reviewer:reviewers) {
			String reviewerLabel;
			if(reviewerNameVisibility == ReviewerNameVisibilityEnum.anonymous) {
				reviewerLabel = Integer.toString(reviewerNumber++);
			} else {
				reviewerLabel = reviewer.getFullName();
			}
			
			Integer cols = numOfColumns.get(reviewer);
			if(cols != null && cols.intValue() > 1) {
				for(int i=0; i<cols.intValue(); i++) {
					String[] partArgs = new String[] { reviewerLabel, Integer.toString(i + 1) };
					headerRow.addCell(pos++, translator.translate("table.excel.header.review.discussion.part", partArgs), workbook.getStyles().getHeaderStyle());
				}
			} else {
				String[] args = new String[] { reviewerLabel };
				headerRow.addCell(pos++, translator.translate("table.excel.header.review.discussion", args), workbook.getStyles().getHeaderStyle());
			}
		}
	}
	
	private void createReviewTextHeader(OpenXMLWorksheet sheet, ReviewElementDefinition definition, OpenXMLWorkbook workbook) {
		Row headerRow = sheet.newRow();
		sheet.setHeaderRows(1);
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("edit.application.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.firstName"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.lastName"), workbook.getStyles().getHeaderStyle());
		
		int reviewerNumber = 1;
		for(Reviewer reviewer:reviewers) {
			String reviewerLabel;
			if(reviewerNameVisibility == ReviewerNameVisibilityEnum.anonymous) {
				reviewerLabel = "review " + (reviewerNumber++);
			} else {
				reviewerLabel = reviewer.getFullName();
			}
			
			String[] args = new String[] { definition.getLabel(), reviewerLabel };
			headerRow.addCell(pos++, translator.translate("table.excel.header.review.text", args), workbook.getStyles().getHeaderStyle());
		}
	}
	
	private void createReviewTextData(OpenXMLWorksheet sheet, ApplicationRow application, ApplicationStatistics appStatistics, ReviewElementDefinition definition) {
		Row row = sheet.newRow();

		int pos = 0;
		row.addCell(pos++, application.getApplication().getId(), null);
		row.addCell(pos++, application.getApplication().getPerson().getFirstName());
		row.addCell(pos++, application.getApplication().getPerson().getLastName());
		
		if(appStatistics != null) {
			for(Reviewer reviewer:reviewers) {
				ApplicationTextCollectionElement el = appStatistics.getTextCollectionElement(reviewer, definition);
				if(el == null) {
					pos++;
				} else {
					row.addCell(pos++, el.getText());
				}
			}
		}
	}
	
	private void createReviewTextData(OpenXMLWorksheet sheet,  ReviewElementDefinition definition) {
		List<ApplicationStatistics> appStatistics = positionStatistics.getApplicationsStatistics();
		Map<Long,ApplicationStatistics> appStatisticsMap = appStatistics.stream()
				.collect(Collectors.toMap(ApplicationStatistics::getKey, Function.identity()));
		
		int rcnt = applicationsDataModel.getRowCount();
		for(int i=0; i<rcnt; i++) {
			ApplicationRow appRow = applicationsDataModel.getObject(i);
			ApplicationStatistics appStats = appStatisticsMap.get(appRow.getApplication().getKey());
			createReviewTextData(sheet, appRow, appStats, definition);
		}
	}

	@Override
	protected void createHeader(Row headerRow, int pos, OpenXMLWorkbook workbook) {
		if(!settings.isWithReviewSliders()) return;
		
		for(ReviewElementDefinition definition:reviewElementsDefinitions) {
			if(definition .getType() == ReviewElementType.slider) {
				String label = definition.getLabel();
				String[] labelArgs = new String[] { label };
				headerRow.addCell(pos++, translator.translate("table.excel.header.sum", labelArgs), workbook.getStyles().getHeaderStyle());
				headerRow.addCell(pos++, translator.translate("table.excel.header.average", labelArgs), workbook.getStyles().getHeaderStyle());
				headerRow.addCell(pos++, translator.translate("table.excel.header.min", labelArgs), workbook.getStyles().getHeaderStyle());
				headerRow.addCell(pos++, translator.translate("table.excel.header.max", labelArgs), workbook.getStyles().getHeaderStyle());
				headerRow.addCell(pos++, translator.translate("table.excel.header.sd", labelArgs), workbook.getStyles().getHeaderStyle());
				headerRow.addCell(pos++, translator.translate("table.excel.header.number.reviews", labelArgs), workbook.getStyles().getHeaderStyle());
			}
		}
	}
	
	@Override
	protected void createData(OpenXMLWorksheet exportSheet, Row dataRow, int row, int pos, OpenXMLWorkbook workbook) {
		if(!settings.isWithReviewSliders()) return;
		
		ApplicationRow appRow = applicationsDataModel.getObject(row);
		ApplicationStatistics appStatistics = positionStatistics.getApplicationStatistics(appRow);

		for(ReviewElementDefinition definition:reviewElementsDefinitions) {
			if(definition .getType() == ReviewElementType.slider) {
				ApplicationStatisticElement  stats = appStatistics == null ? null : appStatistics.getStatisticsElement(definition);
				if(stats == null) {
					pos += 6;
				} else {
					dataRow.addCell(pos++, stats.getSum(), null);
					dataRow.addCell(pos++, stats.getAverage(), null);
					dataRow.addCell(pos++, stats.getMin(), null);
					dataRow.addCell(pos++, stats.getMax(), null);
					dataRow.addCell(pos++, stats.getStandardDeviation(), null);
					dataRow.addCell(pos++, stats.getNumOfReviews(), null);
				}
			}
		}
	}
	
	public static class ListSettings {

		private boolean withReviewSliders;
		private boolean withReviewTexts;
		private boolean withReviewDiscussions;
		
		public boolean isWithReviewSliders() {
			return withReviewSliders;
		}
		
		public void setWithReviewSliders(boolean withReviewSliders) {
			this.withReviewSliders = withReviewSliders;
		}
		
		public boolean isWithReviewTexts() {
			return withReviewTexts;
		}
		
		public void setWithReviewTexts(boolean withReviewTexts) {
			this.withReviewTexts = withReviewTexts;
		}
		
		public boolean isWithReviewDiscussions() {
			return withReviewDiscussions;
		}
		
		public void setWithReviewDiscussions(boolean withReviewDiscussions) {
			this.withReviewDiscussions = withReviewDiscussions;
		}
		
		public boolean withReviews() {
			return withReviewDiscussions || withReviewTexts || withReviewSliders;
		}
	}
}
