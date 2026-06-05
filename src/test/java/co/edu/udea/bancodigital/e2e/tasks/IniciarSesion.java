package co.edu.udea.bancodigital.e2e.tasks;

import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.rest.interactions.Post;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;

import static net.serenitybdd.screenplay.Tasks.instrumented;

public class IniciarSesion implements Task {

    private final String correo;
    private final String contrasena;

    public IniciarSesion(String correo, String contrasena) {
        this.correo = correo;
        this.contrasena = contrasena;
    }

    public static IniciarSesion conCredenciales(String correo, String contrasena) {
        return instrumented(IniciarSesion.class, correo, contrasena);
    }

    @Override
    public <T extends Actor> void performAs(T actor) {
        Map<String, String> body = new HashMap<>();
        body.put("correo", correo);
        body.put("contrasena", contrasena);

        actor.attemptsTo(
                Post.to("/api/v1/auth/login")
                        .with(request -> request
                                .contentType(ContentType.JSON)
                                .body(body))
        );
    }
}
