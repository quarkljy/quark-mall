package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specClient;

    @Autowired
    private TemplateEngine templateEngine;
    @Value("${page.path}")
    private String path;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        // 查询skus
        List<Sku> skus = spu.getSkus();
        // 查询详情
        SpuDetail detail = spu.getSpuDetail();
        // 查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        // 查询商品分类
        List<Long> idList = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<Category> categories = categoryClient.queryCategoryListByIds(idList);
        // 查询规格参数
        List<SpecGroup> specs = specClient.querySpecsByCid(spu.getCid3());
        //存入session
        model.put("title", spu.getTitle());
        model.put("subTitle", spu.getSubTitle());
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs", specs);

        createHtml(spuId, model);

        return model;
    }

    public void createHtml(Long spuId, Map<String, Object> map) {
        // 上下文
        Context context = new Context();
        context.setVariables(map);
        // 输出流
        File dest = getDestFile(spuId);
        if (dest.exists()) {
            dest.delete();
        }
        try {
            PrintWriter writer = new PrintWriter(dest, "UTF-8");
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("[静态页服务 ] 生成静态页异常!", e);
        }
    }

    public void createHtml(Long spuId) {
        // 上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        // 输出流
        File dest = getDestFile(spuId);
        if (dest.exists()) {
            dest.delete();
        }
        try {
            PrintWriter writer = new PrintWriter(dest, "UTF-8");
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("[静态页服务 ] 生成静态页异常!", e);
        }
    }

    private File getDestFile(Long spuId) {
        return new File(path, spuId + ".html");
    }

    public void deleteHtml(Long spuId) {
        File dest = getDestFile(spuId);
        if (dest.exists()) {
            dest.delete();
        }
    }
}
