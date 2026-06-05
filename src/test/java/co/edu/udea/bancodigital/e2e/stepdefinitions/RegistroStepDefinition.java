package co.edu.udea.bancodigital.e2e.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import co.edu.udea.bancodigital.e2e.tasks.RegistrarUsuario;
import co.edu.udea.bancodigital.e2e.questions.ElCodigoDeEstado;
import co.edu.udea.bancodigital.e2e.interactions.ConstruirCuerpoRegistro;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.is;

public class RegistroStepDefinition {

    @Given("the user wants to register")
    public void theUserWantsToRegister() {
    }

    @When("they send a dynamic valid registration")
    public void theySendADynamicValidRegistration() {
        theActorInTheSpotlight().attemptsTo(
                RegistrarUsuario.con(ConstruirCuerpoRegistro.conDatosValidosDinamicos())
        );
    }

    @When("they send a registration with {string} and {string}")
    public void theySendARegistrationWith(String email, String document) {
        theActorInTheSpotlight().attemptsTo(
                RegistrarUsuario.con(ConstruirCuerpoRegistro.con(email, document))
        );
    }

    @Then("the system should respond with status {int}")
    public void theSystemShouldRespondWithStatus(int status) {
        theActorInTheSpotlight().should(
                seeThat(ElCodigoDeEstado.recibido(), is(status))
        );
    }
}
