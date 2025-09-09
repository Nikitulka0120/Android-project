

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

    fun move(seconds: Int) {
        var x: Double = 0.0
        var y: Double = 0.0
        repeat(seconds){

        }
    }
}

fun main() {

}