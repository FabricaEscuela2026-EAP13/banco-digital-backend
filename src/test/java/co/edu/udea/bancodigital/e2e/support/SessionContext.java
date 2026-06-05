package co.edu.udea.bancodigital.e2e.support;

/**
 * Shared session context for storing/retrieving JWT tokens across test scenarios.
 */
public class SessionContext {

    private static String token;

    public static void setToken(String jwt) {
        token = jwt;
    }

    public static String getToken() {
        return token;
    }

    public static void clear() {
        token = null;
    }
}
