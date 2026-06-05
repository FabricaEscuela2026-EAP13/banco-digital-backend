  Feature: Query the list of registered customers


    Scenario: Successful query
      Given the user authenticates with email "admin.prueba@bancodigital.com" and password "Prueba123*"
      When they request the customer list
      Then they can see the customer list with basic information
    
    Scenario: User without administrator permissions
        Given the user authenticates with email "usuario.prueba@bancodigital.com" and password "Password456!"
        When they request the customer list
        Then the financial system should respond with status 403

    Scenario: Unauthenticated user
        Given the user is not authenticated in the system
        When they request the customer list
        Then the financial system should respond with status 401


        