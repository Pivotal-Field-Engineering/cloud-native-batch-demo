# Dragonstone Finance Demo

## Prerequisites

1. Create all services
```bash
cf create-service p-dataflow standard data-flow -c '{"concurrent-task-limit": 2, "scheduler": {"name": "scheduler-for-pcf", "plan": "standard"},"maven.remote-repositories.bintray.url": "https://dl.bintray.com/dpfeffer/maven-repo"}'

cf create-service p-service-registry standard discovery-server 
cf create-service p-config-server standard config-server -c '{"git":{"uri":"https://github.com/doddatpivotal/dragonstone-finance.git","searchPaths":"dragonstone-finance-config","label":"master"}}'

cf create-service nfs Existing volume-service -c '{"share":"nfs-pcf.pez.pivotal.io/pcfone/dpfeffer","uid":"$EMPID","gid":"$EMPID", "mount":"/var/scdf"}'
cf create-service p.mysql db-small app-db

```
2. Deploy volume demo apps. [Instructions](volume-demo-helper.md)
3. Deploy pivotal-mysql-web apps.  [Instructions](mysql-web-helper.md)
4. Deploy auditor-api, but leave ratings-api un-deployed so you can do that in the demo
```bash
cd auditor-api
cf push
```
5. Setup sftp server. Used [this blog](https://medium.com/@biancalorenpadilla/sftp-google-cloud-storage-d559fd16e074) to create sftp server on GCP for the demo.  Remember to create firewall rule allowing inbound access on 22.
6. Delete all data from ratings

## Demo

- Introduce Apps Manager and show the services
- Show that the following services have been provisioned and how you would do that
    - mysql dbs
    - volume service
    - service registry
    - config server
    - SCDF for PCF service
    - scheduler
- Show that auditor-api has already been deployed
- Deploy the ratings-api
- Launch spring cloud data flow
- Show the apps
- Register my task apps
- Create the tasks
- Launch ratings task
    - Call the http endpoint and see no data
    - Show that the app has been pushed to cloud foundry
    - Look at the results of the logs
    - See the execution in the UI
    - Call the http endpoint and see data has been loaded
- Create the streams
    - Send files to the sftp sites
    - See the executions in the UI
    - Call the http endpoint and see data has been loaded based upon the info in ratings
- Schedule the summary job
    - See the executions

## Steps

1. Introduce Apps Manager and Services
- Go to Apps Manager UI
- Review already provisioned services
- Show how to create a service
    - mysql dbs
    - volume service
    - service registry
    - config server
    - SCDF for PCF service
    - scheduler

2. Deploy an app and show service registry and config server
- Show manifest.yml
```bash
cf push
```
- Using Apps Manager, navigate to the Service Registry
- Show config server: `https://spring-cloud-broker.apps.pcfone.io/dashboard/p-config-server/6b914931-d2cb-46f1-90d6-1307733363e3`
- Follow through to github

3. Introduce SCDF UI and shell
- Navigate to SDCF UI through service in Apps Man
- Using terminal
```bash
cf dataflow-shell data-flow
```

4. Register Task Apps
```scdf
app register ratings-loader --type task --uri maven://io.pivotal.dragonstone-finance:ratings-loader:0.0.26
app register trades-loader --type task --uri maven://io.pivotal.dragonstone-finance:trades-loader:0.0.24
```

5. Create Tasks
> Note: Update with the relational database service name  
```scdf
task create ratings-loader-task --definition "ratings-loader --io.pivotal.dataflow-db-service-name=relational-de67ee92-ce90-40ae-9884-6434c71597d1"
task create trades-loader-task --definition "trades-loader --spring.cloud.task.batch.jobNames=tradesLoaderJob --io.pivotal.dataflow-db-service-name=relational-de67ee92-ce90-40ae-9884-6434c71597d1"
task create a-trades-extractor-task --definition "trades-loader --spring.cloud.task.batch.jobNames=aRatedTradesExtractorJob --io.pivotal.dataflow-db-service-name=relational-de67ee92-ce90-40ae-9884-6434c71597d1"
```

6. Test
- Show no data in ratings
```bash
http https://ratings-api.apps.pcfone.io/ratings
```
- Launch the ratings loader task using class path file
```scdf
task launch ratings-loader-task --arguments "localFilePath=classpath:data.csv --spring.cloud.task.batch.failOnJobFailure=true" --properties "deployer.ratings-loader.cloudfoundry.services=app-db,config-server,volume-service deployer.ratings-loader.memory=768"
```
- Show app is now in cloud foundry
```bash
cf apps
```
- Let's look at the logs
```bash
cf logs ratings-loader-task
```
- Check on the SCDF UI for executions
- Check on the api to for any new data 
```bash
http https://ratings-api.apps.pcfone.io/ratings
```

7. Create the streams
```scdf
app register sftp-dataflow-persistent-metadata --type source --uri maven://io.pivotal.dragonstone-finance:sftp-dataflow-source-rabbit:2.1.7
app import https://dataflow.spring.io/rabbitmq-maven-latest

stream create inbound-sftp-trades --definition "sftp-dataflow-persistent-metadata --password=$PASSWORD > :task-launcher-dataflow-destination"
stream create inbound-sftp-ratings --definition "sftp-dataflow-persistent-metadata --password=$PASSWORD > :task-launcher-dataflow-destination" 
stream create process-task-launch-requests --definition ":task-launcher-dataflow-destination > task-launcher-dataflow --spring.cloud.dataflow.client.server-uri=https://dataflow-server.apps.pcfone.io"

stream deploy inbound-sftp-trades --properties "deployer.sftp-dataflow-persistent-metadata.deleteRoute=true,deployer.sftp-dataflow-persistent-metadata.memory=768,deployer.sftp-dataflow-persistent-metadata.cloudfoundry.services=app-db,config-server,volume-service"
stream deploy inbound-sftp-ratings --properties "deployer.sftp-dataflow-persistent-metadata.memory=768,deployer.sftp-dataflow-persistent-metadata.cloudfoundry.services=app-db,config-server,volume-service"
stream deploy process-task-launch-requests --properties "deployer.task-launcher-dataflow.memory=768"
```

8. Test the streams
- Show now data in ratings
```bash
./scripts/transfer-trades-data.sh
./scripts/transfer-ratings-data.sh
```
- Check on the SCDF UI for executions
- Check on the api to for any new data with ratings 
```bash
http https://auditor-api.apps.pcfone.io/trades
```

9. Schedule the extract task
- Using the UI, create a new scheduled task with following info
    - Name: `aTradesExtractMinutely` 
    - Cron Expression: `0/1 * 1/1 * ?`
    - Arguments: `localFilePath=/var/scdf/atrades-shared-files/ tempDir=/home/vcap/`
    - Parameters: `deployer.trades-loader.cloudfoundry.services=app-db,config-server,discovery-server,volume-service,relational-de67ee92-ce90-40ae-9884-6434c71597d1
                   deployer.trades-loader.memory=768`        
- Using the UI, create a new scheduled task with following info
    - Name: `aTradesExtractDaily` 
    - Cron Expression: `0 8 * * ?`
    - Arguments: `localFilePath=/var/scdf/atrades-shared-files/ tempDir=/home/vcap/`
    - Parameters: `deployer.trades-loader.cloudfoundry.services=app-db,config-server,discovery-server,volume-service,relational-de67ee92-ce90-40ae-9884-6434c71597d1
                   deployer.trades-loader.memory=768`        
- See the executions

## Tear Down to Repeat Demo

1. Delete ratings api
```bash
cf delete ratings-api -f -r
```

2. Delete ratings and trade data
```sql
delete from rating;
delete from trade;
```

3. Delete scheduled task definition
```bash
http delete https://dataflow-server.apps.pcfone.io/tasks/schedules/aTradesExtractMinutely
```

4. Destroy and Unregister Tasks
```scdf
task destroy a-trades-extractor-task
task destroy ratings-loader-task
task destroy trades-loader-task
app unregister ratings-loader --type task
app unregister trades-loader --type task
```

5. Undeploy and destroy streams
```scdf
stream undeploy inbound-sftp-ratings
stream undeploy inbound-sftp-trades
stream undeploy process-task-launch-requests
stream destroy inbound-sftp-ratings
stream destroy inbound-sftp-trades
stream destroy process-task-launch-requests
```

6. Delete the scheduled task app
```bash
cf d a-trades-extractor-task -f
```

7. Delete files from volume service
```bash
http https://volume-demo-atrades.apps.pcfone.io/delete_all
```

## Tear Down Pre-reqs

1. Complete the previous teardown steps
