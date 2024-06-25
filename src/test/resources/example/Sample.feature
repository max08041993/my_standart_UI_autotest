@TC-1

Feature: Example

  Scenario Outline: Пример сценария
    Given Открываю страницу http://google.com
    When Ввожу в поле ввода Привет Мир
    When Нажимаю кнопку поиска
    When Проверяю что найдено более <count> результатов
    Examples:
      | count       |
#      Позитивный сценарий
      | 50000000    |
#      Заведомо Fail сценарий
      | 50000000000 |

