import kotlin.random.Random


class Human(
    var fullName: String,
    age: Int,
    currentSpeed: Double
) {
    var age: Int = age
        set(value) {
            if (value >= 0) field = value
            else println("Возраст не может быть отрицательным!")
        }
    var currentSpeed: Double = currentSpeed
        set(value) {
            if (value >= 0) field = value
            else println("Скорость не может быть отрицательной!")
        }

    var x: Double = 0.0
    var y: Double = 0.0
    fun move() {
        x += Random.nextDouble(-currentSpeed, currentSpeed)
        y += Random.nextDouble(-currentSpeed, currentSpeed)
        println("${fullName} ${age}: (${"%.1f".format(x)}, ${"%.1f".format(y)})")
    }
}

fun main() {
    var simulationTime = 10
    val humans = arrayOf(
        Human("Иван Иванов", 25, 2.5),
        Human("Петр Петров", 30, 3.0),
        Human("Анна Сидорова", 22, 1.8),
        Human("Мария Козлова", 28, 2.2),
        Human("Алексей Смирнов", 35, 3.5),
        Human("Елена Волкова", 26, 2.0),
        Human("Дмитрий Попов", 31, 2.8),
        Human("Ольга Васильева", 24, 1.5),
        Human("Сергей Павлов", 29, 3.2),
        Human("Наталья Семенова", 27, 2.1),
        Human("Андрей Голубев", 32, 2.9),
        Human("Татьяна Виноградова", 23, 1.7),
        Human("Михаил Орлов", 33, 3.1),
        Human("Юлия Лебедева", 26, 2.3),
        Human("Владимир Новиков", 34, 3.3),
        Human("Екатерина Морозова", 25, 2.4),
        Human("Александр Ковалев", 30, 2.7),
        Human("Ирина Зайцева", 28, 2.6),
        Human("Николай Соловьев", 31, 3.4),
        Human("Светлана Федорова", 29, 2.5),
        Human("Артем Яковлев", 27, 1.9)
    )
    while (simulationTime!=0) {
        println("============================================================")
        for (human in humans) {
            human.move()
        }
        println("============================================================")
        Thread.sleep(1000)
        simulationTime-=1
    }
}