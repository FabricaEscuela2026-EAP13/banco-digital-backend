package co.edu.udea.bancodigital.e2e.support;

import java.util.UUID;

public class DatosPrueba {
    public static final String DEFAULT_EMAIL = "usuario.prueba@bancodigital.com";
    public static final String DEFAULT_PASSWORD = "Password456!";
    public static final String DEFAULT_ACCOUNT_ID = "4f61d91a-d48d-4ad5-a5e8-2ad95cb5c0f4";
    public static final String DEFAULT_DESTINATION_ACCOUNT_ID = "032f3cb9-3925-47cc-8eb8-0bded3f52461";

    public static String generarCorreoDinamico() {
        String uniqueId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return "usuario" + uniqueId + "@gmail.com";
    }

    public static String generarDocumentoDinamico() {
        return String.valueOf((long) (Math.random() * 900000000L) + 100000000L);
    }
}
