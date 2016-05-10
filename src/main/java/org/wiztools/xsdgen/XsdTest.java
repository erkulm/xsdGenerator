package org.wiztools.xsdgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XsdTest {
	public static void main(String[] args) {
		XsdGen gen = new XsdGen();
		try {
			gen.parse(new File("/Users/mahmut/Desktop/New folder/FATURA MUHURSUZ2.XML"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File out = new File("/Users/mahmut/Desktop/xsdout5.xsd");
		try {
			gen.write(new FileOutputStream(out));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
