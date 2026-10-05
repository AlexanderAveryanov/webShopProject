# Tasks

## 1. Конфигурация сборки

- [x] 1.1 Добавить свойство `maven-surefire-plugin.version` со значением `3.5.5` в раздел `<properties>` родительского `pom.xml`, рядом с `maven.compiler-plugin.version`, с коротким комментарием-ссылкой на блок `pluginManagement`. Проверка: свойство присутствует в pom и используется в блоке плагина без повторения литерала
- [x] 1.2 Добавить блок `maven-surefire-plugin` в `pluginManagement` родительского `pom.xml` со ссылкой `${maven-surefire-plugin.version}`. Проверка: `mvn help:effective-pom -pl backend/product-service` показывает для surefire версию `3.5.5` вместо `2.12.4`
- [x] 1.3 Написать перед блоком `maven-surefire-plugin` поясняющий комментарий: почему версию нужно задавать явно (импорт BOM переносит только `dependencyManagement`, раздел `pluginManagement` не наследуется), почему выбрана именно версия из BOM, почему достаточно `pluginManagement` без подключения плагина в модулях, и способ проверки через `mvn help:effective-pom`. Проверка: комментарий присутствует и содержит все четыре объяснения

## 2. Проверка

- [x] 2.1 Убедиться, что дочерние модули не требуют изменений: в `backend/auth-service/pom.xml` и `backend/product-service/pom.xml` нет объявлений surefire. Проверка: `git diff --stat` показывает изменённым только `pom.xml` в корне
- [x] 2.2 Выполнить `mvn clean test` и убедиться, что тесты реально выполняются во всех модулях. Проверка: в выводе `Running com.shop.auth.service.AuthServiceTest` с `Tests run: 9` и `Running com.shop.product.service.ProductServiceTest` с `Tests run: 14`, итого 23, `BUILD SUCCESS`
- [x] 2.3 Выполнить `mvn clean package` и убедиться, что полная сборка с упаковкой проходит с работающими тестами. Проверка: `BUILD SUCCESS`, в выводе ненулевой итоговый счётчик тестов, для каждого модуля создан исполняемый jar
- [x] 2.4 Проверить, что падающий тест действительно проваливает сборку: временно изменить одно ожидаемое значение в `ProductServiceTest`, выполнить `mvn test -pl backend/product-service` и убедиться в ненулевом коде выхода. Проверка: сборка падает с ошибкой теста. После проверки вернуть изменённое значение обратно и убедиться, что `mvn test` снова зелёный