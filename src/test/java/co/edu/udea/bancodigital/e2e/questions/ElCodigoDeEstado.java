package co.edu.udea.bancodigital.e2e.questions;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElCodigoDeEstado implements Question<Integer> {

    public static ElCodigoDeEstado recibido() {
        return new ElCodigoDeEstado();
    }

    @Override
    public Integer answeredBy(Actor actor) {
        Integer statusCode = Serenity.sessionVariableCalled("STATUS_CODE");
        if (statusCode == null) {
            try {
                statusCode = net.serenitybdd.rest.SerenityRest.lastResponse().statusCode();
            } catch (Exception e) {
                statusCode = 0;
            }
        }
        return statusCode;
    }
}
