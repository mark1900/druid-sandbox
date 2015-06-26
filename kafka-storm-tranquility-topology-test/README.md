
Below is the documentation for [kafka-storm-tranquility-topology-test](https://github.com/mark1900/druid-sandbox/tree/master/kafka-storm-tranquility-topology-test).

This project utilizes a Storm "TridentTopology" as a means to transactionally consume messages from a Kafka Spout (Via the TransactionalTridentKafkaSpout class) and after processing, output to the results back to Kafka (via Tranquility's TridentBeamStateFactory and TridentBeamStateUpdater classes).  The topologies are described here:
* [StormTranquilityStandardTopologyTest.java](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-topology-test/src/main/java/test/storm/StormTranquilityStandardTopologyTest.java).
* [StormTranquilityImprovedTopologyTest.java](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-topology-test/src/main/java/test/storm/StormTranquilityImprovedTopologyTest.java).

The kafka-storm-tranquility-topology-test project uses the following technologies:

* CentOS 7
* ZooKeeper 3.4.6
* Kafka 2.10-0.8.2.1
* Apache Storm 0.9.4
* Tranquility 2.10-0.4.2

This Storm Topology test assumes that all required services are installed onto the same server.

* Remember to replace the "&lt;sever-ip-address&gt;" placeholder.
* Remember to check your firewall settings.

# Server Configuration

## Download

<pre><code>
cd ~/

mkdir kafka-storm-tranquility-test

cd kafka-storm-tranquility-test

wget http://www.us.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
wget http://www.us.apache.org/dist/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz
wget http://www.us.apache.org/dist/storm/apache-storm-0.9.4/apache-storm-0.9.4.tar.gz

tar -xzf zookeeper-3.4.6.tar.gz
tar -xzf kafka_2.10-0.8.2.1.tgz
tar -xzf apache-storm-0.9.4.tar.gz
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
    - "&lt;sever-ip-address&gt;"

storm.local.dir: "/tmp/storm/localStorage"

nimbus.host: "&lt;sever-ip-address&gt;"

ui.port: 28080
</code></pre>


# Running the Server Applications

For convenience I have created the following scripts:

* [start-all.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-topology-test/misc/bash-scripts/start-all.sh)
* [stop-all.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-topology-test/misc/bash-scripts/stop-all.sh)
* [x-purge-state.sh](https://github.com/mark1900/druid-sandbox/blob/master/kafka-storm-tranquility-topology-test/misc/bash-scripts/x-purge-state.sh)

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

# Monitoring the Server Applications

* apache-storm-0.9.4/logs/worker-*.log
* http://&lt;storm-ui-instance-hostname&gt;:28080/index.html

# Building and Deploying the Storm Topology

## Manual Kafka Topic Creation

<pre><code>
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-tranquility-topology-test_v1_phase1
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-storm-tranquility-topology-test_v1_phase2
</code></pre>

## Building the Storm Topology

Update the Application configuration values (e.g. &lt;sever-ip-address&gt;) in the source file:

* kafka-storm-tranquility-topology-test/src/main/java/test/storm/AppConfiguration.java

Build the Storm Topology

* Build with maven ("mvn clean package") to create the Storm Topology jar file: "kafka-storm-tranquility-topology-test.jar".


## Manual Storm Topology Submission

<pre><code>
./apache-storm-0.9.4/bin/storm kill "kafka-storm-tranquility-topology-test"

# Option1
./apache-storm-0.9.4/bin/storm jar kafka-storm-tranquility-topology-test.jar test.storm.StormTranquilityStandardTopologyTest
# Option 2
#./apache-storm-0.9.4/bin/storm jar kafka-storm-tranquility-topology-test.jar test.storm.StormTranquilityImprovedTopologyTest
</code></pre>

# Executing the Test

## Manual Kafka Test Message Creation

Execute Java class file:

* kafka-storm-tranquility-topology-test/src/test/java/test/kafka/KafkaMessageProducer.java


## Kafka Topic Listeners

<pre><code>
./kafka_2.10-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic kafka-storm-tranquility-topology-test_v1_phase1
./kafka_2.10-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic kafka-storm-tranquility-topology-test_v1_phase2
</code></pre>

