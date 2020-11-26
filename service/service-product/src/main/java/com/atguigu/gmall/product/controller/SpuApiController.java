package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@CrossOrigin
@RequestMapping("admin/product")
@RestController
public class SpuApiController {

    @Autowired
    SpuService spuService;


    //文件上传
    @RequestMapping("fileUpload")
    public Result fileUpload(@RequestParam("file") MultipartFile multipartFile){

        String path = SpuApiController.class.getClassLoader().getResource("tracker.conf").getPath();
        System.out.println(path);
        try {
            ClientGlobal.init(path);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        // 得到tracker
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 根据tracker获得storage
        StorageClient storageClient = new StorageClient(trackerServer, null);


        // 上传文件
        String[] jpgs = new String[0];
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            //int i = originalFilename.lastIndexOf(".");
            //originalFilename.substring(i+1);
            String filenameExtension = StringUtils.getFilenameExtension(originalFilename);
            jpgs = storageClient.upload_file(multipartFile.getBytes(), filenameExtension, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }

        StringBuffer url = new StringBuffer("http://192.168.200.128:8080");

        for (String jpg : jpgs) {
            url.append("/"+jpg);
        }
        System.out.println(url);

        return Result.ok(url);
    }


    //获取品牌属性
    @RequestMapping("{page}/{size}")
    public Result spuList(Long category3Id,
                          @PathVariable("page") Long page,
                          @PathVariable("size") Long size ) {

        IPage<SpuInfo> spuInfoPage = new Page<>();
        spuInfoPage.setPages(page);
        spuInfoPage.setSize(size);

        IPage<SpuInfo> infoIPage = spuService.spuList(spuInfoPage, category3Id);

        return Result.ok(infoIPage);
    }

    //获取销售属性
    @RequestMapping("baseSaleAttrList")
    public Result baseSaleAttrList() {

         List<BaseSaleAttr> baseSaleAttrs =  spuService.baseSaleAttrList();

        return Result.ok(baseSaleAttrs);
    }

    //添加SPU
    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {

        //调用SPU保存业务
        spuService.saveSpuInfo(spuInfo);

        return Result.ok();
    }

    //根据spuId获取销售属性
    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") String spuId) {

        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);

        return Result.ok(spuSaleAttrs);
    }

    //根据spuId获取图片
    @RequestMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") String spuId) {

        List<SpuImage> spuImages = spuService.spuImageList(spuId);
        return Result.ok(spuImages);
    }
}

