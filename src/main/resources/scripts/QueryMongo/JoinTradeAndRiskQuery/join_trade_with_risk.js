db=db.getSiblingDB("HSBC")

books=[]
nbooks = 3
startbook=Math.floor(2500+(Math.random()*5000))
for(b=startbook;b<startbook+nbooks;b++) {
books.push(""+b+"")
}

var book = {$in : books}
var tradequery = {
          "index.tradebookId" :  book
        }
c=0;
datasize=0;

var before = new Date()

snapshot = {"valuationDate":"20170110","validTo":{$gt:NumberLong(20170110120000)},"validFrom":"20170110120000"}


print('Fetching Left Table')
printjson(tradequery)
var tradecursor = db.trades.find(tradequery)

tradeids  = []
trades = {}
while(tradecursor.hasNext()) {
        traderec = tradecursor.next()
        tradeid = traderec['index']['tradetradeId'];
        tradeids.push(tradeid)
        traderec['measures'] = []
        trades[tradeid] =  traderec
}

var after = new Date()
var execution_mills = after - before
print("Time (s) " + execution_mills/1000 +"\n");
exit
        measurequery =   { "valuationDate":"20170110",
        "validTo":{$gt:NumberLong(20170110120000)},
        "validFrom":"20170110120000",
        "riskSource.tradeId" : {"$in":tradeids}}

        var mescur = db.riskmeasure.find(measurequery)
        measures = []
        example = null
        while(mescur.hasNext())
        {
                mesrec  = mescur.next()
                id = mesrec.riskSource.tradeId
                example = id
                trades[id]['measures'].push(mesrec)
                c++;if(c%1000  == 0) { print(c) };
        }

printjson(trades[example])
for(t=0;t<tradeids.length;t++){
datasize=datasize+Object.bsonsize(trades[tradeids[t]])
}



print("Data Size (MB) " + datasize/(1024*1024) + " \n")
var after = new Date()
var execution_mills = after - before
print("Time (s) " + execution_mills/1000 +"\n");
