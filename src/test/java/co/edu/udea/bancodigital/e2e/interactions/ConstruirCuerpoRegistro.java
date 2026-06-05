package co.edu.udea.bancodigital.e2e.interactions;

import java.util.HashMap;
import java.util.Map;
import co.edu.udea.bancodigital.e2e.support.DatosPrueba;

public class ConstruirCuerpoRegistro {

    public static Map<String, Object> conDatosValidosDinamicos() {
        return con(DatosPrueba.generarCorreoDinamico(), DatosPrueba.generarDocumentoDinamico());
    }

    public static Map<String, Object> con(String correo, String documento) {
        Map<String, Object> body = new HashMap<>();
        body.put("idTipoDoc", 1);
        body.put("numeroDocumento", documento);
        body.put("nombre", "Pablo");
        body.put("primerApellido", "Perez");
        body.put("segundoApellido", "Gomez");
        body.put("direccion", "Calle 123");
        body.put("telefono", "3001234567");
        body.put("correo", correo);
        body.put("contrasena", "Abc123#@");
        return body;
    }
}
