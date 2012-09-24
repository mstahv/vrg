package org.peimari.vrg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;

public class SessionCounter {

	private static Integer count;

	static {
		read();
	}

	public synchronized static Integer getAndIncrement() {
		count = count + 1;
		write();
		return count;
	}

	private static void write() {

		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(getFile()));
			outputStreamWriter.write(""+count +"\n");
			outputStreamWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static class Hook extends Thread {
		@Override
		public void run() {
			super.run();
		}
	}

	private static void read() {
		File file = getFile();
		if (file.exists()) {
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(new FileInputStream(file)));
				String readLine = bufferedReader.readLine();
				count = Integer.parseInt(readLine);
				bufferedReader.close();
				return;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		count = 0;

	}

	private static File getFile() {
		File home = new File(System.getProperty("user.home"));
		File file = new File(home, ".vrgsessioncount");
		return file;
	}

}
