var startTime = new Date();

var op = db.movieDetails.aggregate([
{
	$match : {"runtime":100}
}],{
	cursor: {batchSize : 5000}
})

op.forEach(function(doc){})

var endTime = new Date();
print(endTime - startTime);