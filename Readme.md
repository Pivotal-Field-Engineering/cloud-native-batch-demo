Test ratings locally
- delete database schemas
- Launch MySql
- Run config server `./gradlew bootRun`
- Run discovery server `./gradlew bootRun`
- Launch ratings API first to create tables
```
SPRING_PROFILES_ACTIVE=local java -jar target/ratings-api-0.0.1-SNAPSHOT.jar
```
- Launch ratings loader to load data (you should see COMPLETED in logs)
```
SPRING_PROFILES_ACTIVE=local java -jar target/ratings-loader-0.0.1-SNAPSHOT.jar localFilePath=classpath:data.csv
```
- access http :8081/ratings and you should have 3 results
```
SPRING_PROFILES_ACTIVE=local java -jar target/trades-loader-0.0.1-SNAPSHOT.jar localFilePath=classpath:data.csv
```



What we have
- Multiple datasources
- 


# Note on DB's
- rating table does not grow, so no incrementing apis.  JPA creates table through API launch



cf create-service p-service-registry standard discovery-server 
cf create-service p-config-server standard config-server -c '{"git":{"uri":"https://github.com/doddatpivotal/dragonstone-finance.git","searchPaths":"dragonstone-finance-config","label":"master"}}'
// cf create-service nfs Existing volume-service -c '{"share":"nfs-pcf.pez.pivotal.io/pcfone/dpfeffer"}'
cf create-service nfs Existing volume-service -c '{"share":"nfs-pcf.pez.pivotal.io/pcfone/dpfeffer","uid":"$EMP_ID","gid":"$EMP_ID", "mount":"/var/scdf"}'
cf create-service p.mysql db-small app-db
cf create-service p-rabbitmq standard rabbit

http https://auditor-api.apps.pcfone.io/trades
http https://ratings-api.apps.pcfone.io/ratings


## Task App Maven Deployments

for trades-loader
```bash
mvn deploy -Ddistribution.management.release.id=bintray -Ddistribution.management.release.url=https://api.bintray.com/maven/dpfeffer/maven-repo/trades-loader
```
> remember to publish
for ratings-loader

```bash
mvn deploy -Ddistribution.management.release.id=bintray -Ddistribution.management.release.url=https://api.bintray.com/maven/dpfeffer/maven-repo/ratings-loader
```
> remember to publish

## SCDF Deployments

java -jar spring-cloud-dataflow-shell-2.1.2.RELEASE.jar
dataflow config server https://dataflow-server.apps.pcfone.io

app register trades-loader --type task --uri maven://io.pivotal.dragonstone-finance:trades-loader:0.0.6
app default --id task:trades-loader --version 0.0.6
app info trades-loader --type task
task create trades-loader-task --definition trades-loader
task validate trades-loader-task
task launch trades-loader-task --arguments "localFilePath=classpath:data.csv --spring.cloud.task.batch.fail-on-job-failure=true" --properties "deployer.trades-loader.cloudfoundry.services=app-db,config-server,discovery-server,volume-service,deployer.trades-loader.memory=768"

app register ratings-loader --type task --uri maven://io.pivotal.dragonstone-finance:ratings-loader:0.0.2
app default --id task:ratings-loader --version 0.0.2
app info ratings-loader --type task
task create ratings-loader-task --definition ratings-loader
task validate ratings-loader-task
task launch ratings-loader-task --arguments "localFilePath=classpath:data.csv --spring.cloud.task.batch.fail-on-job-failure=true" --properties "deployer.ratings-loader.cloudfoundry.services=app-db,config-server,volume-service,deployer.ratings-loader.memory=768"


app import https://dataflow.spring.io/rabbitmq-maven-latest




app register sftp-dataflow-persistent-metadata --type source --uri maven://org.springframework.cloud.stream.app:sftp-dataflow-source-rabbit:2.1.1


// OUT OF DATE: stream create inbound-sftp --definition "ftp --username=$USERNAME --password=$PW --host=ftp.101323.instanturl.net --allow-unknown-keys=true --remote-dir=webspace/httpdocs/dragonstone/trades --local-dir=/var/scdf/shared-files/ --task.launch.request.taskName=trades-loader-task | task-launcher-dataflow --spring.cloud.dataflow.client.server-uri=https://dataflow-server.apps.pcfone.io"

stream deploy inbound-ftp

Tests:
- DONE: Access ratings-api
- DONE: Access auditor-api
- DONE: Run trades-loader
- DONE: Run ratings-loader

TODO:
- Review all files
- Add the api call
- Update readme
- Consider snapshot storage in jcenter
- why does it run through all the files each time it is run
- why can't I get the batch job failure to fail the task
- need to get the share on the job

Work Arounds:
- OSS SDCF (waiting for SCDF for PCF 1.5.1)
- Global binding of services (waiting for instructions on how to have app specific bindings for task)

References:
- [Need 24x7 ETL? Then Move to Cloud Native File Ingest with Spring Cloud Data Flow](https://content.pivotal.io/blog/need-24x7-etl-then-move-to-cloud-native-file-ingest-with-spring-cloud-data-flow)


for ratings-loader

```bash
mvn deploy -Ddistribution.management.release.id=bintray -Ddistribution.management.release.url=https://api.bintray.com/maven/dpfeffer/maven-repo/sftp-dataflow-source-rabbit
```
> remember to publish
