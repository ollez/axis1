<%@ page import="java.util.Iterator,
                 java.util.HashMap,
                 java.util.Collection,
                 org.apache.axis2.Constants,
                 java.util.Hashtable,
                 org.apache.axis2.description.ServiceDescription,
                 org.apache.axis2.description.OperationDescription,
                 javax.xml.namespace.QName,
                 java.io.StringWriter,
                 java.io.PrintWriter"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>List Services</title>
  <link href="css/axis-style.css" rel="stylesheet" type="text/css">
  </head>
  <body>
        <%
            String isFault = (String)request.getSession().getAttribute(Constants.IS_FAULTY);
            if(Constants.IS_FAULTY.equals(isFault)){
                Hashtable errornessservices =(Hashtable)request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
                String servicName = (String)request.getParameter("serviceName");
                %>
                    <h3>This Web service has deployment faults</h3><%
                     %><font color="red" ><%=(String)errornessservices.get(servicName) %></font>
                <%

                    }else{
                %>
             Oh! this place seems to be empty!!!</body>
 <%
            }
        %>

        </body>
</html>


<%--        <jsp:include page="include/link-footer.inc"></jsp:include>--%>
<%--        <jsp:include page="include/footer.inc"></jsp:include>--%>