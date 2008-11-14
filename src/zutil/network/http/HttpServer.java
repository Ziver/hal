package zutil.network.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import zutil.MultiPrintStream;

/**
 * A simple web server that handles both cookies and
 * sessions for all the clients
 * 
 * @author Ziver
 * TODO: File upload
 */
public class HttpServer extends Thread{
	public static final boolean DEBUG = false;
	public static final String SERVER_VERSION = "Evil HttpServer 1.0";
	public static final int COOKIE_TTL = 200;
	public static final int SESSION_TTL = 200;

	public final String server_url;
	public final int server_port;

	private HashMap<String,HttpPage> pages;
	private HttpPage defaultPage;
	private HashMap<String,HashMap<String,String>> sessions;
	private int nextSessionId;

	/**
	 * Creates a new instance of the sever
	 * 
	 * @param url The address to the server
	 * @param port The port that the server should listen to
	 */
	public HttpServer(String url, int port){
		this.server_url = url;
		this.server_port = port;

		pages = new HashMap<String,HttpPage>();
		sessions = new HashMap<String,HashMap<String,String>>();
		nextSessionId = 0;
	}

	/**
	 * Add a HttpPage to a specific URL
	 * 
	 * @param name The URL or name of the page
	 * @param page The page itself
	 */
	public void setPage(String name, HttpPage page){
		pages.put(name, page);
	}

	/**
	 * This is a default page that will be shown
	 * if there is no other matching page,
	 * 
	 * @param page The HttpPage that will be shown
	 */
	public void setDefaultPage(HttpPage page){
		defaultPage = page;
	}

