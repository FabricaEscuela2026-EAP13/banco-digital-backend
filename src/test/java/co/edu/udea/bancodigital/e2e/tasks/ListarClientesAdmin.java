package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.serenitybdd.screenplay.rest.interactions.Get;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class ListarClientesAdmin implements Task {

    public static ListarClientesAdmin deLaLista() {
        return instrumented(ListarClientesAdmin.class);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        String token = actor.recall("TOKEN");
        System.out.println("[DEBUG] TOKEN=" + token);
        CallAnApi apiAbility = actor.abilityTo(CallAnApi.class);
        System.out.println("[DEBUG] CallAnApi ability present=" + (apiAbility != null));
        if (apiAbility != null) {
            String resolvedUrl = apiAbility.resolve("");
            System.out.println("[DEBUG] resolvedUrl=" + resolvedUrl);
        }

        if (token != null && !token.equalsIgnoreCase("TOKEN_INVALIDO_O_VACIO") && !token.isEmpty()) {
            actor.attemptsTo(
                    Get.resource("/api/v1/admin/clientes")
                            .with(request -> request.header("Authorization", "Bearer " + token))
            );
        } else {
            actor.attemptsTo(
                    Get.resource("/api/v1/admin/clientes")
            );
        }
    }
}