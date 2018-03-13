// Скрипт пайплайна в декларативном стиле обязан начинаться с блока pipeline
pipeline {

    // Далее могут определены некорые блоки описывающие:
    // метку агента, на котором будет проводится выполнения всех этапов
    // конфигурацию инструментов задачи, например, версия maven или jdk
    agent {
        // блок может быть опциально опеределен и содержать метку агента.
        // если в разных этапах сборки используется РАЗНЫЕ агенты - необходимо задать значени agent none
        // в этом случае блок agent становится обязательным в каждом stage
        // Документация: https://jenkins.io/doc/book/pipeline/syntax/#agent
        label ""
    }

    // Описание версий используемых инструментов. Опциональный блок. Если указать в этом блоке версии tools по умолчанию
    // можно указать в каждом stage при необходимости
    // Документация: https://jenkins.io/doc/book/pipeline/syntax/#tools
    tools {
        jdk "jdk8"
        maven "mvn3.3.8"
    }

    environment {

        // Нельзя использовать плейсхолдинг переменных вида OTHER = "\${FOO}baz", будет работать как OTHER=nullbaz
        // Все переменные объявленные тут являются СТРОКОВЫМИ контстантами. объявление типизированных объектов не предусмотрено
        // Переменные доступны далее по коду и могут быть вызваны как env.ИМЯПЕРЕМЕННОЙ.
        // Кроме того, они также доступны как переменные откружения операционной системы
        // Можно вывести их Windows: echo %FOO% или linux: echo "FOO"
        // https://issues.jenkins-ci.org/browse/JENKINS-41748
        FOO = "BAR"
    }
    options {
        // Необязательная секция - описание дополнииельных настроек задания:
        // timeout - завершение задания, если время выполнения задания превысит указанное значение
        // Докуметация: https://jenkins.io/doc/book/pipeline/syntax/#options
        timeout(time: 1, unit: 'HOURS')

    }
    parameters {
        // использование значений параметров:  sh "echo ${params.DEBUG_BUILD}"
        booleanParam(name: 'DEBUG_BUILD', defaultValue: true, description: '')
        string(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
        choice(choices: 'US-EAST-1\nUS-WEST-2', description: 'What AWS region?', name: 'region')
        password(name: 'SUPER-SECRET-PASS', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
    }
    trigers {

    }
    stages {
        // At least one stage is required.
        stage("first stage") {
            // Every stage must have a steps block containing at least one step.
            steps {
                timeout(time: true, uint: 'MINUTES') {
                    echo "We're not doing anything particularly special here."
                    sh "mvn -version"
                }
            }

            // Post can be used both on individual stages and for the entire build.
            post {
                success {
                    echo "Only when we haven't failed running the first stage"
                }

                failure {
                    echo "Only when we fail running the first stage."
                }
                always {
                    echo "Only when we fail running the first stage."
                }
            }
        }

        stage('second stage') {
            // You can override tools, environment and agent on each stage if you want.
            tools {
                // Here, we're overriding the original maven tool with a different
                // version.
                maven "mvn3.3.9"
            }

            steps {
                echo "This time, the Maven version should be 3.3.9"
                sh "mvn -version"
            }
        }

        stage('third stage') {
            steps {
                parallel(one: {
                    echo "I'm on the first branch!"
                },
                        two: {
                            echo "I'm on the second branch!"
                        },
                        three: {
                            echo "I'm on the third branch!"
                            echo "But you probably guessed that already."
                        })
            }
        }
        stage('Параллельные stage')
                {
                    parallel {
                        stage('1 Paralell stage') {
                            agent { label params.AGENT_WINDOWS }
                            steps {
                                sh "echo 123"
                            }
                        }
                        stage('2 Paralell stage') {
                            agent { label env.AGENT_LINUX }
                            steps {
                                sh "echo 321"
                            }
                        }
                    }
                }
        stage('Пользовательский ввод')
                {
                    steps {
                        input {
                            message "Should we continue?"
                            ok "Yes, we should."
                            submitter "alice,bob"
                            parameters {
                                password(name: 'PERSON', defaultValue: 'Mr Jenkins', description: 'Who should I say hello to?')
                            }
                        }
                    }
                }
        stage('Условия')
                {
                    when {
                        branch 'production'
                    }
                    steps {
                        sh "echo 333"
                    }
                }
        stage('Бракодельство')
                {
                    steps {
                        script {

                            def CoolVar = new Object.Poof();
                        }
                    }
                }
    }

    post {
        // Always runs. And it runs before any of the other post conditions.
        always {
            // Let's wipe out the workspace before we finish!
            deleteDir()
        }

        success {
            mail(from: "bob@example.com",
                    to: "steve@example.com",
                    subject: "That build passed.",
                    body: "Nothing to see here")
        }

        failure {
            mail(from: "bob@example.com",
                    to: "steve@example.com",
                    subject: "That build failed!",
                    body: "Nothing to see here")
        }
    }
}
