package co.edu.udea.bancodigital.e2e.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.serenitybdd.rest.SerenityRest;
import co.edu.udea.bancodigital.e2e.tasks.IniciarSesion;
import co.edu.udea.bancodigital.e2e.questions.ElCodigoDeEstado;
import co.edu.udea.bancodigital.e2e.support.SessionContext;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.rest.questions.ResponseConsequence.seeThatResponse;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class LoginStepDefinition {

    @Given("the user wants to login")
    public void theUserWantsToLogin() {
    }

    @When("they send valid credentials {string} and {string}")
    public void theySendValidCredentials(String correo, String contrasena) {
        theActorInTheSpotlight().attemptsTo(
                IniciarSesion.conCredenciales(correo, contrasena)
        );
    }

    @When("they send invalid credentials {string} and {string}")
    public void theySendInvalidCredentials(String correo, String contrasena) {
        theActorInTheSpotlight().attemptsTo(
                IniciarSesion.conCredenciales(correo, contrasena)
        );
    }

    @Then("the login should respond with status {int}")
    public void theLoginShouldRespondWithStatus(int statusCode) {
        theActorInTheSpotlight().should(
                seeThat(ElCodigoDeEstado.recibido(), is(statusCode))
        );
    }

    @Then("the login should return a token")
    public void theLoginShouldReturnAToken() {
        String token = SerenityRest.lastResponse().jsonPath().getString("token");
        theActorInTheSpotlight().remember("TOKEN", token);
        SessionContext.setToken(token);

        theActorInTheSpotlight().should(
                seeThatResponse("The login returns a non-null token",
                        response -> response.body("token", notNullValue()))
        );
    }
}
