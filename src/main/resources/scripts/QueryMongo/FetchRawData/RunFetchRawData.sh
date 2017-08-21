valuationDate=$1
snapshot=$2
numThreads=$3

mkdir -p ../../../../output/QueryMongo/FetchOneDayRisk
chmod 777 ../../../../output/QueryMongo/FetchOneDayRisk

start=$(($(date +%s%N)/1000000))
for (( i=1; i <= ${numThreads}; ++i ))
do
    nohup sh FetchRawData.sh $valuationDate $snapshot ../../../../output/QueryMongo/FetchOneDayRisk/FetchRisk_${numThreads}_Concurrent.log > out.log &
done

wait
end=$(($(date +%s%N)/1000000))
duration=$(($end - $start))
echo "Total Concurrent Run Time : " ${duration} "milli seconds" >> ../../../../output/QueryMongo/FetchOneDayRisk/FetchRisk_${numThreads}_Concurrent.log
