// ERROR: Unresolved reference: bar
import java.io.IOException

internal class A {
    internal fun foo() {
        try {
            bar()
        } catch (e: RuntimeException) {
            e.printStackTrace() // print stack trace
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}