valuationDate=$1
snapshot=$2
numThreads=$3


start=$(date +%s)
for (( i=1; i <= ${numThreads}; ++i ))
do
    nohup mongo "mongodb://cluster0-shard-00-00-oym47.mongodb.net:27017,cluster0-shard-00-01-oym47.mongodb.net:27017,cluster0-shard-00-02-oym47.mongodb.net:27017/risk?replicaSet=Cluster0-shard-0" --authenticationDatabase admin --ssl --username shiva --password test_123 --eval "var valDate=${valuationDate},snapshot=${snapshot}" FetchRawData.js > FetchRisk_${i}_Concurrent.log &
done

wait
end=$(date +%s)
duration=$(($end - $start))
echo "Query Run Time : " ${duration} "seconds" >> FetchRisk_${numThreads}_Concurrent.log
