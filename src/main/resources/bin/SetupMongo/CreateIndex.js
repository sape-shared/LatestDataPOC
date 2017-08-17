db.measurenested4.createIndex({'valuationContext.batchTimeStamp':1})
db.measurenested4.createIndex({'validTo':1,'validFrom':1,'valuationDate':1,'measure.name':1,'measure.snapshotcode':1,'valuationContext.description':1},{'name':'latestdata'})
db.measurenested4.createIndex({'riskSource.tradeId':1,'valuationDate':1,'measure.name':1,'validTo':1,'valuationContext.description':1},{'name':'expireRecord'})
db.measurenested4.createIndex({'riskFactor1.curveName':1,'measure.name':1,'axisDetails.axisPoints.xAxisPoints.label':1},{'name':'measureValueSum'})