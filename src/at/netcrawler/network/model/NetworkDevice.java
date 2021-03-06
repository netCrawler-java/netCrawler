package at.netcrawler.network.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import at.andiwand.library.network.ip.IPv4Address;
import at.andiwand.library.util.TypeToken;
import at.netcrawler.network.Capability;


public class NetworkDevice extends NetworkModel {
	
	private static final long serialVersionUID = -1850566228109843184L;
	
	public static final Map<String, TypeToken<?>> TYPE_MAP;
	
	public static final String HOSTNAME = "device.hostname";
	public static final TypeToken<?> HOSTNAME_TYPE = TypeToken
			.get(String.class);
	
	public static final String SYSTEM = "device.system";
	public static final TypeToken<?> SYSTEM_TYPE = TypeToken
			.get(DeviceSystem.class);
	
	public static final String SYSTEM_STRING = "device.systemString";
	public static final TypeToken<?> SYSTEM_STRING_TYPE = TypeToken
			.get(String.class);
	
	public static final String UPTIME = "device.uptime";
	public static final TypeToken<?> UPTIME_TYPE = TypeToken.get(Long.class);
	
	public static final String CAPABILITIES = "device.capabilities";
	public static final TypeToken<?> CAPABILITIES_TYPE = new TypeToken<Set<Capability>>() {};
	
	public static final String MAJOR_CAPABILITY = "device.majorCapability";
	public static final TypeToken<?> MAJOR_CAPABILITY_TYPE = TypeToken
			.get(Capability.class);
	
	public static final String INTERFACES = "device.interfaces";
	public static final TypeToken<?> INTERFACES_TYPE = new TypeToken<Set<NetworkInterface>>() {};
	
	public static final String MANAGEMENT_ADDRESSES = "device.managementAddresses";
	public static final TypeToken<?> MANAGEMENT_ADDRESSES_TYPE = new TypeToken<Set<IPv4Address>>() {};
	
	// public static final String CONNECTED_VIA = "device.connectedVia";
	// public static final TypeToken<?> CONNECTED_VIA_TYPE = new
	// TypeToken<Class<? extends Connection>>() {};
	//
	// public static final String AVAILABLE_CONNECTIONS =
	// "device.availableConnections";
	// public static final TypeToken<?> AVAILABLE_CONNECTIONS_TYPE = new
	// TypeToken<Set<ConnectionType>>() {};
	//
	// public static final String CONNECTION_SETTINGS =
	// "device.connectionSettings";
	// public static final TypeToken<?> CONNECTION_SETTINGS_TYPE = new
	// TypeToken<Map<ConnectionType, ConnectionSettings>>() {};
	
	static {
		Map<String, TypeToken<?>> map = new HashMap<String, TypeToken<?>>();
		map.put(HOSTNAME, HOSTNAME_TYPE);
		map.put(SYSTEM, SYSTEM_TYPE);
		map.put(SYSTEM_STRING, SYSTEM_STRING_TYPE);
		map.put(UPTIME, UPTIME_TYPE);
		map.put(CAPABILITIES, CAPABILITIES_TYPE);
		map.put(MAJOR_CAPABILITY, MAJOR_CAPABILITY_TYPE);
		map.put(INTERFACES, INTERFACES_TYPE);
		map.put(MANAGEMENT_ADDRESSES, MANAGEMENT_ADDRESSES_TYPE);
		// map.put(CONNECTED_VIA, CONNECTED_VIA_TYPE);
		// map.put(AVAILABLE_CONNECTIONS, AVAILABLE_CONNECTIONS_TYPE);
		// map.put(CONNECTION_SETTINGS, CONNECTION_SETTINGS_TYPE);
		TYPE_MAP = Collections.unmodifiableMap(map);
	}
	
	public NetworkDevice() {
		super(TYPE_MAP);
	}
	
}