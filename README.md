# bitme-java
This is a java implementation of the BitMe Bitcoin Exchange API, which is
documented here:

http://bitme.github.io/rest/

With this API, you have full access to your account, including order
manipulation, transaction listings, currency withdrawal and exchange data.

## Build
To build, you'll need maven:

    mvn package

## Install

    mvn install

## Testing
To get the tests to work, you'll have to copy the BitmeAPIKeyExample.java
file to BitmeAPIKey.java in the same directory.  Then, edit the new file
and add your API key information.  DO NOT COMMIT THIS NEW FILE.  The
tests should run now:

    mvn package
