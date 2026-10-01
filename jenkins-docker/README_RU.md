# Семинар Jenkins и Akka JVM

Файлы только для четырёх заданий и ответа на вопросы. Отчёт — REPORT_KK.html. Исходники — Scala/sbt проект в этой папке.

1. Поместите проект в свой Git-репозиторий. На Jenkins-агентах установите sbt 1.10.7 и JDK 11/17/21; в Manage Jenkins → Tools назовите JDK: JDK11, JDK17, JDK21. JVM самого Jenkins controller/agent настраивается отдельно по требованиям установленного Jenkins.
2. Нужны плагины Pipeline, Git, JUnit, Pipeline Stage View, Docker Pipeline; для email настройте SMTP.
3. Создайте Pipeline `Akka-IoT-Ingest-Pipeline`, вставьте Jenkinsfile.task1 в Pipeline script и задайте REPO_URL. После этого замените скрипт на Jenkinsfile.task2 для тестов. Для закрытого Git-репозитория добавьте подходящий credentialsId в шаг git.
4. Для Task 2 выполните успешную сборку, затем FAIL_TEST=true, затем снова false. После этого снимите реальные Stage View, Test Result Trend и консоль ошибки. Альтернатива через Git описана в отчёте.
5. Для матрицы используйте Jenkinsfile.matrix с тем же REPO_URL. Профили Standard и Cluster действительно меняют конфигурацию тестов. Совместимость не объявляется проверенной до запуска всех шести комбинаций.
6. Для полного основного Jenkinsfile создайте Multibranch Pipeline для этого репозитория: `when { branch 'main' }` требует контекста ветки. Добавьте Linux-агент с label `docker-linux`, работающим Docker daemon и доступом к registry. Создайте Username/password credential с ID `dockerhub-credentials-id`: username Docker Hub и token в поле password. Задайте IMAGE_REPOSITORY как `ваш_логин/akka-iot-service`.
7. На main выполните build и снимите скриншоты публикации. Контейнер запускайте с MQTT_BROKER, указывающим доступный брокер: `docker run --rm -e MQTT_BROKER=tcp://broker:1883 ваш_логин/akka-iot-service:НОМЕР`.

Для локальной проверки после установки JDK 17 и sbt:

```text
sbt clean compile scalastyle test assembly
sbt -Diot.profile=Cluster test
sbt -Ddemo.fail=true test
```

Последняя команда должна завершиться неуспешно — это демонстрация сломанного теста.

В текущем окружении Docker daemon недоступен, Jenkins/репозиторий/registry не предоставлены. Сборки, публикация и скриншоты пока не выполнены. Akka 2.6.21 выбрана как доступная учебная версия, а не как рекомендация новой production-системе.
