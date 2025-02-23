package com.qust.demo15;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SyncProblemSimulator {
    private JFrame frame;
    public static JTextArea resultArea;
    private PhilosophersDinner philosophersDinner;
    private ReaderWriter readerWriter;
    private ProducerConsumer producerConsumer;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                SyncProblemSimulator window = new SyncProblemSimulator();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public SyncProblemSimulator() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("多线程同步问题模拟器");
        frame.setBounds(100, 100, 600, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        // 创建 JTextArea 并设置为不可编辑
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true); // 自动换行
        resultArea.setWrapStyleWord(true); // 按单词换行

        // 创建 JScrollPane 并将 JTextArea 添加到其中
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBounds(10, 10, 560, 280);

        // 将 JScrollPane 添加到主窗口
        frame.getContentPane().add(scrollPane);

        // 哲学家就餐问题按钮
        JButton philosopherButton = new JButton("哲学家就餐问题");
        philosopherButton.setBounds(10, 300, 150, 25);
        philosopherButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("运行哲学家就餐问题...\n");
                philosophersDinner = new PhilosophersDinner();
                philosophersDinner.start();
            }
        });
        frame.getContentPane().add(philosopherButton);

        // 读者写者问题按钮
        JButton readerWriterButton = new JButton("读者写者问题");
        readerWriterButton.setBounds(170, 300, 150, 25);
        readerWriterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("运行读者写者问题...\n");
                readerWriter = new ReaderWriter();
                readerWriter.start();
            }
        });
        frame.getContentPane().add(readerWriterButton);

        // 生产者消费者问题按钮
        JButton producerConsumerButton = new JButton("生产者消费者问题");
        producerConsumerButton.setBounds(330, 300, 150, 25);
        producerConsumerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultArea.setText("运行生产者消费者问题...\n");
                producerConsumer = new ProducerConsumer();
                producerConsumer.start();
            }
        });
        frame.getContentPane().add(producerConsumerButton);

        // 停止当前模拟按钮
        JButton stopButton = new JButton("停止当前模拟");
        stopButton.setBounds(10, 340, 150, 25);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (philosophersDinner != null) {
                    philosophersDinner.stop();
                }
                if (readerWriter != null) {
                    readerWriter.stop();
                }
                if (producerConsumer != null) {
                    producerConsumer.stop();
                }
                resultArea.append("模拟已停止。\n");
            }
        });
        frame.getContentPane().add(stopButton);

        // 退出程序按钮
        JButton exitButton = new JButton("退出程序");
        exitButton.setBounds(170, 340, 150, 25);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add(exitButton);

        frame.setVisible(true);
    }
}

// 哲学家就餐问题
class PhilosophersDinner {
    private static final int NUM_PHILOSOPHERS = 5; // 哲学家的数量，固定为5
    private final Object[] chopsticks = new Object[NUM_PHILOSOPHERS]; // 每个哲学家对应一根筷子，用Object数组表示锁
    private final Thread[] philosophers = new Thread[NUM_PHILOSOPHERS]; // 哲学家线程数组
    private volatile boolean running = true; // 控制线程运行状态的标志

