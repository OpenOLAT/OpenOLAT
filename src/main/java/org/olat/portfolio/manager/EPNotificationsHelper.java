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
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureToStructureLink;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.view.EPChangelogController;

/**
 * 
 * EPNotificationsHelper provides functionality to gather SubscriptionListItems
 * for given Maps.<br />
 * 
 * FXOLAT-431, FXOLAT-432<br />
 * this also triggered: OO-111
 * 
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * 
 */
public class EPNotificationsHelper {

	private static OLog logger = Tracing.createLoggerFor(EPNotificationsHelper.class);
	private Translator translator;
	private Identity identity;
	private EPFrontendManager ePFMgr;
	private String rootBusinessPath;

	/**
	 * sets up the helper. provide a locale and an Identity
	 * 
	 * @param locale
	 * @param identity
	 */
	public EPNotificationsHelper(String rootBusinessPath, Locale locale, Identity identity) {
		this.translator = Util.createPackageTranslator(EPChangelogController.class, locale);
		this.identity = identity;
		this.rootBusinessPath = rootBusinessPath;
		ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
	}

	/**
	 * returns a Collection of SubscriptionListItems for the given EPDefaultMap
	 * or EPStructuredMapTemplate (they are handled the same)<br />
	 * the returning list will contain listItems for newly added
	 * artefacts/Pages/StructElements/Comments/Ratings
	 * 
	 * @param compareDate
	 *            the comareDate (only items are added, that are created after
	 *            this date)
	 * @param map
	 *            the EPDefaultMap or
	 * 
	 */
	public List<SubscriptionListItem> getAllSubscrItems_Default(Date compareDate, EPAbstractMap map) {

		/* all items are first put in this list, then sorted, then added to si */
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		/* all struct to struct links (Pages and structureElements) */
		List<EPStructureToStructureLink> structLinkCollection = getAllStruct2StructLinks(map);
		for (EPStructureToStructureLink structLink : structLinkCollection) {
			if (structLink.getCreationDate().after(compareDate)) {
				if (structLink.getChild() instanceof EPPage) {
					EPPage childPage = (EPPage) structLink.getChild();
					String businessPath = rootBusinessPath + "[EPPage:" + childPage.getKey() + "]";
					String urlToSend = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
					allItems.add(new SubscriptionListItem(translator.translate("li.newpage", new String[] { childPage.getTitle() }), urlToSend, structLink
							.getCreationDate(), "b_ep_page_icon"));
				} else {
					allItems.add(new SubscriptionListItem(translator.translate("li.newstruct", new String[] { structLink.getChild().getTitle() }),
							"", structLink.getCreationDate(), "b_ep_struct_icon"));
				}
			}
		}

		/* all artefacts on the maps pages and structElements */
		List<AbstractArtefact> allAs = ePFMgr.getAllArtefactsInMap(map);
		if (logger.isDebug()) {
			logger.debug("getting all artefacts for map " + map.getTitle());
			logger.debug("got " + allAs.size() + " artefacts...");
		}
		for (AbstractArtefact artfc : allAs) {
			if (artfc.getCollectionDate().after(compareDate)) {
				allItems.add(new SubscriptionListItem(translator.translate("li.newartefact", new String[] { getFullNameFromUser(artfc.getAuthor()
						.getUser()) }), "", artfc.getCollectionDate(), "b_eportfolio_link"));
			}
		}

		allItems.addAll(getCRItemsForMap(compareDate, map));

		/* now sort all listItems and add to si */
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
	}

	/**
	 * returns a Collection of SubscriptionListItems for the given
	 * EPStructuredMap<br />
	 * the returning list will contain listItems for newly added
	 * artefacts/Comments/Ratings PLUS the newly added Pages/StructElements from
	 * the "parent"/"source" templateMap
	 * 
	 * @param compareDate
	 *            the comareDate (only items are added, that are created after
	 *            this date)
	 * @param map
	 *            the EPStructuredMap
	 * 
	 */
	public List<SubscriptionListItem> getAllSubscrItems_Structured(Date compareDate, EPStructuredMap map) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		// at this moment, map is not yet synchronized. check the "parent"
		// templateMap for map/structure changes
		PortfolioStructureMap sourceMap = map.getStructuredMapSource();
		allItems = getAllSubscrItems_Default(compareDate, (EPAbstractMap) sourceMap);

		// now check artefacts, comments and ratings of this map
		List<AbstractArtefact> allAs = ePFMgr.getAllArtefactsInMap(map);
		if (logger.isDebug()) {
			logger.debug("getting all artefacts for map " + map.getTitle());
			logger.debug("got " + allAs.size() + " artefacts...");
		}
		for (AbstractArtefact artfc : allAs) {
			if (artfc.getCollectionDate().after(compareDate)) {
				allItems.add(new SubscriptionListItem(
						translator.translate("li.newartefact", new String[] { getFullNameFromUser(artfc.getAuthor()) }), "", artfc
								.getCollectionDate(), "b_eportfolio_link"));
			}
		}
		allItems.addAll(getCRItemsForMap(compareDate, map));

