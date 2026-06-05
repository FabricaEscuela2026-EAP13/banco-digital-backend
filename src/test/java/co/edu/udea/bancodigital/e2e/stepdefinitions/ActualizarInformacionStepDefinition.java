package co.edu.udea.bancodigital.e2e.stepdefinitions;

import co.edu.udea.bancodigital.e2e.tasks.ActualizarInformacion;
import io.cucumber.java.en.When;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;

public class ActualizarInformacionStepDefinition {
    
    @When("they update their personal information with valid data")
    public void theyUpdateTheirPersonalInformationWithValidData() {
        String email = theActorInTheSpotlight().recall("LOGIN_EMAIL");

        theActorInTheSpotlight().attemptsTo(
            ActualizarInformacion.conCorreo(
                email
            )
        );
    }

    @When("they update their personal information with invalid {string}")
    public void theyUpdateTheirPersonalInformationWithInvalidData(String email) {

        theActorInTheSpotlight().attemptsTo(
            ActualizarInformacion.conCorreo(email)
        );
    }

}
