package co.edu.udea.bancodigital.e2e.questions;

import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class ElMensajeDeError implements Question<String> {

    public static ElMensajeDeError recibido() {
        return new ElMensajeDeError();
    }

    @Override
    public String answeredBy(Actor actor) {
        return SerenityRest.lastResponse().jsonPath().getString("message");
    }
}
