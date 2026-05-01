package za.ac.tut.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import za.ac.tut.model.User;

public final class PatientMapper {

    private PatientMapper() {
    }

    public static User fromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setTitle(rs.getString("title"));
        user.setFirstName(rs.getString("first_name"));
        user.setSurname(rs.getString("surname"));
        user.setDob(rs.getDate("dob"));
        user.setGender(rs.getString("gender"));
        user.setMaritalStatus(rs.getString("marital_status"));
        user.setEmail(rs.getString("email"));
        user.setCellNumber(rs.getString("cell_number"));
        user.setIdNumber(rs.getString("id_number"));
        user.setEmergencyContactName(rs.getString("emergency_contact_name"));
        user.setEmergencyContactNumber(rs.getString("emergency_contact_number"));
        user.setBloodGroup(rs.getString("blood_group"));
        user.setKnownAllergies(rs.getString("known_allergies"));
        user.setChronicConditions(rs.getString("chronic_conditions"));
        user.setAddress(rs.getString("address"));
        return user;
    }
}