		/* now sort all listItems and add to si */
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
	}

	/**
	 * gets a list of subscriptionsListItems for the given Map.<br />
	 * the resulting list will contain all new comments and ratings that were
	 * added after the compareDate (for the given map)
	 * 
	 * @param compareDate
	 * @param map
	 * @return
	 */
	private List<SubscriptionListItem> getCRItemsForMap(Date compareDate, EPAbstractMap map) {
		List<SubscriptionListItem> allItemsToAdd = new ArrayList<SubscriptionListItem>();
		/* comments and ratings */
		CommentAndRatingService crs = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
		crs.init(identity, map.getOlatResource(), null, false, false);
		List<UserComment> comments = crs.getUserCommentsManager().getComments();
		for (UserComment comment : comments) {
			if (comment.getCreationDate().after(compareDate))
				allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.newcomment", new String[] { map.getTitle(),
						getFullNameFromUser(comment.getCreator()) }), "", comment.getCreationDate(), "b_info_icon"));
		}
		List<UserRating> ratings = crs.getUserRatingsManager().getAllRatings();
		for (UserRating rating : ratings) {
			if (rating.getCreationDate().after(compareDate))
				allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.newrating", new String[] { map.getTitle(),
						getFullNameFromUser(rating.getCreator()) }), "", rating.getCreationDate(), "b_star_icon"));
		}
		crs = null;

		// get all comments on pages of the map
		List<PortfolioStructure> children = ePFMgr.loadStructureChildren(map);
		for (PortfolioStructure child : children) {
			if (child instanceof EPPage) {
				EPPage childPage = (EPPage) child;
				CommentAndRatingService c_crs = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
				c_crs.init(identity, map.getOlatResource(), childPage.getKey().toString(), false, false);
				List<UserComment> c_comments = c_crs.getUserCommentsManager().getComments();
				for (UserComment comment : c_comments) {
					if (comment.getCreationDate().after(compareDate))
						allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.newcomment", new String[] { child.getTitle(),
								getFullNameFromUser(comment.getCreator()) }), "", comment.getCreationDate(), "b_info_icon"));
				}
				List<UserRating> c_ratings = c_crs.getUserRatingsManager().getAllRatings();
				for (UserRating rating : c_ratings) {
					if (rating.getCreationDate().after(compareDate))
						allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.newrating", new String[] { map.getTitle(),
								getFullNameFromUser(rating.getCreator()) }), "", rating.getCreationDate(), "b_star_icon"));
				}
			}
		}

		return allItemsToAdd;
	}

	/**
	 * helperMethod to find a map with the given resourceableId. This method
	 * looks for all map-types: defaultMap, structuredMap, structuredMapTemplate
	 * 
	 * @param resourceableId
	 * @return
	 */
	protected static EPAbstractMap findMapOfAnyType(Long resourceableId) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		Class<?>[] mapTypes = new Class<?>[] { EPDefaultMap.class, EPStructuredMap.class, EPStructuredMapTemplate.class };

		PortfolioStructure struct = null;
		int loopCounter = 0;
		while (struct == null && loopCounter < mapTypes.length) {
			OLATResourceable res = OresHelper.createOLATResourceableInstance(mapTypes[loopCounter], resourceableId);
			struct = ePFMgr.loadPortfolioStructure(res);
			loopCounter++;
		}

		if (struct instanceof EPAbstractMap) {
			return (EPAbstractMap) struct;
		}
		logger.error("Could not find Map for resourceableID: " + resourceableId);
		return null;
	}

	/**
	 * plain helper method to get "firstname lastname" from given User-object
	 * 
	 * @param user
	 * @return
	 */
	public static String getFullNameFromUser(User user) {
		return user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
	}

	/**
	 * plain helper method to get "firstname lastname" from given
	 * Identity-object
	 * 
	 * @param identity
	 * @return
	 */
	public static String getFullNameFromUser(Identity identity) {
		return getFullNameFromUser(identity.getUser());
	}

	/**
	 * recursively fetches all structure2structureLinks of the given parent
	 * 
	 * @param parent
	 * @return
	 */
	private List<EPStructureToStructureLink> getAllStruct2StructLinks(EPStructureElement parent) {
		List<EPStructureToStructureLink> resultList = new ArrayList<EPStructureToStructureLink>();
		List<EPStructureToStructureLink> structLinkCollection = parent.getInternalChildren();

		// loop over all links, check for children of type EPPage
		for (EPStructureToStructureLink structLink : structLinkCollection) {
			resultList.add(structLink);
			if (structLink.getChild() instanceof EPPage)
				resultList.addAll(getAllStruct2StructLinks((EPPage) structLink.getChild()));
		}

		return resultList;
	}

	/**
	 * compares two SubscriptionListItems according to their date.
	 * 
	 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
	 * 
	 */
	private class SubscriptionListItemComparator implements Comparator<SubscriptionListItem> {

		@Override
		public int compare(SubscriptionListItem o1, SubscriptionListItem o2) {
			if (o1.getDate().after(o2.getDate()))
				return -1;
			return 1;
		}

	}

}
