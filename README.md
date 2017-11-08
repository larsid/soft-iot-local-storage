# FoT-Gateway-Local-Storage

This module is responsible to collect data from sensors (using the configuration of fot-gateway-mapping-devices) and store them in a local database (Apache H2).

To install this bundle, you need install previously these dependencies in karaf:
```
feature:repo-add mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features
feature:install transaction jndi pax-jdbc-h2 pax-jdbc-pool-dbcp2 pax-jdbc-config
```
This module also depends of module [fot-gateway-mapping-devices](https://github.com/WiserUFBA/fot-gateway-mapping-devices).

After this you need create a file with database configuration. The filename is **etc/org.ops4j.datasource-gateway.cfg**. The content of file is:

```
osgi.jdbc.driver.name=H2-pool-xa
url=jdbc:h2:${karaf.data}/fot-gateway-local-storage
dataSourceName=fot-gateway-local-storage
```

<p align="center">
	Developed by Leandro Andrade</br>
  <img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>



