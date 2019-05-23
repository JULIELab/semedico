package de.julielab.scicopia.core.elasticsearch.legacy;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
//import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchClient;

public class ElasticSearchClient implements ISearchClient {
	private String clusterName;
	private String[] hosts;
	private int port;
	private TransportClient transportClient;
	private Logger log;

	public ElasticSearchClient(Logger log, String clusterName, String[] hosts, int port) {
		this.log = log;
		this.clusterName = clusterName;
		this.hosts = hosts;
		this.port = port;
	}

	public ElasticSearchClient(Logger log, String clusterName, String host, int port) {
		this.log = log;
		this.clusterName = clusterName;
		this.hosts = new String[] {host};
		this.port = port;
	}

	public Node getNode() {
		throw new NotImplementedException();
	}

	public Client getNodeClient() {
		return getNode().client();
	}

	public Client getTransportClient() {
		try {
			if (null == transportClient) {
				log.info("Connecting to a ElasticSearch cluster {} via socket connection \"{}:{}\".",
						new Object[] { clusterName, hosts, port });

				Settings settings = Settings.builder().put("cluster.name", clusterName).build();
				// ES 6.5.4
//				transportClient = new PreBuiltTransportClient(settings)
//				.addTransportAddress(new TransportAddress(InetAddress.getByName(host), port));
				transportClient = new PreBuiltTransportClient(settings);
				for (String host : hosts) {
					transportClient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
				}
			}
			return transportClient;
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public void shutdown() {
		if (null != transportClient) {
			transportClient.close();
		}
	}

	public Client getClient() {
		if (hosts != null && port != -1)
			return getTransportClient();
		else if (!StringUtils.isBlank(clusterName))
			return getNodeClient();
		else
			throw new IllegalStateException(
					"Neither an ElasticSearch cluster name nor host and port are delivered. Unable to create ElasticSearch client.");
	}
}