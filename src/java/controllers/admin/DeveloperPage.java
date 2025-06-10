package controllers.admin;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import models.Employee;
import utility.Encryption;
import utility.Validation;

@WebServlet(name = "DeveloperPage", urlPatterns = {"/developerPage"})
public class DeveloperPage extends HttpServlet {

    private String linkInfoAdmin = "View/Developer/InfoAdmin.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String service = request.getParameter("service");

        HttpSession session = request.getSession();
        Employee employeeInfo = (Employee) session.getAttribute("employeeInfo");
        String username = employeeInfo.getUsername();
        session.setAttribute("username", username);

        if (service == null) {
            service = "viewAll";
        }

        if (service.equals("changePass")) {
            Employee em = dal.EmployeeDAO.getInstance().getAccountAdmin(username);
            request.setAttribute("employee", em);
            request.getRequestDispatcher(linkInfoAdmin).forward(request, response);
        }

        if (service.equals("add")) {
            request.getRequestDispatcher("View/Developer/AddManager.jsp").forward(request, response);
        }

        if (service.equals("deleteManager")) {
            int employeeID = Integer.parseInt(request.getParameter("employeeID"));
            dal.AdminDao.getInstance().deleteManagerAccount(employeeID);
            response.sendRedirect("developerPage");
        }

        if (service.equals("viewAll")) {
            Employee em = dal.EmployeeDAO.getInstance().getAccountAdmin(username);
            List<Employee> list = dal.AdminDao.getInstance().getAllEmployee();
            request.setAttribute("list", list);
            request.setAttribute("adminAccount", em);
            request.getRequestDispatcher("View/Developer/DeveloperPage.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String service = request.getParameter("service");
        String userName = request.getParameter("username");

        if (service.equals("changePass")) {
            handleChangePassword(request, response, userName);
        } else if (service.equals("add")) {
            handleAddNewAccount(request, response, userName);
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, String userName)
            throws ServletException, IOException {
        String newPass = request.getParameter("password");
        String newPassSh = Encryption.toSHA256(newPass);
        String confirmPW = request.getParameter("confirmPassword");
        Employee em = dal.EmployeeDAO.getInstance().getAccountAdmin(userName);
        boolean hasError = false;

        String oldPass = request.getParameter("oldpassword");
        String oldPassSh = Encryption.toSHA256(oldPass);
        if (!oldPassSh.equals(em.getPassword())) {
            request.setAttribute("oldPasswordError", "Mật khẩu cũ không đúng");
            request.setAttribute("type", "changepass");
            request.getRequestDispatcher(linkInfoAdmin).forward(request, response);
            return;
        }

        hasError |= Validation.validateField(
                request, "passwordError", newPass, "PASSWORD", "Password",
                "Password must be at least 8 characters, include 1 letter, 1 digit, and 1 special character."
        );

        if (newPassSh.equals(em.getPassword())) {
            hasError = true;
            request.setAttribute("passwordError", "mật khẩu mới đang trùng với mật khẩu cũ");
        }

        if (!confirmPW.equals(newPass)) {
            hasError = true;
            request.setAttribute("confirmPasswordError", "mật khẩu confirm không trùng với mật khẩu mới");
        }

        if (hasError) {
            request.setAttribute("type", "changepass");
            request.getRequestDispatcher(linkInfoAdmin).forward(request, response);
            return;
        }

        dal.EmployeeDAO.getInstance().updatePasswordAdminByUsername(userName, newPassSh);
        response.sendRedirect(request.getContextPath() + "/developerPage?service=viewAll");
    }

    private void handleAddNewAccount(HttpServletRequest request, HttpServletResponse response, String userName)
            throws ServletException, IOException {
        String password = request.getParameter("password");
        boolean hasError = false;

        hasError |= Validation.validateField(
                request, "usernameError", userName, "USERNAME", "Username",
                "Username must be 5–20 characters, letters/numbers/underscores only."
        );
        hasError |= Validation.validateField(
                request, "passwordError", password, "PASSWORD", "Password",
                "Password must be at least 8 characters, include 1 letter, 1 digit, and 1 special character."
        );

        if (isUsernameTaken(userName)) {
            hasError = true;
            request.setAttribute("usernameError", "Username already exists.");
        }

        if (hasError) {
            request.getRequestDispatcher("View/Developer/AddManager.jsp").forward(request, response);
            return;
        }

        dal.AdminDao.getInstance().addNewAccountManager(userName, password);
        response.sendRedirect("developerPage");
    }

    private boolean isUsernameTaken(String userName) {
        List<String> employees = dal.AdminDao.getInstance().getAllUsernames();
        for (String username : employees) {
            if (username.equalsIgnoreCase(userName)) {
                return true;
            }
        }

        List<String> customerAccount = dal.CustomerAccountDAO.getInstance().getAllUsername();
        for (String usernameca : customerAccount) {
            if (usernameca.equalsIgnoreCase(userName)) {
                return true;
            }
        }
        return false;
    }

}
