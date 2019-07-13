## If you need to use oss DF

cf create-service p.mysql db-small mysql
cf create-service p-rabbitmq standard rabbit
deploy spring-cloud-dataflow

wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-skipper-server/2.0.3.RELEASE/spring-cloud-skipper-server-2.0.3.RELEASE.jar
wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-server/2.1.2.RELEASE/spring-cloud-dataflow-server-2.1.2.RELEASE.jar
wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-shell/2.1.2.RELEASE/spring-cloud-dataflow-shell-2.1.2.RELEASE.jar

Add manifest for skipper-server
Add manifest for dataflow-server

cf push skipper-server
cf push dataflow-server

java -jar spring-cloud-dataflow-shell-2.1.2.RELEASE.jar
dataflow config server https://dataflow-server.apps.pcfone.io



stream create inbound-sftp-trades --definition "sftp-dataflow-persistent-metadata --password=KeepItSimple1! > :task-launcher-dataflow-destination"
stream create inbound-sftp-ratings --definition "sftp-dataflow-persistent-metadata --password=KeepItSimple1! > :task-launcher-dataflow-destination" 
stream create process-task-launch-requests --definition ":task-launcher-dataflow-destination > task-launcher-dataflow --spring.cloud.dataflow.client.server-uri=https://dataflow-server.apps.pcfone.io"

stream deploy inbound-sftp-trades --properties "deployer.sftp-dataflow-persistent-metadata.memory=768,deployer.sftp-dataflow-persistent-metadata.cloudfoundry.services=mysql,config-server"
stream deploy inbound-sftp-ratings --properties "deployer.sftp-dataflow-persistent-metadata.memory=768,deployer.sftp-dataflow-persistent-metadata.cloudfoundry.services=mysql,config-server"
stream deploy process-task-launch-requests --properties "deployer.task-launcher-dataflow.memory=768"
