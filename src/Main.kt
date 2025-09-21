import kotlin.random.Random


open class Human(
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
    open fun move() {
        x += Random.nextDouble(-currentSpeed, currentSpeed)
        y += Random.nextDouble(-currentSpeed, currentSpeed)
        println("${fullName} ${age} лет: (${"%.1f".format(x)}, ${"%.1f".format(y)})")
    }
}

class Driver(
    fullName: String,
    age: Int,
    currentSpeed: Double,
    var Category: String,
    var Car: String,
) : Human (fullName, age, currentSpeed) {
    override fun move(){
        x+=Random.nextDouble(currentSpeed)
        println("Водитель ${fullName} ${age} лет едет на автомобиле марки ${Car} со скоростью ${currentSpeed} и обладает правами категории ${Category}, текущая позиция ${"%.1f".format(x)}")
    }
}



fun main() {
    var simulationTime = 10
    val humans = arrayOf(
        Human("Иван Иванов", 25, 2.5),
        Human("Петр Петров", 30, 3.0),
        Human("Анна Сидорова", 22, 1.8),
        Driver("Иван Иванов", 25, 110.0, "B", "Mercedes")
    )

    humans.forEach { human ->
        Thread {
            repeat(10) {

                human.move()
                Thread.sleep(1000)
            }
        }.start()
    }
}
