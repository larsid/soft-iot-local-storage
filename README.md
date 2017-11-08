# FoT Storage

<p align="center">
	Developed by </br>
  <img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>

To install this bundle, you need install previously these dependencies in karaf
```
feature:repo-add mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.8.0/xml/features
feature:install transaction jndi pax-jdbc-h2 pax-jdbc-pool-dbcp2 pax-jdbc-config
```

After this you need create a file with database configuration. The filename is *etc/org.ops4j.datasource-gateway.cfg*. The content of file is:

```
osgi.jdbc.driver.name=H2-pool-xa
url=jdbc:h2:${karaf.data}/fot-gateway-local-storage
dataSourceName=fot-gateway-local-storage
```
</p>
