/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.chart.RadarChartComponent;
import org.olat.core.gui.components.chart.RadarChartElement;
import org.olat.core.gui.components.chart.RadarSeries;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.CommentService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationComment;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.review.ApplicationCommentVoteStatistics;
import org.olat.modules.selectus.model.review.ApplicationStatisticElement;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewElementType;
import org.olat.modules.selectus.model.review.ReviewResponse;
import org.olat.modules.selectus.model.review.ReviewerNameVisibilityEnum;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.review.ReviewEditController.ResponseComparator;

/**
 * 
 * Initial date: 4 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewsController extends FormBasicController implements Activateable2 {
	
	private static final String[] colors = new String[]{
			"#EDC951", "#CC333F", "#00A0B0", "#4E4E6C", "#8DC1A1",
			"#F7BC00", "#BB6511", "#B28092", "#003D40", "#FF69D1"
		};
	
	private FormLink orderButton;
	
	private CloseableModalController cmc;
	private ReviewEditController editReviewCtrl;
	private DialogBoxController confirmDeleteCtrl;
	private DialogBoxController confirmDeleteCommentCtrl;
	
	private int count = 0;
	private Position position;
	private Application application;
	private final List<Identity> reviewCommittee;
	private List<ApplicationReview> reviews;
	private final PositionReviewDefinition reviewDefinition;
	private final List<ReviewElementDefinition> elementDefinitions;
	private final RecruitingPositionSecurityCallback secCallback;
	private final Map<Identity,String> reviewersToColors = new HashMap<>();

	private final boolean statisticsZeroBased;
	private Boolean openStatistics = Boolean.TRUE;
	private final Map<Identity,Boolean> openReviews = new HashMap<>();
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private CommentService commentService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RecruitingModule recruitingModule;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationReviewsController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, List<Identity> reviewCommittee,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "reviews", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		this.reviewCommittee = reviewCommittee;
		statisticsZeroBased = recruitingModule.isReviewStatisticsZeroBased();

		reviewDefinition = reviewService.getReviewDefinition(position.getReviewDefinition());
		elementDefinitions = new ArrayList<>(reviewDefinition.getElements());
		reviewDefinition.getReviewNameVisibility();

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		orderButton = uifactory.addFormLink("order", "order.desc", null, formLayout, Link.BUTTON);
		orderButton.setIconLeftCSS("o_icon o_icon_toggle_on");
		orderButton.setUserObject(OrderReview.desc);
	}
	
	public boolean hasReviewed() {
		for(ApplicationReview review:reviews) {
			if(review.getReviewer().equals(getIdentity())) {
				return !review.isEmpty();
			}
		}
		return false;
	}
	
	public void loadModel() {
		List<ReviewResponse> applicationResponses = reviewService.getResponses(application);
		boolean hasReviewed = false;
		for(ReviewResponse applicationResponse:applicationResponses) {
			if(applicationResponse.getReviewer().equals(getIdentity())
					&& (applicationResponse.getIntegerValue() != null || StringHelper.containsNonWhitespace(applicationResponse.getStringValue()))) {
				hasReviewed = true;
				break;
			}
		}

		boolean canViewReview = secCallback.canViewReviews(hasReviewed);
		//this will set a review number
		reviews = loadReviewsModel(applicationResponses, canViewReview);
		loadComments(reviews);
		sortModel(reviews);
		if(canViewReview) {
			loadStatisticsModel(applicationResponses, reviews);
		}
		
		flc.contextPut("noReviewsAvailable", Boolean.valueOf(reviews.isEmpty()));
		flc.contextPut("hasReviewed", Boolean.valueOf(hasReviewed));
		flc.contextPut("visibleNow", Boolean.valueOf(canViewReview));
		flc.contextPut("visibleAfterSubmission", secCallback.canViewReviewAfterSubmission());
		flc.contextPut("visibleAfterRating", secCallback.canViewReviewAfterRating());
	}

	private void loadStatisticsModel(List<ReviewResponse> applicationResponses, List<ApplicationReview> reviews) {
		Map<Identity,ApplicationReview> identityToReview = new HashMap<>();
		for(ApplicationReview review:reviews) {
			identityToReview.put(review.getReviewer(), review);
		}

		Set<Identity> reviewersSet = new HashSet<>(reviewCommittee);
		Map<ReviewElementDefinition,ApplicationStatisticElement> definitionToStatistics = new HashMap<>();
		
		Set<Identity> reviewers = new HashSet<>();
		for(ReviewResponse applicationResponse:applicationResponses) {
			Identity reviewer = applicationResponse.getReviewer();
			if(reviewersSet.contains(reviewer) && applicationResponse.getElement().getType() == ReviewElementType.slider) {
				ApplicationStatisticElement appReview = definitionToStatistics
					.computeIfAbsent(applicationResponse.getElement(), ApplicationStatisticElement::new);
				
				Integer val = applicationResponse.getIntegerValue();
				if(val != null && val.intValue() < ReviewElementDefinition.MIN_SLIDER_VALUE) {
					val = (int)Math.round(ReviewElementDefinition.MIN_SLIDER_VALUE);
				}
				if(val != null && statisticsZeroBased) {
					val = val.intValue() - 1;
				}
				appReview.addValue(val);
				reviewers.add(applicationResponse.getReviewer());
			}
		}
		
		int colorCount = 0;
		for(Identity evaluator:reviewers) {
			int i = (colorCount++) % colors.length;
			reviewersToColors.put(evaluator, colors[i]);
		}
		
		List<String> axisList = new ArrayList<>();
		Set<Identity> activeReviewers = new HashSet<>();
		List<ApplicationStatisticElement> statistics = new ArrayList<>();

		Map<Identity,RadarSeries> series = new HashMap<>();
		for(ReviewElementDefinition elementDefinition:elementDefinitions) {
			if(elementDefinition == null) continue;

			ApplicationStatisticElement appReview = definitionToStatistics.get(elementDefinition);
			String axis = elementDefinition.getLabel();
			if(appReview != null) {
				statistics.add(appReview);
				axisList.add(axis);
			} else if(elementDefinition.getType() == ReviewElementType.title) {
				statistics.add(new ApplicationStatisticElement(elementDefinition));
			}
			
			for(ReviewResponse applicationResponse:applicationResponses) {
				Identity reviewer = applicationResponse.getReviewer();
				if(reviewersSet.contains(reviewer) && applicationResponse.getElement().equals(elementDefinition)) {
					activeReviewers.add(applicationResponse.getReviewer());
					if(applicationResponse.getElement().getType() == ReviewElementType.slider) {
						RadarSeries serie = series.computeIfAbsent(applicationResponse.getReviewer(), id -> {
							ApplicationReview r = identityToReview.get(applicationResponse.getReviewer());
							StringBuilder legend = new StringBuilder(128);
							legend.append(translate("review.numbered", new String[] { Integer.toString(r.getReviewNumber()) } ));
							if(reviewDefinition.getReviewNameVisibility() == ReviewerNameVisibilityEnum.visible) {
								legend.append(" (").append(RecruitingHelper.formatLastnameFirstName(id)).append(")");
							}
							String color = reviewersToColors.get(id);
							return new RadarSeries(legend.toString(), color);
						});
	
						if(applicationResponse.getIntegerValue() != null ) {
							double value = applicationResponse.getIntegerValue().doubleValue();
							if(value < ReviewElementDefinition.MIN_SLIDER_VALUE) {
								value = ReviewElementDefinition.MIN_SLIDER_VALUE;
							}
							if(statisticsZeroBased) {
								value -= 1.0d;
							}
							serie.addPoint(axis, value);
						}
					}
				}
			}
		}
		flc.contextPut("statisticElements", statistics);
		flc.contextPut("numOfReviewers", reviewCommittee.size());
		flc.contextPut("numOfReviews", activeReviewers.size());
		boolean viewStatistics = (reviewDefinition.getReviewStatisticsEnabled() == null && recruitingModule.isReviewStatisticsEnabled())
				|| (reviewDefinition.getReviewStatisticsEnabled() != null && reviewDefinition.getReviewStatisticsEnabled().booleanValue());
		flc.contextPut("viewStatistics", Boolean.valueOf(viewStatistics));
		
		boolean viewRadarChart = (reviewDefinition.getReviewRadarChartEnabled() == null && recruitingModule.isReviewStatisticsChartEnabled())
				|| (reviewDefinition.getReviewRadarChartEnabled() != null && reviewDefinition.getReviewRadarChartEnabled().booleanValue());
		if(axisList.size() > 2 && reviewDefinition.getDefaultSliderSteps() != null && viewRadarChart) {
			RadarChartElement radarEl = new RadarChartElement("spiderChart");
			Collection<RadarSeries> seriesList = series.values();
			radarEl.setSeries(new ArrayList<>(seriesList));
			radarEl.setShowLegend(false);
			radarEl.setAxis(axisList);
			
			int numOflevels = reviewDefinition.getDefaultSliderSteps();
			if(statisticsZeroBased) {
				numOflevels--;
			}
			radarEl.setLevels(numOflevels);
			radarEl.setMaxValue(numOflevels);
			radarEl.setFormat(RadarChartComponent.Format.integer);
			
			flc.add("spiderChart", radarEl);
		}
	}
	
	private List<ApplicationReview> loadReviewsModel(List<ReviewResponse> applicationResponses, boolean canViewReview) {
		Map<Identity, ApplicationReview> identityToReviews = new HashMap<>();
		Map<Identity,Integer> identityToReviewNumber = loadReviewNumbers(applicationResponses);
		for(ReviewResponse applicationResponse:applicationResponses) {
			if(canViewReview || applicationResponse.getReviewer().equals(getIdentity())) {
				ApplicationReview appReview = identityToReviews
						.computeIfAbsent(applicationResponse.getReviewer(), id -> {
							String fullName = RecruitingHelper.formatLastnameFirstName(id);
							return new ApplicationReview(id, fullName, id.getKey().toString());
						});
				appReview.addResponse(applicationResponse);
			}
		}
		
		boolean canReview = secCallback.canReview(application);
		List<ApplicationReview> reviewList = new ArrayList<>();
		for(Map.Entry<Identity,ApplicationReview> entry:identityToReviews.entrySet()) {
			ApplicationReview review = entry.getValue();
			review.setOpen(getIdentity().equals(entry.getKey()));
			Integer number = identityToReviewNumber.get(entry.getKey());
			review.setReviewNumber(number == null ? ++count : number.intValue());
			reviewList.add(review);
			
			List<ReviewResponse> responses = review.getResponses();
			Map<ReviewElementDefinition,ReviewResponse> definitionToResponses = new HashMap<>();
			for(ReviewResponse response:responses) {
				definitionToResponses.put(response.getElement(), response);
			}
			
			boolean hasLabel = false;
			if(canReview && entry.getKey().equals(getIdentity())) {
				review.setEditButton(createEditButton());
			}
			if(secCallback.canDeleteReviews() || (canReview && entry.getKey().equals(getIdentity()))) {
				review.setDeleteButton(createDeleteButton(review));
			}

			for(ReviewElementDefinition elementDefinition:elementDefinitions) {
				if(elementDefinition == null) continue;
				
				ReviewResponse response = definitionToResponses.get(elementDefinition);
				ApplicationReviewElement reviewElement = new ApplicationReviewElement(elementDefinition, response);
				review.addElement(reviewElement);

				if(elementDefinition.getType() == ReviewElementType.text) {
					FormItem item = initTextElement(elementDefinition, response, flc);
					reviewElement.setItem(item);
					hasLabel = false;
				} else if(elementDefinition.getType() == ReviewElementType.title) {
					hasLabel = false;
				} else if(elementDefinition.getType() == ReviewElementType.slider) {
					FormItem item = initSliderElement(elementDefinition, response, flc);
					if(item == null) {
						continue;
					}
					reviewElement.setItem(item);
					if(!hasLabel) {
						reviewElement.setLeftLabel(reviewDefinition.getDefaultSliderLeftLabel());
						reviewElement.setRightLabel(reviewDefinition.getDefaultSliderRightLabel());
					}
					hasLabel = true;
				}
			}
		}
		
		//make sure the reviewer is in the current committee
		Set<Identity> reviewersSet = new HashSet<>(reviewCommittee);
		for(Iterator<ApplicationReview> it=reviewList.iterator(); it.hasNext(); ) {
			if(!reviewersSet.contains(it.next().getReviewer())) {
				it.remove();
			}
		}
		
		// remove empty reviews
		for(Iterator<ApplicationReview> it=reviewList.iterator(); it.hasNext(); ) {
			if(it.next().isEmpty()) {
				it.remove();
			}
		}

		ReviewerNameVisibilityEnum nameVisibility = reviewDefinition.getReviewNameVisibility();
		flc.contextPut("reviewName", Boolean.valueOf(nameVisibility == ReviewerNameVisibilityEnum.visible));
		flc.contextPut("reviews", reviewList);
		return reviewList;
	}
	
	private Map<Identity,Integer> loadReviewNumbers(List<ReviewResponse> applicationResponses) {
		// order is fixed: by creation date
		Collections.sort(applicationResponses, new ReviewResponseComparator());
		
		int idCount = 0;
		Map<Identity,Integer> identityToNumbers = new HashMap<>();
		for(ReviewResponse applicationResponse:applicationResponses) {
			if(!identityToNumbers.containsKey(applicationResponse.getReviewer())) {
				identityToNumbers.put(applicationResponse.getReviewer(), Integer.valueOf(++idCount));
			}
		}

		return identityToNumbers;
	}
	
	private void loadComments(List<ApplicationReview> reviews) {
		if(!reviewDefinition.isReviewCommentEnabled()) return;

		List<ApplicationComment> comments = commentService.getComments(application);
		Map<Identity,List<ApplicationComment>> reviewerToComments = new HashMap<>();
		for(ApplicationComment comment:comments) {
			Identity identity = comment.getReviewer();
			List<ApplicationComment> commentsPerReview = reviewerToComments
					.computeIfAbsent(identity, i -> new ArrayList<>());
			commentsPerReview.add(comment);
		}
		
		List<ApplicationCommentVoteStatistics> votes = commentService.getVotes(application);
		Map<Identity,List<ApplicationCommentVoteStatistics>> reviewerVotes = new HashMap<>();
		for(ApplicationCommentVoteStatistics vote:votes) {
			Identity reviewer = vote.getComment().getReviewer();
			List<ApplicationCommentVoteStatistics> votesPerReview = reviewerVotes
					.computeIfAbsent(reviewer, i -> new ArrayList<>());
			votesPerReview.add(vote);
		}

		for(ApplicationReview review:reviews) {
			List<ApplicationComment> commentList = reviewerToComments.get(review.getReviewer());
			List<ApplicationCommentVoteStatistics> votesStatistics = reviewerVotes.get(review.getReviewer());
			loadReviewComments(review, commentList, votesStatistics);
		}
	}
	
	private void reloadReviewComments(ApplicationReview review) {
		List<ApplicationComment> commentList = commentService.getComments(application, review.getReviewer());
		List<ApplicationCommentVoteStatistics> votes = commentService.getVotes(application, review.getReviewer());
		loadReviewComments(review, commentList, votes);
	}
	
	private void loadReviewComments(ApplicationReview review, List<ApplicationComment> commentList, List<ApplicationCommentVoteStatistics> votes) {
		FormLayoutContainer commentsCont;
		if(review.getCommentsContainer() == null) {
			String page = velocity_root + "/review_comments.html";
			commentsCont = FormLayoutContainer.createCustomFormLayout("comments-" + count++, getTranslator(), page);
			flc.add(commentsCont);
			commentsCont.setRootForm(mainForm);
			review.setCommentsContainer(commentsCont);
		} else {
			commentsCont = review.getCommentsContainer();
		}
		
		review.getComments().clear();
		

		boolean canComment = secCallback.canCommentReview(application);

		if(commentList != null) {
			Map<ApplicationComment,ApplicationCommentVoteStatistics> voteMap = votes.stream()
					.collect(Collectors.toMap(ApplicationCommentVoteStatistics::getComment, v -> v, (u,v) -> u));
			review.setNumOfComments(commentList.size());
			for(ApplicationComment comment:commentList) {
				ApplicationCommentVoteStatistics voteStatistics = voteMap.get(comment);
				review.addComment(forgeComment(review, comment, voteStatistics, canComment, commentsCont));
			}
		} else {
			review.setNumOfComments(0);
		}
		
		if(canComment) {
			review.addComment(forgeNewComment(review, commentsCont));
		}
		
		List<ApplicationReviewComment> reviewComments = review.getComments();
		if(reviewComments.size() > 1) {
			Map<ApplicationComment,ApplicationReviewComment> reviewCommentKeyToReviewComment = reviewComments.stream()
					.collect(Collectors.toMap(ApplicationReviewComment::getApplicationComment, v -> v, (u,v) -> u));
			
			for(ApplicationReviewComment reviewComment:reviewComments) {
				if(reviewComment.getApplicationComment() != null && reviewComment.getApplicationComment().getParentComment() != null) {
					ApplicationComment parentComment = reviewComment.getApplicationComment().getParentComment();
					ApplicationReviewComment parentReviewComment = reviewCommentKeyToReviewComment.get(parentComment);
					reviewComment.setParentComment(parentReviewComment);
				}
			}

			Collections.sort(reviewComments, new ApplicationReviewCommentComparator());
		}
		commentsCont.contextPut("review", review);
	}
	
	private ApplicationReviewComment forgeComment(ApplicationReview review, ApplicationComment comment,
			ApplicationCommentVoteStatistics voteStatistics, boolean canComment, FormLayoutContainer container) {
		ApplicationReviewComment currentComment = new ApplicationReviewComment(review, comment, getTranslator());
		currentComment.setComment(comment.getComment());
		
		if(canComment) {
			//agree / disagree
			FormLink aggreeLink = uifactory.addFormLink("agree-c-" + count++, "agree.comment", "agree.comment", null, container, Link.LINK);
			aggreeLink.setUserObject(currentComment);
			aggreeLink.setIconLeftCSS("o_icon o_icon_agree");
			currentComment.setAgreeLink(aggreeLink);
			
			FormLink disaggreeLink = uifactory.addFormLink("disagree-c-" + count++, "disagree.comment", "disagree.comment", null, container, Link.LINK);
			disaggreeLink.setUserObject(currentComment);
			disaggreeLink.setIconLeftCSS("o_icon o_icon_disagree");
			currentComment.setDisagreeLink(disaggreeLink);
			// reply
			FormLink replyLink = uifactory.addFormLink("reply-c-" + count++, "reply.comment", "reply.comment", null, container, Link.LINK);
			replyLink.setUserObject(currentComment);
			currentComment.setReplyLink(replyLink);
	
			if(getIdentity().equals(comment.getAuthor())) {
				// edit if owner
				FormLink editLink = uifactory.addFormLink("edit-c-" + count++, "edit.comment", "edit", null, container, Link.LINK);
				editLink.setUserObject(currentComment);
				currentComment.setEditLink(editLink);
				// delete if owner
				FormLink deleteLink = uifactory.addFormLink("delete-c-" + count++, "delete.comment", "delete", null, container, Link.LINK);
				deleteLink.setUserObject(currentComment);
				currentComment.setDeleteLink(deleteLink);	
			}
		}
		
		// names of commenter
		currentComment.setAuthor(getCommentsAuthorName(comment));
		if(voteStatistics == null) {
			currentComment.setNumOfAgree(0);
			currentComment.setNumOfDisagree(0);
		} else {
			currentComment.setNumOfAgree(voteStatistics.getUp());
			currentComment.setNumOfDisagree(voteStatistics.getDown());
		}
		return currentComment;
	}
	
	private String getCommentsAuthorName(ApplicationComment comment) {
		Identity identity = null;
		if(getIdentity().equals(comment.getAuthor())) {
			identity = getIdentity();
		} else if(reviewCommittee.contains(comment.getAuthor())) {
			int index = reviewCommittee.indexOf(comment.getAuthor());
			identity = reviewCommittee.get(index);
		} else {
			identity = securityManager.loadIdentityByKey(comment.getAuthor().getKey());
		}
		if(identity != null) {
			return RecruitingHelper.formatFullNameWithTitle(identity, getLocale());
		}
		return null;
	}
	
	private ApplicationReviewComment forgeNewComment(ApplicationReview review, FormLayoutContainer container) {
		ApplicationReviewComment newComment = new ApplicationReviewComment(review, null, getTranslator());
		newComment.setVisible(true);
		FormLink newCommentButton = uifactory.addFormLink("new-c-" + count++, "new.comment", "new.comment", null, container, Link.BUTTON_SMALL);
		FormLink cancelNewCommentButton = uifactory.addFormLink("cancel-new-c-" + count++, "cancel.new.comment", "cancel", null, container, Link.BUTTON_SMALL);
		TextElement commentEl = uifactory.addTextAreaElement("new-comment-text-c" + count++, null, 32000, 4, 60, false, false, false, "", container);
		commentEl.setPlaceholderKey("comment.placeholder", null);
		newComment.setNewCommentEl(commentEl);
		newComment.setNewCommentButton(newCommentButton);
		newCommentButton.setUserObject(newComment);
		newComment.setCancelNewCommentButton(cancelNewCommentButton);
		cancelNewCommentButton.setUserObject(newComment);
		return newComment;
	}

	private void sortModel(List<ApplicationReview> reviews) {
		if(orderButton != null) {
			OrderReview order = (OrderReview)orderButton.getUserObject();
			Collections.sort(reviews, new ApplicationReviewComparator(order));
		}

		for(ApplicationReview review:reviews) {
			Boolean open = openReviews.computeIfAbsent(review.getReviewer(), id -> Boolean.TRUE);
			review.setOpen(open.booleanValue());
		}
		flc.contextPut("openStatistics", openStatistics);
	}
	
	private FormLink createEditButton() {
		return uifactory.addFormLink("edit_" + (++count), "edit", "edit", null, flc, Link.BUTTON_SMALL);
	}
	
	private FormLink createDeleteButton(ApplicationReview review) {
		FormLink deleteButton = uifactory.addFormLink("delete_" + (++count), "delete", "delete", null, flc, Link.BUTTON_SMALL);
		deleteButton.setUserObject(review);
		return deleteButton;
	}
	
	private FormItem initTextElement(ReviewElementDefinition element, ReviewResponse response, FormItemContainer formLayout) {
		if(response == null || !StringHelper.containsNonWhitespace(response.getStringValue())) return null;
		
		String comment = response.getStringValue();
		TextElement textEl = uifactory.addTextAreaElement("text_" + (++count), "label", 2000, 4, 60, false, false, false, comment, formLayout);
		textEl.setUserObject(element);
		textEl.setLabel(element.getLabel(), null, false);
		textEl.setEnabled(false);
		return textEl;
	}
	
	private FormItem initSliderElement(ReviewElementDefinition element, ReviewResponse response, FormItemContainer formLayout) {
		if(reviewDefinition.getDefaultSliderSteps() == null) return null;
		if(response == null || response.getIntegerValue() == null) return null;

		SliderElement sliderEl = uifactory.addSliderElement("slider_" + (++count), "slider", formLayout);
		sliderEl.setDomReplacementWrapperRequired(false);
		sliderEl.setMinValue(ReviewElementDefinition.MIN_SLIDER_VALUE);
		sliderEl.setMaxValue(reviewDefinition.getDefaultSliderSteps().doubleValue());
		sliderEl.setStep(1);
		sliderEl.setEnabled(false);
		sliderEl.setValue(response.getIntegerValue().doubleValue());
		sliderEl.setUserObject(element);
		sliderEl.setLabel(element.getLabel(), null, false);
		return sliderEl;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Comment".equals(type)) {
			activateComment(entries.get(0).getOLATResourceable().getResourceableId());
		} else if("Review".equals(type)) {
			activateReview(entries.get(0).getOLATResourceable().getResourceableId());
		}
	}
	
	private void activateComment(Long commentKey) {
		if(commentKey == null || reviews == null) return;
		
		for(ApplicationReview review:reviews) {
			if(review.getComments() != null) {
				for(ApplicationReviewComment comment:review.getComments()) {
					if(comment.getApplicationComment() != null
							&& commentKey.equals(comment.getApplicationComment().getKey())) {
						comment.setScrollTo(true);
					}
				}
			}
		}
	}
	
	private void activateReview(Long identityKey) {
		if(identityKey == null || reviews == null) return;
		
		for(ApplicationReview review:reviews) {
			if(identityKey.equals(review.getReviewer().getKey())) {
				review.setScrollTo(true);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editReviewCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteReview((ApplicationReview)confirmDeleteCtrl.getUserObject());
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(confirmDeleteCommentCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteComment((ApplicationReviewComment)confirmDeleteCommentCtrl.getUserObject());
			}
		} else if(cmc == source) {
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(editReviewCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		editReviewCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// 
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(orderButton == source) {
			doToggleSort();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("delete".equals(cmd)) {
				doConfirmDelete(ureq, (ApplicationReview)link.getUserObject());
			} else if("edit".equals(cmd)) {
				doEditReview(ureq);
			} else if("new.comment".equals(cmd)) {
				doSaveComment((ApplicationReviewComment)link.getUserObject());
			} else if("cancel.new.comment".equals(cmd)) {
				doCancelComment((ApplicationReviewComment)link.getUserObject());
			} else if("agree.comment".equals(cmd)) {
				doVote((ApplicationReviewComment)link.getUserObject(), true);
			} else if("disagree.comment".equals(cmd)) {
				doVote((ApplicationReviewComment)link.getUserObject(), false);
			} else if("reply.comment".equals(cmd)) {
				doReply((ApplicationReviewComment)link.getUserObject());
			} else if("edit.comment".equals(cmd)) {
				doEdit((ApplicationReviewComment)link.getUserObject());
			} else if("delete.comment".equals(cmd)) {
				doConfirmDelete(ureq, (ApplicationReviewComment)link.getUserObject());
			}
		} else if(source == flc) {
			if("ONCLICK".equals(event.getCommand())) {
				String cmd = ureq.getParameter("fcid");
				String panel = ureq.getParameter("panel");
				doSavePanel(cmd, panel);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		if(fe == null) {
			super.propagateDirtinessToContainer(fiSrc, fe);
		}
	}

	private void doVote(ApplicationReviewComment reviewComment, boolean agree) {
		ApplicationReview review = reviewComment.getReview();
		commentService.vote(reviewComment.getApplicationComment(), getIdentity(), agree);
		reloadReviewComments(review);
	}
	
	private void doReply(ApplicationReviewComment reviewComment) {
		forgeEdit(reviewComment, null);
		reviewComment.setReply(true);
	}
	
	private void doEdit(ApplicationReviewComment reviewComment) {
		forgeEdit(reviewComment, reviewComment.getComment());
		reviewComment.setEdit(true);
	}
	
	/**
	 * Method to activate the edit area of an existent comment.
	 * 
	 * @param reviewComment The comment wrapper to edit
	 * @param comment The text to edit
	 */
	private void forgeEdit(ApplicationReviewComment reviewComment, String comment) {
		ApplicationReview review = reviewComment.getReview();
		FormLayoutContainer container = review.getCommentsContainer();
		
		FormLink newCommentButton = uifactory.addFormLink("new-c-" + count++, "new.comment", "new.comment", null, container, Link.BUTTON_SMALL);
		FormLink cancelNewCommentButton = uifactory.addFormLink("cancel-new-c-" + count++, "cancel.new.comment", "cancel", null, container, Link.BUTTON_SMALL);
		TextElement commentEl = uifactory.addTextAreaElement("new-comment-text-c" + count++, 4, 60, comment, container);
		commentEl.setPlaceholderKey("comment.placeholder", null);
		reviewComment.setNewCommentEl(commentEl);
		reviewComment.setNewCommentButton(newCommentButton);
		newCommentButton.setUserObject(reviewComment);
		reviewComment.setCancelNewCommentButton(cancelNewCommentButton);
		cancelNewCommentButton.setUserObject(reviewComment);
		
		for(ApplicationReviewComment rc:reviewComment.getReview().getComments()) {
			if(rc.isNewOnly()) {
				rc.setVisible(false);
			} else {
				reviewComment.setEdit(false);
				reviewComment.setReply(false);
			}
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, ApplicationReviewComment reviewComment) {
		String title = translate("confirm.delete.comment.title");
		String text = translate("confirm.delete.comment.descr");
		confirmDeleteCommentCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCommentCtrl);
		confirmDeleteCommentCtrl.setUserObject(reviewComment);
	}
	
	private void doDeleteComment(ApplicationReviewComment reviewComment) {
		String before = auditService.toAuditXml(reviewComment.getApplicationComment());
		commentService.deleteComment(reviewComment.getApplicationComment());
		reloadReviewComments(reviewComment.getReview());
		logComment(Action.delete, null, "audit.log.comment.delete", before);
	}
	
	private void doSaveComment(ApplicationReviewComment reviewComment) {
		String text = reviewComment.getNewCommentEl().getValue();
		if(!StringHelper.containsNonWhitespace(text)) {
			showWarning("comment.empty");
		} else if(reviewComment.isEdit()) {
			ApplicationComment comment = commentService.updateComment(reviewComment.getApplicationComment(), text);
			logComment(Action.update, comment, "audit.log.comment.update", null);
		} else {
			String before = auditService.toAuditXml(reviewComment.getApplicationComment());
			ApplicationReview review = reviewComment.getReview();
			Identity reviewer = review.getReviewer();
			ApplicationComment comment = commentService.addComment(application, reviewer, text, getIdentity(), reviewComment.getApplicationComment());
			logComment(Action.add, comment, "audit.log.comment.add", before);
		}
		reloadReviewComments(reviewComment.getReview());
	}
	
	private void logComment(Action action, ApplicationComment comment, String messageI18n, String before) {	
		String[] messageArgs = new String[] {
				RecruitingHelper.formatFullNameWithTitle(getIdentity(), getLocale()),
				salutationGenerator.getTitleFullname(application, getLocale()),
				application.getId().toString()
		};
		String after = auditService.toAuditXml(comment);
		auditService.auditCommentLog(action, before, after, messageI18n, messageArgs, getTranslator(),
				position, application, comment, getIdentity());
	}
	
	private void doCancelComment(ApplicationReviewComment reviewComment) {
		if(reviewComment == null) return;
		
		if(reviewComment.isReply() || reviewComment.isEdit()) {
			reviewComment.setNewCommentEl(null);
			reviewComment.setNewCommentButton(null);
			reviewComment.setCancelNewCommentButton(null);
			reviewComment.setReply(false);
			reviewComment.setEdit(false);
		} else {
			reviewComment.getNewCommentEl().setValue("");
		}
		
		for(ApplicationReviewComment rc:reviewComment.getReview().getComments()) {
			if(rc.isNewOnly()) {
				rc.setVisible(true);
			}
		}
		flc.setDirty(true);
	}
	
	private void doToggleSort() {
		OrderReview order = (OrderReview)orderButton.getUserObject();
		if(order == OrderReview.asc) {
			order = OrderReview.desc;
			orderButton.setIconLeftCSS("o_icon o_icon_toggle_on");
		} else {
			order = OrderReview.asc;
			orderButton.setIconLeftCSS("o_icon o_icon_toggle_off");
		}
		orderButton.setUserObject(order);
		loadModel();
		flc.setDirty(true);
	}
	
	private void doSavePanel(String command, String reviewId) {
		if(!StringHelper.containsNonWhitespace(command) || !StringHelper.containsNonWhitespace(reviewId)) {
			return;
		}
		
		Boolean open;
		if("show".equals(command)) {
			open = Boolean.TRUE;
		} else if("hide".equals(command)) {
			open = Boolean.FALSE;
		} else {
			return;
		}
		
		if("statistics".equals(reviewId)) {
			openStatistics = open;
		} else {
			ApplicationReview selectedReview = null;
			for(ApplicationReview review:reviews) {
				if(review.getId().equals(reviewId)) {
					selectedReview = review;
					break;
				}
			}
			
			if(selectedReview != null) {
				selectedReview.setOpen(open.booleanValue());
				openReviews.put(selectedReview.getReviewer(), open);
			}
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, ApplicationReview review) {
		String title = translate("confirm.delete.review.title");
		String text;
		if(reviewDefinition.getReviewNameVisibility() == ReviewerNameVisibilityEnum.visible) {
			text = translate("confirm.delete.review.text", new String[] { review.getFullName() });
		} else {
			text = translate("confirm.delete.review.text.anonymous");
		}
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(review);
	}
	
	private void doDeleteReview(ApplicationReview review) {
		reviewService.deleteResponses(application, review.getReviewer());
		
		List<ReviewResponse> currentResponses = review.getResponses();
		if(currentResponses == null) {
			currentResponses = new ArrayList<>();
		}
		Collections.sort(currentResponses, new ResponseComparator());
		String before = auditService.toAuditXml(currentResponses);
		
		String[] messageArgs = new String[] {
			RecruitingHelper.formatFullNameWithTitle(getIdentity(), getLocale()),
			salutationGenerator.getTitleFullname(application, getLocale()),
			application.getId().toString()
		};
		auditService.auditReviewLog(Action.delete, before, "", "audit.log.review.delete", messageArgs, getTranslator(), position, application, getIdentity());
	}
	
	private void doEditReview(UserRequest ureq) {
		if(guardModalController(editReviewCtrl)) return;
		
		editReviewCtrl = new ReviewEditController(ureq, getWindowControl(), position, application);
		listenTo(editReviewCtrl);
		
		String title = translate("new.review");
		cmc = new CloseableModalController(getWindowControl(), "c", editReviewCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	public enum OrderReview {
		asc,
		desc
	}
	
	private class ReviewResponseComparator implements Comparator<ReviewResponse> {
		@Override
		public int compare(ReviewResponse o1, ReviewResponse o2) {
			if(o1 == null && o2 == null) {
				return 0;
			} else if(o1 == null) {
				return -1;
			} else if(o2 == null) {
				return 1;
			}
			
			Date d1 = o1.getCreationDate();
			Date d2 = o2.getCreationDate();
			return d1.compareTo(d2);  
		}
	}
	
	private class ApplicationReviewComparator implements Comparator<ApplicationReview> {
		
		private final OrderReview order;
		
		public ApplicationReviewComparator(OrderReview order) {
			this.order = order;
		}

		@Override
		public int compare(ApplicationReview o1, ApplicationReview o2) {
			if(o1 == null && o2 == null) {
				return 0;
			} else if(o1 == null) {
				return -1;
			} else if(o2 == null) {
				return 1;
			}
			
			Date d1 = o1.getDate();
			Date d2 = o2.getDate();
			if(d1 == null && d2 == null) {
				return 0;
			} else if(d1 == null) {
				return -1;
			} else if(d2 == null) {
				return 1;
			}
			
			int c = d1.compareTo(d2);
			if(order == OrderReview.asc) {
				c = c * -1;
			}
			return  c;  
		}
	}
}
