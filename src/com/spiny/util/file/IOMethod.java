package com.spiny.util.file;

import java.io.File;

public interface IOMethod<T> {
	
	T load(File file);
	void save(File file, T o);
	
}
