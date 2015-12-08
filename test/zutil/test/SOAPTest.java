/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package zutil.test;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import zutil.net.ws.WSInterface;
import zutil.net.ws.WSInterface.WSNamespace;
import zutil.net.ws.WSReturnObject;
import zutil.net.ws.WebServiceDef;
import zutil.net.ws.soap.SOAPHttpPage;
import zutil.parser.wsdl.WSDLWriter;


public class SOAPTest {
    /************************* TEST CASES ************************/
	public static void main(String[] args){
		new SOAPTest();
	}
	
	public SOAPTest(){
		WebServiceDef wsDef = new WebServiceDef( MainSOAPClass.class );
		SOAPHttpPage soap = new SOAPHttpPage( wsDef );
		
		WSDLWriter wsdl = new WSDLWriter( wsDef );
		wsdl.write(System.out);
		System.out.println( "****************** new *********************" );	
		WSDLWriter wsdl2 = new WSDLWriter( wsDef );
		wsdl2.write(System.out);
		
		// Response		
		try {
			System.out.println( "****************** LOG *********************" );
			Document document = soap.genSOAPResponse(
					"<?xml version=\"1.0\"?>" +
					"<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
					"	<soap:Body xmlns:m=\"http://www.example.org/stock\">\n" +
					"		<m:stringArrayMethod>\n" +
					"			<m:StringName>IBM</m:StringName>\n" +
					"		</m:stringArrayMethod>\n" +
					
					"		<m:simpleReturnClassMethod>\n" +
					"			<m:byte>IBM</m:byte>\n" +
					"		</m:simpleReturnClassMethod>\n" +
					"	</soap:Body>\n" +
			"</soap:Envelope>");
			System.out.println( "****************** RESPONSE *********************" );

			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer = new XMLWriter( System.out, format );
			writer.write( document );

			System.out.println();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /************************* TEST CLASSES ************************/
    @SuppressWarnings("unused")
	public static class SpecialReturnClass extends WSReturnObject{
		@WSValueName(value="otherValue1")
		public String param1 = "otherValue1";
		@WSValueName("otherValue2")
		public String param2 = "otherValue2";
		public byte[] b = new byte[]{0x12, 0x23};
		public InnerClass inner = new InnerClass();
	}

    @SuppressWarnings("unused")
	public static class InnerClass extends WSReturnObject{
		public String innerClassParam1 = "innerClass1";
		public String innerClassParam2 = "innerClass2";
	}

    @SuppressWarnings("unused")
	public static class SimpleReturnClass extends WSReturnObject{
		@WSValueName("otherParam1")
		public String param1 = "param1";
		public String param2 = "param2";
	}

    @SuppressWarnings("unused")
	@WSNamespace("http://test.se:8080/")
	public static class MainSOAPClass implements WSInterface{
		public MainSOAPClass(){}
		
		@WSHeader()
		@WSDocumentation("Documentation of method exceptionMethod()")
		public void exceptionMethod(
				@WSParamName(value="otherParam1", optional=true) int param1,
				@WSParamName(value="otherParam2", optional=true) int param2) throws Exception{ 
			System.out.println("Executing method: exceptionMethod()");
			throw new Exception("This is an Exception");
		}

		@WSReturnName("stringArray")
		@WSParamDocumentation("Documentation of stringArrayMethod()")
		public String[][] stringArrayMethod (
				@WSParamName("StringName") String str) throws Exception{ 
			System.out.println("Executing method: stringArrayMethod()");
			return new String[][]{{"test","test2"},{"test3","test4"}};
		}

		@WSReturnName("specialReturnClass")
		@WSParamDocumentation("Documentation of specialReturnMethod()")
		public SpecialReturnClass[] specialReturnMethod (
				@WSParamName("StringName2") String str) throws Exception{ 
			System.out.println("Executing method: specialReturnMethod()");
			return new SpecialReturnClass[]{new SpecialReturnClass(), new SpecialReturnClass()};
		}

		@WSReturnName("SimpleReturnClass")
		@WSParamDocumentation("null is the kala")
		public SimpleReturnClass simpleReturnClassMethod (
				@WSParamName("byte") String lol) throws Exception{ 
			System.out.println("Executing method: simpleReturnClassMethod()");
			SimpleReturnClass tmp = new SimpleReturnClass();
			tmp.param1 = "newParam1";
			tmp.param2 = "newParam2";
			return tmp;
		}
		
		@WSParamDocumentation("void method documentation")
		public void voidMethod (){ }
		
		@WSDisabled()
		public void disabledMethod(){ }
		protected void protectedMethod(){ }	

		private void privateMethod(){ }
	}
}