/**
 * 
 */
/**
 * @author Elijah
 *
 */
package com.spiny.util.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

@SuppressWarnings({"rawtypes","unchecked"})
public class CentralizedFileManager {

	private Map<String, LoadedFile> loadedFiles = new HashMap<String, LoadedFile>();
	private File directory;
	
	private Map<String, IOMethod> ioMethodConfig;
	
	public CentralizedFileManager(File directory, Map<String, IOMethod> ioMethodConfig, String...excludedFiles) {
		this.ioMethodConfig = ioMethodConfig;
		this.directory = directory;
		if(!directory.isDirectory()) return;
		Set<String> excludedFileSet = new HashSet(Arrays.asList(excludedFiles));
		for(File file : directory.listFiles()) {
			IOMethod m = getIOMethod(file.getName());
			if(m != null && !excludedFileSet.contains(file.getName())) loadedFiles.put(file.getName(), new LoadedFile(m.load(file), m));
		}
	}

	public Object get(String fileName) throws FileNotFoundException {
		LoadedFile f = loadedFiles.get(fileName);
		if(f == null) throw new FileNotFoundException();
		return f.o;
	}
	
	public File getFile(String fileName) {
		return new File(directory, fileName);
	}

	public void put(String fileName, Object o) {
		loadedFiles.put(fileName, new LoadedFile(o, getIOMethod(fileName)));
	}

	public void save() {
		for(Entry<String, LoadedFile> e : loadedFiles.entrySet()) {
			e.getValue().ioMethod.save(new File(directory, e.getKey()), e.getValue().o);
		}
	}
	
	private IOMethod getIOMethod(String fileName) {
		String extension = fileName.substring(fileName.indexOf('.') + 1);
		return ioMethodConfig.get(extension);
	}
	
	private class LoadedFile {
		
		public Object o;
		public IOMethod ioMethod;
		
		public LoadedFile(Object o, IOMethod ioMethod) {
			this.o = o;
			this.ioMethod = ioMethod;
		}
	}
}
