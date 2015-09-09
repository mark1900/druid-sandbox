

Below is the documentation for [kafka-gobblin-hdfs-test](https://github.com/mark1900/druid-sandbox/tree/master/kafka-gobblin-hdfs-test).

The kafka-gobblin-hdfs-test project uses the following technologies:

* CentOS 7
* Ambari 2.1.0
    * ZooKeeper 3.4.6
    * Kafka 0.8.2
    * Hadoop (HDFS, MapReduce2, YARN) 2.7.1
    * Pig 0.15.0
    * Tez 0.7.0
    * Hive 1.2.1


Goal

To utilize Gobblin to create a Hadoop MapReduce Job that will:

1. Extract Kafka Topic Messages
2. Transform the JSON Message payload into the AVRO format.
3. Push data into HDFS


Notes:

* Remember to update the application's configuration.
    * Default hostname:  server1 (/etc/hosts.  Cannot point to 127.0.0.1.)

See also:
* https://github.com/linkedin/gobblin/wiki/Kafka-HDFS-Ingestion
* https://github.com/linkedin/gobblin/wiki/Gobblin%20Deployment#Hadoop-MapReduce-Deployment


# Ambari Deployment


## Install and Configure Environment

* https://cwiki.apache.org/confluence/display/AMBARI/Install+Ambari+2.1.0+from+Public+Repositories
* https://issues.apache.org/jira/browse/AMBARI-12793


<pre><code>
cd ~/

mkdir ~/tmp

cd kafka-storm-test

/usr/hdp/2.3.2.0-2621/hadoop/bin/hadoop namenode -format
/usr/hdp/2.3.2.0-2621/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic kafka-gobblin-hdfs-test

</code></pre>


## Build Test Appliation

* Build Gobblin from source
    * https://github.com/linkedin/gobblin.git
    * Set GOBBLIN_HOME environment variable (See kafka-gobblin-hdfs-test/pom.xml).

* Build this Maven project kafka-gobblin-hdfs-test
    * mvn clean package
    * kafka-gobblin-hdfs-test-0.0.0-dist.tar.gz


## Configuration Gobblin


<pre><code>

 vim ~/.bashrc

 # Add the following entries
export HADOOP_BIN_DIR=/usr/hdp/2.3.2.0-2621/hadoop/bin

</code></pre>


<pre><code>

mkdir ~/tmp && cd ~/tmp

 # Copy required files

cd ~/tmp/

tar -zxvf kafka-gobblin-hdfs-test-0.0.0.jar
tar -zxvf gobblin-dist.tar.gz

 # Update the Gobblin config file server1 references (fs.uri=hdfs://server1:8020)
vim gobblin-dist/conf/gobblin-mapreduce.properties

 # Update the Job config file server1 references
vim kafka-gobblin-hdfs-test.pull

 # Fix Windows encoding
yum install -y dos2unix
find . -type f -name "*.sh" -exec dos2unix {} \;
chmod +x bin/gobblin-mapreduce.sh

cd gobblin-dist
./bin/gobblin-mapreduce.sh --jars ../kafka-gobblin-hdfs-test-0.0.0.jar --conf ../kafka-gobblin-hdfs-test.pull --workdir hdfs://server1:8020/gobblin/work

</code></pre>




## Deploy Test Application

<pre><code>

 # Start Hadoop Services using Ambari
 # http://server1:8080
 # http://server1:8088

cd ~/tmp

sudo usermod -a -G hdfs,hadoop $USER

cd gobblin-dist
./bin/gobblin-mapreduce.sh --jars ../kafka-gobblin-hdfs-test-0.0.0.jar --conf ../kafka-gobblin-hdfs-test.pull --workdir hdfs://server1:8020/gobblin/work


 # Kill running application
 # /usr/hdp/2.3.2.0-2621/hadoop/bin/yarn application -kill application_1440008845052_0011

 # View working data
 # /usr/hdp/2.3.2.0-2621/hadoop/bin/hadoop fs -ls /gobblin/work/
 # Erase all Gobblin locks (Set in kafka-gobblin-hdfs-test.pull)
 # /usr/hdp/2.3.2.0-2621/hadoop/bin/hadoop fs -rm -r -skipTrash /gobblin/work/locks


</code></pre>


## Use Test Application


Create Kafka Message


* Option 1:

  Run Java Application test.KafkaMessageProducer


* Option 2:


<pre><code>

/usr/hdp/2.3.2.0-2621/kafka/bin/kafka-console-producer.sh --broker-list localhost:6667 --topic kafka-gobblin-hdfs-test
 # JSON Sample
{"version":"2.0","secret":"****","events": [{"type": "SYSTEM","level": 3,"message": "CPU under heavy load for 5 minutes"},{"type": "SYSTEM","level": 5,"message": "RAM usage above 90 per cent utilization"},{"type": "APPLICATION","level": 7,"message": "RAM usage above 90 per cent utilization"},{"type": "SECURITY","level": 10,"message": "Hack attack!"}]}

</code></pre>


View Gobblin Ingestion

<pre><code>

 # View working data
 /usr/hdp/2.3.2.0-2621/hadoop/bin/hadoop fs -ls /gobblin/work/

</code></pre>
