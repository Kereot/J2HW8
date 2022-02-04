package server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAuthService implements AuthService {

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        try {
            PreparedStatement psLogin = DBHandler.getConnection().prepareStatement("SELECT nickname FROM clients WHERE password = ? AND login = ?;");
            psLogin.setString(1, password);
            psLogin.setString(2, login);
            ResultSet rs = psLogin.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            PreparedStatement psDoReg = DBHandler.getConnection().prepareStatement("INSERT INTO clients (nickname, password, login) VALUES ( ? , ? , ? );");
            psDoReg.setString(1, nickname);
            psDoReg.setString(2, password);
            psDoReg.setString(3, login);
            try {
                psDoReg.executeUpdate(); // Уникальность в полях никнейм и логин: ошибка, если заняты.
                return true;
            } catch (SQLException e) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
