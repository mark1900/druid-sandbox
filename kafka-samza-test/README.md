

Below is the documentation for [kafka-samza-test](https://github.com/mark1900/druid-sandbox/tree/master/kafka-samza-test).

The kafka-storm-topology-test project uses the following technologies:

* CentOS 7
* ZooKeeper 3.4.6
* Kafka 2.10-0.8.2.1
* Samza 0.9.1


Note:

* Remember to update the application's configuration.
** Default hostname:  hadoop-server (Edit /etc/hosts.  Cannot point to 127.0.0.1.)

# Standard Deployment

<pre><code>
cd ~/

mkdir kafka-storm-test

cd kafka-storm-test

wget http://www.us.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
wget http://www.us.apache.org/dist/kafka/0.8.2.1/kafka_2.10-0.8.2.1.tgz
wget http://www.us.apache.org/dist/hadoop/common/hadoop-2.7.1/hadoop-2.7.1.tar.gz

tar -xzf zookeeper-3.4.6.tar.gz
tar -xzf kafka_2.10-0.8.2.1.tgz
tar -xzf hadoop-2.7.1.tar.gz

./hadoop-2.7.0/bin/hadoop namenode -format
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka_samza_test_phase_01
./kafka_2.10-0.8.2.1/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka_samza_test_phase_02

</code></pre>

## Build Test Appliation

* kafka-samza-test-0.0.1-dist.tar.gz

## Deploy Test Application

<pre><code>
hadoop-server:~/tmp/kafka-samza-test-0.0.1-dist.tar.gz

cd ~/tmp
rm -rf kafka-samza-test && mkdir kafka-samza-test && tar -xvf kafka-samza-test-0.0.1-dist.tar.gz -C kafka-samza-test
mv ~/.samza ~/.samza-$(date +"%Y.%m.%d.%S.%N")
mkdir -p ~/.samza/conf && cp kafka-samza-test/config/standard/deploy/* ~/.samza/conf

 # https://samza.apache.org/learn/tutorials/0.9/run-in-multi-node-yarn.html
wget http://www.scala-lang.org/files/archive/scala-2.10.4.tgz
tar -xvf scala-2.10.4.tgz
rm kafka-samza-test/lib/scala-compiler-2.10.4.jar kafka-samza-test/lib/scala-library-2.10.4.jar
cp scala-2.10.4/lib/scala-compiler.jar scala-2.10.4/lib/scala-library.jar kafka-samza-test/lib/
cp scala-2.10.4/lib/scala-compiler.jar scala-2.10.4/lib/scala-library.jar hadoop-2.7.0/share/hadoop/hdfs/lib/
curl -L http://search.maven.org/remotecontent?filepath=org/clapper/grizzled-slf4j_2.10/1.0.1/grizzled-slf4j_2.10-1.0.1.jar > hadoop-2.7.0/share/hadoop/hdfs/lib/grizzled-slf4j_2.10-1.0.1.jar
curl -L http://search.maven.org/remotecontent?filepath=org/apache/samza/samza-yarn_2.10/0.8.0/samza-yarn_2.10-0.8.0.jar > hadoop-2.7.0/share/hadoop/hdfs/lib/samza-yarn_2.10-0.8.0.jar
curl -L http://search.maven.org/remotecontent?filepath=org/apache/samza/samza-core_2.10/0.8.0/samza-core_2.10-0.8.0.jar > hadoop-2.7.0/share/hadoop/hdfs/lib/samza-core_2.10-0.8.0.jar

# Update hadoop-2.7.0/conf/core-site.xml

<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
<configuration>
    <property>
      <name>fs.http.impl</name>
      <value>org.apache.samza.util.hadoop.HttpFileSystem</value>
    </property>
</configuration>


 # Start Hadoop....
 # http://hadoop-server:8088

 #kafka-samza-test/bin/kill-yarn-job.sh application_1440008845052_0008
kafka-samza-test/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/kafka-samza-test/config/standard/processing-stream-task.properties

</code></pre>

## Use Test Application

<pre><code>

 # Input Sample JSON to Kafka Topic
./kafka_2.10-0.8.2.1/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic kafka.kafka_samza_test_phase_01
# {"key1":"value1", "key2", "value2"}

 # View Sample JSON Output
./kafka_2.10-0.8.2.1/bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic kafka.kafka_samza_test_phase_02

</code></pre>



# Ambari Deployment


## Install and Configure

* https://cwiki.apache.org/confluence/display/AMBARI/Install+Ambari+2.1.0+from+Public+Repositories
* https://issues.apache.org/jira/browse/AMBARI-12793


<pre><code>
cd ~/

mkdir kafka-storm-test

cd kafka-storm-test

/usr/hdp/2.3.2.0-2621/hadoop/bin/hadoop namenode -format
/usr/hdp/2.3.2.0-2621/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka_samza_test_phase_01
/usr/hdp/2.3.2.0-2621/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka_samza_test_phase_02


</code></pre>

## Build Test Appliation

* kafka-samza-test-0.0.1-dist.tar.gz

## Deploy Test Application

<pre><code>
hadoop-server:~/tmp/kafka-samza-test-0.0.1-dist.tar.gz

cd ~/tmp
rm -rf kafka-samza-test && mkdir kafka-samza-test && tar -xvf kafka-samza-test-0.0.1-dist.tar.gz -C kafka-samza-test
mv ~/.samza ~/.samza-$(date +"%Y.%m.%d.%S.%N")
mkdir -p ~/.samza/conf && cp kafka-samza-test/config/ambari/deploy/* ~/.samza/conf


 # Start Hadoop Services using Ambari
 # http://hadoop-server:8080
 # http://hadoop-server:8088

 # kafka-samza-test/bin/kill-yarn-job.sh application_1440008845052_0008
 # /usr/hdp/2.3.2.0-2621/hadoop/bin/yarn application -kill application_1440008845052_0011
kafka-samza-test/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/kafka-samza-test/config/ambari/processing-stream-task.properties

</code></pre>

## Use Test Application

<pre><code>

cd /usr/hdp/2.3.2.0-2621/kafka

 # Input Sample JSON to Kafka Topic
bin/kafka-console-producer.sh --broker-list localhost:6667 --topic kafka.kafka_samza_test_phase_01
# {"key1":"value1", "key2", "value2"}

 # View Sample JSON Output
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --from-beginning --topic kafka.kafka_samza_test_phase_02

</code></pre>
