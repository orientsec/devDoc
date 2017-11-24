package thread;

/**
 * Product: demo
 * Package: thread
 * Time: 2017/11/24 18:15
 * Author: Fredric
 * coding is art not science
 */
public class LockDemo {
    private static int count;

    private synchronized void countA() {
        count++;
    }

    private synchronized void countB() {
        count++;
    }

    private static synchronized void countC() {
        count++;
    }

    private static synchronized void countD() {
        count++;
    }

    static class RunnableA implements Runnable {
        private LockDemo lockDemo;

        RunnableA(LockDemo lockDemo) {
            this.lockDemo = lockDemo;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                lockDemo.countA();
            }
        }
    }

    static class RunnableB implements Runnable {

        private LockDemo lockDemo;

        RunnableB(LockDemo lockDemo) {
            this.lockDemo = lockDemo;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                lockDemo.countB();
            }
        }
    }

    static class RunnableC implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                LockDemo.countC();
            }
        }
    }

    static class RunnableD implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                LockDemo.countD();
            }
        }
    }

    public static void main(String... args) throws InterruptedException {
        LockDemo lockDemo = new LockDemo();
        LockDemo lockDemo2 = new LockDemo();
        Runnable runnableA = new RunnableA(lockDemo);
        Runnable runnableB = new RunnableB(lockDemo);
        Runnable runnableB2 = new RunnableB(lockDemo2);
        Runnable runnableC = new RunnableC();
        Runnable runnableD = new RunnableD();


        Thread threadA = new Thread(runnableA);
        Thread threadB = new Thread(runnableB);
        threadA.start();
        threadB.start();
        threadA.join();
        threadB.join();
        System.out.println("count:" + count);
        count = 0;
        Thread.sleep(3000);

        Thread threadC = new Thread(runnableC);
        Thread threadD = new Thread(runnableD);
        threadC.start();
        threadD.start();
        threadC.join();
        threadD.join();
        System.out.println("count:" + count);
        count = 0;
        Thread.sleep(3000);


        Thread threadA2 = new Thread(runnableA);
        Thread threadB2 = new Thread(runnableB2);
        threadA2.start();
        threadB2.start();
        threadA2.join();
        threadB2.join();
        System.out.println("count:" + count);
        count = 0;
        Thread.sleep(3000);

        Thread threadA3 = new Thread(runnableA);
        Thread threadC2 = new Thread(runnableC);
        threadA3.start();
        threadC2.start();
        threadA3.join();
        threadC2.join();
        System.out.println("count:" + count);
        count = 0;
        Thread.sleep(3000);
    }
}
