package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;
import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class RegistrarUsuario implements Task {

    private final Map<String, Object> body;

    public RegistrarUsuario(Map<String, Object> body) {
        this.body = body;
    }

    public static RegistrarUsuario con(Map<String, Object> body) {
        return instrumented(RegistrarUsuario.class, body);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Post.to("/api/v1/usuarios/registro")
                        .with(request -> request
                                .contentType(ContentType.JSON)
                                .body(body))
        );
    }
}
