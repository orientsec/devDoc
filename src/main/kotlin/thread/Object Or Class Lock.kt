package thread

/**
 * Product: demo
 * Package: thread
 * Time: 2017/11/24 13:29
 * Author: Fredric
 * coding is art not science
 */

/**
 * 代工生产线
 */
open class Workshop {
    var price: String = ""
    open fun make(brand: String) {
        if (brand == "宝马") {
            price = "100万"
            println("宝马生产好了")
            Thread.sleep(2000)
        } else {
            price = "10万"
            println("宝骏生产好了")
        }
        println("$brand 价格 $price")
    }
}

class WorkshopV2 : Workshop() {
    @Synchronized
    override fun make(brand: String) {
        super.make(brand)
    }
}

class BmwFactory(private val workshop: Workshop) : Runnable {
    override fun run() {
        workshop.make("宝马")
    }
}

class BjFactory(private val workshop: Workshop) : Runnable {
    override fun run() {
        workshop.make("宝骏")
    }
}

/**
 * 非线程安全的车间
 */
private fun test1() {
    val workshop = Workshop()
    Thread(BmwFactory(workshop)).start()
    Thread(BjFactory(workshop)).start()
}

/**
 * 线程安全的车间
 */
private fun test2() {
    val workshop = WorkshopV2()
    Thread(BmwFactory(workshop)).start()
    Thread(BjFactory(workshop)).start()
}

class Tree {
    var sign = ""

    fun mark(sign: String) {
        this.sign = sign
    }

    fun identify() {
        println("这是$sign 撒的尿")
    }
}

open class Puppy(private val name: String, protected val tree: Tree) : Runnable {
    override fun run() {
        pin()
    }

    @Synchronized
    open fun pin() {
        tree.mark(name)
        if (name == "泰迪") Thread.sleep(2000)
        tree.identify()
    }
}

/**
 * 不同对象，对象锁失效
 */

private fun test3() {
    val tree = Tree()
    val bixiong = Puppy("比熊", tree)
    val taidi = Puppy("泰迪", tree)
    Thread(taidi).start()
    Thread(bixiong).start()
}

/**
 * 类锁
 */
class PuppyV2(name: String, tree: Tree) : Puppy(name, tree) {
    override fun pin() {
        synchronized(Tree::class.java) {
            super.pin()
        }
    }
}

/**
 * 类锁对所有对象生效
 */
private fun test4() {
    val tree = Tree()
    val bixiong = PuppyV2("比熊", tree)
    val taidi = PuppyV2("泰迪", tree)
    Thread(taidi).start()
    Thread(bixiong).start()
}

/**
 * 共用对象锁
 */
class PuppyV3(name: String, tree: Tree) : Puppy(name, tree) {
    override fun pin() {
        synchronized(tree) {
            super.pin()
        }
    }
}

private fun test5() {
    val tree = Tree()
    val bixiong = PuppyV3("比熊", tree)
    val taidi = PuppyV3("泰迪", tree)
    Thread(taidi).start()
    Thread(bixiong).start()
}

fun main(args: Array<String>) {
    if (args.isEmpty()) return
    when (args[0]) {
        "1" -> test1()
        "2" -> test2()
        "3" -> test3()
        "4" -> test4()
        "5" -> test5()
    }
}