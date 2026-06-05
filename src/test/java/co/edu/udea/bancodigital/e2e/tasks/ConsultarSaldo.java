package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import co.edu.udea.bancodigital.e2e.support.DatosPrueba;
import net.thucydides.model.util.EnvironmentVariables;
import net.thucydides.model.environment.SystemEnvironmentVariables;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class ConsultarSaldo implements Task {

    private final String idCuenta;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(java.time.Duration.ofSeconds(10)) 
            .build();

    public ConsultarSaldo(String idCuenta) {
        this.idCuenta = idCuenta;
    }

    public static ConsultarSaldo deLaCuenta(String idCuenta) {
        return instrumented(ConsultarSaldo.class, idCuenta);
    }

    public static ConsultarSaldo deMiCuenta() {
        return instrumented(ConsultarSaldo.class, (String) null);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("TOKEN");
        String finalId = idCuenta;

        EnvironmentVariables variables = SystemEnvironmentVariables.createEnvironmentVariables();
        String baseUrl = variables.getProperty("restapi.baseurl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }

        if (finalId == null || finalId.isEmpty() || finalId.equalsIgnoreCase("their bank account")) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(baseUrl + "/api/v1/cuentas/me"))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + token)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    String body = response.body();
                    int idIndex = body.indexOf("\"idCuenta\":\"");
                    if (idIndex != -1) {
                        int start = idIndex + 12;
                        int end = body.indexOf("\"", start);
                        finalId = body.substring(start, end);
                    } else {
                        finalId = DatosPrueba.DEFAULT_ACCOUNT_ID;
                    }
                } else {
                    finalId = DatosPrueba.DEFAULT_ACCOUNT_ID;
                }
            } catch (Exception e) {
                finalId = DatosPrueba.DEFAULT_ACCOUNT_ID;
            }
        } else if (finalId.equalsIgnoreCase("unauthorized account") || finalId.equalsIgnoreCase("an unauthorized account")) {
            finalId = DatosPrueba.DEFAULT_ACCOUNT_ID;
        }

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/cuentas/" + finalId + "/saldo"))
                    .header("Content-Type", "application/json");

            if (token != null && !token.equalsIgnoreCase("TOKEN_INVALIDO_O_VACIO")) {
                builder.header("Authorization", "Bearer " + token);
            }

            HttpRequest request = builder.GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            Serenity.setSessionVariable("STATUS_CODE").to(response.statusCode());
            Serenity.setSessionVariable("LAST_RESPONSE_BODY").to(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar saldo con HttpClient nativo: " + e.getMessage(), e);
        }
    }
}
