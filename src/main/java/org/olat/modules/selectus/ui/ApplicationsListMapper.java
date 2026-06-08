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
package org.olat.modules.selectus.ui;

import static org.olat.modules.selectus.ui.RecruitingHelper.getPositionDerivedFilename;
import static org.olat.modules.selectus.ui.RecruitingHelper.normalizeFilename;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRefereeStats;
import org.olat.modules.selectus.model.DecisionRubric;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRole;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.comparator.IdComparator;
import org.olat.modules.selectus.ui.comparator.LastnameComparator;
import org.olat.modules.selectus.ui.comparator.PositionApplicationsPDFComparator;
import org.olat.modules.selectus.ui.rating.UserMapperCommitteeRatingComparator;
import org.olat.modules.selectus.ui.resources.FOPMediaResource;
import org.olat.modules.selectus.ui.resources.ExcelFlexiTableResource;
import org.olat.modules.selectus.ui.resources.ReviewStatisticsExcelResources;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  7 mar. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationsListMapper implements Mapper  {
	
	private static final Logger log = Tracing.createLoggerFor(ApplicationsListMapper.class);
	
	private final Locale locale;
	private final Translator translator;
	private Position position;
	private final Identity identity;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;

	public ApplicationsListMapper(Identity identity, Position position, RecruitingPositionSecurityCallback secCallback,
			Locale locale, Translator translator) {
		this.locale = locale;
		this.identity = identity;
		this.position = position;
		this.translator = translator;
		this.secCallback = secCallback;
		CoreSpringFactory.autowireObject(this);
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		if(relPath.startsWith("/")) {
			relPath = relPath.substring(1, relPath.length());
		}
		
		int positionKeyIndex = relPath.indexOf('/');
		if(positionKeyIndex < 0) return new NotFoundMediaResource();
		String positionKeyStr = relPath.substring(0, positionKeyIndex);
		Long positionKey;
		try {
			positionKey = Long.valueOf(positionKeyStr);
		} catch (NumberFormatException e) {
			return new NotFoundMediaResource();
		}
		if(position == null || !position.getKey().equals(positionKey)) {
			return new NotFoundMediaResource();
		}
		
		String derivedFilename = getPositionDerivedFilename(position, locale);
		position = erFrontendManager.getPosition(position.getKey());
		List<ApplicationLight> applications = erFrontendManager.getApplications(position);

		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<IdentityRef> committee = erFrontendManager.getCommitteeRefs(position, ratingRoles);
		
		if(relPath.endsWith("_apps.pdf")) {
			// List / My rating (PDF)
			try {
				List<UserRating> ratings = erFrontendManager.getRatings(position, Collections.singletonList(identity));
				Collections.sort(applications, new CommitteeRatingComparator(ratings));
				PositionApplicationsPDFDataModel model
					= new PositionApplicationsPDFDataModel(identity, position, applications, ratings, translator);
				FOPMediaResource resource =  new FOPTableExport().exportApplications(identity, position, model, locale);
				resource.setFilename(normalizeFilename(derivedFilename) + "_apps");
				return resource;
			} catch (Exception e) {
				log.error("Cannot export table in PDF (FOP)", e);
			}
		} else if(relPath.endsWith("_staff.pdf")) {
			// Applications list (PDF) -> for staff
			try {
				Collections.sort(applications, new PositionApplicationsPDFComparator(position.getApplicationDeadline()));
				List<UserRating> ratings = erFrontendManager.getRatings(position, committee);
				PositionApplicationsPDFDataModel model
					= new PositionApplicationsPDFDataModel(identity, position, applications, ratings, translator);
				FOPMediaResource resource =  new FOPTableExport().exportApplicationsForStaff(null, position, model, locale);
				resource.setFilename(normalizeFilename(derivedFilename) + "_apps");
				return resource;
			} catch (Exception e) {
				log.error("Cannot export table in PDF (FOP)", e);
			}
		} else if(relPath.endsWith("_ratings.pdf")) {
			// Ratings (PDF)
			try {
				List<UserRating> ratings = erFrontendManager.getRatings(position, committee);
				int numOfCommitteeMembers = committee.size();
				Collections.sort(applications, new CommitteeRatingComparator(ratings));

				PositionRatingsPDFDataModel model
					= new PositionRatingsPDFDataModel(numOfCommitteeMembers, applications, ratings, translator);
				FOPMediaResource resource =  new FOPTableExport().exportRatings(null, position, model, locale);
				resource.setFilename(normalizeFilename(derivedFilename) + "_ratings");
				return resource;
			} catch (Exception e) {
				log.error("Cannot export table in PDF (FOP)", e);
			}
		} else if (relPath.endsWith("_applications.xls") || relPath.endsWith("_applications.xlsx")) {
			try {
				return getApplicationListExcel(derivedFilename);
			} catch (Exception e) {
				log.error("Cannot export table in Excel", e);
			}
		} else if (relPath.endsWith("_reviews_statistics.xls") || relPath.endsWith("_reviews_statistics.xlsx")) {
			try {
				return getReviewsStatisticsExcel(derivedFilename);
			} catch (Exception e) {
				log.error("Cannot export table in Excel", e);
			}
		}
		return null;
	}
	
	/**
	 * An Excel file with all informations about applicants,
	 * 
	 * @param derivedFilename
	 * @return
	 * @throws Exception
	 */
	private MediaResource getApplicationListExcel(String derivedFilename) throws Exception {
		//rubric
		List<DecisionRubricDefinition> definitions = null;
		List<DecisionRubric> rubrics = null; 
		if(position.isDecisionTool()) {
			definitions = erFrontendManager.getDecisionRubricDefinition(position);
			rubrics = erFrontendManager.getDecisionRubric(position);
		}
		
		PositionRole[] ratingRoles = recruitingModule.getRolesAllowedToRate();
		List<IdentityRef> committee = erFrontendManager.getCommitteeRefs(position, ratingRoles);
		List<UserRating> ratings = erFrontendManager.getRatings(position, committee);
		List<ApplicationLight> applications = erFrontendManager.getApplications(position);
		
		Map<Long,ApplicationRefereeStats> appKeyToReviewerStats = new HashMap<>();
		if(recruitingModule.isReferenceEnabled()
				&& (position.isExpertRecommendationEnabled() || position.isRefereeRecommendationEnabled() || position.isComparativeAssessmentExpertEnabled())) {
			List<ApplicationRefereeStats> reviewerStats = erFrontendManager.getApplicationReviewerStats(position);
			for(ApplicationRefereeStats stats:reviewerStats) {
				appKeyToReviewerStats.put(stats.getKey(), stats);
			}
		}
		
		Map<Long,List<ApplicationCategoryInfos>> appToCategories = taggingService.getApplicationToCategories(position,
				secCallback.canSeeApplicationAdministrativeCategories());
		
		PositionApplicationsExcelDataModel dataModel =  new PositionApplicationsExcelDataModel(committee.size(),
				position, applications, ratings, appKeyToReviewerStats, definitions, rubrics, appToCategories,
				secCallback, translator);
		String filename = normalizeFilename(derivedFilename) + "_applications.xlsx";
		return new ExcelFlexiTableResource(filename, dataModel, translator);
	}
	
	private MediaResource getReviewsStatisticsExcel(String derivedFilename) {
		return new ReviewStatisticsExcelResources(derivedFilename + ".xlsx", position, identity, secCallback, translator);
	}
	
	public static class CommitteeRatingComparator implements Comparator<ApplicationLight> {

		private final List<UserRating> ratings;
		private final IdComparator idComparator = new IdComparator();
		private final LastnameComparator lastnameComparator = new LastnameComparator();
		private final UserMapperCommitteeRatingComparator committeeComparator = new UserMapperCommitteeRatingComparator();
		
		public CommitteeRatingComparator(List<UserRating> ratings) {
			this.ratings = ratings;
		}
		
		@Override
		public int compare(ApplicationLight o1, ApplicationLight o2) {
			if(o1 == null || o2 == null) {
				if(o1 == null && o2 == null) return 0;
				return o1 == null ? -1 : 1;
			}
			
			UserRatingMapper m1 = getUserRatings(o1);
			UserRatingMapper m2 = getUserRatings(o2);
			int c = -committeeComparator.compare(m1, m2);
			if(c == 0) {
				c = lastnameComparator.compare(o1, o2);
			}
			if(c == 0) {
				c = idComparator.compare(o1, o2);
			}
			return c;
		}
		
		private UserRatingMapper getUserRatings(ApplicationLight app) {
			List<UserRating> appRatings = new ArrayList<>();

			String resSubPath = app.getKey().toString();
			for(UserRating rating:ratings) {
				if(resSubPath.equals(rating.getResSubPath())) {
					appRatings.add(rating);
				}
			}
			
			UserRatingMapper m = new UserRatingMapper(app);
			m.setRatings(appRatings);
			return m;
		}
	}
}