@e2e
Feature: User registration

  Scenario: Successful customer registration
    Given the user wants to register
    When they send a dynamic valid registration
    Then the system should respond with status 201

  Scenario Outline: Failed registration due to invalid data
    Given the user wants to register
    When they send a registration with "<email>" and "<document>"
    Then the system should respond with status <status>

    Examples:
      | email           | document  | status |
      | correo-invalido | 123456789 | 400    |
      |                 | 123456789 | 400    |
      | test@test.com   |           | 400    |