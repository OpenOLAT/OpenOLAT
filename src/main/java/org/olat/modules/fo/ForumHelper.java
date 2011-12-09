/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.modules.fo;

import java.text.Collator;
import java.util.Comparator;

/**
 * 
 * Description:<br>
 * Provides utilities methods and classes.
 *  
 * <P>
 * Initial Date:  14.08.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class ForumHelper {
	public static final String CSS_ICON_CLASS_FORUM = "o_fo_icon";
	public static final String CSS_ICON_CLASS_MESSAGE = "o_forum_message_icon";
	
	public static int NOT_MY_JOB = 0;

	/**
	 * Comparators can be passed to a sort method (such as Collections.sort) 
	 * to allow precise control over the sort order.  
	 * <p>
	 * Sticky threads first, last modified first.
	 * 
	 * @return a MessageNode comparator.
	 * @see java.util.Comparator 
	 */
	public static Comparator getMessageNodeComparator() {
		return new Comparator(){
			//puts the sticky threads first
			public int compare(final Object o1, final Object o2) {
				MessageNode m1 = (MessageNode)o1;
				MessageNode m2 = (MessageNode)o2;				
				if(m1.isSticky() && m2.isSticky()) {
					return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
				} else if(m1.isSticky()) {
					return -1;
				} else if(m2.isSticky()){
					return 1;
				} else {
					return m2.getModifiedDate().compareTo(m1.getModifiedDate()); //last first
				}				
			}};
	}
	
	/**
	 * Compares two MessageWrappers. <br>
	 * If a and b both sticky or if none sticky, let the caller do the sorting (return NOT_MY_JOB),<br> 
	 * else if a is sticky and sortAscending is true then a less then b (return -1) <br>
	 * else if b is sticky and sortAscending is true then a greater then b (return 1)<p>
	 * @param a
	 * @param b
	 * @param sortAscending
	 * @return -1, 1, or NOT_MY_JOB which means the caller has to do the comparison.
	 */
	public static int compare(MessageWrapper a, MessageWrapper b, boolean sortAscending) {
		if (a.isSticky() && b.isSticky()) {
			return NOT_MY_JOB;
		} else if (a.isSticky()) {
			if (sortAscending) return -1;
			else return 1;
		} else if (b.isSticky()) {
			if (sortAscending) return 1;
			else return -1;
		} else {
			return NOT_MY_JOB;
		}
	}
	
	/**
	 * Description:<br>
	 * Wrapper for the table cell values for providing the sticky info
	 * about a message.
	 * <P>
	 * Initial Date: 11.07.2007 <br>
	 * 
	 * @author Lavinia Dumitrescu
	 */
	protected static class MessageWrapper implements Comparable {	
		
		private Comparable value;
		private boolean sticky;
		private Collator collator = Collator.getInstance();
		
		public MessageWrapper(Comparable value_, boolean sticky_, Collator collator) {
			value = value_;
			sticky = sticky_;
			if (collator != null) {				
				this.collator = collator;
			}
		}
		
		/**
		 * 
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value.toString();
		}

		public boolean isSticky() {
			return sticky;
		}			

		public Comparable getValue() {
			return value;
		}

		/**
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object o) {	
			MessageWrapper theOtherMessage = ((MessageWrapper)o);
			if(getValue() instanceof String) {				
				return collator.compare(getValue().toString(), theOtherMessage.getValue().toString());
			}
			return getValue().compareTo(theOtherMessage.getValue());
		}
		
		/**
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			try {
				MessageWrapper theOther = (MessageWrapper)obj;
				return getValue().equals(theOther.getValue());
			} catch(Exception ex) {
				//nothing to do
			}
			return false;
		}		
	}
	
}
