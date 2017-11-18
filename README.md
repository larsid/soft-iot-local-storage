# FoT-Gateway-Local-Storage

## Introduction

This module is responsible to collect data from sensors (using the configuration of fot-gateway-mapping-devices) and store them in a local database (Apache H2). For this, it uses a MQTT client (Paho) to get data from sensors and write this in the database.

## Installation

To install this bundle, you need install previously these dependencies in karaf:
```
feature:repo-add mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features
feature:install transaction jndi pax-jdbc-h2 pax-jdbc-pool-dbcp2 pax-jdbc-config
```
This module also depends of module [fot-gateway-mapping-devices](https://github.com/WiserUFBA/fot-gateway-mapping-devices).

To install this bundle using our custom maven support execute the following commands in Karaf Shell:

```sh
config:edit org.ops4j.pax.url.mvn 
config:property-append org.ops4j.pax.url.mvn.repositories ", https://github.com/WiserUFBA/wiser-mvn-repo/raw/master/releases@id=wiser"
config:update
mvn:br.ufba.dcc.wiser.soft_iot/fot-gateway-mapping-devices/1.0.0
mvn:br.ufba.dcc.wiser.soft_iot/fot-gateway-local-storage/1.0.0
```

After this you need create a file with database configuration. The filename is **etc/org.ops4j.datasource-gateway.cfg**. The content of file is:

```
osgi.jdbc.driver.name=H2-pool-xa
url=jdbc:h2:${karaf.data}/fot-gateway-local-storage
dataSourceName=fot-gateway-local-storage
```

FoT-Storage has a configuration file (*br.ufba.dcc.wiser.soft_iot.local_storage.cfg*), where is possible set information about MQTT server, default frequency to collect data sensor and the frequency of execution of procedure to clean database.

Finally, for correct execution of module you need copy the file:
```
fot-gateway-local-storage/src/main/resources/br.ufba.dcc.wiser.soft_iot.local_storage.cfg
```
to:
```
<servicemix_directory>/etc
```
## Deploy to Maven Repo

To deploy this repo into our custom maven repo, change pom according to the new version and after that execute the following command. Please ensure that both wiser-mvn-repo and this repo are on the same folder.

```sh
mvn -DaltDeploymentRepository=release-repo::default::file:../wiser-mvn-repo/releases/ deploy
```



## Support and development

<p align="center">
	Developed by Leandro Andrade at </br>
  <img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>

