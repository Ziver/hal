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

package zutil.net.http;

import zutil.StringUtil;
import zutil.parser.URLDecoder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.regex.Pattern;

public class HttpHeaderParser {
    public static final String HEADER_COOKIE = "COOKIE";

	private static final Pattern PATTERN_COLON = Pattern.compile(":");
	private static final Pattern PATTERN_EQUAL = Pattern.compile("=");
	private static final Pattern PATTERN_AND = Pattern.compile("&");
	private static final Pattern PATTERN_SEMICOLON = Pattern.compile(";");


    private BufferedReader in;
    private boolean readStatusLine;


	/**
	 * Parses the HTTP header information from a stream
	 * 
	 * @param 	in 				is the stream
	 */
	public HttpHeaderParser(BufferedReader in){
        this.in = in;
        this.readStatusLine = true;
	}

	/**
	 * Parses the HTTP header information from a String
	 * 
	 * @param   in 		is the String
	 */
	public HttpHeaderParser(String in){
        this(new BufferedReader(new StringReader(in)));
	}


    public HttpHeader read() throws IOException {
        HttpHeader header = new HttpHeader();
        String line = null;

        // First line
        if (readStatusLine) {
            if( (line=in.readLine()) != null && !line.isEmpty() )
                parseStatusLine(header, line);
            else
                return null;
        }
        // Read header body
        while( (line=in.readLine()) != null && !line.isEmpty() ){
            parseHeaderLine(header, line);
        }
        // Post processing
        parseHeaderValue(header.getCookieMap(), header.getHeader(HEADER_COOKIE));

        return header;
    }

    /**
     * @param   readStatusLine      indicates if the stream contains http status lines. (default: true)
     */
    public void setReadStatusLine(boolean readStatusLine){
        this.readStatusLine = readStatusLine;
    }

	/**
	 * Parses the first line of a http request/response and stores the values in a HttpHeader object
	 *
     * @param   header          the header object where the cookies will be stored.
	 * @param 	statusLine 		the status line String
	 */
	public static void parseStatusLine(HttpHeader header, String statusLine){
		// Server Response
		if( statusLine.startsWith("HTTP/") ){
			header.setIsRequest(false);
			header.setHTTPVersion( Float.parseFloat( statusLine.substring( 5 , 8)));
			header.setHTTPCode( Integer.parseInt( statusLine.substring( 9, 12 )));
		}
		// Client Request
		else if(statusLine.contains("HTTP/")){
			header.setIsRequest(true);
			header.setRequestType( statusLine.substring(0, statusLine.indexOf(" ")).trim() );
			header.setHTTPVersion( Float.parseFloat( statusLine.substring(statusLine.lastIndexOf("HTTP/")+5 , statusLine.length()).trim()));
			statusLine = (statusLine.substring(header.getRequestType().length()+1, statusLine.lastIndexOf("HTTP/")));

			// parse URL and attributes
			int index = statusLine.indexOf('?');
			if(index > -1){
				header.setRequestURL( statusLine.substring(0, index));
				statusLine = statusLine.substring( index+1, statusLine.length());
				parseURLParameters(header, statusLine);
			}
			else{
				header.setRequestURL(statusLine);
			}
		}
	}

    /**
     * Parses a http key value paired header line
     *
     * @param   header          the header object where the cookies will be stored.
     * @param 	line 	is the next line in the header
     */
    public static void parseHeaderLine(HttpHeader header, String line){
        String[] data = PATTERN_COLON.split( line, 2 );
        header.getHeaderMap().put(
                data[0].trim().toUpperCase(), 					// Key
                (data.length>1 ? data[1] : "").trim()); 		//Value
    }

    /**
     * Parses a header value string that contains key and value paired data and
     * stores them in a HashMap. If a pair only contain a key the the value
     * will be set as a empty string.
	 *
	 * @param   map             the Map where the cookies will be stored.
     * @param   cookieHeader    the raw cookie header String that will be parsed
     */
    public static void parseHeaderValue(Map<String,String> map, String cookieHeader){
        if(cookieHeader != null && !cookieHeader.isEmpty()){
            String[] tmp = PATTERN_SEMICOLON.split(cookieHeader);
            for(String cookie : tmp){
                String[] tmp2 = PATTERN_EQUAL.split(cookie, 2);
                map.put(
                        tmp2[0].trim(), 							// Key
                        StringUtil.trim((tmp2.length>1 ? tmp2[1] : "").trim(), '\"')); 	//Value
            }
        }
    }

	/**
	 * Parses a string with variables from a get or post request that was sent from a client
	 *
     * @param   header              the header object where the cookies will be stored.
	 * @param 	urlAttributes 		is the String containing all the attributes
	 */
	public static void parseURLParameters(HttpHeader header, String urlAttributes){
		String[] tmp;
		urlAttributes = URLDecoder.decode(urlAttributes);
		// get the variables
		String[] data = PATTERN_AND.split( urlAttributes );
		for(String element : data){
			tmp = PATTERN_EQUAL.split(element, 2);
			header.getUrlAttributeMap().put(
					tmp[0].trim(), 								// Key
					(tmp.length>1 ? tmp[1] : "").trim()); 		//Value
		}
	}
}
