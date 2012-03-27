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
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.notifications.items.SubscriptionListItem;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructureToArtefactLink;
import org.olat.portfolio.model.structel.EPStructureToStructureLink;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.ui.structel.view.EPChangelogController;
import org.olat.user.UserManagerImpl;

/**
 * 
 * EPNotificationsHelper provides functionality to gather SubscriptionListItems
 * for given Maps.<br />
 * 
 * 
 * 
 * FXOLAT-431, FXOLAT-432<br />
 * this also triggered: OO-111
 * 
 * 
 * most of the time, the use of the available methods in EPFrontendmanager
 * wasn't possible, I always need the "link-creation-time" of a link (e.g.
 * between artefact and struct-Eement), thus the new methods in this class.
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
	 * 
	 * @param compareDate
	 * @param map
	 * @return
	 */
	public List<SubscriptionListItem> getAllSubscrItemsDefault(Date compareDate, EPMapShort map) {
		EPNotificationManager mgr = CoreSpringFactory.getImpl(EPNotificationManager.class);
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();
		List<Long> mapKeys = Collections.singletonList(map.getKey());
		//structure elements
		List<SubscriptionListItem> notis1 = mgr.getPageSubscriptionListItem(map.getKey(), rootBusinessPath, compareDate, translator);	
		allItems.addAll(notis1);
		//artefacts
		List<SubscriptionListItem> notis2 = mgr.getArtefactNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis2);
		//ratings
		List<SubscriptionListItem> notis3 = mgr.getRatingNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis3);
		//comments
		List<SubscriptionListItem> notis4 = mgr.getCommentNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis4);

		//sort
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
	}
	
	public List<SubscriptionListItem> getAllSubscrItemsStructured(Date compareDate, EPMapShort map) {
		EPNotificationManager mgr = CoreSpringFactory.getImpl(EPNotificationManager.class);
		// at this moment, map is not yet synchronized. check the "parent"
		// templateMap for map/structure changes
	
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		//structure elements
		List<SubscriptionListItem> notis1 = mgr.getPageSubscriptionListItem(map.getSourceMapKey(), rootBusinessPath, compareDate, translator);	
		allItems.addAll(notis1);
		
		List<Long> mapKeys = new ArrayList<Long>();
		mapKeys.add(map.getKey());
		mapKeys.add(map.getSourceMapKey());
		
		//artefacts
		List<SubscriptionListItem> notis2 = mgr.getArtefactNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis2);
		//ratings
		List<SubscriptionListItem> notis3 = mgr.getRatingNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis3);
		//comments
		List<SubscriptionListItem> notis4 = mgr.getCommentNotifications(mapKeys, rootBusinessPath, compareDate, translator);
		allItems.addAll(notis4);

		//sort
		Collections.sort(allItems, new SubscriptionListItemComparator());
		return allItems;
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
	private List<SubscriptionListItem> getAllSubscrItemsForDefaulMap(Date compareDate, EPAbstractMap map) {

		/* all items are first put in this list, then sorted, then returned */
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		String tmp_bPath;
		String tmp_linkUrl;

		/* all struct to struct links (Pages and structureElements) */
		List<EPStructureToStructureLink> structLinkCollection = getAllStruct2StructLinks(map);
		for (EPStructureToStructureLink structLink : structLinkCollection) {
			if (structLink.getCreationDate().after(compareDate)) {
				if (structLink.getChild() instanceof EPPage) {
					EPPage childPage = (EPPage) structLink.getChild();
					tmp_bPath = rootBusinessPath + "[EPPage:" + childPage.getKey() + "]";
					tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
					SubscriptionListItem newItem = new SubscriptionListItem(
							translator.translate("li.newpage", new String[] { childPage.getTitle() }), tmp_linkUrl, structLink.getCreationDate(),
							"b_ep_page_icon");
					newItem.setUserObject(childPage.getKey());
					allItems.add(newItem);
				} else {
					Long pageKey = null;
					if (structLink.getParent() instanceof EPPage) {
						EPPage parentPage = (EPPage) structLink.getParent();
						pageKey = parentPage.getKey();
						tmp_bPath = rootBusinessPath + "[EPPage:" + pageKey + "]";
						tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
					} else {
						tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(rootBusinessPath);
					}
					SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newstruct", new String[] { structLink.getChild()
							.getTitle() }), tmp_linkUrl, structLink.getCreationDate(), "b_ep_struct_icon");
					newItem.setUserObject(pageKey);
					allItems.add(newItem);
				}
			}
		}

		Long tmp_LinkKey;
		String tmp_TargetTitle;
		/* all artefacts on the maps pages and structElements */
		List<EPStructureToArtefactLink> links = getAllArtefactLinks(map);
		for (EPStructureToArtefactLink link : links) {
			if (link.getCreationDate().after(compareDate)) {
				PortfolioStructure linkParent = link.getStructureElement();
				if (linkParent instanceof EPPage) {
					tmp_LinkKey = linkParent.getKey();
					tmp_TargetTitle = linkParent.getTitle();
				} else {
					// it's no page, thus a struct-element, we want to jump to
					// the page
					tmp_LinkKey = linkParent.getRoot().getKey();
					tmp_TargetTitle = linkParent.getRoot().getTitle();
				}

				tmp_bPath = rootBusinessPath + "[EPPage:" + tmp_LinkKey + "]";
				tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
				SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newartefact", new String[] {
						getFullNameFromUser(link.getArtefact().getAuthor()), link.getArtefact().getTitle(), tmp_TargetTitle }), tmp_linkUrl,
						link.getCreationDate(), "b_eportfolio_link");
				newItem.setUserObject(tmp_LinkKey);
				allItems.add(newItem);
			}
		}

		/* comments and ratings */
		allItems.addAll(getCRItemsForMap(compareDate, map));

		/* now sort all listItems */
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
	private List<SubscriptionListItem> getAllSubscrItemsForStructuredMap(Date compareDate, EPStructuredMap map) {
		List<SubscriptionListItem> allItems = new ArrayList<SubscriptionListItem>();

		// at this moment, map is not yet synchronized. check the "parent"
		// templateMap for map/structure changes
		PortfolioStructureMap sourceMap = map.getStructuredMapSource();
		allItems = getAllSubscrItemsForDefaulMap(compareDate, (EPAbstractMap) sourceMap);

		String tmp_bPath;
		String tmp_linkUrl;
		Long tmp_linkKey = 0L;
		String tmp_TargetTitle;

		// now check artefacts, comments and ratings of this map
		List<EPStructureToArtefactLink> links = getAllArtefactLinks(map);
		for (EPStructureToArtefactLink link : links) {
			if (link.getCreationDate().after(compareDate)) {
				PortfolioStructure linkParent = link.getStructureElement();

				if (linkParent instanceof EPPage) {
					tmp_linkKey = linkParent.getKey();
					tmp_TargetTitle = linkParent.getTitle();
				} else {
					// it's no page, thus a struct-element, we want to jump to
					// the page
					tmp_linkKey = linkParent.getRoot().getKey();
					tmp_TargetTitle = linkParent.getRoot().getTitle();
				}

				tmp_bPath = rootBusinessPath + "[EPPage:" + tmp_linkKey + "]";
				tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
				SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newartefact", new String[] {
						getFullNameFromUser(link.getArtefact().getAuthor()), link.getArtefact().getTitle(), tmp_TargetTitle }), tmp_linkUrl,
						link.getCreationDate(), "b_eportfolio_link");
				newItem.setUserObject(tmp_linkKey);
				allItems.add(newItem);
			}
		}

		/* the comments and ratings */
		allItems.addAll(getCRItemsForMap(compareDate, map));

		/* now sort all listItems */
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

		String tmp_bPath;
		String tmp_linkUrl;

		// get all comments and ratings on the map
		CommentAndRatingService crs = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
		crs.init(identity, map.getOlatResource(), null, false, false);
		List<UserComment> comments = crs.getUserCommentsManager().getComments();
		for (UserComment comment : comments) {
			if (comment.getCreationDate().after(compareDate)) {
				tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(rootBusinessPath);
				SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newcomment", new String[] { map.getTitle(),
						getFullNameFromUser(comment.getCreator()) }), tmp_linkUrl, comment.getCreationDate(), "b_info_icon");
				allItemsToAdd.add(newItem);
			}
		}
		List<UserRating> ratings = crs.getUserRatingsManager().getAllRatings();
		for (UserRating rating : ratings) {
			if (rating.getCreationDate().after(compareDate)) {
				tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(rootBusinessPath);
				if (rating.getLastModified() != null) {
					// there is a modified date, also add this as a listitem
					allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.changerating", new String[] { map.getTitle(),
							getFullNameFromUser(rating.getCreator()) }), tmp_linkUrl, rating.getLastModified(), "b_star_icon"));
				}
				allItemsToAdd.add(new SubscriptionListItem(translator.translate("li.newrating", new String[] { map.getTitle(),
						getFullNameFromUser(rating.getCreator()) }), tmp_linkUrl, rating.getCreationDate(), "b_star_icon"));
			}
		}
		crs = null;

		// get all comments and ratings on pages of the map
		List<PortfolioStructure> children = ePFMgr.loadStructureChildren(map);
		for (PortfolioStructure child : children) {
			if (child instanceof EPPage) {
				EPPage childPage = (EPPage) child;
				CommentAndRatingService c_crs = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
				c_crs.init(identity, map.getOlatResource(), childPage.getKey().toString(), false, false);
				List<UserComment> c_comments = c_crs.getUserCommentsManager().getComments();
				for (UserComment comment : c_comments) {
					if (comment.getCreationDate().after(compareDate)) {
						tmp_bPath = rootBusinessPath + "[EPPage:" + comment.getResSubPath() + "]";
						tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
						SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newcomment", new String[] {
								child.getTitle(), getFullNameFromUser(comment.getCreator()) }), tmp_linkUrl, comment.getCreationDate(), "b_info_icon");
						newItem.setUserObject(comment.getResSubPath());
						allItemsToAdd.add(newItem);
					}
				}
				List<UserRating> c_ratings = c_crs.getUserRatingsManager().getAllRatings();
				for (UserRating rating : c_ratings) {
					if (rating.getCreationDate().after(compareDate)) {
						tmp_bPath = rootBusinessPath + "[EPPage:" + rating.getResSubPath() + "]";
						tmp_linkUrl = BusinessControlFactory.getInstance().getURLFromBusinessPathString(tmp_bPath);
						if (rating.getLastModified() != null) {
							// there is a modified date, also add this as a
							// listitem
							SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.changerating",
									new String[] { child.getTitle(), getFullNameFromUser(rating.getCreator()) }), tmp_linkUrl,
									rating.getLastModified(), "b_star_icon");
							newItem.setUserObject(rating.getResSubPath());
							allItemsToAdd.add(newItem);
						}
						SubscriptionListItem newItem = new SubscriptionListItem(translator.translate("li.newrating", new String[] { child.getTitle(),
								getFullNameFromUser(rating.getCreator()) }), tmp_linkUrl, rating.getCreationDate(), "b_star_icon");
						newItem.setUserObject(rating.getResSubPath());
						allItemsToAdd.add(newItem);
					}
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
	private static EPAbstractMap findMapOfAnyType(Long resourceableId) {
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
	private static String getFullNameFromUser(User user) {
		return UserManagerImpl.getInstance().getUserDisplayName(user);
	}

	/**
	 * plain helper method to get "firstname lastname" from given
	 * Identity-object
	 * 
	 * @param identity
	 * @return
	 */
	private static String getFullNameFromUser(Identity identity) {
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
	 * returns a list of StructureToArtefactLinks of the given map.<br />
	 * The resulting lists contains a Struct2Artefact Link for every artefact on
	 * the map's pages, as well as one for every artefact on the page's
	 * struct-elements.
	 * 
	 * 
	 * @param parent
	 * @return
	 */
	private List<EPStructureToArtefactLink> getAllArtefactLinks(EPAbstractMap map) {
		List<EPStructureToArtefactLink> resultList = new ArrayList<EPStructureToArtefactLink>();
		List<PortfolioStructure> allChildStructs = getAllStructuresInMap(map);

		for (PortfolioStructure portfolioStructure : allChildStructs) {
			if (portfolioStructure instanceof EPStructureElement) {
				resultList.addAll(((EPStructureElement) portfolioStructure).getInternalArtefacts());
			} else if (portfolioStructure instanceof EPPage) {
				resultList.addAll(((EPPage) portfolioStructure).getInternalArtefacts());
			}
		}
		return resultList;
	}

	/**
	 * returns all <code>PortfolioStructures</code> of the given map.
	 * assumptions: a map can have pages , pages can have structures. (no more
	 * levels, i.e. no structures in structures)
	 * 
	 * @param map
	 * @return
	 */
	private List<PortfolioStructure> getAllStructuresInMap(EPAbstractMap map) {
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		List<PortfolioStructure> resultList = new ArrayList<PortfolioStructure>();

		// all children of the map, these are the pages
		List<PortfolioStructure> pages = ePFMgr.loadStructureChildren(map);
		for (PortfolioStructure page : pages) {
			resultList.add(page);
			if (page instanceof EPPage) {
				resultList.addAll(ePFMgr.loadStructureChildren(page));
			} else {
				logger.warn("unexpected, child-structure of map was no EPPage..");
			}
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
