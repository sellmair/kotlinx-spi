import core.Greeter
import org.jetbrains.kotlinx.spi.loadService
import kotlin.test.Test


class CommonTest {
    @Test
    fun testGreeting() {
        loadService<Greeter>().forEach { it.greet() }
    }
}