import core.Greeter
import org.jetbrains.kotlinx.spi.Service

@Service(Greeter::class)
object LibB : Greeter {
    override fun greet() {
        println("Hello from **LIB B**")
    }
}