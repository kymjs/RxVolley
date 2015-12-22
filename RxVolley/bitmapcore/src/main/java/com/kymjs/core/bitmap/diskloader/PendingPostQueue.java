package com.kymjs.core.bitmap.diskloader;

final class PendingPostQueue {

    private PendingPost head; //待发送对象队列头节点
    private PendingPost tail;//待发送对象队列尾节点

    /**
     * 入队
     */
    synchronized void enqueue(PendingPost pendingPost) {
        if (pendingPost == null) {
            throw new NullPointerException("null cannot be enqueued");
        }
        if (tail != null) {
            tail.next = pendingPost;
            tail = pendingPost;
        } else if (head == null) {
            head = tail = pendingPost;
        } else {
            throw new IllegalStateException("Head present, but no tail");
        }
        notifyAll();
    }

    /**
     * 取队列头节点的待发送对象
     */
    synchronized PendingPost poll() {
        PendingPost pendingPost = head;
        if (head != null) {
            head = head.next;
            if (head == null) {
                tail = null;
            }
        }
        return pendingPost;
    }

    /**
     * 取待发送对象队列头节点的待发送对象
     */
    synchronized PendingPost poll(int maxMillisToWait) throws InterruptedException {
        if (head == null) {
            wait(maxMillisToWait);
        }
        return poll();
    }
}
