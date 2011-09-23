Sat Feb 13 10:19:27 EST 2010

Download this file:

http://source.sakaiproject.org/maven2-snapshots/org/sakaiproject/basiclti/basiclti-util/1.2-SNAPSHOT/basiclti-util-1.2-SNAPSHOT.jar

And place it in 

OLAT-BASE/webapp/WEB-INF/lib

This jar is not Sakai-specific code - it is actually the IMS
utility code and the OAuth code from OAuth.net.  It ends up
convienently in the Sakai repo as part of the Sakai build process.
We can pull this utility code source into the OLAT tree at
any time.  I would like to get this into a repo for IMS some
day but for now Sakai's repo is easy for me to access and use.

----- Testing the Tool ----

Place the tool in a course and use the following data:

Launch URL:        http://wiscrowd.appspot.com/wiscrowd/
Launch Key:        12345
Launch Secret:     secret

there is also an Basic LTI enabled MediaWiki
Launch URL: http://csev.people.si.umich.edu/mediawiki/extensions/Redirect2Course.php
Key: egal
Secret: secret

-----

Let me know if you have any questions or comments.

--- Chuck
