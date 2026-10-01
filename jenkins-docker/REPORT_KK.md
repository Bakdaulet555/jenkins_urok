# Jenkins және Akka JVM бойынша семинар

## Орындалу күйі

Төрт тапсырмаға арналған файлдар дайындалды. Осы ортада Jenkins сервері көрсетілмеген, Docker daemon жұмыс істемейді; GitHub репозиторийі мен Docker Hub credentials берілмеген. Сондықтан Jenkins build, алты JDK/profile комбинациясы және Docker push орындалды деп мәлімделмейді. Нақты скриншоттар мен сәтті build логтары жоқ. Төмендегі командалар — орындалатын қадамдар, орындалған жұмыстың жалған логы емес.

## 1 тапсырма

`Jenkinsfile.task1` ішінде agent any, Checkout, Compile және Static Analysis бар. Compile: `sbt clean compile`; Static Analysis: `sbt scalastyle`. Windows-та `bat`, Linux-та `sh` қолданылады. Қате кезінде build FAILURE болады; SMTP және NOTIFY_TO бапталса, командаға email жіберіледі. Толық скрипт төмендегі қосымшада берілген.

## 2 тапсырма

`Jenkinsfile.task2` Unit Tests кезеңін қосады. Тест нәтижелері stage post/always ішіндегі `junit '**/target/test-reports/*.xml'` арқылы жарияланады. Стандартты sbt test reporter XML есептерін target/test-reports ішіне жазады. Akka TestKit үш тесті бос бастапқы күйді, дұрыс температураны және жарамсыз өлшемнен кейін күйдің сақталуын тексереді.

Сәтсіздікті көрсету: FAIL_TEST=true арқылы іске қосу. Күтілетін жауап 99.0, актер жауабы 23.5 болғандықтан expectMsg тесті өтпейді. sbt нөлден бөлек exit code қайтарады, сондықтан бұл pipeline қызыл FAILURE болады; JUnit нәтижелері бәрібір жарияланады. FAIL_TEST=false қайтадан дұрыс шартты іске қосады. Егер Git өзгерісі міндетті болса: `git apply --unidiff-zero demo/failure.patch`, commit және push; кейін сол commit-ті `git revert` арқылы қайтару керек. Өз репозиторийіңізде орындаңыз.

## 3 тапсырма

`Jenkinsfile.matrix`: JDK11/JDK17/JDK21 × Standard/Cluster = 6 комбинация. Әр ұяшық бөлек агент workspace-та checkout және clean test орындайды. JDK Jenkins tools арқылы JAVA_HOME/PATH-қа беріледі. `sbt ++jdk17` дұрыс емес: sbt `++` Java емес, Scala нұсқасын ауыстырады.

Standard жергілікті ActorSystem қолданады. Cluster профилі нақты single-node Akka Cluster іске қосып, түйіннің Up күйіне өткенін тексереді. Бұл көп түйінді желілік жүктеме сынағы емес. Бір мезетте алты build үшін кемінде алты бос executor қажет; аз болса, ұяшықтар кезекке тұрады. JDK 21 үйлесімділігі осы жерде орындалмағандықтан расталған жоқ. Қате болса, Console Output-та JVM/Scala/Akka нұсқаларын, bytecode target және конфигурацияны тексеріп, кітапханаларды үйлесімді жиынға жаңарту қажет.

## 4 тапсырма

Негізгі `Jenkinsfile` — Multibranch Pipeline үшін. Тек main тармағы және сәтті тестілерден кейін fat JAR жиналып, Docker image жарияланады. `skipStagesAfterUnstable` және SUCCESS шарты сәтсіз тесттен кейін push жасауға жол бермейді. Dockerfile Java 17 runtime қолданады; JAR ішіне Akka және MQTT тәуелділіктері кіреді. MQTT_BROKER және MQTT_TOPIC runtime айнымалылары арқылы беріледі. MQTT payload — температураның бір сандық мәні. Нақты MQTT брокерімен интеграциялық сынақ бұл ортада орындалған жоқ.

Docker Hub token Jenkins Credentials ішінде `dockerhub-credentials-id` ID-мен сақталады. Image тегтері: BUILD_NUMBER және latest. Кодқа пароль жазылмайды. Image жариялау құрылғыға автоматты deploy жасауды білдірмейді; edge құрылғыда бөлек pull/restart қадамы керек.

## Семинар сұрақтарына жауаптар

