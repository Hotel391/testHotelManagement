package dal;

import models.Room;
import models.RoomNService;
import models.Service;
import models.TypeRoom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

public class RoomDAO {

    private static RoomDAO instance;
    private Connection con;

    public static RoomDAO getInstance() {
        if (instance == null) {
            instance = new RoomDAO();
        }
        return instance;
    }

    private RoomDAO() {
        con = new DBContext().connect;
    }
    
    public int RoomCount(){
        String sql="select count(*) from Room";
        
        try(PreparedStatement st = con.prepareStatement(sql); ResultSet rs = st.executeQuery()){
            if(rs.next()){
                return rs.getInt(1);
            }
        } catch(SQLException e){
        }
        return 0;
    }

    public int RoomAvailableCount() {
        String sql = """
                     SELECT COUNT(*) \r
                     FROM Room r\r
                     WHERE NOT EXISTS (\r
                         SELECT 1 FROM BookingDetail bd \r
                     \tWHERE bd.RoomNumber = r.RoomNumber and \r
                     \tCONVERT(DATE, GETDATE()) >= bd.StartDate\r
                           and CONVERT(DATE, GETDATE()) < bd.EndDate\r
                     )\r
                     """
        ;
        
        try (PreparedStatement st = con.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
        }
        return 0;
    }
    public int RoomBookedCount() {
        String sql = """
                     SELECT COUNT(*) \r
                     FROM Room r\r
                     WHERE EXISTS (\r
                         SELECT 1 FROM BookingDetail bd \r
                     \tWHERE bd.RoomNumber = r.RoomNumber and \r
                     \tCONVERT(DATE, GETDATE()) >= bd.StartDate\r
                           and CONVERT(DATE, GETDATE()) < bd.EndDate\r
                     )\r
                     """
        ;
        
        try (PreparedStatement st = con.prepareStatement(sql); ResultSet rs = st.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
        }
        return 0;
    }

    public List<Room> getAllRoom() {
        List<Room> listRoom = new Vector<>();

        String sql = "SELECT r.RoomNumber, r.isCleaner, "
                + "tr.TypeId, tr.TypeName, tr.Description, tr.Price, "
                + "s.ServiceId, s.ServiceName, s.Price AS ServicePrice, rns.quantity "
                + "FROM Room r "
                + "JOIN TypeRoom tr ON r.TypeId = tr.TypeId "
                + "LEFT JOIN RoomNService rns ON tr.TypeId = rns.TypeId "
                + "LEFT JOIN [Service] s ON rns.ServiceId = s.ServiceId "
                + "ORDER BY r.RoomNumber";

        try (PreparedStatement ptm = con.prepareStatement(sql); ResultSet rs = ptm.executeQuery()) {

            while (rs.next()) {
                int roomNumber = rs.getInt("RoomNumber");
                Room foundRoom = null;

                // Tìm xem Room đã tồn tại trong list chưa
                for (Room r : listRoom) {
                    if (r.getRoomNumber() == roomNumber) {
                        foundRoom = r;
                        break;
                    }
                }

                if (foundRoom == null) {
                    TypeRoom typeRoom = new TypeRoom();
                    typeRoom.setTypeId(rs.getInt("TypeId"));
                    typeRoom.setTypeName(rs.getString("TypeName"));
                    typeRoom.setDescription(rs.getString("Description"));
                    typeRoom.setPrice(rs.getInt("Price"));

                    foundRoom = new Room(roomNumber, rs.getBoolean("isCleaner"), typeRoom);
                    listRoom.add(foundRoom);
                }

                int serviceId = rs.getInt("ServiceId");
                if (serviceId != 0) {
                    Service service = new Service();
                    service.setServiceId(serviceId);
                    service.setServiceName(rs.getString("ServiceName"));
                    service.setPrice(rs.getInt("ServicePrice"));

                    RoomNService rns = new RoomNService();
                    rns.setService(service);
                    rns.setQuantity(rs.getInt("quantity"));
                    rns.setTypeRoom(foundRoom.getTypeRoom());

                    foundRoom.getTypeRoom().addRoomNService(rns);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return listRoom;
    }

    public void updateRoom(int typeRoomID, int roomNumber) {
        String sql = "UPDATE [dbo].[Room]\n"
                + "   SET [TypeId] = ?\n"
                + " WHERE [RoomNumber] =?";

        try (PreparedStatement ptm = con.prepareStatement(sql);) {
            ptm.setInt(1, typeRoomID);
            ptm.setInt(2, roomNumber);
            ptm.executeUpdate();
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

    public int insertRoom(int roomNumber, int typeRoom) {
        String sql = "INSERT INTO [dbo].[Room]\n"
                + "           ([RoomNumber]\n"
                + "           ,[TypeId])\n"
                + "     VALUES(?, ?)";
        int n = 0;
        try (PreparedStatement ptm = con.prepareStatement(sql);) {
            
            ptm.setInt(1, roomNumber);
            ptm.setInt(2, typeRoom);
            n = ptm.executeUpdate();
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
        return n;
    }

    public void deleteRoom(int roomNumber) {
        String sql = "DELETE FROM [dbo].[Room]\n"
                + "      WHERE RoomNumber =?";

        try (PreparedStatement ptm = con.prepareStatement(sql);) {        
            ptm.setInt(1, roomNumber);
            ptm.executeUpdate();
        } catch (SQLException ex) {
            ex.getStackTrace();
        }
    }

    public List<TypeRoom> getAllTypeRoom() {
        List<TypeRoom> listTypeRoom = new Vector<>();

        String sql = "SELECT [TypeId]\n"
                + "      ,[Description]\n"
                + "      ,[TypeName]\n"
                + "      ,[Price]\n"
                + "  FROM [HotelManagementDB].[dbo].[TypeRoom]";

        try (PreparedStatement ptm = con.prepareStatement(sql); ResultSet rs = ptm.executeQuery()) {

            while (rs.next()) {
                TypeRoom tr = new TypeRoom(rs.getInt(1),
                        rs.getString(2), rs.getString(3),
                        rs.getInt(4));
                listTypeRoom.add(tr);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return listTypeRoom;
    }
}
