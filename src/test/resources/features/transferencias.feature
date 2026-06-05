Feature: Bank transfers

  Scenario: Perform successful transfer between accounts
    Given the user has funds in their account to transfer
    When they perform a transfer between valid accounts for an amount of 100
    Then the transfer system should respond with status 201

  Scenario Outline: Failed transfers due to invalid or exceptional data
    Given the user has funds in their account to transfer
    When they perform a transfer from "<origin_account>" to "<destination_account>" for an amount of "<amount>"
    Then the transfer system should respond with status <expected_status>

    Examples:
      | origin_account | destination_account | amount | expected_status |
      | propia         | externa             | -50000 | 400             |
      | propia         | externa             | letras | 400             |