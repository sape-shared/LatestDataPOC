sh._adminCommand({enableSharding:'risk'})
db.createCollection('measurenested4')
sh.shardCollection('risk.measurenested4',{'riskSource.tradeId':'hashed'})
