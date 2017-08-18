user = $1
pass = $2

mongo "mongodb://testcluster-shard-00-00-ctvgt.mongodb.net:27017,testcluster-shard-00-01-ctvgt.mongodb.net:27017,testcluster-shard-00-02-ctvgt.mongodb.net:27017/test?replicaSet=TestCluster-shard-0" --authenticationDatabase admin --ssl --username $user --password $pass CleanCollection.js
mongo "mongodb://testcluster-shard-00-00-ctvgt.mongodb.net:27017,testcluster-shard-00-01-ctvgt.mongodb.net:27017,testcluster-shard-00-02-ctvgt.mongodb.net:27017/test?replicaSet=TestCluster-shard-0" --authenticationDatabase admin --ssl --username $user --password $pass CreateDBandCollection.js
mongo "mongodb://testcluster-shard-00-00-ctvgt.mongodb.net:27017,testcluster-shard-00-01-ctvgt.mongodb.net:27017,testcluster-shard-00-02-ctvgt.mongodb.net:27017/test?replicaSet=TestCluster-shard-0" --authenticationDatabase admin --ssl --username $user --password $pass CreateIndex.js