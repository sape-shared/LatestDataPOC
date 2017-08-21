numThreads=$1
valuationDate=$2
snapshot=$3


START=`date +%Y%m%d%H%M%S`
for (( i=1; i <= ${numThreads}; ++i ))
do
	for (( j=1; j <= 14; ++j ))
		do
			nohup mongo "mongodb://cluster0-shard-00-00-oym47.mongodb.net:27017,cluster0-shard-00-01-oym47.mongodb.net:27017,cluster0-shard-00-02-oym47.mongodb.net:27017/test?replicaSet=Cluster0-shard-0" --authenticationDatabase admin --ssl --username shiva --password test_123 --eval "var valDate=${valuationDate},snapshot=${snapshot}" trade_ids_$j.js > FetchRisk_Thread$i_$j.log &
		done
done

wait
END=`date +%Y%m%d%H%M%S`
echo "Query Run Time : " ${duration} "seconds" >> FetchRisk_3.5mn_${numThreads}_Final.txt
