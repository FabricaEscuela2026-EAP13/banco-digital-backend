Feature: Update customer information

  Scenario: Successful update
    Given the user authenticates with email "usuario.prueba@bancodigital.com" and password "Password456!"
    When they update their personal information with valid data using email "usuario.actualizado4@test.com"
    Then the financial system should respond with status 200


  Scenario Outline: Update with invalid format
    Given the user authenticates with email "usuario.prueba@bancodigital.com" and password "Password456!"
    When they update their personal information with invalid "<email>"
    Then the financial system should respond with status 400
    Examples:
        | email                | 
        | correo.invalido.com  | 
        |                      | 

  Scenario: Unauthenticated user
    Given the user is not authenticated in the system
    When they update their personal information with valid data
    Then the financial system should respond with status 401