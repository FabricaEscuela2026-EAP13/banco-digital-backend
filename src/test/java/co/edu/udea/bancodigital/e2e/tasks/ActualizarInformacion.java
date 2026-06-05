package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.thucydides.model.environment.SystemEnvironmentVariables;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ActualizarInformacion implements Task {

    private final String correo;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ActualizarInformacion(String correo) {
        this.correo = correo;
    }

    public static ActualizarInformacion conCorreo(String correo) {
        return Tasks.instrumented(ActualizarInformacion.class, correo);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("TOKEN");

        SystemEnvironmentVariables variables = SystemEnvironmentVariables.createEnvironmentVariables();
        String baseUrl = variables.getProperty("restapi.baseurl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        String body = String.format(
                "{\"nombre\":\"Juan Actualizado\",\"primerApellido\":\"Pérez\",\"segundoApellido\":\"Gómez\",\"direccion\":\"Calle 123 #45-67\",\"telefono\":\"3123456789\",\"correo\":\"%s\"}",
                correo
        );

        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/usuarios/me"))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(body));

            if (token != null && !token.isBlank() && !token.equalsIgnoreCase("TOKEN_INVALIDO_O_VACIO")) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Serenity.setSessionVariable("STATUS_CODE").to(response.statusCode());
            Serenity.setSessionVariable("LAST_RESPONSE_BODY").to(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Error actualizando la información personal: " + e.getMessage(), e);
        }
    }
}
