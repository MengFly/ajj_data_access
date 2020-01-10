package com.akxy.util;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

/**
 * @author wangp
 */
public class TaskUtil {

    private ThreadPoolExecutor fixRoundExecutor;
    private ThreadPoolExecutor fixRoundExecutor2;
    private static volatile TaskUtil instance;
    private int processorsCount;

    private TaskUtil() {
        processorsCount = Runtime.getRuntime().availableProcessors() + 1;

        this.fixRoundExecutor =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(processorsCount * 2, new TuThreadFactory("fixed"));
    }

    /**
     * 分割任务为子任务分别执行，加快任务执行速度
     *
     * @param childTaskReses 子任务列表
     * @param childTaskLogic 子任务处理逻辑
     */
    public <E> void splitTaskExec(List<List<E>> childTaskReses, BiConsumer<List<E>, Integer> childTaskLogic) {
        splitTaskExec(childTaskReses, childTaskLogic, null);
    }

    /**
     * 分割任务为子任务分别执行，加快任务执行速度
     *
     * @param childTaskReses   子任务列表
     * @param childTaskLogic   子任务处理逻辑
     * @param exceptionHandler 捕获异常后的处理逻辑
     */
    public <E> void splitTaskExec(List<List<E>> childTaskReses, BiConsumer<List<E>, Integer> childTaskLogic,
                                  BiConsumer<Integer, Exception> exceptionHandler) {
        int taskCount = childTaskReses.size();
        CountDownLatch begin = new CountDownLatch(1);
        CountDownLatch end = new CountDownLatch(taskCount);
        for (int i = 0; i < childTaskReses.size(); i++) {
            // 如果存在线程嵌套问题，那么把线程丢进Cache线程池中，防止阻塞发生
            List<E> childTaskRe = childTaskReses.get(i);
            int location = i;
            Runnable runnable = () -> {
                try {
                    begin.await();
                    childTaskLogic.accept(childTaskRe, location);
                } catch (InterruptedException e) {
                    if (exceptionHandler != null) {
                        exceptionHandler.accept(location, e);
                    }
                    e.printStackTrace();
                } finally {
                    end.countDown();
                }
            };
            getExecutor().execute(runnable);
        }
        try {
            begin.countDown();
            end.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private ThreadPoolExecutor getExecutor() {
        if (Thread.currentThread().getName().startsWith("TU-fix")) {
            if (fixRoundExecutor2 == null) {
                fixRoundExecutor2 = (ThreadPoolExecutor)
                        Executors.newFixedThreadPool(processorsCount * 2, new TuThreadFactory("2fix"));
            }
            return fixRoundExecutor2;
        }
        return fixRoundExecutor;
    }

    /**
     * 分割任务为子任务分别执行，加快任务执行速度
     *
     * @param res              主任务列表
     * @param childTaskItemNum 每个子任务要处理的长度
     * @param childTaskLogic   子任务处理逻辑
     * @param exceptionHandler 捕获异常后的处理逻辑
     */
    public <E> void splitTaskExec(List<E> res, int childTaskItemNum, BiConsumer<List<E>, Integer> childTaskLogic,
                                  BiConsumer<Integer, Exception> exceptionHandler) {
        List<List<E>> childTaskRes = AVGList.averageAssign(res, childTaskItemNum);
        splitTaskExec(childTaskRes, childTaskLogic, exceptionHandler);
    }

    public <E> void splitItemTaskExec(List<E> res, BiConsumer<E, Integer> childTaskLogic) {
        splitTaskExec(res, 1, (es, integer) -> {
            childTaskLogic.accept(es.get(0), integer);
        });
    }

    /**
     * 分割任务为子任务分别执行，加快任务执行速度
     *
     * @param res              主任务列表
     * @param childTaskItemNum 每个子任务要处理的长度
     * @param childTaskLogic   子任务处理逻辑
     */
    public <E> void splitTaskExec(List<E> res, int childTaskItemNum, BiConsumer<List<E>, Integer> childTaskLogic) {
        List<List<E>> childTaskRes = AVGList.averageAssign(res, childTaskItemNum);
        splitTaskExec(childTaskRes, childTaskLogic, null);
    }

    public static TaskUtil getInstance() {
        if (instance == null) {
            synchronized (TaskUtil.class) {
                if (instance == null) {
                    instance = new TaskUtil();
                }
            }
        }
        return instance;
    }

    private static class TuThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final static String NAME_PREFIX = "TU";
        private final String namePrefix;

        public TuThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = NAME_PREFIX + "-" + poolName + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }

    }
}
