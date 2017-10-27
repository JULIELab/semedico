for index in relations; do
	curl -XPUT "http://localhost:9200/$index/items/fullhit1" -d @documents/fullhit1.json
	curl -XPUT "http://localhost:9200/$index/items/parthit1" -d @documents/parthit1.json
	curl -XPUT "http://localhost:9200/$index/items/parthit2" -d @documents/parthit2.json
	curl -XPUT "http://localhost:9200/$index/items/parthit3" -d @documents/parthit3.json
	curl -XPUT "http://localhost:9200/$index/items/parthit4" -d @documents/parthit4.json
	curl -XPUT "http://localhost:9200/$index/items/unary1" -d @documents/unary1.json
	curl -XPUT "http://localhost:9200/$index/items/infg1" -d @documents/infg1.json
	curl -XPUT "http://localhost:9200/$index/items/infg2" -d @documents/infg2.json
done