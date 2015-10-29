Simple Apache NiFi processor storing the content of a flowfile in Apache Zookeeper.


Instructions:

Build:

Clone nifi:

git clone https://github.com/apache/nifi.git

build nifi:

export MAVEN_OPTS="-Xmx2048m"

mvn install

If you see tests failing try:
mvn -Dmaven.test.skip=true install

in nifi/nifi-nar-bundles clone the examples:
cd nifi/nifi-nar-bundles
git clone https://github.com/bbende/nifi-example-bundles.git


Clone this repo (PutZKProcessor) into the following folder of your NiFi installation:
cd nifi-example-bundles/nifi-example-utils-bundle
git clone https://github.com/digitalemil/PutZKProcessor.git

Edit:
nifi-example-bundles/pom.xml
and let the parent's version reflect your nifi's version:

<parent>
        ...
	<version>0.3.1-SNAPSHOT</version>
</parent>


Edit:
nifi-example-bundles/nifi-example-utils-bundle/pom.xml
and add the following module:

<module>PutZKProcessor</module>

Edit:
nifi-example-bundles/nifi-example-utils-bundle/nifi-example-utils-nar/pom.xml
Add the PutZKProcessor dependency:

<dependency>
           <groupId>digitalemil.de</groupId>
           <artifactId>PutZK</artifactId>
           <version>0.0.1-SNAPSHOT</version>
</dependency>
	
do a
mvn install
(in nifi-example-bundles)

Copy the nar-file containing RenameJsonProcessor and PutZKProcessor into your nifi/lib folder of your binary installation:
cp /nifi/nifi-nar-bundles/nifi-example-bundles/nifi-example-utils-bundle/nifi-example-utils-nar/target/nifi-example-utils-nar-0.0.1-SNAPSHOT.nar /opt/nifi-1.0.0.0-7/lib

Restart nifi
