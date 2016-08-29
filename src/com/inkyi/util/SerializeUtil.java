package com.inkyi.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeUtil {

	public static byte[] serialize(Object object) {
		if (object == null) {
			throw new RuntimeException("serialize input object==null");
		}
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream(256);
			oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			oos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("serialize error");
		}
	}

	public static Object deserialize(byte[] bytes) {
		if (bytes == null) {
			throw new RuntimeException("deserialize input bytes[]==null");
		}
		ObjectInputStream ois = null;
		ByteArrayInputStream bais = null;
		try {
			// 反序列化
			bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("deserialize error");
		}
	}
	
	public static void serialize(Object object,File file) {
		if (object == null) {
			throw new RuntimeException("serialize input object==null");
		}
		if (file == null) {
			throw new RuntimeException("serialize input file==null");
		}
		ObjectOutputStream oos = null;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("serialize error");
		} finally {
			try {
				if(oos!=null){
					oos.flush();
					oos.close();
				}
				if(fos!=null){
					fos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("serialize close error");
			}  
		}
	}
	
	public static Object deserialize(File file) {
		if (file == null || !file.isFile()) {
			throw new RuntimeException("deserialize input file==null");
		}
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		try {
			// 反序列化
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return ois.readObject();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("deserialize error");
		} finally {
			try {
				if(ois!=null){
					ois.close();
				}
				if(fis!=null){
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("serialize close error");
			}  
		}
	}
	
}
