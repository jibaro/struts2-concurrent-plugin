package org.le.bean;

/**
 * pagelet in one page. every implements class must binding View annotaion.
 * for example:@View(ftlPath = "demo/one.ftl" key = "one")
 */
public interface Pipe {
    /**
     * implement business logic
     */
    void execute();
}
