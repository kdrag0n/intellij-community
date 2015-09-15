internal class A// this is a primary constructor
JvmOverloads internal constructor(p: Int = 1) {
    private val v: Int

    init {
        v = 1
    } // end of primary constructor body

    // this is a secondary constructor 2
    internal constructor(s: String) : this(s.length()) {
    } // end of secondary constructor 2 body
}// this is a secondary constructor 1
// end of secondary constructor 1 body

internal class B// this constructor will disappear
(private val x: Int) // end of constructor body
{

    internal fun foo() {
    }
}