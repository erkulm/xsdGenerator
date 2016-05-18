package org.wiztools.xsdgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class XsdTest {
	public static void main(String[] args) {
		XsdgenJava gen = new XsdgenJava();
		try {
			gen.parse(new File("C:\\Users\\mahmut.erkul\\Desktop\\FATURA MUHURSUZ2.XML"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File out = new File("C:\\Users\\mahmut.erkul\\Desktop\\xsdout4.xsd");
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
