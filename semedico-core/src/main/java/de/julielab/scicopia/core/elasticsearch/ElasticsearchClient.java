package de.julielab.scicopia.core.elasticsearch;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;

//import java.net.InetSocketAddress;
//import java.util.List;
//import org.elasticsearch.common.transport.TransportAddress;

public class ElasticsearchClient {

	private TransportClient client;
	
	@SuppressWarnings("unused")
	private ElasticsearchClient() {}

//  ES 6.5.4
//	public ElasticsearchClient(String hostName, int port) throws UnknownHostException {
//		this.client = new PreBuiltTransportClient(Settings.EMPTY)
//		        .addTransportAddress(new TransportAddress(InetAddress.getByName(hostName), port));
//	}
	
	// ES 5.6.14
	public ElasticsearchClient(String hostName, int port) throws UnknownHostException {
		client = new PreBuiltTransportClient(Settings.EMPTY)
	        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), port));
	}
	
//	public ElasticsearchClient(List<InetSocketAddress> hosts) {
//		this.client = TransportClient.builder().build()
//				.addTransportAddresses(hosts.stream().map(InetSocketTransportAddress::new)
//						.toArray(InetSocketTransportAddress[]::new));
//	}
	
	//Settings settings = Settings.settingsBuilder().put("cluster.name", "myClusterName").build();
}
