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
package org.olat.course.statistic;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;

/**
 * From: http://www.theserverside.com/tt/articles/article.tss?l=UnitTesting
 * <P>
 * Initial Date:  02.03.2010 <br>
 * @author Stefan
 */
public class HibernateUtil {

    private static SessionFactory factory;

    public static synchronized Session getSession() {
        if (factory == null) {
            factory = new Configuration().configure().buildSessionFactory();
        }
        return factory.openSession();
    }

    public static void setSessionFactory(SessionFactory factory) {
        HibernateUtil.factory = factory;
    }

		public static void save(Object stat) {
		    Session session = getSession();
		    Transaction transaction = session.beginTransaction();
		    try {
		        session.save(stat);
		        transaction.commit();
		    }
		    finally {
		        session.close();
		    }
		}
}
