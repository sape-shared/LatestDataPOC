numThreads=$1
valuationDate=$2
snapshot=$3


START=`date +%Y%m%d%H%M%S`
for (( i=1; i <= ${numThreads}; ++i ))
do
    nohup mongo "mongodb://cluster0-shard-00-00-oym47.mongodb.net:27017,cluster0-shard-00-01-oym47.mongodb.net:27017,cluster0-shard-00-02-oym47.mongodb.net:27017/test?replicaSet=Cluster0-shard-0" --authenticationDatabase admin --ssl --username shiva --password test_123 --eval "var valDate=${valuationDate},snapshot=${snapshot}" FetchRawData.js > FetchRisk_Thread$i.log &
done

wait
END=`date +%Y%m%d%H%M%S`
echo "Start Time -- ${START} and End Time -- ${END}"
