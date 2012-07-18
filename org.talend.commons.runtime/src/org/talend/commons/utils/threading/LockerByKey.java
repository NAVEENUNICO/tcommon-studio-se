// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.utils.threading;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.talend.commons.utils.StringUtils;

/**
 * This class is useful to lock some part of code from the provided key.
 * 
 * This class has the same behaviour that <code>java.util.concurrent.locks.ReentrantLock</code> except additionally it
 * expect keys to lock the parts of code.
 * 
 * It uses internally a <code>java.util.concurrent.ConcurrentHashMap</code> to store locks from keys <code>KP</code> and
 * the <code>java.util.concurrent.locks.ReentrantLock</code> as properties of a value wrapper. <br/>
 * 
 * @see java.util.concurrent.locks.ReentrantLock
 * 
 * @param <KP> type of the key
 */
public class LockerByKey<KP> {

    private static Logger log = Logger.getLogger(LockerByKey.class);

    ConcurrentHashMap<InternalKeyLock<KP>, LockerValue<KP>> mapKeyLockToValueLock = new ConcurrentHashMap<InternalKeyLock<KP>, LockerValue<KP>>();

    private final Object lockAllOperations = new Object();

    private AtomicInteger counter = new AtomicInteger();

    private AtomicInteger runningOperations = new AtomicInteger();

    private int cleanPeriod = 500;

    /**
     * 
     * LockerByKey class.<br/>
     * 
     * @param <IKP> key
     */
    class InternalKeyLock<IKP> {

        private IKP key;

        public InternalKeyLock() {
        }

        /**
         * InternalKeyLock constructor comment.
         * 
         * @param key2
         */
        public InternalKeyLock(IKP key) {
            this.key = key;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.key == null) ? 0 : this.key.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final InternalKeyLock other = (InternalKeyLock) obj;
            if (this.key == null) {
                if (other.key != null) {
                    return false;
                }
            } else if (!this.key.equals(other.key)) {
                return false;
            }
            return true;
        }

        public void setKey(IKP key) {
            this.key = key;
        }

