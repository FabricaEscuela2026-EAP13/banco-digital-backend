package co.edu.udea.bancodigital.e2e.stepdefinitions;

import io.cucumber.java.Before;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.actors.OnlineCast;
import net.serenitybdd.screenplay.rest.abilities.CallAnApi;
import net.thucydides.model.util.EnvironmentVariables;
import net.thucydides.model.environment.SystemEnvironmentVariables;

public class Hook {

    @Before
    public void setup() {
        OnStage.setTheStage(new OnlineCast());
        
        EnvironmentVariables variables = SystemEnvironmentVariables.createEnvironmentVariables();
        String baseUrl = variables.getProperty("restapi.baseurl");
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = "http://localhost:8080";
        }
        
        OnStage.theActorCalled("Usuario").can(CallAnApi.at(baseUrl));
    }
}
