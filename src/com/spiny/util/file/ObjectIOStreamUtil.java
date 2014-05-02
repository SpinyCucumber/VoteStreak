package com.spiny.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectIOStreamUtil {

	public static Object load(File file) {
		Object o = null;
		try {
			ObjectInputStream s = new ObjectInputStream(new FileInputStream(file));
			o = s.readObject();
			s.close();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return o;
	}
	
	public static void save(File file, Object o) {
		try {
			file.mkdirs();
			file.createNewFile();
			ObjectOutputStream s = new ObjectOutputStream(new FileOutputStream(file));
			s.writeObject(o);
			s.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

