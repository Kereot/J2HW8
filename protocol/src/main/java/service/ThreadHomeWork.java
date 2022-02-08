package service;

public class ThreadHomeWork {
    private final Object mon = new Object();
    private volatile char l = 'A';

    public static void main(String[] args) {
        ThreadHomeWork t = new ThreadHomeWork();
        new Thread(t::printA).start();
        new Thread(t::printB).start();
        new Thread(t::printC).start();
    }


    private void printA() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (l != 'A') {
                        mon.wait();
                    }
                    System.out.print(l);
                    l = 'B';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printB() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (l != 'B') {
                        mon.wait();
                    }
                    System.out.print(l);
                    l = 'C';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void printC() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (l != 'C') {
                        mon.wait();
                    }
                    System.out.println(l);
                    l = 'A';
                    mon.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
