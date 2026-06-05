package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import co.edu.udea.bancodigital.e2e.support.DatosPrueba;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class TransferirFondos implements Task {

    private final String cuentaOrigen;
    private final String cuentaDestino;
    private final Object monto;

    public TransferirFondos(String cuentaOrigen, String cuentaDestino, Object monto) {
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
    }

    public static TransferirFondos entreCuentasValidasPor(Object monto) {
        return instrumented(TransferirFondos.class, "propia", "externa", monto);
    }

    public static TransferirFondos de(String cuentaOrigen, String cuentaDestino, Object monto) {
        return instrumented(TransferirFondos.class, cuentaOrigen, cuentaDestino, monto);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("TOKEN");

        String origenFinal = cuentaOrigen;
        if ("propia".equalsIgnoreCase(origenFinal)) {
            origenFinal = DatosPrueba.DEFAULT_ACCOUNT_ID;
        }

        String destinoFinal = cuentaDestino;
        if ("externa".equalsIgnoreCase(destinoFinal)) {
            destinoFinal = DatosPrueba.DEFAULT_DESTINATION_ACCOUNT_ID;
        }

        Object montoFinal = monto;
        if (monto instanceof String) {
            try {
                montoFinal = Integer.parseInt((String) monto);
            } catch (NumberFormatException e) {
                // Keep as string if it's invalid like "letras"
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("cuentaOrigen", origenFinal);
        body.put("cuentaDestino", destinoFinal);
        body.put("monto", montoFinal);

        final String finalToken = token;
        actor.attemptsTo(
                Post.to("/api/v1/transferencias")
                        .with(request -> {
                            if (finalToken != null && !finalToken.isEmpty()) {
                                request.header("Authorization", "Bearer " + finalToken);
                            }
                            return request
                                    .contentType(ContentType.JSON)
                                    .body(body);
                        })
        );
    }
}
