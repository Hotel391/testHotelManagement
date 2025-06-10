/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers.common;

import dal.AccountGoogleDAO;
import dal.CustomerAccountDAO;
import dal.CustomerDAO;
import dal.EmployeeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.AccountGoogle;
import models.Customer;
import models.CustomerAccount;
import models.Employee;
import models.Role;

/**
 *
 * @author Hai Long
 */
@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        HttpSession session = request.getSession(true);

        String service = request.getParameter("service");

        String state = request.getParameter("state");

        System.out.println("state: " + state);

        if (service == null) {
            service = "login";
        }

        if ("login".equals(service) && state == null) {
            String submit = request.getParameter("submit");

            if (submit == null) {
                request.getRequestDispatcher("View/Login.jsp").forward(request, response);
                return;
            }

            String username = request.getParameter("username").trim();
            String password = request.getParameter("password");

            if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
                request.setAttribute("error", "Please fill all information");
                request.getRequestDispatcher("View/Login.jsp").forward(request, response);
                return;
            }

            CustomerAccount customerInfo = CustomerAccountDAO.getInstance().checkLogin(username, password);
            if (customerInfo != null) {
                System.out.println("Info: " + customerInfo);
                session.setAttribute("customerInfo", customerInfo);
                response.sendRedirect("customer/home");
                return;
            }

            Employee employeeInfo = EmployeeDAO.getInstance().getEmployeeLogin(username, password);
            if (employeeInfo != null) {
                System.out.println(employeeInfo);
                session.setAttribute("employeeInfo", employeeInfo);
                int roleId = employeeInfo.getRole().getRoleId();
                switch (roleId) {
                    case 0:
                        response.sendRedirect("developerPage");
                        break;
                    case 1:
                        response.sendRedirect("admin/dashboard");
                        break;
                    case 2:
                        response.sendRedirect("receptionistPage");
                        break;
                    case 3:
                        response.sendRedirect("cleanerPage");
                        break;
                    default:
                        request.getRequestDispatcher("View/Login.jsp").forward(request, response);
                        break;
                }
                return;
            }

            request.setAttribute("error", "Wrong username or password");
            request.getRequestDispatcher("View/Login.jsp").forward(request, response);
        }

        if ("loginGoogle".equals(state)) {
            String code = request.getParameter("code");

            String accessToken = AccountGoogleDAO.getInstance().getToken(code);

            AccountGoogle userInfo = AccountGoogleDAO.getInstance().getUserInfo(accessToken);

            if (CustomerDAO.getInstance().checkExistedEmail(userInfo.getEmail()) == false) {
                Customer customerInfo = new Customer();

                customerInfo.setFullName(userInfo.getName());

                customerInfo.setActivate(true);

                customerInfo.setRole(new Role(4, "Customer"));

                customerInfo.setEmail(userInfo.getEmail());

                int customerId = CustomerDAO.getInstance().insertCustomer(customerInfo);

                String[] part = userInfo.getEmail().split("@");

                CustomerAccount accInfo = new CustomerAccount();

                //set username for google account
                if (CustomerAccountDAO.getInstance().isUsernameExisted(part[0])) {
                    accInfo.setUsername(generateRandomString(8));
                    while (CustomerAccountDAO.getInstance().isUsernameExisted(accInfo.getUsername())) {
                        accInfo.setUsername(generateRandomString(8));
                    }
                } else {
                    accInfo.setUsername(part[0]);
                }

                accInfo.setCustomer(CustomerDAO.getInstance().getCustomerByCustomerID(customerId));

                CustomerAccountDAO.getInstance().insertCustomerAccount(accInfo);
                session.setAttribute("customerInfo", accInfo);
            } else {
                CustomerAccount accInfo = CustomerAccountDAO.getInstance().checkAccountByEmail(userInfo.getEmail());
                session.setAttribute("customerInfo", accInfo);
            }
            response.sendRedirect("customer/home");
        }

        if ("logout".equals(service)) {
            session.removeAttribute("customerInfo");
            session.removeAttribute("employeeInfo");
            session.invalidate();
            response.sendRedirect("login");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String generateRandomString(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
