package com.anikanov.paper.crawler.service;

import com.anikanov.paper.crawler.domain.SourceName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CallPermissionService {
//    private final Map<SourceName, Long> crossrefCallsBook;
//
//    public CallPermissionService() {
//        this.crossrefCallsBook = new HashMap<>();
//    }
//
//    public void callAndAwait(SourceName sourceName, ActionType actionType) throws InterruptedException {
//        switch (sourceName) {
//            default:
//                throw new IllegalArgumentException(sourceName + " must not be used in permission service");
//        }
//    }
//
//    public void registerAction(SourceName sourceName, ActionType actionType) {
//        switch (sourceName) {
//            default:
//                throw new IllegalArgumentException(sourceName + " must not be used in permission service");
//        }
//    }
//
//    public void registerCrossrefAction(SourceName actionType) {
//        synchronized (crossrefCallsBook) {
//            crossrefCallsBook.put(actionType, System.currentTimeMillis());
//        }
//    }
//
//    public void callAndAwaitCrossref(SourceName actionType) throws InterruptedException {
//        synchronized (crossrefCallsBook) {
//            final long currentTime = System.currentTimeMillis();
//            final long lastActionCallTime = Optional.ofNullable(crossrefCallsBook.get(actionType)).orElse(0L);
//            switch (actionType) {
//
//                default:
//                    throw new IllegalArgumentException(actionType + " not allowed for Binance");
//            }
//        }
//    }
}