	public void run(){
		try{
			ServerSocket ss = new ServerSocket(server_port);
			MultiPrintStream.out.println("Http Server Running!!!");

			while(true){
				new HttpServerThread(ss.accept());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Internal class that handles all the requests
	 * 
	 * @author Ziver
	 *
	 */
	class HttpServerThread extends Thread{
		private HttpPrintStream out;
		private BufferedReader in;
		private Socket socket;

		public HttpServerThread(Socket socket) throws IOException{
			out = new HttpPrintStream(socket.getOutputStream());
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.socket = socket;
			start();
			if(DEBUG)MultiPrintStream.out.println("New Connection!!! "+socket.getInetAddress().getHostName());
		}

		public void run(){
			String tmp = null;
			int tmpi;

			String page_url = "";
			HashMap<String,String> client_info = new HashMap<String,String>();
			HashMap<String,String> cookie = new HashMap<String,String>();
			HashMap<String,String> request = new HashMap<String,String>();

			//****************************  REQUEST *********************************
			try {
				if(DEBUG)MultiPrintStream.out.println("Reciving Http Request!!!");
				while(!(tmp=in.readLine()).isEmpty()){
					//System.err.println(tmp);
					//***********   Handling Get variables
					if(tmp.startsWith("GET")){
						// Gets the file URL and get values
						tmp = (tmp.substring(5, tmp.indexOf("HTTP/"))).trim();
						page_url = parseHttpHeader(tmp, request);
					}
					//*********   Handling Post variable data
					else if(tmp.startsWith("POST")){
						// Gets the file URL and get values
						tmp = (tmp.substring(6, tmp.indexOf("HTTP/"))).trim();
						page_url = parseHttpHeader(tmp, request);
					}
					//*********   Handling Cookies
					else if(tmp.startsWith("Cookie")){
						tmp = tmp.substring(tmp.indexOf(':')+1, tmp.length());
						while(!tmp.isEmpty()){
							tmpi = ( (tmpi = tmp.indexOf(';')) == -1 ? tmp.length() : tmpi);
							cookie.put(
									(tmp.substring(0, tmp.indexOf('=')).trim() ), 			// Key
									(tmp.substring(tmp.indexOf('=')+1, tmpi)).trim() );		//Value
							if(tmp.indexOf(';') > 0)
								tmp = tmp.substring(tmp.indexOf(';')+1, tmp.length());
							else
								break;
						}
					}
					//*********   Handling Client info
					else{
						if(tmp.indexOf(':') > -1){
							client_info.put(
									(tmp.substring(0, tmp.indexOf(':')).trim() ), 				// Key
									(tmp.substring(tmp.indexOf(':')+1, tmp.length())).trim() ); //Value
						}
						else{
							MultiPrintStream.out.println("Faild to parsse header: "+tmp);
						}
					}
				}

				//******* Read in the post data if available
				if(client_info.containsKey("Content-Length")){
					// Reads the post data size
					tmp = client_info.get("Content-Length");
					int post_data_length = Integer.parseInt(
							tmp.substring(tmp.indexOf(':')+1, tmp.length()).trim() );

					if(client_info.get("Content-Type").equals("application/x-www-form-urlencoded")){
						StringBuffer tmpb = new StringBuffer();
						// read the data
						for(int i=0; i<post_data_length ;i++){
							tmpb.append((char)in.read());
						}
						// get the variables
						parseVariables(tmpb.toString(), request);
					}
					else if(client_info.get("Content-Type").contains("multipart/form-data")){
						// TODO:
						throw new Exception("\"multipart/form-data\" Not implemented!!!");
					}						
				}
				//*****************
			} catch (Exception e) {
				e.printStackTrace();
				try {
					out.sendHeader("HTTP/1.0 500 ERROR");
				} catch (Exception e1) {}
				out.println("500 Internal Error(Header: "+tmp+"): "+e.getMessage());
			}
			try {
				//****************************  HANDLE REQUEST *********************************
				// Get the client session or create one
				HashMap<String,String> client_session;
				if(cookie.containsKey("session_id") && sessions.containsKey(cookie.get("session_id"))){
					client_session = sessions.get(cookie.get("session_id"));
				}
				else{
					client_session = new HashMap<String,String>();
					client_session.put("session_id", ""+nextSessionId);
					sessions.put(""+nextSessionId, client_session);
					nextSessionId++;
				}
				// Debug
				if(DEBUG){
					MultiPrintStream.out.println("# page_url: "+page_url);
					MultiPrintStream.out.println("# cookie: "+cookie);
					MultiPrintStream.out.println("# client_session: "+client_session);
					MultiPrintStream.out.println("# client_info: "+client_info);
					MultiPrintStream.out.println("# request: "+request);
				}
				//****************************  RESPONSE  ************************************
				if(DEBUG)MultiPrintStream.out.println("Sending Http Response!!!");
				out.sendHeader("HTTP/1.0 200 OK");
				out.sendHeader("Server: "+SERVER_VERSION);
				out.sendHeader("Content-Type: text/html");
				out.setCookie("session_id", client_session.get("session_id"));


				if(!page_url.isEmpty() && pages.containsKey(page_url)){
					pages.get(page_url).respond(out, client_info, client_session, cookie, request);
				}
				else if(defaultPage != null){
					defaultPage.respond(out, client_info, client_session, cookie, request);
				}
				else{
					out.println("404 ERROR");	
				}

				//********************************************************************************
			} catch (Exception e) {
				e.printStackTrace();
				out.println("500 Internal Error: "+e.getMessage());
			}

			try{
				if(DEBUG)MultiPrintStream.out.println("Conection Closed!!!");
				out.close();
				in.close();
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Parses the first header line and ads the values to 
	 * the map and returns the file name and path
	 * 
	 * @param header The header String
	 * @param map The HashMap to put the variables to
	 * @return The path and file name as a String
	 */
	private String parseHttpHeader(String header, HashMap<String, String> map){
		String page_url = "";
		// cut out the page name
		if(header.indexOf('?') > -1){ 
			page_url = header.substring(0, header.indexOf('?'));
			header = header.substring(header.indexOf('?')+1, header.length());
			parseVariables(header, map);
		}
		else{
			page_url = header;
		}

		return page_url;
	}

	/**
	 * Parses a String with variables from a get or post
	 * from a client and puts the data into a HashMap
	 * 
	 * @param header A String with all the variables
	 * @param map The HashMap to put all the variables into
	 */
	private void parseVariables(String header, HashMap<String, String> map){
		int tmpi;
		// get the variables
		while(!header.isEmpty()){
			tmpi = ( (tmpi = header.indexOf('&')) == -1 ? header.length() : tmpi);
			map.put(
					(header.substring(0, header.indexOf('=')).trim() ), 			// Key
					(header.substring(header.indexOf('=')+1, tmpi )).trim() );	//Value
			if(header.indexOf('&') > 0)
				header = header.substring(header.indexOf('&')+1, header.length());
			else
				break;
		}
	}
}