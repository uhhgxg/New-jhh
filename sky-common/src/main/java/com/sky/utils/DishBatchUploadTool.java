//package com.sky.utils;
//
//import com.aliyun.oss.OSS;
//import com.aliyun.oss.OSSClientBuilder;
//import com.aliyun.oss.model.OSSObjectSummary;
//import com.aliyun.oss.model.ObjectListing;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.annotation.PostConstruct;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Slf4j
//@Component
//public class DishBatchUploadTool {
//
//    @Value("${sky.alioss.endpoint}")
//    private String endpoint;
//    @Value("${sky.alioss.access-key-id}")
//    private String accessKeyId;
//    @Value("${sky.alioss.access-key-secret}")
//    private String accessKeySecret;
//    @Value("${sky.alioss.bucket-name}")
//    private String bucketName;
//
//    private static final String LOCAL_IMAGE_FOLDER = "D:/BaiduNetdiskDownload/资料，/资料/day03/图片资源/";
//    private static final String OSS_IMAGE_PREFIX = "图片资源/";
//
//    private OSS ossClient;
//    private String ossBaseUrl;
//
//    private JdbcTemplate jdbcTemplate;
//
//    public DishBatchUploadTool(JdbcTemplate jdbcTemplate) {
//        this.jdbcTemplate = jdbcTemplate;
//    }
//
//    @PostConstruct
//    public void init() {
//        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
//        ossBaseUrl = "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + OSS_IMAGE_PREFIX;
//        log.info("批量上传工具初始化完成，OSS基础URL：{}", ossBaseUrl);
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void batchUploadAndUpdateDishImages() {
//        File folder = new File(LOCAL_IMAGE_FOLDER);
//        if (!folder.exists() || !folder.isDirectory()) {
//            log.error("本地图片文件夹不存在，请检查路径：{}", LOCAL_IMAGE_FOLDER);
//            return;
//        }
//        File[] localImages = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png")
//                || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg"));
//        if (localImages == null || localImages.length == 0) {
//            log.warn("本地文件夹中未找到任何图片文件，终止批量上传");
//            return;
//        }
//        log.info("读取到本地图片文件共 {} 个", localImages.length);
//
//        List<Map<String, Object>> dishList = jdbcTemplate.queryForList(
//                "SELECT id FROM dish ORDER BY id ASC");
//        if (dishList.isEmpty()) {
//            log.warn("数据库dish表无菜品数据，终止批量更新");
//            return;
//        }
//        log.info("数据库中读取到 {} 条菜品数据", dishList.size());
//
//        int count = Math.min(localImages.length, dishList.size());
//        for (int i = 0; i < count; i++) {
//            File localImage = localImages[i];
//            Map<String, Object> dish = dishList.get(i);
//            Long dishId = ((Number) dish.get("id")).longValue();
//
//            try {
//                String originalFileName = localImage.getName();
//                String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
//                String objectName = UUID.randomUUID() + extension;
//                String ossKey = OSS_IMAGE_PREFIX + objectName;
//
//                try (FileInputStream fis = new FileInputStream(localImage)) {
//                    ossClient.putObject(bucketName, ossKey, fis);
//                }
//
//                String fullImageUrl = ossBaseUrl + objectName;
//                log.info("dish[id={}] 上传完成：{} → {}", dishId, originalFileName, fullImageUrl);
//
//                int affectedRows = jdbcTemplate.update(
//                        "UPDATE dish SET image = ? WHERE id = ?",
//                        fullImageUrl, dishId
//                );
//
//                if (affectedRows > 0) {
//                    log.info("dish[id={}] 图片URL更新成功", dishId);
//                } else {
//                    log.warn("dish[id={}] 无匹配记录，更新失败", dishId);
//                }
//
//            } catch (IOException e) {
//                log.error("处理dish[id={}] 图片失败：{}", dishId, e.getMessage(), e);
//            }
//        }
//
//        log.info("===== 所有菜品图片批量上传 + 更新完成 =====");
//    }
//
//    @Transactional(rollbackFor = Exception.class)
//    public void updateDishImagesFromOSS() {
//        log.info("开始从OSS云端获取图片并更新数据库...");
//
//        try {
//            ObjectListing objectListing = ossClient.listObjects(bucketName, OSS_IMAGE_PREFIX);
//            List<OSSObjectSummary> objectSummaries = objectListing.getObjectSummaries();
//
//            if (objectSummaries == null || objectSummaries.isEmpty()) {
//                log.warn("OSS中未找到图片文件，终止更新");
//                return;
//            }
//
//            log.info("OSS中找到 {} 个图片文件", objectSummaries.size());
//
//            List<Map<String, Object>> dishList = jdbcTemplate.queryForList(
//                    "SELECT id FROM dish ORDER BY id ASC");
//            if (dishList.isEmpty()) {
//                log.warn("数据库dish表无菜品数据，终止批量更新");
//                return;
//            }
//            log.info("数据库中读取到 {} 条菜品数据", dishList.size());
//
//            int count = Math.min(objectSummaries.size(), dishList.size());
//
//            for (int i = 0; i < count; i++) {
//                OSSObjectSummary objectSummary = objectSummaries.get(i);
//                Map<String, Object> dish = dishList.get(i);
//                Long dishId = ((Number) dish.get("id")).longValue();
//
//                String objectKey = objectSummary.getKey();
//                String objectName = objectKey.replace(OSS_IMAGE_PREFIX, "");
//                String imageUrl = ossBaseUrl + objectName;
//
//                int affectedRows = jdbcTemplate.update(
//                        "UPDATE dish SET image = ? WHERE id = ?",
//                        imageUrl, dishId
//                );
//
//                if (affectedRows > 0) {
//                    log.info("dish[id={}] 更新成功：{}", dishId, imageUrl);
//                } else {
//                    log.warn("dish[id={}] 更新失败", dishId);
//                }
//            }
//
//            log.info("===== 从OSS云端更新菜品图片完成，共更新 {} 条记录 =====", count);
//
//        } catch (Exception e) {
//            log.error("从OSS更新菜品图片失败：{}", e.getMessage(), e);
//        }
//    }
//
//    public void close() {
//        if (ossClient != null) {
//            ossClient.shutdown();
//            log.info("OSS客户端已关闭");
//        }
//    }
//}