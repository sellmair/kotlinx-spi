import core.Greeter
import org.jetbrains.kotlinx.spi.Service

@Service(Greeter::class, ordinal = 10)
object LibAGreeter : Greeter {
    override fun greet() {
        println("Hello from libA")
    }
}

@Service(Greeter::class, ordinal = 10)
internal object LibAInternalGreeter : Greeter {
    override fun greet() {
        println("Hello from libA internals")
    }
}
