# MySql Web Helper

Use the following aid to easily interact with provisioned mysql dbs

1. Clone the repo: `git clone https://github.com/pivotal-cf/PivotalMySQLWeb`
2. Build the project: `mvn clean install`
3. Replace manifest.yml with the following
> For pivotal-mysqlweb-metadata you have to get the service name of the proxy
service created with your SCDF for PCF

```yaml
applications:
- name: pivotal-mysqlweb-metadata
  memory: 768m
  instances: 1
  path: ./target/PivotalMySQLWeb-0.0.1-SNAPSHOT.jar
  services:
    - relational-e918cdd7-4b79-49aa-945c-ecace0a007b7
- name: pivotal-mysqlweb-app-db
  memory: 768m
  instances: 1
  path: ./target/PivotalMySQLWeb-0.0.1-SNAPSHOT.jar
  services:
    - app-db
```
4. Push the app
```bash
cf push
```