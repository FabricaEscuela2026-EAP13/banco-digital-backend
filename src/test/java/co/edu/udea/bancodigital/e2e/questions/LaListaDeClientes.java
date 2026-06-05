package co.edu.udea.bancodigital.e2e.questions;

import net.serenitybdd.core.Serenity;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;


public class LaListaDeClientes implements Question<Boolean> {

    public static LaListaDeClientes contieneInformacionBasica() {
        return new LaListaDeClientes();
        
    }

        @Override
        public Boolean answeredBy(Actor actor) {

            String body = Serenity.sessionVariableCalled("LAST_RESPONSE_BODY");

            System.out.println("==== RESPONSE BODY ====");
            System.out.println(body);

            return body != null && !body.isBlank();
        }
}