1. Jenkins 2011 жылы Hudson қауымдастығының бөлінуінен қалыптасты. Негізгі мәні — жобаны басқару тәуелсіздігі және ашық үлес қосу. LTS тұрақты жаңарту арнасын, weekly жиі жаңартуды ұсынады.
2. CI сервері жоқ кезде build/test қолмен орындалады. Continuous deployment-та тексерілген өзгеріс автоматты енгізіледі. IoT-де бұл қайталанатын жеткізуге көмектеседі, бірақ әр жүйеге автоматты production deploy міндетті емес.
3. Актер хабарламалары асинхронды: TestKit expectMsg/awaitAssert және шектелген timeout қолданылады. Thread.sleep орнына шартты күту керек; агент баяу болса akka.test.timefactor реттеледі. ActorSystem әр тест жиынынан кейін жабылады.
4. Dashboard қай build бұзылғанын бірден көрсетеді; хабарлама job, build және console сілтемесін береді. Бұл ақауды табу уақытын азайтады.
5. Статикалық талдау және coverage өзгерістің сапасын бағалауға көмектеседі. Бірақ жоғары coverage memory leak немесе mailbox overflow жоқ екенін дәлелдемейді: жүктеме және ресурстық сынақтар да қажет.
6. Continuous delivery — release дайын, production енгізуге адам шешім қабылдайды. Continuous deployment — енгізу автоматты. Сыни IoT жүйесінде бөлек approval, кезеңдік rollout және rollback көбіне орынды.
7. OOM кезінде алдымен қай JVM жады таусылғанын анықтау қажет. sbt үшін -J-Xmx және MaxMetaspaceSize, controller үшін JAVA_OPTS бөлек реттеледі; қатар жүретін build саны RAM-ға сай шектеледі. Ауыр жұмыстар агенттерде орындалады.
8. Matrix JDK, profile, transport немесе serializer комбинацияларын бөлек тексеруге мүмкіндік береді. Әр комбинация шынымен конфигурацияға берілуі тиіс; ось атауын өзгерту өздігінен жаңа режим қоспайды.

## Нақты Jenkins орнатылымында қоса тіркелетін дәлелдер

1. Task 1: сәтті Stage View және sbt compile Console Output үзіндісі.
2. Task 2: Test Result Trend, әдейі сәтсіз тест консолі және түзетілген build.
3. Task 3: алты комбинация көрсетілген Matrix View.
4. Task 4: Docker build/push консолі және registry-дегі BUILD_NUMBER тегі.

Бұл дәлелдер тек нақты іске қосқаннан кейін алынады; архивте олардың орнына жасанды скриншоттар жоқ.

## Құжаттама

- https://www.jenkins.io/doc/book/pipeline/syntax/
- https://www.jenkins.io/doc/book/pipeline/docker/
- https://www.jenkins.io/doc/book/platform-information/support-policy-java/
- https://doc.akka.io/api/akka/2.6/akka/index.html


## Jenkinsfile.task1 толық мәтіні

```groovy
def runSbt(String args) {
    if (isUnix()) { sh "sbt -batch ${args}" }
    else { bat "call sbt -batch ${args}" }
}
pipeline {
    agent any
    tools { jdk 'JDK17' }
    options { skipDefaultCheckout(true); disableConcurrentBuilds(); timestamps() }
    parameters {
        string(name: 'REPO_URL', defaultValue: '', description: 'Git repository URL containing this project')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to build')
        string(name: 'NOTIFY_TO', defaultValue: '', description: 'Team email; configure SMTP before setting')
        booleanParam(name: 'FAIL_TEST', defaultValue: false, description: 'Task 2: demonstrate a failed test')
    }
    stages {
        stage('Checkout') {
            steps {
                script { if (!params.REPO_URL.trim()) { error('Set REPO_URL to your Git repository') } }
                git branch: params.GIT_BRANCH, url: params.REPO_URL
            }
        }
        stage('Compile') { steps { script { runSbt('clean compile') } } }
        stage('Static Analysis') { steps { script { runSbt('scalastyle') } } }
    }
    post {
        failure {
            script {
                if (params.NOTIFY_TO?.trim()) {
                    mail to: params.NOTIFY_TO, subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Build failed. Console: ${env.BUILD_URL}console"
                } else { echo 'Build FAILED. NOTIFY_TO is not configured.' }
            }
        }
    }
}
```


## Jenkinsfile.task2 толық мәтіні