        public String toString() {
            return StringUtils.replacePrms(InternalKeyLock.class.getSimpleName() + ": key={0}", key); //$NON-NLS-1$
        }
    }

    class CustomReentrantLock extends ReentrantLock {

        private static final long serialVersionUID = 3730576759454516775L;

        public CustomReentrantLock() {
            super();
        }

        public CustomReentrantLock(boolean fair) {
            super(fair);
        }

        /* (non-Javadoc)
         * @see java.util.concurrent.locks.ReentrantLock#getQueuedThreads()
         */
        @Override
        protected Collection<Thread> getQueuedThreads() {
            return super.getQueuedThreads();
        }
        
    }
    
    /**
     * 
     * LockerValue.<br/>
     * 
     * @param <VKP> key
     */
    public class LockerValue<VKP> {

        private CustomReentrantLock lock;

        private VKP key;

        /**
         * LockerValue constructor.
         * 
         * @param thread
         * @param contextInfo
         * @param fair
         */
        public LockerValue(VKP key, boolean fair) {
            this.lock = new CustomReentrantLock(fair);
            this.key = key;
        }

        public String toString() {
            return StringUtils.replacePrms("LockerValue: key={0}, lock={1}", String.valueOf(key), lock.toString()); //$NON-NLS-1$
        }

        /**
         * Getter for key.
         * 
         * @return the key
         */
        public VKP getKey() {
            return key;
        }

        /**
         * Getter for lock.
         * 
         * @return the lock
         */
        public CustomReentrantLock getLock() {
            return lock;
        }

    }

    private boolean fair = true;

    private volatile boolean blockAllOperations;

    private volatile boolean shuttingDown;

    private volatile boolean stopped;

    /**
     * LockerByKey constructor.
     */
    public LockerByKey() {
        super();
    }

    /**
     * 
     * Constructor LockerByKey.
     * 
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public LockerByKey(boolean fair) {
        super();
        this.fair = fair;
    }

    /**
     * 
     * Constructor LockerByKey.
     * 
     * @param fair {@code true} if this lock should use a fair ordering policy
     * @param cleanPeriod in number of operations, it means that an automatic clean will be done for each
     * <code>cleanPeriod</code> number of unlock operation.
     */
    public LockerByKey(boolean fair, int cleanPeriod) {
        super();
        this.fair = fair;
        if (cleanPeriod <= 0) {
            throw new IllegalArgumentException("The cleanPeriod value has to be greater than 0");
        }
        this.cleanPeriod = cleanPeriod;
    }

    /**
     * Method "isLocked".
     * 
     * @param key
     * @return true if any thread holds this lock and false otherwise
     * 
     * @see java.util.concurrent.locks.ReentrantLock#isLocked()
     */
    public boolean isLocked(KP key) {
        checkKey(key);
        LockerValue<KP> locker = getLocker(key);
        return locker != null && locker.getLock().isLocked();
    }

    /**
     * 
     * Method "lockInterruptibly".
     * 
     * @param key
     * @throws InterruptedException
     * @see java.util.concurrent.locks.ReentrantLock#lockInterruptibly()
     */
    public void lockInterruptibly(KP key) throws InterruptedException {
        checkStopped();
        checkKey(key);
        blockOperationIfRequired();
        LockerValue<KP> locker = prepareInternalLock(key);
        locker.getLock().lockInterruptibly();
    }

    private void checkStopped() {
        if (stopped || shuttingDown) {
            throw new IllegalStateException("This locker is already stopped or is shutting down !");
        }
    }

    /**
     * Method "check". Check if the key is not null.
     * 
     * @param key
     */
    private void checkKey(KP key) {
        if (key == null) {
            throw new IllegalArgumentException("key can't be null"); //$NON-NLS-1$
        }
    }

    /**
     * Method "tryLock".
     * 
     * @param key
     * @return true if the lock was free and was acquired by the current thread, or the lock was already held by the
     * current thread; and false if the waiting time elapsed before the lock could be acquired
     * @throws InterruptedException
     * @throws IllegalArgumentException if bean is null
     * @see java.util.concurrent.locks.ReentrantLock#tryLock()
     */
    public boolean tryLock(KP key) throws InterruptedException {
        if (stopped || shuttingDown) {
            return false;
        }
        checkKey(key);
        blockOperationIfRequired();
        LockerValue<KP> locker = prepareInternalLock(key);
        return locker.getLock().tryLock();
    }

    /**
     * Method "tryLock".
     * 
     * @param key
     * @param timeout the time to wait for the lock in milliseconds
     * @return true if the lock was free and was acquired by the current thread, or the lock was already held by the
     * current thread; and false if the waiting time elapsed before the lock could be acquired
     * @throws InterruptedException
     * @throws IllegalArgumentException if bean is null
     * @see java.util.concurrent.locks.ReentrantLock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    public boolean tryLock(KP key, long timeout) throws InterruptedException {
        return tryLock(key, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Method "tryLock".
     * 
     * @param key
     * @param timeout the time to wait for the lock
     * @param unit the time unit of the timeout argument
     * @return true if the lock was free and was acquired by the current thread, or the lock was already held by the
     * current thread; and false if the waiting time elapsed before the lock could be acquired
     * @throws InterruptedException
     * @throws IllegalArgumentException if bean is null
     * 
     * @see java.util.concurrent.locks.ReentrantLock#tryLock(long, java.util.concurrent.TimeUnit)
     */
    public boolean tryLock(KP key, long timeout, TimeUnit unit) throws InterruptedException {
        checkStopped();
        checkKey(key);
        blockOperationIfRequired();
        LockerValue<KP> locker = prepareInternalLock(key);
        return locker.getLock().tryLock(timeout, unit);
    }

    private LockerValue<KP> prepareInternalLock(KP key) throws InterruptedException {
        incrementRunningOperations();
        InternalKeyLock<KP> internalKeyLock = new InternalKeyLock<KP>(key);
        LockerValue<KP> lockerValue = new LockerValue<KP>(key, fair);
        LockerValue<KP> previousLockerValue = null;
        previousLockerValue = mapKeyLockToValueLock.putIfAbsent(internalKeyLock, lockerValue);
        if (previousLockerValue != null) {
            lockerValue = previousLockerValue;
        }
        decrementRunningOperations();
        interruptIfStopping();
        return lockerValue;
    }

    private void interruptIfStopping() throws InterruptedException {
        if (shuttingDown) {
            throw new InterruptedException("This LockerByKey is shutting down...");
        }
    }

    /**
     * Method "unlock". Unlock the operations with the provided key.
     * 
     * @param key
     * @return true if the key has been found to release the lock; and false otherwise
     * @see java.util.concurrent.locks.ReentrantLock#unlock()
     */
    public boolean unlock(KP key) {
        cleanAccordingOperations();
        checkKey(key);
        blockOperationIfRequired();
        incrementRunningOperations();
        LockerValue<KP> lockerValue = getLocker(key);
        boolean returnValue = false;
        if (lockerValue != null) {
            lockerValue.getLock().unlock();
            returnValue = true;
        }
        decrementRunningOperations();
        return returnValue;
    }

    private void cleanAccordingOperations() {
        synchronized (lockAllOperations) {
            if (cleanPeriod > 0 && counter.incrementAndGet() % cleanPeriod == 0) {
                clean();
            }
        }
    }

    /**
     * 
     * Method "clean".
     * 
     * Clean the map which contains the lock wrappers.
     * 
     * Removed lock wrappers are these where lock is not locked by a thread and no one thread is waiting to obtain the
     * lock.
     * 
     * The default clean will do an automatic clean all 1000 unlock operation, you can disable or change this value from
     * the constructor.
     */
    private void clean() {
        synchronized (lockAllOperations) {
            waitForRunningOperationsEnded();
            Collection<LockerValue<KP>> values = mapKeyLockToValueLock.values();
            log.trace("Cleaning " + values.size() + " keys ...");
            System.out.println("Cleaning " + values.size() + " keys ...");
            InternalKeyLock<KP> internalKeyLock = new InternalKeyLock<KP>();
            for (LockerValue<KP> lockerValue : values) {
                ReentrantLock lock = lockerValue.getLock();
                if (!lock.hasQueuedThreads() && !lock.isLocked()) {
                    internalKeyLock.setKey(lockerValue.getKey());
                    mapKeyLockToValueLock.remove(internalKeyLock);
                }
            }
            resumeAllOperations();
        }
    }

    private void waitForRunningOperationsEnded() {
        blockAllOperations();
        while (runningOperations.get() > 0) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    private void resumeAllOperations() {
        this.blockAllOperations = false;
        lockAllOperations.notifyAll();
    }

    private void blockAllOperations() {
        this.blockAllOperations = true;
    }

    private void blockOperationIfRequired() {
        if (blockAllOperations) {
            try {
                synchronized (lockAllOperations) {
                    lockAllOperations.wait();
                }
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private void decrementRunningOperations() {
        runningOperations.decrementAndGet();
    }

    private void incrementRunningOperations() {
        runningOperations.incrementAndGet();
    }

    /**
     * Get locker.
     * 
     * @param bean
     * @return locker value.
     */
    public LockerValue<KP> getLocker(KP key) {
        checkKey(key);
        InternalKeyLock<KP> internalKeyLock = new InternalKeyLock<KP>(key);
        return mapKeyLockToValueLock.get(internalKeyLock);
    }

    public synchronized void shutdown() {
        shuttingDown = true;
        waitForRunningOperationsEnded();
        Collection<LockerValue<KP>> values = mapKeyLockToValueLock.values();
        for (LockerValue<KP> lockerValue : values) {
            Collection<Thread> queuedThreads = lockerValue.getLock().getQueuedThreads();
            for (Thread thread : queuedThreads) {
                thread.interrupt();
            }
        }
        clean();
        stopped = true;
    }

    public String toString() {
        return "LockerByKey:" + super.toString();
    }

    /**
     * Getter for cleanFrequency.
     * 
     * @return the cleanFrequency
     */
    public int getCleanPeriod() {
        return cleanPeriod;
    }

}
