
Below is the documentation for [kafka-storm-tranquility-druid-topology-test](https://github.com/mark1900/druid-sandbox/tree/master/kafka-storm-tranquility-druid-topology-test).

This project utilizes a Storm "TridentTopology" as a means to transactionally consume messages from a Kafka Spout (Via the TransactionalTridentKafkaSpout class) and after processing, output to the results to Druid (via Tranquility's TridentBeamStateFactory and TridentBeamStateUpdater classes). For more details, see [StormTranquilityTopologyTest.java](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-druid-topology-test/src/main/java/test/storm/StormTranquilityTopologyTest.java).

The kafka-storm-tranquility-druid-topology-test project uses the following technologies:

* CentOS 7
* PostgreSQL 9.2.x
* ZooKeeper 3.4.6
* Kafka 2.10-0.8.2.1
* Apache Storm 0.9.4
* Tranquility 2.10-0.4.2
* Druid 0.7.3

This Storm Topology test assumes that all required services are installed onto the same server.

* Remember to update the application's configuration.
* Remember to check your firewall settings.

# Server Configuration

## Download

<pre><code>
cd ~/

mkdir kafka-storm-tranquility-druid-test

cd kafka-storm-tranquility-druid-test

wget http://www.us.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
wget http://www.us.apache.org/dist/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz
wget http://www.us.apache.org/dist/storm/apache-storm-0.9.4/apache-storm-0.9.4.tar.gz
wget http://static.druid.io/artifacts/releases/druid-0.7.3-bin.tar.gz

tar -xzf zookeeper-3.4.6.tar.gz
tar -xzf kafka_2.10-0.8.2.1.tgz
tar -xzf apache-storm-0.9.4.tar.gz
tar -xzf druid-0.7.3-bin.tar.gz
</code></pre>

## ZooKeeper Configuration

<pre><code>
cp zookeeper-3.4.6/conf/zoo_sample.cfg zookeeper-3.4.6/conf/zoo.cfg
</code></pre>

## Storm Configuration

** Default Configuration: https://github.com/apache/storm/blob/master/conf/defaults.yaml

<pre><code>
vim apache-storm-0.9.4/conf/storm.yaml

storm.zookeeper.servers:
    - "127.0.0.1"

storm.local.dir: "/tmp/storm/localStorage"

nimbus.host: "127.0.0.1"

ui.port: 28080
</code></pre>

## Druid Configuration

<pre><code>
vim druid-0.7.3/config/_common/common.runtime.properties

 # Extensions
druid.extensions.coordinates=["io.druid.extensions:postgresql-metadata-storage","io.druid.extensions:druid-kafka-eight"]

 # Zookeeper
druid.zk.service.host=localhost

 # Metadata Storage
druid.metadata.storage.type=postgresql
druid.metadata.storage.connector.connectURI=jdbc\:postgresql\://localhost\:5432/druid
druid.metadata.storage.connector.user=druid
druid.metadata.storage.connector.password=diurd

 # Deep storage
druid.storage.type=local
druid.storage.storageDirectory=/tmp/druid/localStorage

 # Query Cache
druid.cache.type=local
druid.cache.sizeInBytes=10000000

 # Indexing service discovery
druid.selectors.indexing.serviceName=overlord

 # Metrics logging
druid.emitter=noop
</code></pre>

## Curl Configuration For Druid Queries

<pre><code>
vim ~/.curlrc
-w "\n"
</code></pre>

## PostgreSQL Configuration For Druid

### CentOS 7

<pre><code>
 # http://www.postgresql.org/download/linux/redhat/

yum install postgresql-server
postgresql-setup initdb

systemctl stop postgresql.service

vim /var/lib/pgsql/data/pg_hba.conf
local   all             all                                     peer
host    all             all             127.0.0.1/32            trust
host    all             all             ::1/128                 ident
host    all             all             0.0.0.0/0               md5

vim /var/lib/pgsql/data/postgresql.conf
listen_addresses = '*'
port = 5432

systemctl enable postgresql.service
systemctl start postgresql.service

su -l postgres
psql

CREATE USER druid WITH PASSWORD 'diurd';
CREATE DATABASE druid ENCODING 'UTF8';
GRANT ALL PRIVILEGES ON DATABASE druid to druid;
</code></pre>

# Running the Server Applications

For convenience I have created the following scripts:

* [start-all.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-druid-topology-test/misc/bash-scripts/start-all.sh)
* [stop-all.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-druid-topology-test/misc/bash-scripts/stop-all.sh)
* [x-purge-state.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-druid-topology-test/misc/bash-scripts/x-purge-state.sh)

## Running ZooKeeper

<pre><code>
cd zookeeper-3.4.6

./bin/zkServer.sh start

./bin/zkServer.sh stop
</code></pre>

## Running Kafka

<pre><code>
cd kafka_2.10-0.8.2.1

./bin/kafka-server-start.sh config/server.properties

./bin/kafka-server-stop.sh
</code></pre>

## Running Storm

<pre><code>
cd apache-storm-0.9.4

./bin/storm nimbus

./bin/storm supervisor

./bin/storm ui
</code></pre>

## Running Druid

<pre><code>
cd druid-0.7.3

java -Xmx1g -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/overlord:lib/*:${HADOOP_CONFIG_PATH} io.druid.cli.Main server overlord

java -Xms64m -Xmx64m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/middlemanager:lib/*:${HADOOP_CONFIG_PATH} io.druid.cli.Main server middleManager

java -Xmx256m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/coordinator:lib/* io.druid.cli.Main server coordinator

java -Xmx256m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -classpath config/_common:config/broker:lib/* io.druid.cli.Main server broker

 # Realtime node instances.

 # java -Xmx512m -Duser.timezone=UTC -Dfile.encoding=UTF-8 -Ddruid.realtime.specFile=&lt;path-to-runtime-spec-file&gt; -classpath config/_common:config/realtime:lib/* io.druid.cli.Main server realtime
</code></pre>


# Monitoring the Server Applications

* apache-storm-0.9.4/logs/worker-*.log
* http://&lt;druid-overlord-instance-hostname&gt;:8090/console.html
* http://&lt;storm-ui-instance-hostname&gt;:28080/index.html

# Building and Deploying the Storm Topology

## Manual Kafka Topic Creation

<pre><code>
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-tranquility-druid-topology-test_v1
</code></pre>

## Building the Storm Topology

Update the Application configuration values in the source file:

* kafka-storm-tranquility-druid-topology-test/src/main/java/test/storm/AppConfiguration.java

Build the Storm Topology

* Build with maven ("mvn clean package") to create the Storm Topology jar file: "kafka-storm-tranquility-druid-topology-test.jar".


## Manual Storm Topology Submission

<pre><code>
./apache-storm-0.9.4/bin/storm kill "kafka-storm-tranquility-druid-topology-test"

./apache-storm-0.9.4/bin/storm jar kafka-storm-tranquility-druid-topology-test.jar test.storm.StormTranquilityTopologyTest
</code></pre>

# Executing the Test

## Manual Kafka Test Message Creation

Execute Java class file:

* kafka-storm-tranquility-druid-topology-test/src/test/java/test/kafka/KafkaMessageProducer.java


## Druid Sample Queries

<pre><code>
curl -X POST "http://localhost:8082/druid/v2/?pretty" -H 'Content-type: application/json' -d '{"queryType":"timeBoundary","dataSource":"kafka-storm-tranquility-druid-topology-test-datasource"}'

[ {
  "timestamp" : "2015-06-15T18:17:13.000Z",
  "result" : {
    "minTime" : "2015-06-15T18:17:13.000Z",
    "maxTime" : "2015-06-15T18:18:43.000Z"
  }
} ]


curl -X POST "http://localhost:8082/druid/v2/?pretty" -H 'Content-type: application/json' -d '{"queryType": "groupBy","dataSource": "kafka-storm-tranquility-druid-topology-test-datasource","granularity": "all","dimensions": [ "type" ],"aggregations": [{ "type": "count", "name": "rows" }],"intervals": ["2002-02-01T00:00/2020-01-01T00"]}'

[ {
  "version" : "v1",
  "timestamp" : "2002-02-01T00:00:00.000Z",
  "event" : {
    "type" : "APPLICATION",
    "rows" : 2
  }
}, {
  "version" : "v1",
  "timestamp" : "2002-02-01T00:00:00.000Z",
  "event" : {
    "type" : "SECURITY",
    "rows" : 2
  }
}, {
  "version" : "v1",
  "timestamp" : "2002-02-01T00:00:00.000Z",
  "event" : {
    "type" : "SYSTEM",
    "rows" : 4
  }
} ]
</code></pre>

