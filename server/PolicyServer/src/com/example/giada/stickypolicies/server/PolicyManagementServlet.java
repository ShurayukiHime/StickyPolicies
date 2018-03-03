package com.example.giada.stickypolicies.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

public class PolicyManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public PolicyManagementServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/*protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// response.getWriter().append("Served
		// at:").append(request.getContextPath());
		PrintWriter out = response.getWriter();

		out.println("<html>");
		out.println("<head>");
		out.println("<title>I am a Trusted Authority</title>");
		out.println("<link rel=\"stylesheet\" href=\"styles/default.css\" type=\"text/css\"></link>");
		out.println("</head>");
		out.println("<body>");

		out.println(
				"If you are here, you have made a GET request.<br/> I receive also POST requests, in which you have to send me policies and encrypted messages to evaluate, following this pattern:<br/>");
		out.println("<br/>");
		out.println("Policy, Enc(PubTA, K||h(Policy)), Sig(PrivUser, Enc(PubTA, K||h(Policy))), Enc(K, PII)");
		out.println("<br/>");
		out.println(
				"Where K is the one-time-use symmetric key, TA is me, User is the data owner and PII is the personal data.");
		out.println("<hr/>");
		out.println("<br/>");
		out.println(
				"<form method=\"post\"><input type=\"submit\" name=\"post\" value=\"Reach me via an HTTP POST REQUEST\"/></form>");

		out.println("</body>");
		out.println("</html>");
	}*/

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		BufferedReader body = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = body.readLine()) != null) {
            sb.append(line).append("\n");
        } 
        body.close();
		
		JSONObject requestData = null;
		try {
			requestData = new JSONObject(sb.toString());
		} catch (JSONException e) {
			System.out.println(e.getMessage());
	    	e.printStackTrace();
		}
		
		PrintWriter out = response.getWriter();
		/*JSONObject postData = new JSONObject();
		try {
			postData.put("dataOwner", "TizioCaio");
			postData.put("policy", "Full disclose");
			postData.put("encoding", "sd345ggsdrUJ%%l£km3Nnk");
		} catch (JSONException e) {
			System.out.println(e.getMessage());
	    	e.printStackTrace();
		}
		out.println(postData.toString());
		out.flush();*/
		
		if (requestData != null) {
			out.println(requestData.toString());
		} else {
			out.println("Oooops! Something went wrong!");
		}
		out.println(request.getHeader("User-Agent"));
        out.println(request.getHeader("Access-Control-Request-Method"));
        out.println(request.getHeader("Host"));
        out.println(body.toString());
		response.setCharacterEncoding("UTF-8");
		//response.setStatus(HttpServletResponse.SC_FOUND); // SC_FOUND = 302
		//response.setHeader("Location", "processingsuccessful.html");
	}
}