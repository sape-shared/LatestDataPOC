no_executors=$1
executor_core=$2
file_path=$3
date=$4
no_of_days=$5

start_time=`date +'%Y%m%d %H%M%S'`
echo "Start Time -- " ${start_time} > insert_to_mongo_${start_time}.log

spark-submit --num-executors $no_executors --executor-cores $executor_core --driver-class-path ..\ --class com.mongopoc.crud.InsertL3DataInMongoAtlas LatestDataPOC.jar $file_path $date $no_of_days >> insert_to_mongo_${start_time}.log

end_time=`date +'%Y%m%d %H%M%S'`
echo "End Time -- " ${end_time} >> insert_to_mongo_${start_time}.log