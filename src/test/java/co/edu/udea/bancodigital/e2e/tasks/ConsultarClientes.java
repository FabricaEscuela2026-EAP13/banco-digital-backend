package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.Tasks;
import net.thucydides.model.environment.SystemEnvironmentVariables;
import net.thucydides.model.util.EnvironmentVariables;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ConsultarClientes implements Task {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public static ConsultarClientes registrados() {
        return Tasks.instrumented(ConsultarClientes.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {

        try {
            String token = actor.recall("TOKEN");

            EnvironmentVariables variables =
                    SystemEnvironmentVariables.createEnvironmentVariables();

            String baseUrl = variables.getProperty("restapi.baseurl");

            if (baseUrl == null || baseUrl.isEmpty()) {
                baseUrl = "http://localhost:8080";
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/admin/clientes"))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            Serenity.setSessionVariable("STATUS_CODE")
                    .to(response.statusCode());

            Serenity.setSessionVariable("LAST_RESPONSE_BODY")
                    .to(response.body());

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error consultando clientes: " + e.getMessage(), e
            );
        }
    }
}