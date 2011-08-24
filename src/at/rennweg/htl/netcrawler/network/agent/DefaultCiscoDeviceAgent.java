package at.rennweg.htl.netcrawler.network.agent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.rennweg.htl.netcrawler.cli.SimpleNeighbor;
import at.rennweg.htl.netcrawler.cli.executor.SimpleRemoteExecutor;
import at.rennweg.htl.netcrawler.network.graph.CiscoDevice;
import at.rennweg.htl.netcrawler.network.graph.CiscoRouter;
import at.rennweg.htl.netcrawler.network.graph.CiscoSwitch;
import at.rennweg.htl.netcrawler.network.graph.NetworkDevice;
import at.rennweg.htl.netcrawler.network.graph.NetworkInterface;


public class DefaultCiscoDeviceAgent extends SimpleCiscoDeviceAgent {
	
	public static final Pattern HOSTNAME_PATTERN = Pattern.compile("hostname (.*)");
	public static final int HOSTNAME_GROUP = 1;
	
	public static final Pattern SERIES_NUMBER_PATTERN = Pattern.compile(".*?, (.+?) software \\((.+?)\\).*", Pattern.CASE_INSENSITIVE);
	public static final int SERIES_NUMBER_GROUP = 1;
	
	public static final Pattern PROCESSOR_BOARD_ID_PATTERN = Pattern.compile("processor board id (.*)", Pattern.CASE_INSENSITIVE);
	public static final int PROCESSOR_BOARD_ID_GROUP = 1;
	
	public static final Pattern MOTHERBOARD_SERIAL_NUMBER_PATTERN = Pattern.compile("motherboard serial number *?: ?(.*)", Pattern.CASE_INSENSITIVE);
	public static final int MOTHERBOARD_SERIAL_NUMBER_GROUP = 1;
	
	public static final Pattern NEIGHBOR_NAME_PATTERN = Pattern.compile("device id ?: ?(.*)", Pattern.CASE_INSENSITIVE);
	public static final int NEIGHBOR_NAME_GROUP = 1;
	public static final Pattern NEIGHBOR_MGMT_IP_PATTERN = Pattern.compile("ip address ?: ?(.*)", Pattern.CASE_INSENSITIVE);
	public static final int NEIGHBOR_MGMT_IP_GROUP = 1;
	public static final Pattern NEIGHBOR_INT_PATTERN = Pattern.compile("interface ?: ?(.*?), +port id.*?: ?(.*)", Pattern.CASE_INSENSITIVE);
	public static final int NEIGHBOR_SOURCE_INT_NAME_GROUP = 1;
	public static final int NEIGHBOR_INT_NAME_GROUP = 2;
	
	
	public static final String PATTERNS_FILE_COMMENT_PREFIX = "#";
	
	public static final String ROUTER_PATTERNS_FILE = "router_patterns.txt";
	public static final String SWITCH_PATTERNS_FILE = "switch_patterns.txt";
	
	private static Set<Pattern> ROUTER_PATTERNS;
	private static Set<Pattern> SWITCH_PATTERNS;
	
	
	
