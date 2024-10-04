import core.Greeter
import org.jetbrains.kotlinx.spi.Service
import org.jetbrains.kotlinx.spi.loadService

@Service(Greeter::class)
object RootGreeter : Greeter {
    override fun greet() {
        println("Hello from root")
    }
}

fun main() {
    loadService<Greeter>().forEach { it.greet() }
}