    // 构造函数，初始化筷子（锁）
    public PhilosophersDinner() {
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            chopsticks[i] = new Object(); // 为每根筷子创建一个独立的锁对象
        }
    }

    // 启动哲学家线程
    public void start() {
        for (int i = 0; i < NUM_PHILOSOPHERS; i++) {
            philosophers[i] = new Thread(new Philosopher(i)); // 创建哲学家线程
            philosophers[i].start(); // 启动哲学家线程
        }
    }

    // 停止哲学家线程
    public void stop() {
        running = false; // 设置运行标志为false，通知线程停止运行
        for (Thread philosopher : philosophers) {
            philosopher.interrupt(); // 中断每个哲学家线程
        }
    }

    // 哲学家的行为逻辑
    class Philosopher implements Runnable {
        private final int id; // 哲学家的编号

        // 构造函数，传入哲学家的编号
        public Philosopher(int id) {
            this.id = id;
        }

        // 哲学家线程的运行逻辑
        @Override
        public void run() {
            while (running) { // 检查运行标志
                try {
                    Thread.sleep((int) (Math.random() * 1000)); // 哲学家思考（随机时间）
                    eat(); // 尝试吃饭
                    Thread.sleep((int) (Math.random() * 1000)); // 吃完饭后继续思考
                } catch (InterruptedException e) {
                    log("哲学家 " + id + " 被中断，退出运行");
                    break; // 如果线程被中断，退出循环
                }
            }
        }

        // 吃饭的逻辑
        private void eat() throws InterruptedException {
            int left = id; // 左边的筷子编号
            int right = (id + 1) % NUM_PHILOSOPHERS; // 右边的筷子编号（循环数组）

            // 拿起左边的筷子
            synchronized (chopsticks[left]) {
                log("哲学家 " + id + " 拿起了左边的筷子 (" + left + ")");

                // 拿起右边的筷子
                synchronized (chopsticks[right]) {
                    log("哲学家 " + id + " 拿起了右边的筷子 (" + right + ")");
                    log("哲学家 " + id + " 正在吃饭");
                    Thread.sleep((int) (Math.random() * 1000)); // 模拟吃饭时间
                    log("哲学家 " + id + " 放下了右边的筷子 (" + right + ")");
                }
                log("哲学家 " + id + " 放下了左边的筷子 (" + left + ")");
            }
        }

        // 日志记录方法
        private void log(String message) {
            SwingUtilities.invokeLater(() -> {
                SyncProblemSimulator.resultArea.append(message + "\n");
                SyncProblemSimulator.resultArea.setCaretPosition(SyncProblemSimulator.resultArea.getDocument().getLength());
            });
        }
    }
}
// 读者写者问题
class ReaderWriter {
    // 用于控制读操作的锁
    private final Object readLock = new Object();
    // 用于控制写操作的锁
    private final Object writeLock = new Object();
    // 当前正在读的读者数量
    private int readers = 0;
    // 标志是否正在写入
    private boolean writing = false;
    // 控制线程运行状态的标志
    private volatile boolean running = true;

    // 启动读者和写者线程
    public void start() {
        // 启动3个读者线程
        for (int i = 0; i < 3; i++) {
            new Thread(new Reader(i)).start();
        }
        // 启动2个写者线程
        for (int i = 0; i < 2; i++) {
            new Thread(new Writer(i)).start();
        }
    }

    // 停止所有线程
    public void stop() {
        running = false; // 设置运行标志为false，通知线程停止运行
    }

    // 读者线程的行为逻辑
    class Reader implements Runnable {
        private final int id; // 读者的编号

        // 构造函数，传入读者编号
        public Reader(int id) {
            this.id = id;
        }

        // 读者线程的运行逻辑
        @Override
        public void run() {
            while (running) { // 检查运行标志
                try {
                    Thread.sleep((int) (Math.random() * 1000)); // 读者随机等待一段时间
                    read(); // 尝试读取
                    Thread.sleep((int) (Math.random() * 1000)); // 读取完成后随机等待一段时间
                } catch (InterruptedException e) {
                    log("读者 " + id + " 被中断，退出运行");
                    break; // 如果线程被中断，退出循环
                }
            }
        }

        // 读取操作的逻辑
        private void read() {
            synchronized (readLock) {
                // 等待写者完成写入
                while (writing) {
                    try {
                        log("读者 " + id + " 等待写者完成写入...");
                        readLock.wait(); // 读者等待
                    } catch (InterruptedException e) {
                        log("读者 " + id + " 被中断，退出等待");
                        break; // 如果被中断，退出等待
                    }
                }
                // 增加读者计数
                readers++;
                log("读者 " + id + " 开始读取，当前读者数量: " + readers);
            }

            // 模拟读取操作
            log("读者 " + id + " 正在读取");

            synchronized (readLock) {
                // 减少读者计数
                readers--;
                log("读者 " + id + " 结束读取，当前读者数量: " + readers);
                // 如果没有读者在读取，通知等待的写者
                if (readers == 0) {
                    log("没有读者在读取，通知等待的写者");
                    readLock.notifyAll(); // 唤醒等待的写者
                }
            }
        }
    }

    // 写者线程的行为逻辑
    class Writer implements Runnable {
        private final int id; // 写者的编号

        // 构造函数，传入写者编号
        public Writer(int id) {
            this.id = id;
        }

