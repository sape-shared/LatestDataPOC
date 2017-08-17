var startTime = new Date();
var op = db.measurenested4.aggregate([
{
	$match : {"valuationDate":NumberLong(valDate),"validTo":{$gt:NumberLong(snapshot)},"validFrom":{$lte:NumberLong(snapshot)}}
}],{
	cursor: {batchSize : 5000}
})

op.forEach(function(doc){})

var endTime = new Date();
print(endTime - startTime);
