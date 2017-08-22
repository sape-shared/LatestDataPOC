#!/usr/bin/env bash

no_executors=$1
executor_core=$2

start_time=`date +'%Y%m%d%H%M%S'`
echo "Start Time -- " $start_time > spark_mongo_join_$start_time.log

spark-submit --num-executors $no_executors --executor-cores $executor_core --driver-class-path ../../:/etc/hadoop/conf:/etc/hive/conf:/usr/lib/hadoop-lzo/lib/*:/usr/share/aws/aws-java-sdk/*:/usr/share/aws/emr/emrfs/conf:/usr/share/aws/emr/emrfs/lib/*:/usr/share/aws/emr/emrfs/auxlib/* --class com.mongopoc.crud.DataSparkQueryMain ../../lib/LatestDataPOC.jar >> spark_mongo_join_$start_time.log

end_time=`date +'%Y%m%d%H%M%S'`
echo "End Time -- " $end_time >> spark_mongo_join_$start_time.log
