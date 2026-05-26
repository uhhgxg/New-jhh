//package com.sky.controller.admin;
//import com.sky.utils.DishBatchUploadTool;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RunBatchUpload implements CommandLineRunner {
//
//    private final DishBatchUploadTool dishBatchUploadTool;
//
//    public RunBatchUpload(DishBatchUploadTool dishBatchUploadTool) {
//        this.dishBatchUploadTool = dishBatchUploadTool;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        dishBatchUploadTool.updateDishImagesFromOSS();
//    }
//}