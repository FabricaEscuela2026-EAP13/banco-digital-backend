package co.edu.udea.bancodigital.e2e.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import co.edu.udea.bancodigital.e2e.tasks.IniciarSesion;
import co.edu.udea.bancodigital.e2e.tasks.ConsultarSaldo;
import co.edu.udea.bancodigital.e2e.questions.ElCodigoDeEstado;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.is;

public class CuentasStepDefinition {

    @Given("the user is not authenticated in the system")
    public void theUserIsNotAuthenticated() {
        theActorInTheSpotlight().remember("TOKEN", "TOKEN_INVALIDO_O_VACIO");
    }

    @Given("the user authenticates with email {string} and password {string}")
    public void theUserAuthenticatesWithEmailAndPassword(String email, String password) {
        theActorInTheSpotlight().attemptsTo(
                IniciarSesion.conCredenciales(email, password)
        );
        String token = SerenityRest.lastResponse().jsonPath().getString("token");
        theActorInTheSpotlight().remember("TOKEN", token);
    }

    @When("they request the balance of their bank account")
    public void theyRequestTheBalanceOfTheirBankAccount() {
        theActorInTheSpotlight().attemptsTo(
                ConsultarSaldo.deMiCuenta()
        );
    }

    @When("they request the balance of an unauthorized account")
    public void theyRequestTheBalanceOfAnUnauthorizedAccount() {
        theActorInTheSpotlight().attemptsTo(
                ConsultarSaldo.deLaCuenta("an unauthorized account")
        );
    }

    @Then("the financial system should respond with status {int}")
    public void theFinancialSystemShouldRespondWithStatus(Integer expectedStatus) {
        theActorInTheSpotlight().should(
                seeThat(ElCodigoDeEstado.recibido(), is(expectedStatus))
        );
    }
}
