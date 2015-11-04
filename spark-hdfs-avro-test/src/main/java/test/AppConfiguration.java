package test;


/**
 *  In Eclipse override via the Run > Run Configurations > Project > Arguments > VM Arguments. eg. -Dkey=value
 */
@SuppressWarnings( "nls" )
public class AppConfiguration
{

    public static final boolean RUN_SPARK_LOCALLY = Boolean.parseBoolean( System.getProperty( "test.spark-hdfs-avro-test.run_spark_locally", "true" ) );

    // Apache Ambari Deployment
    public static final String HADOOP_BASE_URI = System.getProperty( "test.spark-hdfs-avro-test.hadoop.uri.base", "hdfs://hadoop-hostname:8020" );

    // Standard Deployment
//    public static final String HADOOP_BASE_URI = System.getProperty( "test.spark-hdfs-avro-test.hadoop.uri.base", "hdfs://hadoop-hostname:9000" );

    public static final String HADOOP_INPUT_FILE_PATH = System.getProperty(
        "test.spark-hdfs-avro-test.hadoop.file.input.path", "/gobblin/work/job-output/my-test-input.avro" );

//    public static final String HADOOP_INPUT_FILE_PATH = System.getProperty(
//        "test.spark-hdfs-avro-test.hadoop.file.input.path", "/gobblin/work/job-output/KAFKA/kafka-gobblin-hdfs-test/20150925152504_append/part.task_kafka-gobblin-hdfs-test_1443194701556_0.avro" );

    public static final String HADOOP_OUTPUT_DIRECTORY_PATH = System.getProperty(
        "test.spark-hdfs-avro-test.hadoop.file.output.path", "/gobblin/work/job-output/my-test-output" );



}
