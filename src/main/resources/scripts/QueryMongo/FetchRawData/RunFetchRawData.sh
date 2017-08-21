valuationDate=$1
snapshot=$2
numThreads=$3


start=$(($(date +%s%N)/1000000))
for (( i=1; i <= ${numThreads}; ++i ))
do
    nohup sh FetchRawData.sh $valuationDate $snapshot FetchRisk_${numThreads}_Concurrent.log > out.log &
done

wait
end=$(($(date +%s%N)/1000000))
duration=$(($end - $start))
echo "Query Total Run Time : " ${duration} "milli seconds" >> FetchRisk_${numThreads}_Concurrent.log
