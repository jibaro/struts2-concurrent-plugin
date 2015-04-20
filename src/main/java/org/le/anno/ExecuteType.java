package org.le.anno;

/**
 * work mode.
 * SYNC:synchronized render pagelet
 * CONCURRNET: concurrent render pagelet in server,
 *          after all pagelet renderd and merge them as one html reponse to browse
 * BIGPIPE: concurrent render pagelet in server, different from concurrent mode ,
 *          bigpipe mode will flush pagelet result to brower as soon as one pagelet renderd
 */
public enum  ExecuteType {
    SYNC,CONCURRNET,BIGPIPE;
}
