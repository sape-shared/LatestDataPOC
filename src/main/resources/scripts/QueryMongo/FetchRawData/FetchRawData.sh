valuationDate=$1
snapshot=$2
outputFile=$3

start=$(($(date +%s%N)/1000000))
mongo "mongodb://cluster0-shard-00-00-oym47.mongodb.net:27017,cluster0-shard-00-01-oym47.mongodb.net:27017,cluster0-shard-00-02-oym47.mongodb.net:27017/risk?replicaSet=Cluster0-shard-0" --authenticationDatabase admin --ssl --username shiva --password test_123 --eval "var valDate=${valuationDate},snapshot=${snapshot}" FetchRawData.js > FetchRisk_${i}_Concurrent.log &
end=$(($(date +%s%N)/1000000))
duration=$(($end - $start))
echo "Query Run Time : " ${duration} "milli seconds" >> ${outputFile}