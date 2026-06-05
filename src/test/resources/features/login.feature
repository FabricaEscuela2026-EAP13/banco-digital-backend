@e2e
Feature: User login

  Scenario: Successful login
    Given the user wants to login
    When they send valid credentials "juan@example.com" and "Abc123#@"
    Then the login should respond with status 200
    And the login should return a token

  Scenario Outline: Failed login
    Given the user wants to login
    When they send invalid credentials "<email>" and "<password>"
    Then the login should respond with status <status>

    Examples:
      | email           | password | status |
      | juan@gmail.com  | mala123  | 404    |
      | correo-invalido | Abc123#@ | 400    |
      |                 | Abc123#@ | 400    |
      | juan@gmail.com  |          | 400    |