```groovy
def runSbt(String args) {
    if (isUnix()) { sh "sbt -batch ${args}" }
    else { bat "call sbt -batch ${args}" }
}
pipeline {
    agent any
    tools { jdk 'JDK17' }
    options { skipDefaultCheckout(true); disableConcurrentBuilds(); timestamps() }
    parameters {
        string(name: 'REPO_URL', defaultValue: '', description: 'Git repository URL containing this project')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch to build')
        string(name: 'NOTIFY_TO', defaultValue: '', description: 'Team email; configure SMTP before setting')
        booleanParam(name: 'FAIL_TEST', defaultValue: false, description: 'Task 2: demonstrate a failed test')
    }
    stages {
        stage('Checkout') {
            steps {
                script { if (!params.REPO_URL.trim()) { error('Set REPO_URL to your Git repository') } }
                git branch: params.GIT_BRANCH, url: params.REPO_URL
            }
        }
        stage('Compile') { steps { script { runSbt('clean compile') } } }
        stage('Static Analysis') { steps { script { runSbt('scalastyle') } } }
        stage('Unit Tests') {
            steps {
                script { runSbt(params.FAIL_TEST ? '-Ddemo.fail=true test' : 'test') }
            }
            post { always { junit '**/target/test-reports/*.xml' } }
        }
    }
    post {
        failure {
            script {
                if (params.NOTIFY_TO?.trim()) {
                    mail to: params.NOTIFY_TO, subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Build failed. Console: ${env.BUILD_URL}console"
                } else { echo 'Build FAILED. NOTIFY_TO is not configured.' }
            }
        }
    }
}
```


## Jenkinsfile.matrix толық мәтіні

```groovy
def runSbt(String args) {
    if (isUnix()) { sh "sbt -batch ${args}" }
    else { bat "call sbt -batch ${args}" }
}
pipeline {
    agent none
    options { skipDefaultCheckout(true); disableConcurrentBuilds(); timestamps() }
    parameters {
        string(name: 'REPO_URL', defaultValue: '', description: 'Git repository URL')
        string(name: 'GIT_BRANCH', defaultValue: 'main', description: 'Branch')
    }
    stages {
        stage('Matrix Testing') {
            matrix {
                axes {
                    axis { name 'JAVA_VERSION'; values 'JDK11', 'JDK17', 'JDK21' }
                    axis { name 'PROFILE'; values 'Standard', 'Cluster' }
                }
                agent any
                tools { jdk "${JAVA_VERSION}" }
                stages {
                    stage('Checkout') {
                        steps {
                            script { if (!params.REPO_URL.trim()) { error('Set REPO_URL') } }
                            git branch: params.GIT_BRANCH, url: params.REPO_URL
                        }
                    }
                    stage('Build & Test') {
                        steps {
                            echo "Java=${JAVA_VERSION}, profile=${PROFILE}"
                            script {
                                if (isUnix()) { sh 'java -version' } else { bat 'java -version' }
                                runSbt("-Diot.profile=${PROFILE} -Dakka.test.timefactor=2 clean test")
                            }
                        }
                        post { always { junit '**/target/test-reports/*.xml' } }
                    }
                }
            }
        }
    }
}
```


## Dockerfile толық мәтіні

```dockerfile
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/scala-2.13/akka-iot-service.jar /app/service.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
```


## Jenkinsfile толық мәтіні

```groovy
def runSbt(String args) {
    if (isUnix()) { sh "sbt -batch ${args}" }
    else { bat "call sbt -batch ${args}" }
}
pipeline {
    agent any
    tools { jdk 'JDK17' }
    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        skipStagesAfterUnstable()
        timestamps()
    }
    parameters {
        string(name: 'IMAGE_REPOSITORY', defaultValue: '', description: 'Docker Hub namespace/akka-iot-service')
        string(name: 'NOTIFY_TO', defaultValue: '', description: 'Team email')
    }
    stages {
        stage('Checkout') { steps { checkout scm } }
        stage('Compile') { steps { script { runSbt('clean compile') } } }
        stage('Static Analysis') { steps { script { runSbt('scalastyle') } } }
        stage('Unit Tests') {
            steps { script { runSbt('test') } }
            post { always { junit '**/target/test-reports/*.xml' } }
        }
        stage('Package') {
            when { branch 'main' }
            steps {
                script { runSbt('assembly') }
                stash name: 'docker-input', includes: 'Dockerfile,.dockerignore,target/scala-2.13/akka-iot-service.jar'
            }
        }
        stage('Package & Push Docker Image') {
            when {
                beforeAgent true
                allOf { branch 'main'; expression { currentBuild.currentResult == 'SUCCESS' } }
            }
            agent { label 'docker-linux' }
            steps {
                unstash 'docker-input'
                script {
                    if (!(params.IMAGE_REPOSITORY ==~ /[a-z0-9][a-z0-9._-]*\/[a-z0-9][a-z0-9._-]*/)) {
                        error('Set IMAGE_REPOSITORY to namespace/repository')
                    }
                    def image = docker.build("${params.IMAGE_REPOSITORY}:${env.BUILD_NUMBER}")
                    docker.withRegistry('https://index.docker.io/v1/', 'dockerhub-credentials-id') {
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                if (params.NOTIFY_TO?.trim()) {
                    mail to: params.NOTIFY_TO, subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                         body: "Build failed. Console: ${env.BUILD_URL}console"
                } else { echo 'Build FAILED. NOTIFY_TO is not configured.' }
            }
        }
    }
}
```
