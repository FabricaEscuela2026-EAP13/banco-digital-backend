package co.edu.udea.bancodigital.e2e.runners;

import io.cucumber.junit.CucumberOptions;
import net.serenitybdd.cucumber.CucumberWithSerenity;
import org.junit.runner.RunWith;

@RunWith(CucumberWithSerenity.class)
@CucumberOptions(
        features = "src/test/resources/features/registro.feature",
        glue = "co.edu.udea.bancodigital.e2e.stepdefinitions",
        snippets = CucumberOptions.SnippetType.CAMELCASE
)
public class RegistroRunner {}
