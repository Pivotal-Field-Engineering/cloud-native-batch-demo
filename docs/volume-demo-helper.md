# Volume Service Web Helper

Use the following aid to easily interact with provisioned volume services

1. Clone the repo: `git clone https://github.com/doddatpivotal/volume-demo`
2. Build the project: `mvn clean install`
3. Replace manifest.yml with the following

```yaml
applications:
- name: volume-demo-atrades
  instances: 1
  memory: 768mb
  buildpack: java_buildpack_offline
  path: target/volume-demo-0.1.1-SNAPSHOT.jar
  services:
  - volume-service
  env:
    STORAGE_PATH: /var/scdf/atrades-shared-files
- name: volume-demo-trades
  instances: 1
  memory: 768mb
  buildpack: java_buildpack_offline
  path: target/volume-demo-0.1.1-SNAPSHOT.jar
  services:
  - volume-service
  env:
    STORAGE_PATH: /var/scdf/trades-shared-files
- name: volume-demo-ratings
  instances: 1
  memory: 768mb
  buildpack: java_buildpack_offline
  path: target/volume-demo-0.1.1-SNAPSHOT.jar
  services:
  - volume-service
  env:
    STORAGE_PATH: /var/scdf/ratings-shared-files
```
4. Push the app
```bash
cf push
```