package at.andiwand.library.util.stream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class IgnoreLineByMatchInputStream extends FilterInputStream {
	
	private boolean closed;
	private LinkedList<Byte> buffer = new LinkedList<Byte>();
	
	private StringBuilder line = new StringBuilder();
	private Pattern pattern;
	private Matcher matcher;
	
	
	public IgnoreLineByMatchInputStream(InputStream in, Pattern pattern) {
		super(in);
		
		this.pattern = pattern;
	}
	
	
	@Override
	public int read() throws IOException {
		if (closed) return -1;
		
		if (!buffer.isEmpty()) {
			return buffer.poll();
		} else {
			int read;
			while ((read = in.read()) != -1) {
				buffer.add((byte) read);
				
				if ((read == '\n') || (read == '\r')) {
					matcher = pattern.matcher(line);
					if (matcher.matches()) {
						buffer.clear();
						
						if (read == '\r') {
							read = in.read();
							if (read != '\n') buffer.add((byte) read);
						}
					}
					
					line = new StringBuilder();
					return read();
				}
				
				line.append((char) read);
			}
		}
		
		closed = true;
		buffer.clear();
		return -1;
	}
	
}