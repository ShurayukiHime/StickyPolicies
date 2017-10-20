package com.example.giada.stickypoliciesserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserManagementServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public UserManagementServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//response.getWriter().append("Served at: ").append(request.getContextPath());
		PrintWriter out = response.getWriter();
		
		out.println("<html>");
        out.println("<head>");
	    out.println("<title>What does this mean?</title>");
	    out.println("<link rel=\"stylesheet\" href=\"styles/default.css\" type=\"text/css\"></link>");
	    out.println("</head>");
	    out.println("<body>");

        out.println("User Management!<br/><br/>");
        out.println("<br/>");
        out.println("<br/>");
        out.println("<hr/>");
        out.println("<br/>");
        out.println("<form method=\"post\"><input type=\"submit\" name=\"post\" value=\"Reach me via an HTTP POST REQUEST\"/></form>");
	        
        out.println("</body>");
    out.println("</html>");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
}
