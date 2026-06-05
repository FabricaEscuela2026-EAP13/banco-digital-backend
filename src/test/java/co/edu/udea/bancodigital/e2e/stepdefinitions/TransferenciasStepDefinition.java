package co.edu.udea.bancodigital.e2e.stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import net.serenitybdd.rest.SerenityRest;
import co.edu.udea.bancodigital.e2e.support.DatosPrueba;
import co.edu.udea.bancodigital.e2e.tasks.IniciarSesion;
import co.edu.udea.bancodigital.e2e.tasks.TransferirFondos;
import co.edu.udea.bancodigital.e2e.questions.ElCodigoDeEstado;

import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static org.hamcrest.Matchers.is;

public class TransferenciasStepDefinition {

    @Given("the user has funds in their account to transfer")
    public void theUserHasFundsInTheirAccountToTransfer() {
        theActorInTheSpotlight().attemptsTo(
                IniciarSesion.conCredenciales(DatosPrueba.DEFAULT_EMAIL, DatosPrueba.DEFAULT_PASSWORD)
        );
        String token = SerenityRest.lastResponse().jsonPath().getString("token");
        theActorInTheSpotlight().remember("TOKEN", token);
    }

    @When("they perform a transfer between valid accounts for an amount of {int}")
    public void theyPerformATransferBetweenValidAccountsForAnAmountOf(int amount) {
        theActorInTheSpotlight().attemptsTo(
                TransferirFondos.entreCuentasValidasPor(amount)
        );
    }

    @When("they perform a transfer from {string} to {string} for an amount of {string}")
    public void theyPerformATransferFromToForAnAmountOf(String origin, String destination, String amount) {
        theActorInTheSpotlight().attemptsTo(
                TransferirFondos.de(origin, destination, amount)
        );
    }

    @Then("the transfer system should respond with status {int}")
    public void theTransferSystemShouldRespondWithStatus(Integer expectedStatus) {
        theActorInTheSpotlight().should(
                seeThat(ElCodigoDeEstado.recibido(), is(expectedStatus))
        );
    }
}
