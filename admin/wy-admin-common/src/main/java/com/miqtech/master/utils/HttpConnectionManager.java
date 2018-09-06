package com.miqtech.master.utils;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.config.SocketConfig.Builder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpConnectionManager {
	public final static int MAX_TOTAL_CONNECTIONS = 1000;
	public final static int WAIT_TIMEOUT = 60000;
	public final static int MAX_ROUTE_CONNECTIONS = 500;
	public final static int CONNECT_TIMEOUT = 1000;
	public final static int READ_TIMEOUT = 3000;

	private static SocketConfig socketConfig;
	private static HttpClientConnectionManager connectionManager;
	private static PoolingHttpClientConnectionManager poolConn;

	static {
		Builder customBuilder = SocketConfig.custom();
		customBuilder.setSoKeepAlive(true);
		customBuilder.setSoTimeout(1000);
		customBuilder.setTcpNoDelay(true);
		socketConfig = customBuilder.build();
		poolConn = new PoolingHttpClientConnectionManager();
		poolConn.setMaxTotal(MAX_TOTAL_CONNECTIONS);
		poolConn.setDefaultMaxPerRoute(MAX_ROUTE_CONNECTIONS);
		poolConn.setDefaultSocketConfig(socketConfig);
		ConnectionConfig defaultConnectionConfig = null;
		poolConn.setDefaultConnectionConfig(defaultConnectionConfig);
	}

	public static CloseableHttpClient getHttpClient() {
		CloseableHttpClient result = HttpClients.custom().setConnectionManager(connectionManager).build();
		return result;
	}

}