## log4j2.xml readme

log4j2.xml in this directory is the default for lazy developers to have output on eclipse console.
You can overwrite it with -Dlog4j.configurationFile="file:/tmp/mylog4j2.xml" (java system property) and
specify a path for your real logging configuration. The configuration of log4j 1 and 2 are not compatible.

Note that when you create a "real" log4j2.xml file you should specify the log dir path and the log 
file name also in your olat.local.properties:

```
log.dir
log.filename
```

The LogFileParser which is used to lookup errors and attach them in error report emails
and to lookup errors in the admin panel must know those parameters above to work properly. This code
also assumes, that older logfiles are available using the filename + ".yyy-mm-dd" to look up log 
entries that occured in the past using the admin panel. Example: olat.log.2012-04-15