        // 写者线程的运行逻辑
        @Override
        public void run() {
            while (running) { // 检查运行标志
                try {
                    Thread.sleep((int) (Math.random() * 1000)); // 写者随机等待一段时间
                    write(); // 尝试写入
                    Thread.sleep((int) (Math.random() * 1000)); // 写入完成后随机等待一段时间
                } catch (InterruptedException e) {
                    log("写者 " + id + " 被中断，退出运行");
                    break; // 如果线程被中断，退出循环
                }
            }
        }

        // 写入操作的逻辑
        private void write() {
            synchronized (writeLock) {
                // 等待所有读者完成读取
                while (readers > 0 || writing) {
                    try {
                        log("写者 " + id + " 等待读者完成读取...");
                        writeLock.wait(); // 写者等待
                    } catch (InterruptedException e) {
                        log("写者 " + id + " 被中断，退出等待");
                        break; // 如果被中断，退出等待
                    }
                }
                // 设置写入标志
                writing = true;
                log("写者 " + id + " 开始写入");
            }

            // 模拟写入操作
            log("写者 " + id + " 正在写入");

            synchronized (writeLock) {
                // 清除写入标志
                writing = false;
                log("写者 " + id + " 结束写入");
                // 通知所有等待的读者和写者
                writeLock.notifyAll(); // 唤醒等待的读者
            }
        }
    }

    // 日志记录方法
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SyncProblemSimulator.resultArea.append(message + "\n");
            SyncProblemSimulator.resultArea.setCaretPosition(SyncProblemSimulator.resultArea.getDocument().getLength());
        });
    }
}
// 生产者消费者问题
class ProducerConsumer {
    private final Object lock = new Object(); // 用于同步的锁对象
    private int count = 0; // 当前产品的数量
    private final int MAX_COUNT = 5; // 缓冲区的最大容量
    private volatile boolean running = true; // 控制线程运行状态的标志

    public void start() {
        new Thread(new Producer()).start(); // 启动一个生产者线程
        new Thread(new Consumer()).start(); // 启动一个消费者线程
    }

    public void stop() {
        running = false; // 设置运行标志为false，通知线程停止运行
    }

    class Producer implements Runnable {
        @Override
        public void run() {
            while (running) { // 检查运行标志
                try {
                    Thread.sleep((int) (Math.random() * 1000)); // 生产者随机等待一段时间
                    produce(); // 尝试生产产品
                } catch (InterruptedException e) {
                    log("生产者被中断，退出运行");
                    break; // 如果线程被中断，退出循环
                }
            }
        }

        private void produce() {
            synchronized (lock) { // 进入同步块，确保线程安全
                while (count >= MAX_COUNT) { // 如果缓冲区已满，生产者等待
                    try {
                        log("缓冲区已满，生产者等待...");
                        lock.wait(); // 生产者等待，释放锁
                    } catch (InterruptedException e) {
                        log("生产者被中断，退出等待");
                        break; // 如果被中断，退出等待
                    }
                }
                count++; // 生产一个产品，增加计数
                log("生产者生产了一个产品，当前数量: " + count);
                lock.notifyAll(); // 唤醒等待的消费者
            }
        }
    }

    class Consumer implements Runnable {
        @Override
        public void run() {
            while (running) { // 检查运行标志
                try {
                    Thread.sleep((int) (Math.random() * 1000)); // 消费者随机等待一段时间
                    consume(); // 尝试消费产品
                } catch (InterruptedException e) {
                    log("消费者被中断，退出运行");
                    break; // 如果线程被中断，退出循环
                }
            }
        }

        private void consume() {
            synchronized (lock) { // 进入同步块，确保线程安全
                while (count <= 0) { // 如果缓冲区为空，消费者等待
                    try {
                        log("缓冲区为空，消费者等待...");
                        lock.wait(); // 消费者等待，释放锁
                    } catch (InterruptedException e) {
                        log("消费者被中断，退出等待");
                        break; // 如果被中断，退出等待
                    }
                }
                if (count > 0) {
                    count--; // 消费一个产品，减少计数
                    log("消费者消费了一个产品，当前数量: " + count);
                } else {
                    log("无法消费，缓冲区为空");
                }
                lock.notifyAll(); // 唤醒等待的生产者
            }
        }
    }

    // 日志记录方法
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            SyncProblemSimulator.resultArea.append(message + "\n");
            SyncProblemSimulator.resultArea.setCaretPosition(SyncProblemSimulator.resultArea.getDocument().getLength());
        });
    }
}