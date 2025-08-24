<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="com.academiahub.alerts.model.Alert" %>
<html>
<head>
  <title>Alerts</title>
</head>
<body>
  <h2>Employee Certificates Alerts</h2>
  <table border="1" cellpadding="5">
    <tr>
      <th>ID</th>
      <th>Title</th>
      <th>Details</th>
      <th>Due At</th>
      <th>Status</th>
    </tr>
    <%
      List<Alert> alerts = (List<Alert>) request.getAttribute("alerts");
      for(Alert a : alerts) {
    %>
    <tr>
      <td><%= a.getId() %></td>
      <td><%= a.getTitle() %></td>
      <td><%= a.getDetails() %></td>
      <td><%= a.getDueAt() %></td>
      <td><%= a.getStatus() %></td>
    </tr>
    <% } %>
  </table>
</body>
</html>
