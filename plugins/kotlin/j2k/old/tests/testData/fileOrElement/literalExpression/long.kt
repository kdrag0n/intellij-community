internal class A {
    private var l1 = 1L
    private var l2: Long = 1
    private val l3 = 1L
    private var l4: Long = -1
    private val l5 = 123456789101112
    private val l6 = -123456789101112
    private val l7 = +1
    private val l8 = +1L

    internal fun foo1(l: Long) {
    }

    internal fun foo2(l: Long?) {
    }

    internal fun bar() {
        foo1(1)
        foo1(1L)
        foo2(1L)
        foo1(-1)
        l1 = 10
        l2 = 10L
        l4 = 10
    }
}