	static {
		try {
			ROUTER_PATTERNS = Collections.unmodifiableSet(readPatterns(
					DefaultCiscoDeviceAgent.class.getResourceAsStream(ROUTER_PATTERNS_FILE)));
			SWITCH_PATTERNS = Collections.unmodifiableSet(readPatterns(
					DefaultCiscoDeviceAgent.class.getResourceAsStream(SWITCH_PATTERNS_FILE)));
		} catch (IOException e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
	
	
	
	private static Set<Pattern> readPatterns(InputStream inputStream) throws IOException {
		Set<Pattern> result = new HashSet<Pattern>();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		for (String pattern = reader.readLine(); pattern != null; pattern = reader.readLine()) {
			if (pattern.isEmpty()) continue;
			if (pattern.startsWith(PATTERNS_FILE_COMMENT_PREFIX)) continue;
			
			result.add(Pattern.compile(pattern));
		}
		reader.close();
		
		return result;
	}
	
	private static boolean matchPattenSet(Set<Pattern> patterns, String string) {
		for (Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(string);
			
			if (matcher.matches()) return true;
		}
		
		return false;
	}
	
	
	
	
	public DefaultCiscoDeviceAgent(SimpleRemoteExecutor executor) {
		super(executor);
	}
	
	
	
	public Set<NetworkInterface> fetchInterfaces() throws IOException {
		Set<NetworkInterface> result = new HashSet<NetworkInterface>();
		
		String interfaces = executor.execute("show ip interface brief");
		
		for (String line : interfaces.split("\r\n")) {
			if (line.toLowerCase().startsWith("interface")) continue;
			if (line.trim().isEmpty()) continue;
			
			String[] columns = line.split("\\s+");
			String interfaceName = columns[0];
			
			if (interfaceName.isEmpty()) continue;
			
			NetworkInterface networkInterface = new NetworkInterface(interfaceName);
			result.add(networkInterface);
		}
		
		return result;
	}
	
	public String fetchHostname() throws IOException {
		String runningConfig = executor.execute("show running-config");
		Matcher matcher = matchLines(HOSTNAME_PATTERN, runningConfig);
		
		if (matcher == null) {
			//TODO: exception class
			throw new RuntimeException("Cannot fetch hostname!");
		}
		
		return matcher.group(HOSTNAME_GROUP);
	}
	public Set<InetAddress> fetchManagementAddresses() throws IOException {
		Set<InetAddress> result = new HashSet<InetAddress>();
		
		String interfaces = executor.execute("show ip interface brief");
		
		for (String line : interfaces.split("\r\n")) {
			if (line.toLowerCase().startsWith("interface")) continue;
			if (line.trim().isEmpty()) continue;
			
			String[] columns = line.split("\\s+");
			
			try {
				InetAddress address = InetAddress.getByName(columns[1]);
				result.add(address);
			} catch (UnknownHostException e) {}
		}
		
		return result;
	}
	
	
	public String fetchSeriesNumber() throws IOException {
		String version = executor.execute("show version");
		Matcher matcher = matchLines(SERIES_NUMBER_PATTERN, version);
		
		if (matcher == null) {
			//TODO: exception class
			throw new RuntimeException("Cannot fetch series number!");
		}
		
		return matcher.group(SERIES_NUMBER_GROUP);
	}
	
	public String fetchProcessorBoardId() throws IOException {
		String version = executor.execute("show version");
		Matcher matcher = matchLines(PROCESSOR_BOARD_ID_PATTERN, version);
		
		if (matcher == null) {
			//TODO: exception class
			throw new RuntimeException("Cannot fetch processor board ID!");
		}
		
		return matcher.group(PROCESSOR_BOARD_ID_GROUP);
	}
	
	public String fetchMotherboardSerialNumber() throws IOException {
		String version = executor.execute("show version");
		Matcher matcher = matchLines(MOTHERBOARD_SERIAL_NUMBER_PATTERN, version);
		
		if (matcher == null) {
			//TODO: exception class
			throw new RuntimeException("Cannot fetch processor board ID!");
		}
		
		return matcher.group(MOTHERBOARD_SERIAL_NUMBER_GROUP);
	}
	
	
	private Matcher matchLines(Pattern pattern, String string) {
		for (String line : string.split("\r\n")) {
			Matcher matcher = pattern.matcher(line);
			
			if (matcher.matches()) return matcher;
		}
		
		return null;
	}
	
	
	@Override
	public CiscoDevice fetchAll() throws IOException {
		CiscoDevice result = fetchComparable();
		
		super.fetchAll(result);
		
		return result;
	}
	@Override
	public void fetchAll(NetworkDevice device) throws IOException {
		String seriesNumber = fetchSeriesNumber();
		
		if (!(device instanceof CiscoRouter) && !(device instanceof CiscoSwitch))
			throw new IllegalArgumentException("The device must be a CiscoRouter or a CiscoSwitch!");
		
		if ((device instanceof CiscoRouter) && !matchPattenSet(ROUTER_PATTERNS, seriesNumber)) {
			//TODO: exception class
			throw new RuntimeException("Illegal series number for routers!");
		} else if ((device instanceof CiscoSwitch) && !matchPattenSet(SWITCH_PATTERNS, seriesNumber)) {
			//TODO: exception class
			throw new RuntimeException("Illegal series number for switches!");
		}
		
		CiscoDevice ciscoDevice = (CiscoDevice) device;
		ciscoDevice.setShowVersion(executor.execute("show version"));
		ciscoDevice.setDirFlash(executor.execute("dir flash:"));
		ciscoDevice.setShowRunningConfig(executor.execute("show running-config"));
		ciscoDevice.setShowIpInterfaceBrief(executor.execute("show ip interface brief"));
		
		if (device instanceof CiscoRouter) {
			CiscoRouter router = (CiscoRouter) device;
			router.setProcessorBoardId(fetchHostname());
		} else {
			CiscoSwitch ciscoSwitch = (CiscoSwitch) device;
			ciscoSwitch.setMotherboardSerialNumber(fetchMotherboardSerialNumber());
		}
		
		super.fetchAll(device);
	}
	
	public CiscoDevice fetchComparable() throws IOException {
		CiscoDevice result = null;
		
		String seriesNumber = fetchSeriesNumber();
		
		if (matchPattenSet(ROUTER_PATTERNS, seriesNumber)) {
			CiscoRouter router = new CiscoRouter();
			router.setProcessorBoardId(fetchHostname());
			result = router;
		} else {
			if (matchPattenSet(SWITCH_PATTERNS, seriesNumber)) {
				CiscoSwitch ciscoSwitch = new CiscoSwitch();
				ciscoSwitch.setMotherboardSerialNumber(fetchMotherboardSerialNumber());
				result = ciscoSwitch;
			} else {
				//TODO: exception class
				throw new RuntimeException("Unkown series number: " + seriesNumber);
			}
		}
		
		result.setHostname(fetchHostname());
		
		return result;
	}
	
	
	public List<SimpleNeighbor> fetchNeighbors() throws IOException {
		List<SimpleNeighbor> result = new ArrayList<SimpleNeighbor>();
		
		String neighbors = executor.execute("show cdp neighbors detail");
		
		SimpleNeighbor currentNeighbor = null;
		List<InetAddress> currentMgmtAddresses = new ArrayList<InetAddress>();
		
		for (String line : neighbors.split("\n")) {
			line = line.trim();
			
			Matcher nameMatcher = NEIGHBOR_NAME_PATTERN.matcher(line);
			
			if (nameMatcher.matches()) {
				if (currentNeighbor != null) {
					currentNeighbor.setManagementAddresses(currentMgmtAddresses);
					currentMgmtAddresses.clear();
					result.add(currentNeighbor);
				}
				
				currentNeighbor = new SimpleNeighbor();
				currentNeighbor.setName(nameMatcher.group(NEIGHBOR_NAME_GROUP));
			} else if (currentNeighbor != null) {
				Matcher mgmtIpMatcher = NEIGHBOR_MGMT_IP_PATTERN.matcher(line);
				
				if (mgmtIpMatcher.matches()) {
					InetAddress address = InetAddress.getByName(mgmtIpMatcher.group(NEIGHBOR_MGMT_IP_GROUP));
					currentMgmtAddresses.add(address);
					
					continue;
				}
				
				Matcher intMatcher = NEIGHBOR_INT_PATTERN.matcher(line);
				
				if (intMatcher.matches()) {
					currentNeighbor.setSourceInterfaceName(intMatcher.group(NEIGHBOR_SOURCE_INT_NAME_GROUP));
					currentNeighbor.setInterfaceName(intMatcher.group(NEIGHBOR_INT_NAME_GROUP));
				}
			}
		}
		
		if (currentNeighbor != null) {
			currentNeighbor.setManagementAddresses(currentMgmtAddresses);
			result.add(currentNeighbor);
		}
		
		return result;
	}
	
}