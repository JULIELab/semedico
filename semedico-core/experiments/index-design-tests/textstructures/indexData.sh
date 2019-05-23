for index in nomapping nested; do
	curl -XPUT "http://localhost:9200/$index/docs/26384528" -d @documents/irrelevant1.json
	curl -XPUT "http://localhost:9200/$index/docs/23712032" -d @documents/irrelevant2.json
	curl -XPUT "http://localhost:9200/$index/docs/23873604" -d @documents/irrelevant3.json
	curl -XPUT "http://localhost:9200/$index/docs/24802125" -d @documents/relevant-perhaps1.json
	curl -XPUT "http://localhost:9200/$index/docs/23640957" -d @documents/relevant1.json
	
	curl -XPUT "http://localhost:9200/$index/docs/mock_irrel1" -d @documents/mock_irrelevant1.json
	curl -XPUT "http://localhost:9200/$index/docs/mock_irrel2" -d @documents/mock_irrelevant2.json
	curl -XPUT "http://localhost:9200/$index/docs/mock_rel1" -d @documents/mock_relevant1.json
	curl -XPUT "http://localhost:9200/$index/docs/mock_rel2" -d @documents/mock_relevant2.json
	curl -XPUT "http://localhost:9200/$index/docs/mock_rel3" -d @documents/mock_relevant3.json
	curl -XPUT "http://localhost:9200/$index/docs/mock_rel4" -d @documents/mock_relevant4.json
	
	curl -XPUT "http://localhost:9200/$index/docs/filter1" -d @documents/filter1.json
	curl -XPUT "http://localhost:9200/$index/docs/filter2" -d @documents/filter2.json
done