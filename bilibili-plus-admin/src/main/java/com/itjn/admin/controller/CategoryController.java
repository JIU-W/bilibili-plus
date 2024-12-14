package com.itjn.admin.controller;

import com.itjn.entity.po.CategoryInfo;
import com.itjn.entity.query.CategoryInfoQuery;
import com.itjn.entity.vo.ResponseVO;
import com.itjn.service.CategoryInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/category")
@Validated
public class CategoryController extends ABaseController{

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);

    @Resource
    private CategoryInfoService categoryInfoService;

    /**
     *
     * @param categoryInfo
     * @return
     */
    @RequestMapping("/loadCategory")
    public ResponseVO loadAllCategory(CategoryInfoQuery categoryInfo) {
        categoryInfo.setOrderBy("sort asc");
        categoryInfo.setConvert2Tree(true);
        List<CategoryInfo> categoryInfoList = categoryInfoService.findListByParam(categoryInfo);
        return getSuccessResponseVO(categoryInfoList);
    }







}
