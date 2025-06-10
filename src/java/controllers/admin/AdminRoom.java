package controllers.admin;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import models.Room;
import models.TypeRoom;

public class AdminRoom extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String choose = request.getParameter("choose");
        if (choose == null) {
            choose = "viewAll";
        }
        String submit = request.getParameter("submit");
        List<TypeRoom> tr = null;
        if (choose.equals("insertRoom") || choose.equals("updateRoom")) {
            tr = dal.RoomDAO.getInstance().getAllTypeRoom();
        }

        if (choose.equals("deleteRoom")) {
            String roomNumberStr = request.getParameter("roomNumber");
            int roomNumber = Integer.parseInt(roomNumberStr);
            dal.RoomDAO.getInstance().deleteRoom(roomNumber);
            response.sendRedirect("room");
        }

        //forward sang InsertRoom.jsp
        if (choose.equals("insertRoom")) {
            if (submit == null) {
                request.setAttribute("typeRoom", tr);
                request.getRequestDispatcher("/View/Admin/InsertRoom.jsp").forward(request, response);
            }
        }

        //forward sang UpdateRoom.jsp
        if (choose.equals("updateRoom")) {
            if (submit == null) {
                String roomNumber = request.getParameter("roomNumber");
                String typeRoomId = request.getParameter("typeRoomId");
                request.setAttribute("roomNumber", roomNumber);
                request.setAttribute("typeRoomId", typeRoomId);
                request.setAttribute("typeRoom", tr);
                request.getRequestDispatcher("/View/Admin/UpdateRoom.jsp").forward(request, response);
            }
        }

        //view list room
        if (choose.equals("viewAll")) {
            List<Room> r = dal.RoomDAO.getInstance().getAllRoom();
            request.setAttribute("listR", r);
            request.getRequestDispatcher("/View/Admin/ViewRoom.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String choose = request.getParameter("choose");
        String roomNumberStr = request.getParameter("roomNumber");
        String typeRoomIdStr = request.getParameter("typeRoomId");
        //update room
        if (choose.equals("updateRoom")) {
            String submit = request.getParameter("submit");
            if (submit != null) {
                int roomNumber = 0;
                int typeRoomID = 0;
                try {
                    roomNumber = Integer.parseInt(roomNumberStr);
                    typeRoomID = Integer.parseInt(typeRoomIdStr);
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "Lỗi định dạng số phòng hoặc loại phòng.");
                }
                dal.RoomDAO.getInstance().updateRoom(typeRoomID, roomNumber);
            }
        }

        //insert new room
        if (choose.equals("insertRoom")) {
            String submit = request.getParameter("submit");
            if (submit != null) {

                boolean haveError = false;
                int roomNumber = 0;
                int typeRoomId = 0;

                request.setAttribute("roomNumber", roomNumberStr);

                List<TypeRoom> tr = dal.RoomDAO.getInstance().getAllTypeRoom();
                request.setAttribute("typeRoom", tr);

                try {
                    typeRoomId = Integer.parseInt(typeRoomIdStr);
                    roomNumber = Integer.parseInt(roomNumberStr);
                    if (roomNumber < 0) {
                        request.setAttribute("error", "Room Number must be a positive integer.");
                        haveError = true;
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("error", "Room Number must be a positive integer.");
                    haveError = true;
                }
                if (haveError) {
                    request.getRequestDispatcher("/View/Admin/InsertRoom.jsp").forward(request, response);
                    return;
                }
                dal.RoomDAO.getInstance().insertRoom(roomNumber, typeRoomId);
            }
        }
        response.sendRedirect("room");
    }
}
