@e2e
Feature: Bank account management

  Scenario: Consult account balance successfully
    Given the user authenticates with email "usuario.prueba@bancodigital.com" and password "Password456!"
    When they request the balance of their bank account
    Then the financial system should respond with status 200
  
  Scenario: Attempt to consult account balance without authentication
    Given the user is not authenticated in the system
    When they request the balance of an unauthorized account
    Then the financial system should respond with status 401