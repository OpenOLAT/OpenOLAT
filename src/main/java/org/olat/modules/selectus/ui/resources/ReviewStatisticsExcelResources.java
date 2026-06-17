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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.CommentService;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
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

/**
 * 
 * Initial date: 19 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewStatisticsExcelResources extends OpenXMLWorkbookResource {

	private static final Logger log = Tracing.createLoggerFor(ReviewStatisticsExcelResources.class);
	private final Translator translator;
	private final Identity identity;
	private final PositionRef positionRef;
	private ReviewerNameVisibilityEnum reviewerNameVisibility;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private DB dbInstance;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private RecruitingService recruitingService;
	
	public ReviewStatisticsExcelResources(String name, PositionRef positionRef,
			Identity identity, RecruitingPositionSecurityCallback secCallback, Translator translator) {
		super(name);
		CoreSpringFactory.autowireObject(this);
		this.translator = translator;
		this.identity = identity;
		this.positionRef = positionRef;
		this.secCallback = secCallback;
	}

	@Override
	protected void generate(OutputStream out) {
		Position position = recruitingService.getPosition(positionRef.getKey());
		List<ApplicationLight> applications = recruitingService.getApplications(position);
		PositionReviewDefinition reviewDefinition = position.getReviewDefinition();
		reviewerNameVisibility = reviewDefinition.getReviewNameVisibility();
		List<ReviewElementDefinition> definitions = reviewDefinition.getElements().stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		PositionStatistics positionStatistics = reviewService.getReviewStatistics(position, identity, secCallback);
		PositionComments positionComments = null;
		if(reviewDefinition.isReviewCommentEnabled()) {
			positionComments = commentService.getComments(position);
		}

		dbInstance.commitAndCloseSession();
		
		List<Reviewer> reviewers = positionStatistics.getReviewers();
		
		List<String> sheetNames = new ArrayList<>();
		sheetNames.add(translator.translate("review.sliders.statistics"));
		for(ReviewElementDefinition definition:definitions) {
			if(definition.getType() == ReviewElementType.text) {
				sheetNames.add(definition.getLabel());
			}	
		}
		if(reviewDefinition.isReviewCommentEnabled()) {
			sheetNames.add(translator.translate("review.discussions"));
		}

		try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, sheetNames.size(), sheetNames)) {
			// review sliders
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			createHeader(sheet, definitions, workbook);
			createData(sheet, applications, positionStatistics, definitions);
			
			//  review texts
			for(ReviewElementDefinition definition:definitions) {
				if(definition.getType() == ReviewElementType.text) {
					OpenXMLWorksheet textSheet = workbook.nextWorksheet();
					textSheet.setHeaderRows(1);
					createReviewTextHeader(textSheet, definition, reviewers, workbook);
					createReviewTextData(textSheet, applications, positionStatistics, reviewers, definition);
				}
			}
			
			// discussion
			if(reviewDefinition.isReviewCommentEnabled()) {
				OpenXMLWorksheet discussionSheet = workbook.nextWorksheet();
				createDiscussionsTextHeader(discussionSheet, reviewers, workbook);
				createDiscussionsTextData(discussionSheet, applications, positionComments, reviewers);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void createDiscussionsTextHeader(OpenXMLWorksheet sheet, List<Reviewer> reviewers, OpenXMLWorkbook workbook) {
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
			String[] args = new String[] { reviewerLabel };
			headerRow.addCell(pos++, translator.translate("table.excel.header.review.discussion", args), workbook.getStyles().getHeaderStyle());
		}
	}
	
	private void createDiscussionsTextData(OpenXMLWorksheet sheet, ApplicationLight application, PositionComments positionComments, List<Reviewer> reviewers) {
		Row row = sheet.newRow();

		int pos = 0;
		row.addCell(pos++, application.getId(), null);
		row.addCell(pos++, application.getPerson().getFirstName());
		row.addCell(pos++, application.getPerson().getLastName());

		for(Reviewer reviewer:reviewers) {
			ApplicationReviewCommentKey appReviewKey = new ApplicationReviewCommentKey(application.getKey(), reviewer.getKey());
			ApplicationReviewComments el = positionComments.getCommentsMap().get(appReviewKey);
			if(el == null) {
				pos++;
			} else {
				String comments = toString(el.getComments());
				row.addCell(pos++, comments);
			}
		}
	}
	
	private String toString(List<ApplicationReviewComment> comments) {
		StringBuilder sb = new StringBuilder(2048);
		for(ApplicationReviewComment comment:comments) {
			String fullName = comment.getAuthorName();
			if(comment.getParentComment() == null) {
				sb.append(translator.translate("review.discussions.comment.by", new String[] { fullName }));
			} else {
				sb.append(translator.translate("review.discussions.reply.to", new String[] { fullName }));	
			}
			sb.append(": ")
			  .append(comment.getText())
			  .append("\r");
		}
		return sb.toString();
	}
	
	private void createDiscussionsTextData(OpenXMLWorksheet sheet, List<ApplicationLight> applications, PositionComments positionComments, List<Reviewer> reviewers) {
		for(ApplicationLight application:applications) {
			createDiscussionsTextData(sheet, application, positionComments, reviewers);
		}
	}
	
	private void createReviewTextHeader(OpenXMLWorksheet sheet, ReviewElementDefinition definition, List<Reviewer> reviewers, OpenXMLWorkbook workbook) {
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
				reviewerLabel = "review " + Integer.toString(reviewerNumber++);
			} else {
				reviewerLabel = reviewer.getFullName();
			}
			
			String[] args = new String[] { definition.getLabel(), reviewerLabel };
			headerRow.addCell(pos++, translator.translate("table.excel.header.review.text", args), workbook.getStyles().getHeaderStyle());
		}
	}
	
	private void createReviewTextData(OpenXMLWorksheet sheet, ApplicationLight application, ApplicationStatistics appStatistics, List<Reviewer> reviewers, ReviewElementDefinition definition) {
		Row row = sheet.newRow();

		int pos = 0;
		row.addCell(pos++, application.getId(), null);
		row.addCell(pos++, application.getPerson().getFirstName());
		row.addCell(pos++, application.getPerson().getLastName());
		
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
	
	private void createReviewTextData(OpenXMLWorksheet sheet, List<ApplicationLight> applications, PositionStatistics positionStatistics, List<Reviewer> reviewers, ReviewElementDefinition definition) {
		List<ApplicationStatistics> appStatistics = positionStatistics.getApplicationsStatistics();
		Map<Long,ApplicationStatistics> appStatisticsMap = appStatistics.stream()
				.collect(Collectors.toMap(ApplicationStatistics::getKey, Function.identity()));
		for(ApplicationLight application:applications) {
			ApplicationStatistics appStats = appStatisticsMap.get(application.getKey());
			createReviewTextData(sheet, application, appStats, reviewers, definition);
		}
	}

	private void createHeader(OpenXMLWorksheet sheet, List<ReviewElementDefinition> definitions, OpenXMLWorkbook workbook) {
		Row headerRow = sheet.newRow();
		sheet.setHeaderRows(1);
		
		int pos = 0;
		headerRow.addCell(pos++, translator.translate("edit.application.id"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.firstName"), workbook.getStyles().getHeaderStyle());
		headerRow.addCell(pos++, translator.translate("edit.application.lastName"), workbook.getStyles().getHeaderStyle());

		for(ReviewElementDefinition definition:definitions) {
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
	
	private void createData(OpenXMLWorksheet sheet, ApplicationLight application, ApplicationStatistics appStatistics, List<ReviewElementDefinition> definitions) {
		
		Row row = sheet.newRow();

		int pos = 0;
		row.addCell(pos++, application.getId(), null);
		row.addCell(pos++, application.getPerson().getFirstName());
		row.addCell(pos++, application.getPerson().getLastName());

		for(ReviewElementDefinition definition:definitions) {
			if(definition .getType() == ReviewElementType.slider) {
				ApplicationStatisticElement  stats = appStatistics == null ? null : appStatistics.getStatisticsElement(definition);
				if(stats == null) {
					pos += 6;
				} else {
					row.addCell(pos++, stats.getSum(), null);
					row.addCell(pos++, stats.getAverage(), null);
					row.addCell(pos++, stats.getMin(), null);
					row.addCell(pos++, stats.getMax(), null);
					row.addCell(pos++, stats.getStandardDeviation(), null);
					row.addCell(pos++, stats.getNumOfReviews(), null);
				}
			}
		}
	}
	
	private void createData(OpenXMLWorksheet sheet, List<ApplicationLight> applications, PositionStatistics positionStatistics, List<ReviewElementDefinition> definitions) {

		List<ApplicationStatistics> appStatistics = positionStatistics.getApplicationsStatistics();
		Map<Long,ApplicationStatistics> appStatisticsMap = appStatistics.stream()
				.collect(Collectors.toMap(ApplicationStatistics::getKey, Function.identity()));
		for(ApplicationLight application:applications) {
			ApplicationStatistics appStats = appStatisticsMap.get(application.getKey());
			createData(sheet, application, appStats, definitions);
		}
	}
}
