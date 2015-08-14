Guiceyfruit has not been [updated](http://code.google.com/p/guiceyfruit/updates/list) by the project owners since July/2009, so I'm setting up this fork to keep it up-to-date with new Google Guice releases and fix bugs.

I'm NOT going to fix issues you find, if you need something, do it yourself and send patches, I'll be glad to integrate them and make them available.

## Release 3.0 ##
I've released version 3.0 depending on Guice 3.0-rc2, the maven repository is here: http://guiceyfruit.googlecode.com/svn/repo/releases/

  * Fixed issue of Guiceyfruit not calling @PostConstruct and @PreDestroy methods on superclasses.
  * Removed osgi module because of build problems (who uses it anyway...)