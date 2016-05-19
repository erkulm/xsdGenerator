package com.hititcs.xsdgenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

public class XsdTest {
	public static void main(String[] args) {
		XsdGen xsdGen = new XsdGen();
		try {
			xsdGen.parse(new File("/Users/mahmut/Desktop/New folder/FATURA MUHURSUZ2.XML"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		File out = new File("C:\\Users\\mahmut.erkul\\Desktop\\xsdout4.xsd");
		try {
			xsdGen.write(new FileOutputStream(out));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
