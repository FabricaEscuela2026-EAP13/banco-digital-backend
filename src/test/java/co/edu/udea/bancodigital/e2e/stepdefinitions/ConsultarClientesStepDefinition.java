package co.edu.udea.bancodigital.e2e.stepdefinitions;

import co.edu.udea.bancodigital.e2e.questions.ElCodigoDeEstado;
import co.edu.udea.bancodigital.e2e.questions.LaListaDeClientes;
import co.edu.udea.bancodigital.e2e.tasks.ConsultarClientes;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static net.serenitybdd.screenplay.GivenWhenThen.seeThat;
import static net.serenitybdd.screenplay.actors.OnStage.theActorInTheSpotlight;
import static org.hamcrest.Matchers.is;

public class ConsultarClientesStepDefinition {


    @When("they request the customer list")
    public void theyRequestTheCustomerList() {

        theActorInTheSpotlight().attemptsTo(
            ConsultarClientes.registrados()
        );
    }

    @Then("they can see the customer list with basic information")
    public void theyCanSeeTheCustomerListWithBasicInformation() {

        theActorInTheSpotlight().should(
            seeThat(ElCodigoDeEstado.recibido(), is(200))
        );

        theActorInTheSpotlight().should(
            seeThat(LaListaDeClientes.contieneInformacionBasica(), is(true))
        );
    }

    @Given("an unauthenticated user tries to access the system")
    public void anUnauthenticatedUserTriesToAccessTheSystem() {
        theActorInTheSpotlight().remember("TOKEN", null);
    }

}